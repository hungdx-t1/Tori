package com.dianxin.tori.api.commands.slash.v2.example;

import com.dianxin.tori.api.commands.slash.v2.annotations.Command;
import com.dianxin.tori.api.commands.slash.v2.annotations.CommandOption;
import com.dianxin.tori.api.commands.slash.v2.annotations.Execute;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

@Command(name = "hello", description = "Gửi lời chào đến bản thân hoặc người khác.")
public class ExampleCommand {

    @CommandOption(type = OptionType.USER, name = "member", description = "Người bạn muốn chào", required = false)
    private Member targetMember;

    @CommandOption(type = OptionType.STRING, name = "message", description = "Lời nhắn gửi kèm", required = false)
    private String customMessage;

    @Execute
    public void run(SlashCommandInteractionEvent event) {
        Member sender = event.getMember();
        Member recipient = (targetMember != null) ? targetMember : sender;

        String extra = (customMessage != null) ? "\nLời nhắn: " + customMessage : "";

        event.getHook().sendMessage("Xin chào " + recipient.getAsMention() + "!" + extra).queue();
    }
}
