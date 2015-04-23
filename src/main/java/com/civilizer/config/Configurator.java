package com.civilizer.config;

import java.util.*;
import java.io.*;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Configurator {
	
    private final Logger logger = LoggerFactory.getLogger(Configurator.class);
	
	public Configurator() {
	    final File privateHome = detectPrivateHome(AppOptions.DEF_PRIVATE_HOME);
        preSetupPrivateHome(privateHome);
		addAppOptionsToSystemProperties(privateHome);
		postSetupPrivateHome(privateHome);
	}

	public Configurator(String defaultPrivateHomeName) {
	    final File privateHome = detectPrivateHome(defaultPrivateHomeName);
        preSetupPrivateHome(privateHome);
        addAppOptionsToSystemProperties(privateHome);
        postSetupPrivateHome(privateHome);
	}
	
	public static String getDefaultPrivateHomePath(String defaultPrivateHomeName) {
	    return System.getProperty("user.home") + File.separatorChar + defaultPrivateHomeName;
	}
	
	private File detectPrivateHome(String defaultPrivateHomeName) {
	    final String defaultPrivateHomePath = getDefaultPrivateHomePath(defaultPrivateHomeName);
        final String privateHomePathByRuntimeArg = System.getProperty(AppOptions.PRIVATE_HOME_PATH);
        
        // We use default private home path unless a path is provided at runtime
        final String privateHomePath = (privateHomePathByRuntimeArg == null) ?
                defaultPrivateHomePath : privateHomePathByRuntimeArg;
        
        return new File(privateHomePath);
	}
	
	private void mergeAppOptions() {
		// [TODO] merge and update options;
		// add default options that do not exist in the existing option list
		// mark as obsolete any existing options that do not exist in the default option list
	}
	
	private void preSetupPrivateHome(File privateHome) {
	    createUnexistingDirectory(privateHome);
	    
	    final String tgtOptionFilePath = privateHome.getAbsolutePath() + File.separatorChar + AppOptions.OPTION_FILE_NAME;
	    final File tgtOptionFile = new File(tgtOptionFilePath);
	    if (tgtOptionFile.exists()) {
	    	mergeAppOptions();
	    }
	    else {
	        // copy the default application option file unless it exists
	        final File defaultOptionFile = 
	                new File (getClass().getClassLoader().getResource(AppOptions.OPTION_FILE_NAME).getFile());
	        try {
                FileUtils.copyFile(defaultOptionFile, tgtOptionFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
	    }
    }
	
	private void postSetupPrivateHome(File privateHome) {
		final String fileBoxHome = System.getProperty(AppOptions.FILE_BOX_HOME);
		createUnexistingDirectory(new File(fileBoxHome));
	}
	
	private void addAppOptionsToSystemProperties(File privateHome) {
		try {
		    // load options from the application option file
		    final Properties p = new Properties();
		    final String optionFilePath = privateHome.getAbsolutePath() + File.separatorChar + AppOptions.OPTION_FILE_NAME;
		    p.load(new FileInputStream(optionFilePath));
			
		    // make sure the database file prefix is an absolute path
		    setPathAbsolute(p, AppOptions.DB_FILE_PREFIX, privateHome, AppOptions.DEF_DB_FILE_PREFIX);
			
			// make sure the file box folder path is absolute
			setPathAbsolute(p, AppOptions.FILE_BOX_HOME, privateHome, AppOptions.DEF_FILE_BOX_HOME);
			
			if (p.getProperty(AppOptions.DEV) == null) {
				p.setProperty(AppOptions.DEV, AppOptions.DEF_DEV);
			}

			if (p.getProperty(AppOptions.INITIALIZE_DB) == null) {
			    p.setProperty(AppOptions.INITIALIZE_DB, AppOptions.DEF_INITIALIZE_DB);
			}
			
			// add the application options into the system properties
			// and then, we can access the options via SpEL (i.g. "#{systemProperties['key']}")
			// [NOTE] do not use System.setProperties() method to operate the same thing;
			// It will cause "systemProperties" SpEL expression not to work properly
			Enumeration<Object> keys = p.keys();
			while (keys.hasMoreElements()) {
			    final String k = keys.nextElement().toString();
			    final String v = p.getProperty(k);
			    System.setProperty(k, v);
			    logger.info("SYS PROP : {}, {}", k, v);
			}
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	private void setPathAbsolute(Properties p, String key, File privateHome, String defValue) {
		String srcPath = p.getProperty(key);
		if (srcPath == null) {
			logger.error("????? The key \"%s\" is NOT found! Use the default value of \"%s\"", key, defValue);
			srcPath = defValue;
		}
		String absPath = null;
		if (new File(srcPath).isAbsolute()) {
		    // already absolute path
			absPath = srcPath;
		}
		else {
		    // relative path
			absPath = privateHome.getAbsolutePath() + File.separatorChar + srcPath;
		}
		p.setProperty(key, absPath);
	}
	
	private void createUnexistingDirectory(File dir) {
		if (! dir.isDirectory()) {
            dir.mkdir();
        }
	}
	
}
