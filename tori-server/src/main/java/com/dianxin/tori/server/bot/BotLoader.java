package com.dianxin.tori.server.bot;

import com.dianxin.tori.api.bot.IBotLoader;
import com.dianxin.tori.api.bot.IBotMeta;
import com.dianxin.tori.api.bot.JavaDiscordBot;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.NoSuchFileException;
import java.util.*;

@SuppressWarnings({"unused", "ResultOfMethodCallIgnored"})
public class BotLoader implements IBotLoader {
    private final Logger logger = LoggerFactory.getLogger(BotLoader.class);
    private final List<JavaDiscordBot> activeBots = new ArrayList<>();

    public void loadBots() throws Exception {
        File botsFolder = new File("bots");
        if (!botsFolder.exists()) botsFolder.mkdirs();

        File[] jarFiles = botsFolder.listFiles((dir, name) -> name.endsWith(".jar"));
        if (jarFiles == null || jarFiles.length == 0) {
            logger.warn("This server has no file bot in folder 'bots/'.");
            return;
        }

        for (File jar : jarFiles) {
            logger.info("⏳ Loading bot file {} from startup...", jar.getName());

            // init ClassLoader for specific bot and turning on
            URL jarUrl = jar.toURI().toURL();
            URLClassLoader botClassLoader = new URLClassLoader(
                    new URL[]{jarUrl},
                    this.getClass().getClassLoader()
            );

            try {
                // get meta
                IBotMeta meta = getBotMetaFromJarFile(botClassLoader, jar);
                logger.info("🚀 Initializing bot: {} (v{}) by {}", meta.botName(), meta.botVersion(), meta.botAuthor());

                // use Reflection to find Class and initialize Object
                Class<?> mainClass = Class.forName(meta.mainClassPath(), true, botClassLoader);

                if (!JavaDiscordBot.class.isAssignableFrom(mainClass)) {
                    logger.error("❌ Class '{}' is not inherit JavaDiscordBot!", meta.mainClassPath());
                    continue;
                }

                // call constructor
                JavaDiscordBot botInstance = (JavaDiscordBot) mainClass.getDeclaredConstructor().newInstance();
                // botInstance.internalSetMeta(meta); // cannot access package-private
                // use Reflection to bypass package-private and set Meta
                java.lang.reflect.Method setMetaMethod = JavaDiscordBot.class.getDeclaredMethod("internalSetMeta", IBotMeta.class);
                setMetaMethod.setAccessible(true); // bypass
                setMetaMethod.invoke(botInstance, meta); // inject meta

                // run bot on seperate thread
                new Thread(() -> startBotSafely(botInstance, meta), "Bot-" + meta.botName()).start();

            } catch (NoSuchFileException e) {
                logger.error("⚠️ Cannot run bot {} because file bot.yml is not exist!", jar.getName(), e);
            } catch (UnsupportedClassVersionError error) {
                logger.error("❌ Cannot load this bot. This bot is compiled with a newer version of Java: {}", jar.getName(), error);
            } catch (Throwable t) {
                logger.error("❌ Couldn't load bot {}. Is it up to date?", jar.getName(), t);
            }
        }
    }

    /**
     * Method read bot.yml with URLClassLoader is streamed to reuse
     */
    private IBotMeta getBotMetaFromJarFile(URLClassLoader childLoader, File jarFile) throws IOException {
        // find file bot.yml in file jar
        URL yamlUrl = childLoader.findResource("bot.yml");
        if (yamlUrl == null) {
            throw new NoSuchFileException("bot.yml not found on file: " + jarFile.getName());
        }

        // read file bot.yml
        try (InputStream is = yamlUrl.openStream()) {
            Yaml yaml = new Yaml();
            Map<String, Object> config = yaml.load(is);

            String botName = String.valueOf(config.get("name"));
            String desc = config.get("description") != null ? String.valueOf(config.get("description")) : "";
            String version = config.get("version") != null ? String.valueOf(config.get("version")) : "unknown-version";
            String author = String.valueOf(config.get("author"));

            List<String> contributors = new ArrayList<>();
            Object rawContribs = config.get("contributors");
            if (rawContribs instanceof List) {
                // if dev write like list yaml
                for (Object o : (List<?>) rawContribs) contributors.add(String.valueOf(o));
            } else if (rawContribs instanceof String str) {
                // if dev write like string split by comma (,)
                contributors.addAll(Arrays.asList(str.split(",\\s*")));
            }

            @Nullable Object mainClassPathSection = config.get("main");
            if(mainClassPathSection == null) {
                throw new NoSuchFileException("Main class path field ('main') is not specified");
            }
            String mainClassPath = String.valueOf(mainClassPathSection);

            @Nullable Object botWebsiteSection = config.get("website");
            String botWebsite = String.valueOf(botWebsiteSection);

            @Nullable Object ownerIdSection = config.get("ownerId");
            if(ownerIdSection == null) {
                throw new IllegalStateException("Owner ID field ('ownerId') is not specified");
            }
            String ownerId = String.valueOf(ownerIdSection);

            if(botName == null) {
                throw new IllegalStateException("Name field ('name') is not specified");
            }

            if(author == null) {
                throw new IllegalStateException("Author field ('author') is not specified");
            }

            if(mainClassPath == null) {
                throw new IllegalStateException("Main class path field ('main') is not specified");
            }

            return new BotMeta(
                    botName,
                    desc == null ? "" : desc,
                    version == null ? "unknown-version" : version,
                    author,
                    contributors.isEmpty() ? List.of() : contributors,
                    mainClassPath,
                    botWebsite,
                    ownerId
            );
        }
    }

    /**
     * Safely starts a bot instance with proper error handling and cleanup.
     * @param botInstance The bot to start
     * @param meta The bot's metadata
     */
    private void startBotSafely(JavaDiscordBot botInstance, IBotMeta meta) {
        try {
            botInstance.start();
            synchronized (activeBots) {
                activeBots.add(botInstance);
            }
            logger.info("✅ Bot {} started successfully!", meta.botName());
        } catch (Throwable t) {
            logger.error("❌ Failed to start bot {}. Is it up to date?", meta.botName(), t);
            shutdownBotGracefully(botInstance, meta);
        }
    }

    /**
     * Attempts to shutdown a bot gracefully with comprehensive error handling and multiple fallback mechanisms.
     * Handles all types of throwables that might occur during shutdown process.
     * @param botInstance The bot to shutdown
     * @param meta The bot's metadata
     */
    private void shutdownBotGracefully(JavaDiscordBot botInstance, IBotMeta meta) {
        String botName = meta.botName();
        boolean jdaShutdownSuccess = false;

        // Phase 1: Try graceful shutdown via onShutdown()
        try {
            logger.debug("Attempting graceful shutdown for bot {}", botName);
            botInstance.onShutdown();
            logger.info("✅ Bot {} shut down gracefully (Phase 1)", botName);
            // return; // Success, no need for further attempts -> removed when try to disable bot completely
        } catch (NoClassDefFoundError classError) {
            logger.warn("⚠️ Bot {} JAR file was likely deleted or modified while running! Skipping graceful logic.", botName, classError);
        } catch (Throwable t) {
            logger.warn("❌ Graceful shutdown failed for bot {}: {}", botName, t.getMessage());
            // Continue to force shutdown attempts
        }

        // Phase 2: Force shutdown via JDA (if available)
        try {
            logger.debug("Attempting force shutdown via JDA for bot {}", botName);
            if (botInstance.getJda() != null) {
                botInstance.getJda().shutdownNow();
                jdaShutdownSuccess = true;
                logger.info("✅ Bot {} force shut down via JDA successful", botName);
                // return; // Success
            } else {
                logger.debug("JDA instance is null for bot {}, skipping JDA shutdown", botName);
            }
        } catch (Throwable t) {
            logger.warn("❌ Force shutdown via JDA failed for bot {}: {}", botName, t.getMessage());
            // Continue to final cleanup attempts
        }

        /*  --- Comment since 26.5.161: Remove emergency cleanup to avoid potential issues with reflection and class loading in unstable bot states ---

        // Phase 3: Emergency cleanup - try to access JDA directly if possible
        try {
            logger.debug("Attempting emergency cleanup for bot {}", botName);
            // Use reflection to access JDA field directly as last resort
            java.lang.reflect.Field jdaField = JavaDiscordBot.class.getDeclaredField("jda");
            jdaField.setAccessible(true);
            Object jdaInstance = jdaField.get(botInstance);

            if (jdaInstance != null) {
                // Try to call shutdownNow via reflection
                java.lang.reflect.Method shutdownMethod = jdaInstance.getClass().getMethod("shutdownNow");
                shutdownMethod.invoke(jdaInstance);
                logger.info("✅ Bot {} emergency cleanup successful", botName);
                return; // Success
            }
        } catch (Throwable t) {
            logger.error("❌ Emergency cleanup failed for bot {}: {}", botName, t.getMessage());
        }

        // Phase 4: Final failure - log critical error
        logger.error("🚨 CRITICAL: All shutdown attempts failed for bot {}. " +
                "Bot resources may not be properly cleaned up. " +
                "Server restart recommended if issues persist.", botName);

         */

        // -- release since 26.5.161: close classloader to release jar on RAM to prevent NoClassDefFoundError has thrown.
        try {
            ClassLoader loader = botInstance.getClass().getClassLoader();
            if (loader instanceof URLClassLoader urlClassLoader) {
                urlClassLoader.close();
                logger.debug("🔒 Closed ClassLoader for bot {}", botName);
            }
        } catch (Throwable t) {
            logger.error("❌ Failed to close ClassLoader for bot {}", botName, t);
        }

        if (!jdaShutdownSuccess && botInstance.getJda() != null) {
            logger.error("🚨 CRITICAL: Failed to shutdown JDA for bot {}. Ghost connection may persist!", botName);
        }
    }

    /**
     * List loaded bots.
     * Use unmodifiableList to prevent another classes modify this list.
     */
    public List<JavaDiscordBot> getActiveBots() {
        return Collections.unmodifiableList(activeBots);
    }

    public void shutdownAll() {
        System.out.println("🛑 Disabling all bots...");
        for (JavaDiscordBot bot : activeBots) {
            bot.onShutdown();
        }
        System.out.println("✅ All bots are shutdown successfully!");
    }

    /**
     * Dynamically loads and starts a bot from a JAR file at runtime.
     *
     * @param jarFileName The name of the bot JAR file (e.g., "MyBot.jar")
     * @return {@code true} if the bot was successfully enabled, {@code false} otherwise.
     */
    @Override
    public boolean enableBot(String jarFileName) {
        File botsFolder = new File("bots");
        if (!botsFolder.exists()) {
            logger.error("❌ Bots folder does not exist!");
            return false;
        }

        File jarFile = new File(botsFolder, jarFileName);
        if (!jarFile.exists()) {
            logger.error("❌ Bot file '{}' not found in bots folder!", jarFileName);
            return false;
        }

        try {
            logger.info("⏳ Dynamically enabling bot file {}...", jarFileName);

            // Check if bot is already loaded
            String nameWithoutJar = jarFileName.replace(".jar", "");
            for (JavaDiscordBot activeBot : activeBots) {
                if (activeBot.getMeta().botName().equalsIgnoreCase(nameWithoutJar)) {
                    logger.warn("⚠️ Bot '{}' is already enabled!", nameWithoutJar);
                    return false;
                }
            }

            // Initialize ClassLoader for the bot
            URL jarUrl = jarFile.toURI().toURL();
            URLClassLoader botClassLoader = new URLClassLoader(
                    new URL[]{jarUrl},
                    this.getClass().getClassLoader()
            );

            // Get bot metadata
            IBotMeta meta = getBotMetaFromJarFile(botClassLoader, jarFile);
            logger.info("🚀 Preparing to load bot: {} (v{}) by {}", meta.botName(), meta.botVersion(), meta.botAuthor());

            // Use Reflection to find and initialize the bot class
            Class<?> mainClass = Class.forName(meta.mainClassPath(), true, botClassLoader);

            if (!JavaDiscordBot.class.isAssignableFrom(mainClass)) {
                logger.error("❌ Class '{}' does not extend JavaDiscordBot!", meta.mainClassPath());
                return false;
            }

            // Create bot instance
            JavaDiscordBot botInstance = (JavaDiscordBot) mainClass.getDeclaredConstructor().newInstance();

            // Inject metadata using reflection
            java.lang.reflect.Method setMetaMethod = JavaDiscordBot.class.getDeclaredMethod("internalSetMeta", IBotMeta.class);
            setMetaMethod.setAccessible(true);
            setMetaMethod.invoke(botInstance, meta);

            // Start bot in separate thread
            new Thread(() -> startBotSafely(botInstance, meta), "Bot-" + meta.botName()).start();

            return true;

        } catch (Exception e) {
            logger.error("❌ Failed to enable bot '{}': {}", jarFileName, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Gracefully shuts down and removes a bot from the active bots list.
     *
     * @param botName The name of the bot to disable (matches the 'name' field in bot.yml)
     * @return {@code true} if the bot was successfully disabled, {@code false} if not found.
     */
    @Override
    public boolean disableBot(String botName) {
        synchronized (activeBots) {
            for (JavaDiscordBot bot : activeBots) {
                if (bot.getMeta().botName().equalsIgnoreCase(botName)) {
                    logger.info("⏹ Disabling bot '{}'...", botName);
                    shutdownBotGracefully(bot, bot.getMeta());
                    activeBots.remove(bot);
                    logger.info("✅ Bot '{}' has been disabled successfully!", botName);
                    return true;
                }
            }
        }
        logger.warn("⚠️ Bot '{}' is not currently active!", botName);
        return false;
    }
}
