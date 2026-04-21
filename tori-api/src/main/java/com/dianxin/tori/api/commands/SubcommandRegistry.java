package com.dianxin.tori.api.commands;

import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.ApiStatus;

/**
 * An interface used to register and define Discord slash subcommands.
 * Classes implementing this interface must provide the necessary data to build a subcommand.
 */
@SuppressWarnings("unused")
public interface SubcommandRegistry {

    /**
     * Gets the subcommand data required for registering this subcommand with Discord.
     *
     * @return The {@link SubcommandData} representing this subcommand's name, description, and options.
     */
    SubcommandData getSubcommand();
}