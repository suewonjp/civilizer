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
	private static Log logger = TestUtil.newLogger(DomainFragmentTest.class);
	
	private List<Tag> tags = new ArrayList<Tag>();
	
	private static Collection<String> buildTagNameList(Collection<Tag> tags) {
        List<String> tagNames = new ArrayList<String>();
        for (Tag t : tags) {
            tagNames.add(t.getTagName());
        }
        assertEquals(tagNames.size(), tags.size());
        return tagNames;
    }
	
	@SuppressWarnings("unchecked")
    private static void buildTagHierarchy(Collection<Tag> tags) {
	    assertNotNull(tags);
	    assertFalse(tags.isEmpty());
	    
	    Random r = TestUtil.getRandom();
	    final int depthCount = 3;

	    Object[] tagsPerDepth = new Object[depthCount];
	    for (int i=0; i<depthCount; ++i) {
	        tagsPerDepth[i] = new ArrayList<Tag>();
	    }
	    
	    for (Tag t : tags) {
	        int d = r.nextInt(depthCount);
	        assertTrue(0 <= d && d < depthCount);
	        ((List<Tag>) tagsPerDepth[d]).add(t);
	    }
	    
	    for (int i=1; i<depthCount; ++i) {
	        List<Tag> parentTags = (List<Tag>) tagsPerDepth[i - 1];
	        if (parentTags.isEmpty()) {
	            continue;
	        }
	        List<Tag> childTags = new ArrayList<Tag>((List<Tag>) tagsPerDepth[i]);
	        int pi = 0;
	        while (childTags.isEmpty() == false) {
	            Tag child = childTags.get(childTags.size() - 1);
	            Tag parent = parentTags.get(pi);
	            if (r.nextBoolean()) {
	                parent.addChild(child);
	                boolean removed = childTags.remove(child);
	                assertTrue(removed);
	            }
	            pi = (pi + 1) % parentTags.size();
	        }
	    }
	    
	    for (int i=1; i<depthCount; ++i) {
            List<Tag> parentTags = (List<Tag>) tagsPerDepth[i - 1];
            List<Tag> childTags = (List<Tag>) tagsPerDepth[i];
            for (Tag p : parentTags) {
                Collection<Tag> children = p.getChildren();
                for (Tag c : children) {
                    assertTrue(childTags.contains(c));                    
                }
            }
	    }
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
		
		buildTagHierarchy(tags);
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
        Collection<String> nameList = buildTagNameList(tags);
        String expected = new String();
        for (String s : nameList) {
        	expected += s + ",";
        }
        assertEquals(expected, actual);
    }
	
	@Test
	public void testMethod_getTagNameCollectionFrom() {
		assertFalse(tags.isEmpty());
		
		Collection<String> actualC = Tag.getTagNameCollectionFrom(tags);
		Object[] actual = actualC.toArray();
		Collection<String> expectedC = buildTagNameList(tags);
		Object[] expected = expectedC.toArray();
		assertArrayEquals(expected, actual);
		
		String actualS = Tag.getTagNamesFrom(tags);
		actualC = Tag.getTagNameCollectionFrom(actualS);
		actual = actualC.toArray();
		assertArrayEquals(expected, actual);
		
		//--- test against edge conditions
		final String delim = Tag.TAG_NAME_DELIMITER;
		actualC = Tag.getTagNameCollectionFrom((String)null);
		assertTrue(actualC != null && actualC.isEmpty());
		
		actualC = Tag.getTagNameCollectionFrom("");
		assertTrue(actualC != null && actualC.isEmpty());

		actualC = Tag.getTagNameCollectionFrom("  ");
		assertTrue(actualC != null && actualC.isEmpty());
		
		actualC = Tag.getTagNameCollectionFrom(delim);
		assertTrue(actualC != null && actualC.isEmpty());
		
		actualC = Tag.getTagNameCollectionFrom(delim+delim+delim);
		assertTrue(actualC != null && actualC.isEmpty());

		actualC = Tag.getTagNameCollectionFrom(delim+" \t\n "+delim);
		assertTrue(actualC != null && actualC.isEmpty());
		
		actualC = Tag.getTagNameCollectionFrom("\t\n tag0  \n");
		assertTrue(actualC != null && actualC.size() == 1);
		for (String s : actualC) {
			assertEquals(s, "tag0");
		}
		
		actualC = Tag.getTagNameCollectionFrom(delim+"tag0"+delim+delim);
		assertTrue(actualC != null && actualC.size() == 1);
		for (String s : actualC) {
			assertEquals(s, "tag0");
		}

		actualC = Tag.getTagNameCollectionFrom(delim+" \ttag0  "+delim+delim);
		assertTrue(actualC != null && actualC.size() == 1);
		for (String s : actualC) {
			assertEquals(s, "tag0");
		}
		
		actualC = Tag.getTagNameCollectionFrom(delim+delim+delim+"tag0"+delim);
		assertTrue(actualC != null && actualC.size() == 1);
		for (String s : actualC) {
			assertEquals(s, "tag0");
		}
		
		actualC = Tag.getTagNameCollectionFrom("tag0"+delim+delim+"tag1");
		assertTrue(actualC != null && actualC.size() == 2);
		List<String> list = new ArrayList<String>(actualC);
		assertEquals(list.get(0), "tag0");
		assertEquals(list.get(1), "tag1");
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
	
	@Test
	public void testMethod_containsId() {
		assertFalse(tags.isEmpty());
		boolean result = false;
		Collection<Tag> emptyTagCollection = new ArrayList<Tag>();
		long id = tags.get(0).getId();
		
		result = Tag.containsId(null, id);
		assertFalse(result);
		assertTrue(emptyTagCollection.isEmpty());
		result = Tag.containsId(emptyTagCollection, id);
		assertFalse(result);
		
		for (Tag t : tags) {
			result = Tag.containsId(tags, t.getId());
			assertTrue(result);
		}
	}
	
	@Test
	public void testMethod_containsName() {
		assertFalse(tags.isEmpty());
		boolean result = false;
		Collection<Tag> emptyTagCollection = new ArrayList<Tag>();
		
		result = Tag.containsName(null, null);
		assertTrue(result);
		assertTrue(emptyTagCollection.isEmpty());
		result = Tag.containsName(emptyTagCollection, "");
		assertTrue(result);
		result = Tag.containsName(null, "");
		assertTrue(result);
		assertTrue(emptyTagCollection.isEmpty());
		result = Tag.containsName(emptyTagCollection, null);
		assertTrue(result);
		
		result = Tag.containsName(tags, "");
		assertFalse(result);
		result = Tag.containsName(null, tags.get(0).getTagName());
		assertFalse(result);
		result = Tag.containsName(emptyTagCollection, tags.get(0).getTagName());
		assertFalse(result);
		
		for (Tag t : tags) {
			result = Tag.containsName(tags, t.getTagName());
			assertTrue(result);
		}
	}
	
	@Test
	public void testMethod_getTopParentTags() {
	    assertFalse(tags.isEmpty());
	    
	    // Edge cases
	    Collection<Tag> topParents = null;
	    topParents = Tag.getTopParentTags(null);
	    assertNull(topParents);
	    topParents = Tag.getTopParentTags(new ArrayList<Tag>());
	    assertNull(topParents);
	    
	    topParents = Tag.getTopParentTags(tags);
	    assertFalse(topParents.isEmpty());
	    for (Tag t : tags) {
	        Collection<Tag> children = t.getChildren();
	        for (Tag c : children) {
	            assertFalse(topParents.contains(c));
	        }
	    }
	}
	
//	@Test
//	public void testConvertToAndFromJsonFormat() {
//		final GsonBuilder builder = new GsonBuilder()
//	       .registerTypeAdapter(DateTime.class, new JodaDateTimeConverter());
//	    final Gson gson = builder.create();
//		final String jsonString = gson.toJson(tags.get(0));
//		assertNotNull(jsonString);
////		logger.info(jsonString);
//		Tag frg = gson.fromJson(jsonString, Tag.class);
//		assertEquals(tags.get(0), frg);
//	}

}
