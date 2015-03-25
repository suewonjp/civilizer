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
	
	int size();
	
	boolean contains(E o);
	
	E[] toArray(E[] a, TraverseOrder traverseOrder);
	
	void clear();
	
	boolean equals(Object o);
	
	int hashCode();
	
	E getData();
	
	void setData(E data);
	
	boolean isLeaf();
	
	Collection<TreeNode<E>> getChildren();
	
	TreeNode<E> findDescendantWith(E o);
		
	void addChild(TreeNode<E> child);

	void removeChild(TreeNode<E> child);

	void addChildWith(E o);
	
	void removeChildWith(E o);
	
	<U> boolean traverse(Traverser<TreeNode<E>, U> traverser, U callerData, TraverseOrder traverseOrder);
	
}
