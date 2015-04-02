package com.civilizer.test.web;

import static org.junit.Assert.*;

import java.util.*;
import java.io.File;

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
import com.civilizer.web.view.FilePathBean;
import com.civilizer.web.view.FilePathTree;

public class WebFileBoxTest {
	
	private static GenericXmlApplicationContext ctx;
	private FileEntityDao fileEntityDao;
	
	@BeforeClass
    public static void setUpBeforeClass() throws Exception {
		TestUtil.configure();
		ctx = new GenericXmlApplicationContext();
		ctx.load("classpath:datasource-context-h2-embedded.xml");
		ctx.refresh();
	}
	
	@AfterClass
    public static void tearDownAfterClass() throws Exception {
		TestUtil.unconfigure();
    }
	
	@Before
    public void setUp() throws Exception {
		fileEntityDao = ctx.getBean("fileEntityDao", FileEntityDao.class);
		assertNotNull(fileEntityDao);
		
		TestUtil.touchTestFilesForFileBox(fileEntityDao);
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

}
