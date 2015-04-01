package com.civilizer.web.view;

import java.util.*;
import java.io.File;
import java.io.Serializable;
import java.util.Collections;

import org.apache.commons.io.FileUtils;

import com.civilizer.config.AppOptions;
import com.civilizer.domain.FileEntity;

@SuppressWarnings("serial")
public final class FileListBean implements Serializable {
	
	private List<FileEntity> fileEntities = Collections.emptyList();

	private List<FileEntity> transientEntities = Collections.emptyList();
	
	private FilePathTree filePathTree;
	
	private String fileName;
	
	private int selectedNodeId;

	public List<FileEntity> getFileEntities() {
		return fileEntities;
	}

	public void setFileEntities(List<FileEntity> fileEntities) {
		this.fileEntities = fileEntities;
	}

	public FilePathTree getFilePathTree() {
		return filePathTree;
	}
	
	public List<FileEntity> getTransientEntities() {
		return transientEntities;
	}
	
	public void removeTransientEntity(FilePathBean filePathBean) {
		transientEntities.remove(filePathBean.getEntity());
	}
	
	public void removeMatchedTransientEntities(FilePathBean filePathBean) {
		final String parentPath = filePathBean.getFullPath();
		Iterator<FileEntity> itr = transientEntities.iterator();
		while (itr.hasNext()) {
			FileEntity fe = itr.next();
			if (fe.isChildOf(parentPath)) {
				// the parent folder is supposed to be removed;
				// so remove this transient entity too
				itr.remove();
			}
		}
	}

	public void renameMatchedTransientEntities(FilePathBean filePathBean, String newName) {
		final String parentPath = filePathBean.getFullPath();
		Iterator<FileEntity> itr = transientEntities.iterator();
		while (itr.hasNext()) {
			FileEntity fe = itr.next();
			if (fe.isChildOf(parentPath)) {
				// the parent folder is supposed to be renamed;
				// so apply it to this transient entity
				fe.replaceNameSegment(parentPath, newName);
			}
		}
	}
	
	private static boolean directoryEmpty(File dir) {
		// [NOTE] An empty directory means it has no file and its all sub-directories have no file at all
		return ! FileUtils.iterateFiles(dir, null, true).hasNext();
	}
	
	public boolean createNewTransientFolder(int parentFolderId, String name, String filesHomePath) {
		final FilePathBean parentPathBean = getFilePathBean(parentFolderId);
		final String parentPath = parentPathBean.getFullPath();
		final String path = (parentPath.equals(File.separator) ? "" : parentPath)
				+ File.separatorChar + name;
		final FileEntity fe = new FileEntity(path);
		
		if (transientEntities.contains(fe)) {
			return false;
		}
		
		final File file = fe.toFile(filesHomePath);
		if (file.isFile()) {
			return false;
		}
		if (file.isDirectory()) {
			if (! directoryEmpty(file)) {
				// the folder already exists, but is not empty;
				return false;
			}
		}
		
		if (transientEntities.isEmpty()) {
			transientEntities = new ArrayList<FileEntity>();
		}
		
		transientEntities.add(fe);
		return true;
	}
	
	private boolean transientEntityPersisted(FileEntity transientEntity) {
		for (FileEntity persistedFile : fileEntities) {
			if (persistedFile.isChildOf(transientEntity.getFileName())) {
				// this directory has some persisted files under it
				// meaning that the directory exists on the file system; it is not transient any more
				return true;
			}
		}
		return false;
	}

	public void setFilePathTree(FilePathTree filePathTree, List<FileEntity> transientEntities) {
		// Clear all transient entities that have been persisted;
		Iterator<FileEntity> itr = transientEntities.iterator();
		while (itr.hasNext()) {
			FileEntity fe = itr.next();
			if (transientEntityPersisted(fe)) {
				itr.remove();
			}
		}
		
		this.transientEntities = transientEntities;
		
		filePathTree.populateNodes(fileEntities, transientEntities);
		
		this.filePathTree = filePathTree;
		
		detectBrokenLinks();
	}
	
	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public int getSelectedNodeId() {
		return selectedNodeId;
	}

	public void setSelectedNodeId(int selectedNodeId) {
		this.selectedNodeId = selectedNodeId;
	}

	public void detectBrokenLinks() {
		final String uploadedFilesHomePath = System.getProperty(AppOptions.UPLOADED_FILES_HOME);
		final List<FilePathBean> filePathBeans = filePathTree.getFilePathBeans();
		for (FilePathBean fpb : filePathBeans) {
			if (fpb.isFolder() || fpb.getEntity() instanceof FileEntity == false) {
				continue;
			}
			
			final File f = ((FileEntity) fpb.getEntity()).toFile(uploadedFilesHomePath);
			if (f.isFile() == false) {
				// this file is managed by the data layer;
				// however, it does not exist on the file system for whatever reasons;
				// so let the user aware of it
				fpb.setBroken(true);
			}
		}
	}
	
	public String getFilePath(int index, String leafName) {
		final FilePathBean filePathBean = getFilePathBean(index);
		final String intermediatePath = filePathBean.getFullPath();
		
		return intermediatePath.equals(File.separator) ?
				File.separatorChar + leafName : intermediatePath + File.separatorChar + leafName;
	}

	public FilePathBean getFilePathBean(int index) {
		return filePathTree.getFilePathBeans().get(index);
	}

}
