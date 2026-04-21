package com.dianxin.tori.api.bot;

import com.dianxin.core.api.console.commands.ConsoleCommandManager;
import com.dianxin.tori.api.controller.VersionController;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.audio.AudioModuleConfig;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.jetbrains.annotations.NotNull;
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
@SuppressWarnings({"unused", "EmptyMethod"})
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

        // Khởi tạo và tạo thư mục NGAY LẬP TỨC khi bot được nạp meta
        // Thư mục sẽ có dạng: plugins/bots/BotName
        this.dataFolder = new File("bots", meta.botName());
        if (!this.dataFolder.exists()) {
            this.dataFolder.mkdirs();
        }
    }

    protected abstract String getBotToken();

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

        this.jda = jdaBuilder
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .build()
                .awaitReady();

        logger.info("Bot {} initialize successfully.", meta.botName());
        logger.info("Link invite bot: {}", jda.getInviteUrl());

        // Register custom console commands
        registerConsoleCommands();

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
        jda.getPresence().setStatus(OnlineStatus.OFFLINE);
        jda.shutdown();
    }

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
    protected void registerConsoleCommands() {
        // Bot subclasses override
    }

    /**
     * The JDA instance representing the bot connection.
     */
    public JDA getJda() {
        return jda;
    }

    public User getSelf() {
        return jda.getSelfUser();
    }

    public String getBotInviteLink() {
        return jda.getInviteUrl();
    }

    public String getBotInviteLink(Permission... permissions) {
        return jda.getInviteUrl(permissions);
    }

    /**
     * @return Meta of the bot
     */
    public @NotNull IBotMeta getMeta() {
        return meta;
    }

    /**
     * Logger instance for this bot class.
     */
    protected Logger getLogger() {
        return logger;
    }
}
