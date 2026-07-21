package com.dianxin.tori.base.scheduler;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("unused")
public class Task {
    private static final AtomicInteger idCounter = new AtomicInteger(0);

    private final int taskId;
    private final boolean isSync;
    private Future<?> future;

    public Task(boolean isSync) {
        this.taskId = idCounter.incrementAndGet();
        this.isSync = isSync;
    }

    public int getTaskId() {
        return taskId;
    }

    public boolean isSync() {
        return isSync;
    }

    /**
     * Hủy tác vụ. Nếu tác vụ đang chạy, nó sẽ cố gắng ngắt (interrupt) luồng.
     */
    public void cancel() {
        if (future != null && !future.isCancelled() && !future.isDone()) {
            future.cancel(true);
        }
    }

    protected void setFuture(Future<?> future) {
        this.future = future;
    }
}