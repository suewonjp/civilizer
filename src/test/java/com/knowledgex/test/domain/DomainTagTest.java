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
	
	private static List<String> buildTagNameList(List<Tag> tags) {
        List<String> tagNames = new ArrayList<String>();
        for (Tag t : tags) {
            Long id = t.getId();
            assertTrue(id >= 0);
            tagNames.add(t.getTagName());
        }
        assertEquals(tagNames.size(), tags.size());
        return tagNames;
    }

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
		assertFalse(tags.isEmpty());
		
		for (Tag t : tags) {
			assertNotNull(t.getId());
		}
	}
	
	@Test
	public void testMethod_getTagNamesFrom() {
        assertFalse(tags.isEmpty());
        String actual = Tag.getTagNamesFrom(tags);
        List<String> nameList = buildTagNameList(tags);
        String expected = new String();
        for (String s : nameList) {
        	expected += s + ", ";
        }
        assertEquals(expected, actual);
    }
	
	@Test
	public void testMethod_getTagFromName() {
        assertFalse(tags.isEmpty());
        Random r = TestUtil.getRandom();
        
        // The method should return the existing Tag if it accepts the name of an existing one.
        int idx = r.nextInt(tags.size());
        assertTrue(0 <= idx && idx < tags.size());
        Tag expected = tags.get(idx);
        String name = expected.getTagName();
        Tag actual = Tag.getTagFromName(name, tags);
        assertSame(expected, actual);
        
        // The method should return NULL if it accepts a non-existing name.
        String nonExistingName = "#$%#%#%#%$#%$#%$#%$#%#$%***&%!%";
        actual = Tag.getTagFromName(nonExistingName, tags);
        assertNull(actual);
    }

}
