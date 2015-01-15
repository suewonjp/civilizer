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
//        populateNodes4Test();
    }
    
//    private void populateNodes4Test() {
//        root = new DefaultTreeNode("Root", null);
//        TreeNode node0 = new DefaultTreeNode("Node 0", root);
//        TreeNode node1 = new DefaultTreeNode("Node 1", root);
//        TreeNode node2 = new DefaultTreeNode("Node 2", root);
//        TreeNode node00 = new DefaultTreeNode("Node 0.0", node0);
//        TreeNode node01 = new DefaultTreeNode("Node 0.1", node0);
//        TreeNode node10 = new DefaultTreeNode("Node 1.0", node1);
//        TreeNode node11 = new DefaultTreeNode("Node 1.1", node1);
//        TreeNode node000 = new DefaultTreeNode("Node 0.0.0", node00);
//        TreeNode node001 = new DefaultTreeNode("Node 0.0.1", node00);
//        TreeNode node010 = new DefaultTreeNode("Node 0.1.0", node01);
//        TreeNode node100 = new DefaultTreeNode("Node 1.0.0", node10);
//    }

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
