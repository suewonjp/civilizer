package com.civilizer.test.dao;

import static org.junit.Assert.*;

import org.junit.*;

import com.civilizer.config.AppOptions;
import com.civilizer.dao.DataSourceInitializer;
import com.civilizer.test.helper.TestUtil;

@Ignore
public class DataSourceInitializerTest extends DaoTest {

	@Before
	public void setUp() throws Exception {
		DaoTest.setUpBeforeClass(
				"classpath:datasource-context-h2-empty.xml"
				, SearchTest.class
				);
		super.setUp();
	}
    
    @After
    public void tearDown() throws Exception {
        super.tearDown();
        DaoTest.tearDownAfterClass();
    }

	@Test
	public void testRunSqlScriptProgrammatically() {
		assertEquals(0, fragmentDao.countAll(true));
		assertEquals(true, tagDao.findAllWithChildren(false).isEmpty());
		
		runSqlScript("db_test/test-data.sql");
		
		assertEquals(false, tagDao.findAllWithChildren(false).isEmpty());
	}

	@Test
	public void testOverwriteData() {
		runSqlScript("db_test/test-data.sql");
		assertEquals(false, fragmentDao.findAll(true).isEmpty());

		runSqlScript("db_test/drop.sql", "db_test/schema.sql");
		assertEquals(true, fragmentDao.findAll(true).isEmpty());
		
		runSqlScript("db_test/test-data.sql");		
		assertEquals(false, fragmentDao.findAll(true).isEmpty());
	}
	
	@Test
    public void testInjectDataSourceInitializer() {
	    final DataSourceInitializer dsi = ctx.getBean("dataSourceInitializer", DataSourceInitializer.class);
	    assertNotNull(dsi);
	    assertNotNull(dsi.getDataSource());
	    assertEquals(false, dsi.getInitializingScripts().isEmpty());
    }

	@Test
	public void testKickInitializingDataSource() throws Exception {
	    // emulate database reset by the user
	    tearDown();
	    
	    final String option = AppOptions.INITIALIZE_DB;
	    System.setProperty(option, "true");
	    
	    setUp();
	    
	    System.clearProperty(option);
	}

	@Test
	public void testFirstRunOfApp() throws Exception {
	    // emulate the 1st run of the application
	    tearDown();
        
	    // The DB schema will be created by default at the time of setUp() call;
	    // But, with the following option set true, creating the schema will be skipped;
	    // No schema for the DB will tell the app that it has started for the 1st time 
	    final String option = "civilizer.no_schema";
        System.setProperty(option, "true");
        
        setUp();
        
        System.clearProperty(option);
	}
	
	@Test
    public void testDataScriptsViaSystemProperty() throws Exception {
	    // make sure DATA_SCRIPTS have no effect without database reset
        tearDown();
        // DATA_SCRIPTS can have multiple scripts
        System.setProperty(AppOptions.DATA_SCRIPTS, ",db_test/test-data.sql,");
        setUp();
        assertEquals(true, fragmentDao.findAll(true).isEmpty()); // The script had no effect 

        // emulate the 1st run of the application
        tearDown();
        System.setProperty("civilizer.no_schema", "true");
//        System.setProperty(AppOptions.INITIALIZE_DB, "true");
        System.setProperty(AppOptions.DATA_SCRIPTS, ",db_test/test-data.sql,");
        setUp();
        assertEquals(false, fragmentDao.findAll(true).isEmpty()); // The script had run

        // check if empty scripts have no effect
        tearDown();
        System.setProperty(AppOptions.DATA_SCRIPTS, "");
        setUp();
        assertEquals(true, fragmentDao.findAll(true).isEmpty());
        
        // CLEAN_START will make the DB start from empty state
        tearDown();
        System.setProperty(AppOptions.OVERRIDE_OPTION_FILE, "true");
        System.setProperty(AppOptions.CLEAN_START, "true");
        TestUtil.configure();
        TestUtil.unconfigure();
        assertEquals("true", System.getProperty(AppOptions.CLEAN_START));
        setUp();
        assertEquals(true, fragmentDao.findAll(true).isEmpty());

        System.clearProperty(AppOptions.OVERRIDE_OPTION_FILE);
        System.clearProperty(AppOptions.CLEAN_START);
        System.clearProperty("civilizer.no_schema");
        System.clearProperty(AppOptions.DATA_SCRIPTS);
        System.clearProperty(AppOptions.INITIALIZE_DB);
    }

}
