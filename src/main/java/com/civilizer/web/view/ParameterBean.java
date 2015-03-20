package com.civilizer.web.view;

import java.io.Serializable;
//import java.util.*;

@SuppressWarnings("serial")
public final class ParameterBean implements Serializable {
	
	private int panelId = 0;
	private int panelIdForTagPalette = 0;
	private int panelIdForSearch = 0;
	private long fragmentId0;
	private long fragmentId1;
	
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

	public long getFragmentId0() {
		return fragmentId0;
	}

	public void setFragmentId0(long fragmentId0) {
		this.fragmentId0 = fragmentId0;
	}

	public long getFragmentId1() {
		return fragmentId1;
	}

	public void setFragmentId1(long fragmentId1) {
		this.fragmentId1 = fragmentId1;
	}

}
