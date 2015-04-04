package com.civilizer.test.web;

import static org.junit.Assert.*;

import java.util.*;
import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileExistsException;
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
	
	private static FileEntityDao fileEntityDao;
	
	static String filesHomePath;
	
	@BeforeClass
    public static void setUpBeforeClass() throws Exception {
		TestUtil.configure();
		
		filesHomePath = System.getProperty(AppOptions.UPLOADED_FILES_HOME);
		
		renewTestData();
	}
	
	@AfterClass
    public static void tearDownAfterClass() throws Exception {
		TestUtil.unconfigure();
    }
	
	@Before
    public void setUp() throws Exception {
    }
	
	@After
	public void tearDown() throws Exception {
	}
	
	private static void renewTestData() {
		GenericXmlApplicationContext ctx = new GenericXmlApplicationContext();
		ctx.load("classpath:datasource-context-h2-embedded.xml");
		ctx.refresh();
		
		fileEntityDao = ctx.getBean("fileEntityDao", FileEntityDao.class);
		assertNotNull(fileEntityDao);
		
		try {
			FileUtils.deleteDirectory(new File(filesHomePath));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		TestUtil.touchTestFilesForFileBox(fileEntityDao);
	}
	
	private static int getRandomFilePathId(FilePathTree filePathTree, boolean forFolder) {
		final List<FilePathBean> filePathBeans = filePathTree.getFilePathBeans();
		int output = 0;
		for (int i=0; i<filePathBeans.size(); ++i) {
			final FilePathBean filePathBean = filePathBeans.get(i);
			if (filePathBean.isBroken())
				continue;
			if (TestUtil.getRandom().nextInt(3) != 0)
				continue;
			if (filePathBean.isFolder()) {
				if (forFolder) {
					output = i;
					break;
				}
			}
			else {
				if (! forFolder) {
					output = i;
					break;
				}
			}
		}
		while (output == 0) {
			output = getRandomFilePathId(filePathTree, forFolder);
		}
		return output;
	}

	@Test
	public void testConstructPathTree() {
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
		final List<FileEntity> fileEntities = fileEntityDao.findAll();
		
		final FileListBean fileListBean = new FileListBean();
		fileListBean.setFileEntities(fileEntities);
		
		FilePathTree filePathTree = new FilePathTree();
		fileListBean.setFilePathTree(filePathTree);
		
		for (int j=0; j<3; ++j) {
			final int parentFolderId = getRandomFilePathId(filePathTree, true);
			final FilePathBean parentPath = fileListBean.getFilePathBean(parentFolderId);
			assertEquals(true, parentPath.isFolder());

			final String newFolderName = "new-directory";
			final File newDir = fileListBean.createNewFolder(parentFolderId, newFolderName, filesHomePath);
			assertNotNull(newDir);
			assertEquals(true, newDir.isDirectory());
			
			try {
				boolean contained = FileUtils.directoryContains(new File(filesHomePath+File.separatorChar+parentPath.getFullPath()), newDir);
				assertEquals(true, contained);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
			try {
				FileUtils.forceDeleteOnExit(newDir);
//				System.out.println("***** " + newDir);
			} catch (IOException e) {
				e.printStackTrace();
			}

			filePathTree = new FilePathTree();
			fileListBean.setFilePathTree(filePathTree);
			final FilePathBean tmp = new FilePathBean(newDir.getAbsolutePath().replace(filesHomePath, ""));
			assertEquals(true, filePathTree.getFilePathBeans().contains(tmp));
		}
	}
	
	@Test
	public void testRenameFiles() {
		testCreateNewDirectory();
		
		final List<FileEntity> fileEntities = fileEntityDao.findAll();
		
		final FileListBean fileListBean = new FileListBean();
		fileListBean.setFileEntities(fileEntities);
		
		FilePathTree filePathTree = new FilePathTree();
		fileListBean.setFilePathTree(filePathTree);
		
		for (int j=0; j<2; ++j) {
			final boolean forFolder = TestUtil.getRandom().nextBoolean();
			final int srcNodeId = getRandomFilePathId(filePathTree, forFolder);
			final String newName = forFolder ?
					"renamed-folder"+j : "renamed-file"+j+".txt";
			final FilePathBean filePathBean = fileListBean.getFilePathBean(srcNodeId);
			assertEquals(forFolder, filePathBean.isFolder());
			if (filePathBean.getName().equals("")) {
				// can't rename the root directory
				--j;
				continue;
			}
			final String oldFilePath = filePathBean.getFullPath();			
			List<FileEntity> entities = Collections.emptyList();
			
			if (filePathBean.isFolder()) {
				final File oldDir = filePathBean.toFile(filesHomePath);
				final FileEntity fe = new FileEntity(oldFilePath);
				fe.replaceNameSegment(oldFilePath, newName);
				final File newDir = fe.toFile(filesHomePath);
				assertEquals(oldDir.getParent(), newDir.getParent());
				
				try {
					FileUtils.moveDirectory(oldDir, newDir);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				entities = fileEntityDao.findByNamePattern(oldFilePath + '%');
			}
			else {
				final File oldFile = filePathBean.toFile(filesHomePath);
				final FileEntity fe = new FileEntity(oldFilePath);
				fe.replaceNameSegment(oldFilePath, newName);
				final File newFile = fe.toFile(filesHomePath);
//				System.out.println("***** " + oldFile);
//				System.out.println("***** " + newFile);
				assertEquals(oldFile.getParent(), newFile.getParent());
				
				try {
					FileUtils.moveFile(oldFile, newFile);
				} catch (IOException e) {
					e.printStackTrace();
				}
				assertEquals(false, oldFile.isFile());
				assertEquals(true, newFile.isFile());
				
				FileEntity entity = fileEntityDao.findByName(oldFilePath);
				if (entity != null) {
					entities = new ArrayList<>();
					entities.add(entity);
				}
			}
			
			for (FileEntity fe : entities) {
				fe.replaceNameSegment(oldFilePath, newName);
				fileEntityDao.save(fe);
			}
			
			filePathTree = new FilePathTree();
			fileListBean.setFilePathTree(filePathTree);
		}
		
		// file structures for test are heavily modified. refresh it.
		renewTestData();
	}
	
	@Test
	public void testMoveFiles() {
		final List<FileEntity> fileEntities = fileEntityDao.findAll();
		
		final FileListBean fileListBean = new FileListBean();
		fileListBean.setFileEntities(fileEntities);
		
		FilePathTree filePathTree = new FilePathTree();
		fileListBean.setFilePathTree(filePathTree);
		
		for (int j=0; j<2; ++j) {
			final boolean forFolder = TestUtil.getRandom().nextBoolean();
			final int srcNodeId = getRandomFilePathId(filePathTree, forFolder);
			final FilePathBean srcPathBean = fileListBean.getFilePathBean(srcNodeId);
			assertEquals(forFolder, srcPathBean.isFolder());
			if (srcPathBean.getName().equals("")) {
				// can't move the root directory
				--j;
				continue;
			}
			final String oldFilePath = srcPathBean.getFullPath();
			final int dstNodeId = getRandomFilePathId(filePathTree, true);
			final FilePathBean dstPathBean = fileListBean.getFilePathBean(dstNodeId);
			assertEquals(true, dstPathBean.isFolder());
			final String newParentPath = dstPathBean.getFullPath();
			List<FileEntity> entities = Collections.emptyList();
			
			if (srcPathBean.isFolder()) {
				final File oldDir = srcPathBean.toFile(filesHomePath);
				final FileEntity fe = new FileEntity(newParentPath + "/" + srcPathBean.getName());
				final File newDir = fe.toFile(filesHomePath);
				
				if (oldDir.equals(newDir)) {
					// the source and destination are identical
					--j;
					continue;
				}
				
				if (newDir.getAbsolutePath().startsWith(oldDir.getAbsolutePath())) {
					// the source is a subdirectory of the destination
					--j;
					continue;
				}
				
				System.out.println("***** " + oldDir);
				System.out.println("***** " + newDir);
				
				try {
					FileUtils.moveDirectory(oldDir, newDir);
				} catch (FileExistsException e) {
					// the destination already exists
					--j;
					continue;
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				assertEquals(false, oldDir.isDirectory());
				assertEquals(true, newDir.isDirectory());
				
				entities = fileEntityDao.findByNamePattern(oldFilePath + '%');
			}
			else {
				final File oldFile = srcPathBean.toFile(filesHomePath);
				final FileEntity fe = new FileEntity(newParentPath + "/" + srcPathBean.getName());
				final File newFile = fe.toFile(filesHomePath);
				
				if (oldFile.equals(newFile)) {
					// source and destination are identical
					--j;
					continue;
				}
				
				System.out.println("***** " + oldFile);
				System.out.println("***** " + newFile);
				
				try {
					FileUtils.moveFile(oldFile, newFile);
				} catch (FileExistsException e) {
					// the destination already exists
					--j;
					continue;
				} catch (IOException e) {
					e.printStackTrace();
				}
				assertEquals(false, oldFile.isFile());
				assertEquals(true, newFile.isFile());
				
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
				System.out.println("----- " + fe);
				fileEntityDao.save(fe);
			}
			
			filePathTree = new FilePathTree();
			fileListBean.setFilePathTree(filePathTree);
		}
		
		// file structures for test are heavily modified. refresh it.
		renewTestData();
	}
	
	@Test
	public void testDeleteFiles() {
		final List<FileEntity> fileEntities = fileEntityDao.findAll();
		
		final FileListBean fileListBean = new FileListBean();
		fileListBean.setFileEntities(fileEntities);
		
		FilePathTree filePathTree = new FilePathTree();
		fileListBean.setFilePathTree(filePathTree);
		
		for (int j=0; j<2; ++j) {
			final boolean forFolder = TestUtil.getRandom().nextBoolean();
			final int srcNodeId = getRandomFilePathId(filePathTree, forFolder);
			final FilePathBean filePathBean = fileListBean.getFilePathBean(srcNodeId);
			if (filePathBean.getName().equals("")) {
				// can't delete the root directory
				--j;
				continue;
			}
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
				final String pathOnFileSystem = filesHomePath + fe.getFileName();
				try {
					assertEquals(false, new File(pathOnFileSystem).exists());
					fileEntityDao.delete(fe);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			filePathTree = new FilePathTree();
			fileListBean.setFilePathTree(filePathTree);
		}
		
		// file structures for test are heavily modified. refresh it.
		renewTestData();
	}

}
