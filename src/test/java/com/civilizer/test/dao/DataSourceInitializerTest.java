package com.civilizer.test.dao;

import static org.junit.Assert.*;

import org.junit.*;

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
		assertEquals(true, tagDao.findAllWithChildren(true).isEmpty());
		
		runSqlScript("db_test/test-data.sql");
		
		assertEquals(false, tagDao.findAllWithChildren(true).isEmpty());
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

}
