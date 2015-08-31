package com.civilizer.utils;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

public final class FsUtil {
    
    // [RULE] The file path style follows Unix convention throughout the application.
    // Never make a path with the following:
    //     - Windows style path separator
    //     - File.separator
    //     - File.separatorChar
    // Instead, use this.
    public static String SEP = "/";
    
    public static String normalizePath(String path) {
        if (path.startsWith("~")) {
            // translate '~' symbol to the user home path
            final String homePath = System.getProperty("user.home");
            final int len = path.length();
            if (len > 1) {
                if (path.charAt(1) == '/' || path.charAt(1) == '\\') {
                    if (len == 2)
                        path = homePath;
                    else
                        path = homePath + path.substring(1);
                }
            }
            else
                path = homePath;
        }

        return FilenameUtils.separatorsToUnix(path);
    }
    
    public static String getAbsolutePath(String srcPath, String basePath) {
        if (srcPath == null) {
            return null;
        }

        srcPath = normalizePath(srcPath);
        String absPath = null;        

        // [NOTE] Be cautious about boolean java.io.File.isAbsolute()!
        //   The definition of absolute pathname is SYSTEM DEPENDENT!
        //      On UNIX systems, a pathname is absolute if its prefix is "/".
        //      On Microsoft Windows systems, a pathname is absolute
        //          if its prefix is a drive specifier followed by "\\", or if its prefix is "\\\\".
        if (new File(srcPath).isAbsolute()) {
            // already absolute path
            absPath = srcPath;
        }
        else {
            // srcPath is relative path
            if (basePath == null) {
                return null;
            }
            
            basePath = normalizePath(basePath);

            if (new File(basePath).isAbsolute() == false) {
                return null;
            }
                
            absPath = FilenameUtils.normalize(basePath + FsUtil.SEP + srcPath, true);
        }
            
        return absPath;
    }
    
    public static String concatPath(String...names) {
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<names.length-1; ++i) {
            sb.append(names[i]).append(FsUtil.SEP);
        }
        sb.append(names[names.length-1]);
        return FilenameUtils.normalizeNoEndSeparator(sb.toString(), true);
    }
    
    public static void createUnexistingDirectory(File dir) {
        if (! dir.isDirectory()) {
            dir.mkdirs();
        }
    }
    
    public static boolean exists(String path) {
        return new File(path).exists();
    }
    
    public static boolean contentEquals(File file0, File file1) throws IOException {
        if (file0 == null || file1 == null)
            return false; // even if both are all nulls.
        if (file0.isFile()) {
            if (file1.isFile())
                return FileUtils.contentEquals(file0, file1);
            return false;
        }
        else if (file0.isDirectory()) {
            if (file1.isFile())
                return false;
            
            if (file0.list().length == 0 && file1.list().length == 0)
                return true; // both are empty folders
            
            final Collection<File> fileItems0 = FileUtils.listFilesAndDirs(
                    file0, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
            final Collection<File> fileItems1 = FileUtils.listFilesAndDirs(
                    file1, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
            if (fileItems0.size() != fileItems1.size())
                return false;
            
            Iterator<File> itr0 = fileItems0.iterator();
            Iterator<File> itr1 = fileItems1.iterator();            
            while (itr0.hasNext()) {
                final File f0 = itr0.next();
                final File f1 = itr1.next();
                if (f0.isDirectory() && f1.isDirectory())
                    continue;
                if (! contentEquals(f0, f1))
                    return false;
            }
            
            return true;
        }
        return false;
    }
    
    public static void compress(ZipOutputStream zipOut, InputStream st, String entryName) throws IOException {
        final int tmpBufSize = 1024;
        final byte[] tmpBuf = new byte[tmpBufSize];
        
        final ZipEntry ze = new ZipEntry(entryName);
        zipOut.putNextEntry(ze);
        int count = 0;

        while ((count = st.read(tmpBuf, 0, tmpBufSize)) != -1) {
            zipOut.write(tmpBuf, 0, count);
        }
    }

    public static void compress(ZipOutputStream zipOut, byte[] data, String entryName) throws IOException {
        try (final ByteArrayInputStream st = new ByteArrayInputStream(data)) {
            compress(zipOut, st, entryName);
        }            
    }
    
    public static void compress(ZipOutputStream zipOut, String tgtPath) throws IOException {
        final File tgtFile = new File(tgtPath);
        assert tgtFile.isAbsolute() && tgtFile.exists();
        
        tgtPath = FilenameUtils.normalizeNoEndSeparator(tgtPath, true);
        final int offset = tgtPath.length();
        final String basePath = FilenameUtils.getName(tgtPath);

        if (tgtFile.isFile()) {
            try (final FileInputStream fis = new FileInputStream(tgtFile.getAbsoluteFile());) {
                FsUtil.compress(zipOut, fis, basePath);
            }
            return;
        }
        
        Collection<File> fileItems = FileUtils.listFilesAndDirs(
                tgtFile,
                TrueFileFilter.INSTANCE,  // include all files
                TrueFileFilter.INSTANCE   // include all sub directories
                );
        
        for (File f : fileItems) {
            String p = FilenameUtils.normalize(f.getAbsolutePath(), true);
            p = basePath + (p.length() > offset ? p.substring(offset) : "");
            if (f.isDirectory()) {
                if (f.list().length == 0) { // empty folder
                    // An empty folder should end with the file separator to denote that.
                    if (! p.endsWith(FsUtil.SEP))
                        p += FsUtil.SEP;
                    zipOut.putNextEntry(new ZipEntry(p));
                }
            }
            else { // file
                try (final FileInputStream fis = new FileInputStream(f.getAbsoluteFile());) {
                    FsUtil.compress(zipOut, fis, p);
                }
            }
        }
    }
    
    public static void compress(String zipFilePath, String[] paths) throws IOException {
        FileUtils.touch(new File(zipFilePath));
        try (final FileOutputStream dst = new FileOutputStream(zipFilePath);
                final ZipOutputStream zipOut = new ZipOutputStream(dst)) {
            
            for (String p : paths) {
                compress(zipOut, p);
            }
        }
    }
    
    public static byte[] uncompress(ZipInputStream zipIn) throws IOException {
        if (zipIn.getNextEntry() == null) {
            return null;
        }

        final int tmpBufSize = 1024;
        final byte[] tmpBuf = new byte[tmpBufSize];
        byte[] output = null;
        
        try (final ByteArrayOutputStream st = new ByteArrayOutputStream();) {

            int count = 0;
            while ((count = zipIn.read(tmpBuf, 0, tmpBufSize)) != -1) {
                st.write(tmpBuf, 0, count);
            }
            output = st.toByteArray();
        }
        
        return output;
    };
    
    public static String uncompressToFile(ZipInputStream zipIn, String parentPath) throws IOException {
        final ZipEntry ze = zipIn.getNextEntry();
        if (ze == null) {
            return null;
        }
        
        final int tmpBufSize = 1024;
        final byte[] tmpBuf = new byte[tmpBufSize];
        int count = 0;
        final String path = FsUtil.normalizePath(parentPath + "/" + ze.getName());
        
        if (path.endsWith(FsUtil.SEP)) {
            // This is an empty folder.
            FsUtil.createUnexistingDirectory(new File(path));
            return path;
        }
        else {
            FileUtils.touch(new File(path));
        }
        
        try (final FileOutputStream fos = new FileOutputStream(path);
                final BufferedOutputStream bos = new BufferedOutputStream(fos, tmpBufSize);) {
            
            while ((count = zipIn.read(tmpBuf, 0, tmpBufSize)) != -1) {
                bos.write(tmpBuf, 0, count);
            }
        };
        
        return path;
    };
    
    public static void uncompressToFolder(String zipFilePath, String parentPath) throws IOException {
        try (final FileInputStream src = new FileInputStream(zipFilePath);
                final ZipInputStream zipIn = new ZipInputStream(src);) {
            
            while (FsUtil.uncompressToFile(zipIn, parentPath) != null) {}
        }
    }

}
