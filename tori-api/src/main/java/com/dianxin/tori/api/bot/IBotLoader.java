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

    /**
     * Dynamically loads and starts a bot from a JAR file at runtime.
     * <p>
     * This method finds the specified JAR file in the bots directory, reads its metadata,
     * initializes its class loader, and starts the bot in a separate thread.
     *
     * @param jarFileName The name of the bot JAR file (e.g., "MyBot.jar")
     * @return {@code true} if the bot was successfully enabled, {@code false} otherwise.
     */
    boolean enableBot(String jarFileName);

    /**
     * Gracefully shuts down and removes a bot from the active bots list.
     * <p>
     * This method finds the bot by name, calls its {@code onShutdown()} lifecycle event,
     * and removes it from the list of active bots.
     *
     * @param botName The name of the bot to disable (matches the 'name' field in bot.yml)
     * @return {@code true} if the bot was successfully disabled, {@code false} if not found.
     */
    boolean disableBot(String botName);
}