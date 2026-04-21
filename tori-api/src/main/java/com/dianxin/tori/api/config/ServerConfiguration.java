package com.dianxin.tori.api.config;

import com.dianxin.core.api.config.yaml.FileConfiguration;

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
}