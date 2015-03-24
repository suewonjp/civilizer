package com.civilizer.domain;

import static javax.persistence.GenerationType.IDENTITY;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@SuppressWarnings("serial")
@Entity
@Table(name = "FILE")
public class FileEntity implements Serializable {
	
	private Long id;
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
