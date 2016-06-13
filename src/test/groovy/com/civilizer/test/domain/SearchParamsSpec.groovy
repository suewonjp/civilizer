package com.civilizer.test.domain

import spock.lang.*;

import com.civilizer.domain.*
import com.civilizer.utils.Pair;
import com.civilizer.test.helper.TestUtil;

@Subject(SearchParams)
class SearchParamsSpec extends spock.lang.Specification {
    
    def "SearchParams.Keyword --- edge caces"() {
        when: "Generated from a blank word, quots, etc."
            def kw = new SearchParams.Keyword(inputWord, false);
        then:
            outputWord == kw.getWord()
            valid == kw.isValid()
            
        where:
            inputWord     || outputWord | valid
            ""            || ""         | false
            '""'          || ""         | false
            "''"          || "''"       | true
            "'"           || "'"        | true
            '"'           || '"'        | true
    }
    
    def "SearchParams.Keyword --- normal caces"() {
        when:
            def kw = new SearchParams.Keyword(inputWord, false);
        then:
            kw.isValid()
            outputWord == kw.getWord()
            caseSensitive == kw.isCaseSensitive()
            wholeWord == kw.isWholeWord()
            
        where:
            inputWord           || outputWord       | caseSensitive | wholeWord
            // [NOTE] If flags are inside double quotes,
            // they are identified as just normal characters
            '"Hello World/c"'   || "Hello World/c"  | false         | false
            
            '"Hello World"/c'   || "Hello World"    | true          | false
            'hello/c'           || "hello"          | true          | false
            'hello/w'           || "hello"          | false         | true
            'hello/wc'          || "hello"          | true          | true
            'hello/cw'          || "hello"          | true          | true
    }
    
    def "SearchParams.Keyword.escapeSqlWildcardCharacters"() {
        when: '''Escape SQL wild cards used in pattern matching for [where ... like ...] clause.
                 Mostly, what we care are "_" and "%"
              '''
            def res = SearchParams.Keyword.escapeSqlWildcardCharacters(inputWord);
        then:
            new Character(escapeChar as char) == res.getSecond()
            outputWord == res.getFirst()
            
        where:
            inputWord                || escapeChar | outputWord
            "_hello%suewon_bahng%"   || '!'        | "!_hello!%suewon!_bahng!%"
            "_hello!%suewon_bahng%"  || '#'        | "#_hello!#%suewon#_bahng#%"
    }
    
    def "SearchParams.Keywords"() {
        when: 
            def keywords = new SearchParams.Keywords(inputWord);
        then:
            size == keywords.getWords().size()
            isAny == keywords.isAny()
            target == keywords.getTarget()
            
        where:
            inputWord                           || size | isAny | target
            ""                                  || 0    | false | SearchParams.TARGET_DEFAULT
            " \t: \t\n \u3000 "                 || 0    | false | SearchParams.TARGET_DEFAULT
            ' :  ""\t'                          || 0    | false | SearchParams.TARGET_DEFAULT
            "text: ..."                         || 1    | false | SearchParams.TARGET_TEXT
            "anytext: . ! ?  ' \" "             || 5    | true  | SearchParams.TARGET_TEXT
            "tag: tag"                          || 1    | false | SearchParams.TARGET_TAG
            "anytag:tag0 tag2"                  || 2    | true  | SearchParams.TARGET_TAG
            "title: title"                      || 1    | false | SearchParams.TARGET_TITLE
            'anytitle:title "title:"  '         || 2    | true  | SearchParams.TARGET_TITLE
            "id:1 3 5 9 11 013"                 || 6    | true  | SearchParams.TARGET_ID
            ":word phrase '' \"\" wholeWorld/w" || 4    | false | SearchParams.TARGET_DEFAULT
            "any: 'hello _%' Phrase/c \"quoted phrase\" "  || 4  | true | SearchParams.TARGET_DEFAULT
    }
    
    def "SearchParams.Keywords --- special cases"() {
        def keywords;
        
        when: "Search phrase contains tag keywords with commas"
            keywords = new SearchParams.Keywords("tag: tag0, tag1, , , tag2, ,\u3000 , ,");
        then: '''As a special rule, tags with trailing commas can be accepted
                 and those commas should be trimmed
              '''
            SearchParams.TARGET_TAG == keywords.getTarget()
            3 == keywords.getWords().size()
            "tag0" == keywords.getWords().get(0).getWord()
            "tag1" == keywords.getWords().get(1).getWord()
            "tag2" == keywords.getWords().get(2).getWord()
            
        when: "Search phrase contains invalid id numerals"
            keywords = new SearchParams.Keywords("id: 0xfff invalid 32h 0");
        then: "Only the final input is acceptable here"
            1 == keywords.getWords().size()
            "0" == keywords.getWords().get(0).getWord()
    }
    
    def "TagCache"() {
        given:
            final List<Tag> tags = TagSpec.buildTags([
                "my tag", "your tag", "Tag000",      
                 "~tag~", "xxxYyy", "#bookmark",
                 "wwwZzz"]);
        
            final Tag rootTag = new Tag("rootTag");
            rootTag.setId((long)tags.size() + 1);
            tags.each {
                rootTag.addChild(it);
            }
            
            Fragment f = new Fragment("fragment", "Some content", null);
            f.setId(0L);
            tags.each {
                f.addTag(it);
            }
            
            f.removeTag(tags.get(tags.size()-1)); // remove the tag "wwwZzz"
            assert tags.size()-1 == f.getTags().size()
            
            tags.add(rootTag);

        def sp, tc;
        
        when: "Build a TagCache from a trivial tag keyword"
            sp = new SearchParams("tag:tag");
            tc = new SearchParams.TagCache(tags, sp);
        then:
            tc.matches(f)
        
        ({
        when: "Try to match a TRASHED Fragment"
            Fragment tmp = new Fragment("fragment", "Some content", null);
            Tag trash = new Tag("trashed tag");
            trash.setId((long)Tag.TRASH_TAG_ID);
            tmp.addTag(trash);
        then: "TagCache can't match a TRASHED Fragment"
            ! tc.matches(tmp)
        })()
            
        when: "Build a TagCache from tag keywords with flags"
            sp = new SearchParams("tag:tag/e Tag/b xxY/c");
            tc = new SearchParams.TagCache(tags, sp);
        then:
            tc.matches(f)
            
        when: "Same as above but with one of no associated tags"
            // Notice that the tag "wwwZzz" has been removed from the Fragment
            sp = new SearchParams("tag:tag/e Tag/b xxY/c zzz");
            tc = new SearchParams.TagCache(tags, sp);
        then: "Shouldn't match"
            ! tc.matches(f)
            
        when: "Build a TagCache from any tag but one with '#'"
            sp = new SearchParams("tag:tag #/-");
            tc = new SearchParams.TagCache(tags, sp);
        then: "Shoudn't match because the fragment has a tag named '#bookmark'"
            ! tc.matches(f)
            
        when: "Build a TagCache from a quated tag name"
            sp = new SearchParams("tag:\"my tag\" zzz/-");
            tc = new SearchParams.TagCache(tags, sp);
        then:
            tc.matches(f)

        when: "Build a TagCache with the 'any' specifier"
            sp = new SearchParams("anytag:\"my tag\"/be zzz");
            tc = new SearchParams.TagCache(tags, sp);
        then:
            tc.matches(f)
            
        when: "Build a TagCache with the 'any' specifier"
            sp = new SearchParams("anytag: zzz/- zzz www");
            tc = new SearchParams.TagCache(tags, sp);
        then:
            tc.matches(f)
            
        when: "Test the '/h' (retrieving all descendant tags) flag"
            final Tag p = rootTag;
            final List<Tag> descendants = new ArrayList<>();
            tags.each {
                if (DomainTagTest.inSameHierarchy(p, it))
                    descendants.add(it);
            }
            sp = new SearchParams("tag:\"" + p.getTagName() + "\"/h");
            assert sp.getKeywords().get(0).getWords().get(0).tagHeirarchyRequired()
            tc = new SearchParams.TagCache(tags, sp);
            descendants.each {
                Fragment tmp = new Fragment("tmp", "Some content", null);
                tmp.addTag(it);
                assert tc.matches(tmp)
            }
        then: ""
    }
    
    def "SearchParams"() {
        when:
            def sp = new SearchParams(searchPhrase);
        then:
            size == sp.getKeywords().size()

        where:
            searchPhrase            || size
            ""                      || 0
            ":"                     || 0
            "anytitle:"             || 0
            "word phrase/w anytag: tag0" || 2
            "text:word phrase/w anytag:TAG any:" || 2
            // [NOTE] any directive inside double quotes should be ignored
            'anytitle:title "any:" text:"good content"' || 2
    }

}
