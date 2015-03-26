package com.civilizer.web.view;

import java.util.*;
import java.io.Serializable;

import com.civilizer.domain.DefaultTreeNode;
import com.civilizer.domain.FileEntity;
import com.civilizer.domain.TreeNode;

@SuppressWarnings("serial")
public class FilePathTree implements Serializable {
	
	private org.primefaces.model.TreeNode root; // = new org.primefaces.model.DefaultTreeNode("Root", null);
	
	public void populateNodes(List<FileEntity> fileEntities) {
		TreeNode<Object> pathTree = new DefaultTreeNode<Object>("");
		
		for (FileEntity fe : fileEntities) {
			fe.addToPathTree(pathTree);
		}
		
		Map<Object, org.primefaces.model.TreeNode> mapPath2TreeNode = new HashMap<>();
		
		Object[] paths = pathTree.toArray(TreeNode.TraverseOrder.BREATH_FIRST);
		
		for (Object o : paths) {
			@SuppressWarnings("unchecked")
			TreeNode<Object> path = (TreeNode<Object>) o;
			FilePathBean filePathBean = new FilePathBean();
			filePathBean.setEntity(path.getData());
			mapPath2TreeNode.put(o, new org.primefaces.model.DefaultTreeNode(filePathBean));
		}
		for (Object o : paths) {
			@SuppressWarnings("unchecked")
			TreeNode<Object> path = (TreeNode<Object>) o;
			TreeNode<Object> parent = path.getParent();
			org.primefaces.model.TreeNode tn = mapPath2TreeNode.get(path);
			org.primefaces.model.TreeNode tnp = mapPath2TreeNode.get(parent);
			if (tnp != null) {
				tn.setParent(tnp);
				tnp.getChildren().add(tn);
				tnp.setExpanded(true);
			}
		}
		
		root = mapPath2TreeNode.get(pathTree);
		root.setExpanded(true);
	}
	
	public org.primefaces.model.TreeNode getRoot() {
		return root;
	}

}
