package com.dianxin.tori.api.commands.messagecontext;

import com.dianxin.tori.api.commands.CommandReplyConfig;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;

public interface IMessageContextMenu {
    void execute(MessageContextInteractionEvent event, CommandReplyConfig replyConfig);
}
