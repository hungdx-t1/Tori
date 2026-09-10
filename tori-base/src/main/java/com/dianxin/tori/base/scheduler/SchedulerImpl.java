package com.dianxin.tori.base.scheduler;

import com.dianxin.tori.base.lifecycle.ExecutorManager;

import java.util.Map;
import java.util.concurrent.*;

/**
 * Default thread-safe implementation of {@link Scheduler}.
 *
 * <p>Employs a single-threaded daemon scheduled executor as an internal clock ticker
 * to prevent thread starvation, delegating actual task computations to either
 * {@link ExecutorManager#cpu()} or {@link ExecutorManager#io()}.</p>
 */
@SuppressWarnings({"unused", "resource"})
public class SchedulerImpl implements Scheduler {

    /** Single-thread timer acting strictly as an internal countdown clock. */
    private final ScheduledExecutorService timerPool;

    /** Active task registry mapping IDs to their respective wrappers for cancellation tracking. */
    private final Map<Integer, Task> taskMap = new ConcurrentHashMap<>();

    /**
     * Initializes the scheduler with a dedicated daemon timer thread.
     */
    public SchedulerImpl() {
        this.timerPool = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "tori-timer-clock");
            t.setDaemon(true);
            return t;
        });
    }

    /**
     * Wraps a runnable payload to guarantee cache eviction upon completion for non-periodic tasks.
     *
     * @param taskId  the identifier of the task
     * @param run     the original runnable
     * @param isTimer {@code true} if the task repeats periodically
     * @return the wrapped runnable
     */
    private Runnable wrap(int taskId, Runnable run, boolean isTimer) {
        return () -> {
            try {
                run.run();
            } finally {
                if (!isTimer) {
                    taskMap.remove(taskId);
                }
            }
        };
    }

    @Override
    public Task runTask(Runnable runnable) {
        Task task = new Task(true);
        Runnable wrapped = wrap(task.getTaskId(), runnable, false);

        task.setFuture(ExecutorManager.cpu().submit(wrapped));
        taskMap.put(task.getTaskId(), task);
        return task;
    }

    @Override
    public Task runTaskAsync(Runnable runnable) {
        Task task = new Task(false);
        Runnable wrapped = wrap(task.getTaskId(), runnable, false);

        task.setFuture(ExecutorManager.io().submit(wrapped));
        taskMap.put(task.getTaskId(), task);
        return task;
    }

    @Override
    public Task runTaskLater(Runnable runnable, long delay, TimeUnit unit) {
        Task task = new Task(true);
        Runnable wrapped = wrap(task.getTaskId(), runnable, false);

        Future<?> future = timerPool.schedule(() -> ExecutorManager.cpu().execute(wrapped), delay, unit);
        task.setFuture(future);
        taskMap.put(task.getTaskId(), task);
        return task;
    }

    @Override
    public Task runTaskLaterAsync(Runnable runnable, long delay, TimeUnit unit) {
        Task task = new Task(false);
        Runnable wrapped = wrap(task.getTaskId(), runnable, false);

        Future<?> future = timerPool.schedule(() -> ExecutorManager.io().execute(wrapped), delay, unit);
        task.setFuture(future);
        taskMap.put(task.getTaskId(), task);
        return task;
    }

    @Override
    public Task runTaskTimer(Runnable runnable, long delay, long period, TimeUnit unit) {
        Task task = new Task(true);
        Runnable wrapped = wrap(task.getTaskId(), runnable, true);

        Future<?> future = timerPool.scheduleAtFixedRate(() -> ExecutorManager.cpu().execute(wrapped), delay, period, unit);
        task.setFuture(future);
        taskMap.put(task.getTaskId(), task);
        return task;
    }

    @Override
    public Task runTaskTimerAsync(Runnable runnable, long delay, long period, TimeUnit unit) {
        Task task = new Task(false);
        Runnable wrapped = wrap(task.getTaskId(), runnable, true);

        Future<?> future = timerPool.scheduleAtFixedRate(() -> ExecutorManager.io().execute(wrapped), delay, period, unit);
        task.setFuture(future);
        taskMap.put(task.getTaskId(), task);
        return task;
    }

    @Override
    public void cancelTask(int taskId) {
        Task task = taskMap.remove(taskId);
        if (task != null) {
            task.cancel();
        }
    }

    @Override
    public void shutdown() {
        timerPool.shutdownNow();
        for (Task task : taskMap.values()) {
            task.cancel();
        }
        taskMap.clear();
    }
}