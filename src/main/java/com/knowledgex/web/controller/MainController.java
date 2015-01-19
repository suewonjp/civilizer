package com.knowledgex.web.controller;

import java.util.*;

//import javax.faces.event.ActionEvent;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.webflow.execution.RequestContext;

import com.knowledgex.dao.FragmentDao;
import com.knowledgex.dao.TagDao;
import com.knowledgex.domain.Fragment;
import com.knowledgex.domain.Tag;
import com.knowledgex.web.view.*;

@Controller
@Component("mainController")
public class MainController {
    
    private static final Logger logger = LoggerFactory.getLogger(MainController.class);
    
    private static Tag trashTag = null;
    
    private boolean displayingTrash = false;
    
	@Autowired
	private FragmentDao fragmentDao;

	@Autowired
	private TagDao tagDao;
	
	public boolean isDisplayingTrash() {
		return displayingTrash;
	}

	public void setDisplayingTrash(boolean displayingTrash) {
		this.displayingTrash = displayingTrash;
	}

	private Tag getTrashTag() {
		if (null != trashTag) {
			return trashTag;
		}
		trashTag = tagDao.findById(Tag.TRASH_TAG_ID);
		return trashTag;
	}

	public FragmentListBean newFragmentListBean(RequestContext context) {
		Long tagId = context.getFlowScope().getLong("tagId");
		
        FragmentListBean fragmentListBean = new FragmentListBean();
        Collection<Fragment> fragments = null;
        if (null == tagId) {
        	// Fetch all the fragments
        	fragments = fragmentDao.findAll();
        }
        else {
        	// Fetch the fragments with the specified tag
        	fragments = tagDao.findFragments(tagId);
        }
        
        boolean trashTag = Tag.isTrashTag(tagId);
        displayingTrash = trashTag;
        if (false == trashTag) {
        	// Exclude fragments that have '#trash' tag
        	List<Long> trashTagId = new ArrayList<Long>();
        	trashTagId.add(0L);
        	fragments = Fragment.applyExclusiveTagFilter(fragments, trashTagId);
        }
        
        List<FragmentBean> fragmentBeans = new ArrayList<FragmentBean>();
        for (Fragment f : fragments) {
        	FragmentBean fb = new FragmentBean();
        	fb.setFragment(f);
        	String tagNames = Tag.getTagNamesFrom(f.getTags());
        	fb.setTagNames(tagNames);
        	fragmentBeans.add(fb);
        }
        fragmentListBean.setFragmentBeans(fragmentBeans);
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
	
	public void trashFragment(RequestContext context) {
		Long fragmentId = context.getFlowScope().getLong("fragmentId");
		if (null != fragmentId) {
			Fragment frg = fragmentDao.findById(fragmentId);
			frg.addTag(getTrashTag());
			fragmentDao.save(frg);
		}
	}

	public void deleteFragment(RequestContext context) {
		Long fragmentId = context.getFlowScope().getLong("fragmentId");
		if (null != fragmentId) {
			Fragment frg = fragmentDao.findById(fragmentId);
			fragmentDao.delete(frg);
		}
	}
	
	public void saveFragment(RequestContext context) {
	    FragmentBean fb = (FragmentBean) context.getViewScope().get("fragmentBean");
	    
	    String tagNames = fb.getTagNames();
	    Collection<Tag> tags = saveTags(context, tagNames);
	    
	    Fragment frg = fb.getFragment();
	    frg.setTags(tags);
	    DateTime dt = new DateTime();
	    if (frg.getCreationDatetime() == null) {
	    	frg.setCreationDatetime(dt);
	    }
	    frg.setUpdateDatetime(dt);
        fragmentDao.save(frg);
        logger.info(fb.toString());
	}
	
	private Collection<Tag> saveTags(RequestContext context, String tagNames) {
		TagListBean tagListBean = (TagListBean) context.getViewScope().get("tagListBean");
		Collection<Tag> existingTags = tagListBean.getTags();
		Collection<String> names = Tag.getTagNameCollectionFrom(tagNames);
		List<Tag> output = new ArrayList<Tag>();
		for (String name : names) {
			Tag t = Tag.getTagFromName(name, existingTags);
			if (t == null) {
				t = new Tag(name);
			}
			DateTime dt = new DateTime();
		    if (t.getCreationDatetime() == null) {
		    	t.setCreationDatetime(dt);
		    }
		    t.setUpdateDatetime(dt);
			tagDao.save(t);
			logger.info(t.toString());
			output.add(t);
		}
		return output;
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

//	public void testActionListener(ActionEvent event) {
//	    Map<String, Object> attributes = event.getComponent().getAttributes();
//	    Object value;
//	    value = attributes.get("fragmentId");
//	    if (value != null) {
//	        logger.info("testActionListener() called with value: {}", value.toString());
//	    }
//	    value = attributes.get("tagId");
//	    if (value != null) {
//	        logger.info("testActionListener() called with value: {}", value.toString());
//	    }
//	}
	
	public TagListBean newTagListBean() {
	    TagListBean tagListBean = new TagListBean();
	    tagListBean.setTags(tagDao.findAllWithChildren());
	    TagTree tagTree = newTagTree();
	    tagListBean.setTagTree(tagTree);
        return tagListBean;
    }
	
	private TagTree newTagTree() {
	    TagTree tagTree = new TagTree();
	    return tagTree;
	}

}
