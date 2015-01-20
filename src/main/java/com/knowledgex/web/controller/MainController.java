package com.knowledgex.web.controller;

import java.util.*;

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
    
	@Autowired
	private FragmentDao fragmentDao;

	@Autowired
	private TagDao tagDao;

	private Tag getTrashTag() {
		if (null != trashTag) {
			return trashTag;
		}
		trashTag = tagDao.findById(Tag.TRASH_TAG_ID);
		return trashTag;
	}

	public FragmentListBean newFragmentListBean(RequestContext context) {
		Long tagId = context.getRequestScope().getLong("tagId");
		
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
        fragmentListBean.setDisplayingTrash(trashTag);
        logger.info("newFragmentListBean() called");
        return fragmentListBean;
    }

	public FragmentBean newFragmentBean() {
	    FragmentBean fragmentBean = new FragmentBean();
	    Fragment frg = new Fragment();
	    fragmentBean.setFragment(frg);
	    return fragmentBean;
	}
	
	public void trashFragment(RequestContext context) {
		Long fragmentId = context.getRequestScope().getLong("fragmentId");
		if (null != fragmentId) {
			Fragment frg = fragmentDao.findById(fragmentId);
			frg.addTag(getTrashTag());
			fragmentDao.save(frg);
		}
	}

	public void deleteFragment(RequestContext context) {
		Long fragmentId = context.getRequestScope().getLong("fragmentId");
		if (null != fragmentId) {
			Fragment frg = fragmentDao.findById(fragmentId);
			fragmentDao.delete(frg);
		}
	}
	
	public void saveFragment(RequestContext context) {
	    FragmentBean fb = (FragmentBean) context.getFlowScope().get("userEditedFragmentBean");
	    
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
		TagListBean tagListBean = (TagListBean) context.getFlowScope().get("tagListBean");
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

	public void test(
			RequestContext context
			, FragmentListBean flb
			, TagListBean tlb
			, FragmentBean fb
			) {
	    Long value;
	    value = context.getRequestScope().getLong("fragmentId");
	    if (value != null) {
	        logger.info("test() called with parameter: {}", value);
	    }
	    value = context.getRequestScope().getLong("tagId");
	    if (value != null) {
	        logger.info("test() called with parameter: {}", value);
	    }
	    if (null != flb) {
	    	logger.info("test() called with FragmentListBean");
	    }
	    if (null != tlb) {
	    	logger.info("test() called with TagListBean");
	    }
	    if (null != fb) {
	    	logger.info("test() called with FragmentBean");
	    }
	}
	
	public FragmentBean inspectFragment(RequestContext context) {
		Integer index = context.getRequestScope().getInteger("fragmentLoopIndex");
		FragmentListBean flb = (FragmentListBean) context.getFlowScope().get("fragmentListBean");
		FragmentBean fb = flb.getFragmentBeanAt(index);
	    logger.info("inspectFragment() called");
	    return fb;
	}

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
