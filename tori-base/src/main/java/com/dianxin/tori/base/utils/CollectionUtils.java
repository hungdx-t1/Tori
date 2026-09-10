package com.dianxin.tori.base.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;

@SuppressWarnings("unused")
public final class CollectionUtils {
    private CollectionUtils() {}

    @NotNull
    public static <T> Set<T> toSet(@Nullable Collection<T> collection) {
        if (collection == null || collection.isEmpty()) {
            return new HashSet<>();
        }
        return new HashSet<>(collection);
    }

    @NotNull
    @Unmodifiable
    public static <T> Set<T> toUnmodifiableSet(@Nullable Collection<T> collection) {
        if (collection == null || collection.isEmpty()) {
            return Set.of();
        }
        return Set.copyOf(collection);
    }

    @NotNull
    public static <T> List<T> toList(@Nullable Collection<T> collection) {
        if (collection == null || collection.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(collection);
    }

    @NotNull
    @Unmodifiable
    public static <T> List<T> toUnmodifiableList(@Nullable Collection<T> collection) {
        if (collection == null || collection.isEmpty()) {
            return List.of();
        }
        return List.copyOf(collection);
    }
}