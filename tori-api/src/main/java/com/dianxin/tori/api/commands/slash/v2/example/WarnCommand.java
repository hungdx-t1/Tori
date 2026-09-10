package com.dianxin.tori.api.commands.slash.v2.example;

import com.dianxin.tori.api.commands.slash.v2.annotations.Command;
import com.dianxin.tori.api.commands.slash.v2.annotations.CommandOption;
import com.dianxin.tori.api.commands.slash.v2.annotations.Execute;
import com.dianxin.tori.api.commands.slash.v2.annotations.Subcommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

/**
 * Example demonstrating a nested subcommand structure for a moderation warning system.
 *
 * <p>Available subcommands:</p>
 * <ul>
 *   <li>{@code /warn add <target> <reason>}</li>
 *   <li>{@code /warn remove <target> <id>}</li>
 * </ul>
 */
@Command(name = "warn", description = "Member warning and moderation management system.")
@SuppressWarnings("unused")
public class WarnCommand {

    /**
     * Subcommand to issue a warning to a guild member.
     */
    @Subcommand(name = "add", description = "Issue a formal warning to a guild member.")
    public static class AddWarn {

        @CommandOption(type = OptionType.USER, name = "target", description = "The member receiving the warning", required = true)
        private Member target;

        @CommandOption(type = OptionType.STRING, name = "reason", description = "The reason for this warning", required = true)
        private String reason;

        /**
         * Executes the add warning logic.
         *
         * @param event the interaction event
         */
        @Execute(permissions = {Permission.MODERATE_MEMBERS})
        public void run(SlashCommandInteractionEvent event) {
            event.getHook().sendMessage("⚠️ Warned " + target.getAsMention() + " for reason: " + reason).queue();
        }
    }

    /**
     * Subcommand to remove an existing warning by its identifier.
     */
    @Subcommand(name = "remove", description = "Revoke or clear an existing warning.")
    public static class RemoveWarn {

        @CommandOption(type = OptionType.USER, name = "target", description = "The target member whose warning will be revoked", required = true)
        private Member target;

        @CommandOption(type = OptionType.INTEGER, name = "id", description = "The unique warning ID to remove", required = true)
        private int warnId;

        /**
         * Executes the remove warning logic.
         *
         * @param event the interaction event
         */
        @Execute(permissions = {Permission.MODERATE_MEMBERS})
        public void run(SlashCommandInteractionEvent event) {
            event.getHook().sendMessage("✅ Revoked warning #" + warnId + " for " + target.getAsMention()).queue();
        }
    }
}