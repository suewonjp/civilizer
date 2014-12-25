package com.knowledgex.web.view;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.knowledgex.domain.Fragment;

@SuppressWarnings("serial")
public class FragmentBean implements Serializable {
    
    private static final Logger logger = LoggerFactory.getLogger(FragmentBean.class);
    
    private Fragment fragment;
    
    private String tags;

    public Fragment getFragment() {
        return fragment;
    }

    public void setFragment(Fragment fragment) {
        this.fragment = fragment;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }
    
    public void clear() {
        logger.info("clear() called");
        
        tags = "";
        if (fragment != null) {
            fragment.setId(null);
            fragment.setTitle("");
            fragment.setContent("");
        }
    }
    
    public String toString() {
        return "{tags:" + tags
                + "}, {" + fragment.toString()
                + "}"
                ;
    }
}
