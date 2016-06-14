package com.civilizer.test.dao

import spock.lang.*;

import com.civilizer.domain.*;
import com.civilizer.test.helper.TestUtil;

class DaoUrlSpec extends DaoSpecBase {
    
    def setupSpec() {
        TestUtil.configure();
        DaoSpecBase.setupApplicationContext(
            "classpath:datasource-context-h2-url.xml");
        TestUtil.unconfigure();
    }
    
    def cleanupSpec() {
        DaoSpecBase.cleanupApplicationContext();
    }
    
    def ".executeQueryForResult"() {
        expect:
            fragmentDao.executeQueryForResult("from Fragment") ==
                fragmentDao.findAll(true)
            tagDao.executeQueryForResult("from Tag") ==
                tagDao.findAll()
    }
    
    def "Persist a new tag"() {
        given: "A new tag"
            def tag = newTag(null);
        expect:
            tag.getId() == null
            
        when: "Save it"
            tagDao.save(tag);
        then:
            tag.getId() != null
    }
    
    def "Persist a new fragment"() {
        given: "A new fragment"
            def frg = newFragment();
        and: "Associdate a new tag to this fragment"
            def tag = newTag(null);
            frg.addTag(tag);
        expect:
            frg.getId() == null
            
        when: "Save it"
            fragmentDao.save(frg);
        then:
            frg.getId() != null
        and: "Check if the added tag has been persisted too"
            tag.getId() != null
            tagDao.findById(tag.getId())
            
        when: "Retrieve a fragment from the associated tag"
            def fs = fragmentDao.findByTagId(tag.getId(), true);
        then: "The retreved fragment is identical to our target"
            1 == fs.size()
            fs[0] == frg
    }
    
    def "Persist FileEntity objects"() {
        given: "File Box path"
            final String filesHome = TestUtil.getFilesHomePath();
        and: "A new FileEntity"
            final def fe = new FileEntity("~~~~temp~new~file~~~~");
        and: "The file corresponding the FileEntity"
            final def f = fe.toFile(filesHome);
            assert f != null;
        and: "Ensure the file does not exist yet"
            if (f.exists())
                f.delete();
            assert !fe.persisted(filesHome)
            
        when: "Save the FileEntity"
            fileEntityDao.save(fe);
        then:
            fe.getId() != null
            fe == fileEntityDao.findById(fe.getId())
            
        when: "Create a file corresponding to the FileEntity"
            assert f.createNewFile()
        then:
            notThrown IOException
        and: "The file on the file system and the FileEntity are coherent"
            fe.persisted(filesHome)
            
        when: "Delete the FileEntity"
            fileEntityDao.delete(fe);
        then: "It's not managed by the DB, but still persisted on the file system"
            fe.persisted(filesHome)
            
        when: "Delete the file on the file system"
            f.delete();
        then:
            ! fe.persisted(filesHome);
    }

}
