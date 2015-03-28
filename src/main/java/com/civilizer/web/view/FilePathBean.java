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
	
	public String getCssClassName() {
		if (entity instanceof FileEntity) {
			String postfix = "";
			if (isBroken()) {
				postfix = "fa-question-circle fb-broken";
			}
			return "fb-file fa-file-o " + postfix;
		}
		else {
			return "fb-dir fa-folder-open ";
		}
	}
	
	public String toString() {
		if (entity instanceof FileEntity) {
			FileEntity fe = (FileEntity) entity;
			String[] tmp = fe.getFileName().split("/");
			return tmp[tmp.length - 1];
		}
		else {
			return entity.toString();
		}
	}

}
