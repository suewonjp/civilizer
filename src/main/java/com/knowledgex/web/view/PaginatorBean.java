package com.knowledgex.web.view;

import java.util.*;
import java.io.Serializable;

@SuppressWarnings("serial")
public class PaginatorBean implements Serializable {
	
	private static final int MAX_VISIBLE_PAGE_COUNT = 5;
	
	private Integer curPage = new Integer(0);
	private Integer pageSize = new Integer(10);
	private Integer maxPages = new Integer(1);
	private List<Integer> visiblePages = new ArrayList<Integer>(MAX_VISIBLE_PAGE_COUNT);
	
	public PaginatorBean() {}

	public PaginatorBean(int curPage, int pageSize, int maxPages) {
		this.curPage = curPage;
		this.pageSize = pageSize;
		this.maxPages = maxPages;
		setVisiblePages();
	}
	
	public Integer getCurPage() {
		return curPage;
	}
	
	public void setCurPage(Integer curPage) {
		this.curPage = curPage;
	}
	
	public Integer getPageSize() {
		return pageSize;
	}
	
	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}

	public Integer getMaxPages() {
		return maxPages;
	}
	
	public void setMaxPages(Integer maxPages) {
		this.maxPages = maxPages;
	}
	
	public void computeMaxPages(int maxItems) {
		maxPages = (maxItems + pageSize - 1) / pageSize;
	}

	public List<Integer> getVisiblePages() {
		return visiblePages;
	}
	
	private void setVisiblePages() {
		int maxChunks = (maxPages + MAX_VISIBLE_PAGE_COUNT - 1) / MAX_VISIBLE_PAGE_COUNT;
		int curChunk = curPage/MAX_VISIBLE_PAGE_COUNT;
		int c = MAX_VISIBLE_PAGE_COUNT;
		if (curChunk >= maxChunks-1) {
			c = maxChunks % MAX_VISIBLE_PAGE_COUNT;
		}
		for (int i=0; i<c; ++i) {
			visiblePages.add(i + MAX_VISIBLE_PAGE_COUNT*curChunk);
		}
	}

}
