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

@SuppressWarnings({"unused", "FieldMayBeFinal"})
public class Server implements ToriServer {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    private ServerConfiguration serverConfiguration;
    private ConsoleCommandManager consoleCommandManager;
    private Scheduler scheduler;
    private IBotLoader botLoader;

    private boolean isRunning = false;

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

        try {
            this.botLoader.loadBots();
        } catch (Exception e) {
            logger.error("An error occured while trying to loading bot on `bots` folder!", e);
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
}
