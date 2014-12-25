package com.knowledgex.web.view;

import java.io.Serializable;
import java.util.List;

import com.knowledgex.domain.Tag;

@SuppressWarnings("serial")
public class TagListBean implements Serializable {

    private List<Tag> tags;

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }
    
}
