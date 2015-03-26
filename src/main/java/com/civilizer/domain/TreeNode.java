package com.civilizer.domain;

import java.util.*;

public interface TreeNode<E> {
	
	public enum TraverseOrder {
		PRE,
		POST,
		BREATH_FIRST
	}
	
	@SuppressWarnings("hiding")
	public interface Traverser<TreeNode, U> {
		boolean onNode(TreeNode node, U callerData);
	}
	
	<U> boolean traverse(Traverser<TreeNode<E>, U> traverser, U callerData, TraverseOrder traverseOrder);
	
	int size();
	
	boolean contains(E o);
	
	E[] toArray(E[] a, TraverseOrder traverseOrder);
	
	void clear();
	
	E getData();
	
	void setData(E data);
	
	boolean isLeaf();
	
	Collection<TreeNode<E>> getChildren();
	
	TreeNode<E> findDescendantWith(E o);
		
	TreeNode<E> addChild(TreeNode<E> child);

	TreeNode<E> removeChild(TreeNode<E> child);

	TreeNode<E> addChildWith(E o);
	
	TreeNode<E> removeDescendantWith(E o);
	
	boolean isParentOf(TreeNode<E> n);

	boolean isDescendantOf(TreeNode<E> n);
	
}
