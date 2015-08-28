package com.civilizer.test.dao;

import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;

import com.civilizer.dao.FileEntityDao;
import com.civilizer.test.helper.TestUtil;

public class DaoUrlTest extends DaoTest {
    
    public static void buildCreateDataSet() throws Exception {
        try {
            TestUtil.configure();
            
            DaoTest.setUpBeforeClass("classpath:datasource-context-h2-url.xml",
                    DaoUrlTest.class);

            FileEntityDao fileEntityDao = ctx.getBean("fileEntityDao",
                    FileEntityDao.class);
            assertNotNull(fileEntityDao);

            TestUtil.touchTestFilesForFileBox(fileEntityDao);
        } finally {
            DaoTest.tearDownAfterClass();
            
            TestUtil.unconfigure();
        }
        
    }

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    	TestUtil.configure();
    	DaoTest.setUpBeforeClass(
                "classpath:datasource-context-h2-url.xml"
                , DaoUrlTest.class
                );
    	TestUtil.unconfigure();
    }
    
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    	DaoTest.tearDownAfterClass();
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }        

    @Test
    public void testFindAllTags() {
        super.testFindAllTags();
    }

    @Test
    public void testTagsHierarchy() {
        super.testTagsHierarchy();
    }
    
    @Test
    public void testSaveTagWithParents() {
    	super.testSaveTagWithParents();
    }
    
    @Test
    public void testTagToFragmentRelationship() {
        super.testTagToFragmentRelationship();
    }
    
    @Test
    public void testFindAllFragments() {
        super.testFindAllFragments();
    }
    
    @Test
    public void testRelatedFragments() {
        super.testRelatedFragments();
    }

    @Test
    public void testFragmentToTagRelationship() {
        super.testFragmentToTagRelationship();
    }

    @Test
    public void testPersistNewTag() {
        super.testPersistNewTag();
    }

    @Test
    public void testPersistNewFragment() {
        super.testPersistNewFragment();
    }

    @Test
    public void testUpdateTag() {
        super.testUpdateTag();
    }
    
    @Test
    public void testRelateFragments() {
        super.testRelateFragments();
    }
    
    @Test
    public void testPagingFragments() {
    	super.testPagingFragments();
    }

    @Test
    public void testPagingFragmentsWithOrder() {
    	super.testPagingFragmentsWithOrder();
    }
    
    @Test
	public void testFileEntityQuery() {
    	super.testFileEntityQuery();
	}

}
