package com.civilizer.web.view;

import java.io.Serializable;

import com.civilizer.domain.SearchParams;

@SuppressWarnings("serial")
public final class PanelContextBean implements Serializable {
	
	public static final long ALL_VALID_TAGS       = -1000;
	public static final long EMPTY_TAG            = -2000;
	// [TODO] the default number of items per page should be configurable
	public static final int  DEF_ITEMS_PER_PAGE   = 10;
	
	private final SearchParams searchParams;
	private final long tagId;
	private final int panelId;
	private final int curPage;
	private final int itemsPerPage;
	private final boolean isLast;
	private final boolean fragmentDeletable;
	
	public PanelContextBean() {
		this.tagId = ALL_VALID_TAGS;
		this.panelId = 0;
		this.curPage = 0;
		this.itemsPerPage = DEF_ITEMS_PER_PAGE;
		this.isLast = false;
		fragmentDeletable = false;
		searchParams = null;
	}

	public PanelContextBean(int panelId, long tagId) {
		this.tagId = tagId;
		this.panelId = panelId;
		this.curPage = 0;
		this.itemsPerPage = DEF_ITEMS_PER_PAGE;
		this.isLast = false;
		fragmentDeletable = false;
		searchParams = null;
	}

	public PanelContextBean(int panelId, long tagId, int curPage) {
		this.tagId = tagId;
		this.panelId = panelId;
		this.curPage = curPage;
		this.itemsPerPage = DEF_ITEMS_PER_PAGE;
		this.isLast = false;
		fragmentDeletable = false;
		searchParams = null;
	}
	
	public PanelContextBean(int panelId, long tagId, int curPage, int itemsPerPage, boolean isLast, boolean fragmentDeletable, SearchParams searchParams) {
		this.tagId = tagId;
		this.panelId = panelId;
		this.curPage = curPage;
		this.itemsPerPage = itemsPerPage;
		this.isLast = isLast;
		this.fragmentDeletable = fragmentDeletable;
		this.searchParams = searchParams;
	}
	
	public SearchParams getSearchParams() {
		return searchParams;
	}

	public long getTagId() {
		return tagId;
	}

	public int getPanelId() {
		return panelId;
	}

	public int getCurPage() {
		return curPage;
	}

	public int getItemsPerPage() {
	    return itemsPerPage;
	}

	public boolean isFirstPage() {
        return curPage == 0;
    }

    public boolean isLastPage() {
        return isLast;
    }
    
    public boolean isFragmentDeletable() {
        return fragmentDeletable;
    }
    
    public String getContextClassName() {
    	return searchParams == null ? "" : "fp-search";
    }
    
    public int firstItemOfCurPage() {
        return curPage * itemsPerPage;
    }

    public int lastItemOfCurPage(int maxItems) {
        return Math.min(maxItems, (curPage + 1) * itemsPerPage - 1);
    }
    
    @Override
    public boolean equals(Object obj) {
    	if (obj != null) {
    		if (obj instanceof PanelContextBean) {
    			PanelContextBean rhs = (PanelContextBean) obj;
    			return this.tagId == rhs.tagId && this.curPage == rhs.curPage;
    		}
    	}
    	return false;
    };
    
    @Override
    public String toString() {
    	return  "{tagId: " + tagId
    			+ "}, {curPage: " + curPage
                + "}, {itermsPerPage: " + itemsPerPage
                + "}, {isLast: " + isLast
                + "},"
                ;
    }

}
