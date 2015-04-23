package com.civilizer.web.view;

import java.io.Serializable;

import com.civilizer.domain.SearchParams;

@SuppressWarnings("serial")
public final class SearchContextBean implements Serializable {
	
	private String quickSearchPhrase;
	private String tagKeywords;
	private String titleKeywords;
	private String contentKeywords;
	private String idKeywords;
	private boolean anyTag;
	private boolean anyTitle;
	private boolean anyContent;
	private int panelId = -1;
	
	public SearchContextBean() {
		reset();
	}
	
	public int getPanelId() {
		return panelId;
	}
	
	public void setPanelId(int panelId) {
	    this.panelId = panelId;
	}
	
	public String getQuickSearchPhrase() {
		return quickSearchPhrase;
	}
	
	public void setQuickSearchPhrase(String phrase) {
		this.quickSearchPhrase = phrase;
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
        SearchParams output = null;
        
        if (quickSearchPhrase.isEmpty()) {
        	final StringBuilder sb = new StringBuilder();
            
            if (tagKeywords.isEmpty() == false) {
            	if (anyTag) {
                	sb.append("any");
                }
            	sb.append("tag:").append(tagKeywords).append(' ');
            }
            if (titleKeywords.isEmpty() == false) {
            	if (anyTitle) {
            		sb.append("any");
            	}
            	sb.append("title:").append(titleKeywords).append(' ');
            }
            if (contentKeywords.isEmpty() == false) {
            	if (anyContent) {
            		sb.append("any");
            	}
            	sb.append("text:").append(contentKeywords).append(' ');
            }
            if (idKeywords.isEmpty() == false) {
            	sb.append("id:").append(idKeywords).append(' ');
            }
            
            output = new SearchParams(sb.toString());
        }
        else {
        	output = new SearchParams(quickSearchPhrase);
        }
        
        reset();
        return output;
    }
    
    private void reset() {
    	quickSearchPhrase = "";
    	tagKeywords = "";
    	titleKeywords = "";
    	contentKeywords = "";
    	idKeywords = "";
    	anyTag = false;
    	anyTitle = false;
    	anyContent = false;
    }

}
