package com.concurrent.datastructures;

import java.util.concurrent.locks.ReentrantLock;

public class RefinableHashSet implements ConcurrentSet {

    private static class Node {
        int key;
        Node next;

        Node(int k, Node n) {
            key = k;
            next = n;
        }
    }

    private volatile Node[] buckets;
    private volatile int bucketCount;
    private final ReentrantLock resizeLock = new ReentrantLock();

    // -------------------------------
    // ✅ Default constructor (1024)
    // -------------------------------
    public RefinableHashSet() {
        this(1024);  // delegate to main constructor
    }

    // -------------------------------
    // Main constructor
    // -------------------------------
    public RefinableHashSet(int initialCapacity) {
        this.bucketCount = initialCapacity;
        this.buckets = new Node[bucketCount];
    }

    private int hash(int key) {
        return Math.abs(key) % bucketCount;
    }

    @Override
    public boolean insert(int key) {
        int index = hash(key);
        resizeLock.lock();
        try {
            Node head = buckets[index];
            for (Node curr = head; curr != null; curr = curr.next) {
                if (curr.key == key) return false;
            }
            buckets[index] = new Node(key, head);
            return true;
        } finally {
            resizeLock.unlock();
        }
    }

    @Override
    public boolean remove(int key) {
        int index = hash(key);
        resizeLock.lock();
        try {
            Node curr = buckets[index];
            Node prev = null;
            while (curr != null) {
                if (curr.key == key) {
                    if (prev == null)
                        buckets[index] = curr.next;
                    else
                        prev.next = curr.next;
                    return true;
                }
                prev = curr;
                curr = curr.next;
            }
            return false;
        } finally {
            resizeLock.unlock();
        }
    }

    @Override
    public boolean contains(int key) {
        int index = hash(key);
        resizeLock.lock();
        try {
            for (Node curr = buckets[index]; curr != null; curr = curr.next) {
                if (curr.key == key) return true;
            }
            return false;
        } finally {
            resizeLock.unlock();
        }
    }
}
