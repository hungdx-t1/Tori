package com.dianxin.tori.base.concurrent;

import com.dianxin.tori.base.lifecycle.ExecutorManager;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;
import java.util.stream.Collectors;

/**
 * Represents an asynchronous execution unit, heavily inspired by JDA's RestAction but
 * designed to be completely independent and powered natively by {@link CompletableFuture}.
 * <p>
 * A FutureAction is lazy; it will not execute until an execution method
 * like {@link #queue()}, {@link #submit()}, or {@link #complete()} is called.
 *
 * @param <T> The expected return type of this action.
 */
@SuppressWarnings({"unused", "unchecked"})
public interface FutureAction<T> {

    // ==========================================
    // STATIC FACTORIES
    // ==========================================

    /**
     * Creates a new FutureAction from a given {@link Callable} task.
     *
     * @param task     The task to be executed.
     * @param executor The executor to run the task on. If null, falls back to the default IO executor.
     * @param <T>      The return type of the task.
     * @return A new FutureAction instance.
     */
    @CheckReturnValue
    static <T> FutureAction<T> action(@NotNull Callable<T> task, @Nullable Executor executor) {
        Executor exec = (executor != null) ? executor : ExecutorManager.io();
        CompletableFuture<T> future = CompletableFuture.supplyAsync(() -> {
            try {
                return task.call();
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        }, exec);
        return new FutureActionImpl<>(future);
    }

    /**
     * Creates a new FutureAction from a given {@link CompletableFuture}.
     *
     * @param future   A {@link CompletableFuture} provided.
     * @param executor The executor to run the task on. If null, falls back to the default IO executor.
     * @param <T>      The return type of the task.
     * @return A new FutureAction instance.
     */
    @CheckReturnValue
    static <T> FutureAction<T> fromCompletableFuture(@NotNull CompletableFuture<T> future, @Nullable Executor executor) {
        FutureAction<T> action = new FutureActionImpl<>(future);
        return executor != null ? action.onExecutor(executor) : action;
    }

    /**
     * Creates a FutureAction that is already completed with the given value.
     *
     * @param value The value to complete the action with.
     * @param <T>   The type of the value.
     * @return A completed FutureAction.
     */
    @CheckReturnValue
    static <T> FutureAction<T> completed(T value) {
        return new FutureActionImpl<>(CompletableFuture.completedFuture(value));
    }

    /**
     * Sequences multiple actions to run sequentially (one after another).
     * This is useful to avoid rate-limiting issues when dispatching multiple heavy requests.
     *
     * @param actions The actions to execute sequentially.
     * @param <E>     The return type of the actions.
     * @return A single FutureAction containing a List of all results in order.
     */
    @NotNull
    @CheckReturnValue
    static <E> FutureAction<List<E>> collect(@NotNull FutureAction<? extends E>... actions) {
        if (actions == null || actions.length == 0) return completed(new ArrayList<>());

        Iterator<FutureAction<? extends E>> iterator = Arrays.asList(actions).iterator();
        FutureAction<List<E>> result = iterator.next().map(firstItem -> {
            List<E> list = new ArrayList<>();
            list.add(firstItem);
            return list;
        });

        while (iterator.hasNext()) {
            FutureAction<? extends E> nextAction = iterator.next();
            result = result.flatMap(currentList ->
                    nextAction.map(nextItem -> {
                        currentList.add(nextItem);
                        return currentList;
                    })
            );
        }
        return result;
    }

    /**
     * Dispatches multiple actions to run in parallel simultaneously.
     * The overall execution time will be roughly equal to the longest individual action.
     *
     * @param actions The actions to execute concurrently.
     * @param <E>     The return type of the actions.
     * @return A single FutureAction containing a List of all results.
     */
    @NotNull
    @CheckReturnValue
    static <E> FutureAction<List<E>> collectParallel(@NotNull FutureAction<? extends E>... actions) {
        if (actions == null || actions.length == 0) return completed(new ArrayList<>());

        return action(() -> {
            List<CompletableFuture<? extends E>> futures = Arrays.stream(actions)
                    .map(FutureAction::submit)
                    .collect(Collectors.toList());

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            return futures.stream()
                    .map(CompletableFuture::join)
                    .collect(Collectors.toList());
        }, null);
    }

    // ==========================================
    // CHECKS & DEADLINES
    // ==========================================

    /**
     * Sets a condition (check) that must return {@code true} before the action is executed.
     * If the check returns {@code false}, the action is silently cancelled.
     * This overrides any previously set checks.
     *
     * @param checks The condition to check before execution, or null to clear checks.
     * @return The current FutureAction instance for chaining.
     */
    @CheckReturnValue
    @NotNull FutureAction<T> setChecks(@Nullable BooleanSupplier checks);

    /**
     * @return The currently configured execution check, or null if none is set.
     */
    @CheckReturnValue
    @Nullable BooleanSupplier getChecks();

    /**
     * Appends a new check to the existing ones using a logical AND (&&).
     * Both the existing checks and the new check must pass for the action to execute.
     *
     * @param checks The new condition to append.
     * @return The current FutureAction instance for chaining.
     */
    @NotNull
    @CheckReturnValue
    default FutureAction<T> addCheck(@NotNull BooleanSupplier checks) {
        BooleanSupplier check = getChecks();
        return setChecks(() -> (check == null || check.getAsBoolean()) && checks.getAsBoolean());
    }

    /**
     * Sets an absolute Unix timestamp deadline for this action.
     * If execution begins after this timestamp, the action will be silently cancelled.
     *
     * @param timestamp The deadline timestamp in milliseconds.
     * @return The current FutureAction instance for chaining.
     */
    @CheckReturnValue
    @NotNull FutureAction<T> deadline(long timestamp);

    /**
     * Sets a relative timeout for this action.
     *
     * @param timeout The duration to wait before timing out.
     * @param unit    The time unit for the timeout parameter.
     * @return The current FutureAction instance for chaining.
     */
    @CheckReturnValue
    @NotNull
    default FutureAction<T> timeout(long timeout, @NotNull TimeUnit unit) {
        return deadline(timeout <= 0 ? 0 : System.currentTimeMillis() + unit.toMillis(timeout));
    }

    // ==========================================
    // EXECUTION
    // ==========================================

    /**
     * Submits the action for asynchronous execution.
     * Success and failure will be handled by the default callbacks.
     */
    default void queue() {
        queue(null, null);
    }

    /**
     * Submits the action for asynchronous execution with a specific success callback.
     *
     * @param success The callback triggered when the action completes successfully.
     */
    default void queue(@Nullable Consumer<? super T> success) {
        queue(success, null);
    }

    /**
     * Submits the action for asynchronous execution with specific success and failure callbacks.
     *
     * @param success The callback triggered upon successful execution.
     * @param failure The callback triggered if an exception occurs.
     */
    void queue(@Nullable Consumer<? super T> success, @Nullable Consumer<? super Throwable> failure);

    /**
     * Blocks the current thread and executes the action synchronously.
     *
     * @return The result of the action.
     * @throws RuntimeException if the action fails.
     */
    default T complete() {
        return complete(true);
    }

    /**
     * Blocks the current thread and executes the action synchronously.
     *
     * @param shouldQueue Whether it should be queued internally.
     * @return The result of the action.
     */
    T complete(boolean shouldQueue);

    /**
     * Blocks the current thread to execute the action synchronously, with a maximum waiting time.
     *
     * @param timeout The maximum time to wait.
     * @param unit    The time unit of the timeout argument.
     * @return The result of the action.
     * @throws TimeoutException if the wait timed out.
     */
    T complete(long timeout, @NotNull TimeUnit unit) throws TimeoutException;

    /**
     * Submits the action for asynchronous execution and returns a {@link CompletableFuture}.
     *
     * @return A CompletableFuture representing the pending result of the action.
     */
    @NotNull
    default CompletableFuture<T> submit() {
        return submit(true);
    }

    /**
     * Submits the action and returns a {@link CompletableFuture}.
     *
     * @param shouldQueue Whether it should be evaluated in the queue immediately.
     * @return A CompletableFuture representing the pending result.
     */
    @NotNull CompletableFuture<T> submit(boolean shouldQueue);

    // ==========================================
    // TRANSFORMATION & COMPOSITION
    // ==========================================

    /**
     * Recovers from an exception by providing a fallback value.
     * If the action fails, the fallback function is called to supply an alternative successful result.
     *
     * @param fallback The function to provide a fallback value based on the thrown exception.
     * @return A new FutureAction containing the original or recovered value.
     */
    @NotNull
    @CheckReturnValue
    FutureAction<T> recover(@NotNull Function<Throwable, T> fallback);

    /**
     * Transforms the result of this action using the provided mapping function.
     *
     * @param mapper The function to transform the result.
     * @param <U>    The type of the new result.
     * @return A new FutureAction containing the transformed result.
     */
    @NotNull
    @CheckReturnValue
    <U> FutureAction<U> map(@NotNull Function<? super T, ? extends U> mapper);

    /**
     * Chains another asynchronous action to run after this one completes successfully.
     *
     * @param mapper The function returning the next FutureAction to execute.
     * @param <U>    The result type of the subsequent action.
     * @return A new FutureAction representing the chained execution.
     */
    @NotNull
    @CheckReturnValue
    <U> FutureAction<U> flatMap(@NotNull Function<? super T, ? extends FutureAction<U>> mapper);

    /**
     * Maps the output of this action into a safe {@link Result} wrapper, which encapsulates
     * either the success value or the thrown exception.
     *
     * @return A new FutureAction yielding a Result wrapper.
     */
    @NotNull
    @CheckReturnValue
    default CompletableFuture<Result<T>> mapToResult() {
        return submit().handle((res, ex) -> {
            if (ex != null) return Result.failure(ex instanceof CompletionException ? ex.getCause() : ex);
            return Result.success(res);
        });
    }

    /**
     * Combines the result of this action with another action using an accumulator function.
     *
     * @param other       The second action to execute.
     * @param accumulator The function that combines both results.
     * @param <U>         The result type of the second action.
     * @param <O>         The final composed output type.
     * @return A new FutureAction containing the combined result.
     */
    @NotNull
    @CheckReturnValue
    default <U, O> FutureAction<O> and(@NotNull FutureAction<U> other, @NotNull BiFunction<? super T, ? super U, ? extends O> accumulator) {
        return this.flatMap(t -> other.map(u -> accumulator.apply(t, u)));
    }

    /**
     * Zips the results of multiple actions together in parallel execution.
     *
     * @param first The first additional action to run.
     * @param other Further actions to run.
     * @return A FutureAction that yields a List of all results.
     */
    @NotNull
    @CheckReturnValue
    default FutureAction<List<T>> zip(@NotNull FutureAction<? extends T> first, @NotNull FutureAction<? extends T>... other) {
        List<FutureAction<? extends T>> list = new ArrayList<>();
        list.add(this);
        list.add(first);
        Collections.addAll(list, other);
        return collectParallel(list.toArray(new FutureAction[0]));
    }

    // ==========================================
    // ERROR HANDLING
    // ==========================================

    /**
     * Maps the exception thrown by this action into a default successful result.
     *
     * @param map The function to map the throwable to a successful result.
     * @return A new FutureAction containing the recovered result.
     */
    @CheckReturnValue
    default FutureAction<T> onErrorMap(@NotNull Function<? super Throwable, ? extends T> map) {
        return onErrorMap(null, map);
    }

    /**
     * Conditionally maps the exception into a successful result.
     *
     * @param condition The predicate that must be met to apply the error mapping.
     * @param map       The mapping function.
     * @return A new FutureAction.
     */
    @CheckReturnValue
    FutureAction<T> onErrorMap(@Nullable Predicate<? super Throwable> condition, @NotNull Function<? super Throwable, ? extends T> map);

    /**
     * Conditionally handles an exception by returning an entirely new asynchronous fallback action.
     *
     * @param condition The predicate to trigger the fallback.
     * @param map       The function returning the fallback FutureAction.
     * @return A new FutureAction.
     */
    @CheckReturnValue
    FutureAction<T> onErrorFlatMap(@Nullable Predicate<? super Throwable> condition, @NotNull Function<? super Throwable, ? extends FutureAction<? extends T>> map);

    /**
     * Directly returns a hardcoded fallback value if an exception occurs.
     *
     * @param fallback The fallback value.
     * @return A new FutureAction.
     */
    @CheckReturnValue
    @NotNull FutureAction<T> onErrorReturn(T fallback);

    /**
     * Appends a success callback without interrupting the chain.
     *
     * @param con The callback to execute on success.
     * @return The current FutureAction instance.
     */
    @CheckReturnValue
    @NotNull FutureAction<T> onSuccess(@NotNull Consumer<? super T> con);

    /**
     * Appends a failure callback without interrupting the chain.
     *
     * @param failure The callback to execute on failure.
     * @return The current FutureAction instance.
     */
    @CheckReturnValue
    @NotNull FutureAction<T> onFailure(@NotNull Consumer<? super Throwable> failure);

    // ==========================================
    // DELAY & SCHEDULING
    // ==========================================

    /**
     * Delays the execution or completion of this action by a specified duration.
     *
     * @param delay The amount of time to delay.
     * @param unit  The time unit.
     * @return A new FutureAction that will yield the result after the delay.
     */
    @NotNull
    @CheckReturnValue
    default FutureAction<T> delay(long delay, @NotNull TimeUnit unit) {
        CompletableFuture<T> delayedFuture = new CompletableFuture<>();
        this.submit().whenCompleteAsync((res, ex) -> {
            if (ex != null) delayedFuture.completeExceptionally(ex);
            else delayedFuture.complete(res);
        }, CompletableFuture.delayedExecutor(delay, unit));
        return new FutureActionImpl<>(delayedFuture);
    }

    default void queueAfter(long delay, @NotNull TimeUnit unit) {
        queueAfter(delay, unit, null, null);
    }

    default void queueAfter(long delay, @NotNull TimeUnit unit, @Nullable Consumer<? super T> success) {
        queueAfter(delay, unit, success, null);
    }

    /**
     * Queues the action asynchronously after waiting for a specified delay.
     *
     * @param delay   The delay duration.
     * @param unit    The time unit.
     * @param success Success callback.
     * @param failure Failure callback.
     */
    default void queueAfter(long delay, @NotNull TimeUnit unit, @Nullable Consumer<? super T> success, @Nullable Consumer<? super Throwable> failure) {
        this.delay(delay, unit).queue(success, failure);
    }

    /**
     * Transfers the completion processing of this action to a specific executor.
     *
     * @param executor The target executor.
     * @return A new FutureAction handling continuations on the specified executor.
     */
    @CheckReturnValue
    @NotNull FutureAction<T> onExecutor(@NotNull Executor executor);

    boolean cancel(boolean mayInterruptIfRunning);

    boolean isDone();

    boolean isCancelled();

    // ==========================================
    // RESULT CLASS
    // ==========================================

    /**
     * A wrapper class to safely hold either the success value or the thrown exception.
     */
    class Result<T> {
        private final T value;
        private final Throwable error;

        private Result(T value, Throwable error) {
            this.value = value;
            this.error = error;
        }

        @NotNull
        public static <E> Result<E> success(@Nullable E value) {
            return new Result<>(value, null);
        }

        @NotNull
        public static <E> Result<E> failure(@NotNull Throwable error) {
            return new Result<>(null, error);
        }

        public boolean isFailure() {
            return error != null;
        }

        public boolean isSuccess() {
            return error == null;
        }

        public T get() {
            if (isFailure()) throw new IllegalStateException(error);
            return value;
        }

        @Nullable
        public Throwable getFailure() {
            return error;
        }
    }
}