package com.dianxin.tori.base.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@SuppressWarnings("unused")
public final class ArrayUtils {
    private ArrayUtils() {}

    @NotNull
    public static <T> List<T> toList(@Nullable T[] array) {
        if (array == null || array.length == 0) return new ArrayList<>();
        return new ArrayList<>(Arrays.asList(array));
    }

    @NotNull
    public static <T> Set<T> toSet(@Nullable T[] array) {
        if (array == null || array.length == 0) return new HashSet<>();
        return new HashSet<>(Arrays.asList(array));
    }

    public static <T> boolean contains(@Nullable T[] array, @Nullable T element) {
        if (array == null || array.length == 0) return false;
        for (T item : array) {
            if (Objects.equals(item, element)) return true;
        }
        return false;
    }
}