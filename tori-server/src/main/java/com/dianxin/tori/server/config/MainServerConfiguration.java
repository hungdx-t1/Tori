package com.dianxin.tori.server.config;

import com.dianxin.core.api.config.yaml.FileConfiguration;
import com.dianxin.core.api.config.yaml.YamlConfiguration;
import com.dianxin.tori.api.config.ServerConfiguration;

import java.io.File;
import java.io.IOException;

public class MainServerConfiguration implements ServerConfiguration {
    private final FileConfiguration configuration;

    public MainServerConfiguration(File file) throws IOException {
        this.configuration = new YamlConfiguration();
        this.configuration.load(file);
    }

    @Override
    public FileConfiguration getConfig() {
        return configuration;
    }

    @Override
    public boolean isIgnoreErrorsOnRestAction() {
        return configuration.getBoolean("jda-default.rest-action.ignore-errors", true);
    }

    @Override
    public boolean isGracefulLogOnUnknownInteractionError() {
        return configuration.getBoolean("jda-default.rest-action.graceful-log-on-unknown-interaction-error", true);
    }
}