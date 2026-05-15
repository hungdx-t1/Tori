package com.dianxin.tori.api.interaction;

import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import org.jetbrains.annotations.NotNull;

public interface IStringSelectMenuHandler {
    @NotNull StringSelectMenu getStringSelectMenu();
    void execute(StringSelectInteractionEvent event);
}