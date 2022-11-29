package com.vaadin.addon.excel;

import java.lang.reflect.Field;

/**
 * Created by basakpie on 2016-10-10.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class ExcelUploader<T> extends AbstractExcelUploader {

	private static final long serialVersionUID = 1L;

	/**
	 *
	 * @param type upload type
	 */
	public ExcelUploader(Class<? extends T> type) {
		super(type);
	}

	@Override
	protected Field findColumnField(Class targetClass, String columnName) {
		//Field field = targetClass.getDeclaredField(columnName);
		return getFieldMap(columnName);
	}

	/**
	 @Override
	 protected void setColumnField(Object object, Field field, String columnValue) throws IllegalAccessException {

	 }
	 **/

}