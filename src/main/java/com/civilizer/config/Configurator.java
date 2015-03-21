package com.civilizer.config;

import java.util.*;
import java.io.*;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Configurator {
	
	private static final String DEFAULT_PRIVATE_HOME_NAME = ".civilizer";
	private static final String OPTION_FILE_NAME = "app-options.properties";
	private static final String KEY_PRIVATE_HOME_PATH = "civilizer.private_home_path";
	private static final String KEY_DB_FILE_PREFIX = "civilizer.db_file_prefix";
	
	@SuppressWarnings("unused")
    private final Logger logger = LoggerFactory.getLogger(Configurator.class);
	
	public Configurator() {
	    final File privateHome = detectPrivateHome(DEFAULT_PRIVATE_HOME_NAME);
        setupPrivateHome(privateHome);
		addAppOptionsToSystemProperties(privateHome);
	}

	public Configurator(String defaultPrivateHomeName) {
	    final File privateHome = detectPrivateHome(defaultPrivateHomeName);
        setupPrivateHome(privateHome);
        addAppOptionsToSystemProperties(privateHome);
	}
	
	public static String getDefaultPrivateHomePath(String defaultPrivateHomeName) {
	    return System.getProperty("user.home") + File.separatorChar + defaultPrivateHomeName;
	}
	
	private File detectPrivateHome(String defaultPrivateHomeName) {
	    final String defaultPrivateHomePath = getDefaultPrivateHomePath(defaultPrivateHomeName);
        final String privateHomePathByRuntimeArg = System.getProperty(KEY_PRIVATE_HOME_PATH);
        
        // We use default private home path unless a path is provided at runtime
        final String privateHomePath = (privateHomePathByRuntimeArg == null) ?
                defaultPrivateHomePath : privateHomePathByRuntimeArg;
        
        return new File(privateHomePath);
	}
	
	private void setupPrivateHome(File privateHome) {
	    if (! privateHome.isDirectory()) {
	        // create the private home directory unless it exists
            privateHome.mkdir();
        }
	    
	    final String tgtOptionFilePath = privateHome.getAbsolutePath() + File.separatorChar + OPTION_FILE_NAME;
	    final File tgtOptionFile = new File(tgtOptionFilePath);
	    if (! tgtOptionFile.exists()) {
	        // copy the default application option file unless it exists
	        final File defaultOptionFile = 
	                new File (getClass().getClassLoader().getResource(OPTION_FILE_NAME).getFile());
	        try {
                FileUtils.copyFile(defaultOptionFile, tgtOptionFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
	    }
    }
	
	private void addAppOptionsToSystemProperties(File privateHome) {
		try {
		    // load options from the application option file
		    final Properties p = new Properties();
		    final String optionFilePath = privateHome.getAbsolutePath() + File.separatorChar + OPTION_FILE_NAME;
		    p.load(new FileInputStream(optionFilePath));
			
		    // make sure the database file prefix to an absolute path
			final String dbFilePrefix = p.getProperty(KEY_DB_FILE_PREFIX);
			String absDbFilePrefix = null;
			if (new File(dbFilePrefix).isAbsolute()) {
			    // already absolute path
			    absDbFilePrefix = dbFilePrefix;
			}
			else {
			    // relative path
			    absDbFilePrefix = privateHome.getAbsolutePath() + File.separatorChar + dbFilePrefix;
			}
			p.setProperty(KEY_DB_FILE_PREFIX, absDbFilePrefix);
			
			// add the application options into the system properties
			// and then, we can access the options via SpringEL (i.g. "{systemProperties['key']}")
			Enumeration<Object> keys = p.keys();
			while (keys.hasMoreElements()) {
			    final String k = keys.nextElement().toString();
			    System.setProperty(k, p.getProperty(k));
			}
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
            e.printStackTrace();
        }
	}
	
}
