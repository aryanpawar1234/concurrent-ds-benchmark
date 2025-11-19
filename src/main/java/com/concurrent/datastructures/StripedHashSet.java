package com.concurrent.datastructures;

import java.util.concurrent.locks.ReentrantLock;

public class StripedHashSet implements ConcurrentSet {

    private final Node[] buckets;
    private final ReentrantLock[] locks;

    private static class Node {
        int key;
        Node next;

        Node(int k, Node n) {
            key = k;
            next = n;
        }
    }

    public StripedHashSet(int stripes) {
        buckets = new Node[stripes];
        locks = new ReentrantLock[stripes];
        for (int i = 0; i < stripes; i++)
            locks[i] = new ReentrantLock();
    }

    private int hash(int key) {
        return Math.abs(key) % buckets.length;
    }

    @Override
    public boolean insert(int key) {
        int h = hash(key);
        locks[h].lock();
        try {
            Node curr = buckets[h];
            while (curr != null) {
                if (curr.key == key) return false;
                curr = curr.next;
            }
            buckets[h] = new Node(key, buckets[h]);
            return true;
        } finally {
            locks[h].unlock();
        }
    }

    @Override
    public boolean remove(int key) {
        int h = hash(key);
        locks[h].lock();
        try {
            Node curr = buckets[h], prev = null;
            while (curr != null) {
                if (curr.key == key) {
                    if (prev == null) buckets[h] = curr.next;
                    else prev.next = curr.next;
                    return true;
                }
                prev = curr;
                curr = curr.next;
            }
            return false;
        } finally {
            locks[h].unlock();
        }
    }

    @Override
    public boolean contains(int key) {
        int h = hash(key);
        locks[h].lock();
        try {
            Node curr = buckets[h];
            while (curr != null) {
                if (curr.key == key) return true;
                curr = curr.next;
            }
            return false;
        } finally {
            locks[h].unlock();
        }
    }
}
