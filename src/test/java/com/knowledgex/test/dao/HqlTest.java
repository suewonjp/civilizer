package com.knowledgex.test.dao;

import static org.junit.Assert.*;

import org.junit.*;
import org.apache.commons.logging.Log;
import org.springframework.context.support.GenericXmlApplicationContext;

import com.knowledgex.dao.FragmentDao;
import com.knowledgex.dao.TagDao;
import com.knowledgex.test.util.TestUtil;

public class HqlTest {
	
	private static Log log;
	private static GenericXmlApplicationContext ctx;
	
	private FragmentDao fragmentDao;
	private TagDao tagDao;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		log = TestUtil.newLogger(HqlTest.class);

		ctx = new GenericXmlApplicationContext();
		ctx.load("classpath:datasource-context-h2-embedded.xml");
		ctx.refresh();
		log.info("GenericXmlApplicationContext initialized OK");
	}

	@Before
	public void setUp() throws Exception {
		fragmentDao = ctx.getBean("fragmentDao", FragmentDao.class);
		assertNotNull(fragmentDao);
		log.info("fragmentDao initialized OK");

		tagDao = ctx.getBean("tagDao", TagDao.class);
		assertNotNull(tagDao);
		log.info("tagDao initialized OK");
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		//fail("Not yet implemented");
	}

}
