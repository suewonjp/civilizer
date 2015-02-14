package com.knowledgex.web.view;

import java.io.Serializable;

@SuppressWarnings("serial")
public class ParameterBean implements Serializable {
	
	private int panelId = 0;

	public int getPanelId() {
		return panelId;
	}

	public void setPanelId(int panelId) {
		this.panelId = panelId;
	}

}
