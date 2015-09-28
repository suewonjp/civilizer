package com.civilizer.test.dao;

import static org.junit.Assert.*;

import java.lang.reflect.Array;
import java.util.*;

import org.junit.*;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.SessionFactoryUtils;

import com.civilizer.dao.hibernate.SearchQueryCreator;
import com.civilizer.domain.*;
import com.civilizer.test.helper.TestUtil;
import com.civilizer.utils.Pair;

public class SearchTest extends DaoTest {
	
	private Session session;
	
	@BeforeClass
    public static void setUpBeforeClass() throws Exception {
        DaoTest.setUpBeforeClass(
                "classpath:datasource-context-h2-empty.xml"
                , SearchTest.class
                );
        runSqlScript("db_test/drop.sql", "db_test/schema.sql");
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
    
    private static boolean matches(List<?> results, Object src, int...idx) {
        final Class<?> elemType = src.getClass();
        assertEquals(true, elemType.isArray());
        boolean match = true;
        for (int i : idx) {
            match &= results.contains(Array.get(src, i));
        }
        return match;
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
			assertEquals(4, results.size());
			assertEquals(true, matches(results, tags, 0, 1, 5, 6));
		}
    	{
    		final Criteria crit = session.createCriteria(Tag.class);
    		crit.add(Restrictions.like("tagName", "_tag"));
    		final List<Tag> results = crit.list();
    		assertEquals(1, results.size());
    		assertEquals(true, matches(results, tags, 1));
    	}
    	{
    		final Criteria crit = session.createCriteria(Tag.class);
    		crit.add(Restrictions.sqlRestriction("TAG_NAME like 'my tag'"));
    		final List<Tag> results = crit.list();
    		assertEquals(1, results.size());
    		assertEquals(true, matches(results, tags, 5));
    	}
    	{
    		final Criteria crit = session.createCriteria(Tag.class);
    		crit.add(Restrictions.ilike("tagName", "tag%"));
    		final List<Tag> results = crit.list();
    		assertEquals(4, results.size());
    		assertEquals(true, matches(results, tags, 0, 2, 3, 4));
    	}
    }
    
    @Test
    public void testMethod_SearchQueryCreator_getPatternFromKeyword() {
      {
          final String word = "\"My keyword\"";
          final SearchParams.Keyword kw = new SearchParams.Keyword(word, false);
          assertEquals(false, kw.isWholeWord());
          assertEquals(true, kw.isValid());
          final Pair<String, Character> tmp = SearchQueryCreator.getPatternFromKeyword(kw);
          final String pattern = tmp.getFirst();
          assertEquals(pattern, "%My keyword%");
          assertEquals(true, tmp.getSecond() == 0);
      }
      {
          final String word = "hello/w";
          final SearchParams.Keyword kw = new SearchParams.Keyword(word, false);
          assertEquals(true, kw.isWholeWord());
          assertEquals(true, kw.isValid());
          final Pair<String, Character> tmp = SearchQueryCreator.getPatternFromKeyword(kw);
          final String pattern = tmp.getFirst();
          assertEquals(pattern, "hello");
          assertEquals(true, tmp.getSecond() == 0);
      }
      {
          final String word = "with_underscore";
          final SearchParams.Keyword kw = new SearchParams.Keyword(word, false);
          assertEquals(true, kw.isValid());
          final Pair<String, Character> tmp = SearchQueryCreator.getPatternFromKeyword(kw);
          final String pattern = tmp.getFirst();          
          assertEquals(pattern, "%with!_underscore%");
          assertEquals(true, tmp.getSecond() == '!');
      }
      {
          final String word = "with%percent";
          final SearchParams.Keyword kw = new SearchParams.Keyword(word, false);
          assertEquals(true, kw.isValid());
          final Pair<String, Character> tmp = SearchQueryCreator.getPatternFromKeyword(kw);
          final String pattern = tmp.getFirst();          
          assertEquals(pattern, "%with!%percent%");
          assertEquals(true, tmp.getSecond() == '!');
      }
    }
    
    protected Pair<Fragment[], Tag[]> createTestData() {
        final Tag[] tags = {
                newTag("my tag"),
                newTag("your tag :-)"),
                newTag("nobody's tag"),
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
        
        return new Pair<Fragment[], Tag[]>(fragments, tags);
    }
    
    @Test
    public void testBasicSearch() {
        final Pair<Fragment[], Tag[]> pair = createTestData();
        final Fragment[] fragments = pair.getFirst();
    	
    	{
			final String searchPhrase = "title:.wrap() ";
			final SearchParams sp = new SearchParams(searchPhrase);
			assertEquals(1, sp.getKeywords().size());
			final List<Fragment> results = fragmentDao.findBySearchParams(sp, null);
			assertEquals(1, results.size());
			assertEquals(true, matches(results, fragments, 8));
		}
    	{
    		final String searchPhrase = "title:. () ";
    		final SearchParams sp = new SearchParams(searchPhrase);
    		assertEquals(1, sp.getKeywords().size());
            final List<Fragment> results = fragmentDao.findBySearchParams(sp, null);
    		assertEquals(fragments.length, results.size());
    	}
    	{
    		final String searchPhrase = "anytitle:pend wrap ";
    		final SearchParams sp = new SearchParams(searchPhrase);
    		assertEquals(1, sp.getKeywords().size());
            final List<Fragment> results = fragmentDao.findBySearchParams(sp, null);
    		assertEquals(5, results.size());
    		assertEquals(true, matches(results, fragments, 2, 3, 8, 9, 10));
    	}
    	{
    		final String searchPhrase = "title:before()  text:before";
    		final SearchParams sp = new SearchParams(searchPhrase);
    		assertEquals(2, sp.getKeywords().size());
            final List<Fragment> results = fragmentDao.findBySearchParams(sp, null);
    		assertEquals(1, results.size());
    		assertEquals(true, matches(results, fragments, 4));
    	}
    	{
    		final String searchPhrase = "text";
    		final SearchParams sp = new SearchParams(searchPhrase);
    		assertEquals(1, sp.getKeywords().size());
            final List<Fragment> results = fragmentDao.findBySearchParams(sp, null);
    		assertEquals(2, results.size());
    		assertEquals(true, matches(results, fragments, 0, 1));
    	}
    	{
    		final String searchPhrase = ":replace";
    		final SearchParams sp = new SearchParams(searchPhrase);
    		assertEquals(1, sp.getKeywords().size());
            final List<Fragment> results = fragmentDao.findBySearchParams(sp, null);
    		assertEquals(2, results.size());
    		assertEquals(true, matches(results, fragments, 0, 6));
    	}
    	{
    		final String searchPhrase = ": text html";
    		final SearchParams sp = new SearchParams(searchPhrase);
    		assertEquals(1, sp.getKeywords().size());
            final List<Fragment> results = fragmentDao.findBySearchParams(sp, null);
    		assertEquals(1, results.size());
    		assertEquals(true, matches(results, fragments, 1));
    	}
    	{
    		final String searchPhrase = "any: text html";
    		final SearchParams sp = new SearchParams(searchPhrase);
    		assertEquals(1, sp.getKeywords().size());
            final List<Fragment> results = fragmentDao.findBySearchParams(sp, null);
    		assertEquals(4, results.size());
    		assertEquals(true, matches(results, fragments, 0, 1, 8, 9));
    	}
    	{
    		final String searchPhrase = "text:HTML/c";
    		final SearchParams sp = new SearchParams(searchPhrase);
    		assertEquals(1, sp.getKeywords().size());
            final List<Fragment> results = fragmentDao.findBySearchParams(sp, null);
    		assertEquals(2, results.size());
    		assertEquals(true, matches(results, fragments, 8, 9));
    	}
    	{
    		final String searchPhrase = ": \"lets you insert\"";
    		final SearchParams sp = new SearchParams(searchPhrase);
    		assertEquals(1, sp.getKeywords().size());
            final List<Fragment> results = fragmentDao.findBySearchParams(sp, null);
    		assertEquals(3, results.size());
    		assertEquals(true, matches(results, fragments, 1, 2, 3));
    	}
    }
    
    @Test
    public void testWordBoundarySearch() {
    	final Pair<Fragment[], Tag[]> pair = createTestData();
        final Fragment[] fragments = pair.getFirst();
        
        {
    		final String searchPhrase = ":replace/w";
    		final SearchParams sp = new SearchParams(searchPhrase);
    		assertEquals(1, sp.getKeywords().size());
            final List<Fragment> results = fragmentDao.findBySearchParams(sp, null);
    		assertEquals(0, results.size());
    	}
        {
        	final String searchPhrase = ":wrap/w ";
        	final SearchParams sp = new SearchParams(searchPhrase);
        	assertEquals(1, sp.getKeywords().size());
            final List<Fragment> results = fragmentDao.findBySearchParams(sp, null);
        	assertEquals(1, results.size());
            assertEquals(true, matches(results, fragments, 8));
        }
        {
        	final String searchPhrase = ":wrap/b ";
        	final SearchParams sp = new SearchParams(searchPhrase);
        	assertEquals(1, sp.getKeywords().size());
            final List<Fragment> results = fragmentDao.findBySearchParams(sp, null);
        	assertEquals(2, results.size());
        	assertEquals(true, matches(results, fragments, 8, 9));
        }
        {
        	final String searchPhrase = ":wrap/e ";
        	final SearchParams sp = new SearchParams(searchPhrase);
        	assertEquals(1, sp.getKeywords().size());
            final List<Fragment> results = fragmentDao.findBySearchParams(sp, null);
        	assertEquals(2, results.size());
            assertEquals(true, matches(results, fragments, 8, 10));
        }
        {
        	final String searchPhrase = "tag/b ";
        	final SearchParams sp = new SearchParams(searchPhrase);
        	assertEquals(1, sp.getKeywords().size());
            final List<Fragment> results = fragmentDao.findBySearchParams(sp, null);
        	assertEquals(5, results.size());
        	assertEquals(true, matches(results, fragments, 5, 6, 8, 9, 10));
        }
    }
    
    @Test
    public void testRegexSearch() {
    	final Pair<Fragment[], Tag[]> pair = createTestData();
        final Fragment[] fragments = pair.getFirst();
    	
    	{
			final String searchPhrase = "title:\\.\\w+()/r ";
			final SearchParams sp = new SearchParams(searchPhrase);
			assertEquals(1, sp.getKeywords().size());
            final List<Fragment> results = fragmentDao.findBySearchParams(sp, null);
			assertEquals(fragments.length, results.size());
		}
    	{
    		final String searchPhrase = "text:\\(.*\\)/r ";
    		final SearchParams sp = new SearchParams(searchPhrase);
    		assertEquals(1, sp.getKeywords().size());
            final List<Fragment> results = fragmentDao.findBySearchParams(sp, null);
    		assertEquals(3, results.size());
            assertEquals(true, matches(results, fragments, 1, 5, 6));
    	}
    	{
    		final String searchPhrase = "title:[a-z]+[A-Z]+/r ";
    		final SearchParams sp = new SearchParams(searchPhrase);
    		assertEquals(1, sp.getKeywords().size());
            final List<Fragment> results = fragmentDao.findBySearchParams(sp, null);
    		assertEquals(2, results.size());
    		assertEquals(true, matches(results, fragments, 6, 9));
    	}
    }
    
    @Test
    public void testInverseSearch() {
    	final Pair<Fragment[], Tag[]> pair = createTestData();
        final Fragment[] fragments = pair.getFirst();
    	
    	{
			final String searchPhrase = "text:selection/w-";
			final SearchParams sp = new SearchParams(searchPhrase);
			assertEquals(1, sp.getKeywords().size());
            final List<Fragment> results = fragmentDao.findBySearchParams(sp, null);
			assertEquals(3, results.size());
			assertEquals(true, matches(results, fragments, 1, 2, 3));
		}
    	{
    		final String searchPhrase = "title:wrap un/-";
    		final SearchParams sp = new SearchParams(searchPhrase);
    		assertEquals(1, sp.getKeywords().size());
            final List<Fragment> results = fragmentDao.findBySearchParams(sp, null);
    		assertEquals(2, results.size());
    		assertEquals(true, matches(results, fragments, 8, 9));
    	}
    	{
    		final String searchPhrase = "text:content contents/w-";
    		final SearchParams sp = new SearchParams(searchPhrase);
    		assertEquals(1, sp.getKeywords().size());
            final List<Fragment> results = fragmentDao.findBySearchParams(sp, null);
    		assertEquals(4, results.size());
    		assertEquals(true, matches(results, fragments, 2, 3, 4, 5));
    	}
    }
    
    @Test
    public void testSearchWithTagRestriction() {
    	final Pair<Fragment[], Tag[]> pair = createTestData();
        final Fragment[] fragments = pair.getFirst();
        final List<Tag> tags = Arrays.asList(pair.getSecond());
        
        {
        	final String searchPhrase = "anytag:\"my tag\" \"your tag\"";
        	final SearchParams sp = new SearchParams(searchPhrase);
        	assertEquals(1, sp.getKeywords().size());
        	final List<Fragment> results = fragmentDao.findBySearchParams(sp, tags);
        	assertEquals(fragments.length, results.size());
        }
        {
            final String searchPhrase = "anytag:\"nonexistent tag\" \"another nonexistent tag\"";
            final SearchParams sp = new SearchParams(searchPhrase);
            assertEquals(1, sp.getKeywords().size());
            final List<Fragment> results = fragmentDao.findBySearchParams(sp, tags);
            assertEquals(0, results.size());
        }
        {
        	final Tag tag = tags.get(2); // nobody's tag
        	final String tagName = tag.getTagName();
        	final String searchPhrase = "tag:\"" + tagName + "\"/w";
        	final SearchParams sp = new SearchParams(searchPhrase);
        	final List<Fragment> results = fragmentDao.findBySearchParams(sp, tags);
        	assertEquals(0, results.size());
        }
        {
        	final Tag tag = tags.get(TestUtil.getRandom().nextInt(tags.size()));
        	final String tagName = tag.getTagName();
			final String searchPhrase = "tag:\"" + tagName + "\"/w";
			final SearchParams sp = new SearchParams(searchPhrase);
			assertEquals(1, sp.getKeywords().size());
			final List<Fragment> results = fragmentDao.findBySearchParams(sp, tags);
			for (Fragment fragment : results) {
				assertEquals(true, fragment.containsTagName(tagName));
			}
		}
        {
            // comma-separated tag list should be equivalent to space-separated tag list
            int count = 0;
            for (Fragment f : fragments) {
                if (f.getTags().size() >= 2)
                    ++count;
            }
            String searchPhrase = "tag:\"my tag\" \"your tag\"";
            SearchParams sp = new SearchParams(searchPhrase);
            List<Fragment> results = fragmentDao.findBySearchParams(sp, tags);
            assertEquals(count, results.size());
            searchPhrase = "tag:\"my tag\", \"your tag\"";
            sp = new SearchParams(searchPhrase);
            results = fragmentDao.findBySearchParams(sp, tags);
            assertEquals(count, results.size());
        }
    }
    
    @Test
    public void testSearchWithFragmentId() {
    	final Pair<Fragment[], Tag[]> pair = createTestData();
        final Fragment[] fragments = pair.getFirst();
        
        // single id;
        {
        	final int index = TestUtil.getRandom().nextInt(fragments.length);
        	final long id = fragments[index].getId();
//        	final String searchPhrase = "id:" + id;
        	final String searchPhrase = "id: \"" + id + "\""; // [NOTE] wrapping with double quotes will work
//        	final String searchPhrase = "id: '" + id + "'"; // [NOTE] wrapping with single quotes will NOT work
        	final SearchParams sp = new SearchParams(searchPhrase);
        	assertEquals(1, sp.getKeywords().size());
        	assertEquals(1, sp.getKeywords().get(0).getWords().size());
            final List<Fragment> results = fragmentDao.findBySearchParams(sp, null);
        	assertEquals(1, results.size());
        	assertEquals(new Long(id), results.get(0).getId());
        	assertEquals(fragments[index], results.get(0));
        }
        // multiple ids;
        {
        	final int[] indices = TestUtil.randomIndices(TestUtil.getRandom(), 2, fragments.length);
        	String searchPhrase = "id:";
        	for (int i : indices) {
				searchPhrase += fragments[i].getId() + " ";
			}
        	final SearchParams sp = new SearchParams(searchPhrase);
        	assertEquals(1, sp.getKeywords().size());
        	assertEquals(indices.length, sp.getKeywords().get(0).getWords().size());
            final List<Fragment> results = fragmentDao.findBySearchParams(sp, null);
        	assertEquals(indices.length, results.size());
        	final List<Fragment> list = Arrays.asList(fragments);
        	for (int i : indices) {
        		final long id = fragments[i].getId();
        		assertEquals(true, Fragment.containsId(list, id));
        	}
        }
    }
    
}
