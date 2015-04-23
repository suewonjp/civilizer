package com.civilizer.test.web;

import static org.junit.Assert.*;

import org.junit.Test;

import com.civilizer.domain.SearchParams;
import com.civilizer.test.util.TestUtil;
import com.civilizer.web.view.SearchContextBean;

public class SearchContextBeanTest {
	
	@Test
    public void testInitStatus() {
    	SearchContextBean scb = new SearchContextBean();
    	assertEquals(true, scb.getQuickSearchPhrase().isEmpty());
    	assertEquals(true, scb.getTagKeywords().isEmpty());
    	assertEquals(true, scb.getTitleKeywords().isEmpty());
    	assertEquals(true, scb.getContentKeywords().isEmpty());
    	assertEquals(true, scb.getIdKeywords().isEmpty());
    	assertEquals(false, scb.isAnyTag());
    	assertEquals(false, scb.isAnyTitle());
    	assertEquals(false, scb.isAnyContent());
    	assertEquals(true, scb.getPanelId() < 0);
    	SearchParams sp = scb.buildSearchParams();
		assertNotNull(sp);
		assertEquals(0, sp.getKeywords().size());
    }

	@Test
    public void testQuickSearchPhrase() {
    	SearchContextBean scb = new SearchContextBean();
    	scb.setTagKeywords("tag0");
    	scb.setQuickSearchPhrase("anytitle: title0 title1");
    	SearchParams sp = scb.buildSearchParams();
    	assertNotNull(sp);
    	assertEquals(1, sp.getKeywords().size());
    	assertEquals(2, sp.getKeywords().get(0).getWords().size());
    }

	@Test
	public void testTagKeywords() {
		SearchContextBean scb = new SearchContextBean();
		scb.setTagKeywords(" tag0  tag1 tag2  ");
		final boolean any = TestUtil.getRandom().nextBoolean();
		scb.setAnyTag(any);
		SearchParams sp = scb.buildSearchParams();
		assertNotNull(sp);
		assertEquals(1, sp.getKeywords().size());
		assertEquals(any, sp.getKeywords().get(0).isAny());
		assertEquals(SearchParams.TARGET_TAG, sp.getKeywords().get(0).getTarget());
		assertEquals(3, sp.getKeywords().get(0).getWords().size());
		assertEquals("tag2", sp.getKeywords().get(0).getWords().get(2).getWord());
	}

	@Test
	public void testTitleKeywords() {
		SearchContextBean scb = new SearchContextBean();
		scb.setTitleKeywords("title0, title1 ");
		final boolean any = TestUtil.getRandom().nextBoolean();
		scb.setAnyTitle(any);
		SearchParams sp = scb.buildSearchParams();
		assertNotNull(sp);
		assertEquals(1, sp.getKeywords().size());
		assertEquals(any, sp.getKeywords().get(0).isAny());
		assertEquals(SearchParams.TARGET_TITLE, sp.getKeywords().get(0).getTarget());
		assertEquals(2, sp.getKeywords().get(0).getWords().size());
		assertEquals("title0,", sp.getKeywords().get(0).getWords().get(0).getWord());
	}

	@Test
	public void testContentKeywords() {
		SearchContextBean scb = new SearchContextBean();
		scb.setContentKeywords("text0 text1 \"text2\" text3 ");
		final boolean any = TestUtil.getRandom().nextBoolean();
		scb.setAnyContent(any);
		SearchParams sp = scb.buildSearchParams();
		assertNotNull(sp);
		assertEquals(1, sp.getKeywords().size());
		assertEquals(any, sp.getKeywords().get(0).isAny());
		assertEquals(SearchParams.TARGET_TEXT, sp.getKeywords().get(0).getTarget());
		assertEquals(4, sp.getKeywords().get(0).getWords().size());
		assertEquals("text2", sp.getKeywords().get(0).getWords().get(2).getWord());
	}

	@Test
	public void testIdKeywords() {
		SearchContextBean scb = new SearchContextBean();
		scb.setIdKeywords(" 0xff  3 6 9");
		SearchParams sp = scb.buildSearchParams();
		assertNotNull(sp);
		assertEquals(1, sp.getKeywords().size());
		assertEquals(true, sp.getKeywords().get(0).isAny());
		assertEquals(SearchParams.TARGET_ID, sp.getKeywords().get(0).getTarget());
		assertEquals(3, sp.getKeywords().get(0).getWords().size());
		assertEquals("9", sp.getKeywords().get(0).getWords().get(2).getWord());
	}

	@Test
	public void testMixed() {
		SearchContextBean scb = new SearchContextBean();
		scb.setTagKeywords("tag0");
		scb.setTitleKeywords("title0 title1");
		scb.setAnyTitle(true);
		scb.setContentKeywords("text0 text1 text2");
		scb.setIdKeywords("1 2 3 4 5 6 7");
		SearchParams sp = scb.buildSearchParams();
		assertNotNull(sp);
		assertEquals(4, sp.getKeywords().size());
		for (SearchParams.Keywords kws : sp.getKeywords()) {
			if (kws.getTarget() == SearchParams.TARGET_TAG) {
				assertEquals(false, kws.isAny());
				assertEquals(1, kws.getWords().size());
				assertEquals("tag0", kws.getWords().get(0).getWord());
			}
			else if (kws.getTarget() == SearchParams.TARGET_TITLE) {
				assertEquals(true, kws.isAny());
				assertEquals(2, kws.getWords().size());
				assertEquals("title0", kws.getWords().get(0).getWord());
			}
			else if (kws.getTarget() == SearchParams.TARGET_TEXT) {
				assertEquals(false, kws.isAny());
				assertEquals(3, kws.getWords().size());
				assertEquals("text2", kws.getWords().get(2).getWord());
			}
			else if (kws.getTarget() == SearchParams.TARGET_ID) {
				assertEquals(true, kws.isAny());
				assertEquals(7, kws.getWords().size());
				assertEquals("1", kws.getWords().get(0).getWord());
			}
		}
	}

}
