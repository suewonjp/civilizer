package com.knowledgex.web.view;

import java.io.Serializable;

@SuppressWarnings("serial")
public class ContextBean implements Serializable {

    private boolean fragmentDeletable = false;

    public boolean isFragmentDeletable() {
        return fragmentDeletable;
    }

    public void setFragmentDeletable(boolean fragmentDeletable) {
        this.fragmentDeletable = fragmentDeletable;
    }
    
}
