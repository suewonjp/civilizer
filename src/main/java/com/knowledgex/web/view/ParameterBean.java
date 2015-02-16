package com.knowledgex.web.view;

import java.io.Serializable;
//import java.util.*;

@SuppressWarnings("serial")
public class ParameterBean implements Serializable {
	
	private int panelId = 0;
	
//	private List<Integer> activePanels;
	
	ParameterBean() {
//	    activePanels = new ArrayList<Integer>();
//	    activePanels.add(0);
	}

	public int getPanelId() {
		return panelId;
	}

	public void setPanelId(int panelId) {
		this.panelId = panelId;
	}

//    public List<Integer> getActivePanels() {
//        return activePanels;
//    }
//
//    public void setActivePanels(List<Integer> activePanels) {
//        this.activePanels = activePanels;
//    }

}
