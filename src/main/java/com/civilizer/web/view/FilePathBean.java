package com.civilizer.web.view;

import java.io.Serializable;

import com.civilizer.domain.FileEntity;

@SuppressWarnings("serial")
public class FilePathBean implements Serializable {
	
	private Object entity;
	
	private String fullPath;
	
	private int id = -1;
	
	private boolean broken;
	
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
	
	public boolean isBroken() {
		return broken;
	}

	public void setBroken(boolean broken) {
		this.broken = broken;
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
			if (isBroken()) {
				return "red";
			}
			return "white";
		}
		else {
			return "gold";
		}
	}
	
	public String getName() {
		if (entity instanceof FileEntity) {
			FileEntity fe = (FileEntity) entity;
			String[] tmp = fe.getFileName().split("/");
			return tmp[tmp.length - 1];
		}
		else {
			return entity.toString();
		}
	}
	
	public String toString() {
		if (entity instanceof FileEntity) {
			FileEntity fe = (FileEntity) entity;
			String[] tmp = fe.getFileName().split("/");
			String postfix = "";
			if (isBroken()) {
				// [TODO] replace hard-coded Unicode literals with some understandable symbols;
				postfix = ' ' + Character.toString((char) 0xf059);
			}
			return Character.toString((char) 0xf016) + ' ' + tmp[tmp.length - 1] + postfix;
		}
		else {
			return Character.toString((char) 0xf07c) + ' ' + entity.toString();
		}
	}

}
