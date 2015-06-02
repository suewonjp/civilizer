package com.civilizer.web.controller;

import java.io.File;
import java.io.IOException;
import java.util.*;

import javax.faces.application.FacesMessage;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
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
import com.civilizer.dao.FileEntityDao;
import com.civilizer.dao.FragmentDao;
import com.civilizer.dao.TagDao;
import com.civilizer.domain.FileEntity;
import com.civilizer.domain.Fragment;
import com.civilizer.domain.FragmentOrder;
import com.civilizer.domain.SearchParams;
import com.civilizer.domain.Tag;
import com.civilizer.domain.TextDecorator;
import com.civilizer.web.view.*;

@Controller
@Component("mainController")
public final class MainController {
	
	// [DEV]
	private static final String DEVELOPMENT_MESSAGE_CLIENT_ID = "fragment-group-form:development-messages";
	
	private static final int    MAX_FRAGMENT_PANELS = 3;
	private static final String REQUEST_PARAM_LOCALE = "locale";
    
//    @SuppressWarnings("unused")
//	private final Logger logger = LoggerFactory.getLogger(MainController.class);
    
//    private final Gson gson = new GsonBuilder().registerTypeAdapter(DateTime.class, new JodaDateTimeConverter()).create();
    
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
	
	private Tag getSpecialTag(String name) {
		return tagDao.findById((long) Tag.getSpecialTagId(name));
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
        }
        else if (tagId == Tag.TRASH_TAG_ID) {
            // Fetch the trashed fragments
            fragments = fragmentDao.findSomeByTagId(tagId, first, count + 1, frgOrder, asc);
            allCount = fragmentDao.countByTagAndItsDescendants(tagId, true, tagDao);
        }
        else if (tagId != PanelContextBean.EMPTY_TAG) {
        	// Fetch the fragments with the specified tag (non-trashed)
        	fragments = fragmentDao.findSomeNonTrashedByTagId(tagId, first, count + 1, frgOrder, asc, tagDao);
        	allCount = fragmentDao.countByTagAndItsDescendants(tagId, false, tagDao);
        }
        else if (sp != null) {
            // Fetch the fragments by the search parameters
            fragments = fragmentDao.findBySearchParams(sp);
            allCount = fragments.size();
            if (allCount == 0)
                sp = null; // no search hit so no need to record any info to the context;
            else
                fragments = Fragment.paginate(fragments, first, count + 1, frgOrder, asc);
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
        	fragmentBeans.add(newFragmentBean(f, sp));
        }
       	if (fragmentBeans.isEmpty()) {
       		fragmentBeans = Collections.emptyList();
       	}
        flb.setFragmentBeans(fragmentBeans);
        
        return flb;
    }

	public FragmentBean newFragmentBean(Fragment f, SearchParams sp) {
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
		    final String name = Tag.SPECIAL_TAG_NAMES[-(int)tagId];
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
	
	public SpecialTagBean newBookmarkTagBean() {
		final SpecialTagBean tagBean = new SpecialTagBean();
		
		final Tag tag = getBookmarkTag();
		tagBean.setTag(tag);
		
		final List<Fragment> fragments = fragmentDao.findByTagId(tag.getId(), false);
		Fragment.sort(fragments, FragmentOrder.TITLE, true);
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
	    frg.removeTag(getBookmarkTag());
	    try {
			fragmentDao.save(frg);
			ViewUtil.addMessage("Unbookmarked", "Fragment #" + frg.getId(), null);
		}
	    catch (Exception e) {
	    	e.printStackTrace();
			ViewUtil.addMessage("Error on unbookmarking!!!", e.getLocalizedMessage(), FacesMessage.SEVERITY_ERROR);
		}
	}

	private void trashFragment(Long fragmentId) {
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

	private void restoreFragment(Long fragmentId) {
	    final Fragment frg = fragmentDao.findById(fragmentId, true, false);
	    final Tag trashcanTag = getTrashcanTag();
	    if (! frg.containsTagId(trashcanTag.getId())) {
	        return;
	    }
	    frg.removeTag(trashcanTag);
	    try {
	        fragmentDao.save(frg);
	        ViewUtil.addMessage("Restored", "Fragment #" + frg.getId(), null);
	    }
	    catch (Exception e) {
	        e.printStackTrace();
	        ViewUtil.addMessage("Error on restorng a fragment!!!", e.getLocalizedMessage(), FacesMessage.SEVERITY_ERROR);
	    }
	}

	public void trashFragment(FragmentBean fb) {
		trashFragment(fb.getFragment().getId());
	}

	public void restoreFragment(FragmentBean fb) {
	    restoreFragment(fb.getFragment().getId());
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

	private void deleteFragment(Long fragmentId) {
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
		return output;
	}
	
	public void saveTag(TagBean tagBean, TagListBean tagListBean) {
		final TagBean tagToEdit = tagListBean.getTagToEdit();
		final Tag t = tagToEdit.getTag();
		final String oldName = t.getTagName();
		final String newName = tagBean.getTag().getTagName();
		if (newName.isEmpty()) {
			ViewUtil.addMessage("Error on updating a tag!!!", "An empty tag name is not allowed!", FacesMessage.SEVERITY_ERROR);
			return;
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
			ViewUtil.addMessage("Updated", "Tag : " + oldName + " => " + newName, null);
		}
		catch (Exception e) {
			e.printStackTrace();
			ViewUtil.addMessage("Error on updating a tag!!!", e.getLocalizedMessage(), FacesMessage.SEVERITY_ERROR);
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
			try {
				tagDao.delete(t);
				ViewUtil.addMessage("Deleted", "Tag : " + t.getTagName(), null);
			}
			catch (Exception e) {
				e.printStackTrace();
				ViewUtil.addMessage("Error on deleting a tag!!!", e.getLocalizedMessage(), FacesMessage.SEVERITY_ERROR);
			}
		}
	}
	
	public void relateFragments(int fromId, int toId) {
		try {
			fragmentDao.relateFragments(fromId, toId);
			ViewUtil.addMessage("Related", "Fragments : " + fromId + " <==> " + toId, null);
		}
		catch (Exception e) {
			e.printStackTrace();
			ViewUtil.addMessage("Error on relating fragments!!!", e.getLocalizedMessage(), FacesMessage.SEVERITY_ERROR);
		}
	}

	public void unrelateFragments(int fromId, int toId) {
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
			}
			
			entities = fileEntityDao.findByNamePattern(oldFilePath + '%');
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
			if (newDir.getAbsolutePath().startsWith(oldDir.getAbsolutePath())) {
				ViewUtil.addMessage("Error on Moving a Folder!!!", fe.getFileName() + " :: The source is a subdirectory of the destination", FacesMessage.SEVERITY_ERROR);
				return;
			}
			
			try {
				FileUtils.moveDirectory(oldDir, newDir);
			} catch (IOException e) {
				e.printStackTrace();
				ViewUtil.addMessage("Error on Moving a Folder!!!", fe.getFileName() + " :: " + e.getLocalizedMessage(), FacesMessage.SEVERITY_ERROR);
			}
			
			entities = fileEntityDao.findByNamePattern(oldFilePath + '%');
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
			entities = fileEntityDao.findByNamePattern(filePath + '%');
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
			try {
				fileEntityDao.delete(fe);
				ViewUtil.addMessage("Files Deleted", fe.getFileName(), null);
			} catch (Exception e) {
				e.printStackTrace();
				ViewUtil.addMessage("Error on Deleting Files!!!", fe.getFileName() + " :: " + e.getLocalizedMessage(), FacesMessage.SEVERITY_ERROR);
			}
		}
	}
	
    @RequestMapping(value = "/fragment/{fragmentId}", method = { RequestMethod.GET })
    public String onRequestForFragment(ModelMap model, @PathVariable Long fragmentId) {
    	final Fragment frg = fragmentDao.findById(fragmentId, true, true);
    	final FragmentBean fb = newFragmentBean(frg, null);
    	model.addAttribute("fragmentBean", fb);
    	return "fragment";
    }

    @RequestMapping(value = "/locale/{locale}", method = { RequestMethod.GET })
    public String onRequestForLocale(@PathVariable String locale, HttpServletResponse response) {
    	Cookie cookie = new Cookie(REQUEST_PARAM_LOCALE, locale);
        response.addCookie(cookie);
    	return "redirect:/app/home?locale=" + locale;
    }

}
