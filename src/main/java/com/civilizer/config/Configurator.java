package com.civilizer.config;

import java.util.*;
import java.io.*;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class Configurator {
	
	private static final String DEFAULT_PRIVATE_HOME_NAME = ".civilizer";
	private static final String DEFAULT_PRIVATE_HOME_PATH = System.getProperty("user.home") + "/" + DEFAULT_PRIVATE_HOME_NAME;
	private static final String OPTION_FILE_NAME = "app-options.properties";
	private static final String DEFAULT_DB_FOLDER_NAME = "database";
	
	public Configurator() {
		addAppOptionsToSystemProperties();
	}
	
	private void addAppOptionsToSystemProperties() {
		final String privateHomePathByRuntimeArg = System.getProperty("civilizer.config_folder_path");
		final String privateHomePath = (privateHomePathByRuntimeArg == null) ?
				DEFAULT_PRIVATE_HOME_PATH : privateHomePathByRuntimeArg;
		
		final File privateHome = new File(privateHomePath);
		if (! privateHome.isDirectory()) {
			privateHome.mkdir();
		}
		
		setupPrivateHome(privateHome);
		
		try {
			final FileInputStream optionFile = new FileInputStream(privateHomePath + "/" + OPTION_FILE_NAME);
			final Properties p = new Properties();
			
			try {
				p.load(optionFile);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			
			final String dbFilePrefix = p.getProperty("civilizer.db_file_prefix");
			String absDbFilePrefix = null;
			if (dbFilePrefix.startsWith("/")) {
			    // absolute path
			    absDbFilePrefix = dbFilePrefix;
			}
			else {
			    // relative path
			    absDbFilePrefix = privateHome + "/" + dbFilePrefix;
			}
			p.setProperty("civilizer.db_file_prefix", absDbFilePrefix);
			
			Enumeration<Object> keys = p.keys();
			while (keys.hasMoreElements()) {
			    final String k = keys.nextElement().toString();
			    System.setProperty(k, p.getProperty(k));
			}
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private void setupPrivateHome(File privateHome) {
		final Path optionFilePath = FileSystems.getDefault().getPath(privateHome.getAbsolutePath(), OPTION_FILE_NAME);
		final File optionFile = optionFilePath.toFile();
		if (! optionFile.exists()) {
			InputStream defOptionsStream = getClass().getClassLoader().getResourceAsStream(OPTION_FILE_NAME);
			try {
				Files.copy(defOptionsStream, optionFilePath, StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		final File dbFolder = new File(privateHome.getAbsoluteFile() + "/" + DEFAULT_DB_FOLDER_NAME);
		if (! dbFolder.isDirectory()) {
		    dbFolder.mkdir();
		}
	}
	
}
