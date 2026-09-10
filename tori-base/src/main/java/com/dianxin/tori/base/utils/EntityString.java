package com.dianxin.tori.base.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.StringJoiner;

/**
 * Fluent builder utility for constructing clean, formatted {@code toString()} representations.
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * @Override
 * public String toString() {
 *     return new EntityString(this)
 *         .add("id", 123)
 *         .add("name", "Tori")
 *         .toString();
 * }
 * }</pre>
 * <p>Output: {@code ClassName{id=123, name=Tori}}</p>
 */
@SuppressWarnings("unused")
public class EntityString {
    private final String entityName;
    private final StringJoiner joiner;

    /**
     * Initializes an entity string using the runtime class of the provided instance.
     *
     * @param entity the object instance
     */
    public EntityString(@NotNull Object entity) {
        this(resolveName(entity.getClass()));
    }

    /**
     * Initializes an entity string using the specified class metadata.
     *
     * @param clazz the target class
     */
    public EntityString(@NotNull Class<?> clazz) {
        this(resolveName(clazz));
    }

    /**
     * Initializes an entity string using an explicit header identifier.
     *
     * @param name the explicit entity name prefix
     */
    public EntityString(@NotNull String name) {
        this.entityName = name;
        this.joiner = new StringJoiner(", ", "{", "}");
    }

    /**
     * Appends a key-value pair to the output string.
     *
     * @param key   the attribute name
     * @param value the attribute value (serialized as {@code "null"} if null)
     * @return this builder instance
     */
    public EntityString add(@NotNull String key, @Nullable Object value) {
        joiner.add(key + "=" + value);
        return this;
    }

    /**
     * Appends a key-value pair with explicit quotes around the string value (e.g., {@code name="Tori"}).
     *
     * @param key   the attribute name
     * @param value the string value
     * @return this builder instance
     */
    public EntityString addString(@NotNull String key, @Nullable String value) {
        if (value == null) {
            joiner.add(key + "=null");
        } else {
            joiner.add(key + "=\"" + value + "\"");
        }
        return this;
    }

    /**
     * Conditionally appends a key-value pair only when the value is not null.
     *
     * @param key   the attribute name
     * @param value the attribute value
     * @return this builder instance
     */
    public EntityString addIfNotNull(@NotNull String key, @Nullable Object value) {
        if (value != null) {
            add(key, value);
        }
        return this;
    }

    @Override
    public String toString() {
        return entityName + joiner.toString();
    }

    private static String resolveName(Class<?> clazz) {
        String name = clazz.getSimpleName();
        if (name.isEmpty()) {
            name = clazz.getSuperclass().getSimpleName();
        }
        return name.replace("Impl", "").replace("$", ".");
    }
}