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
import com.knowledgex.test.util.TestUtil;

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
    	final Tag[] tags = {
    			/*0*/newTag("tag"),
    			/*1*/newTag("$tag"),
    			/*2*/newTag("Tag"),
    			/*3*/newTag("TAG"),
    			/*4*/newTag("Tag-000"),
    			/*5*/newTag("my tag"),
    			/*6*/newTag("your tag :-)"),
    	};
    	
    	for (Tag tag : tags) {
			tagDao.save(tag);
		}
    	
    	{
			final Criteria crit = session.createCriteria(Tag.class);
			crit.add(Restrictions.like("tagName", "%tag%"));
			final List<Tag> results = crit.list();
			logger.info(results);
			assertEquals(4, results.size());
			assertTrue(results.contains(tags[0]));
			assertTrue(results.contains(tags[1]));
			assertTrue(results.contains(tags[5]));
			assertTrue(results.contains(tags[6]));
		}
    	{
    		final Criteria crit = session.createCriteria(Tag.class);
    		crit.add(Restrictions.like("tagName", "_tag"));
    		final List<Tag> results = crit.list();
    		logger.info(results);
    		assertEquals(1, results.size());
    		assertTrue(results.contains(tags[1]));
    	}
    	{
    		final Criteria crit = session.createCriteria(Tag.class);
    		crit.add(Restrictions.sqlRestriction("TAG_NAME like 'my tag'"));
    		final List<Tag> results = crit.list();
    		logger.info(results);
    		assertEquals(1, results.size());
    		assertTrue(results.contains(tags[5]));
    	}
    	{
    		final Criteria crit = session.createCriteria(Tag.class);
    		crit.add(Restrictions.ilike("tagName", "tag%"));
    		final List<Tag> results = crit.list();
    		logger.info(results);
    		assertEquals(4, results.size());
    		assertTrue(results.contains(tags[0]));
    		assertTrue(results.contains(tags[2]));
    		assertTrue(results.contains(tags[3]));
    		assertTrue(results.contains(tags[4]));
    	}
    }
    
    @Test
    public void testMethod_SearchQueryCreator_getPatternFromKeyword() {
      {
          final String word = "\"my keyword\"";
          final SearchParams.Keyword kw = new SearchParams.Keyword(word);
          assertEquals(false, kw.isWholeWord());
          assertEquals(true, kw.isValid());
          final String pattern = SearchQueryCreator.getPatternFromKeyword(kw);
          assertEquals(pattern, "%my keyword%");
      }
      {
          final String word = "hello/w";
          final SearchParams.Keyword kw = new SearchParams.Keyword(word);
          assertEquals(true, kw.isWholeWord());
          assertEquals(true, kw.isValid());
          final String pattern = SearchQueryCreator.getPatternFromKeyword(kw);
          assertEquals(pattern, "hello");
      }
      {
          final String word = "hELLo";
          final SearchParams.Keyword kw = new SearchParams.Keyword(word);
          assertEquals(false, kw.isWholeWord());
          assertEquals(false, kw.isCaseSensitive());
          assertEquals(true, kw.isValid());
          final String pattern = SearchQueryCreator.getPatternFromKeyword(kw);
          assertEquals(pattern, "%hello%");
      }
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testSearch() {
    	final Tag[] tags = {
    			newTag("my tag"),
    			newTag("your tag :-)"),
    	};
    	
    	for (Tag tag : tags) {
			tagDao.save(tag);
		}
    	
    	final Fragment[] fragments = {
    			/*0*/newFragment(".text()", "replaces the text inside a selection"),
    			/*1*/newFragment(".html()", "works like .text() but lets you insert html instead of just text"),
    			/*2*/newFragment(".append()", "lets you insert the specified content as the last child of an element"),
    			/*3*/newFragment(".prepend()", "lets you insert the specified content as the first child of an element"),
    			/*4*/newFragment(".before()", "adds content before the selection"),
    			/*5*/newFragment(".after()", "works just like .before(), except that the content is added after the selection (after its closing tag)."),
    			/*6*/newFragment(".replaceWith()", "completely replaces the selection (including the tag and everything inside it) with whatever you pass"),
    			/*7*/newFragment(".remove()", "removes the selection from the DOM;"),
    			/*8*/newFragment(".wrap()", "wraps each element in a selection in a pair of HTML tags."),
    			/*9*/newFragment(".wrapInner()", "wraps the contents of each element in a selection in HTML tags."),
    		   /*10*/newFragment(".unwrap()", "simply removes the parent tag surrounding the selection."),
    		   /*11*/newFragment(".empty()", "removes all of the contents of a selection, but leaves the selection in place"),
    	};
    	
    	for (Fragment fragment : fragments) {
    		final int n = TestUtil.getRandom().nextInt(3);
    		
    		if (n == 0) {
    			fragment.addTag(tags[0]);
    		}
    		else if (n == 1) {
    			fragment.addTag(tags[1]);
    		}
    		else {
    			fragment.addTag(tags[0]);
    			fragment.addTag(tags[1]);
    		}
    		
			fragmentDao.save(fragment);
		}
    	
    	{
			final String searchPhrase = "title:.wrap() ";
			final SearchParams sp = new SearchParams(searchPhrase);
			assertEquals(1, sp.getKeywords().size());
			final Criteria crit = SearchQueryCreator.newQuery(sp, session);
			assertNotNull(crit);
			final List<Fragment> results = crit.list();
			assertEquals(1, results.size());
			assertTrue(results.contains(fragments[8]));
		}
    	{
    		final String searchPhrase = "title:. () ";
    		final SearchParams sp = new SearchParams(searchPhrase);
    		assertEquals(1, sp.getKeywords().size());
    		final Criteria crit = SearchQueryCreator.newQuery(sp, session);
    		final List<Fragment> results = crit.list();
    		assertEquals(fragments.length, results.size());
    	}
    	{
    		final String searchPhrase = "anytitle:pend wrap ";
    		final SearchParams sp = new SearchParams(searchPhrase);
    		assertEquals(1, sp.getKeywords().size());
    		final Criteria crit = SearchQueryCreator.newQuery(sp, session);
    		final List<Fragment> results = crit.list();
    		assertEquals(5, results.size());
    		assertTrue(results.contains(fragments[2]));
    		assertTrue(results.contains(fragments[3]));
    		assertTrue(results.contains(fragments[8]));
    		assertTrue(results.contains(fragments[9]));
    		assertTrue(results.contains(fragments[10]));
    	}
    	{
    		final String searchPhrase = "title:before()  text:before";
    		final SearchParams sp = new SearchParams(searchPhrase);
    		assertEquals(2, sp.getKeywords().size());
    		final Criteria crit = SearchQueryCreator.newQuery(sp, session);
    		final List<Fragment> results = crit.list();
    		assertEquals(1, results.size());
    		assertTrue(results.contains(fragments[4]));
    	}
    	{
    		final String searchPhrase = "text";
    		final SearchParams sp = new SearchParams(searchPhrase);
    		assertEquals(1, sp.getKeywords().size());
    		final Criteria crit = SearchQueryCreator.newQuery(sp, session);
    		final List<Fragment> results = crit.list();
    		assertEquals(2, results.size());
    		assertTrue(results.contains(fragments[0]));
    		assertTrue(results.contains(fragments[1]));
    	}
    	{
    		final String searchPhrase = ":replace";
    		final SearchParams sp = new SearchParams(searchPhrase);
    		assertEquals(1, sp.getKeywords().size());
    		final Criteria crit = SearchQueryCreator.newQuery(sp, session);
    		final List<Fragment> results = crit.list();
    		assertEquals(2, results.size());
    		assertTrue(results.contains(fragments[0]));
    		assertTrue(results.contains(fragments[6]));
    	}
    	{
    		final String searchPhrase = ":replace/w";
    		final SearchParams sp = new SearchParams(searchPhrase);
    		assertEquals(1, sp.getKeywords().size());
    		final Criteria crit = SearchQueryCreator.newQuery(sp, session);
    		final List<Fragment> results = crit.list();
    		assertEquals(0, results.size());
    	}
    	{
    		final String searchPhrase = ": text html";
    		final SearchParams sp = new SearchParams(searchPhrase);
    		assertEquals(1, sp.getKeywords().size());
    		final Criteria crit = SearchQueryCreator.newQuery(sp, session);
    		final List<Fragment> results = crit.list();
    		assertEquals(1, results.size());
    		assertTrue(results.contains(fragments[1]));
    	}
    	{
    		final String searchPhrase = "any: text html";
    		final SearchParams sp = new SearchParams(searchPhrase);
    		assertEquals(1, sp.getKeywords().size());
    		final Criteria crit = SearchQueryCreator.newQuery(sp, session);
    		final List<Fragment> results = crit.list();
    		assertEquals(4, results.size());
    		assertTrue(results.contains(fragments[0]));
    		assertTrue(results.contains(fragments[1]));
    		assertTrue(results.contains(fragments[8]));
    		assertTrue(results.contains(fragments[9]));
    	}
    	{
    		final String searchPhrase = "text:HTML/c";
    		final SearchParams sp = new SearchParams(searchPhrase);
    		assertEquals(1, sp.getKeywords().size());
    		final Criteria crit = SearchQueryCreator.newQuery(sp, session);
    		final List<Fragment> results = crit.list();
    		assertEquals(2, results.size());
    		assertTrue(results.contains(fragments[8]));
    		assertTrue(results.contains(fragments[9]));
    	}
    	{
    		final String searchPhrase = ": \"lets you insert\"";
    		final SearchParams sp = new SearchParams(searchPhrase);
    		assertEquals(1, sp.getKeywords().size());
    		final Criteria crit = SearchQueryCreator.newQuery(sp, session);
    		final List<Fragment> results = crit.list();
    		assertEquals(3, results.size());
    		assertTrue(results.contains(fragments[1]));
    		assertTrue(results.contains(fragments[2]));
    		assertTrue(results.contains(fragments[3]));
    	}
    }
    
}
