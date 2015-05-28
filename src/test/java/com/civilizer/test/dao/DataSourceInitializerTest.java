package com.civilizer.test.dao;

import static org.junit.Assert.*;

import java.util.Collection;

import javax.sql.DataSource;

import org.junit.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

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
    
    static void runSqlScript(String scriptPath) {
    	final DataSource dataSource = ctx.getBean("dataSource", DataSource.class);
		assertNotNull(dataSource);
		final ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
		final ClassPathResource script = new  ClassPathResource(scriptPath);
		assertNotNull(script);
		populator.addScript(script);
		DatabasePopulatorUtils.execute(populator, dataSource);
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
