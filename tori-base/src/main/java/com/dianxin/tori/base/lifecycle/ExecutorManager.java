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
 * Centralized manager handling core {@link ExecutorService} (Thread Pools) across the framework.
 * <p>
 * This manager segments execution environments into two distinct runtime channels:
 * <ul>
 * <li><b>IO Executor:</b> Reserved for blocking operations waiting on external factors like networking or disk speeds (e.g., Database pools, HTTP Requests, File I/O).</li>
 * <li><b>CPU Executor:</b> Tailored for compute-heavy, complex application logic execution that runs without thread blocking.</li>
 * </ul>
 * <p>
 * This utility class is entirely thread-safe and should be initialized exactly once during application bootstrap via {@link #initialize()}.
 */
@SuppressWarnings({"ClassCanBeRecord", "unused"})
public final class ExecutorManager {
    private static ExecutorService IO_EXECUTOR;
    private static ExecutorService CPU_EXECUTOR;
    private static final AtomicBoolean INITIALIZED = new AtomicBoolean(false);

    private ExecutorManager() { }

    /**
     * Initializes default managed thread pool setups for the framework ecosystem.
     * <p>
     * This execution block executes atomically once. Subsequent configuration attempts
     * are completely ignored. Call this during the bootstrap sequence of your application.
     */
    public static void initialize() {
        if (INITIALIZED.compareAndSet(false, true)) {
            ExecutorBuilder builder = new ExecutorBuilder();
            IO_EXECUTOR = builder.io();
            CPU_EXECUTOR = builder.cpu();
        }
    }

    /**
     * Overloaded initialization method to pass custom external {@link ExecutorService} instances.
     * Useful for specialized server configuration hooks or automated unit testing parameters.
     *
     * @param io  the custom I/O bounded task executor service
     * @param cpu the custom CPU bounded task executor service
     */
    public static void initialize(ExecutorService io, ExecutorService cpu) {
        if (INITIALIZED.compareAndSet(false, true)) {
            ExecutorBuilder builder = new ExecutorBuilder(io, cpu);
            IO_EXECUTOR = builder.io();
            CPU_EXECUTOR = builder.cpu();
        }
    }

    /**
     * Gets the {@link ExecutorService} designated for Input/Output (I/O) bound operations.
     * <p>
     * This pool scales or holds a higher volume of open connections to seamlessly process multiple
     * concurrently blocking network or thread-wait requests.
     *
     * @return the I/O specialized ExecutorService instance
     * @throws IllegalStateException if {@link #initialize()} hasn't been explicitly triggered beforehand
     */
    public static ExecutorService io() {
        ensureInitialized();
        return IO_EXECUTOR;
    }

    /**
     * Gets the {@link ExecutorService} designated for heavy data processing or CPU-bound tasks.
     * <p>
     * Thread counts in this pool are strategically bounded by hardware resources (usually
     * matching physical core capacities) to minimize context-switching overhead penalty.
     *
     * @return the calculation intensive ExecutorService instance
     * @throws IllegalStateException if {@link #initialize()} hasn't been explicitly triggered beforehand
     */
    public static ExecutorService cpu() {
        ensureInitialized();
        return CPU_EXECUTOR;
    }

    /**
     * Gracefully shuts down all managed running framework executor pools.
     * <p>
     * Invoke this method during application/bot teardown lifecycles to guarantee proper
     * cleanup of underlying thread resources.
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

    /**
     * Assures initialization lifecycle requirements are completed prior to usage.
     */
    private static void ensureInitialized() {
        if (!INITIALIZED.get()) {
            throw new IllegalStateException(
                    "ExecutorManager has not been initialized. Call ExecutorManager.initialize() first."
            );
        }
    }

    /**
     * Executes an asynchronous background task on the managed {@link #io()} executor pool.
     *
     * @param supplier the functional logic task wrapper returning a computation result of type T
     * @param <T>      the type of data returned by the execution block
     * @return a {@link CompletableFuture} tracking the pending execution context status
     */
    public static <T> CompletableFuture<T> runIoAsync(@NotNull Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, io());
    }

    /**
     * Executes an asynchronous background task on the managed {@link #cpu()} executor pool.
     *
     * @param supplier the functional logic task wrapper returning a computation result of type T
     * @param <T>      the type of data returned by the execution block
     * @return a {@link CompletableFuture} tracking the pending execution context status
     */
    public static <T> CompletableFuture<T> runCpuAsync(@NotNull Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, cpu());
    }

    /**
     * Inner helper class responsible for building and configuring managed {@link ExecutorService} units.
     * Acts as an upgrade path replacing the previous legacy {@code ExecutorFactory}.
     */
    @NullMarked
    private static class ExecutorBuilder {
        private final ExecutorService io;
        private final ExecutorService cpu;

        /**
         * Instantiates the builder using optimal standard defaults evaluated by native hardware metrics.
         */
        public ExecutorBuilder() {
            this.io = createDefaultIoExecutor();
            this.cpu = createDefaultCpuExecutor();
        }

        /**
         * Instantiates the builder pointing towards explicit user customized configurations.
         *
         * @param io  the custom I/O executor target instance
         * @param cpu the custom CPU executor target instance
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
         * Creates a standard fixed thread pool configuration engineered for I/O operations:
         * <ul>
         * <li>Thread Allocation size: max(4, Core Count * 2)</li>
         * <li>Thread naming schema: dianxin-io-[id]</li>
         * <li>Daemon property: true (automatically terminates when the main execution thread exits)</li>
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
         * Creates a default thread pool specialized for high CPU usage:
         * <ul>
         * <li>Thread Allocation size: Exactly matching available logical processor cores</li>
         * <li>Thread naming schema: dianxin-cpu-[id]</li>
         * <li>Daemon property: true</li>
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