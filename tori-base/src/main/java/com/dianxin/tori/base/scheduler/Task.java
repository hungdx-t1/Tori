package com.dianxin.tori.base.scheduler;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents a scheduled or running execution unit managed by {@link Scheduler}.
 */
@SuppressWarnings("unused")
public class Task {
    private static final AtomicInteger ID_COUNTER = new AtomicInteger(0);

    private final int taskId;
    private final boolean sync;
    private Future<?> future;

    /**
     * Constructs a new tracked task.
     *
     * @param sync {@code true} if dispatched to CPU pool, {@code false} if dispatched to I/O pool
     */
    public Task(boolean sync) {
        this.taskId = ID_COUNTER.incrementAndGet();
        this.sync = sync;
    }

    /**
     * Retrieves the unique identifier of this task.
     *
     * @return the task ID
     */
    public int getTaskId() {
        return taskId;
    }

    /**
     * Checks whether this task runs on the synchronous/CPU thread pool.
     *
     * @return {@code true} if scheduled on the CPU pool, {@code false} if on the I/O pool
     */
    public boolean isSync() {
        return sync;
    }

    /**
     * Checks whether the task execution has been cancelled.
     *
     * @return {@code true} if cancelled before completion
     */
    public boolean isCancelled() {
        return future != null && future.isCancelled();
    }

    /**
     * Checks whether the task execution completed, terminated with an error, or was cancelled.
     *
     * @return {@code true} if done
     */
    public boolean isDone() {
        return future != null && future.isDone();
    }

    /**
     * Cancels this task. If the task is currently active, it attempts to interrupt the worker thread.
     */
    public void cancel() {
        if (future != null && !future.isCancelled() && !future.isDone()) {
            future.cancel(true);
        }
    }

    /**
     * Attaches the underlying execution future to this task wrapper.
     *
     * @param future the tracked future returned by the executor
     */
    protected void setFuture(Future<?> future) {
        this.future = future;
    }
}