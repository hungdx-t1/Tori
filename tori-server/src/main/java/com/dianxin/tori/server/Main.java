package com.dianxin.tori.server;

import com.dianxin.tori.api.ToriProvider;
import com.dianxin.tori.api.base.Constants;
import com.dianxin.tori.api.config.ServerConfiguration;
import com.dianxin.tori.api.controller.VersionController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

public class Main {
    public static final Instant BOOT_TIME = Instant.now(); // save when press start
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    private static Server server;

    public static void main(String[] args) {
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
        server = new Server(config);

        ToriProvider.setServer(server);

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
        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdown, "Tori-Shutdown-Thread"));
        log.info("Tori Server has been started in {} ms!", System.currentTimeMillis() - BOOT_TIME.toEpochMilli());
        log.info("Ready!");
        log.info("Using Tori server v{}", Constants.TORI_SERVER_VERSION);
    }

    public static Server getServer() {
        if(server == null) {
            throw new IllegalStateException("Tori server has not been initialized!");
        }
        return server;
    }
}