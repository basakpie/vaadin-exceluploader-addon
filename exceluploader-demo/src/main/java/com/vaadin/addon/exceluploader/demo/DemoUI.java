package com.vaadin.addon.exceluploader.demo;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.annotation.WebServlet;

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
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.FinishedEvent;
import com.vaadin.ui.Upload.FinishedListener;
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
        setContent(excelUploader());
    }

	private Component excelUploader() {
				
		String basepath = VaadinService.getCurrent().getBaseDirectory().getAbsolutePath();		
		Link xlsLink = new Link("xls test file", new FileResource(Paths.get(basepath + "/temp/upload.xls").toFile()));
		Link xlsxLink = new Link("xlsx test file", new FileResource(Paths.get(basepath + "/temp/upload.xlsx").toFile()));
		
		final Table table = createTable(userList);
        table.setWidth(100, Unit.PERCENTAGE);
        table.setSizeFull();
		
		final ExcelUploader<User> excelUploader = new ExcelUploader<>(User.class);
		
		final Upload upload = new Upload();
		upload.setImmediate(true);
		upload.setReceiver(excelUploader);
		upload.setButtonCaption("Upload");
		upload.addSucceededListener(excelUploader);
		upload.addFinishedListener(new FinishedListener() {			
			@Override
			public void uploadFinished(FinishedEvent event) {
				List<User> users = excelUploader.uploadItems();
				if(users.size()>0) {
					table.removeAllItems();
					table.addItems(users);
				}
			}
		});
		
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
	
	private Table createTable(List<User> users) {
        final Table table = new Table();
        table.setSizeFull();
        table.setContainerDataSource(new BeanItemContainer<>(User.class, users));
        table.setVisibleColumns("id", "name","email");
        table.setColumnHeaders("ID", "Name","Email");
        return table;
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
