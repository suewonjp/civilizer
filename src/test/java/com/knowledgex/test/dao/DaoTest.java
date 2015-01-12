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

	protected static void setUpBeforeClass(String dataSourceContextPath,
			Class<?> clazz) throws Exception {
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
		Tag result = new Tag(name);
		assertNotNull(result);
		temporalTags.add(result);
		return result;
	}

	protected Fragment newFragment() {
		Fragment frg = new Fragment("new fragment " + temporalFragments.size(),
				"Some content...", null);
		assertNotNull(frg);
		temporalFragments.add(frg);
		return frg;
	}

	protected Long getAndValidateId(Fragment f) {
		Long id = f.getId();
		assertTrue(id != null && id >= 0);
		return id;
	}

	protected Long getAndValidateId(Tag f) {
		Long id = f.getId();
		assertTrue(id != null && id >= 0);
		return id;
	}

	protected void assertEquality(Fragment expected, Fragment actual) {
		assertEquals(expected, actual);
		assertEquals(expected.getTitle(), actual.getTitle());
	}

	protected void assertEquality(Tag expected, Tag actual) {
		assertEquals(expected, actual);
		assertEquals(expected.getTagName(), actual.getTagName());
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
			Long id = getAndValidateId(t);
			Tag tag = tagDao.findById(id);
			assertEquality(tag, t);
			assertFalse(Hibernate.isInitialized(tag.getChildren()));
			assertFalse(Hibernate.isInitialized(tag.getFragments()));
		}
	}

	protected void testFindAllFragments() {
		Collection<Fragment> fragments = fragmentDao.findAll();

		for (Fragment f : fragments) {
			Long id = getAndValidateId(f);
			Fragment frg = fragmentDao.findById(id);
			assertEquality(frg, f);
			assertTrue(Hibernate.isInitialized(frg.getTags()));
			assertFalse(Hibernate.isInitialized(frg.getRelatedOnes()));
		}
	}

	protected void testTagToFragmentRelationship() {
		Collection<Tag> tags = tagDao.findAll();
		Collection<Fragment> fragments = fragmentDao.findAll();
		Collection<String> fragmentNames = Fragment
				.getFragmentTitleCollectionFrom(fragments);

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
			Long id = getAndValidateId(f);

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
			Long id = getAndValidateId(t);
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
		Collection<String> fragmentNames = Fragment
				.getFragmentTitleCollectionFrom(fragments);

		for (Fragment f : fragments) {
			Long id = getAndValidateId(f);
			Fragment frgm = fragmentDao.findByIdWithRelatedOnes(id);
			Collection<Fragment> relatedOnes = frgm.getRelatedOnes();
			assertTrue(Hibernate.isInitialized(relatedOnes));
			if (relatedOnes.isEmpty() == false) {
				for (Fragment r : relatedOnes) {
					assertTrue(fragmentNames.contains(r.getTitle()));
					log.info("Fragment " + id + " and " + r.getId()
							+ " are related");
				}
			}
		}
	}

	protected void testPersistNewTag() {
		Tag tag = newTag(null);
		assertNull(tag.getId());
		tagDao.save(tag);
		assertNotNull(tag.getId());
	}

	protected void testPersistNewFragment() {
		Fragment frg = newFragment();

		// Add new tag to this fragment
		Tag tag = newTag("added tag " + counter++);
		frg.addTag(tag);

		assertNull(frg.getId());
		fragmentDao.save(frg);
		assertNotNull(frg.getId());

		// Check if the added tag has been persisted
		assertNotNull(tag.getId());
		assertNotNull(tagDao.findById(tag.getId()));
		log.info("new tag " + tag.getId() + " has been added to fragment "
				+ frg.getId());

		Collection<Fragment> fs = tagDao.findFragments(tag.getId());
		assertNotNull(fs);
		assertEquals(fs.size(), 1);
		for (Fragment f : fs) {
			assertEquality(f, frg);
			Collection<Tag> tags = f.getTags();
			assertEquals(tags.size(), 1);
			for (Tag t : tags) {
				assertEquality(tag, t);
			}
		}
	}

	protected void testUpdateTag() {
		for (int i = 0; i < 3; ++i) {
			testPersistNewTag();
		}

		for (int i = 1; i < temporalTags.size(); ++i) {
			Tag parent = temporalTags.get(i - 1);
			Tag child = temporalTags.get(i);
			parent.addChild(child);
		}
		for (Tag t : temporalTags) {
			tagDao.save(t);
			t.setChildren(null);
		}
		for (int i = 1; i < temporalTags.size(); ++i) {
			Tag parent = tagDao.findByIdWithChildren(temporalTags.get(i - 1)
					.getId());
			Tag child = temporalTags.get(i);
			Collection<Tag> children = parent.getChildren();
			assertTrue(Hibernate.isInitialized(children));
			assertEquals(children.size(), 1);
			for (Tag c : children) {
				assertEquality(c, child);
				Collection<Tag> parents = tagDao.findParentTags(c.getId());
				assertEquals(parents.size(), 1);
				for (Tag p : parents) {
					assertEquality(p, parent);
				}
			}
		}
	}

	protected void testUpdateFragment() {
		for (int i = 0; i < 6; ++i) {
			testPersistNewFragment();
		}
		for (int i = 1; i < temporalFragments.size(); ++i) {
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
		for (int i = 1; i < temporalFragments.size(); ++i) {
			Fragment from = fragmentDao
					.findByIdWithRelatedOnes(temporalFragments.get(i - 1)
							.getId());
			Fragment to = temporalFragments.get(i);
			Collection<Fragment> relatedOnes = from.getRelatedOnes();
			assertTrue(Hibernate.isInitialized(relatedOnes));
			assertEquals(relatedOnes.size(), 1);
			for (Fragment f : relatedOnes) {
				assertEquality(f, to);
			}
		}
	}

	protected void testFindFragmentsByTagIds() {
		Collection<Tag> tags = tagDao.findAll();
		
		// --- Collection<Fragment> findFragments(Long id);
		for (Tag t : tags) {
			Long id = getAndValidateId(t);
			Collection<Fragment> fs = tagDao.findFragments(id);
			for (Fragment f : fs) {
				Collection<Tag> tf = f.getTags();
				assertTrue(tf != null && tf.isEmpty() == false);
				Collection<String> tagNames = Tag.getTagNameCollectionFrom(tf);
				assertTrue(tagNames.contains(t.getTagName()));
			}
		}
				
		// --- Collection<Fragment> findFragments(Collection<Long>, Collection<Long>);
		final int minTags = 4;
		if (tags.size() < minTags) {
			for (int i = 0; i < minTags; ++i) {
				testPersistNewTag();
			}
			tags = tagDao.findAll();
		}
		
		List<Long> ids = new ArrayList<Long>(tags.size());
		for (Tag t : tags) {
			ids.add(t.getId());
		}
		Collections.shuffle(ids);
		
		Collection<Fragment> fragments = null;
		List<Long> idsIn = new ArrayList<Long>();
		List<Long> idsEx = new ArrayList<Long>();
		idsEx.add(ids.get(0));
		idsEx.add(ids.get(1));
		
		// test with edge cases
		fragments = tagDao.findFragments(null, null);
		assertNull(fragments);

		fragments = tagDao.findFragments(null, idsEx);
		assertNull(fragments);

		assertTrue(idsIn.isEmpty());
		fragments = tagDao.findFragments(idsIn, idsEx);
		assertNull(fragments);
		
		idsIn.add(ids.get(ids.size()-1));
		idsIn.add(ids.get(ids.size()-2));
		
		// The inclusion filter and exclusion filter should not have duplicate IDs!
		for (Long idIn : idsIn) {
			assertFalse(idsEx.contains(idIn));
		}
		
		// test with an inclusion filter only
		fragments = tagDao.findFragments(idsIn, null);
		for (Fragment f : fragments) {
			boolean contains = false;
			for (Long tid : idsIn) {
				if (f.containsTagId(tid)) {
					contains = true;
					break;
				}
			}
			assertTrue(contains);
		}
		
		// test with a duplicate inclusion filter
		idsIn.add(idsIn.get(0));
		Collection<Fragment> fragments2 = tagDao.findFragments(idsIn, null);
		assertEquals(fragments, fragments2);
		
//		// test with an exclusion filter
//		fragments = tagDao.findFragments(idsIn, idsEx);
//		for (Fragment f : fragments) {
//			boolean contains = false;
//			for (Long tid : idsEx) {
//				if (f.containsTagId(tid)) {
//					contains = true;
//					break;
//				}
//			}
//			assertFalse(contains);
//		}
//		
//		// test with a duplicate exclusion filter
//		idsEx.add(idsEx.get(0));
//		fragments2 = tagDao.findFragments(idsIn, idsEx);
//		assertEquals(fragments, fragments2);
	}

}
