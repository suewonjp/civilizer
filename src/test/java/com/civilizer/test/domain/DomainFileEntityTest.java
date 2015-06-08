package com.civilizer.test.domain;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import com.civilizer.domain.FileEntity;
import com.civilizer.test.util.TestUtil;
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
	
//	@Test
//	public void testMethod_addToPathTree() {
//		final FileEntity f0 = new FileEntity("/xxx/some.txt");
//		final FileEntity f1 = new FileEntity("/xxx/yyy/whatever.txt");
//		final FileEntity f2 = new FileEntity("/another.txt");
//		final FileEntity f3 = new FileEntity("/xxx/yyy/zzz/another.txt");
//		
//		TreeNode<Object> tree = new DefaultTreeNode<Object>("");
//		assertEquals("", tree.getData());
//		
//		f0.addToPathTree(tree);
//		f1.addToPathTree(tree);
//		f2.addToPathTree(tree);
//		f3.addToPathTree(tree);
//		
//		Object[] expectedNodes = {
//				"", "xxx", "yyy", "zzz",// "some.txt", "whatever.txt", "another.txt", "another.txt",
//				f0, f1, f2, f3
//		};
//		
//		assertEquals(expectedNodes.length, tree.size());
//		
//		for (Object o : expectedNodes) {
//			assertEquals(true, tree.contains(o));
//		}
//		
//		assertEquals(false, tree.contains("someother.txt"));
//		
//		TreeNode<Object> n0 = tree.findDescendantWith("xxx");
//		Collection<TreeNode<Object>> ch0 = n0.getChildren();
//		assertEquals(2, ch0.size());
//		assertEquals(true, n0.contains(f0));
//		assertEquals(true, n0.contains(f1));
//		assertEquals(false, n0.contains(f2));
//		assertEquals(true, n0.contains(f3));
//		
//		TreeNode<Object> n1 = tree.findDescendantWith("yyy");
//		assertEquals(false, n1.contains(f0));
//		assertEquals(true, n1.contains(f1));
//		assertEquals(false, n1.contains(f2));
//		assertEquals(true, n1.contains(f3));
//		
//		TreeNode<Object> n2 = tree.findDescendantWith(f0);
//		assertEquals(true, ch0.contains(n1));
//		assertEquals(true, ch0.contains(n2));
//	}
	
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
