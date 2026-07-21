package com.dianxin.tori.base.concurrent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * The core implementation of the {@link FutureAction} interface.
 * Handles checks, deadlines, mapping, and completion stage bindings natively.
 *
 * @param <T> The return type.
 */
@SuppressWarnings({"unused", "BooleanMethodIsAlwaysInverted"})
class FutureActionImpl<T> implements FutureAction<T> {
    private final CompletableFuture<T> future;

    private static final Logger logger = LoggerFactory.getLogger(FutureActionImpl.class);
    private static Consumer<Object> DEFAULT_SUCCESS = o -> {
    };
    private static Consumer<? super Throwable> DEFAULT_FAILURE =
            t -> logger.error("FutureAction execution failed: [{}] {}", t.getClass().getSimpleName(), t.getMessage());

    protected static long defaultTimeout = 0;

    private long deadline = 0;
    private BooleanSupplier checks;

    public FutureActionImpl(CompletableFuture<T> future) {
        this.future = future;
    }

    public static void setDefaultFailure(Consumer<? super Throwable> callback) {
        DEFAULT_FAILURE = callback == null ? t -> {
        } : callback;
    }

    public static Consumer<? super Throwable> getDefaultFailure() {
        return DEFAULT_FAILURE;
    }

    public static void setDefaultSuccess(Consumer<Object> callback) {
        DEFAULT_SUCCESS = callback == null ? t -> {
        } : callback;
    }

    public static Consumer<Object> getDefaultSuccess() {
        return DEFAULT_SUCCESS;
    }

    public static void setDefaultTimeout(long timeout, @NotNull TimeUnit unit) {
        defaultTimeout = unit.toMillis(timeout);
    }

    public static long getDefaultTimeout() {
        return defaultTimeout;
    }

    @Override
    @NotNull
    public FutureAction<T> setChecks(@Nullable BooleanSupplier checks) {
        this.checks = checks;
        return this;
    }

    @Override
    @Nullable
    public BooleanSupplier getChecks() {
        return this.checks;
    }

    @Override
    @NotNull
    public FutureAction<T> deadline(long timestamp) {
        this.deadline = timestamp;
        return this;
    }

    /**
     * Internal validator to check if execution is permitted based on deadlines and checks.
     *
     * @return true if execution should proceed, false to silently cancel.
     */
    private boolean isExecutionValid() {
        if (deadline > 0 && System.currentTimeMillis() > deadline) {
            return false;
        }
        return checks == null || checks.getAsBoolean();
    }

    @Override
    public void queue(@Nullable Consumer<? super T> success, @Nullable Consumer<? super Throwable> failure) {
        if (!isExecutionValid()) return;

        Consumer<? super T> finalSuccess = (success == null) ? DEFAULT_SUCCESS : success;
        Consumer<? super Throwable> finalFailure = (failure == null) ? DEFAULT_FAILURE : failure;

        future.whenComplete((result, error) -> {
            if (error != null) {
                finalFailure.accept(error instanceof CompletionException ? error.getCause() : error);
            } else {
                finalSuccess.accept(result);
            }
        });
    }

    @Override
    @NotNull
    public CompletableFuture<T> submit(boolean shouldQueue) {
        if (!isExecutionValid()) {
            CompletableFuture<T> cancelled = new CompletableFuture<>();
            cancelled.cancel(false);
            return cancelled;
        }
        return future.thenApply(Function.identity());
    }

    @Override
    public T complete(boolean shouldQueue) {
        try {
            return submit(shouldQueue).join();
        } catch (CompletionException e) {
            throw (RuntimeException) e.getCause();
        }
    }

    @Override
    public T complete(long timeout, @NotNull TimeUnit unit) throws TimeoutException {
        try {
            return submit(true).get(timeout, unit);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @NotNull
    public FutureAction<T> recover(@NotNull Function<Throwable, T> fallback) {
        return new FutureActionImpl<>(future.exceptionally(ex -> {
            Throwable cause = (ex instanceof CompletionException) ? ex.getCause() : ex;
            return fallback.apply(cause);
        }));
    }

    @Override
    @NotNull
    public <U> FutureAction<U> map(@NotNull Function<? super T, ? extends U> mapper) {
        return new FutureActionImpl<>(future.thenApply(mapper));
    }

    @Override
    @NotNull
    public <U> FutureAction<U> flatMap(@NotNull Function<? super T, ? extends FutureAction<U>> mapper) {
        return new FutureActionImpl<>(future.thenCompose(result -> mapper.apply(result).submit()));
    }

    @Override
    public FutureAction<T> onErrorMap(@Nullable Predicate<? super Throwable> condition, @NotNull Function<? super Throwable, ? extends T> map) {
        return new FutureActionImpl<>(future.exceptionally(ex -> {
            Throwable cause = (ex instanceof CompletionException) ? ex.getCause() : ex;
            if (condition == null || condition.test(cause)) {
                return map.apply(cause);
            }
            throw (cause instanceof RuntimeException) ? (RuntimeException) cause : new CompletionException(cause);
        }));
    }

    @Override
    public FutureAction<T> onErrorFlatMap(@Nullable Predicate<? super Throwable> condition, @NotNull Function<? super Throwable, ? extends FutureAction<? extends T>> map) {
        CompletableFuture<T> cf = new CompletableFuture<>();
        future.whenComplete((res, ex) -> {
            if (ex != null) {
                Throwable cause = (ex instanceof CompletionException) ? ex.getCause() : ex;
                if (condition == null || condition.test(cause)) {
                    map.apply(cause).submit().whenComplete((fbRes, fbEx) -> {
                        if (fbEx != null) cf.completeExceptionally(fbEx);
                        else cf.complete(fbRes);
                    });
                    return;
                }
                cf.completeExceptionally(cause);
            } else {
                cf.complete(res);
            }
        });
        return new FutureActionImpl<>(cf);
    }

    @Override
    @NotNull
    public FutureAction<T> onErrorReturn(T fallback) {
        return recover(ex -> fallback);
    }

    @Override
    @NotNull
    public FutureAction<T> onSuccess(@NotNull Consumer<? super T> con) {
        future.thenAccept(con);
        return this;
    }

    @Override
    @NotNull
    public FutureAction<T> onFailure(@NotNull Consumer<? super Throwable> failure) {
        future.exceptionally(ex -> {
            failure.accept(ex instanceof CompletionException ? ex.getCause() : ex);
            return null;
        });
        return this;
    }

    @Override
    @NotNull
    public FutureAction<T> onExecutor(@NotNull Executor executor) {
        return new FutureActionImpl<>(future.thenApplyAsync(Function.identity(), executor));
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return future.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isDone() {
        return future.isDone();
    }

    @Override
    public boolean isCancelled() {
        return future.isCancelled();
    }
}