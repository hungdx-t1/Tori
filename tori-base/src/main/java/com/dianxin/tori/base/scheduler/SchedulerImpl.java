package com.dianxin.tori.base.scheduler;

import com.dianxin.core.api.lifecycle.ExecutorManager;

import java.util.Map;
import java.util.concurrent.*;

@SuppressWarnings({"unused", "resource"})
public class SchedulerImpl implements Scheduler {
    private final ScheduledExecutorService timerPool; // Đồng hồ báo thức (Chỉ đếm ngược, không chạy tác vụ nặng ở đây)
    private final Map<Integer, Task> taskMap = new ConcurrentHashMap<>(); // Lưu trữ các task để có thể gọi hàm cancelTask(id)

    public SchedulerImpl() {
        this.timerPool = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "dianxin-timer-clock");
            t.setDaemon(true); // Tự động tắt khi bot tắt
            return t;
        });
    }

    /**
     * Bọc Runnable lại để tự động xóa khỏi Map khi chạy xong (Tránh rò rỉ bộ nhớ).
     */
    private Runnable wrap(int taskId, Runnable run, boolean isTimer) {
        return () -> {
            try {
                run.run();
            } finally {
                // Nếu không phải là tác vụ lặp lại (Timer), xóa nó khỏi bộ nhớ sau khi chạy xong
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
        // Đẩy thẳng vào CPU Pool chạy ngay lập tức
        task.setFuture(ExecutorManager.cpu().submit(wrapped));
        taskMap.put(task.getTaskId(), task);
        return task;
    }

    @Override
    public Task runTaskAsync(Runnable runnable) {
        Task task = new Task(false);
        Runnable wrapped = wrap(task.getTaskId(), runnable, false);
        // Đẩy thẳng vào IO Pool chạy ngay lập tức
        task.setFuture(ExecutorManager.io().submit(wrapped));
        taskMap.put(task.getTaskId(), task);
        return task;
    }

    @Override
    public Task runTaskLater(Runnable runnable, long delay, TimeUnit unit) {
        Task task = new Task(true);
        Runnable wrapped = wrap(task.getTaskId(), runnable, false);

        // Hẹn giờ, khi hết giờ thì nhét vào CPU Pool
        Future<?> future = timerPool.schedule(() -> ExecutorManager.cpu().execute(wrapped), delay, unit);

        task.setFuture(future);
        taskMap.put(task.getTaskId(), task);
        return task;
    }

    @Override
    public Task runTaskLaterAsync(Runnable runnable, long delay, TimeUnit unit) {
        Task task = new Task(false);
        Runnable wrapped = wrap(task.getTaskId(), runnable, false);

        // Hẹn giờ, khi hết giờ thì nhét vào IO Pool
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