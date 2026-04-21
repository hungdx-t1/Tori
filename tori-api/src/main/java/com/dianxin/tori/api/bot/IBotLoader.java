package com.dianxin.tori.api.bot;

import java.util.List;

/**
 * An interface responsible for the lifecycle management and execution of bot files
 * within the Tori Server environment.
 */
public interface IBotLoader {

    /**
     * Scans the designated bots directory, reads their metadata, initializes their
     * class loaders, and starts the bots concurrently.
     *
     * @throws Exception If a critical error occurs during the file reading or class loading process.
     */
    void loadBots() throws Exception;

    /**
     * Retrieves a list of all bot instances that are currently running on the server.
     *
     * @return An unmodifiable list of active {@link JavaDiscordBot} instances.
     */
    List<JavaDiscordBot> getActiveBots();

    /**
     * Gracefully shuts down all currently active bots.
     * <p>
     * This method triggers the {@code onShutdown()} lifecycle event for each bot,
     * allowing them to safely disconnect their JDA instances and save necessary data.
     */
    void shutdownAll();
}