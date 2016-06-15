package com.civilizer.test.dao

import spock.lang.*;

import javax.sql.DataSource;

import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import com.civilizer.dao.*;
import com.civilizer.domain.*;
import com.civilizer.test.helper.TestUtil;

@Ignore
class DaoSpecBase extends spock.lang.Specification {
    
    static def GenericXmlApplicationContext ctx;
    
    FragmentDao fragmentDao;
    TagDao tagDao;
    FileEntityDao fileEntityDao;
    
    def temporalTags = new ArrayList<Tag>();
    def temporalFragments = new ArrayList<Fragment>();
    
    static def setupApplicationContext(String dataSourceContextPath) {
        TestUtil.newLogger();
        ctx = new GenericXmlApplicationContext();
        ctx.load(dataSourceContextPath);
        ctx.refresh();
    }
    
    static def cleanupApplicationContext() {
        ctx.close();
    }
    
    static void runSqlScript(String ... scripts) {
        DataSource dataSource = ctx.getBean("dataSource", DataSource.class);
        assert dataSource
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        scripts.each {
            def script = new ClassPathResource(it);
            assert script
            populator.addScript(script);
        }
        DatabasePopulatorUtils.execute(populator, dataSource);
    }
    
    void deleteAllTemporalObjects() {
        temporalTags.each {
            if (tagDao.findById(it.getId()))
                tagDao.delete(it);
        }
        temporalFragments.each {
            if (fragmentDao.findById(it.getId()))
                fragmentDao.delete(it);
        }
    }
    
    Tag newTag(String name) {
        if (name == null) {
            name = "new tag " + temporalTags.size();
        }
        else {
            name = name.trim();
        }
        Tag result = new Tag(name);
        assert result;
        temporalTags.add(result);
        result;
    }
    
    Fragment newFragment() {
        Fragment frg = new Fragment(
            "new fragment " + temporalFragments.size(),
            "Some content...", null);
        assert frg;
        temporalFragments.add(frg);
        frg;
        
    }
    
    Fragment newFragment(String title, String content) {
        Fragment frg = new Fragment(title, content, null);
        assert frg;
        temporalFragments.add(frg);
        frg;
    }
    
    Long getAndValidateId(Fragment f) {
        def id = f.getId();
        assert id != null && id >= 0;
        id;
    }

    Long getAndValidateId(Tag f) {
        def id = f.getId();
        assert id != null;
        id;
    }
    
    void findAllAncestorsOfTag(Tag tag, Set<Tag> idsInOut) {
        final List<Tag> parents = tagDao.findParentTags(tag.getId());
        idsInOut.addAll(parents);
        for (Tag t : parents) {
            findAllAncestorsOfTag(t, idsInOut);
        }
    }
    
    void doSetup() {
        TestUtil.configure();
        
        fragmentDao = ctx.getBean("fragmentDao", FragmentDao.class);
        assert fragmentDao;
        tagDao = ctx.getBean("tagDao", TagDao.class);
        assert tagDao;
        fileEntityDao = ctx.getBean("fileEntityDao", FileEntityDao.class);
        assert fileEntityDao;
        
        // Make sure all test files exist on the file system
        TestUtil.touchTestFilesForFileBox(fileEntityDao);
    }
    
    void doCleanup() {
        deleteAllTemporalObjects();
        TestUtil.unconfigure();
    }
    
    def setup() {
        doSetup();
    }
    
    def cleanup() {
        doCleanup();
    }
    
}
