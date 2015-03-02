package com.knowledgex.test.domain;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.knowledgex.domain.SearchParams;
import com.knowledgex.test.util.TestUtil;

public class DomainSearchParamsTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testKeywordFlags() {
    	{
    		final String word = "";
    		final SearchParams.Keyword kw = new SearchParams.Keyword(word);
    		assertEquals(kw.getWord(), "");
    		assertEquals(kw.checkValidity(), false);
    	}
    	{
    		final String word = "''";
    		final SearchParams.Keyword kw = new SearchParams.Keyword(word);
    		assertEquals(kw.getWord(), "");
    		assertEquals(kw.checkValidity(), false);
    	}
    	{
			final String word = "''''";
			final SearchParams.Keyword kw = new SearchParams.Keyword(word);
			assertEquals(kw.getWord(), "''");
			assertEquals(kw.checkValidity(), true);
		}
    	{
    		final String word = "'my \"keyword\"'";
    		final SearchParams.Keyword kw = new SearchParams.Keyword(word);
    		assertEquals(kw.getWord(), "my \"keyword\"");
    		assertEquals(kw.isCaseSensitive(), true);
    		assertEquals(kw.isWholeWord(), false);
    		assertEquals(kw.isAsIs(), true);
    		assertEquals(kw.checkValidity(), true);
    	}
    	{
    		final String word = "hello/c";
    		final SearchParams.Keyword kw = new SearchParams.Keyword(word);
    		assertEquals(kw.getWord(), "hello");
    		assertEquals(kw.isCaseSensitive(), true);
    		assertEquals(kw.isWholeWord(), false);
    		assertEquals(kw.isAsIs(), false);
    		assertEquals(kw.checkValidity(), true);
    	}
    	{
    		final String word = "hello/w";
    		final SearchParams.Keyword kw = new SearchParams.Keyword(word);
    		assertEquals(kw.getWord(), "hello");
    		assertEquals(kw.isCaseSensitive(), false);
    		assertEquals(kw.isWholeWord(), true);
    		assertEquals(kw.isAsIs(), false);
    		assertEquals(kw.checkValidity(), true);
    	}
    	{
    		final String word = TestUtil.getRandom().nextBoolean() ?
    				"hello/wc" : "hello/cw";
    		final SearchParams.Keyword kw = new SearchParams.Keyword(word);
    		assertEquals(kw.getWord(), "hello");
    		assertEquals(kw.isCaseSensitive(), true);
    		assertEquals(kw.isWholeWord(), true);
    		assertEquals(kw.isAsIs(), false);
    		assertEquals(kw.checkValidity(), true);
    	}
	}
}
