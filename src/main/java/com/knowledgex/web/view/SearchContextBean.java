package com.knowledgex.web.view;

import java.io.Serializable;

import com.knowledgex.domain.SearchParams;

@SuppressWarnings("serial")
public final class SearchContextBean implements Serializable {
	
	private final int panelId;
	
	public SearchContextBean(int panelId) {
		this.panelId = panelId;
	}

	public int getPanelId() {
		return panelId;
	}

	public static SearchParams buildSearchParams() {
		// [TODO] build search parameters from the various data collected with the view layer
		return new SearchParams(null); // [STUB]
	}

}
