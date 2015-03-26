package com.civilizer.web.view;

import java.io.Serializable;

import com.civilizer.domain.FileEntity;

@SuppressWarnings("serial")
public class FilePathBean implements Serializable {
	
	private Object entity;

	public Object getEntity() {
		return entity;
	}

	public void setEntity(Object entity) {
		this.entity = entity;
	}
	
	public boolean isFolder() {
		return entity instanceof String;
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
