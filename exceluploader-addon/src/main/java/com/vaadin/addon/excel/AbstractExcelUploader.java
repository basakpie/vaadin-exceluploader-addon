package com.vaadin.addon.excel;

import com.vaadin.server.Page;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.SucceededEvent;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Created by basakpie on 2016-10-10.
 */
@SuppressWarnings({"rawtypes", "unchecked", "unused", "resource"})
public abstract class AbstractExcelUploader<T> implements Upload.Receiver, Upload.SucceededListener {

	private static final long serialVersionUID = 1L;

	private Ext ext;
	private File file;

	private final Class<? super T> type;
	private final Map<String, Field> fieldMap;

	private int firstRow;

	private int sheetAt;

	private String sheetName = "";

	private boolean skipEmptyRows = false;

	public void setSheetAt(int index) {
		if(!sheetName.equals("")) {
			throw new IllegalArgumentException("already defined sheetName (" + sheetName + ")");
		} else if(index<0) {
			throw new IllegalArgumentException("index cannot be negative.");
		}
		this.sheetAt = index;
	}

	public void setSheetName(String name) {
		if(sheetAt!=0) {
			throw new IllegalArgumentException("already defined sheetAt (" + sheetAt + ")");
		}
		this.sheetName = name;
	}

	public void setFirstRow(int row) {
		if(row<0) {
			throw new IllegalArgumentException("row cannot be negative.");
		}
		this.firstRow = row;
	}

	public void setSkipEmptyRows(boolean skipEmptyRows) {
		this.skipEmptyRows = skipEmptyRows;
	}

	private final List<ExcelUploaderSucceededListener<T>> listeners = new ArrayList<ExcelUploaderSucceededListener<T>>();
	
	public AbstractExcelUploader(Class<? super T> type) {
		this.type = type;
		this.fieldMap = new HashMap<>();
	}
	
	public void addSucceededListener(ExcelUploaderSucceededListener listener) {
        listeners.add(listener);
    }

	public void removeSucceededListener(ExcelUploaderSucceededListener listener) {
        listeners.remove(listener);
    }
	
	private void fireUploadSucceededEvent(Upload.SucceededEvent event, List<T> items) {        
        if (listeners != null) {
            for(int i =0; i < listeners.size(); i++) {            	
				ExcelUploaderSucceededListener listener = listeners.get(i);
                listener.succeededListener(event, items);
            }
        }
    }
		
	@Override
	public OutputStream receiveUpload(String filename, String mimeType) {		
		FileOutputStream fos = null;        
        try {
        	int index = filename.lastIndexOf(".");
        	ext = Ext.findByExt(filename.substring(index + 1));
        	if(ext==null) {
        		throw new IOException("allow extenssion *.xls|xlsx"); 			
    		}        	
        	file = File.createTempFile("temp/uploads/excel/" + Long.toString(System.nanoTime()), filename);
            fos = new FileOutputStream(file);            
        } catch(IOException ex) {
        	delete(file);
        	new Notification("Could not open file<br/>", ex.getMessage(), Notification.Type.ERROR_MESSAGE).show(Page.getCurrent());        	
        	return null;
        } 
        return fos;
	}
	
	@Override
	public void uploadSucceeded(SucceededEvent event) {		
		List<T> items = new ArrayList<>();
		try {			
			Class<? extends T> targetClass = (Class<? extends T>) Class.forName(type.getName());
			registerExcelColumns(targetClass);
			if(Ext.xls == ext()) {
				items = readXLSFileToItems(file());
			} else if(Ext.xlsx == ext()) {
				items = readXLSXFileToItems(file());
			}
			delete(file);
			fireUploadSucceededEvent(event, items);			
		} catch(IOException e1) {
        	new Notification("Could not read item<br/>", e1.getMessage(), Notification.Type.ERROR_MESSAGE).show(Page.getCurrent());
        } catch (ClassNotFoundException e2) {
			new Notification("Class Not Found<br/>", e2.getMessage(), Notification.Type.ERROR_MESSAGE).show(Page.getCurrent());
		}
	}
		
	protected List<T> readXLSFileToItems(File file) throws IOException {
		List<T> result = new ArrayList<>();  
		try {
			FileInputStream fileInputStream = new FileInputStream(file);			
			
			HSSFWorkbook wb = new HSSFWorkbook(fileInputStream);
			HSSFSheet sheet = wb.getSheetAt(0);

			if(sheetAt>0) {
				sheet = wb.getSheetAt(sheetAt);
			} else if(!sheetName.equals("")) {
				sheet = wb.getSheet(sheetName);
			};

			Iterator<Row> rows = sheet.rowIterator();
			List<String> propertyNames = new ArrayList<>();

			HSSFRow row;

			while(rows.hasNext()) {
				row = (HSSFRow) rows.next();
				int rowIdx = row.getRowNum();

				if(rowIdx < firstRow) {
					continue;
				}

				if(rowIdx==firstRow) {
					Iterator<Cell> cells = row.cellIterator();
					while(cells.hasNext()) {
						HSSFCell cell = (HSSFCell)cells.next();
						int columnIdx = cell.getColumnIndex();
						propertyNames.add(columnIdx, cell.getStringCellValue());
					}
				} else if (!skipEmptyRows || row.getPhysicalNumberOfCells() > 0) {
					CreationHelper helper = row.getSheet().getWorkbook().getCreationHelper();
					FormulaEvaluator evaluator = helper.createFormulaEvaluator();
					Iterator<Cell> cells = row.cellIterator();					
					T item = (T) createItem(propertyNames, cells, evaluator);
					if(item!=null) {
						result.add(item);
					}
				}
			}
			
		} catch (Exception ex) {
			throw new IOException("readXLSFileToItems error: " + ex.getMessage());
		}
		return result;		
	}

	protected List<T> readXLSXFileToItems(File file) throws IOException {
		List<T> result = new ArrayList<>();
		try {
			FileInputStream fileInputStream = new FileInputStream(file);			
			
			XSSFWorkbook wb = new XSSFWorkbook(fileInputStream);

			XSSFSheet sheet = wb.getSheetAt(0);

			if(sheetAt>0) {
				sheet = wb.getSheetAt(sheetAt);
			} else if(!sheetName.equals("")) {
				sheet = wb.getSheet(sheetName);
			};

			Iterator<Row> rows = sheet.rowIterator();
			List<String> propertyNames = new ArrayList<>();

			XSSFRow row;

			while(rows.hasNext()) {
				row = (XSSFRow) rows.next();
				int rowIdx = row.getRowNum();

				if(rowIdx < firstRow) {
					continue;
				}

				if(rowIdx==firstRow) {
					Iterator<Cell> cells = row.cellIterator();
					while(cells.hasNext()) {
						XSSFCell cell = (XSSFCell)cells.next();
						int columnIdx = cell.getColumnIndex();
						propertyNames.add(columnIdx, cell.getStringCellValue());
					}
				} else if (!skipEmptyRows || row.getPhysicalNumberOfCells() > 0) {
					CreationHelper helper = row.getSheet().getWorkbook().getCreationHelper();
					FormulaEvaluator evaluator = helper.createFormulaEvaluator();
					Iterator<Cell> cells = row.cellIterator();					
					T item = (T) createItem(propertyNames, cells, evaluator);
					if(item!=null) {
						result.add(item);
					}
					
				}
			}
		} catch (Exception ex) {
			throw new IOException("readXLSXFileToItems error: " + ex.getMessage());
		}		
		return result;
		
	}
	
	private Object createItem(List<String> propertyNames, Iterator cells, FormulaEvaluator evaluator) throws IOException {
		try {
			DataFormatter df = new DataFormatter(false);
			Class<? extends T> targetClass = (Class<? extends T>) Class.forName(type.getName());
			Object object = (T)targetClass.newInstance();

			while(cells.hasNext()) {
				Cell cell = null;
				if(Ext.xls == ext()) {
					cell = (HSSFCell) cells.next();
				} else if(Ext.xlsx == ext()) {
					cell = (XSSFCell) cells.next();
				}
				String columnName = propertyNames.get(cell.getColumnIndex());
				String columnValue = df.formatCellValue(cell, evaluator);
				if(columnName.isEmpty() && columnValue.isEmpty()) {
					continue;
				}
				Field field = findColumnField(targetClass, columnName);
				if(field==null) {
					continue;
				}
				field.setAccessible(true);
				setColumnField(object, field, columnValue);
				if(field.get(object)!=null) {
					continue;
				}
				setColumnFieldType(object, field, columnValue);
			}
			return object;
		} catch (Exception ex) {
			throw new IOException("create item error: " + ex.getMessage());
		}
	}

	protected void setColumnFieldType(Object object, Field field, String columnValue) throws IllegalAccessException {
		Class<?> fieldType = field.getType();
		if (fieldType.equals(String.class)) {
            field.set(object, columnValue);
        } else	if (fieldType.equals(boolean.class) || fieldType.equals(Boolean.class)) {
            field.set(object, Boolean.valueOf(columnValue));
        } else if (fieldType.equals(byte.class) || fieldType.equals(Byte.class)) {
            field.set(object, Byte.valueOf(columnValue));
        } else if (fieldType.equals(char.class) || fieldType.equals(Character.class)) {
            field.set(object, Character.valueOf(columnValue.charAt(0)));
        } else if (fieldType.equals(double.class) || fieldType.equals(Double.class)) {
            field.set(object, Double.valueOf(columnValue));
        } else if (fieldType.equals(float.class) || fieldType.equals(Float.class)) {
            field.set(object, Float.valueOf(columnValue));
        } else if (fieldType.equals(int.class) || fieldType.equals(Integer.class)) {
            field.set(object, Integer.valueOf(columnValue));
        } else if (fieldType.equals(long.class) || fieldType.equals(Long.class)) {
            field.set(object, Long.valueOf(columnValue));
        } else if (fieldType.equals(short.class) || fieldType.equals(Short.class)) {
            field.set(object, Short.valueOf(columnValue));
        }
	}

	protected abstract Field findColumnField(Class<? extends T> targetClass, String columnName) throws NoSuchFieldException;

	protected void setColumnField(Object object, Field field, String columnValue) throws IllegalAccessException {
	}

	protected File file() {
		return this.file;
	}
	
	private Ext ext() {
		return this.ext;
	}
	
	public enum Ext {
		xls,
		xlsx;	
		
		public static final Ext findByExt(String value) {
		     for(Ext ext : Ext.values()) {
		        if(ext.name().equals(value))
		            return ext ;
		     }
		     return null;
		   }
	}

	private void delete(File file) {
		try {
			if(file.isFile()) {
				file.delete();
			}
		} catch (Exception ex) {
			new Notification("delete file<br/>", ex.getMessage(), Notification.Type.ERROR_MESSAGE).show(Page.getCurrent());
		}
	}

	protected void registerExcelColumns(Class<?> targetClass) {
		String className = targetClass.getName();
		Field[] fields = targetClass.getDeclaredFields();
		
		Map<String, Field> defaultFieldMap = new HashMap<>();
		Map<String, Field> annotaionFieldMap = new HashMap<>();
		
		for(Field field : fields) {
			ExcelColumn annotation = field.getAnnotation(ExcelColumn.class);
			String key = field.getName();
			if(annotation!=null) {
				key = annotation.value();
				annotaionFieldMap.put(key, field);
			}
			defaultFieldMap.put(key, field);
		}		
		if(defaultFieldMap.size() > 0 || annotaionFieldMap.size() > 0) {			
			if(annotaionFieldMap.size() > 0) {
				this.fieldMap.putAll(annotaionFieldMap);
			} else {
				this.fieldMap.putAll(defaultFieldMap);
			}
			return;
		}
		if(targetClass.getSuperclass() != Object.class) {
			registerExcelColumns(targetClass.getSuperclass());
			return;
		}
	}

	public Field getFieldMap(String mapKey) {
		return this.fieldMap.get(mapKey);
	}

}