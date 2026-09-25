package com.dianxin.tori.server;

import com.dianxin.tori.api.ToriProvider;
import com.dianxin.tori.api.base.Constants;
import com.dianxin.tori.api.config.ServerConfiguration;
import com.dianxin.tori.api.controller.VersionController;
import com.dianxin.tori.server.gui.ToriServerGui;
import com.dianxin.tori.server.logger.ConsoleMode;
import com.dianxin.tori.server.updater.UpdateChecker;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Instant;
import java.util.Arrays;

@SuppressWarnings("TrailingWhitespacesInTextBlock")
public class Main {
    public static final Instant BOOT_TIME = Instant.now(); // save when press start
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    private static Server server;
    private static ToriServerGui gui;

    public static void main(String[] args) {
        // Check if GUI should be enabled (skip if headless or if 'nogui' argument is passed)
        boolean nogui = Arrays.asList(args).contains("nogui") || GraphicsEnvironment.isHeadless();
        if (nogui) {
            System.setProperty("java.awt.headless", "true"); // force run as headless
        }

        System.setProperty("file.encoding", "UTF-8");
        System.setProperty("com.zaxxer.hikari.housekeeper.periodMs", "60000"); // todo - add, if available

        // Reduce the number of redundant Netty worker threads if the container only has 1-2 vCPUs.
        System.setProperty("io.netty.eventLoopThreads", String.valueOf(Math.max(2, Runtime.getRuntime().availableProcessors())));

        checkAndWarnJvmFlags();

        if (!nogui) {
            try {
                gui = new ToriServerGui();
                gui.setVisible(true);
            } catch (Exception e) {
                log.warn("Failed to initialize server GUI, falling back to console mode: {}", e.getMessage());
            }
        }

        System.setProperty("terminal.jline", "true");
        System.setProperty("org.jline.terminal.dumb", "true");
        System.setProperty("terminal.ansi", "true");

        log.info("Starting Tori server, please wait...");

        try {
            VersionController.checkCompatibilityOrThrow();
        } catch (UnsupportedOperationException e) {
            log.error("❌ {}", e.getMessage(), e);
            System.exit(-1);
            return;
        }

        ServerConfiguration config = ToriBootstrap.init();

        // new: load console mode
        log.info("Loading console mode via config, please wait...");
        applyConsoleMode(config.getConfig().getString("console.console-mode", "MODERN"));

        // check whether debug config section is enabled
        if(config.isDebug()) {
            Configurator.setRootLevel(Level.DEBUG);
            log.debug("⚙️ Debug mode enabled globally via config.yml!");
        }

        server = new Server(config);

        // Set ToriProvider immediately to avoid race conditions with bot initialization
        ToriProvider.setServer(server);

        // Check JDave compatibility with proper error handling
        try {
            if(ToriProvider.hasJDave()) {
                VersionController.checkJavaVersionForJDaveOrThrow();
                log.info("✅ JDave audio encryption is enabled. Java version check passed.");
            }
        } catch (UnsupportedOperationException | NoClassDefFoundError | UnsupportedClassVersionError e) {
            log.error("❌ JDave compatibility check failed: {}", e.getMessage(), e);
            System.exit(-1);
            return;
        }

        // Initialize bots AFTER ToriProvider is set and JDave check is complete
        server.initializeBots();

        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            String threadName = thread.getName();

            // Extract bot name from thread name (format: "Bot-{BotName}")
            String botName = null;
            if (threadName.startsWith("Bot-")) {
                botName = threadName.substring(4); // Remove "Bot-" prefix
            }

            // Handle specific exception types with targeted responses
            if (throwable instanceof NoClassDefFoundError || throwable.getCause() instanceof ClassNotFoundException) {
                if (botName != null) {
                    log.error("[BOT-DEPENDENCY-ERROR] Bot '{}' failed to load due to missing dependency: {}",
                            botName, throwable.getMessage());
                    log.error("Please ensure '{}' has all required dependencies in its JAR file or check for updates.", botName);
                } else {
                    log.error("[DEPENDENCY-ERROR] Thread '{}' crashed due to missing dependency: {}",
                            threadName, throwable.getMessage(), throwable);
                }
            } else if (throwable instanceof OutOfMemoryError) {
                log.error("[OUT-OF-MEMORY] Thread '{}' crashed due to insufficient memory. Consider increasing heap size.",
                        threadName, throwable);
            } else if (throwable instanceof StackOverflowError) {
                log.error("[STACK-OVERFLOW] Thread '{}' crashed due to infinite recursion or excessive stack usage.",
                        threadName, throwable);
            } else if (throwable instanceof IllegalStateException) {
                log.error("[ILLEGAL-STATE] Thread '{}' encountered an illegal state: {}",
                        threadName, throwable.getMessage(), throwable);
            } else if (throwable instanceof UnsupportedClassVersionError) {
                log.error("[UNSUPPORTED-JAVA-VERSION] Thread '{}' failed to load a class due to incompatible Java version: {}",
                        threadName, throwable.getMessage(), throwable);
            } else {
                // Default handling for other exceptions
                if (botName != null) {
                    log.error("[BOT-CRASH] Bot '{}' crashed unexpectedly in thread '{}'",
                            botName, threadName, throwable);
                } else {
                    log.error("[BACKGROUND-THREAD-CRASH] Thread '{}' crashed with unknown reason.",
                            threadName, throwable);
                }
            }

            // Optional: Attempt graceful shutdown for bot threads
            if (botName != null && server != null) {
                try {
                    log.warn("Attempting to notify server about bot '{}' failure...", botName);
                    // server.handleBotFailure(botName, throwable);
                } catch (Exception e) {
                    log.error("Failed to handle bot failure gracefully", e);
                }
            }
        });
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (gui != null) {
                SwingUtilities.invokeLater(() -> {
                    gui.setVisible(false);
                    gui.dispose();
                });
            }
            server.shutdown();
        }, "Tori-Shutdown-Thread"));

        log.info("Generating startup scripts...");
        // generateStartupScripts();

        log.info("Tori Server has been started in {} ms!", System.currentTimeMillis() - BOOT_TIME.toEpochMilli());
        log.info("Ready!");
        log.info("Using Tori server v{}", Constants.TORI_SERVER_VERSION);

        UpdateChecker.checkForUpdateAsync().queue(
                success -> {},
                error -> log.error("Error while checking for updates.", error)
        );
    }

    private static void applyConsoleMode(String modeConfig) {
        ConsoleMode mode = ConsoleMode.fromString(modeConfig);

        // assign to System Property and lets Log4j2 read
        System.setProperty("tori.console.pattern", mode.getPattern());

        // request Log4j2 to reload context if logger has initialized before
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        context.reconfigure();
    }

    public static Server getServer() {
        if(server == null) {
            throw new IllegalStateException("Tori server has not been initialized!");
        }
        return server;
    }

    private static void checkAndWarnJvmFlags() {
        java.lang.management.RuntimeMXBean runtimeMx = java.lang.management.ManagementFactory.getRuntimeMXBean();
        java.util.List<String> jvmArgs = runtimeMx.getInputArguments();

        boolean hasOptimizedGc = jvmArgs.stream().anyMatch(arg ->
                arg.contains("UseG1GC") || arg.contains("UseZGC"));

        if (!hasOptimizedGc) {
            log.warn("⚠️ Server is running without optimized GC flags!");
            log.warn("👉 For low-latency & hosting stability, launch with: java -XX:+UseG1GC -XX:MaxGCPauseMillis=20 -jar server.jar");
        }
    }

    @SuppressWarnings("unused")
    private static void generateStartupScripts() {
        String batContent = """
                @echo off
                title Tori Server - Script
                cls
                
                :: JVM Flags tối ưu cho Discord Bot: Giảm GC Pause time, tối ưu Heap và String
                set JVM_FLAGS=-Xms128M -Xmx1024M -XX:+UseG1GC -XX:MaxGCPauseMillis=50 -XX:+UseStringDeduplication -XX:+OptimizeStringConcat
                
                :loop
                echo [Tori Bootstrapper] Starting Tori Server with JVM optimizations...
                java %JVM_FLAGS% -jar server.jar
                set EXIT_CODE=%ERRORLEVEL%
                
                echo.
                echo [Tori Bootstrapper] Server exited with code %EXIT_CODE%
                echo.
                
                if %EXIT_CODE% equ 42 (
                    echo [Tori Bootstrapper] Restart signal received (Code 42).
                    echo [Tori Bootstrapper] Rebooting server in 3 seconds...
                    timeout /t 3 >nul
                    cls
                    goto loop
                )
                
                echo [Tori Bootstrapper] Server stopped permanently (Normal exit).
                pause
                """;

        String shContent = """
                #!/bin/bash
                
                # Clear terminal screen
                clear
                
                # JVM Flags dành cho Linux Container / VPS
                # - Dùng MaxRAMPercentage để tự co giãn theo giới hạn RAM của container mà không bị OOMKilled
                # - Bật headless triệt để để không khởi tạo tài nguyên đồ họa Swing
                # - G1GC với độ trễ cực thấp (20ms) giúp ngăn triệt để 'Thread starvation or clock leap'
                JVM_FLAGS="-Xms128M -XX:MaxRAMPercentage=75.0 \
                -XX:+UseG1GC \
                -XX:MaxGCPauseMillis=20 \
                -XX:+UseStringDeduplication \
                -XX:+ExitOnOutOfMemoryError \
                -Djava.awt.headless=true \
                -Dfile.encoding=UTF-8"
                
                while true; do
                    echo "[Tori Bootstrapper] Starting Tori Server on Linux Container..."
                    
                    java $JVM_FLAGS -jar server.jar nogui
                    EXIT_CODE=$?
                    
                    echo ""
                    echo "[Tori Bootstrapper] Server exited with code $EXIT_CODE"
                    echo ""
                    
                    if [ $EXIT_CODE -eq 42 ]; then
                        echo "[Tori Bootstrapper] Restart signal received (Code 42)."
                        echo "[Tori Bootstrapper] Rebooting server in 3 seconds..."
                        sleep 3
                        clear
                    else
                        echo "[Tori Bootstrapper] Server stopped permanently (Normal exit)."
                        break
                    fi
                done
                """;

        File batFile = new File("start.cmd");
        File shFile = new File("start.sh");

        try {
            if (!batFile.exists()) {
                Files.writeString(batFile.toPath(), batContent, StandardCharsets.UTF_8);
                log.info("📝 Created startup script for Windows: start.cmd");
            }

            if (!shFile.exists()) {
                Files.writeString(shFile.toPath(), shContent, StandardCharsets.UTF_8);

                if (shFile.setExecutable(true, false)) {
                    log.info("📝 Created executable startup script for Linux/Mac: start.sh");
                } else {
                    log.info("📝 Created startup script for Linux/Mac: start.sh (Please run 'chmod +x start.sh' manually)");
                }
            }
        } catch (IOException e) {
            log.error("⚠️ Failed to generate startup scripts!", e);
        }
    }
}