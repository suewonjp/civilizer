package com.civilizer.config;

public final class AppOptions {

	public static final String OPTION_FILE_NAME                   = "app-options.properties";
	
	// options for production usages
	public static final String PRIVATE_HOME_PATH                  = "civilizer.private_home_path";
	public static final String DB_FILE_PREFIX                     = "civilizer.db_file_prefix";
	public static final String FILE_BOX_HOME                      = "civilizer.file_box_home";
	public static final String OVERRIDE_OPTION_FILE               = "civilizer.override_option_file";

	// options for development purposes
	public static final String INITIALIZE_DB                      = "civilizer.initialize_db";
	public static final String DEV                                = "civilizer.dev";
	public static final String SANITIZE_HTML                      = "civilizer.sanitize_html";
	                                                              
	// default values for each option
	public static final String DEF_PRIVATE_HOME                   = ".civilizer";
	public static final String DEF_DB_FILE_PREFIX                 = "civilizer";
	public static final String DEF_FILE_BOX_HOME                  = "files";
	public static final String DEF_OVERRIDE_OPTION_FILE           = "false";	                                                              
	public static final String DEF_DEV        			          = "false";
	public static final String DEF_INITIALIZE_DB                  = "false";
	public static final String DEF_SANITIZE_HTML                  = "true";
	
}
