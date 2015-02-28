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
	private static Log logger = TestUtil.newLogger(DomainFragmentTest.class);

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
	
//	@Test
//	public void testConvertToAndFromJsonFormat() {
//		final GsonBuilder builder = new GsonBuilder()
//	       .registerTypeAdapter(DateTime.class, new JodaDateTimeConverter());
//	    final Gson gson = builder.create();
//		final String jsonString = gson.toJson(fragments.get(0));
//		assertNotNull(jsonString);
////		logger.info(jsonString);
//		Fragment frg = gson.fromJson(jsonString, Fragment.class);
//		assertEquals(fragments.get(0), frg);
//	}
	
}
