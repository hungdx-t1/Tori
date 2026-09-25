package com.dianxin.tori.api.commands.text;

import com.dianxin.tori.api.commands.CommandReplyConfig;
import com.dianxin.tori.base.annotations.ReleasedSince;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.ApiStatus;

/**
 * The core interface for legacy text-based commands (e.g., "!ping").
 * @deprecated Discord is shifting toward Slash Commands to improve user privacy, security, and discoverability.
 * So we need to deprecate any text command classes to align with Discord's requirements.
 * It is recommended to use Slash command registrars for better experience.
 */
@ReleasedSince("26.4.234")
@Deprecated
@ApiStatus.ScheduledForRemoval(inVersion = "26.10.0")
@ApiStatus.Obsolete(since = "Discord Message Content Intent restrictions")
public interface ITextCommand {
    /**
     * Handles the incoming text command interaction.
     *
     * @param event       The {@link MessageReceivedEvent} triggered by Discord.
     * @param args        An array of arguments parsed from the message.
     * @param replyConfig The configuration used for custom error or rejection messages.
     */
    void handle(MessageReceivedEvent event, String[] args, CommandReplyConfig replyConfig);
}