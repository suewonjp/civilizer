package com.civilizer.web.view;

import java.io.Serializable;

import com.civilizer.domain.SearchParams;

@SuppressWarnings("serial")
public final class SearchContextBean implements Serializable {
	
	private String quickSearchText = "";
	private final int panelId;
	
	public SearchContextBean(int panelId) {
		this.panelId = panelId;
	}

	public int getPanelId() {
		return panelId;
	}
	
	public String getQuickSearchText() {
		return quickSearchText;
	}
	
	public void setQuickSearchText(String quickSearchText) {
		this.quickSearchText = quickSearchText;
	}
	
	public SearchParams buildSearchParams() {
		if (! quickSearchText.isEmpty()) {
			return new SearchParams(quickSearchText);
		}
		// [TODO] build search parameters from the various data collected with the view layer
		return null;
	}

}
