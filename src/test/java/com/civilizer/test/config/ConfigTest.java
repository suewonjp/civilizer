package com.civilizer.test.config;

import static org.junit.Assert.*;

import java.util.*;
import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.GenericXmlApplicationContext;

import com.civilizer.config.AppOptions;
import com.civilizer.config.Configurator;
import com.civilizer.dao.FragmentDao;
import com.civilizer.test.util.TestUtil;

public class ConfigTest {
    
    static List<File> filesToDelete = new ArrayList<File>();
    
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
    
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        filesToDelete = new ArrayList<File>();
    }
    
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        for (File file : filesToDelete) {
            deleteFile(file);
        }
    }
    
    @Before
    public void setUp() throws Exception {
    }
    
    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testSetUpDefaultPrivateHome() {
    	TestUtil.unconfigure();
        final String defaultPrivateHomeName = ".civilizer~";
        final String defaultPrivateHomePath = Configurator.getDefaultPrivateHomePath(defaultPrivateHomeName);
        File privateHome = new File(defaultPrivateHomePath);
        assertNotNull(privateHome);
        assertEquals(true, privateHome.isAbsolute());
        deleteFile(privateHome);
        assertEquals(false, privateHome.isDirectory());
        
        new Configurator(defaultPrivateHomeName);
        
        filesToDelete.add(privateHome);
        assertEquals(true, privateHome.isDirectory());
    }

    @Test
    public void testSetUpPrivateHomeProvidedAtRuntime() {
    	final String path = System.getProperty("user.dir") + "/test/private-home";
    	System.setProperty(AppOptions.PRIVATE_HOME_PATH, path);
    	new Configurator();
    	
    	final File f = new File(path);
    	assertNotNull(f);
    	assertEquals(true, f.isDirectory());
    	
    	System.clearProperty(AppOptions.PRIVATE_HOME_PATH);
    }
    
    @Test
    public void testConfigureDataSource() {
        TestUtil.configure();
        
        GenericXmlApplicationContext ctx = new GenericXmlApplicationContext();
        ctx.load("classpath:datasource-context-h2-url.xml");
        ctx.refresh();
        FragmentDao fragmentDao = ctx.getBean("fragmentDao", FragmentDao.class);
        assertNotNull(fragmentDao);
    }

}