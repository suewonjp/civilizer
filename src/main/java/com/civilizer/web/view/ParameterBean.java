package com.civilizer.web.view;

import java.io.Serializable;
//import java.util.*;

@SuppressWarnings("serial")
public final class ParameterBean implements Serializable {
	
	private int panelId = 0;
	
	ParameterBean() {
	}

	public int getPanelId() {
		return panelId;
	}

	public void setPanelId(int panelId) {
		this.panelId = panelId;
	}

	public void clearPanelId() {
	    panelId = -1;
	}

}
