package com.dianxin.tori.base.scheduler;

import java.util.concurrent.TimeUnit;

/**
 * Trình quản lý tác vụ hẹn giờ hiện đại, tích hợp chặt chẽ với ExecutorManager.
 * Sử dụng 1 luồng duy nhất làm "Đồng hồ" để đếm ngược, sau đó phân phối
 * công việc thực tế cho CPU Pool hoặc IO Pool.
 */
@SuppressWarnings("unused")
public interface Scheduler {

    /**
     * Chạy một task ngay lập tức trên luồng xử lý chính (Pool).
     */
    Task runTask(Runnable runnable);

    /**
     * Chạy một task bất đồng bộ trên luồng riêng biệt (dành cho IO nặng).
     */
    Task runTaskAsync(Runnable runnable);

    /**
     * Chạy task sau một khoảng thời gian (Delay).
     * @param delay Thời gian chờ (tính bằng mili giây - tick = 50ms)
     */
    Task runTaskLater(Runnable runnable, long delay, TimeUnit unit);

    /**
     * Chạy task bất đồng bộ sau một khoảng thời gian.
     */
    Task runTaskLaterAsync(Runnable runnable, long delay, TimeUnit unit);

    /**
     * Chạy task lặp đi lặp lại (Timer).
     * @param delay Thời gian chờ trước khi bắt đầu
     * @param period Chu kỳ lặp lại
     */
    Task runTaskTimer(Runnable runnable, long delay, long period, TimeUnit unit);

    /**
     * Chạy task lặp lại bất đồng bộ.
     */
    Task runTaskTimerAsync(Runnable runnable, long delay, long period, TimeUnit unit);

    /**
     * Hủy một task dựa trên ID.
     */
    void cancelTask(int taskId);

    /**
     * Hủy tất cả task đang chạy (Dùng khi shutdown bot).
     */
    void shutdown();
}