package com.dianxin.tori.api.commands.slash;

import com.dianxin.tori.api.commands.CommandReplyConfig;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

/**
 * The foundational interface for all slash commands within the framework.
 */
public interface ISlashCommand {

    /**
     * Handles the incoming slash command interaction.
     * This method is responsible for running all pre-execution checks before invoking the core logic.
     *
     * @param event       The {@link SlashCommandInteractionEvent} triggered by Discord.
     * @param replyConfig The configuration used for custom error/rejection messages.
     */
    void handle(SlashCommandInteractionEvent event, CommandReplyConfig replyConfig);
}