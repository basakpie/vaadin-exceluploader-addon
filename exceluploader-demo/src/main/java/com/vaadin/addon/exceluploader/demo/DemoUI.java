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
import com.vaadin.annotations.Widgetset;
import com.vaadin.server.FileResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.*;
import com.vaadin.ui.Upload.SucceededEvent;

@Theme("demo")
@Title("ExcelUploader Add-on Demo")
@Widgetset("com.vaadin.addon.DemoWidgetSet")
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

    @SuppressWarnings("unchecked")
	private Component field_matching() {
				
		String basepath = VaadinService.getCurrent().getBaseDirectory().getAbsolutePath();		
		Link xlsLink = new Link("field test file.xls", new FileResource(Paths.get(basepath + "/temp/field_upload.xls").toFile()));
		Link xlsxLink = new Link("field test file.xlsx", new FileResource(Paths.get(basepath + "/temp/field_upload.xlsx").toFile()));
		
		final Grid<User> grid = new Grid<>();
		grid.setSizeFull();
        grid.addColumn(User::getId).setCaption("ID");
		grid.addColumn(User::getName).setCaption("Name");
		grid.addColumn(User::getEmail).setCaption("Email");
		
		final ExcelUploader<User> excelUploader = new ExcelUploader<>(User.class);
		excelUploader.addSucceededListener((event, items) -> {
			if(items.size()>0) {
				grid.setItems(items);
			}
        });
		
		final Upload upload = new Upload();
		upload.setButtonCaption("Upload");
		upload.setReceiver(excelUploader);
		upload.addSucceededListener(excelUploader);
		
		HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setSpacing(true);
		buttonLayout.addComponents(xlsLink, xlsxLink, upload);
				
        VerticalLayout rootlayout = new VerticalLayout();
        rootlayout.setSpacing(true);
        rootlayout.setSizeFull();
        rootlayout.addComponents(buttonLayout, grid);
        rootlayout.setComponentAlignment(buttonLayout, Alignment.MIDDLE_RIGHT);
        rootlayout.setExpandRatio(grid, 1f);
		return rootlayout;
	}
	
    @SuppressWarnings("unchecked")
	private Component annotation_matching() {
		
		String basepath = VaadinService.getCurrent().getBaseDirectory().getAbsolutePath();		
		Link xlsLink = new Link("annotation test file.xls", new FileResource(Paths.get(basepath + "/temp/annotation_upload.xls").toFile()));
		Link xlsxLink = new Link("annotation test file.xlsx", new FileResource(Paths.get(basepath + "/temp/annotation_upload.xlsx").toFile()));

		final Grid<UserEx> grid = new Grid<>();
		grid.setSizeFull();
        grid.addColumn(UserEx::getId).setCaption("ID");
		grid.addColumn(UserEx::getName).setCaption("Name");
		grid.addColumn(UserEx::getEmail).setCaption("Email");
		
		final ExcelUploader<UserEx> excelUploader = new ExcelUploader<>(UserEx.class);
		excelUploader.addSucceededListener((event, items) -> {
			if(items.size()>0) {
				grid.setItems(items);
			}
        });
		
		final Upload upload = new Upload();
		upload.setButtonCaption("Upload");
		upload.setReceiver(excelUploader);
		upload.addSucceededListener(excelUploader);
		
		HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setSpacing(true);
		buttonLayout.addComponents(xlsLink, xlsxLink, upload);
				
        VerticalLayout rootlayout = new VerticalLayout();
        rootlayout.setSpacing(true);
        rootlayout.setSizeFull();
        rootlayout.addComponents(buttonLayout, grid);
        rootlayout.setComponentAlignment(buttonLayout, Alignment.MIDDLE_RIGHT);
        rootlayout.setExpandRatio(grid, 1f);
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
