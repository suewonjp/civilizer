package com.civilizer.web.view;

import java.io.Serializable;

import com.civilizer.domain.Tag;

@SuppressWarnings("serial")
public final class TagBean implements Serializable {

    private Tag tag;
    
    private long fragmentCount = 0;

	public void clear() {
        if (tag != null) {
        	tag.setId(null);
        	tag.setTagName("");
        }
    }

	public Tag getTag() {
		return tag;
	}

	public void setTag(Tag tag) {
		this.tag = tag;
	}
	
    public long getFragmentCount() {
		return fragmentCount;
	}

	public void setFragmentCount(long fragmentCount) {
		this.fragmentCount = fragmentCount;
	}
	
	public String typeName() {
	    return Tag.isTrivialTag(tag.getId()) ? "trivial-tag" : "special-tag";
	}
    
}
