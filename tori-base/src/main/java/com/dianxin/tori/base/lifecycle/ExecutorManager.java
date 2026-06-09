package com.dianxin.tori.base.lifecycle;

import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NullMarked;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * Quản lý tập trung các {@link ExecutorService} (Thread Pool) cho toàn bộ framework.
 * <p>
 * Class này cung cấp hai loại executor chính:
 * <ul>
 * <li><b>IO Executor:</b> Dành cho các tác vụ blocking, chờ đợi mạng, ổ đĩa (Database, HTTP Requests, File I/O).</li>
 * <li><b>CPU Executor:</b> Dành cho các tác vụ tính toán nặng, xử lý logic phức tạp không blocking.</li>
 * </ul>
 * <p>
 * Class này Thread-safe và chỉ nên được khởi tạo một lần duy nhất thông qua {@link #initialize()}.
 */
@SuppressWarnings({"ClassCanBeRecord", "unused"})
public final class ExecutorManager {
    private static ExecutorService IO_EXECUTOR;
    private static ExecutorService CPU_EXECUTOR;
    private static final AtomicBoolean INITIALIZED = new AtomicBoolean(false);

    private ExecutorManager() { }

    /**
     * Khởi tạo toàn bộ executor cho framework.
     * <p>
     * Phương thức này chỉ chạy một lần duy nhất. Nếu gọi lần thứ hai sẽ không có tác dụng.
     * Nên được gọi trong giai đoạn bootstrap (khởi động) của ứng dụng.
     */
    public static void initialize() {
        if (INITIALIZED.compareAndSet(false, true)) {
            ExecutorBuilder builder = new ExecutorBuilder();
            IO_EXECUTOR = builder.io();
            CPU_EXECUTOR = builder.cpu();
        }
    }

    // phương thức mới, overload cái cũ
    public static void initialize(ExecutorService io, ExecutorService cpu) {
        if (INITIALIZED.compareAndSet(false, true)) {
            ExecutorBuilder builder = new ExecutorBuilder(io, cpu);
            IO_EXECUTOR = builder.io();
            CPU_EXECUTOR = builder.cpu();
        }
    }

    /**
     * Lấy {@link ExecutorService} dành cho các tác vụ I/O (Input/Output).
     * <p>
     * Pool này thường có số lượng thread lớn (hoặc cached) để xử lý nhiều tác vụ chờ đợi đồng thời.
     *
     * @return ExecutorService cho I/O tasks.
     * @throws IllegalStateException Nếu {@link #initialize()} chưa được gọi trước đó.
     */
    public static ExecutorService io() {
        ensureInitialized();
        return IO_EXECUTOR;
    }

    /**
     * Lấy {@link ExecutorService} dành cho các tác vụ tính toán (CPU-bound).
     * <p>
     * Pool này có số lượng thread giới hạn (thường bằng số nhân CPU) để tối ưu hóa context switching.
     *
     * @return ExecutorService cho CPU tasks.
     * @throws IllegalStateException Nếu {@link #initialize()} chưa được gọi trước đó.
     */
    public static ExecutorService cpu() {
        ensureInitialized();
        return CPU_EXECUTOR;
    }

    /**
     * Tắt (Shutdown) toàn bộ các executor đang chạy.
     * <p>
     * Nên gọi phương thức này khi bot hoặc ứng dụng dừng hoạt động để giải phóng tài nguyên thread.
     */
    public static void shutdown() {
        if (!INITIALIZED.get()) return;

        if (IO_EXECUTOR != null && !IO_EXECUTOR.isShutdown()) {
            IO_EXECUTOR.shutdown();
        }
        if (CPU_EXECUTOR != null && !CPU_EXECUTOR.isShutdown()) {
            CPU_EXECUTOR.shutdown();
        }
    }

    private static void ensureInitialized() {
        if (!INITIALIZED.get()) {
            throw new IllegalStateException(
                    "ExecutorManager has not been initialized. Call ExecutorManager.initialize() first."
            );
        }
    }

    /**
     * Chạy một tác vụ bất đồng bộ (Async) sử dụng {@link #io()} executor.
     *
     * @param supplier Logic cần thực thi trả về kết quả kiểu T.
     * @param <T> Kiểu dữ liệu trả về.
     * @return CompletableFuture chứa kết quả của tác vụ.
     */
    public static <T> CompletableFuture<T> runIoAsync(@NotNull Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, io());
    }

    /**
     * Chạy một tác vụ bất đồng bộ (Async) sử dụng {@link #cpu()} executor.
     *
     * @param supplier Logic cần thực thi trả về kết quả kiểu T.
     * @param <T> Kiểu dữ liệu trả về.
     * @return CompletableFuture chứa kết quả của tác vụ.
     */
    public static <T> CompletableFuture<T> runCpuAsync(@NotNull Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, cpu());
    }

    /**
     * Helper class để xây dựng và cấu hình các {@link ExecutorService}.
     * <p>
     * Class này đóng vai trò thay thế cho {@code ExecutorFactory} cũ.
     */
    @NullMarked
    private static class ExecutorBuilder {
        private final ExecutorService io;
        private final ExecutorService cpu;

        /**
         * Khởi tạo Builder với cấu hình mặc định dựa trên thông số phần cứng.
         */
        public ExecutorBuilder() {
            this.io = createDefaultIoExecutor();
            this.cpu = createDefaultCpuExecutor();
        }

        /**
         * Khởi tạo Builder với các ExecutorService tùy chỉnh (dùng cho Unit Test hoặc cấu hình đặc biệt).
         *
         * @param io  ExecutorService cho I/O.
         * @param cpu ExecutorService cho CPU.
         */
        public ExecutorBuilder(ExecutorService io, ExecutorService cpu) {
            this.io = io;
            this.cpu = cpu;
        }

        public ExecutorService io() {
            return io;
        }

        public ExecutorService cpu() {
            return cpu;
        }

        /**
         * Tạo ThreadPool mặc định cho I/O:
         * <ul>
         * <li>Số thread: max(4, Core * 2)</li>
         * <li>Tên thread: dianxin-io-[id]</li>
         * <li>Daemon: true (tự động tắt khi main thread tắt)</li>
         * </ul>
         */
        private ExecutorService createDefaultIoExecutor() {
            int cores = Runtime.getRuntime().availableProcessors();
            int threads = Math.max(4, cores * 2);
            AtomicInteger counter = new AtomicInteger(1);

            return Executors.newFixedThreadPool(threads, r -> {
                Thread t = new Thread(r);
                t.setName("dianxin-io-" + counter.getAndIncrement());
                t.setDaemon(true);
                return t;
            });
        }

        /**
         * Tạo ThreadPool mặc định cho CPU:
         * <ul>
         * <li>Số thread: Bằng số nhân CPU thực tế</li>
         * <li>Tên thread: dianxin-cpu-[id]</li>
         * <li>Daemon: true</li>
         * </ul>
         */
        private ExecutorService createDefaultCpuExecutor() {
            int cores = Runtime.getRuntime().availableProcessors();
            AtomicInteger counter = new AtomicInteger(1);

            return Executors.newFixedThreadPool(cores, r -> {
                Thread t = new Thread(r);
                t.setName("dianxin-cpu-" + counter.getAndIncrement());
                t.setDaemon(true);
                return t;
            });
        }
    }
}