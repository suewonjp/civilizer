package com.knowledgex.web.view;

import java.io.Serializable;

@SuppressWarnings("serial")
public class PaginatorBean implements Serializable {
	
	private int curPage = 0;
	private int itemsPerPage = 10;
	private boolean isLast = false;
	
	public PaginatorBean() {}

	public void reset() {
		curPage = 0;
	}
	
	public void setCurPageAsLast(boolean isLast) {
	    this.isLast = isLast;
	}
	
	public int getCurPage() {
		return curPage;
	}

	public int getItemsPerPage() {
	    return itemsPerPage;
	}

    public void setItemsPerPage(int items) {
        itemsPerPage = items;
    }

	public void forwardPage(int curPage) {
	    if (!isLast) {
	        this.curPage = curPage + 1;
	    }
	}
	
	public void backwardPage(int curPage) {
	    this.curPage = Math.max(0, --curPage);
	}
	
    public boolean isFirstPage() {
        return curPage == 0;
    }

    public boolean isLastPage() {
        return isLast;
    }
    
    @Override
    public String toString() {
    	return "{curPage: " + curPage
                + "}, {itermsPerPage: " + itemsPerPage
                + "}, {isLast: " + isLast
                + "},"
                ;
    }
    
}
