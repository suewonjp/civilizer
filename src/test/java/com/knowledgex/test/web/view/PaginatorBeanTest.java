package com.knowledgex.test.web.view;

import static org.junit.Assert.*;

import java.util.*;

import org.junit.*;

import com.knowledgex.test.util.TestUtil;
import com.knowledgex.web.view.*;

public class PaginatorBeanTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testEdgeCases() {
        int curPage, itemsPerPage, maxItems, pagesPerChunk;
        List<Integer> accessiblePages = null;
        PaginatorBean pb = new PaginatorBean();
        itemsPerPage = 10;
        maxItems = TestUtil.getRandom().nextInt(100) + 20;
        pagesPerChunk = 8;
             
        curPage = -1;
        pb.paginate(curPage, itemsPerPage, maxItems, pagesPerChunk);
        accessiblePages = pb.getAccessiblePages();
        assertTrue(pb.getCurPage() == 0);
        assertTrue(pb.getItemsPerPage() == itemsPerPage);
        assertTrue(accessiblePages.size() <= pagesPerChunk);
        for (int i = 0; i < accessiblePages.size(); i++) {
            assertTrue(accessiblePages.get(i) == i);
        }
    }

}
