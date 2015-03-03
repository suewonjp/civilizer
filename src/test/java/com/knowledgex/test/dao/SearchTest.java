package com.knowledgex.test.dao;

import static org.junit.Assert.assertNotNull;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.springframework.orm.hibernate3.SessionFactoryUtils;

public class SearchTest extends DaoTest {
	
	private Session session;
	
	@BeforeClass
    public static void setUpBeforeClass() throws Exception {
        DaoTest.setUpBeforeClass(
                "classpath:datasource-context-h2-empty.xml"
                , SearchTest.class
                );
    }
    
    @Before
    public void setUp() throws Exception {
        super.setUp();
        
        SessionFactory sessionFactory = ctx.getBean("sessionFactory", SessionFactory.class);
		assertNotNull(sessionFactory);
		
		session = SessionFactoryUtils.getSession(sessionFactory, true);
		assertNotNull(session);
    }
    
    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }
    
}
