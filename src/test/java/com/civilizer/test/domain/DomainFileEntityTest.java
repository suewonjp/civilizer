package com.civilizer.test.domain;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import com.civilizer.domain.FileEntity;
import com.civilizer.test.helper.TestUtil;
import com.civilizer.utils.Pair;

public class DomainFileEntityTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testMethod_equals() {
		final FileEntity f0 = new FileEntity("/whatever.txt");
		final FileEntity f1 = new FileEntity("/whatever.txt");
		final FileEntity f2 = new FileEntity("/another.txt");
		
		assertEquals(true, f0.equals(f0));
		assertEquals(false, f0.equals(null));
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
		try {
            TestUtil.configure();
            final String filesHome = TestUtil.getFilesHomePath();
            Collection<FileEntity> fileEntities = FileEntity.getFilesUnder(filesHome);
            assertNotNull(fileEntities);
            if (fileEntities.isEmpty()) {
                System.out.println("###### Warning : "
                                + filesHome
                                + " does not contain any file; the test is not effective");
            }
            for (FileEntity fe : fileEntities) {
                // the path separator of FileEntity name should be a Unix separator.
                assertEquals(false, fe.getFileName().contains("\\"));
                final File f = fe.toFile(filesHome);
                //			System.out.println(f.getAbsolutePath());
                assertEquals(true, f.isFile());
            }
        }
		finally {
		    TestUtil.unconfigure();
		}
	}
	
	@Test
	public void testMethod_splitName() {
		Pair<String, String> splitPath = null;
		{
			final FileEntity f = new FileEntity("/");
			splitPath = f.splitName();
			assertEquals("", splitPath.getFirst());
			assertEquals("", splitPath.getSecond());
		}
		{
			final FileEntity f = new FileEntity("/xxx");
			splitPath = f.splitName();
			assertEquals("", splitPath.getFirst());
			assertEquals("xxx", splitPath.getSecond());
		}
		{
			final FileEntity f = new FileEntity("/xxx/yyy/zzz");
			splitPath = f.splitName();
			assertEquals("/xxx/yyy", splitPath.getFirst());
			assertEquals("zzz", splitPath.getSecond());
		}
	}

}
