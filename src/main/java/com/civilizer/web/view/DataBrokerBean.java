package com.civilizer.web.view;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.FlowEvent;

import com.civilizer.config.AppOptions;
import com.civilizer.security.UserDetailsService;
import com.civilizer.utils.FsUtil;

@SuppressWarnings("serial")
public class DataBrokerBean implements Serializable {
    
    public static String importFolderName = "imp";
    public static String exportFolderName = "exp";
    public static String importFileName = "cvz-import.zip";
    public static String exportFileName = "cvz-export.zip";
    
    private String curStep = "";
    private String password = "";
    private boolean exportMode;
    private boolean authFailed;
    
    private static String[] getTargetPaths() {
        final String [] paths = {
                System.getProperty(AppOptions.DB_FILE_PREFIX)+System.getProperty(AppOptions.DB_FILE_SUFFIX),
                System.getProperty(AppOptions.FILE_BOX_HOME),
        };
        return paths;
    }
    
    private static void deleteOldFile(File oldFile) throws IOException, SecurityException {
        if (oldFile.isFile())
            oldFile.delete();
        else if (oldFile.isDirectory())
            FileUtils.deleteDirectory(oldFile);
    }

    private static void moveFile(File srcFile, File dstFile) throws IOException, SecurityException {
        if (srcFile.isFile())
            FileUtils.moveFile(srcFile, dstFile);
        else if (srcFile.isDirectory())
            FileUtils.moveDirectory(srcFile, dstFile);
    }
    
    public static String getImportFolderPath() {
        return FsUtil.concatPath(System.getProperty(AppOptions.TEMP_PATH), importFolderName);
    }

    public static String getImportFilePath() {
        return FsUtil.concatPath(getImportFolderPath(), importFileName);
    }
    
    public static void commitImportData(String uncompressPath) throws IOException, SecurityException {
        final File srcFolder = new File(uncompressPath);        
        final File srcFiles[] = { null, null };
        
        for (File f : FileUtils.listFilesAndDirs(srcFolder, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE)) {
            if (f.equals(srcFolder))
                continue;
            if (f.getName().endsWith(System.getProperty(AppOptions.DB_FILE_SUFFIX)))
                srcFiles[0] = f;
            else if (f.isDirectory() && f.getParentFile().equals(srcFolder))
                srcFiles[1] = f;
            if (srcFiles[0] != null && srcFiles[1] != null)
                break;
        }
        
        // 'paths' is supposed to contain:
        //   1. one file, which is a database file.
        //   2. one folder, which is a file box folder.
        final String[] paths = getTargetPaths();

        for (int i=0; i<2; ++i) {
            final File oldFile = new File(paths[i]);
            final File newFile = srcFiles[i];
            deleteOldFile(oldFile);
            moveFile(newFile, oldFile);
        }
        
        FileUtils.deleteQuietly(srcFolder);
    }
    
    public static String importData() throws IOException {
        final String importFolderPath = getImportFolderPath();
        final String importFilePath = getImportFilePath();
        
        if (! new File(importFilePath).isFile()) {
            throw new IOException("Can't find a file to import!");
        }
        
        final String uncompressPath = FsUtil.concatPath(importFolderPath, "uncmp");
        
        // uncompress the imported file into the temporary folder.
        FsUtil.uncompressToFolder(importFilePath, uncompressPath);
        
        return uncompressPath;
    }

    public static String exportData() throws IOException {
        final String[] paths = getTargetPaths();
        final String tmpPath = FsUtil.concatPath(System.getProperty(AppOptions.TEMP_PATH), exportFolderName);
        FileUtils.deleteQuietly(new File(tmpPath));
        
        final String exportFilePath = FsUtil.concatPath(tmpPath, exportFileName);
        
        // Export the user data.
        FsUtil.createUnexistingDirectory(new File(tmpPath));
        FsUtil.compress(exportFilePath, paths);
        
        return exportFilePath;
    }
    
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    
    public boolean isExportMode() {
        return exportMode;
    }

    public void setExportMode(boolean exportMode) {
        this.exportMode = exportMode;
    }
    
    public void checkNext() {
        if (curStep.equals("auth-step")) {
            if (authFailed)
                RequestContext.getCurrentInstance().addCallbackParam("authFailed", true);
            authFailed = false;
        }
        else if (curStep.equals("preexport-step")) {
            RequestContext.getCurrentInstance().addCallbackParam("exportReady", true);
        }
    }
    
    public void onFileUpload(FileUploadEvent event) {
        final String importFolderPath = getImportFolderPath();
        final File importFolder = new File(importFolderPath);
        if (! importFolder.isDirectory()) {
            FsUtil.createUnexistingDirectory(importFolder);
        }
        final String importFilePath = getImportFilePath();
        try {
            event.getFile().write(importFilePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public String onDataExportFlow(FlowEvent event) {
        final String oldStep = event.getOldStep();
        final String newStep = event.getNewStep();
        
        if (oldStep.equals("auth-step") || newStep.equals("auth-step")) {
            // Authentication step.
            authFailed = false;
            if (password.isEmpty()) {
                return (curStep = "auth-step");
            }
            else { // the password has been provided
                final String pw = password;
                password = "";
                if (! UserDetailsService.authenticatePassword(pw)) {
                    // authentication failed...
                    authFailed = true;
                    return (curStep = "auth-step");
                }                
                return (curStep = exportMode ? "preexport-step" : "upload-step");
            }
        }
        else if (oldStep.equals("upload-step")) {
            // Import the uploaded data.
            curStep = "import-error-step";
            try {
                final String uncompressPath = importData();
                commitImportData(uncompressPath);
                curStep = "confirm-import-step";
            } catch (Exception e) {
                e.printStackTrace();
            }
            return curStep;
        }
        else if (oldStep.equals("preexport-step")) {
            try {
                exportData();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return (curStep = "export-step");
        }
        else if (oldStep.equals("export-step")) {
            return (curStep = "confirm-export-step");
        }

        return oldStep;
    }

}
