package com.knowledgex.web.view;

import java.io.Serializable;
import java.util.*;

import com.knowledgex.domain.Tag;

@SuppressWarnings("serial")
public class TagListBean implements Serializable {

    private Collection<Tag> tags;

    public Collection<Tag> getTags() {
        return tags;
    }

    public void setTags(Collection<Tag> tags) {
        this.tags = tags;
    }
    
}
