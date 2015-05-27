package com.civilizer.web.view;

import java.io.Serializable;

import com.civilizer.domain.Fragment;

@SuppressWarnings("serial")
public final class FragmentBean implements Serializable {
    
    private Fragment fragment;
    
    private String title;

    private String content;
    
    private String concatenatedTagNames;
    
    private boolean checked = false;
    
    public boolean isChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
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
