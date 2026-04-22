package com.dianxin.tori.api;

import com.dianxin.core.api.console.commands.ConsoleCommandManager;
import com.dianxin.core.api.v2.scheduler.Scheduler;
import com.dianxin.tori.api.bot.IBotLoader;
import com.dianxin.tori.api.config.ServerConfiguration;

/**
 * Represents the core foundation of the Tori Server.
 * This interface provides access to the primary managers and systems
 * required to run and manage the multi-bot environment.
 */
@SuppressWarnings("unused")
public interface ToriServer {

    /**
     * Gets the configuration settings of the server.
     *
     * @return The {@link ServerConfiguration} containing the properties loaded from the configuration file.
     */
    ServerConfiguration getConfig();

    /**
     * Gets the console command manager.
     * <p>
     * This manager is used to register, unregister, and handle commands
     * typed directly into the server's terminal/console.
     *
     * @return The {@link ConsoleCommandManager} instance.
     */
    ConsoleCommandManager getConsoleCommandManager();

    /**
     * Gets the server's task scheduler.
     * <p>
     * The scheduler is responsible for executing asynchronous, delayed,
     * or repeating tasks safely within the server's lifecycle.
     *
     * @return The {@link Scheduler} instance.
     */
    Scheduler getScheduler();

    /**
     * Gets the bot loader system.
     * <p>
     * The bot loader handles the discovery, initialization, and lifecycle
     * management (loading/shutting down) of all external bot JAR files.
     *
     * @return The {@link IBotLoader} instance.
     */
    IBotLoader getBotLoader();
}