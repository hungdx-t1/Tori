package com.dianxin.tori.server.commands.console;

import com.dianxin.core.api.console.commands.AbstractConsoleCommand;

/**
 * A console command that provides debugging information about the system environment.
 * <p>
 * Usage:
 * <ul>
 *     <li>{@code debug properties} - Displays all system properties</li>
 *     <li>{@code debug environment} - Displays key environment information</li>
 *     <li>{@code debug bots} - Displays information about running bots</li>
 *     <li>{@code debug threads} - Displays thread information</li>
 *     <li>{@code debug gc} - Displays garbage collection statistics</li>
 *     <li>{@code debug performance} - Displays performance metrics</li>
 * </ul>
 */
public class DebugConsoleCommand extends AbstractConsoleCommand {
    public DebugConsoleCommand() {
        super("debug");
    }

    @Override
    public void execute(String[] args) {
        if (args.length != 1) {
            getLogger().info("Usage: 'debug {properties|environment|bots|threads|gc|performance}'");
            return;
        }

        switch (args[0]) {
            case "properties":
                displaySystemProperties();
                break;
            case "environment":
                displayEnvironmentInfo();
                break;
            case "bots":
                displayBotInformation();
                break;
            case "threads":
                displayThreadInformation();
                break;
            case "gc":
                displayGCInformation();
                break;
            case "performance":
                displayPerformanceMetrics();
                break;
            default:
                getLogger().info("Unknown debug option '{}'. Available options: properties, environment, bots, threads, gc, performance", args[0]);
                break;
        }
    }

    /**
     * Displays all system properties in a formatted manner.
     */
    private void displaySystemProperties() {
        StringBuilder builder = new StringBuilder("System Properties:\n");
        System.getProperties().forEach((key, value) -> builder.append(key).append(" = ").append(value).append("\n"));
        getLogger().info(builder.toString());
    }

    /**
     * Displays key environment information including Java version, OS details, and memory information.
     */
    private void displayEnvironmentInfo() {
        StringBuilder builder = new StringBuilder("Environment Information:\n");
        builder.append("- Java version: ").append(System.getProperty("java.version")).append("\n");
        builder.append("- Java VM: ").append(System.getProperty("java.vm.name")).append("\n");
        builder.append("- Java VM architecture: ").append(System.getProperty("os.arch")).append("\n");
        builder.append("- OS: ").append(System.getProperty("os.name")).append("\n");
        builder.append("- OS version: ").append(System.getProperty("os.version")).append("\n");

        // Memory information
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long maxMemory = runtime.maxMemory();
        long usedMemory = totalMemory - freeMemory;

        builder.append("- Memory (RAM):\n");
        builder.append("  * Total: ").append(formatBytes(totalMemory)).append("\n");
        builder.append("  * Used: ").append(formatBytes(usedMemory)).append("\n");
        builder.append("  * Free: ").append(formatBytes(freeMemory)).append("\n");
        builder.append("  * Max: ").append(formatBytes(maxMemory)).append("\n");

        getLogger().info(builder.toString());
    }

    /**
     * Formats a byte count into a human-readable string.
     *
     * @param bytes The number of bytes to format
     * @return A formatted string (e.g., "64.0 MB")
     */
    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }

    /**
     * Displays information about the running bots.
     */
    private void displayBotInformation() {
        StringBuilder builder = new StringBuilder("Bot Information:\n");

        try {
            var activeBots = com.dianxin.tori.api.ToriProvider.getBotLoader().getActiveBots();

            if (activeBots.isEmpty()) {
                builder.append("- No active bots running\n");
            } else {
                builder.append("- Total active bots: ").append(activeBots.size()).append("\n");
                builder.append("- Bot details:\n");

                for (var bot : activeBots) {
                    var meta = bot.getMeta();
                    builder.append("  * ").append(meta.botName())
                           .append(" (v").append(meta.botVersion()).append(")")
                           .append(" by ").append(meta.botAuthor()).append("\n");

                    // Check if bot is connected to Discord
                    if (bot.getJda() != null) {
                        var jda = bot.getJda();
                        builder.append("    - Status: Connected\n");
                        builder.append("    - Guilds: ").append(jda.getGuilds().size()).append("\n");
                        builder.append("    - Users: ").append(jda.getUsers().size()).append("\n");
                    } else {
                        builder.append("    - Status: Disconnected\n");
                    }
                }
            }
        } catch (Exception e) {
            builder.append("- Error retrieving bot information: ").append(e.getMessage()).append("\n");
        }

        getLogger().info(builder.toString());
    }

    /**
     * Displays information about the current threads.
     */
    private void displayThreadInformation() {
        StringBuilder builder = new StringBuilder("Thread Information:\n");

        try {
            // Get thread information
            ThreadGroup rootGroup = Thread.currentThread().getThreadGroup();
            ThreadGroup parentGroup;
            while ((parentGroup = rootGroup.getParent()) != null) {
                rootGroup = parentGroup;
            }

            int activeCount = rootGroup.activeCount();
            int activeGroupCount = rootGroup.activeGroupCount();

            builder.append("- Active threads: ").append(activeCount).append("\n");
            builder.append("- Active thread groups: ").append(activeGroupCount).append("\n");

            // Get all threads
            Thread[] threads = new Thread[activeCount + 10]; // Add some buffer
            int actualCount = rootGroup.enumerate(threads, true);

            builder.append("- Thread details:\n");

            // Group threads by state
            java.util.Map<Thread.State, Integer> stateCount = new java.util.HashMap<>();
            java.util.Map<Thread.State, java.util.List<String>> stateThreads = new java.util.HashMap<>();

            for (int i = 0; i < actualCount; i++) {
                Thread thread = threads[i];
                if (thread != null) {
                    Thread.State state = thread.getState();
                    stateCount.put(state, stateCount.getOrDefault(state, 0) + 1);

                    String threadName = thread.getName();
                    stateThreads.computeIfAbsent(state, k -> new java.util.ArrayList<>()).add(threadName);
                }
            }

            // Display thread states
            for (Thread.State state : Thread.State.values()) {
                Integer count = stateCount.get(state);
                if (count != null) {
                    builder.append("  * ").append(state).append(": ").append(count).append(" threads\n");

                    // Show first few thread names for each state
                    java.util.List<String> threadNames = stateThreads.get(state);
                    if (threadNames != null && !threadNames.isEmpty()) {
                        int showCount = Math.min(3, threadNames.size());
                        for (int i = 0; i < showCount; i++) {
                            builder.append("    - ").append(threadNames.get(i)).append("\n");
                        }
                        if (threadNames.size() > showCount) {
                            builder.append("    - ... and ").append(threadNames.size() - showCount).append(" more\n");
                        }
                    }
                }
            }

        } catch (Exception e) {
            builder.append("- Error retrieving thread information: ").append(e.getMessage()).append("\n");
        }

        getLogger().info(builder.toString());
    }

    /**
     * Displays garbage collection statistics.
     */
    private void displayGCInformation() {
        StringBuilder builder = new StringBuilder("Garbage Collection Information:\n");

        try {
            // Get garbage collector MXBeans
            var gcBeans = java.lang.management.ManagementFactory.getGarbageCollectorMXBeans();

            if (gcBeans.isEmpty()) {
                builder.append("- No garbage collectors available\n");
            } else {
                builder.append("- Garbage Collectors:\n");

                for (var gcBean : gcBeans) {
                    builder.append("  * ").append(gcBean.getName()).append(":\n");
                    builder.append("    - Collection count: ").append(gcBean.getCollectionCount()).append("\n");
                    builder.append("    - Collection time: ").append(gcBean.getCollectionTime()).append(" ms\n");

                    // Calculate average collection time
                    long count = gcBean.getCollectionCount();
                    long time = gcBean.getCollectionTime();
                    if (count > 0) {
                        double avgTime = (double) time / count;
                        builder.append("    - Average collection time: ").append(String.format("%.2f", avgTime)).append(" ms\n");
                    }

                    // Get memory pool names
                    String[] poolNames = gcBean.getMemoryPoolNames();
                    if (poolNames.length > 0) {
                        builder.append("    - Memory pools: ").append(String.join(", ", poolNames)).append("\n");
                    }
                }

                // Overall GC statistics
                long totalCollections = gcBeans.stream().mapToLong(java.lang.management.GarbageCollectorMXBean::getCollectionCount).sum();
                long totalTime = gcBeans.stream().mapToLong(java.lang.management.GarbageCollectorMXBean::getCollectionTime).sum();

                builder.append("- Overall Statistics:\n");
                builder.append("  * Total collections: ").append(totalCollections).append("\n");
                builder.append("  * Total collection time: ").append(totalTime).append(" ms\n");

                if (totalCollections > 0) {
                    double avgTime = (double) totalTime / totalCollections;
                    builder.append("  * Average collection time: ").append(String.format("%.2f", avgTime)).append(" ms\n");
                }
            }

        } catch (Exception e) {
            builder.append("- Error retrieving garbage collection information: ").append(e.getMessage()).append("\n");
        }

        getLogger().info(builder.toString());
    }

    /**
     * Displays performance metrics.
     */
    private void displayPerformanceMetrics() {
        StringBuilder builder = new StringBuilder("Performance Metrics:\n");

        try {
            // Uptime
            long uptime = java.lang.management.ManagementFactory.getRuntimeMXBean().getUptime();
            builder.append("- Server uptime: ").append(formatUptime(uptime)).append("\n");

            // CPU information
            var osBean = java.lang.management.ManagementFactory.getOperatingSystemMXBean();
            builder.append("- CPU cores: ").append(osBean.getAvailableProcessors()).append("\n");

            // Try to get CPU load if available (may not be supported on all platforms)
            try {
                double systemLoad = osBean.getSystemLoadAverage();
                if (systemLoad >= 0) {
                    builder.append("- System CPU load: ").append(String.format("%.2f", systemLoad * 100)).append("%\n");
                }
            } catch (Exception e) {
                // CPU load not available on this platform
            }

            // Memory metrics
            Runtime runtime = Runtime.getRuntime();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long maxMemory = runtime.maxMemory();
            long usedMemory = totalMemory - freeMemory;

            double memoryUsagePercent = maxMemory > 0 ? (double) usedMemory / maxMemory * 100 : 0;

            builder.append("- Memory usage: ").append(String.format("%.1f", memoryUsagePercent)).append("%\n");
            builder.append("- Heap efficiency: ").append(formatBytes(usedMemory))
                   .append(" / ").append(formatBytes(maxMemory)).append("\n");

            // Class loading information
            var classLoadingBean = java.lang.management.ManagementFactory.getClassLoadingMXBean();
            builder.append("- Classes loaded: ").append(classLoadingBean.getLoadedClassCount()).append("\n");
            builder.append("- Classes unloaded: ").append(classLoadingBean.getUnloadedClassCount()).append("\n");
            builder.append("- Total classes loaded: ").append(classLoadingBean.getTotalLoadedClassCount()).append("\n");

            // Thread information
            var threadBean = java.lang.management.ManagementFactory.getThreadMXBean();
            builder.append("- Live threads: ").append(threadBean.getThreadCount()).append("\n");
            builder.append("- Peak threads: ").append(threadBean.getPeakThreadCount()).append("\n");
            builder.append("- Daemon threads: ").append(threadBean.getDaemonThreadCount()).append("\n");

            // Compilation time (JIT)
            var compilationBean = java.lang.management.ManagementFactory.getCompilationMXBean();
            if (compilationBean != null && compilationBean.isCompilationTimeMonitoringSupported()) {
                builder.append("- JIT compilation time: ").append(compilationBean.getTotalCompilationTime()).append(" ms\n");
            }

        } catch (Exception e) {
            builder.append("- Error retrieving performance metrics: ").append(e.getMessage()).append("\n");
        }

        getLogger().info(builder.toString());
    }

    /**
     * Formats uptime in milliseconds to a human-readable string.
     *
     * @param uptimeMillis The uptime in milliseconds
     * @return A formatted string (e.g., "2d 3h 45m 30s")
     */
    private String formatUptime(long uptimeMillis) {
        long seconds = uptimeMillis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return String.format("%dd %dh %dm %ds", days, hours % 24, minutes % 60, seconds % 60);
        } else if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes % 60, seconds % 60);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds % 60);
        } else {
            return String.format("%ds", seconds);
        }
    }
}
