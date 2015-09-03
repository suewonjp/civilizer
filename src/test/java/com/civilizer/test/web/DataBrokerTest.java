package com.civilizer.test.web;

import static org.junit.Assert.*;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.junit.*;

import java.io.File;

import com.civilizer.test.dao.DaoUrlTest;
import com.civilizer.test.helper.TestUtil;
import com.civilizer.utils.FsUtil;
import com.civilizer.web.view.DataBrokerBean;

public class DataBrokerTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        DaoUrlTest.buildCreateDataSet();
    }

    @Before
    public void setUp() throws Exception {
        TestUtil.configure();
    }

    @After
    public void tearDown() throws Exception {
        TestUtil.unconfigure();
    }
    
    @AfterClass
    public static void tearDownAfterClass() {
        FileUtils.deleteQuietly(new File(TestUtil.getTempFolderPath()));
    }

    @Test
    public void testDataExport() {
        final String tmpPath = FsUtil.concatPath(TestUtil.getTempFolderPath(), DataBrokerBean.exportFolderName);
        try {
            final String exportFilePath = DataBrokerBean.exportData();
            assertNotNull(exportFilePath);
            assertEquals(DataBrokerBean.getExportFilePath(), exportFilePath);
            final File exportFile = new File(exportFilePath);
            assertNotNull(exportFile);
            assertEquals(new File(tmpPath), exportFile.getParentFile());
            assertEquals(true, exportFile.isAbsolute());
            assertEquals(true, exportFile.isFile());
            
            final String uncompressPath = FsUtil.concatPath(tmpPath, "uncmp");
            FsUtil.uncompressToFolder(exportFilePath, uncompressPath);
            
            final String[] paths = new File(uncompressPath).list();
            assertEquals(2, paths.length);
            for (String p : paths) {
                assertNotNull(p);
                final String path = FsUtil.concatPath(uncompressPath, p);
                final File f = new File(path);
                if (f.isFile()) {
                    assertEquals(FilenameUtils.getName(TestUtil.getDatabaseFilePath()), p);
                }
                else if (f.isDirectory()) {
                    assertEquals(FilenameUtils.getName(TestUtil.getFilesHomePath()), p);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }
    
    @Test
    public void testDataImport() {
        final String importFolderPath = DataBrokerBean.getImportFolderPath();
        try {
            final String exportFilePath = DataBrokerBean.exportData();
            assertNotNull(exportFilePath);
            
            // Original source database file may be modified with some reason.
            // So we copy the database file somewhere else and test against it.
            final File tmpDatabaseFile = new File(FsUtil.concatPath(TestUtil.getTempFolderPath(), "tmp-db-file"));
            FileUtils.copyFile(new File(TestUtil.getDatabaseFilePath()), tmpDatabaseFile);
            assertEquals(true, tmpDatabaseFile.isFile());
            
            final File exportFile = new File(exportFilePath);
            assertNotNull(exportFile);
            
            final File importFolder = new File(importFolderPath);
            FsUtil.createUnexistingDirectory(importFolder);
            final File importFile = new File(FsUtil.concatPath(importFolderPath, DataBrokerBean.importFileName));
            FileUtils.moveFile(exportFile, importFile);
            
            final String uncompressPath = DataBrokerBean.prepareDataImport();
            assertNotNull(uncompressPath);
            final File uncompressFolder = new File(uncompressPath);
            assertEquals(true, uncompressFolder.isDirectory());
            
            {
                final String[] paths = uncompressFolder.list();
                assertEquals(2, paths.length);
                for (String p : paths) {
                    assertNotNull(p);
                    final String path = FsUtil.concatPath(uncompressPath, FilenameUtils.getName(p));
                    final File fileToImport = new File(path);
                    if (fileToImport.isFile()) {
//                        final File tgtFile = new File(
//                                TestUtil.getDatabaseFilePath());
//                        assertNotNull(tgtFile);
//                        assertEquals(true, tgtFile.isFile());
//                        assertEquals(true,
                        assertEquals(true,
                                FileUtils.contentEquals(tmpDatabaseFile, fileToImport));
//                                FileUtils.contentEquals(tgtFile, fileToImport));
                    } else if (fileToImport.isDirectory()) {
                        final File tgtFile = new File(
                                TestUtil.getFilesHomePath());
                        assertNotNull(tgtFile);
                        assertEquals(true, tgtFile.isDirectory());
                        assertEquals(true,
                                FsUtil.contentEquals(tgtFile, fileToImport));
                    }
                }
            }
            
            if (System.getProperty("os.name").toLowerCase().contains("win"))
                // DataBrokerBean.commitImportData() doesn't work on Windows due to the following bug.
                // http://bugs.java.com/bugdatabase/view_bug.do?bug_id=4715154
                return;
            
            DataBrokerBean.importData(uncompressPath);
            assertEquals(false, uncompressFolder.isDirectory());
            
            {
                final String[] paths = {
                    TestUtil.getDatabaseFilePath(),
                    TestUtil.getFilesHomePath(),
                };
                for (String p : paths) {
                    final File importedFile = new File(p);
                    assertNotNull(importedFile);
                    if (importedFile.isFile()) {
                        assertEquals(true, importedFile.isFile());
                    } else if (importedFile.isDirectory()) {
                        assertEquals(true, importedFile.isDirectory());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

}
