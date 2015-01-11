package com.knowledgex.test.dao;

import static org.junit.Assert.*;

import java.util.*;

import org.apache.commons.logging.Log;
import org.hibernate.Hibernate;
import org.springframework.context.support.GenericXmlApplicationContext;

import com.knowledgex.dao.*;
import com.knowledgex.domain.*;
import com.knowledgex.test.util.TestUtil;

abstract class DaoTest {
    
    private static Log log;
    private static GenericXmlApplicationContext ctx;
    private static int counter = 0;
    
    private FragmentDao fragmentDao;
    private TagDao tagDao;
    
    private List<Tag> temporalTags = new ArrayList<Tag>();
    private List<Fragment> temporalFragments = new ArrayList<Fragment>();
    
    protected static void setUpBeforeClass(
            String dataSourceContextPath
            , Class<?> clazz
            ) throws Exception {
        log = TestUtil.newLogger(clazz);
        
        ctx = new GenericXmlApplicationContext();
        ctx.load(dataSourceContextPath);
        ctx.refresh();
        log.info("GenericXmlApplicationContext initialized OK");
    }
    
    protected void deleteAllTemporalObjects() {
        for (Tag t : temporalTags) {
            assertNotNull(tagDao.findById(t.getId()));
            tagDao.delete(t);
        }

        for (Fragment frg : temporalFragments) {
            assertNotNull(fragmentDao.findById(frg.getId()));
            fragmentDao.delete(frg);
        }
    }
    
    protected Tag newTag(String name) {
        if (name == null)
            name = "new tag " + temporalTags.size();
        return new Tag(name);
    }
    
    protected void setUp() throws Exception {          
        fragmentDao = ctx.getBean("fragmentDao", FragmentDao.class);
        assertNotNull(fragmentDao);
        log.info("fragmentDao initialized OK");
        
        tagDao = ctx.getBean("tagDao", TagDao.class);
        assertNotNull(tagDao);
        log.info("tagDao initialized OK");
    }
    
    protected void tearDown() throws Exception {
        deleteAllTemporalObjects();
    } 
    
    protected void testFindAllTags() {
    	Collection<Tag> tags = tagDao.findAll();
        
        for (Tag t : tags) {
            log.info(t);
            Long id = t.getId();
            assertTrue(id >= 0);
            Tag tag = tagDao.findById(id);
            assertEquals(tag.getTagName(), t.getTagName());
            assertFalse(Hibernate.isInitialized(tag.getChildren()));
            assertFalse(Hibernate.isInitialized(tag.getFragments()));
        }
    }
    
    protected void testFindAllFragments() {
    	Collection<Fragment> fragments = fragmentDao.findAll();
        
        for (Fragment f : fragments) {
            log.info(f);
            Long id = f.getId();
            assertTrue(id >= 0);
            Fragment frg = fragmentDao.findById(id);
            assertEquals(frg.getTitle(), f.getTitle());
            assertTrue(Hibernate.isInitialized(frg.getTags()));
            assertFalse(Hibernate.isInitialized(frg.getRelatedOnes()));
        }
    }
    
    protected void testTagToFragmentRelationship() {
    	Collection<Tag> tags = tagDao.findAll();        
    	Collection<Fragment> fragments = fragmentDao.findAll();
    	Collection<String> fragmentNames = Fragment.getFragmentTitleCollectionFrom(fragments);
        
        for (Tag t : tags) {
            Long id = t.getId();
            Tag tag = tagDao.findByIdWithFragments(id);
            Collection<Fragment> frgs = tag.getFragments();
            assertTrue(Hibernate.isInitialized(frgs));
            if (frgs.isEmpty() == false) {
                for (Fragment f : frgs) {
                    assertTrue(fragmentNames.contains(f.getTitle()));
                    log.info("Tag " + id + " has a fragment " + f.getId());
                }
            }
        }
    }
    
    protected void testFragmentToTagRelationship() {
    	Collection<Tag> tags = tagDao.findAll();
        assertFalse(tags.isEmpty());
        Collection<String> tagNames = Tag.getTagNameCollectionFrom(tags);
        Collection<Fragment> fragments = fragmentDao.findAll();
        
        for (Fragment f : fragments) {
            Long id = f.getId();
            assertTrue(id >= 0);

            Fragment frgm = fragmentDao.findByIdWithTags(id);
            Collection<Tag> relatedTags = frgm.getTags();
            assertTrue(Hibernate.isInitialized(relatedTags));
            if (relatedTags.isEmpty() == false) {
                for (Tag t : relatedTags) {
                    assertTrue(tagNames.contains(t.getTagName()));
                    log.info("Fragment " + id + " belongs to tag " + t.getId());
                }
            }
        }
    }
    
    protected void testTagsHierarchy() {
    	Collection<Tag> tags = tagDao.findAll();        
        Collection<String> tagNames = Tag.getTagNameCollectionFrom(tags);
 
        for (Tag t : tags) {
            Long id = t.getId();
            Tag tag = tagDao.findByIdWithChildren(id);
            Collection<Tag> children = tag.getChildren();
            assertTrue(Hibernate.isInitialized(children));
            if (children.isEmpty() == false) {
                for (Tag c : children) {
                    assertTrue(tagNames.contains(c.getTagName()));
                    log.info("Tag " + id + " has a child " + c.getId());
                }
            }
        }
    }    
        
    protected void testRelatedFragments() {
    	Collection<Fragment> fragments = fragmentDao.findAll();
    	Collection<String> fragmentNames = Fragment.getFragmentTitleCollectionFrom(fragments);
        
        for (Fragment f : fragments) {
            Long id = f.getId();
            Fragment frgm = fragmentDao.findByIdWithRelatedOnes(id);
            Collection<Fragment> relatedOnes = frgm.getRelatedOnes();
            assertTrue(Hibernate.isInitialized(relatedOnes));
            if (relatedOnes.isEmpty() == false) {
                for (Fragment r : relatedOnes) {
                    assertTrue(fragmentNames.contains(r.getTitle()));
                    log.info("Fragment " + id + " and " + r.getId() + " are related");
                }
            }
        }
    }    
    
    protected void testPersistNewTag() {
        Tag tag = newTag(null);
        temporalTags.add(tag);
        tagDao.save(tag);
        log.info(tag);        
    }

    protected void testPersistNewFragment() {
        Fragment frg = new Fragment(
        		"new fragment " + temporalFragments.size()
        		, "Some content..."
        		, null
        		);
        assertNotNull(frg);
        temporalFragments.add(frg);
        
        // Add new tag to this fragment
        Tag tag = newTag("added tag " + counter++);
        frg.addTag(tag);
        temporalTags.add(tag);
        
        fragmentDao.save(frg);
        
        // Check if the added tag has been persisted
        assertNotNull(tag.getId());
        assertNotNull(tagDao.findById(tag.getId()));
        log.info("new tag " + tag.getId() + " has been added to fragment " + frg.getId());
        
        Collection<Fragment> fs = tagDao.findFragments(tag.getId());
        assertNotNull(fs);
        assertEquals(fs.size(), 1);
        for (Fragment f : fs) {
        	assertEquals(f.getId(), frg.getId());
        	Collection<Tag> tags = f.getTags();
        	assertEquals(tags.size(), 1);
        	for (Tag t : tags) {
        		assertEquals(t.getId(), tag.getId());
        	}
		}
         
        log.info(frg);
    }

    protected void testUpdateTag() {
        for (int i=0; i<3; ++i) {
            testPersistNewTag();
        }
        
        for (int i=1; i<temporalTags.size(); ++i) {
            Tag parent = temporalTags.get(i - 1);
            Tag child = temporalTags.get(i);
            parent.addChild(child);
        }
        for (Tag t : temporalTags) {
            tagDao.save(t);
            t.setChildren(null);
        }
        for (int i=1; i<temporalTags.size(); ++i) {
            Tag parent = tagDao.findByIdWithChildren(temporalTags.get(i - 1).getId());
            Tag child = temporalTags.get(i);
            Collection<Tag> children = parent.getChildren();
            assertTrue(Hibernate.isInitialized(children));
            assertEquals(children.size(), 1);
            for (Tag c : children) {
                assertEquals(c.getId(), child.getId());
                assertEquals(c.getTagName(), child.getTagName());
                Collection<Tag> parents = tagDao.findParentTags(c.getId());
                assertEquals(parents.size(), 1);
                for (Tag p: parents) {
                	assertEquals(p.getId(), parent.getId());
                }
            }
        }
    }
    
    protected void testUpdateFragment() {
        for (int i=0; i<6; ++i) {
            testPersistNewFragment();
        }
        for (int i=1; i<temporalFragments.size(); ++i) {
            Fragment from = temporalFragments.get(i - 1);
            Fragment to = temporalFragments.get(i);
            Collection<Fragment> s = new HashSet<Fragment>();
            s.add(to);
            from.setRelatedOnes(s);
        }
        for (Fragment frg : temporalFragments) {
            fragmentDao.save(frg);
            frg.setRelatedOnes(null);
        }
        for (int i=1; i<temporalFragments.size(); ++i) {
            Fragment from = fragmentDao.findByIdWithRelatedOnes(temporalFragments.get(i - 1).getId());
            Fragment to = temporalFragments.get(i);
            Collection<Fragment> relatedOnes = from.getRelatedOnes();
            assertTrue(Hibernate.isInitialized(relatedOnes));
            assertEquals(relatedOnes.size(), 1);
            for (Fragment f : relatedOnes) {
                assertEquals(f.getId(), to.getId());
                assertEquals(f.getTitle(), to.getTitle());
            }
        }
    }

}
