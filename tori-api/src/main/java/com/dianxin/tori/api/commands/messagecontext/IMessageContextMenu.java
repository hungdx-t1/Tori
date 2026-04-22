package com.dianxin.tori.api.commands.messagecontext;

import com.dianxin.tori.api.commands.CommandReplyConfig;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;

/**
 * The core interface for Message Context Menu commands.
 * Classes implementing this interface define the execution logic for commands
 * triggered when a user right-clicks on a specific message in Discord.
 */
public interface IMessageContextMenu {

    /**
     * Handles the incoming message context menu interaction.
     *
     * @param event       The {@link MessageContextInteractionEvent} triggered by Discord.
     * @param replyConfig The configuration used for custom error or rejection messages.
     */
    void handle(MessageContextInteractionEvent event, CommandReplyConfig replyConfig);
}