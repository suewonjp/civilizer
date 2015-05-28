package com.civilizer.test.dao;

import static org.junit.Assert.*;

import java.util.*;
import org.junit.*;

import com.civilizer.domain.Tag;

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
		
		Collection<Tag> tags = tagDao.findAllWithChildren(true);
		assertEquals(false, tags.isEmpty());
	}

}
