package com.concurrent.datastructures;

import java.util.concurrent.locks.ReentrantLock;

public class ConcurrentAVL implements ConcurrentSet {

    private static class Node {
        int key, height;
        Node left, right;

        Node(int k) {
            key = k;
            height = 1;
        }
    }

    private Node root;
    private final ReentrantLock lock = new ReentrantLock();

    // ---- Utility helpers ----
    private int height(Node n) { return n == null ? 0 : n.height; }

    private int getBalance(Node n) { return n == null ? 0 : height(n.left) - height(n.right); }

    private Node rotateRight(Node y) {
        Node x = y.left;
        Node T = x.right;

        x.right = y;
        y.left = T;

        y.height = Math.max(height(y.left), height(y.right)) + 1;
        x.height = Math.max(height(x.left), height(x.right)) + 1;
        return x;
    }

    private Node rotateLeft(Node x) {
        Node y = x.right;
        Node T = y.left;

        y.left = x;
        x.right = T;

        x.height = Math.max(height(x.left), height(x.right)) + 1;
        y.height = Math.max(height(y.left), height(y.right)) + 1;
        return y;
    }

    @Override
    public boolean insert(int key) {
        lock.lock();
        try {
            root = insertRec(root, key);
            return insertedFlag;
        } finally {
            insertedFlag = false;
            lock.unlock();
        }
    }

    private boolean insertedFlag = false;

    private Node insertRec(Node node, int key) {
        if (node == null) {
            insertedFlag = true;
            return new Node(key);
        }

        if (key == node.key) return node;

        if (key < node.key)
            node.left = insertRec(node.left, key);
        else
            node.right = insertRec(node.right, key);

        node.height = Math.max(height(node.left), height(node.right)) + 1;

        int bal = getBalance(node);

        // LL
        if (bal > 1 && key < node.left.key) return rotateRight(node);
        // RR
        if (bal < -1 && key > node.right.key) return rotateLeft(node);
        // LR
        if (bal > 1 && key > node.left.key) {
            node.left = rotateLeft(node.left);
            return rotateRight(node);
        }
        // RL
        if (bal < -1 && key < node.right.key) {
            node.right = rotateRight(node.right);
            return rotateLeft(node);
        }

        return node;
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
            root = deleteRec(root, key);
            boolean r = deletedFlag;
            deletedFlag = false;
            return r;
        } finally {
            lock.unlock();
        }
    }

    private boolean deletedFlag = false;

    private Node deleteRec(Node node, int key) {
        if (node == null) return null;

        if (key < node.key) node.left = deleteRec(node.left, key);
        else if (key > node.key) node.right = deleteRec(node.right, key);
        else {
            deletedFlag = true;
            if (node.left == null) return node.right;
            else if (node.right == null) return node.left;

            Node succ = node.right;
            while (succ.left != null) succ = succ.left;
            node.key = succ.key;
            node.right = deleteRec(node.right, succ.key);
        }

        // update height
        node.height = Math.max(height(node.left), height(node.right)) + 1;

        int bal = getBalance(node);

        // rebalance exactly like insert
        if (bal > 1 && getBalance(node.left) >= 0) return rotateRight(node);
        if (bal > 1 && getBalance(node.left) < 0) {
            node.left = rotateLeft(node.left);
            return rotateRight(node);
        }
        if (bal < -1 && getBalance(node.right) <= 0) return rotateLeft(node);
        if (bal < -1 && getBalance(node.right) > 0) {
            node.right = rotateRight(node.right);
            return rotateLeft(node);
        }
        return node;
    }
}
