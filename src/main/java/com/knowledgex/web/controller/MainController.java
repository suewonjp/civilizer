package com.knowledgex.web.controller;

import java.util.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.webflow.execution.RequestContext;

import com.knowledgex.dao.FragmentDao;
import com.knowledgex.dao.TagDao;
import com.knowledgex.domain.Fragment;
import com.knowledgex.domain.FragmentOrder;
import com.knowledgex.domain.SearchParams;
import com.knowledgex.domain.Tag;
import com.knowledgex.domain.TextDecorator;
import com.knowledgex.web.view.*;

@Controller
@Component("mainController")
public final class MainController {
	
	// [DEV]
	private static final String DEVELOPMENT_MESSAGE_CLIENT_ID = "fragment-group-form:development-messages";
	
	private static final int    MAX_FRAGMENT_PANELS = 3;
	private static final String REQUEST_PARAM_LOCALE = "locale";
    
    private final Logger logger = LoggerFactory.getLogger(MainController.class);
    
//    private final Gson gson = new GsonBuilder().registerTypeAdapter(DateTime.class, new JodaDateTimeConverter()).create();
    
	@Autowired
	private FragmentDao fragmentDao;

	@Autowired
	private TagDao tagDao;
	
	// [TODO] refactor code to maintain special tags
	private Tag trashcanTag = null;

	private Tag bookmarkTag = null;

	private Tag getTrashcanTag() {
		if (trashcanTag != null) {
			return trashcanTag;
		}
		trashcanTag = tagDao.findById((long) Tag.TRASH_TAG_ID);
		return trashcanTag;
	}

	private Tag getBookmarkTag() {
		if (bookmarkTag != null) {
			return bookmarkTag;
		}
		bookmarkTag = tagDao.findById((long) Tag.BOOKMARK_TAG_ID);
		return bookmarkTag;
	}
	
	private Tag getSpecialTag(String name) {
		if (name.equals(Tag.SPECIAL_TAG_NAMES[Tag.TRASH_TAG_ID])) {
			return getTrashcanTag();
		}
		else if (name.equals(Tag.SPECIAL_TAG_NAMES[-Tag.BOOKMARK_TAG_ID])) {
			return getBookmarkTag();
		}
		return null;
	}
	
	// [DEV]
	public void yetToBeDeveloped() {
		ViewUtil.addMessage(DEVELOPMENT_MESSAGE_CLIENT_ID, "Yet to be developed", "The feature is not ready for now", null);
	}
	
	public List<FragmentListBean> newFragmentListBeans() {
		final List<FragmentListBean> output =  new ArrayList<FragmentListBean>(MAX_FRAGMENT_PANELS);
		for (int i=0; i<MAX_FRAGMENT_PANELS; ++i) {
			final FragmentListBean flb = new FragmentListBean();
			final long tagId = (i == 0) ?
					PanelContextBean.ALL_VALID_TAGS : PanelContextBean.EMPTY_TAG;
			flb.setPanelContextBean(new PanelContextBean(i, tagId));
//			flb.setSearchContextBean(new SearchContextBean(i));
			output.add(flb);
		}
		return output;
	}
	
	public void populateFragmentListBeans(List<FragmentListBean> flbs, PanelContextBean pcb, SearchContextBean scb, RequestContext rc) {
//		final ExternalContext ec = rc.getExternalContext();
//		final ParameterMap pm =  ec.getRequestParameterMap();
//		final String locale = pm.get(REQUEST_PARAM_LOCALE);
//		logger.info(locale);
		
		for (int i=0; i<MAX_FRAGMENT_PANELS; ++i) {
			final PanelContextBean pc = (pcb != null && pcb.getPanelId() == i) ?
					pcb : null;
			final SearchContextBean sc = (scb != null && scb.getPanelId() == i) ?
					scb : null;
			populateFragmentListBean(flbs.get(i), pc, sc);
		}
	}

	private FragmentListBean populateFragmentListBean(FragmentListBean existingFlb, PanelContextBean pcb, SearchContextBean scb) {
        final FragmentListBean flb = existingFlb;
        final PanelContextBean oldPcb = flb.getPanelContextBean();
        final PanelContextBean paramPcb = pcb;
        
        if (pcb == null) {
        	pcb = oldPcb;
        }
        
        final long tagId = pcb.getTagId();
        int curPage = pcb.getCurPage();
        if (paramPcb != null) {
            curPage = Math.max(0, oldPcb.isLastPage() ? (paramPcb.getCurPage() - 1) : paramPcb.getCurPage());
        }
        final int count = pcb.getItemsPerPage();
        final int first = curPage * count;
        final FragmentOrder frgOrder = FragmentOrder.values()[flb.getOrderOption()];
        final boolean asc = flb.isOrderAsc();
        
        final SearchParams sp = (scb != null) ?
        		scb.buildSearchParams() : pcb.getSearchParams();
        		
        List<Fragment> fragments = Collections.emptyList();
        if (sp != null) {
        	// Fetch the fragments by the search parameters
        	// [TODO] pagination and ordering when fetching fragments by search
        	fragments = fragmentDao.findBySearchParams(sp);
        }
        else if (tagId == PanelContextBean.ALL_VALID_TAGS) {
        	// Fetch the fragments regardless of tags
        	fragments = fragmentDao.findSomeNonTrashed(first, count + 1, frgOrder, asc);
        }
        else if (tagId == PanelContextBean.EMPTY_TAG) {
        	fragments = Collections.emptyList();
        }
        else if (tagId == Tag.TRASH_TAG_ID) {
        	// Fetch the trashed fragments
            fragments = fragmentDao.findSomeByTagId(tagId, first, count + 1, frgOrder, asc);
        }
        else {
        	// Fetch the fragments with the specified tag (non-trashed)
            fragments = fragmentDao.findSomeNonTrashedByTagId(tagId, first, count + 1, frgOrder, asc, tagDao);
        }
        
        // [NOTE] The content of fragments should be IMMUTABLE form here!
        
        final boolean isLastPage = fragments.size() <= count;
        final boolean givenTagIsTrashTag = Tag.isTrashTag(tagId);
        flb.setPanelContextBean(new PanelContextBean(pcb.getPanelId(), tagId, curPage, count, isLastPage, givenTagIsTrashTag, sp));
//        ViewUtil.addMessage("pcb", flb.getPanelContextBean());
        
        List<FragmentBean> fragmentBeans = new ArrayList<FragmentBean>();
        final int c = Math.min(count, fragments.size());
       	for (int i=0; i<c; ++i) {
       		Fragment f = fragments.get(i);
        	FragmentBean fb = new FragmentBean();
        	fb.setFragment(f);
        	
        	String title = f.getTitle();
        	String content = f.getContent();
        	if (sp != null) {
        	    title = TextDecorator.highlight(title, sp);
        	    content = TextDecorator.highlight(content, sp);
        	}
        	fb.setTitle(title);
        	fb.setContent(content);
        	
        	final String tagNames = Tag.getTagNamesFrom(f.getTags());
        	fb.setConcatenatedTagNames(tagNames);
        	fragmentBeans.add(fb);
        }
       	if (fragmentBeans.isEmpty()) {
       		fragmentBeans = Collections.emptyList();
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
		final List<Tag> tags = tagDao.findAllWithChildren(false);
	    tagListBean.setTags(tags);
	    final int tc = tags.size();
	    final List<TagBean> tagBeans = new ArrayList<TagBean>();
	    final boolean includeTrashed = false;
	    for (int i = 0; i < tc; i++) {
	    	TagBean tb = new TagBean();
	    	final Tag t = tags.get(i);
	    	tb.setTag(t);
	    	final long fc = fragmentDao.countByTagAndItsDescendants(t.getId(), includeTrashed, tagDao);
	    	tb.setFragmentCount(fc);
	    	tagBeans.add(tb);
	    }
	    tagListBean.setTagBeans(tagBeans);
	    
	    final TagTree tagTree = newTagTree();
	    tagListBean.setTagTree(tagTree);
        return tagListBean;
    }
	
	public TagBean newTagBean() {
		final TagBean tagBean = new TagBean();
		final Tag tag = new Tag();
		tagBean.setTag(tag);
		return tagBean;
	}

	public SpecialTagBean newBookmarkTagBean() {
		final SpecialTagBean tagBean = new SpecialTagBean();
		
		final Tag tag = getBookmarkTag();
		tagBean.setTag(tag);
		
		final List<Fragment> fragments = fragmentDao.findByTagId(tag.getId(), false);
//		final Set<Fragment> fragments = tag.getFragments();
		final List<FragmentBean> fbs = new ArrayList<FragmentBean>();
		for (Fragment fragment : fragments) {
			final FragmentBean fb = new FragmentBean();
			fb.setFragment(fragment);
			fbs.add(fb);
		}
		tagBean.setFragmentBeans(fbs);
		
		return tagBean;
	}
	
	private TagTree newTagTree() {
		final TagTree tagTree = new TagTree();
	    return tagTree;
	}

	public PanelContextBean newPanelContextBean(int panelId, long tagId, int curPage) {
		return new PanelContextBean(panelId, tagId, curPage);
	}
	
//	public SearchContextBean[] newSearchContextBeans() {
//		SearchContextBean[] output = { new SearchContextBean(0), new SearchContextBean(1), new SearchContextBean(2) };
//		return output;
//	}

	public List<SearchContextBean> newSearchContextBeans() {
		List<SearchContextBean> output = new ArrayList<SearchContextBean>();
		output.add(new SearchContextBean(0));
		output.add(new SearchContextBean(1));
		output.add(new SearchContextBean(2));
		return output;
	}
	
	public SearchContextBean getSearchContextBean(List<SearchContextBean> beans, int panelId) {
		return beans.get(panelId);
	}
	
	public void bookmarkFragment(Long fragmentId) {
	    final Fragment frg = fragmentDao.findById(fragmentId, true, false);
	    frg.addTag(getBookmarkTag());
	    fragmentDao.save(frg);
	    ViewUtil.addMessage("Bookmarking", "Fragment #" + frg.getId(), null);
	}

	public void unbookmarkFragment(Long fragmentId) {
	    final Fragment frg = fragmentDao.findById(fragmentId, true, false);
	    frg.removeTag(getBookmarkTag());
	    fragmentDao.save(frg);
	    ViewUtil.addMessage("Unbookmarking", "Fragment #" + frg.getId(), null);
	}

	public void trashFragment(Long fragmentId) {
		final Fragment frg = fragmentDao.findById(fragmentId, true, false);
		frg.addTag(getTrashcanTag());
		fragmentDao.save(frg);
		ViewUtil.addMessage("Trashing", "Fragment #" + frg.getId(), null);
	}

	public void trashFragment(FragmentBean fb) {
		trashFragment(fb.getFragment().getId());
	}

	private void trashFragments(List<Long> fragmentIds) {
		for (Long id : fragmentIds) {
			trashFragment(id);
		}
	}

	public void trashFragments(FragmentListBean flb) {
		final Collection<FragmentBean> fragmentBeans = flb.getFragmentBeans();
		for (FragmentBean fb : fragmentBeans) {
			if (!fb.isChecked()) {
				continue;
			}
			trashFragment(fb.getFragment().getId());
		}
	}

	public void deleteFragment(Long fragmentId) {
		final Fragment frg = fragmentDao.findById(fragmentId);
		fragmentDao.delete(frg);
		ViewUtil.addMessage("Deleting", "Fragment #" + frg.getId(), null);
	}

	public void deleteFragment(FragmentBean fb) {
		deleteFragment(fb.getFragment().getId());
	}
	
	public void deleteFragments(FragmentListBean flb) {
		final Collection<FragmentBean> fragmentBeans = flb.getFragmentBeans();
		for (FragmentBean fb : fragmentBeans) {
			if (!fb.isChecked()) {
				continue;
			}
			deleteFragment(fb.getFragment().getId());
		}
	}
	
	public void saveFragment(FragmentBean fb, TagListBean tagListBean) {
		final String tagNames = fb.getConcatenatedTagNames();
		final Set<Tag> tags = saveTagsWhenSavingFragment(tagListBean, tagNames);
	    
	    Fragment frg = fb.getFragment();
	    boolean weHaveNewFragment = false;
	    
	    final DateTime dt = new DateTime();
	    if (frg.getId() == null) {
	    	// It is a new fragment...
	    	frg.setCreationDatetime(dt);
	    	weHaveNewFragment = true;
	    }
	    else {
	    	// It is an existing fragment...
	    	final String content = frg.getContent();
	    	frg = fragmentDao.findById(frg.getId());
	    	frg.setContent(content);
	    }
	    frg.setUpdateDatetime(dt);

	    frg.setTags(tags);
	    
        fragmentDao.save(frg);
        if (weHaveNewFragment) {
        	ViewUtil.addMessage("Creating", "Fragment #" + frg.getId(), null);
        }
        else {
		    ViewUtil.addMessage("Updating", "Fragment #" + frg.getId(), null);
		}
	}
	
	private void saveTag(Tag t) {
		final DateTime dt = new DateTime();
	    if (t.getId() == null) {
	    	// It is a new tag...
	    	t.setCreationDatetime(dt);
	    }
	    t.setUpdateDatetime(dt);
	    
		tagDao.save(t);
	}
	
	private Set<Tag> saveTagsWhenSavingFragment(TagListBean tagListBean, String tagNames) {
		// [NOTE] this method should be called only when fragments are saved as its name implies
		final Collection<Tag> existingTags = tagListBean.getTags();
		final Collection<String> names = Tag.getTagNameCollectionFrom(tagNames);
		final Set<Tag> output = new HashSet<Tag>();
		for (String name : names) {
			Tag t = Tag.isSpecialTag(name) ?
					getSpecialTag(name) : Tag.getTagFromName(name, existingTags);
			
			boolean weHaveNewTag = false;
			if (t == null) {
				t = new Tag(name);
				weHaveNewTag = true;
			}
			
			saveTag(t);
			
			if (weHaveNewTag) {
				ViewUtil.addMessage("Creating", "Tag : " + t.getTagName(), null);
			}

			output.add(t);
		}
		return output;
	}
	
	public void saveTag(TagBean tb, TagListBean tagListBean) {
		Tag t = tb.getTag();
		if (t.getId() == null) {
			// a new tag
			// [TODO] handle exceptions occurred in the persistence layer.
			// - e.g. tag name collision exception with the existing tags
			// - and redirect the exception information to the view layer elegantly
			saveTag(t);
			ViewUtil.addMessage("Creating", "Tag : " + t.getTagName(), null);
			
		}
		else {
			// an existing tag
			final String newName = t.getTagName();
			t = Tag.getTagFromId(t.getId(), tagListBean.getTags());
			final String oldName = t.getTagName();
			t.setTagName(newName);
			saveTag(t);
			ViewUtil.addMessage("Renaming", "Tag : " + oldName + " => " + newName, null);
		}
	}
	
	public void trashTag(TagBean tb) {
		final Tag t = tb.getTag();
		final Long id = t.getId();
		if (id != null) {
			final List<Long> fids = fragmentDao.findIdsByTagId(id);
			trashFragments(fids);
		}
	}

	public void deleteTag(TagBean tb) {
		Tag t = tb.getTag();
		final Long id = t.getId();
		if (id != null) {
			t = tagDao.findById(id);
			tagDao.delete(t);
			ViewUtil.addMessage("Deleting", "Tag : " + t.getTagName(), null);
		}
	}

	public FragmentBean inspectFragment(Integer index, FragmentListBean flb) {
		final FragmentBean fb = flb.getFragmentBeanAt(index);
	    logger.info("inspectFragment() called");
	    return fb;
	}
	
    @RequestMapping(value = "/fragment/{fragmentId}", method = { RequestMethod.GET })
    public String onRequestForFragment(ModelMap model, @PathVariable Long fragmentId) {
    	final Fragment frg = fragmentDao.findById(fragmentId, true, true);
    	model.addAttribute("fragment", frg);
        return "fragment";
    }

    @RequestMapping(value = "/locale/{locale}", method = { RequestMethod.GET })
    public String onRequestForLocale(@PathVariable String locale, HttpServletResponse response) {
    	Cookie cookie = new Cookie(REQUEST_PARAM_LOCALE, locale);
        response.addCookie(cookie);
    	return "redirect:/app/home?locale=" + locale;
    }

}
