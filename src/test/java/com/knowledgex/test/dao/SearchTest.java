package com.knowledgex.test.dao;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

public class SearchTest extends DaoTest {
	
	@BeforeClass
    public static void setUpBeforeClass() throws Exception {
        DaoTest.setUpBeforeClass(
                "classpath:datasource-context-h2-embedded.xml"
                , DaoEmbeddedTest.class
                );
    }
    
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }
    
    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }
    
}
