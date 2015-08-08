package com.civilizer.test.domain;

import static org.junit.Assert.*;

import java.util.*;

import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;
import org.junit.Before;
import org.junit.Test;

import com.civilizer.domain.*;

public class DomainFragmentTest {
	
	private List<Fragment> fragments = new ArrayList<Fragment>();
	
	private static Collection<String> buildFragmentTitleList(Collection<Fragment> fragments) {
        List<String> fragmentNames = new ArrayList<String>();
        for (Fragment f : fragments) {
            fragmentNames.add(f.getTitle());
        }
        assertEquals(fragmentNames.size(), fragments.size());
        return fragmentNames;
    }

	@Before
	public void setUp() throws Exception {
		assertNotNull(fragments);
		
		for (int i = 0; i < 16; i++) {
			Fragment frg = new Fragment(
					"fragment " + fragments.size()
					, "Some content " + fragments.size()
					, null
					);
			assertNotNull(frg);
			frg.setId(new Long(i));
	        fragments.add(frg);
		}
	}
	
	@Test
	public void testIDsAreValid() {
		assertFalse(fragments.isEmpty());
		
		for (Fragment f : fragments) {
			assertNotNull(f.getId());
		}
	}
	
	@Test
    public void testEquality() {
	    for (Fragment x : fragments) {
	        // reflexive:
	        // for any non-null reference value x, x.equals(x) should return true.
	        assertEquals(true, x.equals(x));
	        
	        Fragment y = new Fragment();
	        y.setId(x.getId());
	        Fragment z = new Fragment();
	        z.setId(x.getId());

	        // symmetric:
	        // for any non-null reference values x and y, x.equals(y) should return true if and only if y.equals(x) returns true.
	        assertEquals(true, x.equals(y));
	        assertEquals(true, y.equals(x));
	        
	        // transitive:
	        // for any non-null reference values x, y, and z, if x.equals(y) returns true and y.equals(z) returns true, then x.equals(z) should return true.
	        assertEquals(true, y.equals(z));
	        assertEquals(true, x.equals(z));
	        
	        // consistent:
	        // for any non-null reference values x and y, multiple invocations of x.equals(y) consistently return true or consistently return false, 
	        // provided no information used in equals comparisons on the objects is modified.
	        assertEquals(true, x.equals(y));
	        assertEquals(true, x.equals(y));
	        assertEquals(true, x.equals(y));

	        //For any non-null reference value x, x.equals(null) should return false.
	        assertEquals(false, x.equals(null));
        }
    }
	
	@Test
	public void testMethod_getFragmentTitleCollectionFrom() {
		assertFalse(fragments.isEmpty());
		
		Collection<String> actualC = Fragment.getFragmentTitleCollectionFrom(fragments);;
		Object[] actual = actualC.toArray();
		Collection<String> expectedC = buildFragmentTitleList(fragments);
		Object[] expected = expectedC.toArray();
		assertArrayEquals(expected, actual);
	}
	
	@Test
	public void testSort() {
	    for (Fragment f : fragments) {
	        assertNotNull(f.getCreationDatetime());
	        assertNotNull(f.getUpdateDatetime());
	        assertNotNull(f.getTitle());
	        assertNotNull(f.getId());
	    }
	    
	    assertEquals(FragmentOrder.UPDATE_DATETIME.ordinal(), 0);
	    assertEquals(FragmentOrder.CREATION_DATETIME.ordinal(), 1);
	    assertEquals(FragmentOrder.TITLE.ordinal(), 2);
	    assertEquals(FragmentOrder.ID.ordinal(), 3);
	    assertEquals(FragmentOrder.COUNT.ordinal(), 4);
	    
	    DateTimeComparator dtCmptr = DateTimeComparator.getInstance();
        assertNotNull(dtCmptr);
        
        Collections.shuffle(fragments);
        Fragment.sort(fragments, FragmentOrder.UPDATE_DATETIME, false);
        for (int i=1; i<fragments.size(); ++i) {
            Fragment f0 = fragments.get(i - 1);
            Fragment f1 = fragments.get(i);
            DateTime dt0 = f0.getUpdateDatetime();
            DateTime dt1 = f1.getUpdateDatetime();
            int r = dtCmptr.compare(dt0, dt1);
            assertTrue(r >= 0);
        }

        Collections.shuffle(fragments);
        Fragment.sort(fragments, FragmentOrder.CREATION_DATETIME, false);
        for (int i=1; i<fragments.size(); ++i) {
            Fragment f0 = fragments.get(i - 1);
            Fragment f1 = fragments.get(i);
            DateTime dt0 = f0.getCreationDatetime();
            DateTime dt1 = f1.getCreationDatetime();
            int r = dtCmptr.compare(dt0, dt1);
            assertTrue(r >= 0);
        }

        Collections.shuffle(fragments);
        Fragment.sort(fragments, FragmentOrder.TITLE, true);
        for (int i=1; i<fragments.size(); ++i) {
            Fragment f0 = fragments.get(i - 1);
            Fragment f1 = fragments.get(i);
            String s0 = f0.getTitle();
            String s1 = f1.getTitle();
            int r = s0.compareToIgnoreCase(s1);
            assertTrue(r <= 0);
        }
        
        Collections.shuffle(fragments);
        Fragment.sort(fragments, FragmentOrder.ID, true);
        for (int i=1; i<fragments.size(); ++i) {
            Fragment f0 = fragments.get(i - 1);
            Fragment f1 = fragments.get(i);
            long id0 = f0.getId();
            long id1 = f1.getId();
            assertTrue(id0 < id1);
        }
	}
	
	@Test
    public void testMethod_matchesTagKeywords() {
	    final List<Tag> tags = DomainTagTest.buildTags(
	        "my tag", "your tag", "Tag000",      
	        "~tag~", "xxxYyy", "#bookmark"
	    );
	    
	    assertEquals(false, fragments.isEmpty());
	    Fragment f = fragments.get(0);
	    for (Tag t : tags) {
	        f.addTag(t);
	    }
	    
	    assertEquals(tags.size(), f.getTags().size());
	    
	    {
            final String words = "tag:tag";
            final SearchParams.Keywords keywords = new SearchParams.Keywords(words);
            assertEquals(SearchParams.TARGET_TAG, keywords.getTarget());
            assertEquals(false, keywords.getWords().isEmpty());
            assertEquals(true, f.matchesTagKeywords(keywords));
        }
	    {
	        final String words = "anytag:zzz";
	        final SearchParams.Keywords keywords = new SearchParams.Keywords(words);
	        assertEquals(false, f.matchesTagKeywords(keywords));
	    }
	    {
	        final String words = "tag:tag/e Tag/b xxY/c";
	        final SearchParams.Keywords keywords = new SearchParams.Keywords(words);
	        assertEquals(true, f.matchesTagKeywords(keywords));
	    }
	    {
	        final String words = "tag:tag/e Tag/b xxY/c zzz";
	        final SearchParams.Keywords keywords = new SearchParams.Keywords(words);
	        assertEquals(false, f.matchesTagKeywords(keywords));
	    }
	    {
	        final String words = "tag:tag ^#.*/r";
	        final SearchParams.Keywords keywords = new SearchParams.Keywords(words);
	        assertEquals(true, f.matchesTagKeywords(keywords));
	    }
	    {
	        final String words = "tag:tag #/-";
	        final SearchParams.Keywords keywords = new SearchParams.Keywords(words);
	        assertEquals(false, f.matchesTagKeywords(keywords));
	    }
	    {
	        final String words = "tag:\"my tag\" zzz/-";
	        final SearchParams.Keywords keywords = new SearchParams.Keywords(words);
	        assertEquals(true, f.matchesTagKeywords(keywords));
	    }
	    {
	        final String words = "anytag:\"my tag\"/be zzz";
	        final SearchParams.Keywords keywords = new SearchParams.Keywords(words);
	        assertEquals(true, keywords.isAny());
	        assertEquals(true, f.matchesTagKeywords(keywords));
	    }
	    {
	        final String words = "anytag: zzz/- zzz www";
	        final SearchParams.Keywords keywords = new SearchParams.Keywords(words);
	        assertEquals(true, f.matchesTagKeywords(keywords));
	    }
	}
            
	
}
