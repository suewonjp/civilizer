package com.civilizer.web.view;

import java.util.*;
import java.io.Serializable;
import java.util.Collections;

import com.civilizer.domain.FileEntity;

@SuppressWarnings("serial")
public final class FileListBean implements Serializable {
	
	private List<FileEntity> fileEntities = Collections.emptyList();
	
	private FilePathTree filePathTree;

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

}
