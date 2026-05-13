package com.dianxin.tori.server;

import com.dianxin.core.api.console.commands.ConsoleCommandManager;
import com.dianxin.core.api.lifecycle.ExecutorManager;
import com.dianxin.core.api.v2.scheduler.Scheduler;
import com.dianxin.core.api.v2.scheduler.SchedulerImpl;
import com.dianxin.tori.api.ToriServer;
import com.dianxin.tori.api.bot.IBotLoader;
import com.dianxin.tori.api.config.ServerConfiguration;
import com.dianxin.tori.server.bot.BotLoader;
import com.dianxin.tori.server.commands.console.*;
import com.dianxin.tori.server.config.MainServerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

@SuppressWarnings({"unused", "FieldMayBeFinal", "UnusedAssignment"})
public class Server implements ToriServer {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    private ServerConfiguration serverConfiguration;
    private ConsoleCommandManager consoleCommandManager;
    private Scheduler scheduler;
    private IBotLoader botLoader;
    private boolean hasJDave = false;

    private static boolean isRunning = false;

    public Server(ServerConfiguration cf) {
        if(isRunning) {
            throw new IllegalStateException("Server is already running!");
        }

        isRunning = true;

        ExecutorManager.initialize(); // initialize all executors variables that can run async tasks on IAction, ResultedAction...

        this.serverConfiguration = cf;
        this.initConsoleCommandManager();
        this.scheduler = new SchedulerImpl();
        this.botLoader = new BotLoader();
        this.hasJDave = false; // Default to false, will be checked in Main
    }

    /**
     * Initializes JDave extension if available.
     * This method should be called AFTER Server construction to avoid blocking initialization.
     */
    public void initializeJDave() {
        try {
            Class.forName("club.minnced.discord.jdave.interop.JDaveSessionFactory");
            this.hasJDave = true;
            logger.info("✅ JDave Audio Encryption extension is loaded.");
        } catch (ClassNotFoundException e) {
            this.hasJDave = false;
            logger.info("ℹ️ Running standard version (JDave is not present).");
        } catch (UnsupportedClassVersionError | NoClassDefFoundError e) {
            this.hasJDave = false;
            logger.warn("⚠️ JDave is present but cannot be loaded due to version mismatch: {}", e.getMessage());
            throw e; // Re-throw to be caught in Main
        }
    }

    public void shutdown() {
        if (!isRunning) return;
        isRunning = false;

        logger.info("Stopping server...");

        botLoader.shutdownAll();
        ExecutorManager.shutdown();
        scheduler.shutdown();

        logger.info("Done! Good bye!");
    }

    /**
     * Initializes and loads all bot files from the bots folder.
     * This method should be called after {@link com.dianxin.tori.api.ToriProvider#setServer(ToriServer)}
     * to ensure bots can access the ToriProvider without race conditions.
     */
    public void initializeBots() {
        try {
            logger.info("Initializing bot loading system...");
            this.botLoader.loadBots();
        } catch (Exception e) {
            logger.error("An error occurred while loading bots from the 'bots' folder!", e);
        }
    }

    private void initConfig() {
        File configFile = new File("config.yml");
        if (!configFile.exists()) {
            try (InputStream in = getClass().getClassLoader().getResourceAsStream("config.yml")) {
                if (in == null) {
                    logger.error("Cannot find default config.yml in resources folder!");
                } else {
                    Files.copy(in, configFile.toPath(), StandardCopyOption.REPLACE_EXISTING); // copy
                    System.out.println("Save default config successful.");
                }
            } catch (IOException e) {
                logger.error("Cannot copy default config.yml into root path!", e);
            }
        }

        try {
            this.serverConfiguration = new MainServerConfiguration(configFile);
            logger.info("Load server config successful!");
        } catch (IOException e) {
            logger.error("An error occured when trying to load file config!", e);
        }
    }

    private void initConsoleCommandManager() {
        this.consoleCommandManager = new ConsoleCommandManager();

        this.consoleCommandManager.register(new HelpConsoleCommand());
        this.consoleCommandManager.register(new ServerInfoConsoleCommand());
        this.consoleCommandManager.register(new StopConsoleCommand());
        this.consoleCommandManager.register(new BotsConsoleCommand());
        this.consoleCommandManager.register(new PingConsoleCommand());
        this.consoleCommandManager.register(new EnableBotConsoleCommand());
        this.consoleCommandManager.register(new DisableBotConsoleCommand());
        this.consoleCommandManager.register(new DebugConsoleCommand());

        this.consoleCommandManager.startListening();
    }

    @Override
    public ServerConfiguration getConfig() {
        return this.serverConfiguration;
    }

    @Override
    public ConsoleCommandManager getConsoleCommandManager() {
        return this.consoleCommandManager;
    }

    @Override
    public Scheduler getScheduler() {
        return this.scheduler;
    }

    @Override
    public IBotLoader getBotLoader() {
        return this.botLoader;
    }

    @Override
    public boolean hasJDave() {
        return this.hasJDave;
    }
}
