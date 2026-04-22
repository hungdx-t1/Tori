package com.dianxin.tori.api.commands;

import net.dv8tion.jda.api.Permission;

/**
 * Configuration interface for customizing the rejection messages sent by the command registrar.
 * Implementing this interface allows developers to support multiple languages (i18n)
 * or custom bot personas without modifying the core logic.
 */
@SuppressWarnings("unused")
public interface CommandReplyConfig {

    /**
     * @return The message sent when a non-owner attempts to use an Owner-only command.
     */
    String getOwnerOnlyMessage();

    /**
     * @return The message sent when a Direct Message-only command is used in a guild.
     */
    String getDmOnlyMessage();

    /**
     * @return The message sent when a command restricted to private channels is used elsewhere.
     */
    String getPrivateChannelOnlyMessage();

    /**
     * @return The message sent when a Guild-only command is used in a direct message.
     */
    String getGuildOnlyMessage();

    /**
     * @param permission The specific permission the user is missing.
     * @return The message sent when the user lacks the required permission.
     */
    String getMissingUserPermissionMessage(Permission permission);

    /**
     * @param permission The specific permission the bot is missing.
     * @return The message sent when the bot lacks the required permission to handle the command.
     */
    String getMissingBotPermissionMessage(Permission permission);

    /**
     * @return The message sent when an unknown or unloaded command is triggered.
     */
    String getCommandNotFoundMessage();
}