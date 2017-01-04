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
        if (fragmentIds != null)
            this.fragmentIds = fragmentIds;
    }
    
    public List<String> getFragmentTitles() {
        return fragmentTitles;
    }

    public void setFragmentTitles(List<String> fragmentTitles) {
        if (fragmentTitles != null)
            this.fragmentTitles = fragmentTitles;
    }
    
    public void addFragment(Fragment fragment) {
        if (fragment == null)
            return;
        if (fragmentIds.equals(Collections.emptyList())) {
            fragmentIds = new ArrayList<>();
            fragmentTitles = new ArrayList<>();
        }
        final Long id = fragment.getId();
        if (! fragmentIds.contains(id)) {
            fragmentIds.add(fragment.getId());
            fragmentTitles.add(fragment.getTitle());
        }
    }
            
    public void removeFragment(Fragment fragment) {
        if (fragment == null)
            return;
        fragmentIds.remove(fragment.getId());
        fragmentTitles.remove(fragment.getTitle());
        if (fragmentIds.isEmpty()) {
            clear();
        }
    }
    
    public void clear() {
        fragmentIds = Collections.emptyList();
        fragmentTitles = Collections.emptyList();
    }
    
    public boolean contains(Long id) {
        return fragmentIds.contains(id);
    }

}
