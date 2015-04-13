package com.civilizer.web.view;

import java.io.Serializable;
import java.util.*;

import com.civilizer.domain.Tag;

@SuppressWarnings("serial")
public final class TagListBean implements Serializable {

    private List<Tag> tags = Collections.emptyList();

    private List<TagBean> tagBeans = Collections.emptyList();

    private TagTree tagTree;
    
    private TagBean tagToEdit;

    private List<Tag> parentTags = Collections.emptyList();

    private List<Tag> childTags = Collections.emptyList();
    
    private long newParentTagId;

    private long newChildTagId;

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

	public TagBean getTagToEdit() {
		return tagToEdit;
	}

	public void setTagToEdit(long tagId) {
		final int index = indexOf(tagId);
		this.tagToEdit = tagBeans.get(index);
		final Tag tag = tagToEdit.getTag();
		if (tag.getChildren().isEmpty() == false) {
			childTags = new ArrayList<>(tag.getChildren());
		}
	}
    
    public List<Tag> getParentTags() {
		return parentTags;
	}

	public void setParentTags(List<Tag> parentTags) {
		this.parentTags = parentTags;
	}

	public List<Tag> getChildTags() {
		return childTags;
	}

	public void setChildTags(List<Tag> childTags) {
		this.childTags = childTags;
	}

	public long getNewParentTagId() {
		return newParentTagId;
	}

	public void setNewParentTagId(long newParentTagId) {
		if (Tag.isTrivialTag(newParentTagId)) {
			final int index = indexOf(newParentTagId);
			if (parentTags.equals(Collections.emptyList())) {
				parentTags = new ArrayList<>();
			}
			parentTags.add(tags.get(index));
		}
	}

	public long getNewChildTagId() {
		return newChildTagId;
	}

	public void setNewChildTagId(long newChildTagId) {
		if (Tag.isTrivialTag(newChildTagId)) {
			final int index = indexOf(newChildTagId);
			if (childTags.equals(Collections.emptyList())) {
				childTags = new ArrayList<>();
			}
			childTags.add(tags.get(index));
		}
	}
	
	public List<Tag> listOfSelectableParents() {
		final List<Tag> output = new LinkedList<>(tags);
		for (Tag tag : parentTags) {
			output.remove(tag);
		}
		if (tagToEdit != null) {
			output.remove(tagToEdit.getTag());
		}
		return output;
	}

	public List<Tag> listOfSelectableChildren() {
		final List<Tag> output = new LinkedList<>(tags);
		for (Tag tag : childTags) {
			output.remove(tag);
		}
		if (tagToEdit != null) {
			output.remove(tagToEdit.getTag());
		}
		return output;
	}
	
//	public void prepareToPersistTagToEdit() {
//		final Tag tgt = tagToEdit.getTag();
//		for (Tag tag : parentTags) {
//			tag.addChild(tgt);
//		}
//		tgt.setChildren(new HashSet<Tag>(childTags));
//	}

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
