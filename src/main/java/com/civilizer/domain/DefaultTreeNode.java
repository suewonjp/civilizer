package com.civilizer.domain;

import java.lang.reflect.Array;
import java.util.*;

public class DefaultTreeNode<E> implements TreeNode<E> {
	
	private List<TreeNode<E>> children = Collections.emptyList();
	private TreeNode<E> parent;
	private E data;
	
	public DefaultTreeNode() {
		super();
	}

	public DefaultTreeNode(E data) {
		super();
		setData(data);
	}
	
	@Override
	public <U> boolean traverse(Traverser<TreeNode<E>, U> traverser, U callerData, TraverseOrder traverseOrder) {
		switch (traverseOrder) {
		case PRE:
			return traverseByPreOrder(traverser, callerData);
		case POST:
			return traverseByPostOrder(traverser, callerData);
		case BREATH_FIRST:
			return traverseByBreathFirstOrder(traverser, callerData);
		}
		return true;
	}
	
	protected <U> boolean traverseByPreOrder(Traverser<TreeNode<E>, U> traverser, U callerData) {
		final Deque<TreeNode<E>> dq = new LinkedList<>();
		dq.add(this);
		
		while (! dq.isEmpty()) {
			TreeNode<E> n = dq.pollLast();
			
			if (! traverser.onNode(n, callerData)) {
				// No more iteration if the caller callback returns false;
				return false;
			}
			
			final List<TreeNode<E>> children = (List<TreeNode<E>>) n.getChildren();
			final int childrenCount = children.size();
			for (int i=childrenCount-1; i>=0; --i) {
				final TreeNode<E> child = (TreeNode<E>) children.get(i);
				dq.add(child);
			}
		}
		
		return true;
	}
	
	protected <U> boolean traverseByPostOrder(Traverser<TreeNode<E>, U> traverser, U callerData) {
		final Deque<TreeNode<E>> dq = new LinkedList<>();
		final Deque<TreeNode<E>> dq2 = new LinkedList<>();
		dq.add(this);
		
		while (! dq.isEmpty()) {
			TreeNode<E> n = dq.pollLast();
			dq2.add(n);
			
			final List<TreeNode<E>> children = (List<TreeNode<E>>) n.getChildren();
			final int childrenCount = children.size();
			for (int i=0; i<childrenCount; ++i) {
				final TreeNode<E> child = (TreeNode<E>) children.get(i);
				dq.add(child);
			}
		}
		
		while (! dq2.isEmpty()) {
			TreeNode<E> n = dq2.pollLast();
			
			if (! traverser.onNode(n, callerData)) {
				// No more iteration if the caller callback returns false;
				return false;
			}
		}
		
		return true;
	}

	protected <U> boolean traverseByBreathFirstOrder(Traverser<TreeNode<E>, U> traverser, U callerData) {
		final Deque<TreeNode<E>> dq = new LinkedList<>();
		dq.add(this);
		
		while (! dq.isEmpty()) {
			TreeNode<E> n = dq.pollFirst();
			
			if (! traverser.onNode(n, callerData)) {
				// No more iteration if the caller callback returns false;
				return false;
			}
			
			final List<TreeNode<E>> children = (List<TreeNode<E>>) n.getChildren();
			final int childrenCount = children.size();
			for (int i=0; i<childrenCount; ++i) {
				final TreeNode<E> child = (TreeNode<E>) children.get(i);
				dq.add(child);
			}
		}
		
		return true;
	}

	@Override
	public int size() {
		final int[] counter = { 0 };
		traverseByBreathFirstOrder(new Traverser<TreeNode<E>, int[]>() {
			@Override
			public boolean onNode(TreeNode<E> node, int[] counter) {
				counter[0] = counter[0] + 1;
				return true;
			}
		}, counter);
		return counter[0];
	}

	@Override
	public boolean contains(final E o) {
		final boolean[] found = { false };
		traverseByBreathFirstOrder(new Traverser<TreeNode<E>, boolean[]>() {
			@Override
			public boolean onNode(TreeNode<E> node, boolean[] found) {
				if (node.getData().equals(o)) {
					found[0] = true;
					return false;
				}
				return true;
			}
		}, found);
		return found[0];
	}

	@SuppressWarnings("unchecked")
	@Override
	public E[] toDataArray(E[] a, TraverseOrder traverseOrder) {
		final int size = size();
		if (a.length < size) {
			a = (E[]) Array.newInstance(a.getClass().getComponentType(), size);
		}
		
		traverse(new Traverser<TreeNode<E>, E[]>() {
			int index = 0;
			
			@Override
			public boolean onNode(TreeNode<E> node, E[] a) {
				a[index++] = node.getData();
				return true;
			}
		}, a, traverseOrder);
		
		return a;
	}

	@Override
	public Object[] toArray(TraverseOrder traverseOrder) {
		final int size = size();
		Object[] a = new Object[size];
		traverse(new Traverser<TreeNode<E>, Object[]>() {
			int index = 0;
			
			@Override
			public boolean onNode(TreeNode<E> node, Object[] a) {
				a[index++] = node;
				return true;
			}
		}, a, traverseOrder);
		return a;
	}

	@Override
	public void clear() {
		children.clear();
		parent = null;
		data = null;
	}
	
	@Override
	public E getData() {
		return data;
	}

	@Override
	public void setData(E data) {
		this.data = data;
	}
	
	@Override
	public boolean isLeaf() {
		return children.isEmpty();
	}

	@Override
	public boolean isRoot() {
		return parent == null;
	}

	@Override
	public TreeNode<E> getParent() {
		return parent;
	}

	@Override
	public void setParent(TreeNode<E> p) {
		this.parent = p;
	}

	@Override
	public Collection<TreeNode<E>> getChildren() {
		return children;
	}

	@SuppressWarnings("unchecked")
	@Override
	public TreeNode<E> findDescendantWith(final E o) {
		final Object[] found = { null };
		traverseByBreathFirstOrder(new Traverser<TreeNode<E>, Object[]>() {
			@Override
			public boolean onNode(TreeNode<E> node, Object[] found) {
				if (node.getData().equals(o)) {
					found[0] = node;
					return false;
				}
				return true;
			}
		}, found);
		return (TreeNode<E>) found[0];
	}

	@Override
	public TreeNode<E> addChild(TreeNode<E> child) {
		if (children.isEmpty()) {
			children = new ArrayList<>();
		}
		children.add(child);
		child.setParent(this);
		return child;
	}
	
	@Override
	public TreeNode<E> removeChild(TreeNode<E> child) {
		if (child != null && children.remove(child)) {
			child.setParent(null);
			return child;
		}
		return null;
	}

	@Override
	public TreeNode<E> addChildWith(E o) {
		return addChild(new DefaultTreeNode<E>(o));
	}

	@Override
	public TreeNode<E> removeDescendantWith(E o) {
		TreeNode<E> node = findDescendantWith(o);
		return removeChild(node);
	}
	
	@Override
	public String toString() {
		final String[] result = { "\n" };
		traverseByBreathFirstOrder(new Traverser<TreeNode<E>, String[]>() {
			@Override
			public boolean onNode(TreeNode<E> node, String[] result) {
				result[0] += node.getData().toString() + "\n";
				return true;
			}
		}, result);
		return result[0];
	}

	@Override
	public boolean isParentOf(TreeNode<E> n) {
		if (n == null) {
			return false;
		}
		return this.equals(n.getParent());
	}

	@Override
	public boolean isDescendantOf(TreeNode<E> n) {
		if (n == null) {
			return false;
		}
		TreeNode<E> tmp = findDescendantWith(data);
		return this.equals(tmp);
	}
	
}
