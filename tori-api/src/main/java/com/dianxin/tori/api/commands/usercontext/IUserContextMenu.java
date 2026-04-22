package com.dianxin.tori.api.commands.usercontext;

import com.dianxin.tori.api.commands.CommandReplyConfig;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;

/**
 * The core interface for User Context Menu commands.
 * Classes implementing this interface define the execution logic for commands
 * triggered when a user right-clicks on another user in Discord.
 */
public interface IUserContextMenu {

    void handle(UserContextInteractionEvent event, CommandReplyConfig replyConfig);
}