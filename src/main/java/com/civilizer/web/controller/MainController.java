package com.civilizer.web.controller;

import java.io.File;
import java.io.IOException;
import java.util.*;

import javax.faces.application.FacesMessage;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.webflow.execution.RequestContext;

import com.civilizer.config.AppOptions;
import com.civilizer.config.Configurator;
import com.civilizer.dao.FileEntityDao;
import com.civilizer.dao.FragmentDao;
import com.civilizer.dao.TagDao;
import com.civilizer.domain.FileEntity;
import com.civilizer.domain.Fragment;
import com.civilizer.domain.FragmentOrder;
import com.civilizer.domain.SearchParams;
import com.civilizer.domain.Tag;
import com.civilizer.domain.TextDecorator;
import com.civilizer.security.UserDetailsService;
import com.civilizer.utils.FsUtil;
import com.civilizer.web.view.*;

@Controller
@Component("mainController")
public final class MainController {
	
	// [DEV]
	private static final String DEVELOPMENT_MESSAGE_CLIENT_ID = "fragment-group-form:development-messages";
	
	private static final int    MAX_FRAGMENT_PANELS = 3;
	private static final String REQUEST_PARAM_LOCALE = "locale";
    
	@Autowired
	private FragmentDao fragmentDao;

	@Autowired
	private TagDao tagDao;
	
	@Autowired
	private FileEntityDao fileEntityDao;
	
	private Tag getTrashcanTag() {
		return tagDao.findById((long) Tag.TRASH_TAG_ID);
	}

	private Tag getBookmarkTag() {
		return tagDao.findById((long) Tag.BOOKMARK_TAG_ID);
	}

	private Tag getUntaggedTag() {
	    return tagDao.findById((long) Tag.UNTAGGED_TAG_ID);
	}
	
	private Tag getSpecialTag(String name) {
		return tagDao.findById((long) Tag.getSpecialTagId(name));
	}
	
	private void removeTag(Fragment frg, Tag tag) {
	    frg.removeTag(tag);
	    if (frg.getTags().isEmpty()) {
	        // [NOTE] As a rule, untagged fragments are not allowed;
	        // So we encounter such a tag, get it tagged with the '#untagged' tag
	        frg.addTag(getUntaggedTag());
	    }
	}
	
	// [DEV]
	public void yetToBeDeveloped(Object ... param) {
		String params = "";
		for (Object p : param) {
			params += p.toString() + ", ";
		}
		ViewUtil.addMessage(DEVELOPMENT_MESSAGE_CLIENT_ID, "Yet to be developed", params, null);
	}
	
	public FragmentListBean[] newFragmentListBeans() {
		final FragmentListBean[] output =  { null, null, null };
		for (int i=0; i<MAX_FRAGMENT_PANELS; ++i) {
			final FragmentListBean flb = new FragmentListBean();
			final long tagId = (i == 0) ?
					PanelContextBean.ALL_VALID_TAGS : PanelContextBean.EMPTY_TAG;
			flb.setPanelContextBean(new PanelContextBean(i, tagId));
			output[i] = flb;
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
			populateFragmentListBean(flbs.get(i), pc, sc, rc);
		}
	}

	private FragmentListBean populateFragmentListBean(FragmentListBean existingFlb, PanelContextBean pcb, SearchContextBean scb, RequestContext rc) {
        final FragmentListBean flb = existingFlb;
        final PanelContextBean oldPcb = flb.getPanelContextBean();
        final PanelContextBean paramPcb = pcb;
        final FragmentSelectionBean fsb = (FragmentSelectionBean) rc.getFlowScope().get("fragmentSelectionBean");
        
        if (pcb == null) {
        	pcb = oldPcb;
        }

        int curPage = pcb.getCurPage();
        if (paramPcb != null) {
        	// the current page has been updated by going forward or forward
        	curPage = Math.max(0,
        			oldPcb.isLastPage() ? (paramPcb.getCurPage() - 1) : paramPcb.getCurPage());
        }
        SearchParams sp = oldPcb.getSearchParams();                
        long tagId = pcb.getTagId();
        if (scb != null) {
        	// a new KEYWORD SEARCH has been kicked;
            sp = scb.buildSearchParams();
            // this branch has the highest priority of all so it forces to overwrite a few key variables like so:
            tagId = PanelContextBean.EMPTY_TAG;
            curPage = 0;
        }
        final int count = pcb.getItemsPerPage();
        final int first = curPage * count;
        final FragmentOrder frgOrder = FragmentOrder.values()[flb.getOrderOption()];
        final boolean asc = flb.isOrderAsc();
        
        List<Fragment> fragments = Collections.emptyList(); // resultant fragments
        long allCount = 0; // the number of fragments at maximum
        if (tagId == PanelContextBean.ALL_VALID_TAGS) {
            // Fetch all the fragments
            fragments = fragmentDao.findSomeNonTrashed(first, count + 1, frgOrder, asc);
            allCount = fragmentDao.countAll(false);
            sp = null;
        }
        else if (tagId == Tag.TRASH_TAG_ID) {
            // Fetch the trashed fragments
            fragments = fragmentDao.findSomeByTagId(tagId, first, count + 1, frgOrder, asc);
            allCount = fragmentDao.countByTagAndItsDescendants(tagId, true, tagDao);
            sp = null;
        }
        else if (tagId != PanelContextBean.EMPTY_TAG) {
        	// Fetch the fragments with the specified tag (non-trashed)
        	fragments = fragmentDao.findSomeNonTrashedByTagId(tagId, first, count + 1, frgOrder, asc, tagDao);
        	allCount = fragmentDao.countByTagAndItsDescendants(tagId, false, tagDao);
            sp = null;
        }
        else if (sp != null) {
            // Fetch the fragments by the search parameters
            final TagListBean tagListBean = (TagListBean) rc.getFlowScope().get("tagListBean");
            fragments = fragmentDao.findBySearchParams(sp, tagListBean.getTags());
            allCount = fragments.size();
            if (allCount > 0) {
                fragments = Fragment.paginate(fragments, first, count + 1, frgOrder, asc);
            }
            tagId = PanelContextBean.EMPTY_TAG;
        }
        
        // [NOTE] The content of fragments should be IMMUTABLE form here!
        
        final boolean isLastPage = fragments.size() <= count;
        final boolean givenTagIsTrashTag = Tag.isTrashTag(tagId);
        flb.setTotalCount(allCount);
        // Record the panel context; it will be referred at the next page update
        flb.setPanelContextBean(new PanelContextBean(pcb.getPanelId(), tagId, curPage, count, isLastPage, givenTagIsTrashTag, sp));
//        ViewUtil.addMessage("pcb", flb.getPanelContextBean());
        
        List<FragmentBean> fragmentBeans = new ArrayList<FragmentBean>();
        final int c = Math.min(count, fragments.size());
       	for (int i=0; i<c; ++i) {
       		Fragment f = fragments.get(i);
        	fragmentBeans.add(newFragmentBean(f, sp, fsb));
        }
       	if (fragmentBeans.isEmpty()) {
       		fragmentBeans = Collections.emptyList();
       	}
        flb.setFragmentBeans(fragmentBeans);
        
        return flb;
    }

	private FragmentBean newFragmentBean(Fragment f, SearchParams sp, FragmentSelectionBean fsb) {
	    FragmentBean fb = new FragmentBean();
        fb.setFragment(f);
        
        // [NOTE] due to some weird bug, accessing Fragment.relatedOnes directly from the view does't work;
        // (the bug appears after relating many pairs of fragments and JSF simply denies retrieving some items from the collection)
        // the solution is accessing a fresh new copy of Fragment.relatedOnes;
        fb.setRelatedOnes(new ArrayList<Fragment>(f.getRelatedOnes()));
        f.setRelatedOnes(Collections.<Fragment>emptySet()); // the original copy is unnecessary.
        
        fb.setFragmentSelectionBean(fsb);
        
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
        
        return fb;
	}
	
	public FragmentBean newFragmentBean() {
	    final FragmentBean fragmentBean = new FragmentBean();
	    final Fragment frg = new Fragment();
	    fragmentBean.setFragment(frg);
	    return fragmentBean;
	}
	
	public TagListBean newTagListBean() {
		boolean includeTrashed = true;
		final List<Tag> tags = tagDao.findAllWithChildren(includeTrashed);
		final TagListBean tagListBean = new TagListBean();
	    tagListBean.setTags(tags);
	    final int tc = tags.size();
	    final List<TagBean> tagBeans = new ArrayList<TagBean>();
	    for (int i = 0; i < tc; i++) {
	    	TagBean tb = new TagBean();
	    	final Tag t = tags.get(i);
	    	tb.setTag(t);
	    	includeTrashed = (t.getId() == Tag.TRASH_TAG_ID);
	    	final long fc = fragmentDao.countByTagAndItsDescendants(t.getId(), includeTrashed, tagDao);
	    	tb.setFragmentCount(fc);
	    	tagBeans.add(tb);
	    }
	    tagListBean.setTagBeans(tagBeans);
	    
	    final TagTree tagTree = newTagTree();
	    tagListBean.setTagTree(tagTree);
        return tagListBean;
    }
	
	public FileListBean newFileListBean() {
		final FileListBean output = new FileListBean();
		final List<FileEntity> fileEntities = fileEntityDao.findAll();
		output.setFileEntities(fileEntities);
		final FilePathTree filePathTree = newFilePathTree();
		output.setFilePathTree(filePathTree);
		final FilePathTree folderTree = newFilePathTree();
		output.setFolderTree(folderTree);
		return output;
	}
	
	public TagBean newTagBean() {
		final TagBean tagBean = new TagBean();
		final Tag tag = new Tag();
		tagBean.setTag(tag);
		return tagBean;
	}

	public void prepareTagListBeanToEditTag(TagListBean tagListBean, TagBean tagBean) {
		final long tagId = tagBean.getTag().getId();
		if (tagId <= 0) {
		    final String name = Tag.getSpecialTagName(tagId);
		    tagBean.getTag().setTagName(name);
		    tagListBean.setParentTags(null);
		    tagListBean.setChildTags(null);
		}
		else {
    		tagListBean.setTagToEdit(tagId);
    		tagBean.getTag().setTagName(tagListBean.getTagToEdit().getTag().getTagName());
    		tagListBean.setParentTags(tagDao.findParentTags(tagId));
		}
	}
	
	public FragmentSelectionBean newBookmarkTagBean() {
		final FragmentSelectionBean output = new FragmentSelectionBean();
		
		final Tag tag = getBookmarkTag();
		
		final List<Fragment> fragments = fragmentDao.findByTagId(tag.getId(), false);
		Fragment.sort(fragments, FragmentOrder.TITLE, true);
		final List<Long> ids = new ArrayList<>();
		final List<String> titles = new ArrayList<>();
		for (Fragment fragment : fragments) {
			ids.add(fragment.getId());
			titles.add(fragment.getTitle());
		}
		output.setFragmentIds(ids);
		output.setFragmentTitles(titles);
		
		return output;
	}

	private TagTree newTagTree() {
		final TagTree tagTree = new TagTree();
	    return tagTree;
	}

	private FilePathTree newFilePathTree() {
		final FilePathTree fpTree = new FilePathTree();
		return fpTree;
	}

	public PanelContextBean newPanelContextBean(int panelId, long tagId, int curPage) {
		return new PanelContextBean(panelId, tagId, curPage);
	}

	public PanelContextBean newPanelContextBean(PanelContextBean oldPcb, int pageOffset) {
	    return new PanelContextBean(oldPcb.getPanelId(), oldPcb.getTagId(), oldPcb.getCurPage() + pageOffset);
	}
	
	public SearchContextBean newSearchContextBean() {
	    return new SearchContextBean();
	}
	
	public FragmentSelectionBean newFragmentSelectionBean() {
	    return new FragmentSelectionBean();
	}

	public void bookmarkFragment(Long fragmentId) {
	    final Fragment frg = fragmentDao.findById(fragmentId, true, false);
	    frg.addTag(getBookmarkTag());
	    try {
			fragmentDao.save(frg);
			ViewUtil.addMessage("Bookmarked", "Fragment #" + frg.getId(), null);
		}
	    catch (Exception e) {
	    	e.printStackTrace();
			ViewUtil.addMessage("Error on bookmarking!!!", e.getLocalizedMessage(), FacesMessage.SEVERITY_ERROR);
		}
	}

	public void unbookmarkFragment(Long fragmentId) {
	    final Fragment frg = fragmentDao.findById(fragmentId, true, false);
	    removeTag(frg, getBookmarkTag());
//	    frg.removeTag(getBookmarkTag());
	    try {
			fragmentDao.save(frg);
			ViewUtil.addMessage("Unbookmarked", "Fragment #" + frg.getId(), null);
		}
	    catch (Exception e) {
	    	e.printStackTrace();
			ViewUtil.addMessage("Error on unbookmarking!!!", e.getLocalizedMessage(), FacesMessage.SEVERITY_ERROR);
		}
	}

	public void trashFragment(Long fragmentId) {
		final Fragment frg = fragmentDao.findById(fragmentId, true, false);
		frg.addTag(getTrashcanTag());
		try {
			fragmentDao.save(frg);
			ViewUtil.addMessage("Trashed", "Fragment #" + frg.getId(), null);
		}
		catch (Exception e) {
			e.printStackTrace();
			ViewUtil.addMessage("Error on trashing a fragment!!!", e.getLocalizedMessage(), FacesMessage.SEVERITY_ERROR);
		}
	}

	public void restoreFragment(Long fragmentId) {
	    final Fragment frg = fragmentDao.findById(fragmentId, true, false);
	    final Tag trashcanTag = getTrashcanTag();
	    if (! frg.containsTagId(trashcanTag.getId())) {
	        return;
	    }
	    removeTag(frg, trashcanTag);
//	    frg.removeTag(trashcanTag);
	    try {
	        fragmentDao.save(frg);
	        ViewUtil.addMessage("Restored", "Fragment #" + frg.getId(), null);
	    }
	    catch (Exception e) {
	        e.printStackTrace();
	        ViewUtil.addMessage("Error on restorng a fragment!!!", e.getLocalizedMessage(), FacesMessage.SEVERITY_ERROR);
	    }
	}

	private void trashFragments(List<Long> fragmentIds) {
		for (Long id : fragmentIds) {
			trashFragment(id);
		}
	}

	public void trashFragments(FragmentSelectionBean fsb, String unselected) {
	    final String[] tmp = StringUtils.split(unselected);
	    final long[] excIds = new long[tmp.length]; // ids to be excluded
	    for (int i=0; i<tmp.length; ++i) {
            excIds[i] = Integer.parseInt(tmp[i]);
        }
	    final List<Long> ids = new ArrayList<>();
	    for (Long id : fsb.getFragmentIds()) {
	        if (ArrayUtils.indexOf(excIds, id) >= 0)
	            continue;
            ids.add(id);
        }
	    trashFragments(ids);
	    fsb.clear();
	}
	
	public void deleteFragment(Long fragmentId) {
		final Fragment frg = fragmentDao.findById(fragmentId);
		try {
			fragmentDao.delete(frg);
			ViewUtil.addMessage("Deleted", "Fragment #" + frg.getId(), null);
		}
		catch (Exception e) {
			e.printStackTrace();
			ViewUtil.addMessage("Error on deleting a fragment!!!", e.getLocalizedMessage(), FacesMessage.SEVERITY_ERROR);
		}
	}

	public void emptyTrash() {
        final Tag tag = getTrashcanTag();
        final List<Fragment> fragments = fragmentDao.findByTagId(tag.getId(), true);
        try {
            for (Fragment frg : fragments) {
                fragmentDao.delete(frg);
            }
            ViewUtil.addMessage("Trash emptied", "", null);
        }
        catch (Exception e) {
            e.printStackTrace();
            ViewUtil.addMessage("Error on emptying trash!!!", e.getLocalizedMessage(), FacesMessage.SEVERITY_ERROR);
        }            
	}
	
	public void saveFragment(FragmentBean fb, TagListBean tagListBean, Long relatedFrgId) {
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
	    	final String title = frg.getTitle();
	    	frg = fragmentDao.findById(frg.getId());
	    	frg.setContent(content);
	    	frg.setTitle(title);
	    }
	    frg.setUpdateDatetime(dt);

	    frg.setTags(tags);
	    
        try {
			fragmentDao.save(frg);
			ViewUtil.addMessage(weHaveNewFragment ? "Created" : "Updated", "Fragment #" + frg.getId(), null);
		}
        catch (Exception e) {
        	e.printStackTrace();
			ViewUtil.addMessage("Error on saving a fragment!!!", e.getLocalizedMessage(), FacesMessage.SEVERITY_ERROR);
			return;
		}
        
        if (relatedFrgId != null) {
            relateFragments(relatedFrgId, frg.getId());
        }
	}
	
	public void touchFragment(Long fragmentId) {
	    final Fragment frg = fragmentDao.findById(fragmentId);
	    final DateTime dt = new DateTime();
	    frg.setUpdateDatetime(dt);
        
        try {
            fragmentDao.save(frg);
            ViewUtil.addMessage("Updated", "Fragment #" + frg.getId(), null);
        }
        catch (Exception e) {
            e.printStackTrace();
            ViewUtil.addMessage("Error on saving a fragment!!!", e.getLocalizedMessage(), FacesMessage.SEVERITY_ERROR);
            return;
        }
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
			    name = Tag.stripDoubleQuotes(name);
			    final char invalidCharacter = Tag.findInvalidCharFromName(name);
			    if (invalidCharacter != 0) {
			        final String msg = String.format("'%s' contains a disallowed character : %s", name, invalidCharacter);
			        ViewUtil.addMessage("Error on saving a new tag!!!", msg, FacesMessage.SEVERITY_ERROR);
			        continue;
			    }
				t = new Tag(name);
				weHaveNewTag = true;
			}
			
			try {
				tagDao.save(t);
				if (weHaveNewTag) {
					ViewUtil.addMessage("Created", "Tag : " + t.getTagName(), null);
				}
			}
			catch (Exception e) {
				e.printStackTrace();
				ViewUtil.addMessage("Error on saving a tag during saving fragments!!!", e.getLocalizedMessage(), FacesMessage.SEVERITY_ERROR);
			}

			output.add(t);
		}
		
		final Tag untaggedTag = getUntaggedTag();
		if (output.isEmpty()) {
		    // no tag attached; simply tag it as #untagged
		    ViewUtil.addMessage("No tag assigned", " - it has been tagged as "+untaggedTag.getTagName(), null);
		    output.add(untaggedTag);
		}
		else if (output.contains(untaggedTag)) {
		    // detach the tag of #untagged if the fragment has another (non-special) tag;
		    boolean shouldDetach = false;
		    for (Tag tag : output) {
                if (Tag.isTrivialTag(tag.getId())) {
                    shouldDetach = true;
                    break;
                }
            }
		    if (shouldDetach) {
		        output.remove(untaggedTag);
		    }
		}
		
		return output;
	}
	
	public void saveTag(TagBean tagBean, TagListBean tagListBean, boolean isNewTag) {
		final String newName = tagBean.getTag().getTagName();        
        if (newName.isEmpty()) {
            ViewUtil.addMessage("Error on creating/updating a tag!!!", "An empty tag name is not allowed!", FacesMessage.SEVERITY_ERROR);
            return;
        }
		
        Tag t = null;
        String oldName = null;

        if (isNewTag) {
		    t = new Tag();
		}
		else {
		    final TagBean tagToEdit = tagListBean.getTagToEdit();
		    t = tagToEdit.getTag();
		    oldName = t.getTagName();
		}
		t.setTagName(newName);
		
		try {
			if (tagListBean.isHierarchyTouched()) {
				// persistence request from the tag editor; tag hierarchy would be updated
				tagDao.saveWithHierarchy(t, tagListBean.getParentTags(), tagListBean.getChildTags());
			}
			else {
				// persistence request without updating relationships; e.g. renaming only
				tagDao.save(t);
			}
			if (isNewTag)
			    ViewUtil.addMessage("Created", "Tag : " + newName, null);
			else
			    ViewUtil.addMessage("Updated", "Tag : " + oldName + " => " + newName, null);
		}
		catch (Exception e) {
			e.printStackTrace();
			ViewUtil.addMessage("Error on creating/updating a tag!!!", e.getLocalizedMessage(), FacesMessage.SEVERITY_ERROR);
		}
	}
	
	public void trashTag(Long id) {
        final List<Long> fids = fragmentDao.findIdsByTagId(id);
        trashFragments(fids);
	}
	
	public void deleteTag(Long id) {
        final Tag t = tagDao.findById(id);
        try {
            tagDao.delete(t);
            ViewUtil.addMessage("Deleted", "Tag : " + t.getTagName(), null);
        }
        catch (Exception e) {
            e.printStackTrace();
            ViewUtil.addMessage("Error on deleting a tag!!!", e.getLocalizedMessage(), FacesMessage.SEVERITY_ERROR);
        }
	}
	
	public void relateFragments(long fromId, long toId) {
		try {
			fragmentDao.relateFragments(fromId, toId);
			ViewUtil.addMessage("Related", "Fragments : " + fromId + " <==> " + toId, null);
		}
		catch (Exception e) {
			e.printStackTrace();
			ViewUtil.addMessage("Error on relating fragments!!!", e.getLocalizedMessage(), FacesMessage.SEVERITY_ERROR);
		}
	}

	public void relateFragments(FragmentSelectionBean fsb, String unselected) {
        final String[] tmp = StringUtils.split(unselected);
        final long[] excIds = new long[tmp.length]; // ids to be excluded
        for (int i=0; i<tmp.length; ++i) {
            excIds[i] = Integer.parseInt(tmp[i]);
        }
        final List<Long> ids = new ArrayList<>();
        for (Long id : fsb.getFragmentIds()) {
            if (ArrayUtils.indexOf(excIds, id) >= 0)
                continue;
            ids.add(id);
        }
	    try {
	        final int idc = ids.size();
	        for (int i=0; i<idc-1; ++i) {
	            for (int j=i+1; j<idc; ++j) {
	                long fromId = ids.get(i), toId = ids.get(j);
	                fragmentDao.relateFragments(fromId, toId);
	                ViewUtil.addMessage("Related", "Fragments : " + fromId + " <==> " + toId, null);
	            }
	        }
	    }
	    catch (Exception e) {
	        e.printStackTrace();
	        ViewUtil.addMessage("Error on relating fragments!!!", e.getLocalizedMessage(), FacesMessage.SEVERITY_ERROR);
	    }
	}

	public void unrelateFragments(long fromId, long toId) {
		try {
			fragmentDao.unrelateFragments(fromId, toId);
			ViewUtil.addMessage("Unrelated", "Fragments : " + fromId + " <" + Character.toString((char) 0x2260) + "> " + toId, null);
		}
		catch (Exception e) {
			e.printStackTrace();
			ViewUtil.addMessage("Error on unrelating fragments!!!", e.getLocalizedMessage(), FacesMessage.SEVERITY_ERROR);
		}
	}
	
	public void uploadFile(FileUploadBean fileUploadBean, FileListBean fileListBean) {
		final int dstNodeId = fileListBean.getDstNodeId();
		final String newFileName = fileUploadBean.getFileName();
		final String filePath = fileListBean.getFullFilePath(dstNodeId, newFileName);
		final String filesHomePath = System.getProperty(AppOptions.FILE_BOX_HOME);
		final String fileWritePath = filesHomePath + filePath;
		if (fileUploadBean.saveFile(fileWritePath)) {
			final FileEntity fe = new FileEntity(filePath);
			try {
				fileEntityDao.save(fe);
				ViewUtil.addMessage("File Uploaded", filePath, null);
			}
			catch (Exception e) {
				e.printStackTrace();
				ViewUtil.addMessage("Error on File Upload!!!", filePath + " :: " + e.getLocalizedMessage(), FacesMessage.SEVERITY_ERROR);
			}
		}
		else {
			ViewUtil.addMessage("Error on File Upload!!!", filePath, FacesMessage.SEVERITY_ERROR);
		}
	}

	public void renameFile(FileListBean fileListBean) {
		final int srcNodeId = fileListBean.getSrcNodeId();
		final String newName = fileListBean.getFileName();
		final String filesHomePath = System.getProperty(AppOptions.FILE_BOX_HOME);
		
		if (srcNodeId < 0) {
			// [RULE] Create a new directory if *srcNodeId* is a minus value;
			// [NOTE] we need to decode *srcNodeId* before passing it to the next processing
			if (fileListBean.createNewFolder(-srcNodeId - 1, newName, filesHomePath) == null) {
				ViewUtil.addMessage("Error on Creating a Folder!!!", newName + " : already exists!", FacesMessage.SEVERITY_ERROR);
			}
			return;
		}
		
		final FilePathBean filePathBean = fileListBean.getFilePathBean(srcNodeId);
		
		final String oldFilePath = filePathBean.getFullPath();
		List<FileEntity> entities = Collections.emptyList();
		
		if (filePathBean.isFolder()) {
			final File oldDir = filePathBean.toFile(filesHomePath);
			final FileEntity fe = new FileEntity(oldFilePath);
			fe.replaceNameSegment(oldFilePath, newName);
			final File newDir = fe.toFile(filesHomePath);
			
			try {
				FileUtils.moveDirectory(oldDir, newDir);
			} catch (IOException e) {
				e.printStackTrace();
				ViewUtil.addMessage("Error on Renaming a Folder!!!", oldFilePath + " :: " + e.getLocalizedMessage(), FacesMessage.SEVERITY_ERROR);
				return;
			}
			
			entities = fileEntityDao.findByNamePattern(oldFilePath+File.separator+'%');
		}
		else {
			final File oldFile = filePathBean.toFile(filesHomePath);
			final FileEntity fe = new FileEntity(oldFilePath);
			fe.replaceNameSegment(oldFilePath, newName);
			final File newFile = fe.toFile(filesHomePath);
			
			try {
				FileUtils.moveFile(oldFile, newFile);
			} catch (IOException e) {
				e.printStackTrace();
				ViewUtil.addMessage("Error on Renaming a File!!!", oldFilePath + " :: " + e.getLocalizedMessage(), FacesMessage.SEVERITY_ERROR);
				return;
			}
			
			FileEntity entity = fileEntityDao.findByName(oldFilePath);
			if (entity != null) {
				entities = new ArrayList<>();
				entities.add(entity);
			}
		}
		
		for (FileEntity fe : entities) {
			fe.replaceNameSegment(oldFilePath, newName);
			try {
				fileEntityDao.save(fe);
				ViewUtil.addMessage("File Renamed", fe.getFileName(), null);
			} catch (Exception e) {
				e.printStackTrace();
				ViewUtil.addMessage("Error on Renaming a File!!!", fe.getFileName() + " :: " + e.getLocalizedMessage(), FacesMessage.SEVERITY_ERROR);
			}
		}
	}
	
	public void moveFile(FileListBean fileListBean) {
		final int srcNodeId = fileListBean.getSrcNodeId();
		final String filesHomePath = System.getProperty(AppOptions.FILE_BOX_HOME);
		final FilePathBean srcPathBean = fileListBean.getFilePathBean(srcNodeId);
		final String oldFilePath = srcPathBean.getFullPath();
		final int dstNodeId = fileListBean.getDstNodeId();
		final FilePathBean dstPathBean = fileListBean.getFolderPathBean(dstNodeId);
		final String newParentPath = dstPathBean.getFullPath();
		List<FileEntity> entities = Collections.emptyList();
		
		if (srcPathBean.isFolder()) {
			final File oldDir = srcPathBean.toFile(filesHomePath);
			final FileEntity fe = new FileEntity(newParentPath + "/" + srcPathBean.getName());
			final File newDir = fe.toFile(filesHomePath);
			
			if (oldDir.equals(newDir)) {
				ViewUtil.addMessage("No Effect!!!", fe.getFileName() + " :: The source and destination are identical", FacesMessage.SEVERITY_WARN);
				return;
			}
			if (newDir.getAbsolutePath().startsWith(oldDir.getAbsolutePath()+File.separator)) {
				ViewUtil.addMessage("Error on Moving a Folder!!!", fe.getFileName() + " :: The source is a subdirectory of the destination", FacesMessage.SEVERITY_ERROR);
				return;
			}
			
			try {
				FileUtils.moveDirectory(oldDir, newDir);
			} catch (IOException e) {
				e.printStackTrace();
				ViewUtil.addMessage("Error on Moving a Folder!!!", fe.getFileName() + " :: " + e.getLocalizedMessage(), FacesMessage.SEVERITY_ERROR);
				return;
			}
			
			entities = fileEntityDao.findByNamePattern(oldFilePath+File.separator+'%');
		}
		else {
			final File oldFile = srcPathBean.toFile(filesHomePath);
			final FileEntity fe = new FileEntity(newParentPath + "/" + srcPathBean.getName());
			final File newFile = fe.toFile(filesHomePath);
			
			if (oldFile.equals(newFile)) {
				ViewUtil.addMessage("Error on Moving a File!!!", fe.getFileName() + " :: The destination already exists", FacesMessage.SEVERITY_ERROR);
				return;
			}
			
			try {
				FileUtils.moveFile(oldFile, newFile);
			} catch (IOException e) {
				e.printStackTrace();
				ViewUtil.addMessage("Error on Moving a File!!!", fe.getFileName() + " :: " + e.getLocalizedMessage(), FacesMessage.SEVERITY_ERROR);
				return;
			}
			
			FileEntity entity = fileEntityDao.findByName(oldFilePath);
			if (entity != null) {
				entities = new ArrayList<>();
				entities.add(entity);
			}
		}
		
		for (FileEntity fe : entities) {
			if (srcPathBean.isFolder()) {
				fe.setFileName(newParentPath + "/" + srcPathBean.getName() + fe.getFileName().replace(oldFilePath, ""));
			}
			else {
				fe.setFileName(newParentPath + "/" + fe.endName());
			}
			
			try {
				fileEntityDao.save(fe);
				ViewUtil.addMessage("File Moved", fe.getFileName(), null);
			} catch (Exception e) {
				e.printStackTrace();
				ViewUtil.addMessage("Error on Moving a File!!!", fe.getFileName() + " :: " + e.getLocalizedMessage(), FacesMessage.SEVERITY_ERROR);
			}
		}
	}

	public void deleteFile(FileListBean fileListBean) {
		final int srcNodeId = fileListBean.getSrcNodeId();
		final FilePathBean filePathBean = fileListBean.getFilePathBean(srcNodeId);
		
		final String filesHomePath = System.getProperty(AppOptions.FILE_BOX_HOME);
		final String filePath = filePathBean.getFullPath();
		List<FileEntity> entities = Collections.emptyList();
		
		if (filePathBean.isFolder()) {
			entities = fileEntityDao.findByNamePattern(filePath+File.separator+'%');
		}
		else {
			FileEntity entity = fileEntityDao.findByName(filePath);
			if (entity != null) {
				entities = new ArrayList<>();
				entities.add(entity);
			}
		}
		
		FileUtils.deleteQuietly(filePathBean.toFile(filesHomePath));
		
		for (FileEntity fe : entities) {
			fileEntityDao.delete(fe);
			ViewUtil.addMessage("Files Deleted", fe.getFileName(), null);
		}
	}
	
	public boolean saveUserProfile() {
	    UserProfileBean upb = ViewUtil.findBean("userProfileBean");
	    boolean ok = false;
	    try {
            UserDetailsService.saveCustomCredential(upb.getUserName(), upb.getPassword());
            ok = true;
        } catch (IOException e) {
            ViewUtil.addMessage("Error on Saving User Profile!!!", e.getLocalizedMessage(), FacesMessage.SEVERITY_ERROR);
            e.printStackTrace();
        }
	    return ok;
	}
	
    @RequestMapping(value = "/fragment/{fragmentId}", method = { RequestMethod.GET })
    public String onRequestForFragment(ModelMap model, @PathVariable Long fragmentId) {
    	final Fragment frg = fragmentDao.findById(fragmentId, true, true);
    	final FragmentBean fb = newFragmentBean(frg, null, null);
    	model.addAttribute("fragmentBean", fb);
    	return "fragment";
    }

    @RequestMapping(value = "/locale/{locale}", method = { RequestMethod.GET })
    public String onRequestForLocale(@PathVariable String locale, HttpServletResponse response) {
    	Cookie cookie = new Cookie(REQUEST_PARAM_LOCALE, locale);
        response.addCookie(cookie);
    	return "redirect:/app/home?locale=" + locale;
    }

    @RequestMapping(value = "/dev/export_db_as_script", method = { RequestMethod.GET })
    public String onExportDbAsScript() {
        final String url = "redirect:/app/home";
        if (!Configurator.isTrue(AppOptions.DEV))
            return url;
        final String tmpPath = System.getProperty(AppOptions.TEMP_PATH);
        FsUtil.createUnexistingDirectory(new File(tmpPath));
        final String outputPath = tmpPath  + File.separator + "exported.sql";
        fragmentDao.exportDbAsScript(outputPath);
        String postShellScript = System.getProperty("civilizer.exp_db_post_ss");
        postShellScript = FsUtil.getAbsolutePath(postShellScript, null);
        if (postShellScript != null && !postShellScript.isEmpty()) {
            try {
                Runtime.getRuntime().exec(postShellScript);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return url;
    }

}
