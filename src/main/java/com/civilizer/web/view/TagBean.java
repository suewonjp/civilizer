package com.civilizer.web.view;

import java.io.Serializable;

import com.civilizer.domain.Tag;

@SuppressWarnings("serial")
public final class TagBean implements Serializable {

    private Tag tag;
    
    // number of fragments associated with this tag only
    private long fragmentCount = 0;

    // number of fragments associated with this tag and its descendants
    private long fragmentCountWtHrc = 0;

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

    public long getFragmentCountWtHrc() {
		return fragmentCountWtHrc;
	}

	public void setFragmentCountWtHrc(long count) {
		this.fragmentCountWtHrc = count;
	}
	
	public String typeName() {
	    return Tag.isTrivialTag(tag.getId()) ? "trivial-tag" : "special-tag";
	}
    
}
