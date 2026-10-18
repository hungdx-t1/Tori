package com.dianxin.tori.base.utils;

import com.dianxin.tori.base.annotations.ReleasedSince;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.IntFunction;

/**
 * Utility class providing helper methods for standard array conversions and element checks.
 */
@ReleasedSince("26.8.301")
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
     * Converts a {@link Collection} into an array using the provided generator function.
     *
     * @param <T>        the element type of the collection and the resulting array
     * @param collection the source collection to be converted, can be {@code null}
     * @param generator  the array constructor reference used to allocate the array of the correct type
     *                   (e.g., {@code String[]::new})
     * @return an array containing all the elements in the collection,
     *         or an empty array of type {@code T} if the collection is {@code null} or empty
     * @throws IllegalArgumentException if the {@code generator} is {@code null}
     *         and the collection is not null or empty
     */
    @NotNull
    public static <T> T[] toArray(@Nullable Collection<T> collection, @NotNull IntFunction<T[]> generator) {
        if (collection == null || collection.isEmpty()) return generator.apply(0);
        return collection.toArray(generator);
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

    private static void testToArrayMethod() {
        Collection<String> names = List.of("Anna", "Celsius", "Syren");

        // String[]::new as same num -> new String[num]
        String[] arr = ArrayUtils.toArray(names, String[]::new);
    }
}