package com.knowledgex.web.view;

import java.io.Serializable;
import java.util.*;

@SuppressWarnings("serial")
public class FragmentListBean implements Serializable {
    
    private List<FragmentBean> fragmentBeans;
    
    private boolean displayingTrash = false;
    
    public boolean isDisplayingTrash() {
		return displayingTrash;
	}

	public void setDisplayingTrash(boolean displayingTrash) {
		this.displayingTrash = displayingTrash;
	}
	
    public Collection<FragmentBean> getFragmentBeans() {
    	return fragmentBeans;
    }
    
    public void setFragmentBeans(List<FragmentBean> fragmentBeans) {
    	this.fragmentBeans = fragmentBeans;
    }
    
    public FragmentBean getFragmentBeanAt(int index) {
    	return fragmentBeans.get(index);
    }

}
