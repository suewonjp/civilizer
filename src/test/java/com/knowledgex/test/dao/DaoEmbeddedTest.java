package com.knowledgex.test.dao;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class DaoEmbeddedTest extends DaoTest {
     
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        DaoTest.setUpBeforeClass(
                "classpath:datasource-context-h2-embedded.xml"
                , DaoEmbeddedTest.class
                );
    }
    
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
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
    public void testUpdateFragment() {
        super.testUpdateFragment();
    }
    
    @Test
    public void testFindFragmentsByTagIds() {
        super.testFindFragmentsByTagIds();
    }
    
    @Test
    public void testPagingFragments() {
    	super.testPagingFragments();
    }

}
