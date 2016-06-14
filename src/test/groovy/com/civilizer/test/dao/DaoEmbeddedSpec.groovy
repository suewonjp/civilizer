package com.civilizer.test.dao

import spock.lang.*;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;

import com.civilizer.domain.*;
import com.civilizer.test.helper.TestUtil;

class DaoEmbeddedSpec extends DaoSpecBase {
    
    def setupSpec() {
        DaoSpecBase.setupApplicationContext(
            "classpath:datasource-context-h2-embedded.xml");
    }
    
    def cleanupSpec() {
        DaoSpecBase.cleanupApplicationContext();
    }
    
    def ".executeQueryForResult"() {
        expect:
            fragmentDao.executeQueryForResult("from Fragment") ==
                fragmentDao.findAll(true)
            tagDao.executeQueryForResult("from Tag") ==
                tagDao.findAll()
    }
    
    def "TagDao.findAllWithChildren"() {
        given: "Fetch all tags (each tag comes with its children) including special tags"
            def tags = tagDao.findAllWithChildren(true);
        and: 
            TestUtil.checkIfNoDuplicateExistsIn(tags);
        and: "Check data integrity"
            tags.each {
                def id = getAndValidateId(it);
                def tag = tagDao.findById(id);
                assert tag == it
                assert ! Hibernate.isInitialized(tag.getChildren())
                assert ! Hibernate.isInitialized(tag.getFragments())
            }
    }
    
    def ".countAll"() {
        expect:
            fragmentDao.findAll(true).size() == fragmentDao.countAll(true);
            fragmentDao.findAll(false).size() == fragmentDao.countAll(false);
            tagDao.findAll().size() == tagDao.countAll()
    }
    
    def "FragmentDao.countByTagId"() {
        given: "All tags"
            def tags = tagDao.findAll();
        and: "Count fragments per tag"
            tags.each {
                def tid = it.getId();
                assert fragmentDao.findByTagId(tid, true).size() ==
                    fragmentDao.countByTagId(tid, true)
                assert fragmentDao.findByTagId(tid, false).size() ==
                    fragmentDao.countByTagId(tid, false)
            }
    }
    
    def "FragmentDao.findAll and .findById"() {
        given: "All fragments"
            def fragments = fragmentDao.findAll(TestUtil.getRandom().nextBoolean());
        and: "Check each fragment for data integrity"
            fragments.each { f ->
                def id = getAndValidateId(f);
                def withTags = false, withRelatedOnes = false;
                def frg = fragmentDao.findById(id, withTags, withRelatedOnes);
                assert frg == f
                assert ! Hibernate.isInitialized(frg.getTags())
                assert ! Hibernate.isInitialized(frg.getRelatedOnes())
                
                withTags = true;
                frg = fragmentDao.findById(id, withTags, withRelatedOnes);
                assert Hibernate.isInitialized(frg.getTags())
                frg.getTags().each {
                    assert Hibernate.isInitialized(it)
                }
                
                withRelatedOnes = true;
                frg = fragmentDao.findById(id, withTags, withRelatedOnes);
                assert Hibernate.isInitialized(frg.getRelatedOnes())
                frg.getRelatedOnes().each {
                    assert Hibernate.isInitialized(it)
                }
            }
    }
    
    def "Tag to fragment relationship"() {
        given: "Tags and fragments"
            Collection<Tag> tags = tagDao.findAll();
            Collection<Fragment> fragments = fragmentDao.findAll(true);
            Collection<String> fragmentNames = Fragment.getFragmentTitleCollectionFrom(fragments);
        and: "Access associcated fragments from each tag"
            tags.each {
                def id = getAndValidateId(it);
                Tag tag = tagDao.findById(id, true, false);
                def frgs = tag.getFragments();
                assert Hibernate.isInitialized(frgs)
                frgs.each {
                    assert fragmentNames.contains(it.getTitle())
                }
            }
            tags.each {
                assert ! Hibernate.isInitialized(it.getFragments())
                tagDao.populate(it, true, false);
                assert Hibernate.isInitialized(it.getFragments())
                assert ! Hibernate.isInitialized(it.getChildren())
            }
    }
    
    def "Fragment to tag relationship"() {
        given: "Tags and fragments"
            Collection<Tag> tags = tagDao.findAll();
            Collection<String> tagNames = Tag.getTagNameCollectionFrom(tags);
            Collection<Fragment> fragments = fragmentDao.findAll(true);
        and: "Access associcated tags from each fragment"
            fragments.each {
                def id = getAndValidateId(it);
                Fragment frg = fragmentDao.findById(id, true, false);
                def relatedTags = frg.getTags();
                assert Hibernate.isInitialized(relatedTags)
                relatedTags.each {
                    assert tagNames.contains(it.getTagName())
                }
            }
    }
    
    def "Tag hierarchy"() {
        given: "Fetch all tags (each tag comes with its children) including special tags"
            def tags = tagDao.findAllWithChildren(true);
        and: "Access all descendants of each tag and check data integrity"
            tags.each { tag ->
                assert Hibernate.isInitialized(tag.getChildren())
                def id = getAndValidateId(tag);
                def descendantIds = new HashSet<Long>();
                tagDao.findIdsOfAllDescendants(id, null, descendantIds);
                descendantIds.each {
                    def descendant = tagDao.findById(it);
                    def ancestors = new HashSet<Tag>();
                    findAllAncestorsOfTag(descendant, ancestors);
                    assert ancestors.contains(tag);
                }
                def descendantIds2 = tag.getIdsOfDescendants(false);
                assert descendantIds == descendantIds2
            }
    }
    
    void "TagDao.saveWithHierarchy"() {
        given: "Tags randomly shuffled"
            def tags = tagDao.findAll();
            Collections.shuffle(tags, TestUtil.getRandom());
        and: "The target tag"
            def tag = tags.get(tags.size() - 1);
            tagDao.populate(tag, false, true);
        and: "Child tags of the target tag"
            def children = tag.listOfChildren();
        and: "Parent tags arbitrarily picked"
            def parents = tags.subList(0, Math.min(5, tags.size()-1));
            assert ! parents.contains(tag) : "The parent tags should not contain the target tag"
            
        if (Collections.disjoint(parents, children) == false) {
            // Parents and children should not have items in common.
            // So we expect an exception triggered here.
            try {
                tagDao.saveWithHierarchy(tag, parents, children);
                assert false : "Failed to catch an expected exception"
            }
            catch (IllegalArgumentException e) {}
            
            return; // Just resort to next try.
        }
        
        when: "Trivial cases"
            tagDao.saveWithHierarchy(tag, parents, children);
            parents.each { p ->
                p = tagDao.findById(p.getId(), false, true);
                assert p.getChildren().contains(tag)
            }
            children.each { c ->
                assert tag.getChildren().contains(c)
            }
        then:
            notThrown IllegalArgumentException
        
        when: "Intentional Exception - The target tag exists in the parents"
            parents.add(tag);
            tagDao.saveWithHierarchy(tag, parents, children);
        then:
            thrown IllegalArgumentException
            
        when: "Intentional Exception - The target tag exists in the children"
            parents.remove(tag);
            children.add(tag);
            tagDao.saveWithHierarchy(tag, parents, children);
        then:
            thrown IllegalArgumentException
    }
    
    def "Fragment to fragment relationship"() {
        given: "All fragments including trashed ones"
            def fragments = fragmentDao.findAll(true);
        and: "Titles of all fragments"
            def fragmentNames = Fragment.getFragmentTitleCollectionFrom(fragments);
        and: "Access related fragments of each fragment and check data integrity"
            fragments.each {
                def id = getAndValidateId(it);
                Fragment frg = fragmentDao.findById(id, false, true);
                def relatedOnes = frg.getRelatedOnes();
                assert Hibernate.isInitialized(relatedOnes)
                assert relatedOnes.every {
                    fragmentNames.contains(it.getTitle())
                }
            }
    }
    
    def "Persist a new tag"() {
        given: "A new tag"
            def tag = newTag(null);
        expect:
            tag.getId() == null
            
        when: "Save it"
            tagDao.save(tag);
        then:
            tag.getId() != null
    }
    
    def "Persist a new fragment"() {
        given: "A new fragment"
            def frg = newFragment();
        and: "Associdate a new tag to this fragment"
            def tag = newTag(null);
            frg.addTag(tag);
        expect:
            frg.getId() == null
            
        when: "Save it"
            fragmentDao.save(frg);
        then:
            frg.getId() != null
        and: "Check if the added tag has been persisted too"
            tag.getId() != null
            tagDao.findById(tag.getId())
            
        when: "Retrieve a fragment from the associated tag"
            def fs = fragmentDao.findByTagId(tag.getId(), true);
        then: "The retreved fragment is identical to our target"
            1 == fs.size()
            fs[0] == frg
    }
    
    def "Update tags"() {
        given: "Create new tags"
            3.times {
                tagDao.save(newTag(null));
            }
        and: "Construct parent-child relationships among them"
            for (int i = 1; i < temporalTags.size(); ++i) {
                Tag parent = temporalTags[i - 1];
                Tag child = temporalTags[i];
                parent.addChild(child);
            }
        and: "Update them"
            temporalTags.each {
                tagDao.save(it);
                it.setChildren(null);
            }
        and: "Check if the heirarchy has been persisted"
            for (int i = 1; i < temporalTags.size(); ++i) {
                Tag parent = tagDao.findById(temporalTags[i - 1].getId(), false, true);
                Tag child = temporalTags[i];
                def children = parent.getChildren();
                assert Hibernate.isInitialized(children)
                assert 1 == children.size()
                children.each { c ->
                    assert c == child
                    def parents = tagDao.findParentTags(c.getId());
                    assert 1 == parents.size()
                    assert parents.every { it == parent }
                }
            }
    }
    
    def "Relate fragments"() {
        given: "Create new fragments"
            6.times {
                fragmentDao.save(newFragment());
            }
        and: "Construct relationships among them"
        for (int i = 1; i < temporalFragments.size(); ++i) {
            Fragment from = temporalFragments.get(i - 1);
            Fragment to = temporalFragments.get(i);
            fragmentDao.relateFragments(from.getId(), to.getId());
        }
        and: "Confirm the relationships"
        for (int i = 1; i < temporalFragments.size(); ++i) {
            Fragment from = fragmentDao
                .findById(temporalFragments[i - 1].getId(), false, true);
            Fragment to = fragmentDao
                .findById(temporalFragments[i].getId(), false, true);
            assert from.isRelatedTo(to)
            assert to.isRelatedTo(from)
        }
        when: "Reconstruct existing relationships"
            for (int i = 1; i < temporalFragments.size(); ++i) {
                Fragment from = temporalFragments.get(i - 1);
                Fragment to = temporalFragments.get(i);
                fragmentDao.relateFragments(from.getId(), to.getId());
            }
        then: "That shouldn't throw any exception"
            notThrown HibernateException
    }
    
    def "FragmentDao.findByTagIds"() {
        given: "Tags"
            def tags = tagDao.findAll();
        and: "Create tags at least 4 of them"
            final int minTags = 4;
            if (tags.size() < minTags) {
                minTags.each {
                    tagDao.save(newTag(null));
                }
                tags = tagDao.findAll();
            }
        and: "List of tag ids"
            final def ids = tags.collect { it.getId() }
        and: "Shuffle them"
            Collections.shuffle(ids);
        and: "The inclusive filter"            
            final def idsIn = [];
        and: "The exclusive filter"            
            final def idsEx = ids[0..1];
            
        def fragments;
            
        when: "Check edge cases"
            fragments = fragmentDao.findByTagIds(null, null);
        then:
            fragments == null
        when:
            fragments = fragmentDao.findByTagIds(null, idsEx);
        then:
            fragments == null
        when:
            fragments = fragmentDao.findByTagIds([], idsEx);
        then:
            fragments == null
            
        when: "Populate the inclusive filter"
            idsIn << ids[-1];
            idsIn << ids[-2];
        then: "The two filters should not have common IDs!"
            idsIn.every {
                ! idsEx.contains(it)
            }

        when: "Test with the inclusive filter only"
            fragments = fragmentDao.findByTagIds(idsIn, null);
        then: "Works"
            fragments.every { f ->
                idsIn.any {
                    f.containsTagId(it)
                }
            }
            
        when: "Test with a duplicate inclusive filter"
            idsIn << idsIn[0];
        then: "Works too"
            fragments == fragmentDao.findByTagIds(idsIn, null)
            
        when: "Test with the exclusive filter"
            fragments = fragmentDao.findByTagIds(idsIn, idsEx);
        then: "Works"
            fragments.every { f ->
                idsEx.every {
                    ! f.containsTagId(it)
                }
            }
            
        when: "Test with a duplicate exclusive filter"
            idsEx << idsEx[0];
        then: "Works too"
            fragments == fragmentDao.findByTagIds(idsIn, idsEx)
    }
    
    def "Fragments pagination"() {
        given: "All fragments but trashed"
            final def allFragments = fragmentDao.findAll(false);
        and: "Range for each page"
            int first = 0, count = 0;
        and: "Sort order option"
            final def fo = FragmentOrder.CREATION_DATETIME;
        and:
            final boolean asc = false;
            final int allCount = allFragments.size();
            def someFragments = [];
            
        when: "An edge case; zero range"
            first = 0; count = 0;
            someFragments = fragmentDao.findSomeNonTrashed(first, count, fo, asc);
        then: "Returns an empty page"
            someFragments.isEmpty()
        when: "An edge case; the beginning index is negative"
            first = -1; count = 10;
            someFragments = fragmentDao.findSomeNonTrashed(first, count, fo, asc);
        then: "Returns a page with size of the specified count"
            count == someFragments.size()
        when: "An edge case; the count is negative"
            first = 1; count = -1;
            someFragments = fragmentDao.findSomeNonTrashed(first, count, fo, asc);
        then: "Returns an empty page"
            someFragments.isEmpty()
        when: "An edge case; all are negative"
            first = -1; count = -1;
            someFragments = fragmentDao.findSomeNonTrashed(first, count, fo, asc);
        then: "Returns an empty page"
            someFragments.isEmpty()
        when: "An edge case; the count exceeds the max. available count"
            first = 0; count = allCount + 100;
            someFragments = fragmentDao.findSomeNonTrashed(first, count, fo, asc);
        then: "Returns the largest page"
            allCount == someFragments.size()
        when: "An edge case; the beginning index exceeds the max. available count"
            first = allCount + 10; count = 1;
            someFragments = fragmentDao.findSomeNonTrashed(first, count, fo, asc);
        then: "Returns an empty page"
            someFragments.isEmpty()
            
        when: "Normal cases"
            count = Math.max(1, TestUtil.getRandom().nextInt(allCount));
            assert 1 <= count && count < allCount
            first = Math.max(0, TestUtil.getRandom().nextInt(count));
            assert 0 <= first && first < count
            someFragments = fragmentDao.findSomeNonTrashed(first, count, fo, asc);
        then: "Returns a page with size of the specified range"
            Math.min(count, allCount-first) == someFragments.size()
            someFragments.every {
                allFragments.contains(it)
            }
    }
    
    def "Fragments pagination with order"() {
        given: "All fragments but trashed"
            final def allFragments = fragmentDao.findAll(false);
        and: "Range for each page"
            int first = 0, count = 0;
        and: "All order options"
            final def orders = [
                FragmentOrder.UPDATE_DATETIME,
                FragmentOrder.CREATION_DATETIME,
                FragmentOrder.TITLE,
                FragmentOrder.ID
            ];
        and: "Options to indicate ascending or descending order"
            final def asc = [
                false,
                false,
                true,
                false
            ];
        and: "Comparators from each order option"
            final def comparators = [];
            orders.eachWithIndex { order, i ->
                comparators << FragmentOrder.getComparator(order, asc[i]);
            }
        and:
            final int allCount = allFragments.size();
            def someFragments = [];
            orders.eachWithIndex { order, i ->
                count = Math.max(1, TestUtil.getRandom().nextInt(allCount));
                assert 1 <= count && count < allCount
                first = Math.max(0, TestUtil.getRandom().nextInt(count));
                assert 0 <= first && first < count
                someFragments = fragmentDao.findSomeNonTrashed(first, count, order, asc[i]);
                Math.min(count, allCount-first) == someFragments.size()
                final def cmptr = (Comparator<Fragment>) comparators[i];
                for (int j=1; j<someFragments.size(); ++j) {
                    Fragment f0 = someFragments[j - 1];
                    Fragment f1 = someFragments[j];
                    assert cmptr.compare(f0, f1) <= 0
                }
            }
    }
    
    def "Raw SQL queries"() {
        given: "Create two new fragments"
            2.times {
                fragmentDao.save(newFragment());
            }
        and: "Their ids"
            final def ids = [ temporalFragments[0].getId(), temporalFragments[1].getId() ];
            
        when: "Execute a raw SQL query (relate the two fragments)"
            final def sqlQuery = "insert into fragment2fragment(from_id, to_id) values ("+ ids[0] + ", "+ ids[1] + ")";
            fragmentDao.executeQuery(sqlQuery, true);
        then: "The fragments are related"
            fragmentDao.findById(ids[0], false, true).isRelatedTo(
                fragmentDao.findById(ids[1], false, true)) 
    }
    
    def "Trivial FileEntity queries"() {
        given: "File Box path"
            final String filesHome = TestUtil.getFilesHomePath();
        and: "FileEntity objects from the DB"
            final def fileEntitiesFromDB = fileEntityDao.findAll();
            assert fileEntitiesFromDB.size() == fileEntityDao.countAll()
        and: "FileEntity objects from the File Box"
            final def fileEntities = FileEntity.getFilesUnder(filesHome);
            assert fileEntitiesFromDB.size() == fileEntities.size()
        and: "Check if the two objects are coherent"
            fileEntities.each { fe ->
                assert fileEntitiesFromDB.contains(fe)
                final def fe2 = fileEntityDao.findByName(fe.getFileName());
                assert fileEntitiesFromDB.contains(fe2)
            }
    }
    
    def "Persist FileEntity objects"() {
        given: "File Box path"
            final String filesHome = TestUtil.getFilesHomePath();
        and: "A new FileEntity"
            final def fe = new FileEntity("~~~~temp~new~file~~~~");
        and: "The file corresponding the FileEntity"
            final def f = fe.toFile(filesHome);
            assert f != null;
        and: "Ensure the file does not exist yet"
            if (f.exists())
                f.delete();
            assert !fe.persisted(filesHome)
            
        when: "Save the FileEntity"
            fileEntityDao.save(fe);
        then:
            fe.getId() != null
            fe == fileEntityDao.findById(fe.getId())
            
        when: "Create a file corresponding to the FileEntity"
            assert f.createNewFile()
        then:
            notThrown IOException
        and: "The file on the file system and the FileEntity are coherent"
            fe.persisted(filesHome)
            
        when: "Delete the FileEntity"
            fileEntityDao.delete(fe);
        then: "It's not managed by the DB, but still persisted on the file system"
            fe.persisted(filesHome)
            
        when: "Delete the file on the file system"
            f.delete();
        then:
            ! fe.persisted(filesHome);
    }
    
    def "FileEntityDao.findByNamePattern"() {
        setup: "Save a new FileEntity object"
            final def fullPath = "/abc/efg/hijk/lmn";
            final def fe = new FileEntity(fullPath);
            fileEntityDao.save(fe);
        when: "Search the new object by name pattern query"
            def pattern = fullPath + "%";
            def results = fileEntityDao.findByNamePattern(pattern);
        then:
            [ fe ] == results
        when: "Do it with another query"
            pattern = "/abc/efg/%";
            results = fileEntityDao.findByNamePattern(pattern);
        then:
            [ fe ] == results
        cleanup:
            fileEntityDao.delete(fe);
    }

}
