package com.vaadin.addon.excel;

import java.util.List;
import com.vaadin.ui.Upload;

/**
 * ExcelUploaderSucceededListener interface
 * @param <T>
 */

public interface ExcelUploaderSucceededListener<T> {

	void succeededListener(Upload.SucceededEvent event, List<T> items);

}