package com.knowledgex.web.view;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.knowledgex.domain.Tag;

@SuppressWarnings("serial")
public class SpecialTagBean implements Serializable {
	
	@SuppressWarnings("unused")
	private final Logger logger = LoggerFactory.getLogger(SpecialTagBean.class);
	
	private List<FragmentBean> fragmentBeans = Collections.emptyList();
	
	private Tag tag = null;
	
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
