package com.civilizer.test.web;

import static org.junit.Assert.*;

import java.util.*;
import java.util.logging.FileHandler;
import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.junit.*;
import org.springframework.context.support.GenericXmlApplicationContext;

import com.civilizer.config.AppOptions;
import com.civilizer.dao.FileEntityDao;
import com.civilizer.domain.DefaultTreeNode;
import com.civilizer.domain.FileEntity;
import com.civilizer.domain.TreeNode;
import com.civilizer.test.util.TestUtil;
import com.civilizer.web.view.FileListBean;
import com.civilizer.web.view.FilePathBean;
import com.civilizer.web.view.FilePathTree;

public class WebFileBoxTest {
	
	private static GenericXmlApplicationContext ctx;
	private static FileEntityDao fileEntityDao;
	
	@BeforeClass
    public static void setUpBeforeClass() throws Exception {
		TestUtil.configure();
		ctx = new GenericXmlApplicationContext();
		ctx.load("classpath:datasource-context-h2-embedded.xml");
		ctx.refresh();
		
		fileEntityDao = ctx.getBean("fileEntityDao", FileEntityDao.class);
		assertNotNull(fileEntityDao);
		
		TestUtil.touchTestFilesForFileBox(fileEntityDao);
	}
	
	@AfterClass
    public static void tearDownAfterClass() throws Exception {
		TestUtil.unconfigure();
    }
	
	@Before
    public void setUp() throws Exception {
    }

	@Test
	public void testConstructPathTree() {
		final String filesHomePath = System.getProperty(AppOptions.UPLOADED_FILES_HOME);
		Collection<File> dirs = FileUtils.listFilesAndDirs(
				new File(filesHomePath),  // directory
				FalseFileFilter.INSTANCE, // exclude all files
				TrueFileFilter.INSTANCE   // include all sub directories
		);
		
		final TreeNode<FilePathBean> pathTree = new DefaultTreeNode<>(new FilePathBean("", ""));
		
		for (File file : dirs) {
			String path = file.toString().replace(filesHomePath, "");
			FilePathTree.addToPathTree(pathTree, path);
		}
		
		FilePathBean[] filePathBeans = pathTree.toDataArray(new FilePathBean[]{}, TreeNode.TraverseOrder.BREATH_FIRST);
		assertEquals(dirs.size(), filePathBeans.length);
		for (FilePathBean filePathBean : filePathBeans) {
			assertEquals(true, dirs.contains(new File(filesHomePath + File.separatorChar + filePathBean.getFullPath())));
//			System.out.println("***** " + filePathBean.getFullPath());
		}
		
		final List<FileEntity> fileEntities = fileEntityDao.findAll();
		for (FileEntity fileEntity : fileEntities) {
			assertEquals(true, FilePathTree.addFileEntityToPathTree(pathTree, fileEntity));
		}
		
		filePathBeans = pathTree.toDataArray(new FilePathBean[]{}, TreeNode.TraverseOrder.BREATH_FIRST);
		for (FilePathBean filePathBean : filePathBeans) {
			assertEquals(true, new File(filesHomePath + File.separatorChar + filePathBean.getFullPath()).exists());
//			System.out.println("***** " + filePathBean.getFullPath());
		}
	}
	
	@Test
	public void testMethod_FilePathTree_populateNodes() {
		final List<FileEntity> fileEntities = fileEntityDao.findAll();
		final FilePathTree filePathTree = new FilePathTree();
		filePathTree.populateNodes(fileEntities);
		assertNotNull(filePathTree.getRoot());
		assertEquals(true, filePathTree.getRoot().getChildCount() > 0);
		
		final List<FilePathBean> filePathBeans = filePathTree.getFilePathBeans();
		assertEquals(false, filePathBeans.isEmpty());
		for (int i=0; i<filePathBeans.size(); ++i) {
			final FilePathBean filePathBean = filePathBeans.get(i);
			assertEquals(i, filePathBean.getId());
			assertEquals(false, filePathBean.isBroken());
//			System.out.println("***** " + filePathBean.getFullPath());
		}
	}
	
	@Test
	public void testCreateNewDirectory() {
		final String filesHomePath = System.getProperty(AppOptions.UPLOADED_FILES_HOME);
		
		final List<FileEntity> fileEntities = fileEntityDao.findAll();
		
		final FileListBean fileListBean = new FileListBean();
		fileListBean.setFileEntities(fileEntities);
		
		FilePathTree filePathTree = new FilePathTree();
		fileListBean.setFilePathTree(filePathTree);
		
		final List<FilePathBean> filePathBeans = filePathTree.getFilePathBeans();
		int parentFolderId = 0;
		for (int i=0; i<filePathBeans.size(); ++i) {
			final FilePathBean filePathBean = filePathBeans.get(i);
			if (! filePathBean.isBroken() &&  filePathBean.isFolder() && TestUtil.getRandom().nextBoolean()) {
				parentFolderId = i;
				break;
			}
		}

		final String newFolderName = "new-directory";
		final File dir = fileListBean.createNewFolder(parentFolderId, newFolderName, filesHomePath);
		assertNotNull(dir);
		assertEquals(true, dir.isDirectory());
		try {
			FileUtils.forceDeleteOnExit(dir);
//			System.out.println("***** " + dir);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		filePathTree = new FilePathTree();
		fileListBean.setFilePathTree(filePathTree);
		final FilePathBean tmp = new FilePathBean(dir.getAbsolutePath().replace(filesHomePath, ""));
		assertEquals(true, filePathTree.getFilePathBeans().contains(tmp));
	}

}
