package com.civilizer.web.view;

import java.util.*;
import java.io.File;
import java.io.Serializable;
import java.util.Collections;

import com.civilizer.domain.FileEntity;
import com.civilizer.utils.FsUtil;
import com.civilizer.utils.Pair;

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

//	public static boolean directoryEmpty(File dir) {
//		// [NOTE] An empty directory means it has no file and its all sub-directories have no file at all
//		return ! FileUtils.iterateFiles(dir, null, true).hasNext();
//	}
//	
//	public static void removeEmptyDirectories(String filesHomePath) {
//		Collection<File> dirs = FileUtils.listFilesAndDirs(
//				new File(filesHomePath),  // directory
//				FalseFileFilter.INSTANCE, // exclude all files
//				TrueFileFilter.INSTANCE   // include all sub directories
//		);
//		
//		for (File dir : dirs) {
//			if (dir.getPath().equals(filesHomePath)) {
//				// skip the root directory
//				continue;
//			}
//			if (dir.isDirectory() && directoryEmpty(dir)) {
//				FileUtils.deleteQuietly(dir);
//			}
//		}
//	}

	public Pair<File, String> createNewFolder(int parentFolderId, String name, String filesHomePath) {
	    final FilePathBean parentPathBean = getFilePathBean(parentFolderId);
	    final String parentPath = parentPathBean.getFullPath();
	    String path = FsUtil.concatPath(filesHomePath, parentPath, name);
	    final File file = new File(path);
	    path = FsUtil.normalizePath(path.substring(filesHomePath.length()));
	    
	    if (file.isFile()) {
	        return new Pair<File, String>(null, path);
	    }
	    if (file.isDirectory() == false) {
	        if (! file.mkdir()) {
	            return new Pair<File, String>(null, path);
	        }
	    }
	    
	    return new Pair<File, String>(file, path);
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
	    if (fileName != null)
	        this.fileName = fileName.intern();
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
		
		return parentPath.equals(FsUtil.SEP) ?
		        FsUtil.concatPath("", leafName) :
		            FsUtil.concatPath(parentPath, leafName);
	}

	public FilePathBean getFilePathBean(int index) {
		return filePathTree.getFilePathBeans().get(index);
	}

	public FilePathBean getFolderPathBean(int index) {
		return folderTree.getFilePathBeans().get(index);
	}

}
