package com.civilizer.test.utils

import spock.lang.*

import com.civilizer.utils.DefaultTreeNode;
import com.civilizer.utils.TreeNode;

class TreeNodeSpec extends spock.lang.Specification {
    
    TreeNode<Character> root, a, b, c, d, e, f, g, h
    
    def setup() {
         /*
             The generated tree structure will look like the following figure
         
                    h
                  / | \
                 /  e  \
                d        g
              / | \      |
             /  |  \     f
            a   b   c
         
            can be iterated over in preorder (hdabcegf), postorder (abcdefgh), or breadth-first order (hdegabcf).
         */
 
         a = new DefaultTreeNode<Character> ('a');
         b = new DefaultTreeNode<Character> ('b');
         c = new DefaultTreeNode<Character> ('c');
         d = new DefaultTreeNode<Character> ('d');
         e = new DefaultTreeNode<Character> ('e');
         f = new DefaultTreeNode<Character> ('f');
         g = new DefaultTreeNode<Character> ('g');
         h = new DefaultTreeNode<Character> ('h');
     
         assert d == h.addChild(d);
         assert e == h.addChild(e);
         assert g == h.addChild(g);
         assert a == d.addChild(a);
         assert b == d.addChild(b);
         assert c == d.addChild(c);
         assert f == g.addChild(f);
         
         root = h;
    }
    
    def "Methods for the root"() {
        expect:
            root.isRoot()
            8 == root.size()
    }
    
    def "Queries regarding the tree structure"() {
        expect:
            root.contains('d')
            ! root.contains('x')
            a.isLeaf()
            f.isLeaf()
            ! g.isLeaf()
            ! h.isLeaf()
            d.isParentOf(c)
            a == root.findDescendantWith('a')
            null == root.findDescendantWith('z')
    }
    
    def "Removing a node"() {
        when: "A node removed"           
            g == root.removeDescendantWith('g')
        then: "The removed one and its descendants won't be members of the tree"
            ! root.contains('g')
            ! root.contains('f')
    }
    
    def "Adding a node"() {
        when: "A node added"
            final TreeNode<Character> i = e.addChildWith('i')
        then: "The tree structure is maintained"
            i.isLeaf()
            e.isParentOf(i)
            i.isDescendantOf(root)
    }
    
    def "Tree Traversal"() {        
        char[] chars = new char[8];

        when: "Preorder traversal"
            root.traverse(new TreeNode.Traverser<TreeNode<Character>, char[]>() {
                int index = 0;
    
                @Override
                public boolean onNode(TreeNode<Character> node, char[] _chars) {
                    _chars[index++] = node.getData();
                    return true;
                }
            }, chars, TreeNode.TraverseOrder.PRE);    
        then:
            "hdabcegf" == new String(chars)

        when: "Postorder traversal"
            root.traverse(new TreeNode.Traverser<TreeNode<Character>, char[]>() {
                int index = 0;
    
                @Override
                public boolean onNode(TreeNode<Character> node, char[] _chars) {
                    _chars[index++] = node.getData();
                    return true;
                }
            }, chars, TreeNode.TraverseOrder.POST);
        then:
            "abcdefgh" == new String(chars)
        
        when: "Breath-First traversal"
            root.traverse(new TreeNode.Traverser<TreeNode<Character>, char[]>() {
                int index = 0;
    
                @Override
                public boolean onNode(TreeNode<Character> node, char[] _chars) {
                    _chars[index++] = node.getData();
                    return true;
                }
            }, chars, TreeNode.TraverseOrder.BREATH_FIRST);
        then:
            "hdegabcf" == new String(chars)
    }
    
    def "The tree can be flatten into an array"() {
        String[] arr = new String[8];
        
        when: "Converted by preorder traversal"
            arr = root.toDataArray(arr, TreeNode.TraverseOrder.PRE);
        then: "The order is maintained"
            "hdabcegf" == arr.inject("") { result, c -> result + c }             

        when: "Converted by postorder traversal"
            arr = root.toDataArray(arr, TreeNode.TraverseOrder.POST);
        then:
            "abcdefgh" == arr.inject("") { result, c -> result + c }             

        when: "Converted by breath-first traversal"
            arr = root.toDataArray(arr, TreeNode.TraverseOrder.BREATH_FIRST);
        then:
            "hdegabcf" == arr.inject("") { result, c -> result + c }             
    }
    
}

