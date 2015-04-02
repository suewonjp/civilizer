package com.civilizer.web.view;

import java.util.*;
import java.io.File;
import java.io.Serializable;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

import com.civilizer.config.AppOptions;
import com.civilizer.domain.DefaultTreeNode;
import com.civilizer.domain.FileEntity;
import com.civilizer.domain.Pair;
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
			String fullPath = tmp.getData().toString();
			while ((tmp = tmp.getParent()) != null) {
				final String seg = tmp.getData().toString();
				if (seg.isEmpty()) {
					continue;
				}
				fullPath = tmp.getData().toString() + File.separatorChar + fullPath;
			}
			return File.separatorChar + fullPath;
		}
	}
	
	public static void addToPathTree(TreeNode<FilePathBean> root, String path) {
		final String[] names = path.split("/");
		String name = "";
		String fullPath = name;
		TreeNode<FilePathBean> parent = root;
		FilePathBean data = parent.getData();
		
		for (int i=1; i<names.length; ++i) {
			parent = root.findDescendantWith(data);
			name = names[i];
			fullPath += File.separatorChar + name;
			data = new FilePathBean(name, fullPath);
			if (parent != null && root.findDescendantWith(data) == null) {
				parent.addChild(new DefaultTreeNode<FilePathBean>(data));
			}
		}
	}
	
	public static boolean addFileEntityToPathTree(TreeNode<FilePathBean> root, FileEntity fileEntity) {
		Pair<String, String> splitPath = fileEntity.splitName();
		TreeNode<FilePathBean> parent = root.findDescendantWith(new FilePathBean(splitPath.getFirst()));
		if (parent == null) {
			return false;
		}
		else {
			parent.addChild(new DefaultTreeNode<FilePathBean>(new FilePathBean(splitPath.getSecond(), fileEntity.getFileName())));
			return true;
		}
	}
	
	public void populateNodes(List<FileEntity> fileEntities) {
		final String filesHomePath = System.getProperty(AppOptions.UPLOADED_FILES_HOME);
		Collection<File> dirs = FileUtils.listFilesAndDirs(
				new File(filesHomePath),  // directory
				FalseFileFilter.INSTANCE, // exclude all files
				TrueFileFilter.INSTANCE   // include all sub directories
		);
		
		final TreeNode<FilePathBean> pathTree = new DefaultTreeNode<>(new FilePathBean("", ""));
		
		for (File file : dirs) {
			String path = file.toString().replace(filesHomePath, "");
			FilePathTree.addToPathTree(pathTree, path);
		}
		
		for (FileEntity fileEntity : fileEntities) {
			FilePathTree.addFileEntityToPathTree(pathTree, fileEntity);
		}
		
		Map<Object, org.primefaces.model.TreeNode> mapPath2TreeNode = new HashMap<>();
		filePathBeans = new ArrayList<FilePathBean>();
		Object[] paths = pathTree.toArray(TreeNode.TraverseOrder.BREATH_FIRST);
		
		for (int i=0; i<paths.length; ++i) {
			final Object o = paths[i];
			@SuppressWarnings("unchecked")
			TreeNode<FilePathBean> path = (TreeNode<FilePathBean>) o;

			final FilePathBean filePathBean = path.getData();
			filePathBean.setId(i);
			filePathBean.check(filesHomePath);
			filePathBeans.add(filePathBean);
			
			mapPath2TreeNode.put(path, new org.primefaces.model.DefaultTreeNode(filePathBean));
		}
		
		for (int i=0; i<paths.length; ++i) {
			final Object o = paths[i];
			@SuppressWarnings("unchecked")
			TreeNode<FilePathBean> path = (TreeNode<FilePathBean>) o;
			TreeNode<FilePathBean> parent = path.getParent();
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
			
			final FilePathBean filePathBean = new FilePathBean(i);
			filePathBeans.add(filePathBean);
			final Object data = path.getData();
			filePathBean.setEntity(data);
			final String fp = getFullPathOf(path);
			filePathBean.setFullPath(fp);
			
			// detect a transient folder;
			// it is a virtual folder not persisted on the file system yet;
			// so it will disappear after the user's session expires
			if (data instanceof FileEntity) {
				filePathBean.setTraansient(trancientEntities.contains((FileEntity) data));
			}
			else {
				if (trancientEntities.contains(new FileEntity(fp))) {
					// In this case, a transient folder has a sub transient folder;
					filePathBean.setTraansient(true);
				}
			}
			
			final org.primefaces.model.TreeNode viewNode = new org.primefaces.model.DefaultTreeNode(filePathBean);			
			mapPath2TreeNode.put(o, viewNode);
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
