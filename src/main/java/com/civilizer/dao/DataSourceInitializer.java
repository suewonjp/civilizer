package com.civilizer.dao;

import java.util.*;

import javax.sql.DataSource;

import org.springframework.beans.factory.InitializingBean;

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
        if (Configurator.isTrue(AppOptions.INITIALIZE_DB)) {
//            for (String s : initializingScripts) {
//                System.out.println(s);
//            }
        }
    }

}
