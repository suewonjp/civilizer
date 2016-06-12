package com.civilizer.test.utils

import spock.lang.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.civilizer.test.helper.TestUtil;
import com.civilizer.utils.FsUtil;

@Subject(FsUtil)
class FsUtilSpec extends spock.lang.Specification {
    
    def setup() {
        TestUtil.configure();
    }
    
    def cleanup() {
        TestUtil.unconfigure();
    }
    
    def createFileStructureForTest(String parentPath) throws IOException {
        // Create the file structure for testing.
        def paths = [
          "/tmp-fs/file-a",      
          "/tmp-fs/folder0/folder1/file-b",
          "/tmp-fs/folder0/folder2/",
          "/tmp-fs/folder2/file-c",
          "/tmp-fs/folder3/",
        ]
        
        paths.each {
            it = FsUtil.normalizePath(parentPath + it);
            File f = new File(it);
            if (it.endsWith(FsUtil.SEP)) {
                FsUtil.createUnexistingDirectory(f);
            }
            else {
                FileUtils.touch(f);
            }
        }
        
        paths
    }
    
    def "FsUtil.getAbsolutePath -- for ~(tilde) notation"() {
        def absPath;
        
        given: "The (absolute) path of the user home directory"
            final String homePath = FsUtil.normalizePath(System.getProperty("user.home"));
            
        expect: "~ notation is properly translated"
            homePath == FsUtil.getAbsolutePath("~", null);
            homePath == FsUtil.getAbsolutePath("~/", null);
            homePath+FsUtil.normalizePath("/foo/bar") ==
                FsUtil.getAbsolutePath("~/foo/bar", null);
            homePath+FsUtil.normalizePath("/foo/bar") ==
                FsUtil.getAbsolutePath("foo/bar", "~");
            homePath+FsUtil.normalizePath("/foo/bar") ==
                FsUtil.getAbsolutePath("foo/bar", "~/");
    }
    
    
    def "FsUtil.getAbsolutePath -- for Unix path convention"() {
        given: "For *nix systems only"
            if (System.getProperty("os.name").toLowerCase().contains("win"))
                return;

        when:
            def result = FsUtil.getAbsolutePath(
                srcPath ? FsUtil.normalizePath(srcPath) : null, 
                FsUtil.normalizePath(basePath))
        then:
            result == (absPath ? FsUtil.normalizePath(absPath) : null);
            
        where: ""
            basePath            | srcPath                   || absPath
            "/base/path"        | null                      || null 
            "/base/path"        | "/foo/bar"                || "/foo/bar" 
            "/base/path"        | "~/foo/bar"               || FsUtil.normalizePath(System.getProperty("user.home")) + "/foo/bar" 
            "/base/path"        | "foo/bar"                 || "/base/path/foo/bar" 
            "/base/path"        | "./foo/bar"               || "/base/path/foo/bar" 
            "/base/path"        | "../foo/bar"              || "/base/foo/bar" 
            "/base/path"        | "../../foo/bar"           || "/foo/bar" 
    }
    
    def "FsUtil.getResourceAsFile"() {
        given: "A file from resource"
            final File file = FsUtil.getResourceAsFile(getClass(), "dir for test/test resource.txt");
        expect:
            file.isFile()
            
        def content;
        
        when: "Read content from the file"
            try {
                content = FileUtils.readFileToString(file, Charset.forName("UTF-8"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        then: "The content is as expected"
            "ひらがな　カタカナ　한글 english\n" == content
    }
    
    def "FsUtil.concatPath"() {
        when: "We concatenate multiple path"
            def path = FsUtil.concatPath(names as String[])
        then:
            path == result
            
        where: ""
            names                   || result
            [ "foo" ]               || "foo"
            [ "", "foo" ]           || FsUtil.SEP + "foo"
            [ "foo", "bar" ]        || "foo" + FsUtil.SEP + "bar"
            [ "foo", "bar/" ]       || "foo" + FsUtil.SEP + "bar"
            [ "foo\\", "/bar\\" ]   || "foo" + FsUtil.SEP + "bar"
            [ "/foo", "bar" ]       || FsUtil.SEP + "foo" + FsUtil.SEP + "bar"
    }
    
    def "FsUtil.forceDelete --- for a file"() {
        given: "A file to delete"
            File f = new File(FsUtil.concatPath(TestUtil.getPrivateHomePath(), "sample.txt"));
        FileUtils.writeStringToFile(f, "Hello, Civilizer...");
        when: "Delete it!"
            FsUtil.forceDelete(f);
        then:
            notThrown IOException
            ! f.exists()
    }

    def "FsUtil.forceDelete --- for a directory"() {
        given: "A directory to delete"
            final def dirPath = FsUtil.concatPath(TestUtil.getTempFolderPath(), "tmpdir");
            final def dir = new File(dirPath);
            FsUtil.createUnexistingDirectory(dir);
        and: "Attach some arbitrary files to the directory"
            [ "sample0.txt", "sample1.txt" ].each {
                File f = new File(FsUtil.concatPath(dirPath, it));
                FileUtils.touch(f);
                FileUtils.writeStringToFile(f, "Hello, Civilizer...");
            }
        when: "Delete it!"
            FsUtil.forceDelete(dir);
        then:
            notThrown IOException
            ! dir.exists()
    }
    
    def "FsUtil.contentEquals"() {
        given: "Two arbitrary folders"
            final String tmpPath = TestUtil.getTempFolderPath();
            final String[] srcPaths = createFileStructureForTest(tmpPath);
            final String srcFolder  = StringUtils.split(srcPaths[0], '/')[0];
            final File files0 = new File(FsUtil.concatPath(tmpPath, srcFolder));
            final File files1 = new File(FsUtil.concatPath(tmpPath, "files"));
            FsUtil.createUnexistingDirectory(files1);
            
        when: "Copy content of one folder to another folder"
            FileUtils.copyDirectory(files0, files1);
        then: 
            FsUtil.contentEquals(files0, files1)

        cleanup:
            FileUtils.deleteQuietly(new File(tmpPath))
    }
    
    def "FsUtil.compress / uncompress"() {
        given: "Arbitrary data to compress"
            final String inContent = TestUtil.randomString(TestUtil.getRandom(), 512, 1024);
            assert inContent != null;
            final ByteArrayInputStream inputDataStm = new ByteArrayInputStream(inContent.getBytes());
            final InputStream[] inputStreams =  [ inputDataStm ];
        and: "File structure for the compressed data"
            final String[] names = [ "test.dat" ];
            final String zipFilePath = FsUtil.concatPath(TestUtil.getTempFolderPath(), "tmp.zip");
            File tmpFolder = new File(TestUtil.getTempFolderPath());
            FsUtil.createUnexistingDirectory(tmpFolder);
            assert tmpFolder.isDirectory();
            
        when: "Compress"            
            final ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipFilePath));
            inputStreams.eachWithIndex {
                ins, idx -> FsUtil.compress(zipOut, ins, names[idx]) }
            zipOut.close();
            final File zipFileInput = new File(zipFilePath);
        then: "We have the zip file"
            zipFileInput.isFile()
            
        when: "Uncompress"
            final ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFileInput));
            byte[] bytes = null;
            int i = 0;
            while ((bytes = FsUtil.uncompress(zipIn)) != null) {
                final String outContent = new String(bytes);
                assert inContent == outContent;
                ++i;
            }
        then:
            i == inputStreams.length
            
        cleanup:
            zipIn.close();
            FileUtils.deleteQuietly(tmpFolder)
    }
    
    def "Compress/uncompress file structure"() {
        given: "The files and folders for testing"
            final String tmpPath = TestUtil.getTempFolderPath();
            final String[] paths = createFileStructureForTest(tmpPath);
        
        when: "Compress the file structure"
            final String zipFilePath = FsUtil.concatPath(tmpPath, "tmp.zip");
            FsUtil.compress(zipFilePath, 
                [ FsUtil.concatPath(tmpPath, "tmp-fs") ] as String[] );
        then: "We have the output zip file"
            FsUtil.exists(zipFilePath)
            
        when: "Restore the file structure from the zip"
            FileUtils.deleteQuietly(new File(FsUtil.normalizePath(tmpPath+"/tmp-fs")));
            FsUtil.uncompressToFolder(zipFilePath, tmpPath);
            
        then: "Is the structure valid?"
            paths.each {
                it = FsUtil.normalizePath(tmpPath + it);
                if (it.endsWith(FsUtil.SEP)) { // empty folder
                    assert new File(it).isDirectory()
                    assert 0 == new File(it).list().length
                }
                else { // file
                    assert new File(it).isFile()
                }
            }            
        
        cleanup:
            FileUtils.deleteQuietly(new File(tmpPath));
    }

}
