package com.civilizer.web.view;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.civilizer.domain.Tag;

@SuppressWarnings("serial")
public final class SpecialTagBean implements Serializable {
	
	private List<FragmentBean> fragmentBeans = Collections.emptyList();
	
	private Tag tag;
	
	public Collection<FragmentBean> getFragmentBeans() {
    	return fragmentBeans;
    }
    
    public void setFragmentBeans(List<FragmentBean> fragmentBeans) {
    	this.fragmentBeans = fragmentBeans;
    }
    
	public Tag getTag() {
		return tag;
	}

	public void setTag(Tag tag) {
		this.tag = tag;
	}
	
}
