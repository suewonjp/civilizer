package com.knowledgex.web.view;

import java.io.Serializable;
import java.util.*;

import org.primefaces.model.TreeNode;
import org.primefaces.model.DefaultTreeNode;

import com.knowledgex.domain.Tag;

@SuppressWarnings("serial")
public class TagTree implements Serializable {
    
    private TreeNode root = null;
    
    public TagTree() {
    }

    public void populateNodes(Collection<Tag> tags) {
        root = new DefaultTreeNode("Root", null);
        for (Tag t : tags) {
            new DefaultTreeNode(t, root);
        }
    }

    public TreeNode getRoot() {
        return root;
    }

}
