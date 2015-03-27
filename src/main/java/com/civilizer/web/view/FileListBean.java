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
	}
	
	public int getSelectedNodeId() {
		return selectedNodeId;
	}

	public void setSelectedNodeId(int selectedNodeId) {
		this.selectedNodeId = selectedNodeId;
	}

	public void detectBrokenLinks() {
		final String uuploadedFilesHomePath = System.getProperty(AppOptions.UPLOADED_FILES_HOME);
		for (FileEntity fe : fileEntities) {
			final File f = fe.toFile(uuploadedFilesHomePath);
		}
	}

}
