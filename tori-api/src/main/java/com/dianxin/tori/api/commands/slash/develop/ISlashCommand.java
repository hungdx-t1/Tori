package com.dianxin.tori.api.commands.slash.develop;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.ApiStatus;

@FunctionalInterface
@ApiStatus.Experimental // đang trong quá trình phát triển
public interface ISlashCommand {

    @ApiStatus.OverrideOnly
    void onCommand(SlashCommandInteractionEvent event);

    @ApiStatus.OverrideOnly
    default boolean isDefer() { return false; }
}
