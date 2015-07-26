package com.civilizer.test.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.civilizer.test.helper.TestUtil;
import com.civilizer.utils.FsUtil;

public class FsUtilTest {

    @Before
    public void setUp() throws Exception {
        TestUtil.configure();
    }
    
    @After
    public void tearDown() {
        TestUtil.unconfigure();
    }
    
//    @Test
//    public void testPathSeparators() {
//        {
//            final String inPath = "/abc/de/f/gh";
//            final String outPath = FilenameUtils.normalize(inPath);
//            assertEquals("/abc/de/f/gh", outPath);
//        }
//        {
//            final String inPath = "/abc//de//f/gh";
//            final String outPath = FilenameUtils.normalize(inPath);
//            assertEquals("/abc/de/f/gh", outPath);
//        }
//        {
//            final String inPath = "c:\\abc\\de\\\\f\\\\";
//            final boolean unixSeparators = true;
//            final String outPath = FilenameUtils.normalize(inPath, unixSeparators);
//            assertEquals("c:/abc/de/f/", outPath);
//        }
//        {
//            // [CAUTION!] double backslashes following a drive specifier
//            // will be converted to double slashes.
//            final String inPath = "c:\\\\abc\\de\\\\f\\\\";
//            final boolean unixSeparators = true;
//            final String outPath = FilenameUtils.normalize(inPath, unixSeparators);
//            assertEquals("c://abc/de/f/", outPath);
//        }
//        {
//            final String inPath = "c:/abc/de\\\\f\\";
//            final boolean unixSeparators = true;
//            final String outPath = FilenameUtils.normalize(inPath, unixSeparators);
//            assertEquals("c:/abc/de/f/", outPath);
//            assertEquals("c:\\abc\\de\\f\\", FilenameUtils.normalize(inPath, ! unixSeparators));
//        }
//    }

    @Test
    public void testMethod_getAbsolutePath() {
        final String baesPath = FsUtil.toNativePath("/base/path");
        {
            final String srcPath = FsUtil.toNativePath("/foo/bar");
            final String absPath = FsUtil.getAbsolutePath(srcPath, baesPath);
            assertEquals(FsUtil.toNativePath("/foo/bar"), absPath);
        }
        {
            final String srcPath = FsUtil.toNativePath("~/foo/bar");
            final String absPath = FsUtil.getAbsolutePath(srcPath, baesPath);
            assertEquals(System.getProperty("user.home")+FsUtil.toNativePath("/foo/bar"), absPath);
        }
        {
            final String srcPath = FsUtil.toNativePath("./foo/bar");
            final String absPath = FsUtil.getAbsolutePath(srcPath, baesPath);
            assertEquals(FsUtil.toNativePath("/base/path/foo/bar"), absPath);
        }
        {
            final String srcPath = FsUtil.toNativePath("../foo/bar");
            final String absPath = FsUtil.getAbsolutePath(srcPath, baesPath);
            assertEquals(FsUtil.toNativePath("/base/foo/bar"), absPath);
        }
        {
            final String srcPath = FsUtil.toNativePath("../../foo/bar");
            final String absPath = FsUtil.getAbsolutePath(srcPath, baesPath);
            assertEquals(FsUtil.toNativePath("/foo/bar"), absPath);
        }
        {
            final String srcPath = FsUtil.toNativePath("foo/bar");
            final String absPath = FsUtil.getAbsolutePath(srcPath, baesPath);
            assertEquals(FsUtil.toNativePath("/base/path/foo/bar"), absPath);
        }
    }
    
    @Test
    public void testMethod_CompressUncompress() {
        final String inContent = TestUtil.randomString(TestUtil.getRandom(), 512, 1024);
        assertNotNull(inContent);
        final ByteArrayInputStream inputDataStm = new ByteArrayInputStream(inContent.getBytes());
        final InputStream[] inputStreams =  { inputDataStm };
        final String[] names = { "test.dat" };
        final String zipFilePath = TestUtil.getTempFolderPath() + File.separator + "tmp.zip";
        
        File tmpFolder = new File(TestUtil.getTempFolderPath());
        FsUtil.createUnexistingDirectory(tmpFolder);
        assertEquals(true, tmpFolder.isDirectory());
        
        try (FileOutputStream dst = new FileOutputStream(zipFilePath);
                final ZipOutputStream zipOut = new ZipOutputStream(dst)) {
            
            for (int i=0; i<inputStreams.length; ++i) {
                final InputStream ins = inputStreams[i];
                assertEquals(inContent.length(), FsUtil.compress(zipOut, ins, names[i]));
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            fail("failed in creating the destination zip file!");
        };
        
        final File zipFileInput = new File(zipFilePath);
        assertEquals(zipFileInput.isFile(), true);
        
        try (FileInputStream src = new FileInputStream(zipFileInput);
                ZipInputStream zipIn = new ZipInputStream(src);) {
            
            int i = 0;
            byte[] bytes = null;
            while ((bytes = FsUtil.uncompress(zipIn)) != null) {
                final String outContent = new String(bytes);
                assertEquals(inContent, outContent);
                ++i;
            }
            assertEquals(inputStreams.length, i);
        }
        catch (IOException e) {
            e.printStackTrace();
            fail("failed in creating the source zip file!");
        }
        finally {
            FileUtils.deleteQuietly(tmpFolder);
        }
    }
    
    @Test
    public void testCompressUncompressFileStructure() {
        final String tmpPath = TestUtil.getTempFolderPath();
        try {
            // Create the file structure for testing.
            final String[] paths = {
              "/tmp-fs/file-a",      
              "/tmp-fs/folder0/folder1/file-b",
              "/tmp-fs/folder0/folder2/",
              "/tmp-fs/folder2/file-c",
              "/tmp-fs/folder3/",
            };
            for (String path : paths) {
                path = FsUtil.toNativePath(tmpPath+path);
                File f = new File(path);
                if (path.endsWith(File.separator)) {
                    FsUtil.createUnexistingDirectory(f);
                    assertEquals(true, f.isDirectory());
                }
                else { 
                    FileUtils.touch(f);
                    assertEquals(true, f.isFile());
                }
            }
            
            // compress the file structure.
            final String zipFilePath = TestUtil.getTempFolderPath() + File.separator + "tmp.zip";
            FsUtil.compressFolder(zipFilePath, TestUtil.getTempFolderPath() + File.separator + "tmp-fs");
            assertEquals(true, FsUtil.exists(zipFilePath));
            
            FileUtils.deleteQuietly(new File(FsUtil.toNativePath(tmpPath+"/tmp-fs")));
            
            // restore the file structure.
            FsUtil.uncompressToFolder(zipFilePath, tmpPath);
            
            // check its validity.
            for (String _p : paths) {
                final String path = FsUtil.toNativePath(tmpPath+_p);
                if (path.endsWith(File.separator)) { // empty folder
                    assertEquals(true, new File(path).isDirectory());
                    assertEquals(0, new File(path).list().length);
                }
                else { // file
                    assertEquals(true, new File(path).isFile());
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            fail("Something wrong!");
        }
        finally {
            FileUtils.deleteQuietly(new File(tmpPath));
        }
    }
    
//    @Test
//    public void testImportExportUserData() {
//    }
        
}

