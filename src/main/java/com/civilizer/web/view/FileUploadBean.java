package com.civilizer.web.view;

import java.io.File;
import java.io.Serializable;

import org.apache.commons.io.FileUtils;
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
			FileUtils.writeByteArrayToFile(new File(path), this.file.getContents());
			result = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}

}
