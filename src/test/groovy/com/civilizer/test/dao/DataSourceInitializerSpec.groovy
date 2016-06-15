package com.civilizer.test.dao

import spock.lang.*;

import com.civilizer.config.AppOptions;
import com.civilizer.dao.*;
import com.civilizer.test.helper.TestUtil;

@Subject(DataSourceInitializer)
class DataSourceInitializerSpec extends DaoSpecBase {
    
    @Override
    void doSetup() {
        DaoSpecBase.setupApplicationContext(
            "classpath:datasource-context-h2-empty.xml");
        
        TestUtil.configure();
        
        fragmentDao = ctx.getBean("fragmentDao", FragmentDao.class);
        assert fragmentDao;
        tagDao = ctx.getBean("tagDao", TagDao.class);
        assert tagDao;
        fileEntityDao = ctx.getBean("fileEntityDao", FileEntityDao.class);
        assert fileEntityDao;
        
        // Make sure all test files exist on the file system
        TestUtil.touchTestFilesForFileBox(fileEntityDao);
    } 

    @Override
    void doCleanup() {
        deleteAllTemporalObjects();
        TestUtil.unconfigure();
        DaoSpecBase.cleanupApplicationContext();
    }
    
    def "Run SQL scripts programmatically"() {
        expect: "Initially, we have no data"
            0 == fragmentDao.countAll(true)
            tagDao.findAllWithChildren(false).isEmpty()
        when: "Run some SQL script to populate some data"
            runSqlScript("db_test/test-data.sql");
        then: "Now we have some data"
            ! tagDao.findAllWithChildren(false).isEmpty()
    }
    
    def "Overwrite data"() {
        when: "Populate some data"
            runSqlScript("db_test/test-data.sql");
        then: "We have data"
            ! fragmentDao.findAll(true).isEmpty()
        when: "Purge all data"
            runSqlScript("db_test/drop.sql", "db_test/schema.sql");
        then: "We have no data"
            fragmentDao.findAll(true).isEmpty()
        when: "Repopulate the data"
            runSqlScript("db_test/test-data.sql");
        then: "We've restored the data"
            ! fragmentDao.findAll(true).isEmpty()
    }
    
    def "Inject DataSourceInitializer"() {
        when: "Inject it"
            final DataSourceInitializer dsi = ctx.getBean("dataSourceInitializer", DataSourceInitializer.class);
        then:
            dsi && dsi.getDataSource()
            ! dsi.getInitializingScripts().isEmpty()
    }
    
    def "Initialize the data source"() {
        setup: "Emulate DB reset by the user"
            doCleanup();
        when: "Instruct the system to initialize the DB at startup"
            final def option = AppOptions.DEF_INITIALIZE_DB;
            System.setProperty(option, "true");
            doSetup();
        then:
            notThrown Exception
        cleanup:
            System.clearProperty(option);
    }
    
    def "First run of the app"() {
        when: "Let the system think it runs first time"
            doCleanup();
            // The DB schema will be created by default at the time of doSetup() call;
            // But, with the following option set true, creating the schema will be skipped;
            // No schema for the DB will fool the app into thinking it runs first time
            final String option = "civilizer.no_schema";
            System.setProperty(option, "true");
            doSetup();
        then:
            notThrown Exception
        cleanup:
            System.clearProperty(option);
    }
    
    def "AppOptions.DATA_SCRIPTS property"() {
        when: "Run a script via DATA_SCRIPTS property; WITHOUT DB RESET"
            doCleanup();
            // Note that DATA_SCRIPTS can have multiple scripts
            System.setProperty(AppOptions.DATA_SCRIPTS, ",db_test/test-data.sql,");
            doSetup();
        then: "The script shouldn't run"
            fragmentDao.findAll(true).isEmpty();
            
        when: '''Run a script via DATA_SCRIPTS property;
                 But this time, emulate the situation where the app runs first time
              '''
            doCleanup();
            System.setProperty("civilizer.no_schema", "true");
    //        System.setProperty(AppOptions.INITIALIZE_DB, "true");
            System.setProperty(AppOptions.DATA_SCRIPTS, ",db_test/test-data.sql,");
            doSetup();
        then: "The script runs"
            ! fragmentDao.findAll(true).isEmpty();
            
        when: "Set DATA_SCRIPTS property empty"
            doCleanup();
            System.setProperty(AppOptions.DATA_SCRIPTS, "");
            doSetup();
        then: "There should be no effect"
            fragmentDao.findAll(true).isEmpty();
            
        cleanup:
            System.clearProperty("civilizer.no_schema");
            System.clearProperty(AppOptions.DATA_SCRIPTS);
            System.clearProperty(AppOptions.INITIALIZE_DB);
    }
    
    def "AppOptions.CLEAN_START property"() {
        setup:
            doCleanup();
        
        when: "Set CLEAN_START property true"
            System.setProperty(AppOptions.CLEAN_START, "true");
        and: "Also do the following to have CLEAN_START work"
            System.setProperty(AppOptions.OVERRIDE_OPTION_FILE, "true");
            TestUtil.configure();
            TestUtil.unconfigure();
            assert "true" == System.getProperty(AppOptions.CLEAN_START);
        and: "Emulate restart of the app"
            doSetup();
        then: "CLEAN_START makes the DB start from a clean empty state"
            fragmentDao.findAll(true).isEmpty();
            
        cleanup:
            System.clearProperty(AppOptions.OVERRIDE_OPTION_FILE);
            System.clearProperty(AppOptions.CLEAN_START);
    }
}
