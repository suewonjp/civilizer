package com.knowledgex.web.view;

import java.io.Serializable;
import java.util.*;

@SuppressWarnings("serial")
public class FragmentListBean implements Serializable {
    
    private List<FragmentBean> fragmentBeans;
    
    private PaginatorBean paginatorBean;
    
    private long curTagId = -1;
    
    public Collection<FragmentBean> getFragmentBeans() {
    	return fragmentBeans;
    }
    
    public void setFragmentBeans(List<FragmentBean> fragmentBeans) {
    	this.fragmentBeans = fragmentBeans;
    }
    
    public FragmentBean getFragmentBeanAt(int index) {
    	return fragmentBeans.get(index);
    }

	public PaginatorBean getPaginatorBean() {
		return paginatorBean;
	}

	public void setPaginatorBean(PaginatorBean paginatorBean) {
		this.paginatorBean = paginatorBean;
	}

	public long getCurTagId() {
		return curTagId;
	}

	public void setCurTagId(long curTagId) {
		this.curTagId = curTagId;
	}

}
