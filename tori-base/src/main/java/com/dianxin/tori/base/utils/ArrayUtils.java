package com.dianxin.tori.base.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Utility class providing helper methods for standard array conversions and element checks.
 */
@SuppressWarnings("unused")
public final class ArrayUtils {

    private ArrayUtils() {
        throw new AssertionError("Utility class cannot be instantiated.");
    }

    /**
     * Converts an array into a mutable {@link List}.
     *
     * @param <T>   the element type
     * @param array the source array, can be {@code null}
     * @return a mutable {@link ArrayList} containing the array elements, or an empty list if array is {@code null} or empty
     */
    @NotNull
    public static <T> List<T> toList(@Nullable T[] array) {
        if (array == null || array.length == 0) return new ArrayList<>();
        return new ArrayList<>(Arrays.asList(array));
    }

    /**
     * Converts an array into a mutable {@link Set}.
     *
     * @param <T>   the element type
     * @param array the source array, can be {@code null}
     * @return a mutable {@link HashSet} containing unique array elements, or an empty set if array is {@code null} or empty
     */
    @NotNull
    public static <T> Set<T> toSet(@Nullable T[] array) {
        if (array == null || array.length == 0) return new HashSet<>();
        return new HashSet<>(Arrays.asList(array));
    }

    /**
     * Checks if a specific element exists within the provided array.
     *
     * @param <T>     the element type
     * @param array   the array to inspect, can be {@code null}
     * @param element the element to find, can be {@code null}
     * @return {@code true} if the element is found, otherwise {@code false}
     */
    @SuppressWarnings("RedundantLengthCheck")
    public static <T> boolean contains(@Nullable T[] array, @Nullable T element) {
        if (array == null || array.length == 0) return false;
        for (T item : array) {
            if (Objects.equals(item, element)) return true;
        }
        return false;
    }
}