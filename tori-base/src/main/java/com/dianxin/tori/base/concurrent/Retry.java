package com.dianxin.tori.base.concurrent;

import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;

import java.time.Duration;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Represents a retry strategy configuration for asynchronous execution units such as {@link FutureAction}.
 * <p>
 * Inspired by reactive backoff models, instances of this interface define attempt limits,
 * delay intervals, conditional error filtering, and interception hooks for each retry attempt.
 */
@NullMarked
@SuppressWarnings("unused")
public interface Retry {

    /**
     * Creates a retry strategy that immediately retries up to the given maximum attempts with zero delay.
     *
     * @param maxAttempts The maximum number of retry attempts. Must be non-negative.
     * @return A new {@link Retry} strategy instance.
     */
    @CheckReturnValue
    static Retry max(long maxAttempts) {
        return fixedDelay(maxAttempts, Duration.ZERO);
    }

    /**
     * Creates a retry strategy that retries up to the given maximum attempts with a fixed backoff delay between attempts.
     *
     * @param maxAttempts The maximum number of retry attempts. Must be non-negative.
     * @param delay       The fixed duration to wait before triggering the next retry attempt.
     * @return A new {@link Retry} strategy instance.
     */
    @CheckReturnValue
    static Retry fixedDelay(long maxAttempts, Duration delay) {
        return new RetryImpl(maxAttempts, delay);
    }

    /**
     * Retrieves the maximum number of retry attempts configured for this strategy.
     *
     * @return The maximum retry count.
     */
    long getMaxAttempts();

    /**
     * Retrieves the delay duration configured between retry attempts.
     *
     * @return The backoff {@link Duration}.
     */
    Duration getDelay();

    /**
     * Retrieves the configured listener callback executed prior to every retry invocation.
     *
     * @return The retry consumer callback, or {@code null} if none was registered.
     */
    @Nullable Consumer<RetryContext> getRetryListener();

    /**
     * Retrieves the predicate used to evaluate whether an exception qualifies for a retry attempt.
     *
     * @return The error evaluation predicate, or {@code null} if all exceptions are eligible.
     */
    @Nullable Predicate<? super Throwable> getFilter();

    /**
     * Registers a listener to be invoked before each retry execution is dispatched.
     * <p>
     * Typically used for tracking metrics, debugging, or logging contextual retry details.
     *
     * @param action A consumer accepting the active {@link RetryContext}.
     * @return The current {@link Retry} instance for method chaining.
     */
    @CheckReturnValue
    Retry doBeforeRetry(Consumer<RetryContext> action);

    /**
     * Specifies a condition to filter which exceptions are eligible for retry attempts.
     * If an exception does not satisfy this predicate, the failure propagates immediately without further retries.
     *
     * @param condition A predicate evaluating the encountered {@link Throwable}.
     * @return The current {@link Retry} instance for method chaining.
     */
    @CheckReturnValue
    Retry filter(Predicate<? super Throwable> condition);

    /**
     * Encapsulates runtime contextual metadata associated with the current retry iteration.
     */
    @NullMarked
    interface RetryContext {

        /**
         * The current attempt index of the retry lifecycle (1-based index).
         *
         * @return The current iteration number.
         */
        long iteration();

        /**
         * The total maximum number of retries configured in the strategy.
         *
         * @return The maximum allowable attempts.
         */
        long totalRetries();

        /**
         * The exception that caused the previous execution attempt to fail.
         *
         * @return The root or wrapped {@link Throwable}.
         */
        Throwable exception();
    }
}