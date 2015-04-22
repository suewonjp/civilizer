package com.civilizer.web.view;

import java.io.Serializable;
import java.util.*;

@SuppressWarnings("serial")
public final class FragmentListBean implements Serializable {
    
    private List<FragmentBean> fragmentBeans = Collections.emptyList();
    
    private PanelContextBean panelContextBean;
    
    private int orderOption = 0;
    
    private boolean orderAsc = false;
    
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

	public int getOrderOption() {
		return orderOption;
	}

	public void setOrderOption(int orderOption) {
		this.orderOption = orderOption;
	}

	public boolean isOrderAsc() {
		return orderAsc;
	}

	public void setOrderAsc(boolean orderAsc) {
		this.orderAsc = orderAsc;
	}

}
