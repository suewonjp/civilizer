package com.knowledgex.test.domain;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.knowledgex.domain.Pair;
import com.knowledgex.domain.SearchParams;
import com.knowledgex.test.util.TestUtil;

public class DomainSearchParamsTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testSearchParamsKeyword() {
		// edge cases
    	{
    		final String word = "";
    		final SearchParams.Keyword kw = new SearchParams.Keyword(word);
    		assertEquals(kw.getWord(), "");
    		assertEquals(kw.isValid(), false);
    	}
    	{
    		final String word = "\"\"";
    		final SearchParams.Keyword kw = new SearchParams.Keyword(word);
    		assertEquals(kw.getWord(), "");
    		assertEquals(kw.isValid(), false);
    	}
    	{
    		final String word = "''";
    		final SearchParams.Keyword kw = new SearchParams.Keyword(word);
    		assertEquals(kw.getWord(), "''");
    		assertEquals(kw.isValid(), true);
    	}
    	{
    		final String word = "'";
    		final SearchParams.Keyword kw = new SearchParams.Keyword(word);
    		assertEquals(kw.getWord(), "'");
    		assertEquals(kw.isValid(), true);
    	}
    	{
    		final String word = "\"";
    		final SearchParams.Keyword kw = new SearchParams.Keyword(word);
    		assertEquals(kw.getWord(), "\"");
    		assertEquals(kw.isValid(), true);
    	}
    	{
    		// [NOTE] If flags are inside double quotes, they are identified as just normal characters
    		final String word = "\"Hello World/c\"";
    		final SearchParams.Keyword kw = new SearchParams.Keyword(word);
    		assertEquals(kw.getWord(), "Hello World/c");
    		assertEquals(kw.isCaseSensitive(), false);
    		assertEquals(kw.isWholeWord(), false);
    		assertEquals(kw.isAsIs(), true);
    		assertEquals(kw.isValid(), true);
    	}
    	{
    		final String word = "hello/c";
    		final SearchParams.Keyword kw = new SearchParams.Keyword(word);
    		assertEquals(kw.getWord(), "hello");
    		assertEquals(kw.isCaseSensitive(), true);
    		assertEquals(kw.isWholeWord(), false);
    		assertEquals(kw.isAsIs(), false);
    		assertEquals(kw.isValid(), true);
    	}
    	{
    		final String word = "\"Hello World\"/c";
    		final SearchParams.Keyword kw = new SearchParams.Keyword(word);
    		assertEquals(kw.getWord(), "Hello World");
    		assertEquals(kw.isCaseSensitive(), true);
    		assertEquals(kw.isWholeWord(), false);
    		assertEquals(kw.isAsIs(), true);
    		assertEquals(kw.isValid(), true);
    	}
    	{
    		final String word = "hello/w";
    		final SearchParams.Keyword kw = new SearchParams.Keyword(word);
    		assertEquals(kw.getWord(), "hello");
    		assertEquals(kw.isCaseSensitive(), false);
    		assertEquals(kw.isWholeWord(), true);
    		assertEquals(kw.isAsIs(), false);
    		assertEquals(kw.isValid(), true);
    	}
    	{
    		final String word = TestUtil.getRandom().nextBoolean() ?
    				"hello/wc" : "hello/cw";
    		final SearchParams.Keyword kw = new SearchParams.Keyword(word);
    		assertEquals(kw.getWord(), "hello");
    		assertEquals(kw.isCaseSensitive(), true);
    		assertEquals(kw.isWholeWord(), true);
    		assertEquals(kw.isAsIs(), false);
    		assertEquals(kw.isValid(), true);
    	}
	}
	
	@Test
	public void testEscapeSqlWildcards() {
		{
    		final String word = "_hello%suewon_bahng%";
    		final SearchParams.Keyword kw = new SearchParams.Keyword(word);
    		assertEquals(kw.isValid(), true);
    		final Pair<String, Character> res = SearchParams.Keyword.escapeSqlWildcardCharacters(word);
    		assertEquals(res.getSecond(), new Character('!'));
    		assertEquals(res.getFirst(), "!_hello!%suewon!_bahng!%");
    	}
		{
			final String word = "_hello!%suewon_bahng%";
			final SearchParams.Keyword kw = new SearchParams.Keyword(word);
			assertEquals(kw.isValid(), true);
			final Pair<String, Character> res = SearchParams.Keyword.escapeSqlWildcardCharacters(word);
			assertEquals(res.getSecond(), new Character('#'));
			assertEquals(res.getFirst(), "#_hello!#%suewon#_bahng#%");
		}
	}
	
	@Test
	public void testSearchParamsKeywords() {
		{
			final String words = "";
			final SearchParams.Keywords keywords = new SearchParams.Keywords(words);
			assertEquals(0, keywords.getWords().size());
			assertEquals(SearchParams.TARGET_ALL, keywords.getTarget());
			assertEquals(false, keywords.isAny());
		}
		{
			final String words = " \t: \t\n ";
			final SearchParams.Keywords keywords = new SearchParams.Keywords(words);
			assertEquals(0, keywords.getWords().size());
			assertEquals(SearchParams.TARGET_ALL, keywords.getTarget());
			assertEquals(false, keywords.isAny());
		}
		{
			final String words = " :  \"\"\t";
			final SearchParams.Keywords keywords = new SearchParams.Keywords(words);
			assertEquals(0, keywords.getWords().size());
			assertEquals(SearchParams.TARGET_ALL, keywords.getTarget());
			assertEquals(false, keywords.isAny());
		}
		{
			final String words = ":word phrase '' \"\" wholeWorld/w";
			final SearchParams.Keywords keywords = new SearchParams.Keywords(words);
			assertEquals(4, keywords.getWords().size());
			assertEquals(SearchParams.TARGET_ALL, keywords.getTarget());
			assertEquals(false, keywords.isAny());
		}
		{
			final String words = "any: 'hello _%' Phrase/c \"quoted phrase\" ";
			final SearchParams.Keywords keywords = new SearchParams.Keywords(words);
			assertEquals(4, keywords.getWords().size());
			assertEquals(SearchParams.TARGET_ALL, keywords.getTarget());
			assertEquals(true, keywords.isAny());
		}
		{
			final String words = "tag: tag";
			final SearchParams.Keywords keywords = new SearchParams.Keywords(words);
			assertEquals(SearchParams.TARGET_TAG, keywords.getTarget());
			assertEquals(false, keywords.isAny());
		}
		{
			final String words = "anytag:tag0 tag2";
			final SearchParams.Keywords keywords = new SearchParams.Keywords(words);
			assertEquals(SearchParams.TARGET_TAG, keywords.getTarget());
			assertEquals(true, keywords.isAny());
		}
		{
			final String words = "title: title";
			final SearchParams.Keywords keywords = new SearchParams.Keywords(words);
			assertEquals(SearchParams.TARGET_TITLE, keywords.getTarget());
			assertEquals(false, keywords.isAny());
		}
		{
			final String words = "anytitle:title \"title:\"  ";
			final SearchParams.Keywords keywords = new SearchParams.Keywords(words);
			assertEquals(SearchParams.TARGET_TITLE, keywords.getTarget());
			assertEquals(true, keywords.isAny());
		}
		{
			final String words = "text: ...";
			final SearchParams.Keywords keywords = new SearchParams.Keywords(words);
			assertEquals(1, keywords.getWords().size());
			assertEquals(SearchParams.TARGET_TEXT, keywords.getTarget());
			assertEquals(false, keywords.isAny());
		}
		{
			final String words = "anytext: . ! ?  ' \" ";
			final SearchParams.Keywords keywords = new SearchParams.Keywords(words);
			assertEquals(5, keywords.getWords().size());
			assertEquals(SearchParams.TARGET_TEXT, keywords.getTarget());
			assertEquals(true, keywords.isAny());
		}
		{
			final String words = "url:.com";
			final SearchParams.Keywords keywords = new SearchParams.Keywords(words);
			assertEquals(SearchParams.TARGET_URL, keywords.getTarget());
			assertEquals(false, keywords.isAny());
		}
		{
			final String words = "anyurl: .com .org ";
			final SearchParams.Keywords keywords = new SearchParams.Keywords(words);
			assertEquals(SearchParams.TARGET_URL, keywords.getTarget());
			assertEquals(true, keywords.isAny());
		}
	}
	
	@Test
	public void testSearchParams() {
		{
			final String searchPhrase = "";
			final SearchParams sp = new SearchParams(searchPhrase);
			assertEquals(0, sp.getKeywords().size());
		}
		{
			final String searchPhrase = ":";
			final SearchParams sp = new SearchParams(searchPhrase);
			assertEquals(0, sp.getKeywords().size());
		}
		{
			final String searchPhrase = "anytitle:";
			final SearchParams sp = new SearchParams(searchPhrase);
			assertEquals(0, sp.getKeywords().size());
		}
		{
			// [NOTE] any directive inside double quotes should be ignored
			final String searchPhrase = "anytitle:title \"any:\" url:.org";
			final SearchParams sp = new SearchParams(searchPhrase);
			assertEquals(2, sp.getKeywords().size());
		}
		
		{
			final String searchPhrase = "word phrase/w anytag: tag0";
			final SearchParams sp = new SearchParams(searchPhrase);
			assertEquals(2, sp.getKeywords().size());
		}
		{
			final String searchPhrase = "text:word phrase/w anytag:TAG any:";
			final SearchParams sp = new SearchParams(searchPhrase);
			assertEquals(2, sp.getKeywords().size());
		}
	}
	
}
