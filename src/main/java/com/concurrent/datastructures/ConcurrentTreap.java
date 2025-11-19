package com.concurrent.datastructures;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.ReentrantLock;

public class ConcurrentTreap implements ConcurrentSet {

    private static class Node {
        int key;
        int priority;
        Node left, right;

        Node(int key) {
            this.key = key;
            this.priority = ThreadLocalRandom.current().nextInt();
        }
    }

    private Node root;
    private final ReentrantLock lock = new ReentrantLock();

    private Node rotateRight(Node y) {
        Node x = y.left;
        y.left = x.right;
        x.right = y;
        return x;
    }

    private Node rotateLeft(Node x) {
        Node y = x.right;
        x.right = y.left;
        y.left = x;
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

        if (key < node.key) {
            node.left = insertRec(node.left, key);
            if (node.left.priority > node.priority)
                node = rotateRight(node);
        } else {
            node.right = insertRec(node.right, key);
            if (node.right.priority > node.priority)
                node = rotateLeft(node);
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
            if (node.right == null) return node.left;

            if (node.left.priority > node.right.priority) {
                node = rotateRight(node);
                node.right = deleteRec(node.right, key);
            } else {
                node = rotateLeft(node);
                node.left = deleteRec(node.left, key);
            }
        }
        return node;
    }
}
