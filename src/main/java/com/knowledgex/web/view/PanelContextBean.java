package com.knowledgex.web.view;

import java.io.Serializable;

@SuppressWarnings("serial")
public final class PanelContextBean implements Serializable {
	
	public static final long TAG_ID_FOR_ALL_VALID_TAGS = -1;
	
	private final long tagId;
	private final int curPage;
	private final int itemsPerPage;
	private final boolean isLast;
	private final boolean fragmentDeletable;
	
	public PanelContextBean() {
		this.tagId = TAG_ID_FOR_ALL_VALID_TAGS;
		this.curPage = 0;
		this.itemsPerPage = 10;
		this.isLast = false;
		fragmentDeletable = false;
	}

	public PanelContextBean(long tagId, int curPage) {
		this.tagId = tagId;
		this.curPage = curPage;
		this.itemsPerPage = 10;
		this.isLast = false;
		fragmentDeletable = false;
	}
	
	public PanelContextBean(long tagId, int curPage, int itemsPerPage, boolean isLast, boolean fragmentDeletable) {
		this.tagId = tagId;
		this.curPage = curPage;
		this.itemsPerPage = itemsPerPage;
		this.isLast = isLast;
		this.fragmentDeletable = fragmentDeletable;
	}
	
	public long getTagId() {
		return tagId;
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
    
    @Override
    public boolean equals(Object obj) {
    	if (null != obj) {
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
