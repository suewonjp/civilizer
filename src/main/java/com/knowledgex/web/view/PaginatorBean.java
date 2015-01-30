package com.knowledgex.web.view;

import java.util.*;
import java.io.Serializable;

@SuppressWarnings("serial")
public class PaginatorBean implements Serializable {
	
	private Integer curPage = new Integer(0);
	private Integer itemsPerPage = new Integer(10);
	private Integer maxPages = new Integer(1);
	private List<Integer> accessiblePages = new ArrayList<Integer>();
	
	public PaginatorBean() {}

	public void paginate(int curPage, int itemsPerPage, int maxItems, int pagesPerChunk) {
	    setItemsPerPage(itemsPerPage);
	    maxPages = (maxItems + itemsPerPage - 1) / itemsPerPage;
		setCurPage(curPage);
		populateAccessiblePages(pagesPerChunk);
	}
	
	public Integer getCurPage() {
		return curPage;
	}
	
	public void setCurPage(Integer curPage) {
	    curPage = Math.max(0, curPage);
	    curPage = Math.min(curPage, maxPages-1);
	    this.curPage = curPage;
	}
	
	public void forwardPage() {
	    curPage = Math.min(curPage+1, maxPages-1);
	}

	public void backwardPage() {
	    curPage = Math.max(0, curPage-1);
	}
	
	public Integer getItemsPerPage() {
        return itemsPerPage;
    }

    public void setItemsPerPage(Integer itemsPerPage) {
        if (itemsPerPage <= 0) {
            throw new IllegalArgumentException();
        }
        this.itemsPerPage = itemsPerPage;
    }

    public List<Integer> getAccessiblePages() {
        return accessiblePages;
    }

//    public void setAccessiblePages(List<Integer> accessiblePages) {
//        this.accessiblePages = accessiblePages;
//    }
	
	private void populateAccessiblePages(int pagesPerChunk) {
		int maxChunks = (maxPages + pagesPerChunk - 1) / pagesPerChunk;
		int curChunk = curPage / pagesPerChunk;
		int c = pagesPerChunk;
		if (curChunk >= maxChunks-1) {
			c = maxPages % pagesPerChunk;
		}
		accessiblePages.clear();
		for (int i=0; i<c; ++i) {
			accessiblePages.add(i + pagesPerChunk*curChunk);
		}
	}

}
