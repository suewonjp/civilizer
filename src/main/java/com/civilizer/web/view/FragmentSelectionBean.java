package com.civilizer.web.view;

import java.io.Serializable;
import java.util.*;

@SuppressWarnings("serial")
public final class FragmentSelectionBean implements Serializable {
    
    private List<Long> fragmentIds = Collections.emptyList();

    public List<Long> getFragmentIds() {
        return fragmentIds;
    }

    public void setFragmentIds(List<Long> fragmentIds) {
        this.fragmentIds = fragmentIds;
    }
    
    public void addFragmentId(long id) {
        if (fragmentIds.equals(Collections.emptyList())) {
            fragmentIds = new ArrayList<>();
        }
        fragmentIds.add(id);
    }
    
    public void removeFragmentId(long id) {
        fragmentIds.remove(id);
    }
    
    public boolean contains(long id) {
        return fragmentIds.contains(id);
    }
    
    public static List<FragmentBean> selectFragmentBeans(Collection<FragmentBean> srcList, Collection<Long> selectionIds) {
        List<FragmentBean> output = new ArrayList<>();
        for (long id : selectionIds) {
            for (FragmentBean fb : srcList) {
                if (fb.getFragment().getId() == id) {
                    output.add(fb);
                }
            }
        }
        return output;
    }
    
    public List<FragmentBean> selectFragmentBeans(Collection<FragmentBean> srcList) {
        return selectFragmentBeans(srcList, fragmentIds);
    }

}
