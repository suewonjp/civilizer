package com.civilizer.test.domain;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import com.civilizer.domain.FileEntity;
import com.civilizer.test.util.TestUtil;

public class DomainFileEntityTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testMethod_equals() {
		final FileEntity f0 = new FileEntity("/whatever.txt");
		final FileEntity f1 = new FileEntity("/whatever.txt");
		final FileEntity f2 = new FileEntity("/another.txt");
		
		assertEquals(true, f0.equals(f1));
		assertEquals(false, f2.equals(f1));
	}
	
	@Test
	public void testMethod_toFile() {
		{
			final FileEntity fe = new FileEntity("/whatever.txt");
			final File f = fe.toFile("");
			assertNotNull(f);
		}
		{
			final FileEntity fe = new FileEntity();
			final File f = fe.toFile("");
			assertNull(f);
		}
	}
	
	@Test
	public void testMethod_getFilesUnder() {
		// edge cases
		{
			Collection<FileEntity> fileEntities = FileEntity.getFilesUnder("~~~non-existing-directory~~~");
			assertNotNull(fileEntities);
			assertEquals(true, fileEntities.isEmpty());
		}
		
		// trivial cases
		TestUtil.configure();
		final String filesHome = TestUtil.getFilesHomePath();
		Collection<FileEntity> fileEntities = FileEntity.getFilesUnder(filesHome);
		assertNotNull(fileEntities);
		if (fileEntities.isEmpty()) {
			System.out.println("###### Warning : " + filesHome +
					" does not contain any file; the test is not effective");
		}
		for (FileEntity fe : fileEntities) {
			final File f = fe.toFile(filesHome);
//			System.out.println(f.getAbsolutePath());
			assertEquals(true, f.isFile());
		}
		TestUtil.unconfigure();
	}

}
