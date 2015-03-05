package com.knowledgex.test.dao;

import static org.junit.Assert.*;

import java.util.*;

import org.junit.*;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.SessionFactoryUtils;

import com.knowledgex.dao.hibernate.SearchQueryCreator;
import com.knowledgex.domain.*;

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
    
    @Test
    @SuppressWarnings("unchecked")
    public void testHibernateCriteriaAPI_like_ilike() {
    	Tag[] tags = {
    			newTag("tag"),
    			newTag("$tag"),
    			newTag("Tag"),
    			newTag("TAG"),
    			newTag("Tag-000"),
    			newTag("my tag"),
    			newTag("your tag :-)"),
    			};
    	
    	for (Tag tag : tags) {
			tagDao.save(tag);
		}
    	
    	{
			final Criteria crit = session.createCriteria(Tag.class);
			crit.add(Restrictions.like("tagName", "%tag%"));
			final List<Tag> results = crit.list();
			logger.info(results);
			assertTrue(results.contains(tags[0]));
			assertTrue(results.contains(tags[1]));
			assertTrue(!results.contains(tags[2]));
			assertTrue(!results.contains(tags[3]));
			assertTrue(!results.contains(tags[4]));
			assertTrue(results.contains(tags[5]));
			assertTrue(results.contains(tags[6]));
		}
    	{
    		final Criteria crit = session.createCriteria(Tag.class);
    		crit.add(Restrictions.like("tagName", "_tag"));
    		final List<Tag> results = crit.list();
    		logger.info(results);
    		assertTrue(!results.contains(tags[0]));
    		assertTrue(results.contains(tags[1]));
    		assertTrue(!results.contains(tags[2]));
    		assertTrue(!results.contains(tags[3]));
    		assertTrue(!results.contains(tags[4]));
    		assertTrue(!results.contains(tags[5]));
    		assertTrue(!results.contains(tags[6]));
    	}
    	{
    		final Criteria crit = session.createCriteria(Tag.class);
    		crit.add(Restrictions.ilike("tagName", "tag%"));
    		final List<Tag> results = crit.list();
    		logger.info(results);
    		assertTrue(results.contains(tags[0]));
    		assertTrue(!results.contains(tags[1]));
    		assertTrue(results.contains(tags[2]));
    		assertTrue(results.contains(tags[3]));
    		assertTrue(results.contains(tags[4]));
    		assertTrue(!results.contains(tags[5]));
    		assertTrue(!results.contains(tags[6]));
    	}
    }
    
    @Test
    public void testMethod_SearchQueryCreator_newPattern() {
      {
          final String word = "'my keyword'";
          final SearchParams.Keyword kw = new SearchParams.Keyword(word);
          assertEquals(kw.isWholeWord(), false);
          assertEquals(kw.isAsIs(), true);
          assertEquals(kw.isValid(), true);
          final String pattern = SearchQueryCreator.newPattern(kw);
          assertEquals(pattern, "%my keyword%");
      }
      {
          final String word = "hello/w";
          final SearchParams.Keyword kw = new SearchParams.Keyword(word);
          assertEquals(kw.isWholeWord(), true);
          assertEquals(kw.isAsIs(), false);
          assertEquals(kw.isValid(), true);
          final String pattern = SearchQueryCreator.newPattern(kw);
          assertEquals(pattern, "hello");
      }
      {
          final String word = "?hello*world";
          final SearchParams.Keyword kw = new SearchParams.Keyword(word);
          assertEquals(kw.isWholeWord(), false);
          assertEquals(kw.isAsIs(), false);
          assertEquals(kw.isValid(), true);
          final String pattern = SearchQueryCreator.newPattern(kw);
          assertEquals(pattern, "%_hello%world%");
      }
    }
    
}
