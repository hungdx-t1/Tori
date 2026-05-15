package com.dianxin.tori.api.interaction;

import net.dv8tion.jda.api.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import org.jetbrains.annotations.NotNull;

public interface IEntitySelectMenuHandler {
    @NotNull EntitySelectMenu getEntitySelectMenu();
    void execute(EntitySelectInteractionEvent event);
}
