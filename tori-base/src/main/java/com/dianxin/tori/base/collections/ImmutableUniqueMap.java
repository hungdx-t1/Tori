package com.dianxin.tori.base.collections;

import com.dianxin.tori.base.annotations.ReleasedSince;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@ReleasedSince("26.10.0")
final class ImmutableUniqueMap<K, V> implements UniqueMap<K, V> {

    @SuppressWarnings("rawtypes")
    private static final ImmutableUniqueMap EMPTY = new ImmutableUniqueMap<>(Map.of(), Map.of());

    private final Map<K, V> keyToValue;
    private final Map<V, K> valueToKey;

    private ImmutableUniqueMap(Map<K, V> keyToValue, Map<V, K> valueToKey) {
        this.keyToValue = keyToValue;
        this.valueToKey = valueToKey;
    }

    @SuppressWarnings("unchecked")
    static <K, V> ImmutableUniqueMap<K, V> empty() {
        return (ImmutableUniqueMap<K, V>) EMPTY;
    }

    @SafeVarargs
    static <K, V> ImmutableUniqueMap<K, V> from(Entry<K, V>... entries) {
        Objects.requireNonNull(entries, "Entries array cannot be null");

        Map<K, V> forward = new HashMap<>(entries.length);
        Map<V, K> backward = new HashMap<>(entries.length);

        for (Entry<K, V> entry : entries) {
            Objects.requireNonNull(entry, "Entry cannot be null");
            K key = Objects.requireNonNull(entry.key(), "Key cannot be null");
            V value = Objects.requireNonNull(entry.value(), "Value cannot be null");

            if (forward.put(key, value) != null) {
                throw new IllegalArgumentException("Duplicate key detected: " + key);
            }
            if (backward.put(value, key) != null) {
                throw new IllegalArgumentException("Duplicate value detected: " + value);
            }
        }

        return new ImmutableUniqueMap<>(Map.copyOf(forward), Map.copyOf(backward));
    }

    @Override
    public void putUnique(K key, V value) {
        throw new UnsupportedOperationException("ImmutableUniqueMap cannot be modified");
    }

    @NonNull
    @Override
    public V get(K key) {
        return keyToValue.get(key);
    }

    @NonNull
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
        throw new UnsupportedOperationException("ImmutableUniqueMap cannot be modified");
    }

    @Override
    public void removeByValue(V value) {
        throw new UnsupportedOperationException("ImmutableUniqueMap cannot be modified");
    }

    @Override
    public int size() {
        return keyToValue.size();
    }

    @Override
    public @Unmodifiable @NonNull Map<K, V> getAll() {
        return keyToValue;
    }
}