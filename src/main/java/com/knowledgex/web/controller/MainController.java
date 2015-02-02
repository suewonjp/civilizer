package com.knowledgex.web.controller;

import java.util.*;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import com.knowledgex.dao.FragmentDao;
import com.knowledgex.dao.TagDao;
import com.knowledgex.domain.Fragment;
import com.knowledgex.domain.Tag;
import com.knowledgex.web.view.*;

@Controller
@Component("mainController")
public class MainController {
	
	private static final int MAX_FRAGMENT_PANELS = 3;
    
    private static final Logger logger = LoggerFactory.getLogger(MainController.class);
    
	@Autowired
	private FragmentDao fragmentDao;

	@Autowired
	private TagDao tagDao;
	
	private Tag trashTag = null;
	
	private Tag getTrashTag() {
		if (null != trashTag) {
			return trashTag;
		}
		trashTag = tagDao.findById(Tag.TRASH_TAG_ID);
		return trashTag;
	}
	
	public List<FragmentListBean> newFragmentListBeans() {
		List<FragmentListBean> output =  new ArrayList<FragmentListBean>(MAX_FRAGMENT_PANELS);
		for (int i=0; i<MAX_FRAGMENT_PANELS; ++i) {
			output.add(null);
		}
		return output;
	}

	public FragmentListBean newFragmentListBean(FragmentListBean existingFlb, PanelContextBean pcb) {
        FragmentListBean flb = existingFlb;
        if (null == flb) {
			flb = new FragmentListBean();
		}
        
        if (null == pcb) {
        	pcb = new PanelContextBean();
        }
        
        final long tagId = pcb.getTagId();
        final int curPage = pcb.isLastPage() ? (pcb.getCurPage() - 1) : Math.max(0, pcb.getCurPage());
        final int count = pcb.getItemsPerPage();
        final int first = curPage * count;
        
        Collection<Fragment> fragments = null;
        if (tagId == PanelContextBean.TAG_ID_FOR_ALL_VALID_TAGS) {
        	// Fetch all the fragments
        	fragments = fragmentDao.findSome(first, count + 1);
        }
        else {
        	// Fetch the fragments with the specified tag
        	fragments = tagDao.findFragments(tagId, first, count + 1);
        }
        
        final boolean isLastPage = fragments.size() <= count;
        
        final boolean trashTag = Tag.isTrashTag(tagId);
        if (!trashTag) {
        	// Exclude fragments that have '#trash' tag
        	List<Long> trashTagId = new ArrayList<Long>();
        	trashTagId.add(0L);
        	fragments = Fragment.applyExclusiveTagFilter(fragments, trashTagId);
        }

        flb.setPanelContextBean(new PanelContextBean(tagId, curPage, count, isLastPage, trashTag));
//        ViewUtil.addMessage("pcb", flb.getPanelContextBean());
        
        // [NOTE] The content of fragments should be immutable form here!
        
        List<FragmentBean> fragmentBeans = new ArrayList<FragmentBean>();
        for (Fragment f : fragments) {
        	FragmentBean fb = new FragmentBean();
        	fb.setFragment(f);
        	String tagNames = Tag.getTagNamesFrom(f.getTags());
        	fb.setTagNames(tagNames);
        	fragmentBeans.add(fb);
        }
        flb.setFragmentBeans(fragmentBeans);
        
        return flb;
    }

	public FragmentBean newFragmentBean() {
	    FragmentBean fragmentBean = new FragmentBean();
	    Fragment frg = new Fragment();
	    fragmentBean.setFragment(frg);
	    return fragmentBean;
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

	public PanelContextBean newPanelContextBean(long tagId, int curPage) {
		return new PanelContextBean(tagId, curPage);
	}

	public ContextBean newContextBean() {
	    return new ContextBean();
	}
	
	public void trashFragment(Long fragmentId) {
		Fragment frg = fragmentDao.findById(fragmentId);
		frg.addTag(getTrashTag());
		fragmentDao.save(frg);
		ViewUtil.addMessage("Successful", "Fragment #" + frg.getId() + " has been trashed", null);
	}

	public void deleteFragment(Long fragmentId) {
		Fragment frg = fragmentDao.findById(fragmentId);
		fragmentDao.delete(frg);
		ViewUtil.addMessage("Successful", "Fragment #" + frg.getId() + " has been deleted", null);
	}
	
	public void saveFragment(FragmentBean fb, TagListBean tagListBean) {
	    String tagNames = fb.getTagNames();
	    Collection<Tag> tags = saveTags(tagListBean, tagNames);
	    
	    Fragment frg = fb.getFragment();
	    frg.setTags(tags);
	    DateTime dt = new DateTime();
	    if (frg.getCreationDatetime() == null) {
	    	frg.setCreationDatetime(dt);
	    }
	    frg.setUpdateDatetime(dt);
        fragmentDao.save(frg);
        ViewUtil.addMessage("Successful", "Fragment #" + frg.getId() + " has been saved", null);
	}
	
	private Collection<Tag> saveTags(TagListBean tagListBean, String tagNames) {
		Collection<Tag> existingTags = tagListBean.getTags();
		Collection<String> names = Tag.getTagNameCollectionFrom(tagNames);
		List<Tag> output = new ArrayList<Tag>();
		for (String name : names) {
			Tag t = Tag.getTagFromName(name, existingTags);
			boolean newTag = false;
			if (null == t) {
				t = new Tag(name);
				newTag = true;
			}
			DateTime dt = new DateTime();
		    if (t.getCreationDatetime() == null) {
		    	t.setCreationDatetime(dt);
		    }
		    t.setUpdateDatetime(dt);
			tagDao.save(t);
			if (newTag) {
				ViewUtil.addMessage("Successful", "Tag \"" + t.getTagName() + "\" has been saved", null);
			}
			output.add(t);
		}
		return output;
	}

	public FragmentBean inspectFragment(Integer index, FragmentListBean flb) {
		FragmentBean fb = flb.getFragmentBeanAt(index);
	    logger.info("inspectFragment() called");
	    return fb;
	}


}
