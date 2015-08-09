package com.civilizer.test.domain;

import static org.junit.Assert.*;

import java.util.*;

import org.junit.*;

import com.civilizer.domain.Fragment;
import com.civilizer.domain.SearchParams;
import com.civilizer.domain.Tag;
import com.civilizer.test.helper.TestUtil;
import com.civilizer.utils.Pair;

public class DomainSearchParamsTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testSearchParamsKeyword() {
		// edge cases
    	{
    		final String word = "";
    		final SearchParams.Keyword kw = new SearchParams.Keyword(word, false);
    		assertEquals(kw.getWord(), "");
    		assertEquals(kw.isValid(), false);
    	}
    	{
    		final String word = "\"\"";
    		final SearchParams.Keyword kw = new SearchParams.Keyword(word, false);
    		assertEquals(kw.getWord(), "");
    		assertEquals(kw.isValid(), false);
    	}
    	{
    		final String word = "''";
    		final SearchParams.Keyword kw = new SearchParams.Keyword(word, false);
    		assertEquals(kw.getWord(), "''");
    		assertEquals(kw.isValid(), true);
    	}
    	{
    		final String word = "'";
    		final SearchParams.Keyword kw = new SearchParams.Keyword(word, false);
    		assertEquals(kw.getWord(), "'");
    		assertEquals(kw.isValid(), true);
    	}
    	{
    		final String word = "\"";
    		final SearchParams.Keyword kw = new SearchParams.Keyword(word, false);
    		assertEquals(kw.getWord(), "\"");
    		assertEquals(kw.isValid(), true);
    	}
    	{
    		// [NOTE] If flags are inside double quotes, they are identified as just normal characters
    		final String word = "\"Hello World/c\"";
    		final SearchParams.Keyword kw = new SearchParams.Keyword(word, false);
    		assertEquals(kw.getWord(), "Hello World/c");
    		assertEquals(kw.isCaseSensitive(), false);
    		assertEquals(kw.isWholeWord(), false);
    		assertEquals(kw.isValid(), true);
    	}
    	{
    		final String word = "hello/c";
    		final SearchParams.Keyword kw = new SearchParams.Keyword(word, false);
    		assertEquals(kw.getWord(), "hello");
    		assertEquals(kw.isCaseSensitive(), true);
    		assertEquals(kw.isWholeWord(), false);
    		assertEquals(kw.isValid(), true);
    	}
    	{
    		final String word = "\"Hello World\"/c";
    		final SearchParams.Keyword kw = new SearchParams.Keyword(word, false);
    		assertEquals(kw.getWord(), "Hello World");
    		assertEquals(kw.isCaseSensitive(), true);
    		assertEquals(kw.isWholeWord(), false);
    		assertEquals(kw.isValid(), true);
    	}
    	{
    		final String word = "hello/w";
    		final SearchParams.Keyword kw = new SearchParams.Keyword(word, false);
    		assertEquals(kw.getWord(), "hello");
    		assertEquals(kw.isCaseSensitive(), false);
    		assertEquals(kw.isWholeWord(), true);
    		assertEquals(kw.isValid(), true);
    	}
    	{
    		final String word = TestUtil.getRandom().nextBoolean() ?
    				"hello/wc" : "hello/cw";
    		final SearchParams.Keyword kw = new SearchParams.Keyword(word, false);
    		assertEquals(kw.getWord(), "hello");
    		assertEquals(kw.isCaseSensitive(), true);
    		assertEquals(kw.isWholeWord(), true);
    		assertEquals(kw.isValid(), true);
    	}
	}
	
	@Test
	public void testEscapeSqlWildcards() {
		{
    		final String word = "_hello%suewon_bahng%";
    		final SearchParams.Keyword kw = new SearchParams.Keyword(word, false);
    		assertEquals(kw.isValid(), true);
    		final Pair<String, Character> res = SearchParams.Keyword.escapeSqlWildcardCharacters(word);
    		assertEquals(res.getSecond(), new Character('!'));
    		assertEquals(res.getFirst(), "!_hello!%suewon!_bahng!%");
    	}
		{
			final String word = "_hello!%suewon_bahng%";
			final SearchParams.Keyword kw = new SearchParams.Keyword(word, false);
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
			assertEquals(SearchParams.TARGET_DEFAULT, keywords.getTarget());
			assertEquals(false, keywords.isAny());
		}
		{
			final String words = " \t: \t\n ";
			final SearchParams.Keywords keywords = new SearchParams.Keywords(words);
			assertEquals(0, keywords.getWords().size());
			assertEquals(SearchParams.TARGET_DEFAULT, keywords.getTarget());
			assertEquals(false, keywords.isAny());
		}
		{
			final String words = " :  \"\"\t";
			final SearchParams.Keywords keywords = new SearchParams.Keywords(words);
			assertEquals(0, keywords.getWords().size());
			assertEquals(SearchParams.TARGET_DEFAULT, keywords.getTarget());
			assertEquals(false, keywords.isAny());
		}
		{
			final String words = ":word phrase '' \"\" wholeWorld/w";
			final SearchParams.Keywords keywords = new SearchParams.Keywords(words);
			assertEquals(4, keywords.getWords().size());
			assertEquals(SearchParams.TARGET_DEFAULT, keywords.getTarget());
			assertEquals(false, keywords.isAny());
		}
		{
			final String words = "any: 'hello _%' Phrase/c \"quoted phrase\" ";
			final SearchParams.Keywords keywords = new SearchParams.Keywords(words);
			assertEquals(4, keywords.getWords().size());
			assertEquals(SearchParams.TARGET_DEFAULT, keywords.getTarget());
			assertEquals(true, keywords.isAny());
		}
		{
			final String words = "tag: tag";
			final SearchParams.Keywords keywords = new SearchParams.Keywords(words);
			assertEquals(SearchParams.TARGET_TAG, keywords.getTarget());
			assertEquals(false, keywords.isAny());
		}
		{
		    // As a special rule, tags with trailing commas can be accepted;
		    // and those commas should be trimmed
		    final String words = "tag: tag0, tag1, , , tag2, , , ,";
		    final SearchParams.Keywords keywords = new SearchParams.Keywords(words);
		    assertEquals(SearchParams.TARGET_TAG, keywords.getTarget());
		    assertEquals(3, keywords.getWords().size());
	        assertEquals("tag0", keywords.getWords().get(0).getWord());
	        assertEquals("tag1", keywords.getWords().get(1).getWord());
	        assertEquals("tag2", keywords.getWords().get(2).getWord());
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
			final String words = "id:1 3 5 9 11 013";
			final SearchParams.Keywords keywords = new SearchParams.Keywords(words);
			assertEquals(6, keywords.getWords().size());
			assertEquals(SearchParams.TARGET_ID, keywords.getTarget());
			assertEquals(true, keywords.isAny());
			for (SearchParams.Keyword kw : keywords.getWords()) {
				assertEquals(false, kw.isTrivial());
				assertEquals(true, kw.isId());
			}
		}
		// check against invalid id search;
        {
        	final String searchPhrase = "id: 0xfff invalid 32h 0"; // [NOTE] only the final input is acceptable here
        	final SearchParams sp = new SearchParams(searchPhrase);
        	assertEquals(1, sp.getKeywords().size());
        	assertEquals(1, sp.getKeywords().get(0).getWords().size());
        	assertEquals("0", sp.getKeywords().get(0).getWords().get(0).getWord());
        }
	}
	
	@Test
    public void testTagCache() {
	    final List<Tag> tags = DomainTagTest.buildTags(
                "my tag", "your tag", "Tag000",      
                "~tag~", "xxxYyy", "#bookmark",
                "wwwZzz"
                );
        
        Fragment f = new Fragment("fragment", "Some content", null);
        f.setId(0L);
        for (Tag t : tags) {
            f.addTag(t);
        }
        f.removeTag(tags.get(tags.size()-1)); // remove the tag "wwwZzz"
        
        assertEquals(tags.size()-1, f.getTags().size());
        
        {
            final SearchParams sp = new SearchParams("tag:tag");
            final SearchParams.TagCache tc = new SearchParams.TagCache(tags, sp);
            assertEquals(true, tc.matches(f));
        }
        {
            final SearchParams sp = new SearchParams("tag:tag/e Tag/b xxY/c");
            final SearchParams.TagCache tc = new SearchParams.TagCache(tags, sp);
            assertEquals(true, tc.matches(f));
        }
        {
            final SearchParams sp = new SearchParams("tag:tag/e Tag/b xxY/c zzz");
            final SearchParams.TagCache tc = new SearchParams.TagCache(tags, sp);
            assertEquals(false, tc.matches(f));
        }
        {
            final SearchParams sp = new SearchParams("tag:tag #/-");
            final SearchParams.TagCache tc = new SearchParams.TagCache(tags, sp);
            assertEquals(false, tc.matches(f));
        }
        {
            final SearchParams sp = new SearchParams("tag:\"my tag\" zzz/-");
            final SearchParams.TagCache tc = new SearchParams.TagCache(tags, sp);
            assertEquals(true, tc.matches(f));
        }
        {
            final SearchParams sp = new SearchParams("anytag:\"my tag\"/be zzz");
            final SearchParams.TagCache tc = new SearchParams.TagCache(tags, sp);
            assertEquals(true, tc.matches(f));
        }
        {
            final SearchParams sp = new SearchParams("anytag: zzz/- zzz www");
            final SearchParams.TagCache tc = new SearchParams.TagCache(tags, sp);
            assertEquals(true, tc.matches(f));
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
			final String searchPhrase = "anytitle:title \"any:\" text:\"good content\"";
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
