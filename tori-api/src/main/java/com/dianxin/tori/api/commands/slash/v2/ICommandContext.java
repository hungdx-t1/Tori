package com.dianxin.tori.api.commands.slash.v2;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

public interface ICommandContext {
    JDA getJDA();
    Guild getGuild();
    Member getMember();
    User getAuthor();
    MessageChannel getChannel();

    // Thống nhất hàm phản hồi
    void reply(String message);
    void replyEmbeds(MessageEmbed... embeds);

    // Defer async (Slash thì deferReply, Text thì hiển thị typing)
    void defer(boolean ephemeral);
    void replyHook(String message);
}