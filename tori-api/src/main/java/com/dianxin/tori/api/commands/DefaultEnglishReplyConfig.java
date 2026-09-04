package com.dianxin.tori.api.commands;

import net.dv8tion.jda.api.Permission;
import org.jspecify.annotations.NullMarked;

/**
 * The default English implementation of {@link CommandReplyConfig}.
 * Provides standard, readable error messages for all command validation failures.
 */
@SuppressWarnings("unused")
@NullMarked
public class DefaultEnglishReplyConfig implements CommandReplyConfig {
    @Override
    public String getOwnerOnlyMessage() { return "❌ Only the bot owner can use this command."; }

    @Override
    public String getDmOnlyMessage() { return "❌ This command can only be used in Direct Messages."; }

    @Override
    public String getPrivateChannelOnlyMessage() { return "❌ This command is restricted to Private Channels."; }

    @Override
    public String getGuildOnlyMessage() { return "❌ This command can only be used inside a Server (Guild)."; }

    @Override
    public String getMissingUserPermissionMessage(Permission permission) {
        return "❌ You lack the `" + permission.getName() + "` permission.";
    }

    @Override
    public String getMissingBotPermissionMessage(Permission permission) {
        return "❌ I am missing the `" + permission.getName() + "` permission.";
    }

    @Override
    public String getCommandNotFoundMessage() { return "❌ This command does not exist or is not loaded."; }
}