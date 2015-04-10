package com.civilizer.web.view;

import java.io.Serializable;
import java.util.*;

import com.civilizer.domain.Tag;

@SuppressWarnings("serial")
public final class TagBean implements Serializable {

//    @SuppressWarnings("unused")
//	private final Logger logger = LoggerFactory.getLogger(TagBean.class);
    
    private Tag tag;
    
    private List<Tag> parentTags;
    
    private long fragmentCount = 0;

//	public void clear() {
//        if (tag != null) {
//        	tag.setId(null);
//        	tag.setTagName("");
//        }
//    }

	public Tag getTag() {
		return tag;
	}

	public void setTag(Tag tag) {
		this.tag = tag;
	}
	
	public List<Tag> getParentTags() {
		return parentTags;
	}

	public void setParentTags(List<Tag> parentTags) {
		this.parentTags = parentTags;
	}

	public long getFragmentCount() {
		return fragmentCount;
	}

	public void setFragmentCount(long fragmentCount) {
		this.fragmentCount = fragmentCount;
	}
    
}
