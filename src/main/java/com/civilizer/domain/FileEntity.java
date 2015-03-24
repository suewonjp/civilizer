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
		return new File(prefix + File.separatorChar + fileName);
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
