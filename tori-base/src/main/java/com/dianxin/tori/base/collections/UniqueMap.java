package com.dianxin.tori.base.collections;

import java.util.Map;

@SuppressWarnings("unused")
public interface UniqueMap<K, V> {
    void putUnique(K key, V value);
    V get(K key);
    K getKeyByValue(V value);
    boolean containsKey(K key);
    boolean containsValue(V value);
    void removeByKey(K key);
    void removeByValue(V value);
    int size();
    Map<K, V> getAll();
}
