package com.dianxin.tori.api.commands.slash.v3;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.utils.FileUpload;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class SlashContext {
    private final SlashCommandInteractionEvent event;

    public SlashContext(SlashCommandInteractionEvent event) {
        this.event = event;
    }

    public SlashCommandInteractionEvent getEvent() { return event; }
    public Guild getGuild() { return event.getGuild(); }
    public Member getMember() { return event.getMember(); }
    public User getUser() { return event.getUser(); }

    @Nullable
    public OptionMapping getOption(@NotNull String name) {
        return event.getOption(name);
    }

    public String getString(@NotNull String name, @Nullable String defaultValue) {
        OptionMapping opt = getOption(name);
        return opt != null ? opt.getAsString() : defaultValue;
    }

    public long getLong(@NotNull String name, long defaultValue) {
        OptionMapping opt = getOption(name);
        return opt != null ? opt.getAsLong() : defaultValue;
    }

    /** Phản hồi an toàn dựa trên trạng thái đã defer hay chưa */
    public void reply(@NotNull String message) {
        if (event.isAcknowledged()) {
            event.getHook().sendMessage(message).queue();
        } else {
            event.reply(message).queue();
        }
    }

    public void replyFile(@NotNull byte[] data, @NotNull String name) {
        if (event.isAcknowledged()) {
            event.getHook().sendFiles(FileUpload.fromData(data, name)).queue();
        } else {
            event.replyFiles(FileUpload.fromData(data, name)).queue();
        }
    }
}