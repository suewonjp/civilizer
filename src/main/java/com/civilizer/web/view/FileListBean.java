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

	public List<FileEntity> getTransientEntities() {
		return transientEntities;
	}

	public void setTransientEntities(List<FileEntity> transientEntities) {
		this.transientEntities = transientEntities;
	}

	public FilePathTree getFilePathTree() {
		return filePathTree;
	}
	
	private boolean transientEntryGetsPersisted(FileEntity tgt) {
		for (FileEntity fe : fileEntities) {
			if (tgt.isChildOf(fe)) {
				return true;
			}
		}
		return false;
	}

	public void setFilePathTree(FilePathTree filePathTree, List<FileEntity> transientEntites) {
		Iterator<FileEntity> itr = transientEntites.iterator();
		while (itr.hasNext()) {
			FileEntity fe = itr.next();
			if (transientEntryGetsPersisted(fe)) {
				itr.remove();
			}
		}
		
		filePathTree.populateNodes(fileEntities, transientEntites);
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
	
	public String getFilePath(int index) {
		return filePathTree.getFilePathBeans().get(index).getFullPath();
	}

}
