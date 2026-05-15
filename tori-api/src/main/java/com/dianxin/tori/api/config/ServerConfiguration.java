package com.dianxin.tori.api.config;

import com.dianxin.core.api.config.yaml.FileConfiguration;
import net.dv8tion.jda.api.exceptions.ContextException;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;

/**
 * Represents the core configuration settings for the Tori Server.
 * Provides access to both the raw configuration file data and specific configuration properties.
 */
@SuppressWarnings("unused")
public interface ServerConfiguration {

    /**
     * Retrieves the underlying raw configuration file data.
     *
     * @return The {@link FileConfiguration} instance containing all loaded settings.
     */
    FileConfiguration getConfig();

    /**
     * Checks whether errors on JDA RestActions should be globally ignored.
     *
     * @return {@code true} if REST action errors are ignored (fire-and-forget fallback),
     * {@code false} if they require explicit handling.
     */
    boolean isIgnoreErrorsOnRestAction();

    /**
     * Checks whether logs Discord API Error is formatted gracefully.
     *
     * @return {@code true} if Suppresses the massive default JDA stack trace and, instead prints
     * a clean, single-line warning in the console, when
     * {@code false} if they prints the full {@link ErrorResponseException} and {@link ContextException} stack traces.
     */
    boolean isGracefulLogOnUnknownInteractionError();

    /**
     * Checks whether the global debug mode is enabled for the server.
     * When enabled, additional trace and debug level logging will be output to the console.
     *
     * @return {@code true} if debug mode is active, {@code false} otherwise.
     */
    boolean isDebug();
}