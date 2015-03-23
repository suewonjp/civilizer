package com.civilizer.web.view;

import java.io.Serializable;

import org.primefaces.model.UploadedFile;

@SuppressWarnings("serial")
public class FileUploadBean  implements Serializable {

	private UploadedFile file;

	public void processFile() {
		System.out.println("****** hello");
	}

	public UploadedFile getFile() {
		return file;
	}

	public void setFile(UploadedFile file) {
		System.out.println("++++++ " + file.getFileName());
		this.file = file;
	}

}
