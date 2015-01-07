package com.knowledgex.test.domain;

import static org.junit.Assert.*;

import java.util.*;

import org.apache.commons.logging.Log;
import org.junit.Before;
import org.junit.Test;

import com.knowledgex.domain.*;
import com.knowledgex.test.util.TestUtil;

public class DomainTagTest {
	
	@SuppressWarnings("unused")
	private static Log log = TestUtil.newLogger(DomainFragmentTest.class);
	
	private List<Tag> tags = new ArrayList<Tag>();

	@Before
	public void setUp() throws Exception {
		assertNotNull(tags);
		
		for (int i = 0; i < 16; i++) {
			Tag t = new Tag("tag " + tags.size());
			assertNotNull(t);
			t.setId(new Long(i));
	        tags.add(t);
		}
	}

	@Test
	public void testIDsAreValid() {
		assertTrue(tags.size() > 0);
		
		for (Tag t : tags) {
			assertNotNull(t.getId());
		}
	}

}
