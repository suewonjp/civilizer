package com.civilizer.web.view;

import java.util.List;
import java.util.ArrayList;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.RequestContextHolder;

import com.civilizer.config.AppOptions;

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
//		final UserProfileBean userProfileBean = ViewUtil.findBean("userProfileBean");
//    	final Locale locale = userProfileBean.getLocale();
    	final ResourceBundle bundle = ResourceBundle.getBundle(MESSAGE_RESOURCE_BASE_NAME, new Locale(System.getProperty(AppOptions.LOCALE)));
    	return bundle.getString(key);
	}
    
    public static boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth.getPrincipal();
        return principal instanceof UserDetails;
    }

    @SuppressWarnings("unchecked")
    public static <T> void putAttributeToFlowScope(String key, T value) {
        RequestContext requestContext = RequestContextHolder.getRequestContext();
        MutableAttributeMap<Object> attrMap = requestContext.getFlowScope();
        ArrayList<T> attr = (ArrayList<T>) attrMap.get(key);
        if (attr == null) {
            attrMap.put(key, new ArrayList<T>());
            attr = (ArrayList<T>) attrMap.get(key);
        }
        attr.add(value);
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> getAttributesFromFlowScope(String key) {
        RequestContext requestContext = RequestContextHolder.getRequestContext();
        MutableAttributeMap<Object> attrMap = requestContext.getFlowScope();
        return (List<T>) attrMap.get(key);
    }

    public static void removeAttributesFromFlowScope(String key) {
        RequestContext requestContext = RequestContextHolder.getRequestContext();
        MutableAttributeMap<Object> attrMap = requestContext.getFlowScope();
        attrMap.remove(key);
    }
}
