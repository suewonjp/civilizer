package com.civilizer.test.domain;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.civilizer.domain.TreeNode;
import com.civilizer.domain.DefaultTreeNode;

public class DomainTreeNodeTest {

	@Before
	public void setUp() throws Exception {
	}
	
	@Test
	public void testTraverse() {
	/*
		the tree
	
		       h
		     / | \
		    /  e  \
		   d        g
		 / | \      |
		/  |  \     f
	   a   b   c       
	
		can be iterated over in preorder (hdabcegf), postorder (abcdefgh), or breadth-first order (hdegabcf). 
	 */

		final TreeNode<Character> a = new DefaultTreeNode<Character> ('a');
		final TreeNode<Character> b = new DefaultTreeNode<Character> ('b');
		final TreeNode<Character> c = new DefaultTreeNode<Character> ('c');
		final TreeNode<Character> d = new DefaultTreeNode<Character> ('d');
		final TreeNode<Character> e = new DefaultTreeNode<Character> ('e');
		final TreeNode<Character> f = new DefaultTreeNode<Character> ('f');
		final TreeNode<Character> g = new DefaultTreeNode<Character> ('g');
		final TreeNode<Character> h = new DefaultTreeNode<Character> ('h');
		
		h.addChild(d);
		h.addChild(e);
		h.addChild(g);
		d.addChild(a);
		d.addChild(b);
		d.addChild(c);
		g.addChild(f);
		
		final TreeNode<Character> root = h;
		
		char[] chars = new char[8];
		
		root.traverse(new TreeNode.Traverser<TreeNode<Character>, char[]>() {
			int index = 0;
			
			@Override
			public boolean onNode(TreeNode<Character> node, char[] chars) {
				chars[index++] = node.getData();
				return true;
			}
		}, chars, TreeNode.TraverseOrder.PRE);
		assertEquals("hdabcegf", new String(chars));

		root.traverse(new TreeNode.Traverser<TreeNode<Character>, char[]>() {
			int index = 0;
			
			@Override
			public boolean onNode(TreeNode<Character> node, char[] chars) {
				chars[index++] = node.getData();
				return true;
			}
		}, chars, TreeNode.TraverseOrder.POST);
		assertEquals("abcdefgh", new String(chars));

		root.traverse(new TreeNode.Traverser<TreeNode<Character>, char[]>() {
			int index = 0;
			
			@Override
			public boolean onNode(TreeNode<Character> node, char[] chars) {
				chars[index++] = node.getData();
				return true;
			}
		}, chars, TreeNode.TraverseOrder.BREATH_FIRST);
		assertEquals("hdegabcf", new String(chars));
	}
	
	@Test
	public void testOtherMethods() {
		final TreeNode<Character> a = new DefaultTreeNode<Character> ('a');
		final TreeNode<Character> b = new DefaultTreeNode<Character> ('b');
		final TreeNode<Character> c = new DefaultTreeNode<Character> ('c');
		final TreeNode<Character> d = new DefaultTreeNode<Character> ('d');
		final TreeNode<Character> e = new DefaultTreeNode<Character> ('e');
		final TreeNode<Character> f = new DefaultTreeNode<Character> ('f');
		final TreeNode<Character> g = new DefaultTreeNode<Character> ('g');
		final TreeNode<Character> h = new DefaultTreeNode<Character> ('h');
		
		assertEquals(d, h.addChild(d));
		assertEquals(e, h.addChild(e));
		assertEquals(g, h.addChild(g));
		assertEquals(a, d.addChild(a));
		assertEquals(b, d.addChild(b));
		assertEquals(c, d.addChild(c));
		assertEquals(f, g.addChild(f));
		
		final TreeNode<Character> root = h;
		assertEquals(true, root.isRoot());
		
		assertEquals(8, root.size());
		assertEquals(true, root.contains('d'));
		assertEquals(false, root.contains('x'));
		assertEquals(true, a.isLeaf());
		assertEquals(true, f.isLeaf());
		assertEquals(false, g.isLeaf());
		assertEquals(false, h.isLeaf());
		assertEquals(true, d.isParentOf(c));
		
		char[] chars = new char[8];
		Character[] chars2 = new Character[8];
		
		chars2 = root.toArray(chars2, TreeNode.TraverseOrder.PRE);
		for (int i=0; i<chars.length; ++i) {
			chars[i] = chars2[i];
		}
		assertEquals("hdabcegf", new String(chars));

		chars2 = root.toArray(chars2, TreeNode.TraverseOrder.POST);
		for (int i=0; i<chars.length; ++i) {
			chars[i] = chars2[i];
		}
		assertEquals("abcdefgh", new String(chars));

		chars2 = root.toArray(chars2, TreeNode.TraverseOrder.BREATH_FIRST);
		for (int i=0; i<chars.length; ++i) {
			chars[i] = chars2[i];
		}
		assertEquals("hdegabcf", new String(chars));
		
		assertEquals(a, root.findDescendantWith('a'));
		assertEquals(null, root.findDescendantWith('z'));

		assertEquals(g, root.removeDescendantWith('g'));
		assertEquals(false, root.contains('g'));
		assertEquals(false, root.contains('f'));
		
		final TreeNode<Character> i = e.addChildWith('i');
		assertEquals(true, i.isLeaf());
		assertEquals(true, e.isParentOf(i));
		assertEquals(true, i.isDescendantOf(root));
	}

}
