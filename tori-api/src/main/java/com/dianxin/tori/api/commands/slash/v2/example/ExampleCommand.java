package com.dianxin.tori.api.commands.slash.v2.example;

import com.dianxin.tori.api.commands.slash.v2.annotations.Command;
import com.dianxin.tori.api.commands.slash.v2.annotations.CommandOption;
import com.dianxin.tori.api.commands.slash.v2.annotations.Execute;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

/**
 * Example demonstrating a standard, single flat slash command with optional options.
 *
 * <p>Usage: {@code /hello [member] [message]}</p>
 */
@Command(name = "hello", description = "Send a greeting to yourself or another member.")
@SuppressWarnings("unused")
public class ExampleCommand {

    @CommandOption(type = OptionType.USER, name = "member", description = "The member you want to greet", required = false)
    private Member targetMember;

    @CommandOption(type = OptionType.STRING, name = "message", description = "An optional attached message", required = false)
    private String customMessage;

    /**
     * Executes the greeting command.
     *
     * @param event the interaction event
     */
    @Execute
    public void run(SlashCommandInteractionEvent event) {
        Member sender = event.getMember();
        Member recipient = (targetMember != null) ? targetMember : sender;

        String extra = (customMessage != null) ? "\nMessage: " + customMessage : "";

        event.getHook().sendMessage("Hello " + (recipient != null ? recipient.getAsMention() : "there") + "!" + extra).queue();
    }
}