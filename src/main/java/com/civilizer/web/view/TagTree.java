package com.civilizer.web.view;

import java.io.Serializable;
import java.util.*;

import org.primefaces.event.NodeCollapseEvent;
import org.primefaces.event.NodeExpandEvent;

import com.civilizer.domain.Tag;

@SuppressWarnings("serial")
public final class TagTree implements Serializable {
    
    private org.primefaces.model.TreeNode root;
    
    private List<Tag> tags;
    
    private List<TagBean> tagBeans;
    
    public void populateNodes(List<Tag> tags, List<TagBean> tagBeans) {
    	this.tags = tags;
    	this.tagBeans = tagBeans;
    	
    	// These tags have no parent
    	Collection<Tag> topParentTags = Tag.getTopParentTags(tags);
    	
    	root =  new org.primefaces.model.DefaultTreeNode(null, null);
    	
    	for (Tag t : topParentTags) {
    		final int index = Tag.getIndexOf(t.getId(), tags);
    		final org.primefaces.model.TreeNode parentNode = new org.primefaces.model.DefaultTreeNode(tagBeans.get(index), root);
    		if (t.getChildren().isEmpty() == false) {
    			// If it has children, just insert a dummy child node for users to expand the tree;
    			// The actual children will be inserted at runtime with Ajax
    			new org.primefaces.model.DefaultTreeNode(null, parentNode);
    		}
    	}
    }
    
    public void onNodeExpand(NodeExpandEvent event) {
    	final org.primefaces.model.TreeNode parentNode = event.getTreeNode();
    	final Object data = parentNode.getData();
    	if (data instanceof TagBean) {
    		parentNode.getChildren().clear();
    		TagBean tagBean = (TagBean) data;
    		Tag tag = tagBean.getTag();
    		Collection<Tag> children = tag.getChildren();
    		for (Tag c : children) {
    			final int index = Tag.getIndexOf(c.getId(), tags);
    			final org.primefaces.model.TreeNode cn =
    					new org.primefaces.model.DefaultTreeNode(tagBeans.get(index), parentNode);
    			if (c.getChildren().isEmpty() == false) {
    				// If it has children, just insert a dummy child node for users to expand the tree;
        			// The actual children will be inserted at runtime with Ajax
        			new org.primefaces.model.DefaultTreeNode(null, cn);
    			}
    		}
    	}
    }

    public void onNodeCollapse(NodeCollapseEvent event) {
    	final org.primefaces.model.TreeNode parentNode = event.getTreeNode();
    	final Object data = parentNode.getData();
    	if (data instanceof TagBean) {
    		parentNode.getChildren().clear();
    		new org.primefaces.model.DefaultTreeNode(null, parentNode);
    	}
    }

    public  org.primefaces.model.TreeNode getRoot() {
        return root;
    }

}
