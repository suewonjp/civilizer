package com.civilizer.test.utils;

import static org.junit.Assert.*;

import org.junit.*;

import java.io.File;

import com.civilizer.utils.FsUtil;

public class FsUtilTest {

    @Before
    public void setUp() throws Exception {
    }

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

}
