package com.dianxin.tori.api.commands.slash.v3;

import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public interface ICommand {

    void execute(SlashContext ctx) throws Exception;

    /** Tự động sinh SlashCommandData từ Annotation */
    default SlashCommandData buildCommandData() {
        SlashInfo info = this.getClass().getAnnotation(SlashInfo.class);
        if (info == null) {
            throw new IllegalStateException("Missing @SlashInfo on " + this.getClass().getSimpleName());
        }
        SlashCommandData data = Commands.slash(info.name(), info.description());
//        data.setGuildOnly(info.guildOnly());
        configure(data);
        return data;
    }

    /** Hook để các subclass tự do bổ sung options / subcommands */
    default void configure(SlashCommandData data) {}
}