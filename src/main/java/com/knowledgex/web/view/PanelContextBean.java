package com.knowledgex.web.view;

import java.io.Serializable;

@SuppressWarnings("serial")
public class PanelContextBean implements Serializable {
	
	private final long tagId;
	private final int curPage;
	private final int itemsPerPage;
	private final boolean isLast;
	
	public PanelContextBean(int tagId, int curPage, int itemsPerPage, boolean isLast) {
		this.tagId = tagId;
		this.curPage = curPage;
		this.itemsPerPage = itemsPerPage;
		this.isLast = isLast;
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
