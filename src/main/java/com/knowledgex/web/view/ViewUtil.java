package com.knowledgex.web.view;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

public class ViewUtil {
	
	public static void addMessage(String title, String content, FacesMessage.Severity severity) {
		if (null == severity) {
			severity = FacesMessage.SEVERITY_INFO;
		}
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, title, content));
	}

	public static void addMessage(String objName, Object obj) {
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(objName, obj.toString()));
	}
	
}
