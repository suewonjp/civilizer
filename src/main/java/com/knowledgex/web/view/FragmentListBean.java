package com.knowledgex.web.view;

import java.io.Serializable;
import java.util.List;

import com.knowledgex.domain.Fragment;

@SuppressWarnings("serial")
public class FragmentListBean implements Serializable {
    
    private List<Fragment> fragments;
    
    public List<Fragment> getFragments() {
        return fragments;
    }

    public void setFragments(List<Fragment> fragments) {
        this.fragments = fragments;
    }

}
