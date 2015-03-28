package com.civilizer.test.dao;

import org.junit.After;
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

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }
    
    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }
    
    @Test
    public void testExecuteArbitraryQuery() {
        super.testExecuteArbitraryQuery();
    }
    
    @Test
	public void testCountAll() {
		super.testCountAll();
	}
    
    @Test
    public void testCountFragmentsPerTag() {
        super.testCountFragmentsPerTag();
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
    public void testRelateFragments() {
        super.testRelateFragments();
    }
    
    @Test
    public void testFindFragmentsByTagIds() {
        super.testFindFragmentsByTagIds();
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
    public void testExplicitQuery() {
    	super.testExplicitQuery();
    }
    
    @Test
	public void testFileEntityQuery() {
    	super.testFileEntityQuery();
	}

}