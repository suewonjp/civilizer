package com.civilizer.utils;

import java.io.File;

import org.apache.commons.io.FilenameUtils;

public final class FsUtil {
    
    public static String toNativePath(String path) {
        return FilenameUtils.separatorsToSystem(path);
    }
    
    public static String getAbsolutePath(String srcPath, String basePath) {
        srcPath = toNativePath(srcPath);
        String absPath = null;
        if (new File(srcPath).isAbsolute()) {
            // already absolute path
            absPath = srcPath;
        }
        else {
            // relative path
            if (srcPath.startsWith("~/") || srcPath.startsWith("~\\")) {
                absPath = System.getProperty("user.home") + srcPath.substring(1);
            }
            else {
                absPath = FilenameUtils.normalize(basePath + File.separator + srcPath);
            }
        }
        return absPath;
    }
    
    public static void createUnexistingDirectory(File dir) {
        if (! dir.isDirectory()) {
            dir.mkdir();
        }
    }

}
