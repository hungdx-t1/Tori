package com.dianxin.tori.base.console.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class representing a terminal or console command.
 * Any custom system command must extend this class and implement the {@link #execute(String[])} method.
 */
@SuppressWarnings("unused")
public abstract class AbstractConsoleCommand {
    private final Logger logger;
    private final String commandLine;

    /**
     * Constructs a new console command with the specified trigger keyword.
     *
     * @param commandLine the unique string trigger for this command (e.g., "stop", "help")
     */
    public AbstractConsoleCommand(String commandLine) {
        this.commandLine = commandLine.trim();
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    /**
     * Executes the core logic of this command.
     *
     * @param args the array of arguments passed to the command, excluding the command trigger itself
     */
    public abstract void execute(String[] args);

    /**
     * Gets the logger associated with the runtime class of this command instance.
     *
     * @return the logger instance
     */
    protected Logger getLogger() {
        return logger;
    }

    /**
     * Gets the command line keyword trigger.
     *
     * @return the lowercase trimmed trigger string
     */
    public String getCommandLine() {
        return commandLine;
    }
}