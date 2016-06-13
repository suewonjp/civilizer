package com.civilizer.test.web;

import static org.junit.Assert.*;

import org.junit.*;

import java.util.*;
import java.io.*;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.springframework.context.support.GenericXmlApplicationContext;

import com.civilizer.config.AppOptions;
import com.civilizer.dao.FileEntityDao;
import com.civilizer.domain.FileEntity;
import com.civilizer.test.helper.TestUtil;
import com.civilizer.utils.DefaultTreeNode;
import com.civilizer.utils.FsUtil;
import com.civilizer.utils.Pair;
import com.civilizer.utils.TreeNode;
import com.civilizer.web.view.FileListBean;
import com.civilizer.web.view.FilePathBean;
import com.civilizer.web.view.FilePathTree;

public class WebFileBoxTest {
    
    private static GenericXmlApplicationContext ctx;    
    private static FileEntityDao fileEntityDao;    
    static String filesHomePath;
    
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        TestUtil.configure();        
        filesHomePath = System.getProperty(AppOptions.FILE_BOX_HOME);        
        renewTestData();
    }
    
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        ctx.close();
        TestUtil.unconfigure();
    }
    
    private static void renewTestData() {
        ctx = new GenericXmlApplicationContext();
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

    private static int getRandomFilePathId(List<FilePathBean> filePathBeans, boolean forFolder, boolean excludeRoot) {
        assertEquals(false, filePathBeans.isEmpty());
        List<Integer> indices = new ArrayList<>();
        for (int i=0; i<filePathBeans.size(); ++i) {
            final FilePathBean filePathBean = filePathBeans.get(i);
            if (filePathBean.isBroken())
                continue;
            if (filePathBean.isFolder()) {
                if (excludeRoot && filePathBean.getName().equals(""))
                    continue;
                if (forFolder)
                    indices.add(i);
            }
            else {
                if (! forFolder)
                    indices.add(i);
            }
        }
        assertEquals(false, indices.isEmpty());
        return indices.get(TestUtil.getRandom().nextInt(indices.size()));
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
            String path = FsUtil.normalizePath(file.toString()).replace(filesHomePath, "");
            FilePathTree.addToPathTree(pathTree, path);
        }
        
        FilePathBean[] filePathBeans = pathTree.toDataArray(new FilePathBean[]{}, TreeNode.TraverseOrder.BREATH_FIRST);
        assertEquals(dirs.size(), filePathBeans.length);
        for (FilePathBean filePathBean : filePathBeans) {
            assertEquals(true, dirs.contains(new File(FsUtil.concatPath(filesHomePath, filePathBean.getFullPath()))));
        }
        
        for (FileEntity f: fileEntityDao.findAll()) {
            assertEquals(true, FilePathTree.addFileEntityToPathTree(pathTree, f));
        }
        
        filePathBeans = pathTree.toDataArray(new FilePathBean[]{}, TreeNode.TraverseOrder.BREATH_FIRST);
        for (FilePathBean filePathBean : filePathBeans) {
            assertEquals(true, new File(FsUtil.concatPath(filesHomePath, filePathBean.getFullPath())).exists());
        }
    }
    
    @Test
    public void testMethod_FilePathTree_populateNodes() {
        final FilePathTree filePathTree = new FilePathTree();
        filePathTree.populateNodes(fileEntityDao.findAll());
        assertNotNull(filePathTree.getRoot());
        assertEquals(true, filePathTree.getRoot().getChildCount() > 0);
        
        final List<FilePathBean> filePathBeans = filePathTree.getFilePathBeans();
        assertEquals(false, filePathBeans.isEmpty());
        for (int i=0; i<filePathBeans.size(); ++i) {
            final FilePathBean filePathBean = filePathBeans.get(i);
            assertEquals(i, filePathBean.getId());
            assertEquals(false, filePathBean.isBroken());
        }
    }
    
    @Test
    public void testCreateNewDirectory() {
        final FileListBean fileListBean = new FileListBean();
        fileListBean.setFileEntities(fileEntityDao.findAll());
        FilePathTree filePathTree = new FilePathTree();
        fileListBean.setFilePathTree(filePathTree);
        
        final int parentFolderId = getRandomFilePathId(filePathTree.getFilePathBeans(), true, false);
        final FilePathBean parentPath = fileListBean.getFilePathBean(parentFolderId);
        assertEquals(true, parentPath.isFolder());

        final String newFolderName = "new-directory";
        Pair<File, String> tmp = fileListBean.createNewFolder(parentFolderId, newFolderName, filesHomePath);
        assertNotNull(tmp);
        final File newDir = tmp.getFirst();
        assertNotNull(newDir);
        assertNotNull(tmp.getSecond());
        assertEquals(FsUtil.normalizePath(newDir.getAbsolutePath()), FsUtil.concatPath(filesHomePath, tmp.getSecond()));
        assertEquals(true, newDir.isDirectory());
        
        try {
            assertEquals(true, 
                FileUtils.directoryContains(new File(FsUtil.concatPath(filesHomePath, parentPath.getFullPath())), newDir));
            FileUtils.forceDeleteOnExit(newDir);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }
    
    @Test
    public void testRenameFiles() {
        testCreateNewDirectory();
        
        final FileListBean fileListBean = new FileListBean();
        fileListBean.setFileEntities(fileEntityDao.findAll());
        FilePathTree filePathTree = new FilePathTree();
        fileListBean.setFilePathTree(filePathTree);
        
        final boolean forFolder = TestUtil.getRandom().nextBoolean();
        final int srcNodeId = getRandomFilePathId(filePathTree.getFilePathBeans(), forFolder, true);
        final String newName = forFolder ? "renamed-folder" : "renamed-file.txt";
        final FilePathBean filePathBean = fileListBean.getFilePathBean(srcNodeId);
        assertEquals(forFolder, filePathBean.isFolder());
        final String oldFilePath = filePathBean.getFullPath();            
        
        final File oldFile = filePathBean.toFile(filesHomePath);
        final FileEntity fe = new FileEntity(oldFilePath);
        fe.replaceNameSegment(oldFilePath, newName);
        final File newFile = fe.toFile(filesHomePath);
        assertEquals(oldFile.getParent(), newFile.getParent());
        
        try {
            FsUtil.moveFile(oldFile, newFile);
        } catch (Exception e) {
            e.printStackTrace();
            fail(String.format("moving a file/folder %s to %s failed!",
                oldFile.getAbsolutePath(), newFile.getAbsolutePath()));
        }
        assertEquals(false, oldFile.exists());
        assertEquals(true, newFile.exists());

        List<FileEntity> entities = Collections.emptyList();
        if (filePathBean.isFolder()) {
            entities = fileEntityDao.findByNamePattern(oldFilePath+"/%");
        }
        else {
            FileEntity entity = fileEntityDao.findByName(oldFilePath);
            if (entity != null) {
                entities = new ArrayList<>();
                entities.add(entity);
            }
        }
        for (FileEntity f : entities) {
            f.replaceNameSegment(oldFilePath, newName);
            fileEntityDao.save(f);
        }
        
        // file structures for test are heavily modified. refresh it.
        renewTestData();
    }
    
    @Test
    public void testMoveFiles() {
        final FileListBean fileListBean = new FileListBean();
        fileListBean.setFileEntities(fileEntityDao.findAll());
        FilePathTree filePathTree = new FilePathTree();
        fileListBean.setFilePathTree(filePathTree);
        FilePathTree folderTree = new FilePathTree();
        fileListBean.setFolderTree(folderTree);
        folderTree.populateNodes(null);
        
        final boolean forFolder = TestUtil.getRandom().nextBoolean();
        final int srcNodeId = getRandomFilePathId(filePathTree.getFilePathBeans(), forFolder, true);
        final FilePathBean srcPathBean = fileListBean.getFilePathBean(srcNodeId);
        assertEquals(forFolder, srcPathBean.isFolder());
        final String oldFilePath = srcPathBean.getFullPath();
        final int dstNodeId = getRandomFilePathId(folderTree.getFilePathBeans(), true, true);
        final FilePathBean dstPathBean = fileListBean.getFolderPathBean(dstNodeId);
        assertEquals(true, dstPathBean.isFolder());
        final String newParentPath = dstPathBean.getFullPath();
        
        final File oldFile = srcPathBean.toFile(filesHomePath);
        final FileEntity fe = new FileEntity(newParentPath + FsUtil.SEP + srcPathBean.getName());
        final File newFile = fe.toFile(filesHomePath);
        
        // Abort the test in either of the following cases
        // - The source and destination are identical
        // - The destination already exists
        // - The source is a subdirectory of the destination (when moving a folder)
        if (oldFile.equals(newFile) || newFile.exists())
            return;
        if (srcPathBean.isFolder() && newFile.getAbsolutePath().startsWith(oldFile.getAbsolutePath()))
            return;
        
        try {
            FsUtil.moveFile(oldFile, newFile);
        } catch (Exception e) {
            e.printStackTrace();
            fail(String.format("moving a file/folder %s to %s failed!",
                oldFile.getAbsolutePath(), newFile.getAbsolutePath()));
        }
        assertEquals(false, oldFile.exists());
        assertEquals(true, newFile.exists());

        List<FileEntity> entities = Collections.emptyList();
        if (srcPathBean.isFolder()) {
            entities = fileEntityDao.findByNamePattern(oldFilePath+"/%");
        }
        else {
            FileEntity entity = fileEntityDao.findByName(oldFilePath);
            if (entity != null) {
                entities = new ArrayList<>();
                entities.add(entity);
            }
        }
        for (FileEntity f : entities) {
            if (srcPathBean.isFolder())
                f.setFileName(newParentPath + FsUtil.SEP + srcPathBean.getName() + f.getFileName().replace(oldFilePath, ""));
            else
                f.setFileName(newParentPath + FsUtil.SEP + f.endName());
            fileEntityDao.save(f);
        }
        
        renewTestData();
    }
    
    @Test
    public void testDeleteFiles() {
        final FileListBean fileListBean = new FileListBean();
        fileListBean.setFileEntities(fileEntityDao.findAll());        
        FilePathTree filePathTree = new FilePathTree();
        fileListBean.setFilePathTree(filePathTree);
        
        final boolean forFolder = TestUtil.getRandom().nextBoolean();
        final int srcNodeId = getRandomFilePathId(filePathTree.getFilePathBeans(), forFolder, true);
        final FilePathBean filePathBean = fileListBean.getFilePathBean(srcNodeId);
        final String filePath = filePathBean.getFullPath();
        
        try {
            FsUtil.forceDelete(filePathBean.toFile(filesHomePath));
        } catch (IOException e) {
            e.printStackTrace();
            fail(String.format("deleting '%s' failed!", filePath));
        }
        
        List<FileEntity> entities = Collections.emptyList();        
        if (filePathBean.isFolder()) {
            entities = fileEntityDao.findByNamePattern(filePath+"/%");
        }
        else {
            FileEntity entity = fileEntityDao.findByName(filePath);
            if (entity != null) {
                entities = new ArrayList<>();
                entities.add(entity);
            }
        }        
        for (FileEntity f : entities) {
            final String pathOnFileSystem = filesHomePath + f.getFileName();
            assertEquals(false, new File(pathOnFileSystem).exists());
            fileEntityDao.delete(f);
        }
        
        renewTestData();
    }

}
