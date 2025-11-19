package com.concurrent.datastructures;

import java.util.concurrent.locks.ReentrantLock;

public class ConcurrentBST implements ConcurrentSet {

    private static class Node {
        int key;
        Node left, right;

        Node(int k) {
            key = k;
        }
    }

    private Node root;
    private final ReentrantLock lock = new ReentrantLock();

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
                curr = (key < curr.key) ? curr.left : curr.right;
            }
            if (key < parent.key) parent.left = new Node(key);
            else parent.right = new Node(key);
            return true;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean contains(int key) {
        lock.lock();
        try {
            Node curr = root;
            while (curr != null) {
                if (key == curr.key) return true;
                curr = (key < curr.key) ? curr.left : curr.right;
            }
            return false;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean remove(int key) {
        lock.lock();
        try {
            root = removeRec(root, key);
            boolean r = removedFlag;
            removedFlag = false;
            return r;
        } finally {
            lock.unlock();
        }
    }

    private boolean removedFlag = false;

    private Node removeRec(Node node, int key) {
        if (node == null) return null;

        if (key < node.key) node.left = removeRec(node.left, key);
        else if (key > node.key) node.right = removeRec(node.right, key);
        else {
            removedFlag = true;

            // 1-child or no child
            if (node.left == null) return node.right;
            if (node.right == null) return node.left;

            // two children -> replace with inorder successor
            Node succParent = node;
            Node succ = node.right;
            while (succ.left != null) {
                succParent = succ;
                succ = succ.left;
            }

            node.key = succ.key;

            if (succParent != node)
                succParent.left = succ.right;
            else
                succParent.right = succ.right;
        }
        return node;
    }
}
