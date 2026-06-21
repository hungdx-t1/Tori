package com.dianxin.tori.base.collections;

import java.util.HashMap;
import java.util.Map;

/**
 * A bidirectional implementation of the {@link UniqueMap} interface, backed by two {@link HashMap}s.
 * This class ensures that both keys and values remain unique, allowing $O(1)$ time complexity
 * for lookups in both directions (key-to-value and value-to-key).
 * * <p><strong>Note:</strong> While the {@link #putUnique(Object, Object)} method is synchronized,
 * other read and write operations are not. If multiple threads access this map concurrently,
 * external synchronization may be required.</p>
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 */
@SuppressWarnings("unused")
public class BiUniqueMap<K, V> implements UniqueMap<K, V> {
    private final Map<K, V> keyToValue = new HashMap<>();
    private final Map<V, K> valueToKey = new HashMap<>();

    /**
     * Constructs an empty {@code BiUniqueMap} with default initial capacity.
     */
    public BiUniqueMap() {}

    /**
     * Constructs a {@code BiUniqueMap} containing the same mappings as the specified map.
     * If the input map contains duplicate keys or duplicate values, an {@link IllegalArgumentException} will be thrown.
     * If the input map is {@code null}, this constructor returns an empty map.
     *
     * @param map the map whose mappings are to be placed in this unique map, may be {@code null}
     * @throws IllegalArgumentException if the specified map contains duplicate values or keys
     */
    public BiUniqueMap(Map<K, V> map) {
        if (map == null) return;
        for (Map.Entry<K, V> entry : map.entrySet()) {
            putUnique(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Constructs a {@code BiUniqueMap} initialized with a single key-value pair
     * provided by the specified {@link SimpleKeyValue}.
     * If the input parameter is {@code null}, this constructor returns an empty map.
     *
     * @param simpleKeyValue the single key-value container used to populate this map, may be {@code null}
     */
    public BiUniqueMap(SimpleKeyValue<K, V> simpleKeyValue) {
        if (simpleKeyValue == null) return;
        putUnique(simpleKeyValue.key(), simpleKeyValue.value());
    }

    @Override
    public synchronized void putUnique(K key, V value) {
        if (keyToValue.containsKey(key))
            throw new IllegalArgumentException("Key has already defined: " + key);

        if (valueToKey.containsKey(value))
            throw new IllegalArgumentException("Value has already defined: " + value);

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

    /**
     * A simple immutable record representing a key-value pair.
     *
     * @param <K> the type of the key
     * @param <V> the type of the value
     */
    public record SimpleKeyValue<K, V>(K key, V value) {}
}