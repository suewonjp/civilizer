package com.civilizer.web.view;

import java.io.Serializable;

import org.primefaces.model.UploadedFile;

@SuppressWarnings("serial")
public final class FileUploadBean  implements Serializable {

	private UploadedFile file;

	public UploadedFile getFile() {
		return file;
	}

	public void setFile(UploadedFile file) {
		this.file = file;
	}
	
	public String getFileName() {
		return file.getFileName();
	}
	
	public boolean saveFile(String path) {
		boolean result = false;
		
		try {
			this.file.write(path);
			result = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}

}
