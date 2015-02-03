package com.knowledgex.test.dao;

import static org.junit.Assert.*;

import org.junit.*;

import java.util.*;

import org.apache.commons.logging.Log;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.Hibernate;
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
	public void testSimpleCriteriaQuery() {
		Criteria crit = session.createCriteria(Fragment.class);
		List<Fragment> fragments = (List<Fragment>) crit.list();
		assertNotNull(fragments);
		assertFalse(fragments.isEmpty());
		for (Fragment f : fragments) {
			assertNotNull(f.getId());
			assertTrue(Hibernate.isInitialized(f.getTags()));
		}
		
		final long id = 1;
		crit.add(Restrictions.eq("id", id));
		Fragment f0 = (Fragment) crit.uniqueResult();
		Fragment f1 = fragmentDao.findById(id);
		assertEquals(f0, f1);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testCriteriaOrderBy() {
		List<Fragment> fragments0 = (List<Fragment>)
				session
				.createQuery("from Fragment f order by updateDatetime desc")
				.list();
		
		assertNotNull(fragments0);
		assertFalse(fragments0.isEmpty());
		
		Criteria crit = session.createCriteria(Fragment.class);
		crit.addOrder(Order.desc("updateDatetime"));
		crit.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		List<Fragment> fragments1 = (List<Fragment>) crit.list();
		
		assertEquals(fragments0.size(), fragments1.size());
		
		for (int i=0; i<fragments1.size(); ++i) {
            Fragment f0 = fragments0.get(i);
            Fragment f1 = fragments1.get(i);
            log.info("i="+i+", id0="+f0.getId()+",id1="+f1.getId());
            assertEquals(f0, f1);
		}
		
		for (int i=1; i<fragments1.size(); ++i) {
            Fragment f0 = fragments1.get(i - 1);
            Fragment f1 = fragments1.get(i);
            DateTime dt0 = f0.getUpdateDatetime();
            DateTime dt1 = f1.getUpdateDatetime();
            int r = dtCmptr.compare(dt0, dt1);
            assertTrue(r >= 0);
        }
	}

}
