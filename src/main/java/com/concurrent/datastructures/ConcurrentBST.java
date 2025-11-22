package com.concurrent.datastructures;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Concurrent BST with fine-grained hand-over-hand (lock coupling) locking.
 * Each node has its own lock, allowing multiple threads to operate on different parts of the tree.
 */
public class ConcurrentBST implements ConcurrentSet {

    private static class Node {
        int key;
        volatile Node left, right;
        final ReentrantLock lock = new ReentrantLock();

        Node(int k) {
            key = k;
        }

        void lock() { lock.lock(); }
        void unlock() { lock.unlock(); }
    }

    // Sentinel root node (never removed) - simplifies edge cases
    private final Node root = new Node(Integer.MIN_VALUE);

    @Override
    public boolean contains(int key) {
        // Optimistic read - no locking for contains (lock-free traversal)
        Node curr = root.right;
        while (curr != null) {
            if (key == curr.key) return true;
            else if (key < curr.key) curr = curr.left;
            else curr = curr.right;
        }
        return false;
    }

    @Override
    public boolean insert(int key) {
        Node parent = root;
        parent.lock();
        
        try {
            Node curr = root.right;
            
            // If tree is empty
            if (curr == null) {
                root.right = new Node(key);
                return true;
            }
            
            // Hand-over-hand locking down the tree
            curr.lock();
            try {
                while (true) {
                    if (key == curr.key) {
                        return false; // Already exists
                    } else if (key < curr.key) {
                        if (curr.left == null) {
                            curr.left = new Node(key);
                            return true;
                        }
                        // Move down - lock child, unlock parent
                        Node next = curr.left;
                        next.lock();
                        parent.unlock();
                        parent = curr;
                        curr = next;
                    } else {
                        if (curr.right == null) {
                            curr.right = new Node(key);
                            return true;
                        }
                        // Move down - lock child, unlock parent
                        Node next = curr.right;
                        next.lock();
                        parent.unlock();
                        parent = curr;
                        curr = next;
                    }
                }
            } finally {
                curr.unlock();
            }
        } finally {
            parent.unlock();
        }
    }

    @Override
    public boolean remove(int key) {
        Node grandparent = null;
        Node parent = root;
        parent.lock();
        
        try {
            Node curr = root.right;
            if (curr == null) return false;
            
            curr.lock();
            try {
                // Find node to delete with hand-over-hand locking
                while (curr.key != key) {
                    Node next;
                    if (key < curr.key) {
                        next = curr.left;
                    } else {
                        next = curr.right;
                    }
                    
                    if (next == null) {
                        return false; // Key not found
                    }
                    
                    next.lock();
                    if (grandparent != null) {
                        grandparent.unlock();
                    }
                    grandparent = parent;
                    parent = curr;
                    curr = next;
                }
                
                // Found the node - now delete it
                // Case 1: No children or one child
                if (curr.left == null || curr.right == null) {
                    Node child = (curr.left != null) ? curr.left : curr.right;
                    
                    if (parent.left == curr) {
                        parent.left = child;
                    } else {
                        parent.right = child;
                    }
                    return true;
                }
                
                // Case 2: Two children - find inorder successor
                Node succParent = curr;
                Node succ = curr.right;
                succ.lock();
                
                try {
                    while (succ.left != null) {
                        Node next = succ.left;
                        next.lock();
                        succParent.unlock();
                        succParent = succ;
                        succ = next;
                    }
                    
                    // Copy successor's key to current node
                    curr.key = succ.key;
                    
                    // Remove successor
                    if (succParent == curr) {
                        succParent.right = succ.right;
                    } else {
                        succParent.left = succ.right;
                    }
                    
                    return true;
                } finally {
                    succ.unlock();
                    if (succParent != curr) {
                        succParent.unlock();
                    }
                }
                
            } finally {
                curr.unlock();
            }
        } finally {
            parent.unlock();
            if (grandparent != null) {
                grandparent.unlock();
            }
        }
    }
}