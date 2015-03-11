package com.knowledgex.web.view;

import java.io.Serializable;
//import java.util.*;

@SuppressWarnings("serial")
public final class ParameterBean implements Serializable {
	
	private int panelId = 0;
	private int panelIdForTagPalette = 0;
	private int panelIdForSearch = 0;
	
	ParameterBean() {
	}

	public int getPanelId() {
		return panelId;
	}

	public void setPanelId(int panelId) {
		this.panelId = panelId;
	}

	public int getPanelIdForTagPalette() {
		return panelIdForTagPalette;
	}

	public void setPanelIdForTagPalette(int panelIdForTagPalette) {
		this.panelIdForTagPalette = panelIdForTagPalette;
	}

	public int getPanelIdForSearch() {
		return panelIdForSearch;
	}

	public void setPanelIdForSearch(int panelIdForSearch) {
		this.panelIdForSearch = panelIdForSearch;
	}

}
