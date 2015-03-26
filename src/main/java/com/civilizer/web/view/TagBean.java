package com.civilizer.web.view;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.civilizer.domain.Tag;

@SuppressWarnings("serial")
public final class TagBean implements Serializable {

    @SuppressWarnings("unused")
	private final Logger logger = LoggerFactory.getLogger(TagBean.class);
    
    private Tag tag;
    
    private long fragmentCount = 0;

	public Tag getTag() {
		return tag;
	}

	public void setTag(Tag tag) {
		this.tag = tag;
	}
	
	public void clear() {
        if (tag != null) {
        	tag.setId(null);
        	tag.setTagName("");
        }
    }

	public long getFragmentCount() {
		return fragmentCount;
	}

	public void setFragmentCount(long fragmentCount) {
		this.fragmentCount = fragmentCount;
	}
    
}
