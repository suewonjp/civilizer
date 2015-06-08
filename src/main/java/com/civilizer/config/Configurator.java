package com.civilizer.config;

import java.util.*;
import java.io.*;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.civilizer.utils.FsUtil;

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
    
	private static boolean equals(Properties p, String optionKey, String optionValue, boolean caseSensitive) {
	    String v = p.getProperty(optionKey);
	    if (v == null)
	        return false;
	    if (caseSensitive)
	        v = v.toLowerCase();
	    return v.equals(optionValue);
	}

	private static boolean isTrue(Properties p, String optionKey) {
	    String v = p.getProperty(optionKey);
	    if (v == null)
	        return false;
        v = v.toLowerCase();
	    return v.equals("true") || v.equals("yes") || v.equals("on");
	}

    public static boolean equals(String optionKey, String optionValue, boolean caseSensitive) {
        return equals(System.getProperties(), optionKey, optionValue, caseSensitive);
    }

    public static boolean isTrue(String optionKey) {
        return isTrue(System.getProperties(), optionKey);
    }
	
	public static String getDefaultPrivateHomePath(String defaultPrivateHomeName) {
	    return System.getProperty("user.home") + File.separatorChar + defaultPrivateHomeName;
	}
	
	private File detectPrivateHome(String defaultPrivateHomeName) {
		// We use default private home path unless a path is provided at runtime

		final String defaultPrivateHomePath = getDefaultPrivateHomePath(defaultPrivateHomeName);
        final String privateHomePathByRuntimeArg = System.getProperty(AppOptions.PRIVATE_HOME_PATH);
        
        if (privateHomePathByRuntimeArg != null) {
        	final File f = new File(privateHomePathByRuntimeArg);
        	if (! f.isAbsolute()) {
        		// if the private home path is relative, the final path gets determined by the context of the current user working directory.
        		// it might get troublesome unless it is intended by the user explicitly
        		logger.warn("The specified home path \"%s\" is not absolute! it's error-prone!", privateHomePathByRuntimeArg);
        	}
        	return f;
        }
        
        return new File(defaultPrivateHomePath);
	}
	
	private void mergeAppOptions() {
		// [TODO] merge and update options;
		// add default options that do not exist in the existing option list
		// mark as obsolete any existing options that do not exist in the default option list
	}
	
	private void preSetupPrivateHome(File privateHome) {
	    FsUtil.createUnexistingDirectory(privateHome);
	    
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
		FsUtil.createUnexistingDirectory(new File(fileBoxHome));
	}
	
	private void overrideOptionValue(String k, Properties p) {
    	final String v = System.getProperty(k);
    	if (v != null && ! v.isEmpty()) {
    		p.setProperty(k, v);
    	}
	}
	
	private void setUnspecifiedOptionsWithDefaultValues(Properties p) {
		if (p.getProperty(AppOptions.DEV) == null) {
			p.setProperty(AppOptions.DEV, AppOptions.DEF_DEV);
		}

		if (p.getProperty(AppOptions.INITIALIZE_DB) == null) {
		    p.setProperty(AppOptions.INITIALIZE_DB, AppOptions.DEF_INITIALIZE_DB);
		}
	}
	
	private void setConstrainedOptions(Properties p) {
	    if (! isTrue(p, AppOptions.DEV)) {
            // [NOTE] 'database initialization' is available only for a development mode
            p.setProperty(AppOptions.INITIALIZE_DB, AppOptions.DEF_INITIALIZE_DB);
        }
	}
	
	private void addAppOptionsToSystemProperties(File privateHome) {
		try {
		    // load options from the application option file
		    final Properties p = new Properties();
		    final String optionFilePath = privateHome.getAbsolutePath() + File.separatorChar + AppOptions.OPTION_FILE_NAME;
		    p.load(new FileInputStream(optionFilePath));
		    
		    final boolean override =
		            isTrue(p, AppOptions.OVERRIDE_OPTION_FILE) || isTrue(AppOptions.OVERRIDE_OPTION_FILE);
		    if (override) {
		    	// override some options with the corresponding system properties if any
		    	overrideOptionValue(AppOptions.DB_FILE_PREFIX, p);
		    	overrideOptionValue(AppOptions.FILE_BOX_HOME, p);
		    }
			
		    // make sure the database file prefix is an absolute path
		    setPathAbsolute(p, AppOptions.DB_FILE_PREFIX, privateHome, AppOptions.DEF_DB_FILE_PREFIX);
			
			// make sure the file box folder path is absolute
			setPathAbsolute(p, AppOptions.FILE_BOX_HOME, privateHome, AppOptions.DEF_FILE_BOX_HOME);
			
			setUnspecifiedOptionsWithDefaultValues(p);
			
			// as a rule, some options are constrained with another option's value.
			// they may be reset under some condition no matter how they have been set by the user.
			setConstrainedOptions(p);
			
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
		String absPath = FsUtil.getAbsolutePath(srcPath, privateHome);
		p.setProperty(key, absPath);
	}
	
}
