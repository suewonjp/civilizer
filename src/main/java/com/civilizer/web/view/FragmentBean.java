package com.civilizer.web.view;

import java.io.Serializable;

import javax.persistence.Transient;

import com.civilizer.domain.Fragment;

@SuppressWarnings("serial")
public final class FragmentBean implements Serializable {
    
    private Fragment fragment;
    
    private String title;

    private String content;
    
    private String concatenatedTagNames;
    
    @Transient
    private FragmentSelectionBean fragmentSelectionBean;
    
    public boolean isChecked() {
        if (fragmentSelectionBean != null) {
            return fragmentSelectionBean.contains(fragment.getId());
        }
        return false;
	}

	public void setChecked(boolean checked) {
	    final long id = fragment.getId();
	    if (fragmentSelectionBean != null) {
	        if (checked)
	            fragmentSelectionBean.addFragmentId(id);
	        else
	            fragmentSelectionBean.removeFragmentId(id);
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
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getConcatenatedTagNames() {
		return concatenatedTagNames;
	}

	public void setConcatenatedTagNames(String concatenatedTagNames) {
		this.concatenatedTagNames = concatenatedTagNames;
	}
	
	public void setFragmentSelectionBean(FragmentSelectionBean fsb) {
	    fragmentSelectionBean = fsb;
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
