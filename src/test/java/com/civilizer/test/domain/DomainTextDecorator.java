package com.civilizer.test.domain;

import static org.junit.Assert.*;

import org.apache.commons.logging.Log;
import org.junit.Before;
import org.junit.Test;

import com.civilizer.domain.SearchParams;
import com.civilizer.domain.TextDecorator;
import com.civilizer.test.util.TestUtil;

public class DomainTextDecorator {
	
	@SuppressWarnings("unused")
	private static Log logger = TestUtil.newLogger(DomainTextDecorator.class);

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
		assertTrue(decoratedText.contains(TextDecorator.PREFIX_HTML_TAG_FOR_HIGHLIGHT+"Function"+TextDecorator.POSTFIX_HTML_TAG_FOR_HIGHLIGHT));
		assertTrue(! decoratedText.contains(TextDecorator.PREFIX_HTML_TAG_FOR_HIGHLIGHT+"function"+TextDecorator.POSTFIX_HTML_TAG_FOR_HIGHLIGHT));
		
		final String tmp = decoratedText
		        .replace(TextDecorator.PREFIX_HTML_TAG_FOR_HIGHLIGHT, "")
		        .replace(TextDecorator.POSTFIX_HTML_TAG_FOR_HIGHLIGHT, "");
		
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
		assertTrue(decoratedText.contains(TextDecorator.PREFIX_HTML_TAG_FOR_HIGHLIGHT+"length"+TextDecorator.POSTFIX_HTML_TAG_FOR_HIGHLIGHT));
		assertTrue(decoratedText.contains(TextDecorator.PREFIX_HTML_TAG_FOR_HIGHLIGHT+"parentheses"+TextDecorator.POSTFIX_HTML_TAG_FOR_HIGHLIGHT));
		
		final String tmp = decoratedText
		        .replace(TextDecorator.PREFIX_HTML_TAG_FOR_HIGHLIGHT, "")
		        .replace(TextDecorator.POSTFIX_HTML_TAG_FOR_HIGHLIGHT, "");
		
		assertEquals(text, tmp);
	}

}
