package com.civilizer.web.view;

import java.io.Serializable;
import java.util.*;

import com.civilizer.domain.Fragment;

@SuppressWarnings("serial")
public final class FragmentSelectionBean implements Serializable {
    
    private List<Long> fragmentIds = Collections.emptyList();

    private List<String> fragmentTitles = Collections.emptyList();
    
    public List<Long> getFragmentIds() {
        return fragmentIds;
    }
    
    public void setFragmentIds(List<Long> fragmentIds) {
        this.fragmentIds = fragmentIds;
    }
    
    public List<String> getFragmentTitles() {
        return fragmentTitles;
    }

    public void setFragmentTitles(List<String> fragmentTitles) {
        this.fragmentTitles = fragmentTitles;
    }
    
    public void addFragment(Fragment fragment) {
        if (fragmentIds.equals(Collections.emptyList())) {
            fragmentIds = new ArrayList<>();
            fragmentTitles = new ArrayList<>();
        }
        fragmentIds.add(fragment.getId());
        fragmentTitles.add(fragment.getTitle());
    }
    
    public void removeFragment(Fragment fragment) {
        fragmentIds.remove(fragment.getId());
        fragmentTitles.remove(fragment.getTitle());
    }
    
    public boolean contains(long id) {
        return fragmentIds.contains(id);
    }

}
