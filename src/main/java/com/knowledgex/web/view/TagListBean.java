package com.knowledgex.web.view;

import java.io.Serializable;
import java.util.*;

import com.knowledgex.domain.Tag;

@SuppressWarnings("serial")
public final class TagListBean implements Serializable {

    private List<Tag> tags;

    private List<Long> fragmentCountList;
    
    private TagTree tagTree = null;

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public List<Long> getFragmentCountList() {
        return fragmentCountList;
    }

    public void setFragmentCountList(List<Long> fragmentCountList) {
        this.fragmentCountList = fragmentCountList;
    }

    public TagTree getTagTree() {
        return tagTree;
    }

    public void setTagTree(TagTree tagTree) {
        try {
            tagTree.populateNodes(tags);
        }
        catch (NullPointerException e) {
            e.printStackTrace();
        }
        
        this.tagTree = tagTree;
    }
    
}
