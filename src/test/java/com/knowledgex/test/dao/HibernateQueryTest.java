package com.knowledgex.test.dao;

import static org.junit.Assert.*;

import org.junit.*;

import java.util.*;

import org.apache.commons.logging.Log;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.hibernate.SessionFactory;
import org.hibernate.Session;
import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;

import com.knowledgex.dao.*;
import com.knowledgex.domain.*;
import com.knowledgex.test.util.TestUtil;

public class HibernateQueryTest {
	
	private static Log log;
	private static GenericXmlApplicationContext ctx;
	private static final DateTimeComparator dtCmptr = DateTimeComparator.getInstance();
	
	private FragmentDao fragmentDao;
	private TagDao tagDao;
	
	private Session session;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		assertNotNull(dtCmptr);
		
		log = TestUtil.newLogger(HibernateQueryTest.class);

		ctx = new GenericXmlApplicationContext();
		ctx.load("classpath:datasource-context-h2-embedded.xml");
		ctx.refresh();
		log.info("GenericXmlApplicationContext initialized OK");
	}

	@Before
	public void setUp() throws Exception {
		fragmentDao = ctx.getBean("fragmentDao", FragmentDao.class);
		assertNotNull(fragmentDao);
		log.info("fragmentDao initialized OK");

		tagDao = ctx.getBean("tagDao", TagDao.class);
		assertNotNull(tagDao);
		log.info("tagDao initialized OK");
		
		SessionFactory sessionFactory = ctx.getBean("sessionFactory", SessionFactory.class);
		assertNotNull(sessionFactory);
		
		session = SessionFactoryUtils.getSession(sessionFactory, true);
		assertNotNull(session);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testHqlOrderBy() {
		List<Fragment> fragments = (List<Fragment>)
				session
				.createQuery("from Fragment f order by updateDatetime desc")
				.list();
		
		// [NOTE] 'order by' can't be parameterized...
		// you can't use parameters for columns, only values
		
		assertNotNull(fragments);
		assertFalse(fragments.isEmpty());
		
		for (int i=1; i<fragments.size(); ++i) {
            Fragment f0 = fragments.get(i - 1);
            Fragment f1 = fragments.get(i);
            DateTime dt0 = f0.getUpdateDatetime();
            DateTime dt1 = f1.getUpdateDatetime();
            int r = dtCmptr.compare(dt0, dt1);
            assertTrue(r >= 0);
        }
	}

}
