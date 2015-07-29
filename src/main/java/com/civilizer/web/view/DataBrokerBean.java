package com.civilizer.web.view;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FlowEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.civilizer.config.AppOptions;
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
        else if (curStep.equals("predownload-step")) {
            RequestContext.getCurrentInstance().addCallbackParam("exportReady", true);
        }
    }
    
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
    
    public static String getImportFolderPath() {
        return System.getProperty(AppOptions.TEMP_PATH) + File.separator + importFolderName;
    }
    
    public static void commitImportData(String uncompressPath) throws IOException, SecurityException {
        final String[] paths = getTargetPaths();
        for (String p : paths) {
            final String path =
                    uncompressPath+File.separator+FilenameUtils.getName(p);
            final File newFile = new File(path);
            final File oldFile = new File(p);
            if (newFile.isFile()) { // Database file
                deleteOldFile(oldFile);
                FileUtils.moveFile(newFile, oldFile);
            }
            else if (newFile.isDirectory()) { // file box directory
                deleteOldFile(oldFile);
                FileUtils.moveDirectory(newFile, oldFile);
            }
        }
        
        FileUtils.deleteQuietly(new File(uncompressPath));
    }
    
    public static String importData() throws IOException {
        final String importFolderPath = getImportFolderPath();
        final File importFolder = new File(importFolderPath);
        if (! importFolder.isDirectory()) {
            throw new IOException("The import folder doesn't exist!");
        }
        
        final File importFile =
                new File(importFolder.getAbsolutePath()+File.separator+importFileName);
        if (! importFile.isFile()) {
            throw new IOException("Can't find a file to import!");
        }
        
        final String uncompressPath = importFolderPath + File.separator + "uncmp";
        
        FsUtil.uncompressToFolder(importFile.getAbsolutePath(), uncompressPath);
        
        return uncompressPath;
    }

    public static String exportData() throws IOException {
        final String[] paths = getTargetPaths();
        final String tmpPath = System.getProperty(AppOptions.TEMP_PATH) + File.separator + exportFolderName;
        FileUtils.deleteQuietly(new File(tmpPath));
        
        final String exportFilePath =
                tmpPath + File.separator + exportFileName;
        
        // Export the user data.
        FsUtil.createUnexistingDirectory(new File(tmpPath));
        FsUtil.compress(exportFilePath, paths);
        
        return exportFilePath;
    }
    
    public void packExportData() {
        if (curStep.equals("predownload-step")) {
            boolean ok = true;
            try {
                exportData();
            } catch (Exception e) {
                e.printStackTrace();
                ok = false;
            }
            RequestContext.getCurrentInstance().addCallbackParam("exportReady", ok);
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
                
                final Authentication auth =SecurityContextHolder.getContext().getAuthentication();
                final Object principal = auth.getPrincipal();
                if (principal instanceof UserDetails) {
                    final UserDetails ud = (UserDetails) principal;
                    if (new BCryptPasswordEncoder().matches(pw, ud.getPassword()) == false) {
                        // authentication failed...
                        authFailed = true;
                        return (curStep = "auth-step");
                    }
                }
                
                return (curStep = exportMode ? "predownload-step" : "upload-step");
            }
        }
        else if (oldStep.equals("upload-step")) {
            // Import the uploaded data.
            return (curStep = "confirm-import-step");
        }
        else if (oldStep.equals("predownload-step")) {
            return (curStep = "download-step");
        }
        else if (oldStep.equals("download-step")) {
            return (curStep = "confirm-export-step");
        }

        return oldStep;
    }

}
