package com.civilizer.web.view;

import java.util.Locale;
import java.util.ResourceBundle;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

public final class ViewUtil {
	
	private static final String MESSAGE_RESOURCE_BASE_NAME = "i18n.MessageResources";
	
	public static void addMessage(String title, String content, FacesMessage.Severity severity) {
		if (severity == null) {
			severity = FacesMessage.SEVERITY_INFO;
		}
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, title, content));
	}

	public static void addMessage(String clientId, String title, String content, FacesMessage.Severity severity) {
		if (severity == null) {
			severity = FacesMessage.SEVERITY_INFO;
		}
		FacesContext.getCurrentInstance().addMessage(clientId, new FacesMessage(severity, title, content));
	}

	public static void addMessage(String objName, Object obj) {
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(objName, obj.toString()));
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T findBean(String beanName) {
	    FacesContext context = FacesContext.getCurrentInstance();
	    return (T) context.getApplication().evaluateExpressionGet(context, "#{" + beanName + "}", Object.class);
	}
	
	public static String getResourceBundleString(String key) {
		final UserProfileBean userProfileBean = ViewUtil.findBean("userProfileBean");
    	final Locale locale = userProfileBean.getLocale();
    	final ResourceBundle bundle = ResourceBundle.getBundle(MESSAGE_RESOURCE_BASE_NAME, locale);
    	return bundle.getString(key);
	}
}
