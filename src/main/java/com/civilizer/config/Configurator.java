package com.civilizer.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.CopyOption;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class Configurator {
	
	private static final String DEFAULT_PRIVATE_HOME_NAME = ".civilizer";
	private static final String DEFAULT_PRIVATE_HOME_PATH = System.getProperty("user.home") + "/" + DEFAULT_PRIVATE_HOME_NAME;
	private static final String OPTION_FILE_NAME = "app-options.properties";
	
	public Configurator() {
//		addAppOptionsToSystemProperties();
		
		System.setProperty("civilizer.db_file_prefix", "db-data/test");
		System.out.println("++++++++++++++++ " + System.getProperty("civilizer.db_file_prefix"));
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
			final Properties p = new Properties(System.getProperties());
			
			try {
				p.load(optionFile);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			
			System.setProperties(p);
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private void setupPrivateHome(File privateHome) {
		final Path optionFilePath = FileSystems.getDefault().getPath(privateHome.getAbsolutePath(), OPTION_FILE_NAME);
//		final String optionFilePath = privateHome.getAbsolutePath() + "/" + OPTION_FILE_NAME;
//		final File optionFile = new File(optionFilePath.toString());
		final File optionFile = optionFilePath.toFile();
		if (! optionFile.exists()) {
			InputStream defOptionsStream = getClass().getResourceAsStream(OPTION_FILE_NAME);
//			InputStream defOptionsStream = this.getClass().getClassLoader().getResourceAsStream(OPTION_FILE_NAME);
			try {
				Files.copy(defOptionsStream, optionFilePath, (CopyOption[]) null);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
}
