package com.civilizer.web.view;

import java.util.*;
import java.io.File;
import java.io.Serializable;
import java.util.Collections;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

import com.civilizer.domain.FileEntity;

@SuppressWarnings("serial")
public final class FileListBean implements Serializable {
	
	private List<FileEntity> fileEntities = Collections.emptyList();

	private FilePathTree filePathTree;

	private FilePathTree folderTree;
	
	private String fileName;
	
	private int srcNodeId;

	private int dstNodeId;

	public List<FileEntity> getFileEntities() {
		return fileEntities;
	}

	public void setFileEntities(List<FileEntity> fileEntities) {
		this.fileEntities = fileEntities;
	}

	public FilePathTree getFilePathTree() {
		return filePathTree;
	}
	
	public FilePathTree getFolderTree() {
		return folderTree;
	}

	public static boolean directoryEmpty(File dir) {
		// [NOTE] An empty directory means it has no file and its all sub-directories have no file at all
		return ! FileUtils.iterateFiles(dir, null, true).hasNext();
	}
	
	public static void removeEmptyDirectories(String filesHomePath) {
		Collection<File> dirs = FileUtils.listFilesAndDirs(
				new File(filesHomePath),  // directory
				FalseFileFilter.INSTANCE, // exclude all files
				TrueFileFilter.INSTANCE   // include all sub directories
		);
		
		for (File dir : dirs) {
			if (dir.getPath().equals(filesHomePath)) {
				// skip the root directory
				continue;
			}
			if (dir.isDirectory() && directoryEmpty(dir)) {
				FileUtils.deleteQuietly(dir);
			}
		}
	}
	
	public File createNewFolder(int parentFolderId, String name, String filesHomePath) {
		final FilePathBean parentPathBean = getFilePathBean(parentFolderId);
		final String parentPath = parentPathBean.getFullPath();
		final String path = filesHomePath + File.separatorChar + parentPath + File.separatorChar + name;
		final File file = new File(path);
		
		if (file.isFile()) {
			return null;
		}
		if (file.isDirectory() == false) {
			if (! file.mkdir()) {
				return null;
			}
		}
		
		return file;
	}
	
	public void setFilePathTree(FilePathTree filePathTree) {
		filePathTree.populateNodes(fileEntities);
		this.filePathTree = filePathTree;
	}

	public void setFolderTree(FilePathTree folderTree) {
		this.folderTree = folderTree;
		folderTree.createRoot();
	}
	
	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public int getSrcNodeId() {
		return srcNodeId;
	}

	public void setSrcNodeId(int srcNodeId) {
		this.srcNodeId = srcNodeId;
	}

	public int getDstNodeId() {
		return dstNodeId;
	}

	public void setDstNodeId(int dstNodeId) {
		this.dstNodeId = dstNodeId;
	}

	public String getFullFilePath(int folderIndex, String leafName) {
		final FilePathBean folderPathBean = getFolderPathBean(folderIndex);
		final String parentPath = folderPathBean.getFullPath();
		
		return parentPath.equals(File.separator) ?
				File.separatorChar + leafName : parentPath + File.separatorChar + leafName;
	}

	public FilePathBean getFilePathBean(int index) {
		return filePathTree.getFilePathBeans().get(index);
	}

	public FilePathBean getFolderPathBean(int index) {
		return folderTree.getFilePathBeans().get(index);
	}

}
