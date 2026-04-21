package com.dianxin.tori.api.commands;

import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.ApiStatus;

/**
 * An interface used to register and define main Discord slash commands.
 * Classes implementing this interface must provide the necessary data to build a top-level command.
 */
@SuppressWarnings("unused")
public interface MaincommandRegistry {

    /**
     * Gets the command data required for registering this main command with Discord.
     *
     * @return The {@link CommandData} representing this command's name, description, options, and subcommands.
     */
    CommandData getCommand();
}