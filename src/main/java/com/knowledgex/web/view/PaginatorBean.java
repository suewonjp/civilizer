package com.knowledgex.web.view;

import java.io.Serializable;

@SuppressWarnings("serial")
public class PaginatorBean implements Serializable {
	
	private int curPage = 0;
	private int itemsPerPage = 10;
	private boolean isLast = false;
	
	public PaginatorBean() {}

	// called by the controller
	public void reset() {
		curPage = 0;
	}
	
	// called by the controller
	public void setCurPageAsLast(boolean isLast) {
	    this.isLast = isLast;
	}
	
	// called by the controller
	public int getCurPage() {
		return curPage;
	}

	// called by the controller
	public int getItemsPerPage() {
	    return itemsPerPage;
	}

    // called by the views
    public void setItemsPerPage(int items) {
        itemsPerPage = items;
    }

	// called by the views
	public void forwardPage() {
	    if (!isLast) {
	        ++curPage;
	    }
	}

	// called by the views
	public void backwardPage() {
	    curPage = Math.max(0, --curPage);
	}
	
	// called by the views
    public boolean isFirstPage() {
        return curPage == 0;
    }

    // called by the views
    public boolean isLastPage() {
        return isLast;
    }
    
}
