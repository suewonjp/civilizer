package com.civilizer.web.view;

import java.util.*;
import java.io.File;
import java.io.Serializable;

import com.civilizer.domain.DefaultTreeNode;
import com.civilizer.domain.FileEntity;
import com.civilizer.domain.TreeNode;

@SuppressWarnings("serial")
public class FilePathTree implements Serializable {
	
	private org.primefaces.model.TreeNode root;
	
	private List<FilePathBean> filePathBeans;
	
	private static String getFullPathOf(TreeNode<Object> path) {
		// [NOTE] the returned path will start with a file separator (e.g. / or \)
		final Object entity = path.getData();
		if (entity instanceof FileEntity) {
			return ((FileEntity) entity).toString().replace('/', File.separatorChar);
		}
		else {
			TreeNode<Object> tmp = path;
			String fullPath = File.separatorChar + tmp.getData().toString();
			while ((tmp = tmp.getParent()) != null) {
				fullPath = tmp.getData().toString() + fullPath;
			}
			return fullPath;
		}
	}
	
	public void populateNodes(List<FileEntity> fileEntities, List<FileEntity> trancientEntities) {
		TreeNode<Object> pathTree = new DefaultTreeNode<Object>("");
		
		for (FileEntity fe : fileEntities) {
			fe.addToPathTree(pathTree);
		}

		for (FileEntity fe : trancientEntities) {
			fe.addToPathTree(pathTree);
		}
		
		Map<Object, org.primefaces.model.TreeNode> mapPath2TreeNode = new HashMap<>();
		
		Object[] paths = pathTree.toArray(TreeNode.TraverseOrder.BREATH_FIRST);
		
		filePathBeans = new ArrayList<FilePathBean>();
		
		for (int i=0; i<paths.length; ++i) {
			final Object o = paths[i];
			@SuppressWarnings("unchecked")
			TreeNode<Object> path = (TreeNode<Object>) o;
			FilePathBean filePathBean = new FilePathBean(i);
			filePathBeans.add(filePathBean);
			filePathBean.setEntity(path.getData());
			final String fp = getFullPathOf(path);
			filePathBean.setFullPath(fp);
			mapPath2TreeNode.put(o, new org.primefaces.model.DefaultTreeNode(filePathBean));
		}
		
		for (int i=0; i<paths.length; ++i) {
			final Object o = paths[i];
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
	
	public List<FilePathBean> getFilePathBeans() {
		return filePathBeans;
	}

}
