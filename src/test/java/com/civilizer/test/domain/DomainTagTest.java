package com.civilizer.test.domain;

import static org.junit.Assert.*;

import java.util.*;

import org.junit.Before;
import org.junit.Test;

import com.civilizer.domain.*;
import com.civilizer.test.helper.TestUtil;

public class DomainTagTest {
	
	private List<Tag> tags;
	
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
	    assertEquals(false, tags.isEmpty());
	    
	    Random r = TestUtil.getRandom();
	    final int depthCount = 2 + r.nextInt(3);

	    Object[] tagsPerDepth = new Object[depthCount];
	    for (int i=0; i<depthCount; ++i) {
	        tagsPerDepth[i] = new ArrayList<Tag>();
	    }
	    
	    for (Tag t : tags) {
	        int d = r.nextInt(depthCount);
	        assertEquals(true, 0 <= d && d < depthCount);
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
	                assertEquals(true, removed);
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
                    assertEquals(true, childTags.contains(c));                    
                }
            }
	    }
	}
	
	public static List<Tag> buildTags(int count) {
	    List<Tag> tags = new ArrayList<Tag>();
	    for (int i = 0; i < count; i++) {
            Tag t = new Tag("tag " + tags.size());
            assertNotNull(t);
            t.setId(new Long(i));
            tags.add(t);
        }        
        buildTagHierarchy(tags);
        return tags;
	}

	public static List<Tag> buildTags(String...names) {
	    List<Tag> tags = new ArrayList<Tag>();
	    for (int i = 0; i < names.length; i++) {
	        Tag t = new Tag(names[i]);
	        assertNotNull(t);
	        t.setId(new Long(i + 1));
	        tags.add(t);
	    }        
	    buildTagHierarchy(tags);
	    return tags;
	}

	@Before
	public void setUp() throws Exception {
		tags = buildTags(16 + TestUtil.getRandom().nextInt(10));
	}

	@Test
	public void testIDsAreValid() {
		assertEquals(false, tags.isEmpty());
		
		for (Tag t : tags) {
			assertNotNull(t.getId());
		}
	}
    
    @Test
    public void testEquality() {
        for (Tag x : tags) {
            // reflexive:
            // for any non-null reference value x, x.equals(x) should return true.
            assertEquals(true, x.equals(x));
            
            Tag y = new Tag();
            y.setId(x.getId());
            Tag z = new Tag();
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
	public void testMethod_getTagNamesFrom() {
        assertEquals(false, tags.isEmpty());
        
        String actual = Tag.getTagNamesFrom(tags);
        Collection<String> nameList = buildTagNameList(tags);
        String expected = new String();
        for (String s : nameList) {
            assertEquals(0, Tag.findInvalidCharFromName(s));
        	expected += s + ",";
        }
        assertEquals(expected, actual);
    }
	
	@Test
	public void testMethod_getTagNameCollectionFrom() {
		assertEquals(false, tags.isEmpty());
		
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
		assertEquals(true, actualC != null && actualC.isEmpty());
		
		actualC = Tag.getTagNameCollectionFrom("");
		assertEquals(true, actualC != null && actualC.isEmpty());

		actualC = Tag.getTagNameCollectionFrom("  ");
		assertEquals(true, actualC != null && actualC.isEmpty());
		
		actualC = Tag.getTagNameCollectionFrom(delim);
		assertEquals(true, actualC != null && actualC.isEmpty());
		
		actualC = Tag.getTagNameCollectionFrom(delim+delim+delim);
		assertEquals(true, actualC != null && actualC.isEmpty());

		actualC = Tag.getTagNameCollectionFrom(delim+" \t\n "+delim);
		assertEquals(true, actualC != null && actualC.isEmpty());
		
		actualC = Tag.getTagNameCollectionFrom("\t\n tag0  \n");
		assertEquals(true, actualC != null && actualC.size() == 1);
		for (String s : actualC) {
			assertEquals(s, "tag0");
		}
		
		actualC = Tag.getTagNameCollectionFrom(delim+"tag0"+delim+delim);
		assertEquals(true, actualC != null && actualC.size() == 1);
		for (String s : actualC) {
			assertEquals(s, "tag0");
		}

		actualC = Tag.getTagNameCollectionFrom(delim+" \ttag0  "+delim+delim);
		assertEquals(true, actualC != null && actualC.size() == 1);
		for (String s : actualC) {
			assertEquals(s, "tag0");
		}
		
		actualC = Tag.getTagNameCollectionFrom(delim+delim+delim+"tag0"+delim);
		assertEquals(true, actualC != null && actualC.size() == 1);
		for (String s : actualC) {
			assertEquals(s, "tag0");
		}
		
		actualC = Tag.getTagNameCollectionFrom("tag0"+delim+delim+"tag1");
		assertEquals(true, actualC != null && actualC.size() == 2);
		List<String> list = new ArrayList<String>(actualC);
		assertEquals(list.get(0), "tag0");
		assertEquals(list.get(1), "tag1");
	}
		
	@Test
	public void testMethod_getTagFromName() {
        assertEquals(false, tags.isEmpty());
        Random r = TestUtil.getRandom();
        
        // The method should return the existing Tag if it accepts the name of an existing one.
        int idx = r.nextInt(tags.size());
        assertEquals(true, 0 <= idx && idx < tags.size());
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
		assertEquals(false, tags.isEmpty());
		boolean result = false;
		Collection<Tag> emptyTagCollection = new ArrayList<Tag>();
		long id = tags.get(0).getId();
		
		result = Tag.containsId(null, id);
		assertEquals(false, result);
		assertEquals(true, emptyTagCollection.isEmpty());
		result = Tag.containsId(emptyTagCollection, id);
		assertEquals(false, result);
		
		for (Tag t : tags) {
			result = Tag.containsId(tags, t.getId());
			assertEquals(true, result);
		}
	}
	
	@Test
	public void testMethod_containsName() {
		assertEquals(false, Tag.containsName(null, null));
		assertEquals(false, Tag.containsName(null, ""));

		assertEquals(false, tags.isEmpty());
		Collection<Tag> emptyTagCollection = new ArrayList<Tag>();
		assertEquals(true, emptyTagCollection.isEmpty());
		assertEquals(false, Tag.containsName(emptyTagCollection, ""));
		assertEquals(false, Tag.containsName(emptyTagCollection, null));
		assertEquals(false, Tag.containsName(emptyTagCollection, tags.get(0).getTagName()));

		assertEquals(false, Tag.containsName(tags, null));
		assertEquals(false, Tag.containsName(tags, ""));
		
		for (Tag t : tags) {
			assertEquals(true, Tag.containsName(tags, t.getTagName()));
		}
	}
	
	@Test
	public void testMethod_getTopParentTags() {
	    assertEquals(false, tags.isEmpty());
	    
	    // Edge cases
	    Collection<Tag> topParents = null;
	    topParents = Tag.getTopParentTags(null);
	    assertNull(topParents);
	    topParents = Tag.getTopParentTags(new ArrayList<Tag>());
	    assertNull(topParents);
	    
	    topParents = Tag.getTopParentTags(tags);
	    assertEquals(false, topParents.isEmpty());
	    for (Tag t : tags) {
	        Collection<Tag> children = t.getChildren();
	        for (Tag c : children) {
	            assertEquals(false, topParents.contains(c));
	        }
	    }
	}
	
	@Test
    public void testTagNameValidation() {
	    {
            Tag t = new Tag("\"tag name with quots\"");
            assertEquals('\"', Tag.findInvalidCharFromName(t.getTagName()));
        }
	    {
	        Tag t = new Tag("tag name / with slashes");
	        assertEquals('/', Tag.findInvalidCharFromName(t.getTagName()));
	    }
	    {
	        Tag t = new Tag(",tag name with commas");
	        assertEquals(',', Tag.findInvalidCharFromName(t.getTagName()));
	    }
	    {
	        Tag t = new Tag("tag name \\ with backslashes");
	        assertEquals('\\', Tag.findInvalidCharFromName(t.getTagName()));
	    }
    }

}
