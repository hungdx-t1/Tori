package com.dianxin.tori.api;

import com.dianxin.core.api.console.commands.ConsoleCommandManager;
import com.dianxin.core.api.v2.scheduler.Scheduler;
import com.dianxin.tori.api.bot.IBotLoader;
import com.dianxin.tori.api.config.ServerConfiguration;

/**
 * A global provider and registry for the {@link ToriServer} instance.
 * This class acts as a central access point for plugins or bots to interact
 * with the core server functionalities without needing direct dependency injection.
 */
@SuppressWarnings("unused")
public final class ToriProvider {
    private static ToriServer serverInstance;

    /**
     * Sets the global instance of the ToriServer.
     * <p>
     * This method is intended to be called only once during the server's bootstrap
     * or startup phase. Subsequent calls will result in an exception to prevent
     * the core server instance from being overwritten.
     *
     * @param instance The initialized {@link ToriServer} instance.
     * @throws UnsupportedOperationException if the server instance has already been set.
     */
    public static void setServer(ToriServer instance) {
        if (serverInstance != null) {
            throw new UnsupportedOperationException("ToriServer has already been set!");
        }
        serverInstance = instance;
    }

    private static ToriServer get() {
        if (serverInstance == null) {
            throw new IllegalStateException("ToriServer is not initialized!");
        }
        return serverInstance;
    }

    /**
     * Retrieves the global configuration of the server.
     *
     * @return The {@link ServerConfiguration} instance containing server settings.
     * @throws IllegalStateException if the server has not been initialized yet.
     */
    public static ServerConfiguration getConfig() {
        return get().getConfig();
    }

    /**
     * Retrieves the manager responsible for handling terminal console commands.
     *
     * @return The {@link ConsoleCommandManager} instance.
     * @throws IllegalStateException if the server has not been initialized yet.
     */
    public static ConsoleCommandManager getConsoleCommandManager() {
        return get().getConsoleCommandManager();
    }
    
    /**
     * Retrieves the scheduler responsible for managing timed and scheduled tasks.
     *
     * @return The {@link Scheduler} instance.
     * @throws IllegalStateException if the server has not been initialized yet.
     */
    public static Scheduler getScheduler() {
        return get().getScheduler();
    }

    /**
     * Retrieves the bot loader responsible for loading and managing bots.
     *
     * @return The {@link IBotLoader} instance.
     * @throws IllegalStateException if the server has not been initialized yet.
     */
    public static IBotLoader getBotLoader() {
        return get().getBotLoader();
    }
}
