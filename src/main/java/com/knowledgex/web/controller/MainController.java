package com.knowledgex.web.controller;

import java.util.*;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

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
    
    private static final Logger logger = LoggerFactory.getLogger(MainController.class);
    
    private static Tag trashTag = null;
    
	@Autowired
	private FragmentDao fragmentDao;

	@Autowired
	private TagDao tagDao;
	
	
	private static void addMessage(String title, String content, FacesMessage.Severity severity) {
		if (null == severity) {
			severity = FacesMessage.SEVERITY_INFO;
		}
		FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, new FacesMessage(severity, title, content));
	}

	private Tag getTrashTag() {
		if (null != trashTag) {
			return trashTag;
		}
		trashTag = tagDao.findById(Tag.TRASH_TAG_ID);
		return trashTag;
	}

	public FragmentListBean newFragmentListBean(FragmentListBean existingFlb, Long tagId, ContextBean ctxt) {
        FragmentListBean flb = existingFlb;
        if (null == flb) {
			flb = new FragmentListBean();
			flb.setPaginatorBean(new PaginatorBean());
		}
        
        PaginatorBean pb = flb.getPaginatorBean();
//        {
//			long tid = (null == tagId) ? -1 : tagId;
//			if (tid != flb.getCurTagId()) {
//				pb.reset();
//			}
//		}
        int count = pb.getItemsPerPage();
        int first = pb.getCurPage() * count;
//        addMessage("info", "count = "+count+", first = "+first, null);
        
        Collection<Fragment> fragments = null;
        if (null == tagId) {
        	// Fetch all the fragments
        	fragments = fragmentDao.findSome(first, count + 1);
        	flb.setCurTagId(-1);
        }
        else {
        	// Fetch the fragments with the specified tag
        	fragments = tagDao.findFragments(tagId);
//        	fragments = tagDao.findFragments(tagId, first, count + 1);
        	flb.setCurTagId(tagId);
        }
        
        pb.setCurPageAsLast(fragments.size() <= count);
        
        boolean trashTag = Tag.isTrashTag(tagId);
        if (!trashTag) {
        	// Exclude fragments that have '#trash' tag
        	List<Long> trashTagId = new ArrayList<Long>();
        	trashTagId.add(0L);
        	fragments = Fragment.applyExclusiveTagFilter(fragments, trashTagId);
        }
        ctxt.setFragmentDeletable(trashTag);
        
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
	
	public ContextBean newContextBean() {
	    return new ContextBean();
	}
	
	public void trashFragment(Long fragmentId) {
		Fragment frg = fragmentDao.findById(fragmentId);
		frg.addTag(getTrashTag());
		fragmentDao.save(frg);
		addMessage("Successful", "Fragment #" + frg.getId() + " has been trashed", null);
	}

	public void deleteFragment(Long fragmentId) {
		Fragment frg = fragmentDao.findById(fragmentId);
		fragmentDao.delete(frg);
		addMessage("Successful", "Fragment #" + frg.getId() + " has been deleted", null);
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
        addMessage("Successful", "Fragment #" + frg.getId() + " has been saved", null);
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
				addMessage("Successful", "Tag \"" + t.getTagName() + "\" has been saved", null);
			}
			output.add(t);
		}
		return output;
	}

	public void test(
			FragmentListBean flb
			, TagListBean tlb
			, FragmentBean fb
			) {
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
	
	public FragmentBean inspectFragment(Integer index, FragmentListBean flb) {
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
