package com.civilizer.test.config;

import static org.junit.Assert.*;
import org.junit.*;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.support.GenericXmlApplicationContext;

import com.civilizer.config.AppOptions;
import com.civilizer.config.Configurator;
import com.civilizer.dao.FragmentDao;
import com.civilizer.test.helper.TestUtil;
import com.civilizer.utils.FsUtil;

@Ignore
public class ConfigTest {
    
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
        
        FileUtils.deleteQuietly(privateHome);
        assertEquals(false, privateHome.isDirectory());
        
        new Configurator(defaultPrivateHomeName);
        assertEquals(true, privateHome.isDirectory());
        
        FileUtils.deleteQuietly(privateHome);
    }

    @Test
    public void testSetUpPrivateHomeProvidedAtRuntime() {
    	final String path = TestUtil.getPrivateHomePath();
    	System.setProperty(AppOptions.PRIVATE_HOME_PATH, path);
    	new Configurator();
    	
    	final File f = new File(System.getProperty(AppOptions.PRIVATE_HOME_PATH));
    	assertNotNull(f);
    	assertEquals(true, f.isDirectory());
    	assertEquals(true, f.isAbsolute());
    	
    	System.clearProperty(AppOptions.PRIVATE_HOME_PATH);
    }
    
    @Test
    public void testRelativeFilePathRules() {
    	final String homePath = TestUtil.getPrivateHomePath();
    	System.setProperty(AppOptions.PRIVATE_HOME_PATH, homePath);
    	System.setProperty(AppOptions.OVERRIDE_OPTION_FILE, "true");
    	System.setProperty(AppOptions.DB_FILE_PREFIX, "db/file/prefix");
    	System.setProperty(AppOptions.FILE_BOX_HOME, "file/box/home");
    	new Configurator();
    	
    	FileUtils.deleteQuietly(new File(homePath + "/file"));
    	
    	assertEquals("true", System.getProperty(AppOptions.OVERRIDE_OPTION_FILE));
    	assertEquals(FsUtil.normalizePath(homePath + "/db/file/prefix"), System.getProperty(AppOptions.DB_FILE_PREFIX));
    	assertEquals(FsUtil.normalizePath(homePath + "/file/box/home"), System.getProperty(AppOptions.FILE_BOX_HOME));
    	
    	System.clearProperty(AppOptions.PRIVATE_HOME_PATH);
    	System.clearProperty(AppOptions.DB_FILE_PREFIX);
    	System.clearProperty(AppOptions.FILE_BOX_HOME);
    }

    @Test
    public void testAbsoluteFilePathRules() {
    	final String homePath = TestUtil.getPrivateHomePath();
    	final String userHomePath = System.getProperty("user.home");
    	System.setProperty(AppOptions.PRIVATE_HOME_PATH, homePath);
    	System.setProperty(AppOptions.OVERRIDE_OPTION_FILE, "true");
    	System.setProperty(AppOptions.DB_FILE_PREFIX, userHomePath + "/db/file/prefix");
//    	System.setProperty(AppOptions.FILE_BOX_HOME, userHomePath + "/file/box/home");
    	new Configurator();
    	
    	assertEquals("true", System.getProperty(AppOptions.OVERRIDE_OPTION_FILE));
    	assertEquals(FsUtil.normalizePath(userHomePath + "/db/file/prefix"), System.getProperty(AppOptions.DB_FILE_PREFIX));
//    	assertEquals(FsUtil.normalizePath(userHomePath + "/file/box/home"), System.getProperty(AppOptions.FILE_BOX_HOME));
    	
    	System.clearProperty(AppOptions.PRIVATE_HOME_PATH);
    	System.clearProperty(AppOptions.DB_FILE_PREFIX);
    	System.clearProperty(AppOptions.FILE_BOX_HOME);
    }

    @Test
    public void testHomeAliasFilePathRules() {
    	final String homePath = TestUtil.getPrivateHomePath();
    	final String userHomePath = System.getProperty("user.home");
    	System.setProperty(AppOptions.PRIVATE_HOME_PATH, homePath);
    	System.setProperty(AppOptions.OVERRIDE_OPTION_FILE, "true");
    	System.setProperty(AppOptions.DB_FILE_PREFIX, "~/db/file/prefix");
//    	System.setProperty(AppOptions.FILE_BOX_HOME, "~/file/box/home");
    	new Configurator();
    	
    	assertEquals("true", System.getProperty(AppOptions.OVERRIDE_OPTION_FILE));
    	assertEquals(FsUtil.normalizePath(userHomePath + "/db/file/prefix"), System.getProperty(AppOptions.DB_FILE_PREFIX));
//    	assertEquals(FsUtil.normalizePath(userHomePath + "/file/box/home"), System.getProperty(AppOptions.FILE_BOX_HOME));
    	
    	System.clearProperty(AppOptions.PRIVATE_HOME_PATH);
    	System.clearProperty(AppOptions.DB_FILE_PREFIX);
    	System.clearProperty(AppOptions.FILE_BOX_HOME);
    }

    @Test
    public void testFilePathSeparatorConvention() {
        final String homePath = TestUtil.getPrivateHomePath();
        final String userHomePath = System.getProperty("user.home");
        System.setProperty(AppOptions.PRIVATE_HOME_PATH, homePath);
        System.setProperty(AppOptions.OVERRIDE_OPTION_FILE, "true");
        System.setProperty(AppOptions.DB_FILE_PREFIX, userHomePath + "\\db\\file\\prefix");
//        System.setProperty(AppOptions.FILE_BOX_HOME, userHomePath + "\\file\\box\\home");
        new Configurator();
        
        assertEquals(false, System.getProperty(AppOptions.PRIVATE_HOME_PATH).contains("\\"));
        assertEquals(FsUtil.normalizePath(userHomePath + "/db/file/prefix"), System.getProperty(AppOptions.DB_FILE_PREFIX));
//        assertEquals(FsUtil.normalizePath(userHomePath + "/file/box/home"), System.getProperty(AppOptions.FILE_BOX_HOME));
        
        System.clearProperty(AppOptions.PRIVATE_HOME_PATH);
        System.clearProperty(AppOptions.DB_FILE_PREFIX);
        System.clearProperty(AppOptions.FILE_BOX_HOME);
    }
    
    @Test
    public void testJapaneseString() {
        final String homePath = TestUtil.getPrivateHomePath();
        System.setProperty(AppOptions.PRIVATE_HOME_PATH, homePath);
        new Configurator();
        final String actual = System.getProperty("civilizer.sample_japanese_str");
        if (actual != null) {
            final String expected = "カタカナ";
            try {
                assertEquals(expected, new String(actual.getBytes("ISO-8859-1"), Charset.forName("UTF-8")));
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
                fail();
            }
        }
        System.clearProperty(AppOptions.PRIVATE_HOME_PATH);
    }
    
    @Test
    public void testConfigureDataSource() {
        TestUtil.configure();
        
        GenericXmlApplicationContext ctx = new GenericXmlApplicationContext();
		try {
			ctx.load("classpath:datasource-context-h2-url.xml");
			ctx.refresh();
			FragmentDao fragmentDao = ctx.getBean("fragmentDao", FragmentDao.class);
			assertNotNull(fragmentDao);
		} catch (BeansException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} finally {
			ctx.close();
		}
    }
    
    @Test
    public void testMethod_Configurator_equals() {
        final String option = "Whatever Option";
        assertEquals(false, Configurator.equals(option, "true", false));
        System.setProperty(option, "TRUE");
        assertEquals(true, Configurator.equals(option, "true", false));
        System.setProperty(option, "True");
        assertEquals(true, Configurator.equals(option, "true", false));
        System.setProperty(option, "false");
        assertEquals(false, Configurator.equals(option, "true", false));
        System.clearProperty(option);
    }

    @Test
    public void testMethod_Configurator_isTrue() {
        final String option = "Boolean Option";
        assertEquals(false, Configurator.isTrue(option));
        System.setProperty(option, "TRUE");
        assertEquals(true, Configurator.isTrue(option));
        System.setProperty(option, "true");
        assertEquals(true, Configurator.isTrue(option));
        System.setProperty(option, "yes");
        assertEquals(true, Configurator.isTrue(option));
        System.setProperty(option, "ON");
        assertEquals(true, Configurator.isTrue(option));
        System.clearProperty(option);
    }

}
