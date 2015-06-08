package com.civilizer.test.dao;

import static org.junit.Assert.*;

import org.junit.*;

import java.util.*;

import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;
import org.hibernate.SessionFactory;
import org.hibernate.Session;
import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;

import com.civilizer.domain.*;
import com.civilizer.test.helper.TestUtil;

public class HibernateQueryTest extends DaoTest {
	
	private static final DateTimeComparator dtCmptr = DateTimeComparator.getInstance();
	
	private Session session;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		assertNotNull(dtCmptr);
		
		DaoTest.setUpBeforeClass(
                "classpath:datasource-context-h2-embedded.xml"
                , HibernateQueryTest.class
                );
	}
    
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    	DaoTest.tearDownAfterClass();
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
	
	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleCriteriaQuery() {
		final Criteria crit = session.createCriteria(Fragment.class);
		
		final List<Fragment> fragments = crit.list();
		assertNotNull(fragments);
		assertFalse(fragments.isEmpty());
		for (Fragment f : fragments) {
			assertNotNull(f.getId());
		}
		
		final long id = fragments.get(TestUtil.getRandom().nextInt(fragments.size())).getId();
		crit.add(Restrictions.eq("id", id));
		final Fragment f0 = (Fragment) crit.uniqueResult();
		final Fragment f1 = fragmentDao.findById(id, false, false);
		assertEquals(f0, f1);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testCriteriaOrder() {
		final List<Fragment> fragments0 = session
				.createQuery("from Fragment f order by updateDatetime desc")
				.list();
		
		assertNotNull(fragments0);
		assertFalse(fragments0.isEmpty());
		
		final Criteria crit = session.createCriteria(Fragment.class);
		assertNotNull(crit);
		crit.addOrder(Order.desc("updateDatetime"));

		// [NOTE] to get distinct results, use either of the following method
		// --- 1. lazy fetching of the specified child selections unless it is in lazy fetching mode
//		crit.setFetchMode("tags", FetchMode.SELECT);
		// --- 2. using distinct transformer
//		crit.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY); // distinct transformer
		
		final List<Fragment> fragments1 = crit.list();
		
		assertEquals(fragments0.size(), fragments1.size());
		
		for (int i=0; i<fragments1.size(); ++i) {
            Fragment f0 = fragments0.get(i);
            Fragment f1 = fragments1.get(i);
//            logger.info("i="+i+", id0="+f0.getId()+",id1="+f1.getId());
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
	
	@Test
	@SuppressWarnings("unchecked")
	public void testCriteriaProjection() {
		final List<Fragment> fragments = session.createCriteria(Fragment.class)
				.addOrder(Order.asc("id"))
				.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
				.list();
		
		final List<Object[]> results = (List<Object[]>)
				session.createCriteria(Fragment.class)
				.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
			    .setProjection( Projections.projectionList()
			        .add( Projections.rowCount() )
			        .add( Projections.min("id") )
			        .add( Projections.max("id") )
			    )
			    .list();
		assertTrue(results.size() == 1);
		Object[] prjList = results.get(0);
		assertTrue((Long) prjList[0]== fragments.size());
		assertTrue((Long) prjList[1]== fragments.get(0).getId());
		assertTrue((Long) prjList[2]== fragments.get(fragments.size()-1).getId());
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void testCriteriaAssociation() {
		final String tgtTagName = "#trash";
		
		final Criteria crit = session.createCriteria(Fragment.class);
		
		final Criteria tagCrit = crit.createCriteria("tags");
		assertNotNull(tagCrit);
		tagCrit.add(Restrictions.eq("tagName", tgtTagName));
		
		final List<Fragment> fragments = crit.list();
		assertNotNull(fragments);
		assertFalse(fragments.isEmpty());
		
		final Fragment f0 = fragments.get(0);
		assertTrue(f0 != null && f0.getId() != null);
		
		final Collection<Tag> tags = f0.getTags();
		assertNotNull(tags);
		assertFalse(tags.isEmpty());
		
		assertTrue(f0.containsTagName(tgtTagName));
	}
	
	@Test
//	@SuppressWarnings("unchecked")
	public void testDetachedCriteria() {
		final List<Fragment> fragments = fragmentDao.findAll(true);
		assertNotNull(fragments);
		assertFalse(fragments.isEmpty());
		for (Fragment f : fragments) {
			assertNotNull(f.getId());
		}
		
		final DetachedCriteria query = DetachedCriteria.forClass(Fragment.class);
		final long id = fragments.get(TestUtil.getRandom().nextInt(fragments.size())).getId();
		query.add(Property.forName("id").eq(id));
		
		final Criteria crit = query.getExecutableCriteria(session);
		
		final Fragment f0 = (Fragment) crit.uniqueResult();
		final Fragment f1 = fragmentDao.findById(id);
		assertEquals(f0, f1);
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void testJoinTable() {
		final List<Fragment> fragments = fragmentDao.findAll(true);
		final List<Tag> tags = tagDao.findAll();		
		final List<Tag2Fragment> t2fs = session
				.createQuery("from Tag2Fragment t2f")
				.list();
		
		assertNotNull(t2fs);
		assertFalse(t2fs.isEmpty());
		for (Tag2Fragment row : t2fs) {
			assertNotNull(row.getId());
			assertNotNull(row.getTagId());
			assertNotNull(row.getFragmentId());
			
			final Long tagId = row.getTagId();
			assertTrue(Tag.containsId(tags, tagId));
			
			final Long fragmentId = row.getFragmentId();
			assertTrue(Fragment.containsId(fragments, fragmentId));
		}
		
		final List<Long> trashedFragmentIds = session
				.getNamedQuery("Tag2Fragment.findTrashedFragmentIds")
				.list();
		for (Long id : trashedFragmentIds) {
			Fragment f = fragmentDao.findById(id, true, false);
			assertNotNull(f);
			assertNotNull(f.getTags());
			boolean hasTrashTag = false;
			for (Tag t : f.getTags()) {
				if (t.getId() == Tag.TRASH_TAG_ID) {
					hasTrashTag = true;
				}
			}
			assertTrue(hasTrashTag);
		}
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void testQueryNonTrashedFragmentsByTag() {
		final Long tgtTagId = 13L;
		
		final List<Fragment> fragments = session
				.createQuery(
						  "select distinct f "
						+ "from Fragment f "
						+ "  left join fetch f.tags t "
						+ "where t.id = :id and f.id not in ("
						+ "  select f2.id "
						+ "  from Tag t2 "
						+ "    join t2.fragments f2 "
						+ "  where t2.id = 0 "
						+ ")"
				 )
				.setParameter("id", tgtTagId)
				.list();
		for (Fragment f : fragments) {
			Collection<Tag> tags = f.getTags();
			assertNotNull(tags);
		    assertTrue(Tag.containsId(tags, tgtTagId));
		    assertFalse(Tag.containsId(tags, Tag.TRASH_TAG_ID));
		}
	}
	
}
