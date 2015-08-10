package com.civilizer.test.domain;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.civilizer.domain.SearchParams;
import com.civilizer.domain.TextDecorator;

public class DomainTextDecoratorTest {
    final String PRE = TextDecorator.PREFIX_FOR_HIGHLIGHT;
    final String POST = TextDecorator.POSTFIX_FOR_HIGHLIGHT;
	
	@Before
	public void setUp() throws Exception {
	}
	
	@Test
    public void testHighlightNoMatch() {
	    final String text = "Hello World!! ";
        final String searchKeywords = "Hi";
        final SearchParams sp = new SearchParams(searchKeywords);
        assertEquals(1, sp.getKeywords().size());
        assertEquals(1, sp.getKeywords().get(0).getWords().size());
        
        final String decoratedText = TextDecorator.highlight(text, sp);
        assertNotNull(decoratedText);
        
        assertEquals(text, decoratedText);
	}

	@Test
	public void testHighlightCaseSensitive() {
		final String text = 
				"Functions with no arguments can be called without the parentheses. "
				+ "For example, the length() function on String can be invoked as \"abc\".length rather than \"abc\".length(). "
				+ "If the function is a Scala function defined without parentheses, then the function must be called without parentheses. ";
		final String searchKeywords = "Function/c";
		final SearchParams sp = new SearchParams(searchKeywords);
		assertEquals(1, sp.getKeywords().size());
		assertEquals(1, sp.getKeywords().get(0).getWords().size());
		
		final String decoratedText = TextDecorator.highlight(text, sp);
		assertNotNull(decoratedText);
		assertEquals(true, decoratedText.contains(PRE+"Function"+POST));
		assertEquals(false, decoratedText.contains(PRE+"function"+POST));
		
		final String tmp = decoratedText.replace(PRE, "").replace(POST, "");
		assertEquals(text, tmp);
	}

	@Test
	public void testHighlightCaseInsensitive() {
		final String text = 
				"Functions with no arguments can be called without the parentheses. "
				+ "For example, the length() function on String can be invoked as \"abc\".length rather than \"abc\".length(). "
				+ "If the function is a Scala function defined without parentheses, then the function must be called without parentheses. ";
		final String searchKeywords = "length parentheses";
		final SearchParams sp = new SearchParams(searchKeywords);
		assertEquals(1, sp.getKeywords().size());
		assertEquals(2, sp.getKeywords().get(0).getWords().size());
		
		final String decoratedText = TextDecorator.highlight(text, sp);
		assertNotNull(decoratedText);
		assertEquals(true, decoratedText.contains(PRE+"length"+POST));
		assertEquals(true, decoratedText.contains(PRE+"parentheses"+POST));
		
		final String tmp = decoratedText.replace(PRE, "").replace(POST, "");
		assertEquals(text, tmp);
	}
	
	@Test
	public void testHighlightKeywordsWithRegexMetaCharacters() {
		final String text = "( [ { \\ ^ $ | ) ] } ? * + .";
		final String[] metas = { "(", "[", "{", "\\",  "^",  "$", "|", ")", "]", "}", "?", "*", "+", "." };
		for (String meta : metas) {
			final SearchParams sp = new SearchParams(meta);
			assertEquals(1, sp.getKeywords().size());
			assertEquals(1, sp.getKeywords().get(0).getWords().size());
			
			final String decoratedText = TextDecorator.highlight(text, sp);
			assertNotNull(decoratedText);
			assertEquals(true, decoratedText.contains(PRE+meta+POST));
			
			final String tmp = decoratedText.replace(PRE, "").replace(POST, "");
			assertEquals(text, tmp);
		}
	}
	
	@Test
	public void testHighlightWithUrls() {
		final String text = "http://bsw.com/hello/world";
		final String searchKeywords = "bsw hello world";
		final SearchParams sp = new SearchParams(searchKeywords);
		assertEquals(1, sp.getKeywords().size());
		assertEquals(3, sp.getKeywords().get(0).getWords().size());
		
		final String decoratedText = TextDecorator.highlight(text, sp);
		assertNotNull(decoratedText);
		assertEquals(text, decoratedText);
	}
	
	@Test
    public void testHighlightKeywordsWithWordBoundaries() {
	    final String text = "The Javascript language has nothing to do with the Java language";
        {
            final SearchParams sp = new SearchParams("java/w");
            final String decoratedText = TextDecorator.highlight(text, sp);
            assertEquals(text.indexOf("Java language"), decoratedText.indexOf(PRE+"Java"+POST));
        }
        {
            final SearchParams sp = new SearchParams("java/b");
            final String decoratedText = TextDecorator.highlight(text, sp);
            assertEquals(4, decoratedText.indexOf(PRE+"Java"+POST));
            String tmp = decoratedText.substring(4+(PRE+"Java"+POST).length());
            assertEquals(true, tmp.contains(PRE+"Java"+POST));
        }
    }

}
