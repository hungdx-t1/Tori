package com.dianxin.tori.base.scheduler;

import java.util.concurrent.TimeUnit;

/**
 * Task scheduling management interface integrated with {@code ExecutorManager}.
 *
 * <p>Uses a single dedicated clock thread to count down time offsets and subsequently
 * dispatches execution payloads to either the CPU thread pool or the I/O thread pool.</p>
 */
@SuppressWarnings("unused")
public interface Scheduler {

    /**
     * Executes a task immediately on the primary CPU-bound thread pool.
     *
     * @param runnable the task logic to execute
     * @return a tracked {@link Task} instance
     */
    Task runTask(Runnable runnable);

    /**
     * Executes a task immediately on the asynchronous I/O-bound thread pool.
     *
     * @param runnable the task logic to execute
     * @return a tracked {@link Task} instance
     */
    Task runTaskAsync(Runnable runnable);

    /**
     * Schedules a task to execute after a specified delay on the CPU thread pool.
     *
     * @param runnable the task logic to execute
     * @param delay    the time to wait before executing
     * @param unit     the time unit of the delay parameter
     * @return a tracked {@link Task} instance
     */
    Task runTaskLater(Runnable runnable, long delay, TimeUnit unit);

    /**
     * Schedules a task to execute asynchronously after a specified delay on the I/O thread pool.
     *
     * @param runnable the task logic to execute
     * @param delay    the time to wait before executing
     * @param unit     the time unit of the delay parameter
     * @return a tracked {@link Task} instance
     */
    Task runTaskLaterAsync(Runnable runnable, long delay, TimeUnit unit);

    /**
     * Schedules a repeated task to execute on the CPU thread pool.
     *
     * @param runnable the task logic to execute
     * @param delay    the time to wait before the first execution
     * @param period   the interval between successive executions
     * @param unit     the time unit of the delay and period parameters
     * @return a tracked {@link Task} instance
     */
    Task runTaskTimer(Runnable runnable, long delay, long period, TimeUnit unit);

    /**
     * Schedules a repeated task to execute asynchronously on the I/O thread pool.
     *
     * @param runnable the task logic to execute
     * @param delay    the time to wait before the first execution
     * @param period   the interval between successive executions
     * @param unit     the time unit of the delay and period parameters
     * @return a tracked {@link Task} instance
     */
    Task runTaskTimerAsync(Runnable runnable, long delay, long period, TimeUnit unit);

    /**
     * Cancels an active or scheduled task matching the unique task ID.
     *
     * @param taskId the unique identifier of the target task
     */
    void cancelTask(int taskId);

    /**
     * Cancels all currently scheduled tasks and releases underlying thread pool resources.
     */
    void shutdown();
}