package com.knowledgex.web.view;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.knowledgex.domain.Fragment;

@SuppressWarnings("serial")
public class FragmentBean implements Serializable {
    
    private static final Logger logger = LoggerFactory.getLogger(FragmentBean.class);
    
    private Fragment fragment;
    
    private String tagNames;

    public Fragment getFragment() {
        return fragment;
    }

    public void setFragment(Fragment fragment) {
        this.fragment = fragment;
    }

    public String getTagNames() {
        return tagNames;
    }

    public void setTagNames(String tags) {
        this.tagNames = tags;
    }
    
    public void clear() {
        logger.info("clear() called");
        
        tagNames = "";
        if (fragment != null) {
            fragment.setId(null);
            fragment.setTitle("");
            fragment.setContent("");
        }
    }
    
    public String toString() {
        return "{tags:" + tagNames
                + "}, {" + fragment.toString()
                + "}"
                ;
    }
}
