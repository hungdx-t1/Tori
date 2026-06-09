package com.dianxin.tori.base.collections;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class BiUniqueMap<K, V> implements UniqueMap<K, V> {
    private final Map<K, V> keyToValue = new HashMap<>();
    private final Map<V, K> valueToKey = new HashMap<>();

    public BiUniqueMap() {}

    public BiUniqueMap(Map<K, V> map) {
        if (map == null) return;
        for (Map.Entry<K, V> entry : map.entrySet()) {
            putUnique(entry.getKey(), entry.getValue());
        }
    }

    public BiUniqueMap(SimpleKeyValue<K, V> simpleKeyValue) {
        if (simpleKeyValue == null) return;
        putUnique(simpleKeyValue.key(), simpleKeyValue.value());
    }

    @Override
    public synchronized void putUnique(K key, V value) {
        if (keyToValue.containsKey(key))
            throw new IllegalArgumentException("Key đã tồn tại: " + key);

        if (valueToKey.containsKey(value))
            throw new IllegalArgumentException("Value đã tồn tại: " + value);

        keyToValue.put(key, value);
        valueToKey.put(value, key);
    }

    @Override
    public V get(K key) {
        return keyToValue.get(key);
    }

    @Override
    public K getKeyByValue(V value) {
        return valueToKey.get(value);
    }

    @Override
    public boolean containsKey(K key) {
        return keyToValue.containsKey(key);
    }

    @Override
    public boolean containsValue(V value) {
        return valueToKey.containsKey(value);
    }

    @Override
    public void removeByKey(K key) {
        V value = keyToValue.remove(key);
        if (value != null) {
            valueToKey.remove(value);
        }
    }

    @Override
    public void removeByValue(V value) {
        K key = valueToKey.remove(value);
        if (key != null) {
            keyToValue.remove(key);
        }
    }

    @Override
    public int size() {
        return keyToValue.size();
    }

    @Override
    public Map<K, V> getAll() {
        return Map.copyOf(keyToValue);
    }

    public record SimpleKeyValue<K, V>(K key, V value) {}
}