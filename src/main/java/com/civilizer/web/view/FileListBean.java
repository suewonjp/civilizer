package com.civilizer.web.view;

import java.util.*;
import java.io.File;
import java.io.Serializable;
import java.util.Collections;

import com.civilizer.config.AppOptions;
import com.civilizer.domain.FileEntity;

@SuppressWarnings("serial")
public final class FileListBean implements Serializable {
	
	private List<FileEntity> fileEntities = Collections.emptyList();
	
	private FilePathTree filePathTree;
	
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

	public void setFilePathTree(FilePathTree filePathTree) {
		filePathTree.populateNodes(fileEntities);
		this.filePathTree = filePathTree;
		detectBrokenLinks();
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
			if (fpb.isFolder()) {
				continue;
			}
			if (fpb.getEntity() instanceof FileEntity == false) {
				continue;
			}
			final File f = ((FileEntity) fpb.getEntity()).toFile(uploadedFilesHomePath);
			if (f.isFile() == false) {
				fpb.setBroken(true);
			}
		}
	}
	
	public String getFilePath(String fileName) {
		final String intermediatePath = (selectedNodeId > -1) ?
				filePathTree.getFilePathBeans().get(selectedNodeId).getFullPath() : File.separator;
		
		return intermediatePath.equals(File.separator) ?
				File.separatorChar + fileName : intermediatePath + File.separatorChar + fileName;
	}

}
