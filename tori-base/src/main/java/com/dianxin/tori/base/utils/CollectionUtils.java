package com.dianxin.tori.base.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;

/**
 * Utility class providing conversion helpers for collections with mutable and unmodifiable variants.
 */
@SuppressWarnings("unused")
public final class CollectionUtils {

    private CollectionUtils() {
        throw new AssertionError("Utility class cannot be instantiated.");
    }

    /**
     * Converts a collection into a mutable {@link Set}.
     *
     * @param <T>        the element type
     * @param collection the source collection, can be {@code null}
     * @return a mutable {@link HashSet} containing unique elements, or an empty set if the input is {@code null} or empty
     */
    @NotNull
    public static <T> Set<T> toSet(@Nullable Collection<T> collection) {
        if (collection == null || collection.isEmpty()) {
            return new HashSet<>();
        }
        return new HashSet<>(collection);
    }

    /**
     * Converts a collection into an unmodifiable {@link Set}.
     *
     * @param <T>        the element type
     * @param collection the source collection, can be {@code null}
     * @return an unmodifiable {@link Set}, or {@link Set#of()} if the input is {@code null} or empty
     */
    @NotNull
    @Unmodifiable
    public static <T> Set<T> toUnmodifiableSet(@Nullable Collection<T> collection) {
        if (collection == null || collection.isEmpty()) {
            return Set.of();
        }
        return Set.copyOf(collection);
    }

    /**
     * Converts a collection into a mutable {@link List}.
     *
     * @param <T>        the element type
     * @param collection the source collection, can be {@code null}
     * @return a mutable {@link ArrayList}, or an empty list if the input is {@code null} or empty
     */
    @NotNull
    public static <T> List<T> toList(@Nullable Collection<T> collection) {
        if (collection == null || collection.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(collection);
    }

    /**
     * Converts a collection into an unmodifiable {@link List}.
     *
     * @param <T>        the element type
     * @param collection the source collection, can be {@code null}
     * @return an unmodifiable {@link List}, or {@link List#of()} if the input is {@code null} or empty
     */
    @NotNull
    @Unmodifiable
    public static <T> List<T> toUnmodifiableList(@Nullable Collection<T> collection) {
        if (collection == null || collection.isEmpty()) {
            return List.of();
        }
        return List.copyOf(collection);
    }
}