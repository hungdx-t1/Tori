package com.dianxin.tori.api.bot;

import com.dianxin.tori.api.ToriProvider;
import com.dianxin.tori.base.annotations.ReleasedSince;
import com.dianxin.tori.base.console.commands.ConsoleCommandManager;
import com.dianxin.tori.api.controller.VersionController;
import com.dianxin.tori.base.env.VirtualEnvironmentConfiguration;
import com.dianxin.tori.base.scheduler.Scheduler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.audio.AudioModuleConfig;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.EnumSet;

/**
 * Represents a base class for creating Discord bots using JDA with an extended
 * lifecycle system, console command support, and customizable startup options.
 * <p>
 * Users of this API should extend this class and override the desired methods such as:
 * <ul>
 *     <li>{@link #onEnable()} - Called after the bot finishes starting.</li>
 *     <li>{@link #onDisable()} - Called before the bot shuts down.</li>
 *     <li>{@link #registerConsoleCommands()} - Register custom console commands.</li>
 *     <li>{@link #getIntents()} - Provide required {@link GatewayIntent} values.</li>
 *     <li>{@link #getActivity()} - Define the bot presence/activity.</li>
 * </ul>
 *
 * <p>
 * The class also automatically manages:
 * <ul>
 *     <li>JDA initialization and shutdown</li>
 *     <li>Console command listener</li>
 *     <li>Presence and intents setup</li>
 *     <li>Basic logging flow</li>
 * </ul>
 *
 * Example usage:
 * <pre>{@code
 * public class MyBot extends JavaDiscordBot {
 *     public MyBot() {
 *         super("YOUR_TOKEN", "MyBot");
 *     }
 *
 *     @Override
 *     public void onEnable() {
 *         getLogger().info("MyBot is ready!");
 *     }
 *
 *     @Override
 *     protected EnumSet<GatewayIntent> getIntents() {
 *         return IntentContext.getDefaultIntents();
 *     }
 *
 *     @Override
 *     protected Activity getActivity() {
 *         return Activity.playing("Hello world!");
 *     }
 * }
 * }</pre>
 */
@SuppressWarnings({"unused", "EmptyMethod", "SameReturnValue"})
public abstract class JavaDiscordBot {
    private JDA jda;
    private IBotMeta meta;
    private final Logger logger;
    private volatile boolean started = false;

    private File dataFolder;

    public JavaDiscordBot() {
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    final void internalSetMeta(IBotMeta meta) {
        this.meta = meta;

        // format /bots/BotName
        this.dataFolder = new File("bots", meta.botName());
        if (!this.dataFolder.exists()) {
            this.dataFolder.mkdirs();
        }
    }

    /**
     * Provides the bot token required for authentication with Discord.
     *
     * @return The bot token as a string.
     */
    protected abstract String getBotToken();

    /**
     * Creates and configures the JDABuilder with the bot's intents, activity, and audio module config.
     * <p>
     * Subclasses can override this method to further customize the JDABuilder.
     *
     * @return The configured JDABuilder instance.
     */
    protected JDABuilder builder() {
        JDABuilder jdaBuilder;
        EnumSet<GatewayIntent> intents = getIntents();
        Activity activity = getActivity();
        AudioModuleConfig audioModuleConfig = getAudioModuleConfig();

        String botToken = getBotToken();

        if(intents == null) {
            jdaBuilder = JDABuilder.createDefault(botToken);
        } else {
            jdaBuilder = JDABuilder.createDefault(botToken, intents);
        }

        if(activity != null) {
            jdaBuilder.setActivity(activity);
        }

        if(audioModuleConfig != null) {
            jdaBuilder.setAudioModuleConfig(audioModuleConfig);
        }

        return jdaBuilder;
    }

    /**
     * Starts the bot, initializes JDA, loads intents/activity,
     * registers console commands, and begins console listening.
     *
     * <p>This method blocks until the JDA session is ready.
     *
     * @throws InterruptedException If the current thread is interrupted.
     */
    public synchronized void start() throws InterruptedException {
        long startTime = System.currentTimeMillis();

        if (started) {
            throw new IllegalStateException("Bot is already started!");
        }

        started = true;

        VersionController.checkJDACompatibilityOrThrow();

        JDABuilder jdaBuilder = this.builder();
        this.jda = jdaBuilder
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .build()
                .awaitReady();

        logger.info("Bot {} initialize successfully.", meta.botName());
        logger.info("Link invite bot: {}", jda.getInviteUrl());

        onEnable();

        long elapsedMillis = System.currentTimeMillis() - startTime;

        // Display format (ex: "1250 ms" or "1.25 s")
        if (elapsedMillis < 1000) {
            logger.info("ℹ️ Bot enabled on {} ms", elapsedMillis);
        } else {
            double elapsedSeconds = elapsedMillis / 1000.0;
            logger.info("ℹ️ Bot enabled on {} s", String.format("%.2f", elapsedSeconds));
        }
    }

    /**
     * Called when the bot fully starts and is ready.
     * Override to initialize listeners, commands, database, etc.
     */
    protected void onEnable() { }

    /**
     * Called before the bot shuts down.
     * Override to close resources or save data.
     */
    protected void onDisable() { }

    /**
     * Gracefully shuts down the bot, updating its status to offline,
     * calling {@link #onDisable()}, and closing the JDA connection.
     */
    public void onShutdown() {
        onDisable();
        logger.info("⏹ Shutting down bot {}...", meta.botName());

        // 26.6.101 patch: prevent NullPointerException on shutdown when bot has an error when loading
        if(jda != null) {
            jda.getPresence().setStatus(OnlineStatus.OFFLINE);
            jda.shutdown();
        }
    }

    /**
     * Gets the data storage directory specific to this bot.
     *
     * @return A {@link File} object representing the bot's data folder.
     * @throws IllegalStateException If called before the bot metadata is fully initialized (e.g., inside the constructor).
     */
    public File getDataFolder() {
        if (this.dataFolder == null) {
            // prevent dev call on constructor (when core is not load BotMeta completely)
            throw new IllegalStateException("Cannot call getDataFolder() when BotMeta is not initialized! Must call in onEnable().");
        }
        return this.dataFolder;
    }

    /**
     * Provides the list of gateway intents required for the bot.
     *
     * @return An {@link EnumSet} of intents or {@code null} to use default JDA behavior.
     */
    protected EnumSet<GatewayIntent> getIntents() {
        return null;
    }

    /**
     * Defines the bot's presence/activity shown on Discord.
     *
     * @return A {@link Activity} instance or {@code null} for no activity.
     */
    protected Activity getActivity() {
        return null;
    }

    /**
     * Provides dave session if available.
     *
     * @return A AudioModuleConfig.
     */
    protected AudioModuleConfig getAudioModuleConfig() { return null; }

    /**
     * Override this to register custom console commands using {@link ConsoleCommandManager}.
     */
    @Deprecated
    protected void registerConsoleCommands() {
        // Bot subclasses override
    }

    /**
     * Gets the JDA instance representing the active bot connection.
     *
     * @return The active {@link JDA} instance.
     */
    public JDA getJda() {
        return jda;
    }

    /**
     * Gets the self-user profile of the bot on Discord.
     *
     * @return The bot's {@link User} entity.
     */
    public User getSelf() {
        return jda.getSelfUser();
    }

    /**
     * Generates a default bot invitation link with no pre-defined permissions.
     *
     * @return A string containing the bot invite URL.
     */
    public String getBotInviteLink() {
        return jda.getInviteUrl();
    }

    /**
     * Generates a bot invitation link pre-configured with specific permissions.
     *
     * @param permissions An array of {@link Permission} flags to request upon invitation.
     * @return A string containing the bot invite URL with permissions.
     */
    public String getBotInviteLink(Permission... permissions) {
        return jda.getInviteUrl(permissions);
    }

    /**
     * Retrieves an environment configuration value by its variable name.
     *
     * @param variable The name of the environment variable.
     * @return The string value of the environment variable, or {@code null} if not found.
     */
    @Nullable
    @ReleasedSince("26.10.0")
    public String getEnv(String variable) {
        return VirtualEnvironmentConfiguration.get(variable);
    }

    /**
     * Retrieves an environment configuration value by its variable name, throwing an exception if missing.
     *
     * @param variable The name of the environment variable.
     * @return The string value of the environment variable.
     * @throws RuntimeException If the variable is not defined.
     */
    @NonNull
    @ReleasedSince("26.10.0")
    public String getEnvOrThrow(String variable) {
        return VirtualEnvironmentConfiguration.getOrThrow(variable);
    }

    /**
     * Retrieves an environment configuration value, returning a fallback default if not found.
     *
     * @param variable     The name of the environment variable.
     * @param defaultValue The fallback value to return if the variable is absent.
     * @return The configuration value or the default value.
     */
    @NonNull
    @ReleasedSince("26.10.0")
    public String getEnvOrDefault(String variable, String defaultValue) {
        return VirtualEnvironmentConfiguration.getOrDefault(variable, defaultValue);
    }

    /**
     * Retrieves an environment configuration value, throwing a custom throwable if missing.
     *
     * @param variable   The name of the environment variable.
     * @param throwable  The throwable to be thrown if the variable is absent.
     * @return The string value of the environment variable.
     * @throws Throwable If the variable is not found.
     */
    @NonNull
    @ReleasedSince("26.10.0")
    public String getEnvOrThrow(String variable, Throwable throwable) throws Throwable {
        return VirtualEnvironmentConfiguration.getOrThrow(variable, throwable);
    }

    /**
     * Gets the metadata container associated with this bot.
     *
     * @return The {@link IBotMeta} instance containing bot information.
     */
    public @NonNull IBotMeta getMeta() {
        return meta;
    }

    /**
     * Gets the logger instance dedicated to this bot class.
     *
     * @return The SLF4J {@link Logger} instance.
     */
    @NonNull
    protected Logger getLogger() {
        return logger;
    }

    /**
     * Gets the global task scheduler provided by the Tori environment.
     *
     * @return The active {@link Scheduler} instance.
     */
    @NonNull
    public final Scheduler getScheduler() {
        return ToriProvider.getScheduler();
    }
}
