package com.dianxin.tori.base.concurrent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;

import java.time.Duration;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

class RetryImpl implements Retry {
    private final long maxAttempts;
    private final Duration delay;
    private Consumer<RetryContext> retryListener;
    private Predicate<? super Throwable> filter;

    public RetryImpl(long maxAttempts, @NotNull Duration delay) {
        if (maxAttempts < 0) {
            throw new IllegalArgumentException("maxAttempts must be >= 0");
        }
        this.maxAttempts = maxAttempts;
        this.delay = Objects.requireNonNull(delay, "delay cannot be null");
    }

    @Override
    public long getMaxAttempts() {
        return maxAttempts;
    }

    @Override
    public @NotNull Duration getDelay() {
        return delay;
    }

    @Override
    public @Nullable Consumer<RetryContext> getRetryListener() {
        return retryListener;
    }

    @Override
    public @Nullable Predicate<? super Throwable> getFilter() {
        return filter;
    }

    @NotNull
    @Override
    public Retry doBeforeRetry(@NotNull Consumer<RetryContext> action) {
        this.retryListener = Objects.requireNonNull(action, "action cannot be null");
        return this;
    }

    @NotNull
    @Override
    public Retry filter(@NotNull Predicate<? super Throwable> condition) {
        this.filter = Objects.requireNonNull(condition, "filter condition cannot be null");
        return this;
    }

    // Record represents the runtime status of the retry attempt
    record DefaultRetryContext(long iteration, long totalRetries, Throwable exception) implements RetryContext {}
}