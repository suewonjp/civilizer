package com.civilizer.test.web

import spock.lang.*;

import com.civilizer.domain.SearchParams;
import com.civilizer.test.helper.TestUtil;
import com.civilizer.web.view.SearchContextBean;

@Subject(SearchContextBean)
class SearchContextBeanSpec extends spock.lang.Specification {
    
    def "Initial status"() {
        given:
            SearchContextBean scb = new SearchContextBean();
        expect: "The following are expected default conditions of SearchContextBean"
            scb.getQuickSearchPhrase().isEmpty()
            scb.getTagKeywords().isEmpty()
            scb.getTitleKeywords().isEmpty()
            scb.getContentKeywords().isEmpty()
            scb.getIdKeywords().isEmpty()
            ! scb.isAnyTag()
            ! scb.isAnyTitle()
            ! scb.isAnyContent()
            scb.getPanelId() < 0
            
        when: "Compile search parameters from no input"
            SearchParams sp = scb.buildSearchParams();
        then: "No SearchParams.Keywords object expected"
            sp && sp.getKeywords().isEmpty()
    }
    
    def "Quick search phrase"() {
        given: "An arbitrary search phrase"
            SearchContextBean scb = new SearchContextBean();
            scb.setTagKeywords("tag0"); // Note that this will be ignored
            scb.setQuickSearchPhrase("anytitle: title0 title1");
            
        when: "Compile the search parameters"
            SearchParams sp = scb.buildSearchParams();
        then: '''We have only one SearchParams.Keywords object here.
                 When a quick search phrase is specified,
                 every other search keyword will be ignored.
              '''
            1 == sp.getKeywords().size()
        and: "We only have 2 words [ 'title0', 'title1' ] inside the Keyword object"
            2 == sp.getKeywords().get(0).getWords().size()
    }
    
    def "Tag keywords"() {
        given: "An arbitrary search phrase only including tag keywords"
            SearchContextBean scb = new SearchContextBean();
            scb.setTagKeywords(" tag0  tag1 tag2  ");
        and: "Switch between 'tag:' and 'anytag:' by a random decision"
            final boolean isAny = TestUtil.getRandom().nextBoolean();
            scb.setAnyTag(isAny);
            
        when:
            SearchParams sp = scb.buildSearchParams();
        then: "Expect 1 SearchParams.Keywords object with 3 words"
            1 == sp.getKeywords().size()
            3 == sp.getKeywords().get(0).getWords().size()
            "tag2" == sp.getKeywords().get(0).getWords().get(2).getWord()
        and: '''The phrase has a target category of "tag:"
                or "anytag:" in case that [isAny] is true
             '''
            SearchParams.TARGET_TAG == sp.getKeywords().get(0).getTarget()
            isAny == sp.getKeywords().get(0).isAny()
    }
    
    def "Title keywords"() {
        given: "A search phrase"
            SearchContextBean scb = new SearchContextBean();
            scb.setTitleKeywords("title0, title1 ");
        and: "Switch between 'title:' and 'anytitle:' by a random decision"
            final boolean isAny = TestUtil.getRandom().nextBoolean();
            scb.setAnyTitle(isAny);
        
        when:
            SearchParams sp = scb.buildSearchParams();
        then: "Expect 1 SearchParams.Keywords object with 2 words"
            1 == sp.getKeywords().size()
            2 == sp.getKeywords().get(0).getWords().size()
            "title0," == sp.getKeywords().get(0).getWords().get(0).getWord()
        and:
            SearchParams.TARGET_TITLE == sp.getKeywords().get(0).getTarget()
            isAny == sp.getKeywords().get(0).isAny()
    }
    
    def "Content keywords"() {
        given: "A search phrase"
            SearchContextBean scb = new SearchContextBean();
            scb.setContentKeywords("text0 text1 \"text2\" text3 ");
        and: "Switch between 'text:' and 'anytext:' by a random decision"
            final boolean isAny = TestUtil.getRandom().nextBoolean();
            scb.setAnyContent(isAny);
        
        when:
            SearchParams sp = scb.buildSearchParams();
        then: "Expect 1 SearchParams.Keywords object with 4 words"
            1 == sp.getKeywords().size()
            4 == sp.getKeywords().get(0).getWords().size()
        and: 'The 2nd word in the phrase should be text2 not "text2"'
            "text2" == sp.getKeywords().get(0).getWords().get(2).getWord()
        and:
            SearchParams.TARGET_TEXT == sp.getKeywords().get(0).getTarget()
            isAny == sp.getKeywords().get(0).isAny()
    }
    
    def "Id keywords"() {
        given: "A search phrase"
            SearchContextBean scb = new SearchContextBean();
            scb.setIdKeywords(" 0xff   3 6 9");
            
        when:
            SearchParams sp = scb.buildSearchParams();
        then:
            SearchParams.TARGET_ID == sp.getKeywords().get(0).getTarget()
            1 == sp.getKeywords().size()
        and: "0xff will be dropped; Only decimal numbers accepted"
            3 == sp.getKeywords().get(0).getWords().size()
            "9" == sp.getKeywords().get(0).getWords().get(2).getWord()
        and: "'id:' target implies any of given IDs"
            sp.getKeywords().get(0).isAny()
    }
    
    def "Search of mixed target categories"() {
        given: "A search phrase with numerous target directives"
            SearchContextBean scb = new SearchContextBean();
            scb.setTagKeywords("tag0");
            scb.setTitleKeywords("title0 title1");
            scb.setAnyTitle(true);
            scb.setContentKeywords("text0 text1 text2");
            scb.setIdKeywords("1 2 3 4 5 6 7");
        
        when:
            SearchParams sp = scb.buildSearchParams();
        then: "Expect 4 SearchParams.Keywords objects"
            4 == sp.getKeywords().size()
        and: "Check each of them in detail"
            sp.getKeywords().each {
                // The type of [it] is SearchParams.Keywords
                if (it.getTarget() == SearchParams.TARGET_TAG) {
                    ! it.isAny()
                    1 == it.getWords().size()
                    "tag0" == it.getWords().get(0).getWord()
                }
                else if (it.getTarget() == SearchParams.TARGET_TITLE) {
                    it.isAny()
                    2 == it.getWords().size()
                    "title0" == it.getWords().get(0).getWord()
                }
                else if (it.getTarget() == SearchParams.TARGET_TEXT) {
                    ! it.isAny()
                    3 == it.getWords().size()
                    "text2" == it.getWords().get(2).getWord()
                }
                else if (it.getTarget() == SearchParams.TARGET_ID) {
                    it.isAny()
                    7 == it.getWords().size()
                    "7" == it.getWords().get(6).getWord()
                }
            }
    }

}
