package com.concurrent.datastructures;

public interface ConcurrentSet {
    boolean insert(int key);
    boolean remove(int key);
    boolean contains(int key);
}
