package com.knowledgex.web.view;

import java.io.Serializable;
import java.util.*;

@SuppressWarnings("serial")
public final class FragmentListBean implements Serializable {
    
    private List<FragmentBean> fragmentBeans;
    
    private PanelContextBean panelContextBean;
    
    public Collection<FragmentBean> getFragmentBeans() {
    	return fragmentBeans;
    }
    
    public void setFragmentBeans(List<FragmentBean> fragmentBeans) {
    	this.fragmentBeans = fragmentBeans;
    }
    
    public PanelContextBean getPanelContextBean() {
		return panelContextBean;
	}

	public void setPanelContextBean(PanelContextBean panelContextBean) {
		this.panelContextBean = panelContextBean;
	}

	public FragmentBean getFragmentBeanAt(int index) {
    	return fragmentBeans.get(index);
    }

	public boolean hasFragments() {
		return !fragmentBeans.isEmpty();
	}

}
