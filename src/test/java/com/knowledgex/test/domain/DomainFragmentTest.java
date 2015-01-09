package com.knowledgex.test.domain;

import static org.junit.Assert.*;

import java.util.*;

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
	public void testMethod_getFragmentTitleListFrom() {
		assertFalse(fragments.isEmpty());
		
		Collection<String> actualC = Fragment.getFragmentTitleCollectionFrom(fragments);;
		Object[] actual = actualC.toArray();
		Collection<String> expectedC = buildFragmentTitleList(fragments);
		Object[] expected = expectedC.toArray();
		assertArrayEquals(expected, actual);
	}

}
