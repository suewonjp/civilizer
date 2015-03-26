package com.civilizer.web.view;

import java.io.Serializable;
import java.util.*;

import com.civilizer.domain.Tag;

@SuppressWarnings("serial")
public final class TagListBean implements Serializable {

    private List<Tag> tags = Collections.emptyList();

    private List<TagBean> tagBeans = Collections.emptyList();

    private TagTree tagTree;

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }
    
    public List<TagBean> getTagBeans() {
		return tagBeans;
	}

	public void setTagBeans(List<TagBean> tagBeans) {
		this.tagBeans = tagBeans;
	}

	public int indexOf(long tagId) {
    	final long tc = tags.size();
    	for (int i = 0; i < tc; i++) {
			if (tags.get(i).getId() == tagId) {
				return i;
			}
		}
    	return -1;
    }

    public TagTree getTagTree() {
        return tagTree;
    }

    public void setTagTree(TagTree tagTree) {
        tagTree.populateNodes(tags, tagBeans);
        this.tagTree = tagTree;
    }
    
    public List<String> suggest(String input) {
    	final List<String> results = new ArrayList<String>();
    	
    	if (input == null || input.isEmpty()) {
    		// the drop down button has been pressed
    		for (Tag t : tags) {
    			results.add(t.getTagName());
    		}
    		return results;
    	}
    	
		List<String> tmp = Tag.getTagNameCollectionFrom(input);
		if (tmp.isEmpty()) {
			return Collections.emptyList();
		}
		
		final String typed = tmp.get(tmp.size() - 1);
		for (Tag t : tags) {
			final String tagName = t.getTagName();
			if (tagName.startsWith(typed)) {
				results.add(tagName);
			}
    	}
		
    	return results;
    }
    
}
