package com.dianxin.tori.api.interaction;

import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a handler for a specific Discord Button interaction.
 * Implement this interface to define the behavior when a user clicks a custom button.
 */
public interface IButtonHandler {

    /**
     * Gets the Discord Button component associated with this handler.
     *
     * @return The configured {@link Button} instance.
     */
    @NotNull Button getButton();

    /**
     * Executes the specific logic when the associated button is clicked.
     *
     * @param event The {@link ButtonInteractionEvent} triggered by the user.
     */
    void execute(ButtonInteractionEvent event);
}