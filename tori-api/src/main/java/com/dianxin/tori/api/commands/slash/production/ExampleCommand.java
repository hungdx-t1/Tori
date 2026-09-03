package com.dianxin.tori.api.commands.slash.production;

import com.dianxin.tori.api.commands.slash.develop.Command;
import com.dianxin.tori.api.commands.slash.develop.CommandOption;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.OptionType;

@Command(name = "hello", description = "Gửi lời chào đến bản thân hoặc người khác.")
public class ExampleCommand {

    @CommandOption(type = OptionType.MENTIONABLE, name = "member", required = false)
    Member targetMember;



}
