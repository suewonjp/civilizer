package com.civilizer.dao;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import javax.sql.DataSource;

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
        final boolean kickInitializing = firstRun || forcedByUser;
        
        if (kickInitializing) {
            if (firstRun)
                logger.info("the app seems running for the 1st time");
            else if (forcedByUser)
                logger.info("the user has requested database reset");
            logger.info("intializing (reseting) database...");
            final ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            for (String s : initializingScripts) {
                final ClassPathResource script = new  ClassPathResource(s);
                populator.addScript(script);
            }
            DatabasePopulatorUtils.execute(populator, dataSource);
        }
    }

}
