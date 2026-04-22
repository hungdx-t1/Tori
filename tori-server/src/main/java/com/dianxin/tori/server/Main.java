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

        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) ->
                log.error("[BACKGROUND-THREAD-CRASH] Thread '{}' is crashed with unknown reason.", thread.getName(), throwable));
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