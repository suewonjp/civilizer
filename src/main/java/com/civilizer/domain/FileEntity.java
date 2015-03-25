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
	
	public void addToNameTree(TreeNode<String> tree) {
		final String[] names = fileName.split("/");
		if (! tree.contains(names[0])) {
			tree.addChildWith(names[0]);
		}
		for (int i=1; i<names.length; ++i) {
			TreeNode<String> parent = tree.findDescendantWith(names[i - 1]);
			if (! tree.contains(names[i])) {
				TreeNode<String> child = new DefaultTreeNode<>(names[i]);
				parent.addChild(child);
			}
		}
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
