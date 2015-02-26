package com.knowledgex.web.view;

import java.io.Serializable;
import java.util.*;

import org.primefaces.model.TreeNode;
import org.primefaces.model.DefaultTreeNode;

import com.knowledgex.domain.Tag;

@SuppressWarnings("serial")
public final class TagTree implements Serializable {
    
    private TreeNode root = new DefaultTreeNode("Root", null);
    
    public void populateNodes(List<Tag> tags, List<TagBean> tagBeans) {
    	// These tags have no parent
    	Collection<Tag> topParentTags = Tag.getTopParentTags(tags);
    	
    	Map<Long, TreeNode> mapTagId2TreeNode = new HashMap<Long, TreeNode>();
    	
    	for (Tag t : topParentTags) {
    		final int index = Tag.getIndexOf(t.getId(), tags);
    		mapTagId2TreeNode.put(t.getId(), new DefaultTreeNode(tagBeans.get(index), root));
    	}
    	
    	for (Tag t : tags) {
    		Collection<Tag> children = t.getChildren();
    		for (Tag c : children) {
    			TreeNode parentTreeNode = mapTagId2TreeNode.get(t.getId());
    			final int index = Tag.getIndexOf(c.getId(), tags);
    			mapTagId2TreeNode.put(c.getId(), new DefaultTreeNode(tagBeans.get(index), parentTreeNode));
    			parentTreeNode.setExpanded(true);
    		}
    	}
    }

    public TreeNode getRoot() {
        return root;
    }

}
