package com.dianxin.tori.base.collections;

import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NonNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A thread-safe, bidirectional implementation of the {@link UniqueMap} interface.
 * <p>
 * This class guarantees bijective uniqueness across concurrent threads using
 * a {@link ReadWriteLock}, allowing concurrent non-blocking reads while ensuring
 * strict atomicity for mutation operations.
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 */
@SuppressWarnings("unused")
public class ConcurrentUniqueMap<K, V> implements UniqueMap<K, V> {

    private final Map<K, V> keyToValue = new HashMap<>();
    private final Map<V, K> valueToKey = new HashMap<>();

    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Lock readLock = rwLock.readLock();
    private final Lock writeLock = rwLock.writeLock();

    public ConcurrentUniqueMap() {}

    public ConcurrentUniqueMap(Map<? extends K, ? extends V> map) {
        if (map != null) {
            for (Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
                putUnique(entry.getKey(), entry.getValue());
            }
        }
    }

    @Override
    public void putUnique(@NonNull K key, @NonNull V value) {
        Objects.requireNonNull(key, "Key must not be null");
        Objects.requireNonNull(value, "Value must not be null");

        writeLock.lock();
        try {
            if (keyToValue.containsKey(key)) {
                throw new IllegalArgumentException("Key is already defined: " + key);
            }
            if (valueToKey.containsKey(value)) {
                throw new IllegalArgumentException("Value is already defined: " + value);
            }

            keyToValue.put(key, value);
            valueToKey.put(value, key);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public V get(K key) {
        readLock.lock();
        try {
            return keyToValue.get(key);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public K getKeyByValue(V value) {
        readLock.lock();
        try {
            return valueToKey.get(value);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public boolean containsKey(K key) {
        readLock.lock();
        try {
            return keyToValue.containsKey(key);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public boolean containsValue(V value) {
        readLock.lock();
        try {
            return valueToKey.containsKey(value);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void removeByKey(K key) {
        writeLock.lock();
        try {
            V value = keyToValue.remove(key);
            if (value != null) {
                valueToKey.remove(value);
            }
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void removeByValue(V value) {
        writeLock.lock();
        try {
            K key = valueToKey.remove(value);
            if (key != null) {
                keyToValue.remove(key);
            }
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public int size() {
        readLock.lock();
        try {
            return keyToValue.size();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public @Unmodifiable Map<K, V> getAll() {
        readLock.lock();
        try {
            // Snapshot copy on read lock for thread-safe
            return Collections.unmodifiableMap(new HashMap<>(keyToValue));
        } finally {
            readLock.unlock();
        }
    }
}