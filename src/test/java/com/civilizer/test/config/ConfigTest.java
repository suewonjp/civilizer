package com.civilizer.test.config;

import static org.junit.Assert.*;

import java.util.*;
import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.springframework.context.support.GenericXmlApplicationContext;

import com.civilizer.config.Configurator;
import com.civilizer.dao.FragmentDao;

public class ConfigTest {
    
    List<File> filesToDelete = new ArrayList<File>();
    
    private static void deleteFile(File file) {
        try {
            if (file.isDirectory()) {
                FileUtils.deleteDirectory(file);
            }
            else if (file.isFile()) {
                file.delete();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Before
    public void setUp() throws Exception {
    }
    
    @After
    public void tearDown() throws Exception {
        for (File file : filesToDelete) {
            deleteFile(file);
        }
    }

    @Test
    public void testSetUpDefaultPrivateHome() {
        final String defaultPrivateHomeName = ".civilizer~";
        final String defaultPrivateHomePath = Configurator.getDefaultPrivateHomePath(defaultPrivateHomeName);
        final File privateHome = new File(defaultPrivateHomePath);
        filesToDelete.add(privateHome);
        assertNotNull(privateHome);
        assertEquals(true, privateHome.isAbsolute());
        if (privateHome.exists()) {
            deleteFile(privateHome);
        }
        
        new Configurator(defaultPrivateHomeName);
        
        assertEquals(true, privateHome.isDirectory());
    }
    
    @Test
    public void testConfigureDataSource() {
        System.setProperty("civilizer.db_file_prefix", "db-data/test");
        
        GenericXmlApplicationContext ctx = new GenericXmlApplicationContext();
        ctx.load("classpath:datasource-context-h2-url.xml");
        ctx.refresh();
        FragmentDao fragmentDao = ctx.getBean("fragmentDao", FragmentDao.class);
        assertNotNull(fragmentDao);
    }

}
