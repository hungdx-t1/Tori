package com.dianxin.tori.api.interaction;

import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.jetbrains.annotations.NotNull;

public interface IButtonHandler {
    @NotNull Button getButton();
    void execute(ButtonInteractionEvent event);
}
