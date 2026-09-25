package com.dianxin.tori.api.interaction;

import com.dianxin.tori.base.annotations.ReleasedSince;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a handler for a specific Discord Button interaction.
 * Implement this interface to define the behavior when a user clicks a custom button.
 */
@ReleasedSince("26.5.123")
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