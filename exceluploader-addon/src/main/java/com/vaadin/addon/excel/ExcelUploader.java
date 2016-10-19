package com.vaadin.addon.excel;

import java.lang.reflect.Field;

/**
 * Created by basakpie on 2016-10-10.
 */
@SuppressWarnings("unused")
public class ExcelUploader<T> extends AbstractExcelUploader {

	public ExcelUploader(Class type) {
		super(type);
	}

	@Override
	protected Field findColumnField(Class targetClass, String columnName) throws NoSuchFieldException {
		Field field = targetClass.getDeclaredField(columnName);
		return field;
	}

	/**
	@Override
	protected void setColumnField(Object object, Field field, String columnValue) throws IllegalAccessException {

	}
	**/

}