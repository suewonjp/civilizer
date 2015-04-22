package com.civilizer.web.view;

import java.io.Serializable;

import com.civilizer.domain.SearchParams;

@SuppressWarnings("serial")
public final class SearchContextBean implements Serializable {
	
	private String quickSearchText = "";
	private String tagKeywords = "";
	private String titleKeywords = "";
	private String contentKeywords = "";
	private String idKeywords = "";
	private boolean anyTag;
	private boolean anyTitle;
	private boolean anyContent;
	private int panelId = -1;
	
	public SearchContextBean() {
	}

	public int getPanelId() {
		return panelId;
	}
	
	public void setPanelId(int panelId) {
	    this.panelId = panelId;
	}
	
	public String getQuickSearchText() {
		return quickSearchText;
	}
	
	public void setQuickSearchText(String quickSearchText) {
		this.quickSearchText = quickSearchText;
	}

    public String getTagKeywords() {
        return tagKeywords;
    }

    public void setTagKeywords(String tagKeywords) {
        this.tagKeywords = tagKeywords;
    }

    public String getTitleKeywords() {
        return titleKeywords;
    }

    public void setTitleKeywords(String titleKeywords) {
        this.titleKeywords = titleKeywords;
    }

    public String getContentKeywords() {
        return contentKeywords;
    }

    public void setContentKeywords(String contentKeywords) {
        this.contentKeywords = contentKeywords;
    }

    public String getIdKeywords() {
        return idKeywords;
    }

    public void setIdKeywords(String idKeywords) {
        this.idKeywords = idKeywords;
    }

    public boolean isAnyTag() {
        return anyTag;
    }

    public void setAnyTag(boolean anyTag) {
        this.anyTag = anyTag;
    }

    public boolean isAnyTitle() {
        return anyTitle;
    }

    public void setAnyTitle(boolean anyTitle) {
        this.anyTitle = anyTitle;
    }

    public boolean isAnyContent() {
        return anyContent;
    }

    public void setAnyContent(boolean anyContent) {
        this.anyContent = anyContent;
    }
    
    public SearchParams buildSearchParams() {
        if (! quickSearchText.isEmpty()) {
            return new SearchParams(quickSearchText);
        }
        // [TODO] build search parameters from the various data collected with the view layer
        return null;
    }

}
