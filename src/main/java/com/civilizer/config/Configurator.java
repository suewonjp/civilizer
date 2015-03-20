package com.civilizer.config;

public final class Configurator {
	
	Configurator() {
		System.setProperty("civilizer.db_file_prefix", "db-data/test");
		System.out.println("++++++++++++++++ " + System.getProperty("civilizer.db_file_prefix"));
	}

}
