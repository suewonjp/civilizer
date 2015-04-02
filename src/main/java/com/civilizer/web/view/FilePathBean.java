package com.civilizer.web.view;

import java.io.File;
import java.io.Serializable;

import com.civilizer.domain.FileEntity;

@SuppressWarnings("serial")
public class FilePathBean implements Serializable {
	
	private Object entity;
	
	private String name = "";
	
	private String fullPath = "";
	
	private int id = -1;
	
	private boolean folder;
	
	private boolean broken;
	
	private boolean traansient; // not a typo. *transient* is a reserved keyword
	
	public FilePathBean() {}

	public FilePathBean(int id) { this.id = id; }

	public FilePathBean(String fullPath) { 
		this.fullPath = fullPath;
	}

	public FilePathBean(String name, String fullPath) { 
		this.name = name;
		this.fullPath = fullPath;
	}

	public Object getEntity() {
		return entity;
	}

	public void setEntity(Object entity) {
		this.entity = entity;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public boolean isBroken() {
		return broken;
	}

	public void setBroken(boolean broken) {
		this.broken = broken;
	}

	public boolean isTraansient() {
		return traansient;
	}

	public void setTraansient(boolean traansient) {
		this.traansient = traansient;
	}

	public String getFullPath() {
		return fullPath;
	}

	public void setFullPath(String fp) {
		this.fullPath = fp;
	}

	public boolean isFolder() {
		return folder || isTraansient() || entity instanceof String;
	}
	
	public String getCssClassName() {
		if (isTraansient()) {
			return "fb-transient fa-folder-open";
		}
		else if (isFolder()) {
			return "fb-dir fa-folder-open";
		}
		else {
			return "fb-file fa-file-o "
				+ (isBroken() ? "fa-question-circle fb-broken" : "");
		}
	}
	
	public void check(String filesHomePath) {
		final File f = new File(filesHomePath + File.separatorChar + fullPath);
		if (f.isDirectory()) {
			folder = true;
		}
		else if (f.isFile() == false) {
			broken = true;
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FilePathBean other = (FilePathBean) obj;
        return this.fullPath.equals(other.fullPath);
	}
	
	@Override
	public String toString() {
		if (entity instanceof FileEntity) {
			FileEntity fe = (FileEntity) entity;
			String[] tmp = fe.getFileName().split("/");
			return tmp[tmp.length - 1];
		}
		else {
			if (entity == null) {
				return fullPath;
			}
			return entity.toString();
		}
	}

}
