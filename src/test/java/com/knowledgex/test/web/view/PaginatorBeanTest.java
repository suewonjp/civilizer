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

        pb.backwardPage();
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
        
        pb.forwardPage();
        assertEquals(pb.getCurPage(), curPage);
        assertEquals(pb.isLastPage(), isLastPage);
        
        isLastPage = false;
        pb.setCurPageAsLast(isLastPage);
        assertEquals(pb.isLastPage(), isLastPage);
        
        int additionalPages = 5;
        
        for (int i = 1; i < additionalPages; i++) {
            pb.forwardPage();
            assertEquals(pb.getCurPage(), curPage + i);
            assertEquals(pb.isLastPage(), isLastPage);
        }
        
        curPage = pb.getCurPage();
        isLastPage = true;
        pb.setCurPageAsLast(isLastPage);
        assertEquals(pb.isLastPage(), isLastPage);
        pb.forwardPage();
        assertEquals(pb.getCurPage(), curPage);
        pb.forwardPage();
        assertEquals(pb.getCurPage(), curPage);
    }

}
