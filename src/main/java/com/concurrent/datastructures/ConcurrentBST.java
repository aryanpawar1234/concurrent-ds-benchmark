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

            Node curr = root;
            Node parent = null;

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
            root = removeRec(root, key);
            return true;
        } finally {
            lock.unlock();
        }
    }

    private Node removeRec(Node node, int key) {
        if (node == null) return null;

        if (key < node.key) {
            node.left = removeRec(node.left, key);
        }
        else if (key > node.key) {
            node.right = removeRec(node.right, key);
        }
        else {
            // found node
            if (node.left == null) return node.right;
            else if (node.right == null) return node.left;

            // two children → replace with inorder successor
            Node succ = node.right;
            while (succ.left != null) succ = succ.left;

            node.key = succ.key;
            node.right = removeRec(node.right, succ.key);
        }

        return node;
    }
}
