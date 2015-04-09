package com.civilizer.web.controller;

import java.io.File;
import java.io.IOException;
import java.util.*;

import javax.faces.application.FacesMessage;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
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
    
    @SuppressWarnings("unused")
	private final Logger logger = LoggerFactory.getLogger(MainController.class);
    
//    private final Gson gson = new GsonBuilder().registerTypeAdapter(DateTime.class, new JodaDateTimeConverter()).create();
    
	@Autowired
	private FragmentDao fragmentDao;

	@Autowired
	private TagDao tagDao;
	
	@Autowired
	private FileEntityDao fileEntityDao;
	
	// [TODO] refactor code to maintain special tags
	private Tag trashcanTag;

	private Tag bookmarkTag;

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
	
	public void populateTagBean(TagBean tagBean) {
		// [TODO] replace the stub code with implementation
		System.out.println("***** " + tagBean.getTag().getId());
	}
	
	public SpecialTagBean newBookmarkTagBean() {
		final SpecialTagBean tagBean = new SpecialTagBean();
		
		final Tag tag = getBookmarkTag();
		tagBean.setTag(tag);
		
		final List<Fragment> fragments = fragmentDao.findByTagId(tag.getId(), false);
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
	
	public SearchContextBean[] newSearchContextBeans() {
		SearchContextBean[] output = { new SearchContextBean(0), new SearchContextBean(1), new SearchContextBean(2) };
		return output;
	}

	public SearchContextBean getSearchContextBean(List<SearchContextBean> beans, int panelId) {
		return beans.get(panelId);
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
	    	frg = fragmentDao.findById(frg.getId());
	    	frg.setContent(content);
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
	
	public void saveTag(TagBean tb, TagListBean tagListBean) {
		Tag t = tb.getTag();
		if (t.getId() == null) {
			// a new tag
			try {
				tagDao.save(t);
				ViewUtil.addMessage("Created", "Tag : " + t.getTagName(), null);
			}
			catch (Exception e) {
				e.printStackTrace();
				ViewUtil.addMessage("Error on creating a tag!!!", e.getLocalizedMessage(), FacesMessage.SEVERITY_ERROR);
			}
			
		}
		else {
			// an existing tag
			final String newName = t.getTagName();
			t = Tag.getTagFromId(t.getId(), tagListBean.getTags());
			final String oldName = t.getTagName();
			t.setTagName(newName);
			try {
				tagDao.save(t);
				ViewUtil.addMessage("Renamed", "Tag : " + oldName + " => " + newName, null);
			}
			catch (Exception e) {
				e.printStackTrace();
				ViewUtil.addMessage("Error on renaming a tag!!!", e.getLocalizedMessage(), FacesMessage.SEVERITY_ERROR);
			}
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
