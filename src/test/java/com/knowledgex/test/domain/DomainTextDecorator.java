package com.knowledgex.test.domain;

import static org.junit.Assert.*;

import org.apache.commons.logging.Log;
import org.junit.Before;
import org.junit.Test;

import com.knowledgex.domain.SearchParams;
import com.knowledgex.domain.TextDecorator;
import com.knowledgex.test.util.TestUtil;

public class DomainTextDecorator {
	
	@SuppressWarnings("unused")
	private static Log logger = TestUtil.newLogger(DomainTextDecorator.class);

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testHighlight() {
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
		
		assertEquals("Functions with no arguments can be called without the <span class=\"search-keyword\">parentheses</span>. "
				+ "For example, the <span class=\"search-keyword\">length</span>() function on String can be invoked as \"abc\".<span class=\"search-keyword\">length</span> rather than \"abc\".<span class=\"search-keyword\">length</span>(). "
				+ "If the function is a Scala function defined without <span class=\"search-keyword\">parentheses</span>, then the function must be called without <span class=\"search-keyword\">parentheses</span>"
				, decoratedText);
	}

}
