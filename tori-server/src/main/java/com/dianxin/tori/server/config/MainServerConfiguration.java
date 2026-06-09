package com.dianxin.tori.server.config;

import com.dianxin.tori.api.config.ServerConfiguration;
import com.dianxin.tori.base.configuration.yaml.FileConfiguration;
import com.dianxin.tori.base.configuration.yaml.YamlConfiguration;

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

    @Override
    public boolean isDebug() {
        return configuration.getBoolean("console.debug", false);
    }

    @Override
    public boolean isSuppressingSomePackageOnStackTraceEnabled() {
        return configuration.getBoolean("console.enable-suppressing-some-package-on-stack-trace", true);
    }
}