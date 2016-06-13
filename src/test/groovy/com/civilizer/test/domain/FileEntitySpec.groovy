package com.civilizer.test.domain

import spock.lang.*;

import com.civilizer.domain.FileEntity;
import com.civilizer.utils.Pair;
import com.civilizer.test.helper.TestUtil;

@Subject(FileEntity)
class FileEntitySpec extends spock.lang.Specification {
    
    def "Equality"() {
        given:
            final FileEntity x = new FileEntity("/whatever.txt");
            final FileEntity y = new FileEntity("/whatever.txt");
            final FileEntity z = new FileEntity("/whatever.txt");
        expect:
            // REFLEXIVE:
            x.equals(x)
            // SYMMETRIC:
            x.equals(y)
            y.equals(x)
            // TRANSITIVE:
            y.equals(z)
            x.equals(z)
            // CONSISTENT:
            x.equals(y)
            x.equals(y)
            // For any non-null reference value x, x.equals(null) should return false.
            ! x.equals(null)
            // Returns false for any of different file names
            ! x.equals(new FileEntity("/another.txt"))
    }
    
    def "FileEntity.toFile"() {
        File f;
        when: "Called with a valid prefix"
            f = new FileEntity("/whatever.txt").toFile();
        then: "Returns a valid File object"
            f != null
        when: "Called for a NULL object"
            f = new FileEntity().toFile();
        then: "Returns NULL"
            f == null
        when: "Called for a NULL object"
            f = new FileEntity().toFile("/whatever.txt");
        then: "Returns NULL"
            f == null
    }
    
    def "FileEntity.getFilesUnder --- edge cases"() {
        when: "Called for a non-existing directory"
            Collection<FileEntity> fileEntities = 
                FileEntity.getFilesUnder("~~~non-existing-directory~~~");
        then: "Returns an empty collection"
            fileEntities.isEmpty();
    }

    def "FileEntity.getFilesUnder --- normal cases"() {
        setup:
            TestUtil.configure();
        and: "File Box directory"
            final String filesHome = TestUtil.getFilesHomePath();
            
        when: "Called for the File Box directory"
            Collection<FileEntity> fileEntities = FileEntity.getFilesUnder(filesHome);
        then: "Returns a non-empty collection"
            ! fileEntities.isEmpty()
            
        when: "Check each of the files under the File Box"
            fileEntities.each {
                // The path separator of FileEntity name should be the Unix separator.
                assert ! it.getFileName().contains("\\")
                
                // Can convert each FileEntity object to a valid File object 
                assert it.toFile(filesHome).isFile()
            }
        then: ""
        
        cleanup:
            TestUtil.unconfigure();
    }
    
    def "FileEntity.splitPath"() {
        Pair<String, String> splitPath;
        
        when: "Called for the root path"
            splitPath = new FileEntity("/").splitName();
        then: "Returns an empty paths"
            "" == splitPath.getFirst()
            "" == splitPath.getSecond()
            
        when:
            splitPath = new FileEntity("/xxx").splitName();
        then:
            "" == splitPath.getFirst()
            "xxx" == splitPath.getSecond()
            
        when:
            splitPath = new FileEntity("/xxx/yyy/zzz").splitName();
        then:
            "/xxx/yyy" == splitPath.getFirst()
            "zzz" == splitPath.getSecond()
    }
}
