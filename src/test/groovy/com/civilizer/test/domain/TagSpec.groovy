package com.civilizer.test.domain;

import spock.lang.*;

import com.civilizer.domain.*;
import com.civilizer.test.helper.TestUtil;

@Subject(Tag)
class TagSpec extends spock.lang.Specification {
    
    @Shared
    List<Tag> tags;
    
    @Shared
    def delim = Tag.TAG_NAME_DELIMITER;
    
    def setup() {
        tags = buildTags(16 + TestUtil.getRandom().nextInt(10));
    }
    
    def buildTagNameList(Collection<Tag> tags) {
        List<String> tagNames = new ArrayList<String>();
        for (Tag t : tags) {
            tagNames.add(t.getTagName());
        }
        assert tagNames.size() == tags.size();
        tagNames;
    }
    
    def buildTagHierarchy(Collection<Tag> tags) {
        assert tags && ! tags.isEmpty()
        
        Random r = TestUtil.getRandom();
        final int depthCount = 2 + r.nextInt(3);

        Object[] tagsPerDepth = new Object[depthCount];
        for (int i=0; i<depthCount; ++i) {
            tagsPerDepth[i] = new ArrayList<Tag>();
        }
        
        for (Tag t : tags) {
            int d = r.nextInt(depthCount);
            assert 0 <= d && d < depthCount;
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
                    assert removed;
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
                    assert childTags.contains(c);                    
                }
            }
        }
    }
    
    def buildTags(int count) {
        List<Tag> tags = new ArrayList<Tag>();
        for (int i = 0; i < count; i++) {
            Tag t = new Tag("tag " + tags.size());
            assert t
            t.setId(new Long(i));
            tags.add(t);
        }        
        buildTagHierarchy(tags);
        tags;
    }

    def buildTags(String...names) {
        List<Tag> tags = new ArrayList<Tag>();
        for (int i = 0; i < names.length; i++) {
            Tag t = new Tag(names[i]);
            assert t
            t.setId(new Long(i + 1));
            tags.add(t);
        }        
        buildTagHierarchy(tags);
        tags;
    }
    
    boolean inSameHierarchy(Tag possibleParent, Tag possibleDescendant) {
        if (possibleParent.equals(possibleDescendant) || possibleParent.getChildren().contains(possibleDescendant))
            return true;
        for (Tag c : possibleParent.getChildren()) {
            if (inSameHierarchy(c, possibleDescendant))
                return true;
        }
        return false;
    }
    
    def "IDs are valid"() {
        expect:
            ! tags.isEmpty()
        tags.each {
            assert it.getId() != null
        }
    }
    
    def "Equality"() {
        expect:
            ! tags.isEmpty()

        tags.each {
            def x = it;          
            //  REFLEXIVE:
            //  For any non-null reference value x,
            //  x.equals(x) should return true.
            assert x.equals(x)
            
            Tag y = new Tag();
            y.setId(x.getId());
            Tag z = new Tag();
            z.setId(x.getId());

            // SYMMETRIC:
            // For any non-null reference values x and y, 
            // x.equals(y) should return true
            // if and only if y.equals(x) returns true.
            assert x.equals(y)
            assert y.equals(x)
            
            // TRANSITIVE: 
            // For any non-null reference values x, y, and z,
            // if x.equals(y) returns true and y.equals(z) returns true,
            // then x.equals(z) should return true.
            assert y.equals(z)
            assert x.equals(z)
            
            // CONSISTENT:
            // For any non-null reference values x and y,
            // multiple invocations of x.equals(y) consistently return true or consistently return false,
            // provided no information used in equals comparisons on the objects is modified.
            assert x.equals(y)
            assert x.equals(y)
            assert x.equals(y)

            // For any non-null reference value x, x.equals(null) should return false.
            assert ! x.equals(null)
        }
    }
    
    def "Tag.getTagNamesFrom"() {
        given: "Tag names built from the method in question"
            def actual = Tag.getTagNamesFrom(tags);
        and: "Names built on the fly"
            def expected = new String()
            for (name in buildTagNameList(tags)) {
                assert 0 == Tag.findInvalidCharFromName(name)
                expected += name + ",";
            }
        expect:
            expected == actual
    }
    
    def "Tag.getTagNameCollectionFrom"() {
        given: "Tag names built from the method in question"
            Collection<String> actual = Tag.getTagNameCollectionFrom(tags);
        and: "Names built on the fly"
            Collection<String> expected = buildTagNameList(tags);
        expect:
            expected == actual
        when: "Build tag names using another version of the method"
            def mergedName = Tag.getTagNamesFrom(tags);
            actual = Tag.getTagNameCollectionFrom(mergedName);
        then:
            expected == actual
    }
    
    def "Tag.getTagNameCollectionFrom --- edge cases"() {
        when:
            Collection<String> actual = Tag.getTagNameCollectionFrom(input as String)
        then:
            output == actual
            
        where:
            input                           || output
            null                            || []
            ""                              || []
            "   "                           || []
            delim                           || []
            delim+delim+delim               || []
            delim+" \t\n "+delim            || []
            "\t\n tag0  \n "                || [ "tag0" ]
            delim+"tag0"+delim+delim        || [ "tag0" ]
            delim+delim+delim+"tag0"+delim  || [ "tag0" ]
            delim+"  \ttag0 "+delim+delim   || [ "tag0" ]
            "tag0"+delim+delim+"tag1"+delim || [ "tag0", "tag1" ]
    }
    
    def "Tag.getTagFromName"() {
        when: "The method accepts the name of an existing tag"
            int idx = TestUtil.getRandom().nextInt(tags.size());
            assert 0 <= idx && idx < tags.size()
            Tag expected = tags.get(idx);
            String name = tags.get(idx).getTagName();
            Tag actual = Tag.getTagFromName(name, tags);
        then: "It should return that tag"
            expected == actual
        when: "The method accepts a non-existing name"
            String nonExistingName = '#$%#%#%#%$#%$#%$#%$#%#$%***&%!%';
            actual = Tag.getTagFromName(nonExistingName, tags);
        then: "It should return NULL"
            actual == null
    }
    
    def "Tag.containsId"() {
        def result = false;
        long id = tags.get(0).getId();
        
        when: "Called for NULL (an edge case)"
            result = Tag.containsId(null, id);
        then: "Returns false"
            ! result
        when: "Called for an empty collection (an edge case)"
            result = Tag.containsId([], id);
        then: "Returns false"
            ! result
            
        when: "Called for a populated collection with a valid id (normal cases)"
            tags.each {
                assert Tag.containsId(tags, it.getId())
            }
        then: "Works"
    }
    
    def "Tag.containsName"() {
        when: "Called for a populated collection with a valid name (normal cases)"
            tags.each {
                assert Tag.containsName(tags, it.getTagName())
            }
        then: "Works"
        
        when: "Called for invalid input (null, an empty collection, an empty string, etc.)"
            def result = Tag.containsName(collection, name);
        then: "Returns false"
            false == result
        where:
            collection << [ null, null, [], [],   [],     tags, tags ]
            name       << [ null, "",   "", null, "tag0", null, ""   ]
    }
    
    def "Tag.getTopParentTags"() {
        when: "Called for NULL"
            Collection<Tag> topParents = Tag.getTopParentTags(null);
        then: "Returns NULL"
            ! topParents            
        when: "Called for an empty collection"
            topParents = Tag.getTopParentTags([]);
        then: "Returns NULL"
            ! topParents
            
        when: "Called for a normal collection"
            topParents = Tag.getTopParentTags(tags);
        then: "Returns parent tags of the root level"
            ! topParents.isEmpty()
        and: "That means any of the returned tags should not be a descendent"
            tags.each {
                it.getChildren().each {
                    assert ! topParents.contains(it)
                }
            }
    }
    
    def "Tag.getIdsOfDescendants"() {
        when: "Call the method for each tag and confirm the output respects the tag hierarchy"
            tags.each { tag ->
                boolean includeSelf = TestUtil.getRandom().nextBoolean();
                Set<Long> idSet = tag.getIdsOfDescendants(includeSelf);
                assert idSet != null
                if (includeSelf) {
                    assert idSet.contains(tag.getId())
                }
                tag.getChildren().each { child ->
                    assert idSet.contains(child.getId())
                }
                idSet.each { id ->
                    def idx = Tag.getIndexOf(id, tags);
                    assert -1 < idx && idx < tags.size()
                    Tag desc = tags[idx];
                    assert inSameHierarchy(tag, desc);
                }
            }
        then: ""
    }
    
    def "Tag.trimName"() {
        String expected = "expected tag name";
        
        expect: "The method trims any of preceding (or trailing) white spaces and double quots"
            expected == Tag.trimName(expected)
            expected == Tag.trimName(" \t  " + expected + "\t\t ")
            expected == Tag.trimName('"' + expected)
            expected == Tag.trimName("\t \"\t " + expected)
            expected == Tag.trimName(expected + "\"")
            expected == Tag.trimName(expected + " \t\" ")
            expected == Tag.trimName("\"" + expected + "\"")
            expected == Tag.trimName("  \"" + expected + "\"  \t")
            expected == Tag.trimName("\t \" \t " + expected + "  \"\t ")
            Tag.trimName(null).isEmpty()
            Tag.trimName("").isEmpty()
    }
    
    def "Tag name validation"() {
        def tagName;
        
        when: "A tag name with double quotes"
            tagName = new Tag("\"tag name with quots\"").getTagName();
        then: "Detects the quots" 
            '"' == Tag.findInvalidCharFromName(tagName)
        when: "Strip the quots"
            tagName = new Tag(Tag.stripDoubleQuotes(tagName)).getTagName();
        then: "No quots"
            0 == Tag.findInvalidCharFromName(tagName)
            
        when: "A tag name with a comma"
            tagName = new Tag(",tag name with commas").getTagName();
        then: "Detects the comma"
            ',' == Tag.findInvalidCharFromName(tagName)
            
        when: "A tag name with a backslashes"
            tagName = new Tag("tag name \\ with backslashes").getTagName();
        then: "Detects the backslashes"
            '\\' == Tag.findInvalidCharFromName(tagName)
            
        when: "A tag name with a colon"
            tagName = new Tag("tag name : with colons").getTagName();
        then: "Detects the colon"
            ':' == Tag.findInvalidCharFromName(tagName)
    }

}

