package com.civilizer.dao;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import com.civilizer.config.AppOptions;
import com.civilizer.config.Configurator;

public class DataSourceInitializer implements InitializingBean {
    
    private DataSource dataSource;
    private List<String> initializingScripts = new LinkedList<>();
    
    public Object getDataSource() {
        return dataSource;
    }
    
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<String> getInitializingScripts() {
        return initializingScripts;
    }

    public void setInitializingScripts(List<String> initializingScripts) {
        this.initializingScripts = initializingScripts;
    }
    
    public void afterPropertiesSet() {
        Logger logger = LoggerFactory.getLogger(DataSourceInitializer.class);
        if (dataSource == null) {
            logger.error("'dataSource' has not been injected! so DataSourceInitializer will do nothing...");
            return;
        }
        
        // check if our database schema exists;
        // if not, that means the application runs for the 1st time
        Connection conn = null;
        DatabaseMetaData dbmd = null;
        ResultSet rs = null;
        boolean schemaExists = false;
        try {
            conn = dataSource.getConnection();
            dbmd = conn.getMetaData();
            rs = dbmd.getTables(null, null, "GLOBAL_SETTING", null);
            schemaExists = rs.next();
        } catch (SQLException e) {
            String obj = "an object";
            if (conn == null)
                obj = "the java.sql.Connection";
            else if (dbmd == null)
                obj = "the java.sql.DatabaseMetaData";
            else if (rs == null)
                obj = "the java.sql.ResultSet";
            logger.error("failed in retrieving {} from 'dataSource'", obj);
            e.printStackTrace();
            return;
        }
        
        final boolean firstRun = (schemaExists == false);
        final boolean forcedByUser = Configurator.isTrue(AppOptions.INITIALIZE_DB);
        // [NOTE] the DB will get reset/initialized under either of the following conditions
        //      1. the application runs for the 1st time
        //          - more specifically, it runs for a new private home
        //      2. the user has set INITIALIZE_DB property true
        //          - to set it true, the user also needs to set DEV property true. 
        final boolean kickInitializing = firstRun || forcedByUser;
        
        if (kickInitializing) {
            // the DB will get reset/initialized;
            // this will overwrite any existing data
            if (firstRun)
                logger.info("the app seems running for the 1st time");
            else if (forcedByUser)
                logger.info("the user has requested database reset");
            logger.info("initializing (reseting) database...");
            
            // gather scripts specified via the data source context XML file
            final ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            for (String s : initializingScripts) {
                final ClassPathResource script = new  ClassPathResource(s.trim());
                populator.addScript(script);
            }
            
            // gather scripts specified via system properties
            final String[] dataScripts = StringUtils.split(System.getProperty(AppOptions.DATA_SCRIPTS), ",");
            for (String s : dataScripts) {
                final ClassPathResource script = new  ClassPathResource(s.trim());
                populator.addScript(script);
            }
            
            // execute the gathered scripts
            DatabasePopulatorUtils.execute(populator, dataSource);
        }
    }

}
