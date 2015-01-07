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
		assertTrue(fragments.size() > 0);
		
		for (Fragment f : fragments) {
			assertNotNull(f.getId());
		}
	}

}
