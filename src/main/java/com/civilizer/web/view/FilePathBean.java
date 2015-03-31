package com.civilizer.web.view;

import java.io.Serializable;

import com.civilizer.domain.FileEntity;

@SuppressWarnings("serial")
public class FilePathBean implements Serializable {
	
	private Object entity;
	
	private String fullPath = "";
	
	private int id = -1;
	
//	private boolean creator;
	
	private boolean broken;
	
	private boolean traansient; // not a typo. *transient* is a reserved keyword
	
	
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
	
//	public boolean isCreator() {
//		return creator;
//	}
//
//	public void setCreator(boolean creator) {
//		this.creator = creator;
//	}

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
		return isTraansient() || entity instanceof String;
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
		String postfix = "";
		if (entity instanceof FileEntity) {
			if (isTraansient()) {
				postfix = "fb-transient";
			}
			else if (isBroken()) {
				postfix = "fa-question-circle fb-broken";
			}
			return "fb-file fa-file-o " + postfix;
		}
		else {
//			if (isCreator()) {
//				postfix = "fb-creator";
//			}
			return "fb-dir fa-folder-open " + postfix;
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
