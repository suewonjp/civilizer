package com.civilizer.test.domain;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import com.civilizer.config.AppOptions;
import com.civilizer.domain.FileEntity;
import com.civilizer.test.util.TestUtil;

public class DomainFileEntityTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testMethod_equals() {
		final FileEntity f0 = new FileEntity("whatever.txt");
		final FileEntity f1 = new FileEntity("whatever.txt");
		final FileEntity f2 = new FileEntity("another.txt");
		
		assertEquals(true, f0.equals(f1));
		assertEquals(false, f2.equals(f1));
	}
	
	@Test
	public void testMethod_toFile() {
		{
			final FileEntity fe = new FileEntity("whatever.txt");
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
			Collection<FileEntity> fileEntries = FileEntity.getFilesUnder("~~~non-existing-directory~~~");
			assertNotNull(fileEntries);
			assertEquals(true, fileEntries.isEmpty());
		}
		
		// trivial cases
		TestUtil.configure();
		final String filesHome =
			System.getProperty(AppOptions.PRIVATE_HOME_PATH) + File.separatorChar + "files";
		Collection<FileEntity> fileEntries = FileEntity.getFilesUnder(filesHome);
		assertNotNull(fileEntries);
		if (fileEntries.isEmpty()) {
			System.out.println("###### Warning : " + filesHome +
					" does not contain any file; the test is not effective");
		}
		for (FileEntity fe : fileEntries) {
			final File f = fe.toFile(filesHome);
//			System.out.println(f.getAbsolutePath());
			assertEquals(true, f.isFile());
		}
		TestUtil.unconfigure();
	}

}
