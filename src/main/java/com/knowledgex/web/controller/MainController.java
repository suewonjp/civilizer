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
import com.knowledgex.domain.FragmentOrder;
import com.knowledgex.domain.Tag;
import com.knowledgex.web.view.*;

@Controller
@Component("mainController")
public final class MainController {
	
	private static final int MAX_FRAGMENT_PANELS = 3;
    
    private static final Logger logger = LoggerFactory.getLogger(MainController.class);
    
	@Autowired
	private FragmentDao fragmentDao;

	@Autowired
	private TagDao tagDao;
	
	private Tag trashTag = null;
	
	private Tag getTrashTag() {
		if (trashTag != null) {
			return trashTag;
		}
		trashTag = tagDao.findById(Tag.TRASH_TAG_ID);
		return trashTag;
	}
	
	public List<FragmentListBean> newFragmentListBeans() {
		final List<FragmentListBean> output =  new ArrayList<FragmentListBean>(MAX_FRAGMENT_PANELS);
		for (int i=0; i<MAX_FRAGMENT_PANELS; ++i) {
			output.add(null);
		}
		return output;
	}

	public FragmentListBean newFragmentListBean(FragmentListBean existingFlb, PanelContextBean pcb) {
        final FragmentListBean flb =
        		(existingFlb == null) ? new FragmentListBean() : existingFlb;
        
        if (pcb == null) {
        	pcb = new PanelContextBean();
        }
        
        final long tagId = pcb.getTagId();
        final int curPage = pcb.isLastPage() ? (pcb.getCurPage() - 1) : Math.max(0, pcb.getCurPage());
        final int count = pcb.getItemsPerPage();
        final int first = curPage * count;
        
        List<Fragment> fragments = null;
        if (tagId == PanelContextBean.TAG_ID_FOR_ALL_VALID_TAGS) {
        	// Fetch the fragments regardless of tags
//        	fragments = fragmentDao.findSome(first, count + 1);
        	fragments = fragmentDao.findSomeNonTrashed(first, count + 1);
        }
        else if (tagId == Tag.TRASH_TAG_ID) {
        	// Fetch the trashed fragments
        	fragments = tagDao.findFragments(tagId, first, count + 1);
        	Fragment.sort(fragments, FragmentOrder.UPDATE_DATETIME, false);
        }
        else {
        	// Fetch the fragments with the specified tag (non-trashed)
//        	fragments = tagDao.findFragments(tagId, first, count + 1);
        	fragments = tagDao.findNonTrashedFragments(tagId, first, count + 1);
        	Fragment.sort(fragments, FragmentOrder.UPDATE_DATETIME, false);
        }
        
        final boolean isLastPage = fragments.size() <= count;
        final boolean givenTagIsTrashTag = Tag.isTrashTag(tagId);
//        if (!givenTagIsTrashTag) {
//        	// Exclude fragments that have '#trash' tag
//        	final List<Long> trashTagId = new ArrayList<Long>();
//        	trashTagId.add(0L);
//        	Fragment.applyExclusiveTagFilter(fragments, trashTagId);
//        }

        flb.setPanelContextBean(new PanelContextBean(tagId, curPage, count, isLastPage, givenTagIsTrashTag));
//        ViewUtil.addMessage("pcb", flb.getPanelContextBean());
        
        // [NOTE] The content of fragments should be IMMUTABLE form here!
        
        final List<FragmentBean> fragmentBeans = new ArrayList<FragmentBean>();
        final int c = Math.min(count, fragments.size());
//        for (Fragment f : fragments) {
       	for (int i=0; i<c; ++i) {
       		Fragment f = fragments.get(i);
        	FragmentBean fb = new FragmentBean();
        	fb.setFragment(f);
        	final String tagNames = Tag.getTagNamesFrom(f.getTags());
        	fb.setTagNames(tagNames);
        	fragmentBeans.add(fb);
        }
        flb.setFragmentBeans(fragmentBeans);
        
        return flb;
    }

	public FragmentBean newFragmentBean() {
	    final FragmentBean fragmentBean = new FragmentBean();
	    final Fragment frg = new Fragment();
	    fragmentBean.setFragment(frg);
	    return fragmentBean;
	}
	
	public TagListBean newTagListBean() {
		final TagListBean tagListBean = new TagListBean();
	    tagListBean.setTags(tagDao.findAllWithChildren());
	    final TagTree tagTree = newTagTree();
	    tagListBean.setTagTree(tagTree);
        return tagListBean;
    }
	
	private TagTree newTagTree() {
		final TagTree tagTree = new TagTree();
	    return tagTree;
	}

	public PanelContextBean newPanelContextBean(long tagId, int curPage) {
		return new PanelContextBean(tagId, curPage);
	}
	
	public void trashFragment(Long fragmentId) {
		final Fragment frg = fragmentDao.findById(fragmentId);
		frg.addTag(getTrashTag());
		fragmentDao.save(frg);
		ViewUtil.addMessage("Successful", "Fragment #" + frg.getId() + " has been trashed", null);
	}

	public void deleteFragment(Long fragmentId) {
		final Fragment frg = fragmentDao.findById(fragmentId);
		fragmentDao.delete(frg);
		ViewUtil.addMessage("Successful", "Fragment #" + frg.getId() + " has been deleted", null);
	}
	
	public void saveFragment(FragmentBean fb, TagListBean tagListBean) {
		final String tagNames = fb.getTagNames();
		final Collection<Tag> tags = saveTags(tagListBean, tagNames);
	    
	    final Fragment frg = fb.getFragment();
	    frg.setTags(tags);
	    final DateTime dt = new DateTime();
	    if (frg.getCreationDatetime() == null) {
	    	// It is a new fragment...
	    	frg.setCreationDatetime(dt);
	    }
	    frg.setUpdateDatetime(dt);
	    
        fragmentDao.save(frg);
        ViewUtil.addMessage("Successful", "Fragment #" + frg.getId() + " has been saved", null);
	}
	
	private Collection<Tag> saveTags(TagListBean tagListBean, String tagNames) {
		final Collection<Tag> existingTags = tagListBean.getTags();
		final Collection<String> names = Tag.getTagNameCollectionFrom(tagNames);
		final List<Tag> output = new ArrayList<Tag>();
		for (String name : names) {
			Tag t = Tag.getTagFromName(name, existingTags);
			boolean weHaveNewTag = false;
			if (t == null) {
				t = new Tag(name);
				weHaveNewTag = true;
			}
			
			final DateTime dt = new DateTime();
		    if (t.getCreationDatetime() == null) {
		    	t.setCreationDatetime(dt);
		    }
		    t.setUpdateDatetime(dt);
		    
			tagDao.save(t);
			if (weHaveNewTag) {
				ViewUtil.addMessage("Successful", "Tag \"" + t.getTagName() + "\" has been saved", null);
			}
			output.add(t);
		}
		return output;
	}

	public FragmentBean inspectFragment(Integer index, FragmentListBean flb) {
		final FragmentBean fb = flb.getFragmentBeanAt(index);
	    logger.info("inspectFragment() called");
	    return fb;
	}


}
