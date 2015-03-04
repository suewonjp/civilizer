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
    		assertEquals(kw.checkValidity(), false);
    	}
    	{
    		final String word = "\"\"";
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
    		final String word = "'";
    		final SearchParams.Keyword kw = new SearchParams.Keyword(word);
    		assertEquals(kw.getWord(), "'");
    		assertEquals(kw.checkValidity(), true);
    	}
    	{
    		final String word = "\"";
    		final SearchParams.Keyword kw = new SearchParams.Keyword(word);
    		assertEquals(kw.getWord(), "\"");
    		assertEquals(kw.checkValidity(), true);
    	}
    	{
    		// [NOTE] If flags are inside double quotes, they are identified as just normal characters
    		final String word = "\"Hello World/c\"";
    		final SearchParams.Keyword kw = new SearchParams.Keyword(word);
    		assertEquals(kw.getWord(), "Hello World/c");
    		assertEquals(kw.isCaseSensitive(), false);
    		assertEquals(kw.isWholeWord(), false);
    		assertEquals(kw.isAsIs(), false);
    		assertEquals(kw.checkValidity(), true);
    	}
    	{
    		// [NOTE] If flags follow single quotes, they get ignored
    		final String word = "'Hello World'/w";
    		final SearchParams.Keyword kw = new SearchParams.Keyword(word);
    		assertEquals(kw.getWord(), "Hello World");
    		assertEquals(kw.isCaseSensitive(), true);
    		assertEquals(kw.isWholeWord(), false);
    		assertEquals(kw.isAsIs(), true);
    		assertEquals(kw.checkValidity(), true);
    	}
    	
    	// trivial cases
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
    		final String word = "\"Hello World\"/c";
    		final SearchParams.Keyword kw = new SearchParams.Keyword(word);
    		assertEquals(kw.getWord(), "Hello World");
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
	
	@Test
	public void testEscapeSqlWildcards() {
		{
    		final String word = "_hello%suewon_bahng%";
    		final SearchParams.Keyword kw = new SearchParams.Keyword(word);
    		assertEquals(kw.checkValidity(), true);
    		final Pair<String, Character> res = SearchParams.Keyword.escapeSqlWildcardCharacters(word);
    		assertEquals(res.getSecond(), new Character('!'));
    		assertEquals(res.getFirst(), "!_hello!%suewon!_bahng!%");
    	}
		{
			final String word = "_hello!%suewon_bahng%";
			final SearchParams.Keyword kw = new SearchParams.Keyword(word);
			assertEquals(kw.checkValidity(), true);
			final Pair<String, Character> res = SearchParams.Keyword.escapeSqlWildcardCharacters(word);
			assertEquals(res.getSecond(), new Character('#'));
			assertEquals(res.getFirst(), "#_hello!#%suewon#_bahng#%");
		}
	}
	
//	@Test
//	public void testTranslateToPatternForSqlLIKEClause() {
//		{
//    		final String word = "'my keyword'";
//    		final SearchParams.Keyword kw = new SearchParams.Keyword(word);
//    		assertEquals(kw.isWholeWord(), false);
//    		assertEquals(kw.isAsIs(), true);
//    		assertEquals(kw.checkValidity(), true);
//    		final String translatedWord =
//    				SearchParams.Keyword.translateToPatternForSqlLIKEClause(kw.getWord(), kw.isWholeWord(), kw.isAsIs());
//    		assertEquals(translatedWord, "%my keyword%");
//    	}
//		{
//			final String word = "hello/w";
//			final SearchParams.Keyword kw = new SearchParams.Keyword(word);
//			assertEquals(kw.isWholeWord(), true);
//			assertEquals(kw.isAsIs(), false);
//			assertEquals(kw.checkValidity(), true);
//			final String translatedWord =
//					SearchParams.Keyword.translateToPatternForSqlLIKEClause(kw.getWord(), kw.isWholeWord(), kw.isAsIs());
//			assertEquals(translatedWord, "%[^a-z0-9_-]hello[^a-z0-9_-]%");
//		}
//		{
//			final String word = "?hello*world";
//			final SearchParams.Keyword kw = new SearchParams.Keyword(word);
//			assertEquals(kw.isWholeWord(), false);
//			assertEquals(kw.isAsIs(), false);
//			assertEquals(kw.checkValidity(), true);
//			final String translatedWord =
//					SearchParams.Keyword.translateToPatternForSqlLIKEClause(kw.getWord(), kw.isWholeWord(), kw.isAsIs());
//			assertEquals(translatedWord, "%_hello%world%");
//		}
//	}
	
	@Test
	public void testSearchParamsKeywords() {
		{
			final String words = "";
			final SearchParams.Keywords keywords = new SearchParams.Keywords(words);
			assertTrue(keywords.getWords().isEmpty());
			assertTrue(keywords.getTarget() == SearchParams.TARGET_ALL);
			assertTrue(keywords.isAny() == false);
		}
		{
			final String words = " \t: \t\n ";
			final SearchParams.Keywords keywords = new SearchParams.Keywords(words);
			assertTrue(keywords.getWords().isEmpty());
			assertTrue(keywords.getTarget() == SearchParams.TARGET_ALL);
			assertTrue(keywords.isAny() == false);
		}
		{
			final String words = ":word *phrase* '' \"\" wholeWorld/w";
			final SearchParams.Keywords keywords = new SearchParams.Keywords(words);
			assertTrue(keywords.getWords().size() == 5);
			assertTrue(keywords.getTarget() == SearchParams.TARGET_ALL);
			assertTrue(keywords.isAny() == false);
		}
		{
			final String words = "any: 'hello _%' ?Phrase*/c \"quoted phrase*\" ";
			final SearchParams.Keywords keywords = new SearchParams.Keywords(words);
			assertTrue(keywords.getWords().size() == 3);
			assertTrue(keywords.getTarget() == SearchParams.TARGET_ALL);
			assertTrue(keywords.isAny() == true);
		}
		{
			final String words = "tag: tag*";
			final SearchParams.Keywords keywords = new SearchParams.Keywords(words);
			assertTrue(keywords.getTarget() == SearchParams.TARGET_TAG);
			assertTrue(keywords.isAny() == false);
		}
		{
			final String words = "anyintag:tag0 tag2";
			final SearchParams.Keywords keywords = new SearchParams.Keywords(words);
			assertTrue(keywords.getTarget() == SearchParams.TARGET_TAG);
			assertTrue(keywords.isAny() == true);
		}
		{
			final String words = "title: title";
			final SearchParams.Keywords keywords = new SearchParams.Keywords(words);
			assertTrue(keywords.getTarget() == SearchParams.TARGET_TITLE);
			assertTrue(keywords.isAny() == false);
		}
		{
			final String words = "anyintitle:title* ?title?  ";
			final SearchParams.Keywords keywords = new SearchParams.Keywords(words);
			assertTrue(keywords.getTarget() == SearchParams.TARGET_TITLE);
			assertTrue(keywords.isAny() == true);
		}
		{
			final String words = "text: ...";
			final SearchParams.Keywords keywords = new SearchParams.Keywords(words);
			assertTrue(keywords.getWords().size() == 1);
			assertTrue(keywords.getTarget() == SearchParams.TARGET_TEXT);
			assertTrue(keywords.isAny() == false);
		}
		{
			final String words = "anyintext: . ! ?  ' \" ";
			final SearchParams.Keywords keywords = new SearchParams.Keywords(words);
			assertTrue(keywords.getWords().size() == 5);
			assertTrue(keywords.getTarget() == SearchParams.TARGET_TEXT);
			assertTrue(keywords.isAny() == true);
		}
		{
			final String words = "url:https*";
			final SearchParams.Keywords keywords = new SearchParams.Keywords(words);
			assertTrue(keywords.getTarget() == SearchParams.TARGET_URL);
			assertTrue(keywords.isAny() == false);
		}
		{
			final String words = "anyinurl: *.com *.org ";
			final SearchParams.Keywords keywords = new SearchParams.Keywords(words);
			assertTrue(keywords.getTarget() == SearchParams.TARGET_URL);
			assertTrue(keywords.isAny() == true);
		}
	}
	
}
