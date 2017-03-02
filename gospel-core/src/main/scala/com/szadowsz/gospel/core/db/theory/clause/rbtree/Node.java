package com.szadowsz.gospel.core.db.theory.clause.rbtree;

/**
 * Implements a Red Black Tree's node.
 *
 * Introduced by Paolo Contessi,
 * retrieved from: http://en.literateprograms.org/Red-black_tree_(Java)?oldid=16622
 *
 * @since 2.2
 *
 * @param <K>   It's the type of the key used to recall values
 * @param <V>   It's the type of the values stored in the tree
 */
public class Node<K extends Comparable<K>,V>
{
    public K key;
   public V value;
   public Node<K,V> left;
   public Node<K,V> right;
    public Node<K,V> parent;
    public Color color;

    public Node(K key, V value, Color nodeColor, Node<K,V> left, Node<K,V> right) {
        this.key = key;
        this.value = value;
        this.color = nodeColor;
        this.left = left;
        this.right = right;
        if (left  != null)  left.parent = this;
        if (right != null) right.parent = this;
        this.parent = null;
    }

    public Node<K,V> grandparent() {
        assert parent != null; // Not the root node
        assert parent.parent != null; // Not child of root
        return parent.parent;
    }

    public Node<K,V> sibling() {
        assert parent != null; // Root node has no sibling
        if (this == parent.left)
            return parent.right;
        else
            return parent.left;
    }

    public Node<K,V> uncle() {
        assert parent != null; // Root node has no uncle
        assert parent.parent != null; // Children of root have no uncle
        return parent.sibling();
    }

}
