package com.knowledgex.web.controller;

import java.util.Map;

//import javax.faces.component.html.HtmlInputText;
import javax.faces.event.ActionEvent;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
//import org.springframework.webflow.core.collection.ParameterMap;
import org.springframework.webflow.execution.RequestContext;

import com.knowledgex.dao.FragmentDao;
import com.knowledgex.dao.TagDao;
import com.knowledgex.domain.Fragment;
import com.knowledgex.web.view.FragmentBean;
import com.knowledgex.web.view.FragmentListBean;
import com.knowledgex.web.view.TagListBean;

@Controller
@Component("mainController")
public class MainController {
    
    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

	@Autowired
	private FragmentDao fragmentDao;

	@Autowired
	private TagDao tagDao;
	
	public FragmentListBean newFragmentListBean() {
        FragmentListBean fragmentListBean = new FragmentListBean();
        fragmentListBean.setFragments(fragmentDao.findAll());
        return fragmentListBean;
    }

	public FragmentBean newFragmentBean() {
	    logger.info("Creating new FragmentBean");
	    FragmentBean fragmentBean = new FragmentBean();
	    Fragment frg = new Fragment();
	    fragmentBean.setFragment(frg);
	    return fragmentBean;
	}
	
	public Fragment showFragment(RequestContext context) {
		Long id = context.getRequestScope().getLong("fragmentId");
		logger.info("Selected fragment id: {}", id);
		Fragment ret = fragmentDao.findById(id);
		return ret;
	}
	
	public void saveFragment(RequestContext context) {
	    FragmentBean fb = (FragmentBean) context.getViewScope().get("fragmentBean");
	    Fragment frg = fb.getFragment();
	    frg.setCreationDatetime(new DateTime());
	    frg.setUpdateDatetime(new DateTime());
        fragmentDao.save(frg);
        logger.info(fb.toString());
	}

	public void test(RequestContext context) {
	    Long value;
	    value = context.getRequestScope().getLong("fragmentId");
	    if (value != null) {
	        logger.info("test() called with parameter: {}", value);
	    }
	    value = context.getRequestScope().getLong("tagId");
	    if (value != null) {
	        logger.info("test() called with parameter: {}", value);
	    }
	}
	
	public void test2(RequestContext context) {
	    FragmentBean fb = (FragmentBean) context.getViewScope().get("fragmentBean");
	    logger.info("test2() called");
	    logger.info(fb.toString());
	}

	public void testActionListener(ActionEvent event) {
	    Map<String, Object> attributes = event.getComponent().getAttributes();
	    Object value;
	    value = attributes.get("fragmentId");
	    if (value != null) {
	        logger.info("testActionListener() called with value: {}", value.toString());
	    }
	    value = attributes.get("tagId");
	    if (value != null) {
	        logger.info("testActionListener() called with value: {}", value.toString());
	    }
	}
	
	public TagListBean newTagListBean() {
	    TagListBean tagListBean = new TagListBean();
	    tagListBean.setTags(tagDao.findAll());
        return tagListBean;
    }

}
