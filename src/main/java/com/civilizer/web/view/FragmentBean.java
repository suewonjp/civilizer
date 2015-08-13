package com.civilizer.web.view;

import java.io.Serializable;
import java.util.*;

import javax.persistence.Transient;

import com.civilizer.domain.Fragment;

@SuppressWarnings("serial")
public final class FragmentBean implements Serializable {
    
    private Fragment fragment;
    
    private String title = "";

    private String content = "";
    
    private String concatenatedTagNames = "";
    
    private List<Fragment> relatedOnes = Collections.emptyList();
    
    @Transient
    private FragmentSelectionBean fragmentSelectionBean;
    
    public boolean isChecked() {
        if (fragmentSelectionBean != null) {
            return fragmentSelectionBean.contains(fragment.getId());
        }
        return false;
	}

	public void setChecked(boolean checked) {
	    if (fragmentSelectionBean != null) {
	        if (checked)
	            fragmentSelectionBean.addFragment(fragment);
	        else
	            fragmentSelectionBean.removeFragment(fragment);
	    }
	}

    public Fragment getFragment() {
        return fragment;
    }

    public void setFragment(Fragment fragment) {
        this.fragment = fragment;
    }

    public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
	    if (title != null)
	        this.title = title.intern();
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
	    if (content != null)
	        this.content = content.intern();
	}

	public String getConcatenatedTagNames() {
		return concatenatedTagNames;
	}

	public void setConcatenatedTagNames(String concatenatedTagNames) {
	    if (concatenatedTagNames != null)
	        this.concatenatedTagNames = concatenatedTagNames.intern();
	}
	
	public void setFragmentSelectionBean(FragmentSelectionBean fsb) {
	    fragmentSelectionBean = fsb;
	}
	
    public List<Fragment> getRelatedOnes() {
        return relatedOnes;
    }

    public void setRelatedOnes(List<Fragment> relatedOnes) {
        this.relatedOnes = relatedOnes;
    }
	
	public void clear() {
        setConcatenatedTagNames("");
        if (fragment != null) {
            fragment.setId(null);
            fragment.setTitle("");
            fragment.setContent("");
        }
    }
    
    public String toString() {
        return "{tags:" + getConcatenatedTagNames()
                + "}, {" + fragment.toString()
                + "}"
                ;
    }
    
}
