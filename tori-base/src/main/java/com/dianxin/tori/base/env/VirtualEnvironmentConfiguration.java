package com.dianxin.tori.base.env;

import io.github.cdimascio.dotenv.Dotenv;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;

/**
 * Utility class for environment configuration management.
 * Loads configuration variables from both native system environment variables
 * and local environment files (e.g., {@code .env}) via the Dotenv library.
 * <p>
 * System environment variables always take precedence over properties defined in the local file.
 * </p>
 */
@NullMarked
@SuppressWarnings("unused")
public final class VirtualEnvironmentConfiguration {
    private static final Dotenv dotenv = VirtualEnvironmentConfiguration.load();

    /**
     * Configures and loads the Dotenv instance.
     * Looks up the lookup directory using the {@code DOTENV_DIR} system variable,
     * defaulting to the current working directory ({@code "."}) if absent.
     *
     * @return a configured {@link Dotenv} instance
     */
    private static Dotenv load() {
        String dir = System.getenv().getOrDefault("DOTENV_DIR", ".");
        return Dotenv.configure().directory(dir).ignoreIfMissing().load();
    }

    private VirtualEnvironmentConfiguration() { }

    /**
     * Retrieves the environment variable value linked with the specified key.
     * Looks up system environment variables first before checking the local dotenv context.
     *
     * @param key the configuration key name
     * @return the configuration value string, or {@code null} if the key is not defined anywhere
     */
    @Nullable
    public static String get(String key) {
        // Prioritize native system environment variables first
        String sys = System.getenv(key);
        return sys != null ? sys : dotenv.get(key);
    }

    /**
     * Retrieves the environment variable value associated with the specified key,
     * returning the fallback value if the key is missing.
     *
     * @param key          the configuration key name
     * @param defaultValue the fallback value to return if no entry is found
     * @return the configuration value, or the default value if unresolved
     */
    public static String getOrDefault(String key, String defaultValue) {
        String value = get(key);
        return value != null ? value : defaultValue;
    }

    /**
     * Retrieves the environment variable value linked with the specified key,
     * throwing an exception if the key is not present.
     *
     * @param key the configuration key name
     * @return the resolved configuration value string
     * @throws RuntimeException if the key cannot be found in the current environment context
     */
    public static String getOrThrow(String key) {
        String value = get(key);
        if (value == null) {
            throw new RuntimeException("Key %s is not provided".formatted(key));
        }
        return value;
    }

    /**
     * Retrieves the environment variable value linked with the specified key,
     * throwing a custom exceptions wrapper if the key is missing.
     *
     * @param key the configuration key name
     * @param th  the custom exception instance to be thrown
     * @return the resolved configuration value string
     * @throws Throwable the customized exception provided as a parameter
     */
    public static String getOrThrow(String key, Throwable th) throws Throwable {
        String value = get(key);
        if (value == null) {
            throw th;
        }
        return value;
    }
}