package com.vaadin.addon.excel;

import com.vaadin.ui.Upload;
import org.junit.AfterClass;
import org.junit.Test;

import java.io.*;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by gmind on 2016-10-31.
 */
public class TestExcelUploader {

    final File fieldXls;
    final File fieldXlsx;

    final File annotationXls;
    final File annotationXlsx;

    public TestExcelUploader() throws Exception {
        ClassLoader classLoader = getClass().getClassLoader();
        fieldXls = new File(classLoader.getResource("field_upload.xls").getFile());
        fieldXlsx = new File(classLoader.getResource("field_upload.xlsx").getFile());
        annotationXls = new File(classLoader.getResource("annotation_upload.xls").getFile());
        annotationXlsx = new File(classLoader.getResource("annotation_upload.xlsx").getFile());
    }

    @Test
    public void testFieldXlsUpload() throws IOException {
        ExcelUploader<TestBean> excelUploader = new ExcelUploader<>(TestBean.class);
        excelUploader.addSucceededListener(new ExcelUploaderSucceededListener() {
            @Override
            public void succeededListener(Upload.SucceededEvent event, List items) {
                System.out.println("testFieldXlsUpload = [" + event + "], items = [" + items + "]");
                assertTrue("item size", items.size()==13);
                TestBean bean = (TestBean)items.get(0);
                assertTrue("item[0] id: ", bean.getId()==1L);
                assertEquals("item[0] name: ", bean.getName(), "1_name_xls");
                assertEquals("item[0] email: ", bean.getEmail(), "1_xls@email.com");
            }
        });
        filewrite(excelUploader, fieldXls);
    }

    @Test
    public void testFieldXlsxUpload() throws IOException {
        ExcelUploader<TestBean> excelUploader = new ExcelUploader<>(TestBean.class);
        excelUploader.addSucceededListener(new ExcelUploaderSucceededListener() {
            @Override
            public void succeededListener(Upload.SucceededEvent event, List items) {
                System.out.println("testFieldXlsxUpload = [" + event + "], items = [" + items + "]");
                assertTrue("item size", items.size()==10);
                TestBean bean = (TestBean)items.get(0);
                assertTrue("item[0] id: ", bean.getId()==1L);
                assertEquals("item[0] name: ", bean.getName(), "1_name_xlsx");
                assertEquals("item[0] email: ", bean.getEmail(), "1_xlsx@email.com");
            }
        });
        filewrite(excelUploader, fieldXlsx);
    }

    @Test
    public void testAnnotaionXlsUpload() throws IOException {
        ExcelUploader<TestBeanAnno> excelUploader = new ExcelUploader<>(TestBeanAnno.class);
        excelUploader.addSucceededListener(new ExcelUploaderSucceededListener() {
            @Override
            public void succeededListener(Upload.SucceededEvent event, List items) {
                System.out.println("testAnnotaionXlsUpload = [" + event + "], items = [" + items + "]");
                assertTrue("item size", items.size()==13);
                TestBeanAnno bean = (TestBeanAnno)items.get(0);
                assertTrue("item[0] id: ", bean.getId()==1L);
                assertEquals("item[0] name: ", bean.getName(), "1_name_xls");
                assertEquals("item[0] email: ", bean.getEmail(), "1_xls@email.com");
            }
        });
        filewrite(excelUploader, annotationXls);
    }

    @Test
    public void testAnnotaionXlsxUpload() throws IOException {
        ExcelUploader<TestBeanAnno> excelUploader = new ExcelUploader<>(TestBeanAnno.class);
        excelUploader.addSucceededListener(new ExcelUploaderSucceededListener() {
            @Override
            public void succeededListener(Upload.SucceededEvent event, List items) {
                System.out.println("testAnnotaionXlsxUpload = [" + event + "], items = [" + items + "]");
                assertTrue("item size", items.size()==8);
                TestBeanAnno bean = (TestBeanAnno)items.get(0);
                assertTrue("item[0] id: ", bean.getId()==1L);
                assertEquals("item[0] name: ", bean.getName(), "1_name_xlsx");
                assertEquals("item[0] email: ", bean.getEmail(), "1_xlsx@email.com");
            }
        });
        filewrite(excelUploader, annotationXlsx);
    }

    private void filewrite(ExcelUploader<?> excelUploader, File file) {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = new FileInputStream(file);
            outputStream = excelUploader.receiveUpload(file.getName(), "application/vnd.ms-excel");
            int data = 0;
            while ((data = inputStream.read()) != -1) {
                outputStream.write(data);
            }
            excelUploader.uploadSucceeded(null);
        } catch (Exception e) {
        } finally {
            try {
                inputStream.close();
                outputStream.close();
                excelUploader.file().delete();
            } catch (Exception e) {
            }

        }
    }

}