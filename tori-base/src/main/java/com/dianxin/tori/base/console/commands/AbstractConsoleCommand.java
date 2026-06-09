package com.dianxin.tori.base.console.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unused")
public abstract class AbstractConsoleCommand {
    private final Logger logger;
    private final String commandLine;

    public AbstractConsoleCommand(String commandLine) {
        this.commandLine = commandLine.trim();
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    public abstract void execute(String[] args);

    protected Logger getLogger() {
        return logger;
    }

    public String getCommandLine() {
        return commandLine;
    }
}