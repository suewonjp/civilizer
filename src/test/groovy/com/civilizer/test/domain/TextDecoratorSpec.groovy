package com.civilizer.test.domain

import spock.lang.*;

import com.civilizer.domain.SearchParams;
import com.civilizer.domain.TextDecorator;

@Subject(TextDecorator)
class TextDecoratorSpec extends spock.lang.Specification {
    @Shared
    def PRE = TextDecorator.PREFIX_FOR_HIGHLIGHT;
    @Shared
    def POST = TextDecorator.POSTFIX_FOR_HIGHLIGHT;
    
    def "No match"() {
        given: "Target text and search keyword"
            def text = "Hello World!! ";
            def searchKeywords = "Hi";
            def sp = new SearchParams(searchKeywords);
        expect:
            1 == sp.getKeywords().size()
            1 == sp.getKeywords().get(0).getWords().size()
            
        when: "Decorate the search keyword"
            def decoratedText = TextDecorator.highlight(text, sp);
        then: "Nothing changes"
            text == decoratedText
    }
    
    def "Case sensitive matches"() {
        given: "Target text and search keyword"
    		def text = '''Functions with no arguments can be called without the parentheses. 
        		          For example, the length() function on String can be invoked as \"abc\".length rather than \"abc\".length().
        		          If the function is a Scala function defined without parentheses, then the function must be called without parentheses.
                       ''';
    		def searchKeywords = "Function/c";
    		def sp = new SearchParams(searchKeywords);
        when: "Decorate the search keywords with case sensitive match"
            def decoratedText = TextDecorator.highlight(text, sp);
        then: "Properly decorated with cases respected"        
            decoratedText.contains(PRE + "Function" + POST)
            ! decoratedText.contains(PRE + "function" + POST)
        and: "Same as the original rext if the prefix and postfix is removed"
            decoratedText.replace(PRE, "").replace(POST, "") == text
    }

    def "Case insensitive matches"() {
        given: "Target text and search keyword"
            def text = '''Functions with no arguments can be called without the parentheses. 
                          For example, the length() function on String can be invoked as \"abc\".length rather than \"abc\".length().
                          If the function is a Scala function defined without parentheses, then the function must be called without parentheses.
                       ''';
            def searchKeywords = "length parentheses";
            def sp = new SearchParams(searchKeywords);
        when: "Decorate the search keywords with case insensitive match"
            def decoratedText = TextDecorator.highlight(text, sp);
        then: "Properly decorated with cases ignored"        
            decoratedText.contains(PRE + "length" + POST)
            decoratedText.contains(PRE + "parentheses" + POST)
        and: "Same as the original rext if the prefix and postfix is removed"
            decoratedText.replace(PRE, "").replace(POST, "") == text
    }
    
    def "Matches with regex meta characters"() {
        given: "Target text and search keywords"
            def text = '( [ { \\ ^ $ | ) ] } ? * + .';
            def metas = [ "(", "[", "{", "\\",  "^",  "\$", "|", ")", "]", "}", "?", "*", "+", "." ];
            
        when: "Decorate the search keywords which are regex meta characters"
            metas.each {
                def sp = new SearchParams(it);
                def decoratedText = TextDecorator.highlight(text, sp);
                assert decoratedText.contains(PRE + it + POST) 
            }
        then: ""
    }
    
    def "Word boundary matches" () {
        given : "Target text"
            def text = "The Javascript language has nothing to do with the Java language";

        when: "Decorate the search keywords with the word boundary flag"
            def decoratedText = TextDecorator.highlight(text, new SearchParams("java/w"));
        then: "The number of matches is only one"
            text.indexOf("Java language") == decoratedText.indexOf(PRE+"Java"+POST)
        when: "Decorate the search keywords with the 'beginning with' flag"
            decoratedText = TextDecorator.highlight(text, new SearchParams("java/b"));
        then: "The number of matches are two in this case"
            4 == decoratedText.indexOf(PRE + "Java" + POST)
            decoratedText.substring(4+(PRE+"Java"+POST).length()).contains(PRE+"Java"+POST)
            
    }
    
}
