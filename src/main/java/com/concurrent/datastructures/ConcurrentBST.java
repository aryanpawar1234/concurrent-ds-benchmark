package com.concurrent.datastructures;

import java.util.concurrent.locks.ReentrantLock;

public class ConcurrentBST implements ConcurrentSet {

    private static class Node {
        int key;
        Node left, right;
        Node(int k) { key = k; }
    }

    private Node root = null;
    private final ReentrantLock lock = new ReentrantLock();

    @Override
    public boolean contains(int key) {
        lock.lock();
        try {
            Node curr = root;
            while (curr != null) {
                if (key == curr.key) return true;
                else if (key < curr.key) curr = curr.left;
                else curr = curr.right;
            }
            return false;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean insert(int key) {
        lock.lock();
        try {
            if (root == null) {
                root = new Node(key);
                return true;
            }

            Node curr = root, parent = null;
            while (curr != null) {
                parent = curr;
                if (key == curr.key) return false;
                else if (key < curr.key) curr = curr.left;
                else curr = curr.right;
            }

            if (key < parent.key) parent.left = new Node(key);
            else parent.right = new Node(key);
            return true;

        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean remove(int key) {
        lock.lock();
        try {
            Node curr = root;
            Node parent = null;

            // search for key
            while (curr != null && curr.key != key) {
                parent = curr;
                if (key < curr.key) curr = curr.left;
                else curr = curr.right;
            }

            if (curr == null) return false;

            // case 1: one child or zero
            if (curr.left == null || curr.right == null) {
                Node child = (curr.left != null) ? curr.left : curr.right;

                if (parent == null) root = child;
                else if (parent.left == curr) parent.left = child;
                else parent.right = child;
            }
            else {
                // case 2: two children → find inorder successor
                Node succParent = curr;
                Node succ = curr.right;

                while (succ.left != null) {
                    succParent = succ;
                    succ = succ.left;
                }

                curr.key = succ.key; // copy successor key

                if (succParent.left == succ)
                    succParent.left = succ.right;
                else
                    succParent.right = succ.right;
            }

            return true;

        } finally {
            lock.unlock();
        }
    }
}
