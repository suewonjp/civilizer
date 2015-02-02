package com.knowledgex.test.domain;

import static org.junit.Assert.*;

import java.util.*;

import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;
import org.apache.commons.logging.Log;
import org.junit.Before;
import org.junit.Test;

import com.knowledgex.domain.*;
import com.knowledgex.test.util.TestUtil;

public class DomainFragmentTest {
	
	@SuppressWarnings("unused")
	private static Log log = TestUtil.newLogger(DomainFragmentTest.class);

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
	public void testMethod_getFragmentTitleCollectionFrom() {
		assertFalse(fragments.isEmpty());
		
		Collection<String> actualC = Fragment.getFragmentTitleCollectionFrom(fragments);;
		Object[] actual = actualC.toArray();
		Collection<String> expectedC = buildFragmentTitleList(fragments);
		Object[] expected = expectedC.toArray();
		assertArrayEquals(expected, actual);
	}
	
	@Test
	public void testSortByDate() {
	    for (Fragment f : fragments) {
            assertNotNull(f.getCreationDatetime());
            assertNotNull(f.getUpdateDatetime());
        }
	    
	    Comparator<Fragment> cmptrForCreationTime =
	            FragmentComparator.newComparatorForCreationDatetime();
	    assertNotNull(cmptrForCreationTime);
	    Comparator<Fragment> cmptrForUpdateTime =
	            FragmentComparator.newComparatorForUpdateDatetime();
	    assertNotNull(cmptrForUpdateTime);
	    DateTimeComparator dtCmptr = DateTimeComparator.getInstance();
	    
	    Collections.shuffle(fragments);
	    Collections.sort(fragments, cmptrForCreationTime);
	    for (int i=1; i<fragments.size(); ++i) {
	        Fragment f0 = fragments.get(i - 1);
	        Fragment f1 = fragments.get(i);
	        DateTime dt0 = f0.getCreationDatetime();
	        DateTime dt1 = f1.getCreationDatetime();
	        int r = dtCmptr.compare(dt0, dt1);
	        assertTrue(r <= 0);
	    }

	    Collections.shuffle(fragments);
	    Collections.sort(fragments, cmptrForUpdateTime);
	    for (int i=1; i<fragments.size(); ++i) {
	        Fragment f0 = fragments.get(i - 1);
	        Fragment f1 = fragments.get(i);
	        DateTime dt0 = f0.getUpdateDatetime();
	        DateTime dt1 = f1.getUpdateDatetime();
	        int r = dtCmptr.compare(dt0, dt1);
	        assertTrue(r <= 0);
	    }
	}

}
