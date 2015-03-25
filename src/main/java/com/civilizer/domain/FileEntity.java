package com.civilizer.domain;

import static javax.persistence.GenerationType.IDENTITY;

import java.util.*;
import java.io.File;
import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.io.FileUtils;

@SuppressWarnings("serial")
@Entity
@Table(name = "FILE")
public class FileEntity implements Serializable {
	
	private Long id;
	
	// [RULE] should be a relative path and begin with slash (/)
	private String fileName = "";
	
	public FileEntity() {}
	
	public FileEntity(String name) {
		setFileName(name);
	}
	
	@Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "file_id")
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	@Column(name = "file_name", unique = true)
	public String getFileName() {
		return fileName;
	}
	
	public void setFileName(String name) {
		this.fileName = name.intern();
	}
	
	public File toFile(String prefix) {
		if (fileName.isEmpty()) {
			return null;
		}
		return new File(prefix + fileName);
	}
	
	public boolean persisted(String filesHome) {
		final File file = toFile(filesHome);
		if (file == null) {
			return false;
		}
		return file.isFile();
	}
	
	public static Collection<FileEntity> getFilesUnder(String directory) {
		final File dir = new File(directory);
		if (! dir.isDirectory()) {
			Collection<FileEntity> tmp = Collections.emptyList();
			return tmp;
		}
		directory = dir.getAbsolutePath();
		
		@SuppressWarnings("unchecked")
		Collection<File> files = FileUtils.listFiles(dir, null, true);
		final Collection<FileEntity> output = new ArrayList<>();
		final int beginIndex = directory.length(); // 
		for (File file : files) {
			// [NOTE] as a rule, we need to pass a relative path when creating a FileEntry
			output.add(new FileEntity(file.getAbsolutePath().substring(beginIndex)));
		}
		
		return output;
	}
	
	public void addToPathTree(TreeNode<Object> root) {
		// [NOTE] the root node should contain an empty string ("")
		final String[] names = fileName.split("/");
		
		TreeNode<Object> prev = root.findDescendantWith(names[0]);
		
		if (prev == null) {
			TreeNode<Object> child = new DefaultTreeNode<Object>(names[0]);
			root.addChild(child);
			prev = child;
		}
		
		if (names.length > 1) {
			// this file is located under some folders 
			for (int i=1; i<names.length-1; ++i) {
				TreeNode<Object> parent = root.findDescendantWith(names[i - 1]);
				prev = root.findDescendantWith(names[i]);
				if (prev == null) {
					// these nodes represent intermediate paths (folders) and contain string values
					TreeNode<Object> child = new DefaultTreeNode<Object>(names[i]);
					parent.addChild(child);
					prev = child;
				}
			}
			// this node represents a file name below any folder except the root folder;
			prev = prev.addChild(new DefaultTreeNode<Object>(names[names.length - 1]));
		}
		
		// [NOTE] the leaf node contains a FileEntry instance unlike other nodes which contain string values
		prev.addChild(new DefaultTreeNode<Object>(this));
	}
	
	@Override
    public int hashCode() {
        final int prime = 59;
        int result = prime + ((id == null) ? 0 : id.hashCode());
        return result;
    }
	
	@Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FileEntity other = (FileEntity) obj;
        return this.fileName.equals(other.fileName);
    }

    @Override
    public String toString() {
        return  getFileName();
    }
	
}
