package com.vaadin.addon.exceluploader.demo;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.annotation.WebServlet;

import com.vaadin.addon.excel.ExcelUploaderSucceededListener;
import com.vaadin.addon.excel.ExcelUploader;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.server.FileResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Link;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.VerticalLayout;

@Theme("demo")
@Title("ExcelUploader Add-on Demo")
@SuppressWarnings("serial")
public class DemoUI extends UI {
	
	public static List<User> userList = new ArrayList<>();

    @WebServlet(value = "/*", asyncSupported = true)
    @VaadinServletConfiguration(productionMode = false, ui = DemoUI.class)
    public static class Servlet extends VaadinServlet {
    }

    @Override
    protected void init(VaadinRequest vaadinRequest) {   
    	TabSheet tabSheet = new TabSheet();
        tabSheet.setSizeFull();
        tabSheet.addTab(field_matching(), "field_matching");
        tabSheet.addTab(annotation_matching(), "annotation_matching");
        setContent(tabSheet);        
    }

	private Component field_matching() {
				
		String basepath = VaadinService.getCurrent().getBaseDirectory().getAbsolutePath();		
		Link xlsLink = new Link("xls test file", new FileResource(Paths.get(basepath + "/temp/field_upload.xls").toFile()));
		Link xlsxLink = new Link("xlsx test file", new FileResource(Paths.get(basepath + "/temp/field_upload.xlsx").toFile()));
		
		final Table table = new Table();
		table.setSizeFull();
        table.setContainerDataSource(new BeanItemContainer<>(User.class, null));
        table.setVisibleColumns("id", "name","email");
        table.setColumnHeaders("ID", "Name","Email");
		
		final ExcelUploader<User> excelUploader = new ExcelUploader<>(User.class);
		excelUploader.addSucceededListener(new ExcelUploaderSucceededListener<User>() {
			@Override
			public void succeededListener(SucceededEvent event, List<User> items) {
				if(items.size()>0) {
                    table.removeAllItems();
                    table.addItems(items);
                }
			}
		});
		
		final Upload upload = new Upload();
		upload.setImmediate(true);
		upload.setButtonCaption("Upload");
		upload.setReceiver(excelUploader);
		upload.addSucceededListener(excelUploader);
		
		HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setSpacing(true);
		buttonLayout.addComponents(xlsLink, xlsxLink, upload);
				
        VerticalLayout rootlayout = new VerticalLayout();
        rootlayout.setSpacing(true);
        rootlayout.setSizeFull();
        rootlayout.addComponents(buttonLayout, table);
        rootlayout.setComponentAlignment(buttonLayout, Alignment.MIDDLE_RIGHT);
        rootlayout.setExpandRatio(table, 1f);
		return rootlayout;
	}
	
	private Component annotation_matching() {
		
		String basepath = VaadinService.getCurrent().getBaseDirectory().getAbsolutePath();		
		Link xlsLink = new Link("xls test file", new FileResource(Paths.get(basepath + "/temp/annotation_upload.xls").toFile()));
		Link xlsxLink = new Link("xlsx test file", new FileResource(Paths.get(basepath + "/temp/annotation_upload.xlsx").toFile()));
		
		final Table table = new Table();
		table.setSizeFull();
        table.setContainerDataSource(new BeanItemContainer<>(UserEx.class, null));
        table.setVisibleColumns("id", "name","email");
        table.setColumnHeaders("아이디", "이름","이메일");
		
		final ExcelUploader<UserEx> excelUploader = new ExcelUploader<>(UserEx.class);
		excelUploader.addSucceededListener(new ExcelUploaderSucceededListener<UserEx>() {
			@Override
			public void succeededListener(SucceededEvent event, List<UserEx> items) {
				if(items.size()>0) {
                    table.removeAllItems();
                    table.addItems(items);
                }
			}
		});
		
		final Upload upload = new Upload();
		upload.setImmediate(true);
		upload.setButtonCaption("Upload");
		upload.setReceiver(excelUploader);
		upload.addSucceededListener(excelUploader);
		
		HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setSpacing(true);
		buttonLayout.addComponents(xlsLink, xlsxLink, upload);
				
        VerticalLayout rootlayout = new VerticalLayout();
        rootlayout.setSpacing(true);
        rootlayout.setSizeFull();
        rootlayout.addComponents(buttonLayout, table);
        rootlayout.setComponentAlignment(buttonLayout, Alignment.MIDDLE_RIGHT);
        rootlayout.setExpandRatio(table, 1f);
		return rootlayout;
	}
		
	static {
		loadingData();
	}
	
    public static void loadingData() {
        for(int i=0; i < 20; i++) {
            User user = new User(Long.valueOf(i));
            user.setEmail(i + "_@native.com");
            user.setName(i + "_native");
            userList.add(user);
        }
    }

    
}
