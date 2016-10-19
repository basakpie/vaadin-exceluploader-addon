package com.vaadin.addon.excel;

import java.util.List;
import com.vaadin.ui.Upload;

public interface ExcelUploaderSucceededListener<T> {
	
	public void succeededListener(Upload.SucceededEvent event, List<T> items);

}