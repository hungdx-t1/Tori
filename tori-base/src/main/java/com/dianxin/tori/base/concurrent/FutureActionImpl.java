package com.dianxin.tori.base.concurrent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.function.*;

/**
 * The core implementation of the {@link FutureAction} interface.
 * Handles checks, deadlines, mapping, and completion stage bindings natively.
 *
 * @param <T> The return type.
 */
@SuppressWarnings({"unused", "BooleanMethodIsAlwaysInverted"})
class FutureActionImpl<T> implements FutureAction<T> {
    private static final Logger logger = LoggerFactory.getLogger(FutureActionImpl.class);

    private final Supplier<CompletableFuture<T>> taskSupplier;
    private final Executor executor;

    private static Consumer<Object> DEFAULT_SUCCESS = o -> { };
    private static Consumer<? super Throwable> DEFAULT_FAILURE =
            t -> logger.error("FutureAction execution failed: [{}] {}", t.getClass().getSimpleName(), t.getMessage());

    protected static long defaultTimeout = 0;

    private long deadline = 0;
    private BooleanSupplier checks;

    public FutureActionImpl(Supplier<CompletableFuture<T>> taskSupplier, Executor executor) {
        this.taskSupplier = taskSupplier;
        this.executor = executor;
    }

    public FutureActionImpl(CompletableFuture<T> fixedFuture) {
        this(() -> fixedFuture, null);
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

    @NotNull
    @Override
    public FutureAction<T> retryWhen(@NotNull Retry retry) {
        Supplier<CompletableFuture<T>> retryableSupplier = () -> {
            CompletableFuture<T> promise = new CompletableFuture<>();
            executeWithRetryPolicy(this.taskSupplier, retry, 1, promise);
            return promise;
        };

        return new FutureActionImpl<>(retryableSupplier, this.executor);
    }

    private void executeWithRetryPolicy(
            Supplier<CompletableFuture<T>> supplier,
            Retry retry,
            long currentAttempt,
            CompletableFuture<T> targetFuture
    ) {
        supplier.get().whenComplete((result, ex) -> {
            if (ex == null) {
                targetFuture.complete(result);
                return;
            }

            Throwable cause = (ex instanceof CompletionException) ? ex.getCause() : ex;
            boolean canRetry = currentAttempt <= retry.getMaxAttempts()
                    && (retry.getFilter() == null || retry.getFilter().test(cause));

            if (!canRetry) {
                targetFuture.completeExceptionally(cause);
                return;
            }

            // call callback before retry
            Consumer<Retry.RetryContext> listener = retry.getRetryListener();
            if (listener != null) {
                try {
                    listener.accept(new RetryImpl.DefaultRetryContext(currentAttempt, retry.getMaxAttempts(), cause));
                } catch (Exception logEx) {
                    logger.warn("Error inside retry listener: {}", logEx.getMessage());
                }
            }

            // delay execute if delay's profile has been set
            long delayMillis = retry.getDelay().toMillis();
            if (delayMillis > 0) {
                CompletableFuture.delayedExecutor(delayMillis, TimeUnit.MILLISECONDS)
                        .execute(() -> executeWithRetryPolicy(supplier, retry, currentAttempt + 1, targetFuture));
            } else {
                executeWithRetryPolicy(supplier, retry, currentAttempt + 1, targetFuture);
            }
        });
    }

    @Override
    public void queue(@Nullable Consumer<? super T> success, @Nullable Consumer<? super Throwable> failure) {
        if (!isExecutionValid()) return;

        Consumer<? super T> finalSuccess = (success == null) ? DEFAULT_SUCCESS : success;
        Consumer<? super Throwable> finalFailure = (failure == null) ? DEFAULT_FAILURE : failure;

        submit(true).whenComplete((result, error) -> {
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
        return taskSupplier.get();
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
        return new FutureActionImpl<>(() -> submit().exceptionally(ex -> {
            Throwable cause = (ex instanceof CompletionException) ? ex.getCause() : ex;
            return fallback.apply(cause);
        }), this.executor);
    }

    @Override
    @NotNull
    public <U> FutureAction<U> map(@NotNull Function<? super T, ? extends U> mapper) {
        return new FutureActionImpl<>(() -> submit().thenApply(mapper), this.executor);
    }

    @Override
    @NotNull
    public <U> FutureAction<U> flatMap(@NotNull Function<? super T, ? extends FutureAction<U>> mapper) {
        return new FutureActionImpl<>(() -> submit().thenCompose(res -> mapper.apply(res).submit()), this.executor);
    }

    @Override
    public FutureAction<T> onErrorMap(@Nullable Predicate<? super Throwable> condition, @NotNull Function<? super Throwable, ? extends T> map) {
        return new FutureActionImpl<>(() -> submit().exceptionally(ex -> {
            Throwable cause = (ex instanceof CompletionException) ? ex.getCause() : ex;
            if (condition == null || condition.test(cause)) {
                return map.apply(cause);
            }
            throw (cause instanceof RuntimeException) ? (RuntimeException) cause : new CompletionException(cause);
        }), this.executor);
    }

    @Override
    public FutureAction<T> onErrorFlatMap(@Nullable Predicate<? super Throwable> condition, @NotNull Function<? super Throwable, ? extends FutureAction<? extends T>> map) {
        return new FutureActionImpl<>(() -> {
            CompletableFuture<T> cf = new CompletableFuture<>();
            submit().whenComplete((res, ex) -> {
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
            return cf;
        }, this.executor);
    }

    @Override
    @NotNull
    public FutureAction<T> onErrorReturn(T fallback) {
        return recover(ex -> fallback);
    }

    @Override
    @NotNull
    public FutureAction<T> onSuccess(@NotNull Consumer<? super T> con) {
        return new FutureActionImpl<>(() -> submit().thenAccept(con).thenApply(v -> null), this.executor);
    }

    @Override
    @NotNull
    public FutureAction<T> onFailure(@NotNull Consumer<? super Throwable> failure) {
        return new FutureActionImpl<>(() -> submit().whenComplete((r, ex) -> {
            if (ex != null) failure.accept(ex instanceof CompletionException ? ex.getCause() : ex);
        }), this.executor);
    }

    @Override
    @NotNull
    public FutureAction<T> onExecutor(@NotNull Executor executor) {
        return new FutureActionImpl<>(() -> submit().thenApplyAsync(Function.identity(), executor), executor);
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return submit().cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isDone() {
        return submit().isDone();
    }

    @Override
    public boolean isCancelled() {
        return submit().isCancelled();
    }
}