package com.knowledgex.test.web.view;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.knowledgex.test.util.TestUtil;
import com.knowledgex.web.view.PaginatorBean;

public class PaginatorBeanTest {
    
    PaginatorBean pb;
    
    @Before
    public void setUp() throws Exception {
        pb = new PaginatorBean();
    }
    
    @Test
    public void testInitalStatus() {
        assertTrue(pb.isFirstPage());
        assertFalse(pb.isLastPage());
        assertEquals(pb.getCurPage(), 0);
    }
    
    @Test
    public void testBackwardEdgeCases() {
        int curPage = 0;
        boolean isLastPage = TestUtil.getRandom().nextBoolean();
        pb.setCurPageAsLast(isLastPage);
        assertEquals(pb.getCurPage(), curPage);
        assertEquals(pb.isLastPage(), isLastPage);

        pb.backwardPage(curPage);
        assertEquals(pb.getCurPage(), curPage);
        assertEquals(pb.isLastPage(), isLastPage);
    }

    @Test
    public void testForwardEdgeCases() {
        int curPage = 0;
        boolean isLastPage = true;
        
        pb.setCurPageAsLast(isLastPage);
        assertEquals(pb.isLastPage(), isLastPage);
        assertEquals(pb.getCurPage(), curPage);
        
        pb.forwardPage(curPage);
        assertEquals(pb.getCurPage(), curPage);
        assertEquals(pb.isLastPage(), isLastPage);
        
        isLastPage = false;
        pb.setCurPageAsLast(isLastPage);
        assertEquals(pb.isLastPage(), isLastPage);
        
        int additionalPages = 5;
        
        for (int i = 1; i < additionalPages; i++) {
            curPage = pb.getCurPage();
            pb.forwardPage(curPage);
            assertEquals(pb.getCurPage(), curPage + 1);
            assertEquals(pb.isLastPage(), isLastPage);
        }
        
        isLastPage = true;
        pb.setCurPageAsLast(isLastPage);
        assertEquals(pb.isLastPage(), isLastPage);
        curPage = pb.getCurPage();
        pb.forwardPage(curPage);
        assertEquals(pb.getCurPage(), curPage);
        curPage = pb.getCurPage();
        pb.forwardPage(curPage);
        assertEquals(pb.getCurPage(), curPage);
    }

}
