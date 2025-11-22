package com.concurrent.datastructures;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Refinable HashSet with:
 * - Lock striping (multiple locks for different buckets)
 * - Dynamic resizing when load factor exceeded
 * - Read-write lock for resize operations
 */
public class RefinableHashSet implements ConcurrentSet {

    private static class Node {
        int key;
        Node next;

        Node(int k, Node n) {
            key = k;
            next = n;
        }
    }

    private static final float LOAD_FACTOR = 0.75f;
    private static final int MIN_BUCKETS = 16;

    private volatile Node[] buckets;
    private volatile ReentrantLock[] locks;
    private volatile int numLocks;
    private final AtomicInteger size = new AtomicInteger(0);
    private final ReentrantReadWriteLock resizeLock = new ReentrantReadWriteLock();

    public RefinableHashSet() {
        this(1024);
    }

    public RefinableHashSet(int initialCapacity) {
        int capacity = Math.max(MIN_BUCKETS, initialCapacity);
        this.buckets = new Node[capacity];
        this.numLocks = Math.min(capacity, Runtime.getRuntime().availableProcessors() * 4);
        this.locks = new ReentrantLock[numLocks];
        for (int i = 0; i < numLocks; i++) {
            locks[i] = new ReentrantLock();
        }
    }

    private int hash(int key, int capacity) {
        return Math.abs(key % capacity);
    }

    private ReentrantLock getLock(int bucketIndex) {
        return locks[bucketIndex % numLocks];
    }

    @Override
    public boolean contains(int key) {
        resizeLock.readLock().lock();
        try {
            Node[] table = buckets;
            int index = hash(key, table.length);
            ReentrantLock lock = getLock(index);
            
            lock.lock();
            try {
                for (Node curr = table[index]; curr != null; curr = curr.next) {
                    if (curr.key == key) return true;
                }
                return false;
            } finally {
                lock.unlock();
            }
        } finally {
            resizeLock.readLock().unlock();
        }
    }

    @Override
    public boolean insert(int key) {
        resizeLock.readLock().lock();
        try {
            Node[] table = buckets;
            int index = hash(key, table.length);
            ReentrantLock lock = getLock(index);
            
            lock.lock();
            try {
                // Check if already exists
                for (Node curr = table[index]; curr != null; curr = curr.next) {
                    if (curr.key == key) return false;
                }
                // Insert at head
                table[index] = new Node(key, table[index]);
                int currentSize = size.incrementAndGet();
                
                // Check if resize needed (do it outside lock)
                if (currentSize > table.length * LOAD_FACTOR) {
                    lock.unlock();
                    resizeLock.readLock().unlock();
                    resize();
                    return true;
                }
                return true;
            } finally {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        } finally {
            if (resizeLock.getReadHoldCount() > 0) {
                resizeLock.readLock().unlock();
            }
        }
    }

    @Override
    public boolean remove(int key) {
        resizeLock.readLock().lock();
        try {
            Node[] table = buckets;
            int index = hash(key, table.length);
            ReentrantLock lock = getLock(index);
            
            lock.lock();
            try {
                Node curr = table[index];
                Node prev = null;
                
                while (curr != null) {
                    if (curr.key == key) {
                        if (prev == null) {
                            table[index] = curr.next;
                        } else {
                            prev.next = curr.next;
                        }
                        size.decrementAndGet();
                        return true;
                    }
                    prev = curr;
                    curr = curr.next;
                }
                return false;
            } finally {
                lock.unlock();
            }
        } finally {
            resizeLock.readLock().unlock();
        }
    }

    private void resize() {
        resizeLock.writeLock().lock();
        try {
            Node[] oldBuckets = buckets;
            int oldCapacity = oldBuckets.length;
            
            // Double check resize is still needed
            if (size.get() <= oldCapacity * LOAD_FACTOR) {
                return;
            }
            
            int newCapacity = oldCapacity * 2;
            Node[] newBuckets = new Node[newCapacity];
            
            // Rehash all elements
            for (int i = 0; i < oldCapacity; i++) {
                Node curr = oldBuckets[i];
                while (curr != null) {
                    Node next = curr.next;
                    int newIndex = hash(curr.key, newCapacity);
                    curr.next = newBuckets[newIndex];
                    newBuckets[newIndex] = curr;
                    curr = next;
                }
            }
            
            // Update number of locks if needed (refine)
            int newNumLocks = Math.min(newCapacity, numLocks * 2);
            if (newNumLocks > numLocks) {
                ReentrantLock[] newLocks = new ReentrantLock[newNumLocks];
                for (int i = 0; i < newNumLocks; i++) {
                    newLocks[i] = new ReentrantLock();
                }
                locks = newLocks;
                numLocks = newNumLocks;
            }
            
            buckets = newBuckets;
            
        } finally {
            resizeLock.writeLock().unlock();
        }
    }
}