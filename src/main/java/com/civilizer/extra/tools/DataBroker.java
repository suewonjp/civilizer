package com.civilizer.extra.tools;

import java.io.File;
//import java.net.URL;
//import java.net.URLClassLoader;

import java.io.IOException;
import java.util.*;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.civilizer.config.AppOptions;
import com.civilizer.config.Configurator;
import com.civilizer.utils.FsUtil;
import com.civilizer.web.view.DataBrokerBean;

public final class DataBroker {
    
    private static final Logger logger = LoggerFactory.getLogger(DataBroker.class);
    
//    private static void printClasspath() {
//        ClassLoader cl = ClassLoader.getSystemClassLoader();
//        URL[] urls = ((URLClassLoader)cl).getURLs();
//        for(URL url: urls){
//            System.out.println(url.getFile());
//        }
//    }
    
    private static void printHelpMessage() {
        String msg = "* Options :\n";
        msg += "\t-export path: export Civilizer data to the given FOLDER path\n";
        msg += "\t-import path: import Civilizer data from the given FILE path\n";
        System.out.println(msg);
    }
    
    private static void onExport(String userSpecifiedPath) throws IOException {
        final String exportFilePath = DataBrokerBean.exportData();
        final File exportFile = new File(exportFilePath);
        if (exportFile.isFile() == false) {
            logger.error("Exporting data failed!");
            return;
        }
        
        if (userSpecifiedPath == null) {
            logger.info("[Success!] Data exported to '{}'", exportFilePath);
            return;
        }

        final File userSpecifiedFolder = new File(userSpecifiedPath);
        if (userSpecifiedFolder.isDirectory() == false) {
            FsUtil.createUnexistingDirectory(userSpecifiedFolder);
            if (userSpecifiedFolder.isDirectory() == false) {
                logger.error("Can't create the user specified folder at '{}'", userSpecifiedFolder.getAbsolutePath());
                return;
            }
        }
        
        final String dstPath = FsUtil.concatPath(userSpecifiedPath, FilenameUtils.getName(exportFilePath));
        final File dstFile = new File(dstPath);
        FileUtils.moveFile(exportFile, dstFile);
        if (dstFile.isFile() == true) {
            logger.info("[Success!] Data exported to '{}'", dstFile.getAbsolutePath());
        }
        else {
            logger.warn("Moving exported data to '{}' failed!", dstFile.getAbsolutePath());
            logger.info("[Success!] Data exported to '{}'", exportFilePath);
        }
    }

    private static void onImport(String userSpecifiedPath) throws IOException {
        final String importFolderPath = DataBrokerBean.getImportFolderPath();
        final File importFolder = new File(importFolderPath);
        if (importFolder.isDirectory() == false) {
            FsUtil.createUnexistingDirectory(importFolder);
            if (importFolder.isDirectory() == false) {
                logger.error("Can't create the temporary folder at '{}'", importFolder.getAbsolutePath());
                return;
            }            
        }

        final String importFilePath = DataBrokerBean.getImportFilePath();
        final File importFile = new File(importFilePath);
        File srcFile = new File(DataBrokerBean.getExportFilePath());

        if (userSpecifiedPath != null) {
            final File userSpecifiedFile = new File(userSpecifiedPath);
            if (userSpecifiedFile.isFile() == false) {
                logger.error("Can't find the user specified file at '{}'", userSpecifiedFile.getAbsolutePath());
                return;
            }
            srcFile = userSpecifiedFile;
        }

        FileUtils.copyFile(srcFile, importFile);
        if (importFile.isFile() == false) {
            logger.error("Moving the file '{}' to '{}' failed!",
                    srcFile.getAbsolutePath(),
                    importFile.getAbsolutePath());
            return;
        }
        
        final String uncompressPath = DataBrokerBean.prepareDataImport();
        final File uncompressFolder = new File(uncompressPath);
        if (uncompressFolder.isDirectory() == false) {
            logger.error("Importing data failed!");
            return;
        }
        
        DataBrokerBean.importData(uncompressPath);

        logger.info("[Success!] Data Imported");
    }
    
    public static void main(String[] args) throws IOException {
        System.out.println("Data Broker : Offline Data Export/Import CUI Tool for Civilizer\n");        
        
        final String privateHomePath = System.getProperty(AppOptions.PRIVATE_HOME_PATH);
        if (privateHomePath != null && new File(privateHomePath).isAbsolute() == false)
            System.clearProperty(AppOptions.PRIVATE_HOME_PATH);
        new Configurator();
        
        Arrays.sort(args);
        
        {
            final String option = "-export";
            final int iii = Arrays.binarySearch(args, option);
            if (-1 < iii) {
                if (iii < args.length-1) {
                    onExport(args[iii + 1]);
                }
                else {
                    onExport(null);
                }
                return;
            }
        }
        {
            final String option = "-import";
            final int iii = Arrays.binarySearch(args, option);
            if (-1 < iii) {
                if (iii < args.length-1) {
                    onImport(args[iii + 1]);
                }
                else {
                    onImport(null);
                }
                return;
            }
        }
        
        printHelpMessage();
    }

}
