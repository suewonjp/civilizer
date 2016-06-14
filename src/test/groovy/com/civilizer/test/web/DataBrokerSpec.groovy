package com.civilizer.test.web

import spock.lang.*;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils

import com.civilizer.test.dao.DaoEmbeddedSpec;
import com.civilizer.test.helper.TestUtil;
import com.civilizer.utils.FsUtil;
import com.civilizer.web.view.DataBrokerBean;

@Subject(DataBrokerBean)
class DataBrokerSpec extends spock.lang.Specification {
    
    def setupSpec() {
        DaoEmbeddedSpec.buildCreateDataSet();
    }
    
    def cleanupSpec() {
        FileUtils.deleteQuietly(new File(TestUtil.getTempFolderPath()));
    }
    
    def setup() {
        TestUtil.configure();
    }
    
    def cleanup() {
        TestUtil.unconfigure();
    }
    
    def "Data export"() {
        given: "A temporary folder where to export data"
            final String tmpPath = FsUtil.concatPath(TestUtil.getTempFolderPath(), DataBrokerBean.exportFolderName);
            
        when: "Export the data"
            final String exportFilePath = DataBrokerBean.exportData();
        then:
            notThrown Exception
            DataBrokerBean.getExportFilePath() == exportFilePath
        and: "The exported file has been properly stored on the file system?"
            final File exportFile = new File(exportFilePath);
            exportFile
            new File(tmpPath) == exportFile.getParentFile()
            exportFile.isAbsolute()
            exportFile.isFile()
            
        when: "Uncompress the exported file to inspect the data inside it"
            final String pathWhereToUncompress = FsUtil.concatPath(tmpPath, "uncmp");
            FsUtil.uncompressToFolder(exportFilePath, pathWhereToUncompress);
        then: "2 files should be extracted from the exported file"
            notThrown Exception
            final String[] paths = new File(pathWhereToUncompress).list();
            2 == paths.length;
        and: '''One is the DB file and the other is the File Box folder.
                Compare only the names here.
             '''
            paths.each {
                final String path = FsUtil.concatPath(pathWhereToUncompress, it);
                final File f = new File(path);
                if (f.isFile()) { // DB
                    it == FilenameUtils.getName(TestUtil.getDatabaseFilePath())
                }
                else if (f.isDirectory()) { // File Box
                    it == FilenameUtils.getName(TestUtil.getFilesHomePath())
                }
            }
    }
    
    def "Data import"() {
        given: "The path from which to import data"
            final String importFolderPath = DataBrokerBean.getImportFolderPath();
            
        when: "Export the data first"
            final String exportFilePath = DataBrokerBean.exportData();
        then:
            exportFilePath
            
        when: "Make a copy of the DB file." 
            // Reason : The original DB file might be modified
            final File tmpDatabaseFile = new File(FsUtil.concatPath(TestUtil.getTempFolderPath(), "tmp-db-file"));
            FileUtils.copyFile(new File(TestUtil.getDatabaseFilePath()), tmpDatabaseFile);
        then:
            tmpDatabaseFile.isFile()
            
        when: "Move the exported file into the path from which to import it"
            // This is a necessary step because the data can't be imported from an arbitrary path.
            final File importFolder = new File(importFolderPath);
            FsUtil.createUnexistingDirectory(importFolder);
            final File importFile = new File(FsUtil.concatPath(importFolderPath, DataBrokerBean.importFileName));
            final File exportFile = new File(exportFilePath);            
            FileUtils.moveFile(exportFile, importFile);
        and: "Prepare the data to import" 
            final String uncompressPath = DataBrokerBean.prepareDataImport();
            // Note that we have one more step left for the complete import"
        then: "Uncompress the file to inspect the data inside it"
            uncompressPath
            final File uncompressFolder = new File(uncompressPath)
            uncompressFolder.isDirectory()
            uncompressFolder.list().each {
                final String path = FsUtil.concatPath(uncompressPath, FilenameUtils.getName(it));
                final File fileToImport = new File(path);
                if (fileToImport.isFile()) {
                    // Compare the contents between the original and imported DB
                    FileUtils.contentEquals(tmpDatabaseFile, fileToImport)
                }
                else if (fileToImport.isDirectory()) {
                    final File tgtFile = new File(TestUtil.getFilesHomePath());
                    tgtFile && tgtFile.isDirectory()
                    // Compare the contents between the original and imported File Box
                    FsUtil.contentEquals(tgtFile, fileToImport)
                }
            }
            
        if (System.getProperty("os.name").toLowerCase().contains("win"))
            // DataBrokerBean.commitImportData() doesn't work on Windows due to the following bug.
            // http://bugs.java.com/bugdatabase/view_bug.do?bug_id=4715154
            return;
            
        when: "Delete original data"
            final File dbFile = new File(TestUtil.getDatabaseFilePath());
            final File fbFolder = new File(TestUtil.getFilesHomePath());
            FileUtils.deleteQuietly(dbFile);
            FileUtils.deleteQuietly(fbFolder);
        then: "Confirm the original data are gone"
            ! dbFile.exists()
            ! fbFolder.exists()
            
        and: "Perform the final step"
            DataBrokerBean.importData(uncompressPath);
        then: "The temporary folder shouldn' exist"
            ! uncompressFolder.isDirectory()
        and: "The imported data should exist in the right places"
            dbFile.isFile()
            fbFolder.isDirectory()
    }

}
