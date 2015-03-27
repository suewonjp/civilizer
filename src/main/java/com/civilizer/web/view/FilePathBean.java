package com.civilizer.web.view;

import java.io.Serializable;

import com.civilizer.domain.FileEntity;

@SuppressWarnings("serial")
public class FilePathBean implements Serializable {
	
	private Object entity;
	
	private String fullPath;
	
	private int id = -1;
	
	public FilePathBean() {}

	public FilePathBean(int id) { this.id = id; }

	public Object getEntity() {
		return entity;
	}

	public void setEntity(Object entity) {
		this.entity = entity;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public String getFullPath() {
		return fullPath;
	}

	public void setFullPath(String fp) {
		this.fullPath = fp;
	}

	public boolean isFolder() {
		return entity instanceof String;
	}
		
	public String toColorNotation() {
		if (entity instanceof FileEntity) {
			return "white";
		}
		else {
			return "gold";
		}
	}
	
	public String toString() {
		if (entity instanceof FileEntity) {
			FileEntity fe = (FileEntity) entity;
			String[] tmp = fe.getFileName().split("/");
			return Character.toString((char) 0xf016) + ' ' + tmp[tmp.length - 1];
		}
		else {
			return Character.toString((char) 0xf07c) + ' ' + entity.toString();
		}
	}

}
