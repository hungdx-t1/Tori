package com.dianxin.tori.api.interaction;

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.modals.Modal;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a handler for a specific Discord Modal interaction.
 * Implement this interface to define the behavior when a user submits a modal form.
 */
public interface IModalHandler {

    /**
     * Gets the Discord Modal component associated with this handler.
     *
     * @return The configured {@link Modal} instance.
     */
    @NotNull Modal getModal();

    /**
     * Executes the specific logic when the modal is submitted.
     *
     * @param event The {@link ModalInteractionEvent} triggered by the user.
     */
    void execute(ModalInteractionEvent event);
}