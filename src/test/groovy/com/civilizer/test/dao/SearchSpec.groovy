package com.civilizer.test.dao

import spock.lang.*;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

import com.civilizer.dao.hibernate.SearchQueryCreator;
import com.civilizer.domain.*;
import com.civilizer.utils.Pair;
import com.civilizer.test.helper.TestUtil;

@Subject(SearchQueryCreator)
class SearchSpec extends DaoSpecBase {
    
    def setupSpec() {
        DaoSpecBase.setupApplicationContext(
            "classpath:datasource-context-h2-empty.xml");
        runSqlScript("db_test/drop.sql", "db_test/schema.sql");
    }
    
    def cleanupSpec() {
        DaoSpecBase.cleanupApplicationContext();
    }
    
    @Override
    void doSetup() {
        super.doSetup();
        beginTransaction();
    }
    
    @Override
    void doCleanup() {
        endTransaction(true);
        super.doCleanup();
    }

    static def matches(def results, def src, Object...idx) {
//        assert src.getClass().isArray();
        idx.every {
            results.contains(src[it])
        }
    }
    
    def "Hibernate Criteria APIs; .like or .ilike"() {
        given: "Arbitrary tags"
            final Tag[] tags = [
                /*0*/newTag('tag'),
                /*1*/newTag('$tag'),
                /*2*/newTag('Tag'),
                /*3*/newTag('TAG'),
                /*4*/newTag('Tag-000'),
                /*5*/newTag('my tag'),
                /*6*/newTag('your tag :-)'),
                ];
        and: "Save them"
            tags.each { tagDao.save(it); }

        def results;
        when: "Search tags matching the given pattern"
            results = 
                session.createCriteria(Tag.class)
                .add(Restrictions.like("tagName", "%tag%"))
                .list();
        then:
            4 == results.size()
            matches(results, tags, 0, 1, 5, 6)
        when:
            results = 
                session.createCriteria(Tag.class)
                .add(Restrictions.like("tagName", "_tag"))
                .list();
        then:
            1 == results.size()
            matches(results, tags, 1)
        when:
            results = 
                session.createCriteria(Tag.class)
                .add(Restrictions.sqlRestriction("TAG_NAME like 'my tag'"))
                .list();
        then:
            1 == results.size()
            matches(results, tags, 5)
        when:
            results = 
                session.createCriteria(Tag.class)
                .add(Restrictions.ilike("tagName", "tag%"))
                .list();
        then:
            4 == results.size()
            matches(results, tags, 0, 2, 3, 4)
    }
    
    def "SearchQueryCreator.getPatternFromKeyword"() {
        def kw;
        Pair<String, Character> pattern;
        when: "Convert the input keyword with the method"
            kw = new SearchParams.Keyword('"My keyword"', false);
            assert kw.isValid() && ! kw.isWholeWord() 
            pattern = SearchQueryCreator.getPatternFromKeyword(kw);
        then: "Produces output that is more friendly to DB queries"
            "%My keyword%" == pattern.getFirst()
            0 == pattern.getSecond()
        when: "Convert the input keyword with the method"
            kw = new SearchParams.Keyword('hello/w', false);
            assert kw.isValid() && kw.isWholeWord() 
            pattern = SearchQueryCreator.getPatternFromKeyword(kw);
        then: "Produces output that is more friendly to DB queries"
            "hello" == pattern.getFirst()
            0 == pattern.getSecond()
        when: "Convert the input keyword with the method"
            kw = new SearchParams.Keyword('with_underscore', false);
            assert kw.isValid() 
            pattern = SearchQueryCreator.getPatternFromKeyword(kw);
        then: "Produces output that is more friendly to DB queries"
            "%with!_underscore%" == pattern.getFirst()
            '!' as Character == pattern.getSecond()
        when: "Convert the input keyword with the method"
            kw = new SearchParams.Keyword('with%percent', false);
            assert kw.isValid() 
            pattern = SearchQueryCreator.getPatternFromKeyword(kw);
        then: "Produces output that is more friendly to DB queries"
            "%with!%percent%" == pattern.getFirst()
            '!' as Character == pattern.getSecond()
    }
    
    Pair<Fragment[], Tag[]> createTestData() {
        final Tag[] tags = [
            newTag("my tag"),
            newTag("your tag :-)"),
            newTag("nobody's tag"),
            ];
        tags.each { tagDao.save(it); }
        
        final Fragment[] fragments = [
            /*0*/newFragment(".text()", "replaces the text inside a selection"),
            /*1*/newFragment(".html()", "works like .text() but lets you insert html instead of just text"),
            /*2*/newFragment(".append()", "lets you insert the specified content as the last child of an element"),
            /*3*/newFragment(".prepend()", "lets you insert the specified content as the first child of an element"),
            /*4*/newFragment(".before()", "adds content before the selection"),
            /*5*/newFragment(".after()", "works just like .before(), except that the content is added after the selection (after its closing tag)."),
            /*6*/newFragment(".replaceWith()", "completely replaces the selection (including the tag and everything inside it) with whatever you pass"),
            /*7*/newFragment(".remove()", "removes the selection from the DOM;"),
            /*8*/newFragment(".wrap()", "wraps each element in a selection in a pair of HTML tags."),
            /*9*/newFragment(".wrapInner()", "wraps the contents of each element in a selection in HTML tags."),
           /*10*/newFragment(".unwrap()", "simply removes the parent tag surrounding the selection."),
           /*11*/newFragment(".empty()", "removes all of the contents of a selection, but leaves the selection in place"),
           ];
       
       fragments.each {
            final int n = TestUtil.getRandom().nextInt(3);
            if (n == 0) {
                it.addTag(tags[0]);
            }
            else if (n == 1) {
                it.addTag(tags[1]);
            }
            else {
                it.addTag(tags[0]);
                it.addTag(tags[1]);
            }
            fragmentDao.save(it);
       }
        
       new Pair<Fragment[], Tag[]>(fragments, tags);
    }
    
    def "Basic search"() {
        given: "Test data"
            final Pair<Fragment[], Tag[]> pair = createTestData();
            final Fragment[] fragments = pair.getFirst();
            
        def sp, results;
        when: "Search with the given search phrase"
            sp = new SearchParams("title:.wrap() ");
            results = fragmentDao.findBySearchParams(sp, null);
        then:
            1 == results.size()
            matches(results, fragments, 8)
        when:
            sp = new SearchParams("title:. () ");
            results = fragmentDao.findBySearchParams(sp, null);
        then:
            fragments.length == results.size()
        when:
            sp = new SearchParams("anytitle:pend wrap ");
            results = fragmentDao.findBySearchParams(sp, null);
        then:
            5 == results.size()
            matches(results, fragments, 2, 3, 8, 9, 10)
        when:
            sp = new SearchParams("title:before()  text:before");
            results = fragmentDao.findBySearchParams(sp, null);
        then:
            1 == results.size()
            matches(results, fragments, 4)
        when:
            sp = new SearchParams("text");
            results = fragmentDao.findBySearchParams(sp, null);
        then:
            2 == results.size()
            matches(results, fragments, 0, 1)
        when:
            sp = new SearchParams(":replace");
            results = fragmentDao.findBySearchParams(sp, null);
        then:
            2 == results.size()
            matches(results, fragments, 0, 6)
        when:
            sp = new SearchParams(": text html");
            results = fragmentDao.findBySearchParams(sp, null);
        then:
            1 == results.size()
            matches(results, fragments, 1)
        when:
            sp = new SearchParams("any: text html");
            results = fragmentDao.findBySearchParams(sp, null);
        then:
            4 == results.size()
            matches(results, fragments, 0, 1, 8, 9)
        when:
            sp = new SearchParams("text:HTML/c");
            results = fragmentDao.findBySearchParams(sp, null);
        then:
            2 == results.size()
            matches(results, fragments, 8, 9)
        when:
            sp = new SearchParams(': "lets you insert"');
            results = fragmentDao.findBySearchParams(sp, null);
        then:
            3 == results.size()
            matches(results, fragments, 1, 2, 3)
    }
    
    def "Word boundary search"() {
        given: "Test data"
            final Pair<Fragment[], Tag[]> pair = createTestData();
            final Fragment[] fragments = pair.getFirst();
            
        def sp, results;
        when:
            sp = new SearchParams(':replace/w');
            results = fragmentDao.findBySearchParams(sp, null);
        then:
            0 == results.size()
        when:
            sp = new SearchParams(':wrap/w ');
            results = fragmentDao.findBySearchParams(sp, null);
        then:
            1 == results.size()
            matches(results, fragments, 8)
        when:
            sp = new SearchParams(':wrap/b ');
            results = fragmentDao.findBySearchParams(sp, null);
        then:
            2 == results.size()
            matches(results, fragments, 8, 9)
        when:
            sp = new SearchParams(':wrap/e ');
            results = fragmentDao.findBySearchParams(sp, null);
        then:
            2 == results.size()
            matches(results, fragments, 8, 10)
        when:
            sp = new SearchParams('tag/b');
            results = fragmentDao.findBySearchParams(sp, null);
        then:
            5 == results.size()
            matches(results, fragments, 5, 6, 8, 9, 10)
    }
    
    def "Regex search"() {
        given: "Test data"
            final Pair<Fragment[], Tag[]> pair = createTestData();
            final Fragment[] fragments = pair.getFirst();
            
        def sp, results;
        when:
            sp = new SearchParams('title:\\.\\w+()/r ');
            results = fragmentDao.findBySearchParams(sp, null);
        then:
            fragments.length == results.size()
        when:
            sp = new SearchParams('text:\\(.*\\)/r ');
            results = fragmentDao.findBySearchParams(sp, null);
        then:
            3 == results.size()
            matches(results, fragments, 1, 5, 6)
        when:
            sp = new SearchParams('title:[a-z]+[A-Z]+/r  ');
            results = fragmentDao.findBySearchParams(sp, null);
        then:
            2 == results.size()
            matches(results, fragments, 6, 9)
    }
    
    def "Inverse search"() {
        given: "Test data"
            final Pair<Fragment[], Tag[]> pair = createTestData();
            final Fragment[] fragments = pair.getFirst();
            
        def sp, results;
        when:
            sp = new SearchParams('text:selection/w- ');
            results = fragmentDao.findBySearchParams(sp, null);
        then:
            3 == results.size()
            matches(results, fragments, 1, 2, 3)
        when:
            sp = new SearchParams('title:wrap un/-');
            results = fragmentDao.findBySearchParams(sp, null);
        then:
            2 == results.size()
            matches(results, fragments, 8, 9)
        when:
            sp = new SearchParams('text:content contents/w-');
            results = fragmentDao.findBySearchParams(sp, null);
        then:
            4 == results.size()
            matches(results, fragments, 2, 3, 4, 5)
    }
    
    def "Search with tag restriction"() {
        given: "Test data"
            final Pair<Fragment[], Tag[]> pair = createTestData();
            final Fragment[] fragments = pair.getFirst();
            final def tags = Arrays.asList(pair.getSecond());
            
        def sp, results, tag;
        when:
            sp = new SearchParams('anytag:"my tag" "your tag"');
            results = fragmentDao.findBySearchParams(sp, tags);
        then:
            fragments.length == results.size()
        when:
            sp = new SearchParams('anytag:"nonexistent tag" "another nonexistent tag"');
            results = fragmentDao.findBySearchParams(sp, tags);
        then:
            0 == results.size()
        when:
            tag = tags[2]; // nobody's tag
            sp = new SearchParams("tag:\"${tag.getTagName()}\"/w");
            results = fragmentDao.findBySearchParams(sp, tags);
        then:
            0 == results.size()
        when:
            tag = tags.get(TestUtil.getRandom().nextInt(tags.size()));
            sp = new SearchParams("tag:\"${tag.getTagName()}\"/w");
            results = fragmentDao.findBySearchParams(sp, tags);
        then:
            results.every {
                it.containsTagName(tag.getTagName())
            }
        when:
            int count = 0;
            fragments.each {
                if (it.getTags().size() >= 2)
                    ++count;
            }
            sp = new SearchParams('tag:"my tag" "your tag"');
            results = fragmentDao.findBySearchParams(sp, tags);
        then:
            count == results.size()
        when:
            // Comma-separated tag list should be equivalent to space-separated tag list
            sp = new SearchParams('tag:"my tag", "your tag"');
            results = fragmentDao.findBySearchParams(sp, tags);
        then:
            count == results.size()
    }
    
    def "Search with fragment ids"() {
        given: "Test data"
            final Pair<Fragment[], Tag[]> pair = createTestData();
            final Fragment[] fragments = pair.getFirst();
            
        ({
        when: "Case of a single id"
            final int index = TestUtil.getRandom().nextInt(fragments.length);
            final long id = fragments[index].getId();
//          final String searchPhrase = "id:${id}";
            // [NOTE] wrapping with double quotes will work
            final String searchPhrase = 'id: "${id}"'; 
            // [NOTE] wrapping with single quotes will NOT work
//          final String searchPhrase = "id: '${id}'"; 
            final SearchParams sp = new SearchParams(searchPhrase);
            final def results = fragmentDao.findBySearchParams(sp, null);
        then:
            1 == results.size()
            id == results[0].getId()
            fragments[index] == results[0]
        })()
        
        ({
        when: "Case of multiple ids"
            final int[] indices = TestUtil.randomIndices(TestUtil.getRandom(), 2, fragments.length);
            String searchPhrase = "id:";
            indices.each {
                searchPhrase += fragments[it].getId() + " ";
            }
            final SearchParams sp = new SearchParams(searchPhrase);
            assert indices.length == sp.getKeywords().get(0).getWords().size();
            final def results = fragmentDao.findBySearchParams(sp, null);
        then:
            final def list = Arrays.asList(fragments);
            indices.every {
                Fragment.containsId(list, fragments[it].getId());
            }
        })()
    }

}
