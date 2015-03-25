package com.civilizer.test.domain;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import com.civilizer.domain.DefaultTreeNode;
import com.civilizer.domain.FileEntity;
import com.civilizer.domain.TreeNode;
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
	
	@Test
	public void testMethod_addToNameTree() {
		final FileEntity f0 = new FileEntity("/xxx/some.txt");
		final FileEntity f1 = new FileEntity("/xxx/yyy/whatever.txt");
		final FileEntity f2 = new FileEntity("/another.txt");
		final FileEntity f3 = new FileEntity("/xxx/yyy/zzz/another.txt");
		
		TreeNode<String> tree = new DefaultTreeNode<>("");
		assertEquals("", tree.getData());
		
		f0.addToNameTree(tree);
		f1.addToNameTree(tree);
		f2.addToNameTree(tree);
		f3.addToNameTree(tree);
		
		assertEquals(7, tree.size());
		
		assertEquals(true, tree.contains("xxx"));
		assertEquals(true, tree.contains("yyy"));
		assertEquals(true, tree.contains("zzz"));
		assertEquals(true, tree.contains("whatever.txt"));
		assertEquals(false, tree.contains("other.txt"));
		assertEquals(true, tree.contains("another.txt"));
		
		TreeNode<String> n0 = tree.findDescendantWith("xxx");
		Collection<TreeNode<String>> ch = n0.getChildren();
		assertEquals(2, ch.size());
		
		TreeNode<String> n1 = tree.findDescendantWith("yyy");
		TreeNode<String> n2 = tree.findDescendantWith("some.txt");
		assertEquals(true, ch.contains(n1));
		assertEquals(true, ch.contains(n2));
	}

}
