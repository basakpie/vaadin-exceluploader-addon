package com.vaadin.addon.excel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.sl.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.vaadin.server.FileResource;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinService;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.SucceededEvent;

/**
 * Created by basakpie on 2016-10-10.
 */
@SuppressWarnings("unused")
public class ExcelUploader<T> implements Upload.Receiver, Upload.SucceededListener {

	private static final long serialVersionUID = 1L;
	
	private Ext ext;
	
	private File file;
	
	private List<T> items;
	
	private final Class<? super T> type;
	
	public ExcelUploader(Class<? super T> type) {
		this.type = type;
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
		try {
			if(Ext.xls == ext()) {
				items = readXLSFileToItems(file());
			} else if(Ext.xlsx == ext()) {
				items = readXLSXFileToItems(file());
			}
		} catch(IOException ex) {			
        	new Notification("Could not read item<br/>", ex.getMessage(), Notification.Type.ERROR_MESSAGE).show(Page.getCurrent());
        } finally {
        	delete(file);
        }
	}
	
	public List<T> uploadItems() {
		return this.items;
	}
	
	@SuppressWarnings({"unchecked", "resource"})
	private List<T> readXLSFileToItems(File file) throws IOException {
		List<T> result = new ArrayList<>();  
		try {
			FileInputStream fileInputStream = new FileInputStream(file);			
			
			HSSFWorkbook wb = new HSSFWorkbook(fileInputStream);
			
			HSSFSheet sheet = wb.getSheetAt(0);
			HSSFRow row;
			
			Iterator<Row> rows = sheet.rowIterator();
			List<String> propertyNames = new ArrayList<>();
			
			while(rows.hasNext()) {
				row = (HSSFRow) rows.next();
				int rowIdx = row.getRowNum();
				
				if(rowIdx==0) {
					Iterator<Cell> cells = row.cellIterator();
					while(cells.hasNext()) {
						HSSFCell cell = (HSSFCell)cells.next();
						int columnIdx = cell.getColumnIndex();
						propertyNames.add(columnIdx, cell.getStringCellValue());
					}
				} else {
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
	
	@SuppressWarnings({"unchecked", "resource"})
	private List<T> readXLSXFileToItems(File file) throws IOException {
		List<T> result = new ArrayList<>();
		try {
			FileInputStream fileInputStream = new FileInputStream(file);			
			
			XSSFWorkbook wb = new XSSFWorkbook(fileInputStream);
			
			XSSFSheet sheet = wb.getSheetAt(0);
			XSSFRow row;
			
			Iterator<Row> rows = sheet.rowIterator();
			List<String> propertyNames = new ArrayList<>();
			
			while(rows.hasNext()) {
				row = (XSSFRow) rows.next();
				int rowIdx = row.getRowNum();
				
				if(rowIdx==0) {
					Iterator<Cell> cells = row.cellIterator();
					while(cells.hasNext()) {
						XSSFCell cell = (XSSFCell)cells.next();
						int columnIdx = cell.getColumnIndex();
						propertyNames.add(columnIdx, cell.getStringCellValue());
					}
				} else {
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
	
	@SuppressWarnings({"rawtypes", "unchecked"}) 
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
				String name = propertyNames.get(cell.getColumnIndex());
				String value = df.formatCellValue(cell, evaluator);
				if(!name.isEmpty() && !value.isEmpty()) {
					Field field = targetClass.getDeclaredField(name);
					Class<?> fieldType = field.getType();
					field.setAccessible(true);					
					if (fieldType.equals(String.class)) {
						field.set(object, value);
					} else	if (fieldType.equals(boolean.class) || fieldType.equals(Boolean.class)) {
						field.set(object, Boolean.valueOf(value));
					} else if (fieldType.equals(byte.class) || fieldType.equals(Byte.class)) {
						field.set(object, Byte.valueOf(value));
					} else if (fieldType.equals(char.class) || fieldType.equals(Character.class)) {
						field.set(object, Character.valueOf(value.charAt(0)));
					} else if (fieldType.equals(double.class) || fieldType.equals(Double.class)) {
						field.set(object, Double.valueOf(value));
					} else if (fieldType.equals(float.class) || fieldType.equals(Float.class)) {
						field.set(object, Float.valueOf(value));
					} else if (fieldType.equals(int.class) || fieldType.equals(Integer.class)) {
						field.set(object, Integer.valueOf(value));
					} else if (fieldType.equals(long.class) || fieldType.equals(Long.class)) {
						field.set(object, Long.valueOf(value));
					} else if (fieldType.equals(short.class) || fieldType.equals(Short.class)) {
						field.set(object, Short.valueOf(value));
					} else {
						field.set(object, value);
					}
				}
			}
			return object;
		} catch (Exception ex) {
			throw new IOException("create item error: " + ex.getMessage());
		}
	}
	
	private File file() {
		return this.file;
	}
	
	private Ext ext() {
		return this.ext;
	}
	
	private enum Ext {
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
			boolean filePresent = file.exists();
			if(!file.delete()) {
				if(!filePresent) {
					throw new FileNotFoundException("File does not exits:" + file);
				}
				String message = "Unable to delete file: " + file;
				throw new IOException(message);
			}
		} catch (IOException ex) {
			new Notification("delete file<br/>", ex.getMessage(), Notification.Type.ERROR_MESSAGE).show(Page.getCurrent());
		}
		
	}
}