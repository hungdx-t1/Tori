package com.dianxin.tori.base.collections;

import com.dianxin.tori.base.annotations.ReleasedSince;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;

import java.util.Map;
import java.util.Objects;

/**
 * A map that enforces a one-to-one (bijective) mapping between keys and values.
 * Unlike a standard {@link Map}, this structure guarantees that both keys and values are unique.
 * It allows bidirectional lookups: finding a value by its key, or finding a key by its value.
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 */
@NullMarked
@SuppressWarnings("unused")
public interface UniqueMap<K, V> {

    /**
     * Associates the specified value with the specified key in this map.
     * Both the key and the value must be unique within the map.
     *
     * @param key   the key with which the specified value is to be associated
     * @param value the value to be associated with the specified key
     * @throws IllegalArgumentException if the key or the value is already present in the map
     */
    void putUnique(K key, V value);

    /**
     * Returns the value to which the specified key is mapped,
     * or {@code null} if this map contains no mapping for the key.
     *
     * @param key the key whose associated value is to be returned
     * @return the value to which the specified key is mapped, or {@code null}
     */
    V get(K key);

    /**
     * Returns the key associated with the specified value,
     * or {@code null} if this map contains no mapping for the value.
     *
     * @param value the value whose associated key is to be returned
     * @return the key associated with the specified value, or {@code null}
     */
    K getKeyByValue(V value);

    /**
     * Returns {@code true} if this map contains a mapping for the specified key.
     *
     * @param key the key whose presence in this map is to be tested
     * @return {@code true} if this map contains a mapping for the specified key
     */
    boolean containsKey(K key);

    /**
     * Returns {@code true} if this map maps one or more keys to the specified value.
     *
     * @param value the value whose presence in this map is to be tested
     * @return {@code true} if this map maps one or more keys to the specified value
     */
    boolean containsValue(V value);

    /**
     * Removes the mapping for a key from this map if it is present.
     * Also removes the corresponding value from the bidirectional mapping.
     *
     * @param key the key whose mapping is to be removed from the map
     */
    void removeByKey(K key);

    /**
     * Removes the mapping for a value from this map if it is present.
     * Also removes the corresponding key from the bidirectional mapping.
     *
     * @param value the value whose mapping is to be removed from the map
     */
    void removeByValue(V value);

    /**
     * Returns the number of key-value mappings in this map.
     *
     * @return the number of key-value mappings in this map
     */
    int size();

    /**
     * Returns an unmodifiable {@link Map} view of the mappings contained in this map.
     *
     * @return an unmodifiable map containing all key-value pairs
     */
    @Unmodifiable Map<K, V> getAll();

    /**
     * Returns an unmodifiable {@link UniqueMap} containing the given key-value mappings.
     *
     * @param <R1>    the key type
     * @param <R2>    the value type
     * @param entries the key-value pairs to populate the map with
     * @return an immutable {@link UniqueMap} containing the specified entries
     * @throws NullPointerException     if the array or any entry/key/value is null
     * @throws IllegalArgumentException if duplicate keys or duplicate values are provided
     */
    @SafeVarargs
    @ReleasedSince("26.10.0")
    @SuppressWarnings({"ConstantValue"})
    static <R1, R2> UniqueMap<R1, R2> of(Entry<R1, R2>... entries) {
        if (entries == null || entries.length == 0) {
            return ImmutableUniqueMap.empty();
        }
        return ImmutableUniqueMap.from(entries);
    }

    /**
     * A simple immutable record representing a key-value pair.
     *
     * @param <K> the type of the key
     * @param <V> the type of the value
     */
    record Entry<K, V>(K key, V value) {
        public Entry {
            Objects.requireNonNull(key);
            Objects.requireNonNull(value);
        }

        public static <K, V> Entry<K, V> of(K key, V value) {
            return new Entry<>(key, value);
        }
    }
}