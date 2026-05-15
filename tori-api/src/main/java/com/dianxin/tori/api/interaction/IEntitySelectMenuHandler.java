package com.dianxin.tori.api.interaction;

import net.dv8tion.jda.api.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a handler for a specific Discord Entity Select Menu interaction.
 * Implement this interface to define the behavior when a user selects an entity (User, Role, Channel, etc.).
 */
public interface IEntitySelectMenuHandler {

    /**
     * Gets the Discord Entity Select Menu component associated with this handler.
     *
     * @return The configured {@link EntitySelectMenu} instance.
     */
    @NotNull EntitySelectMenu getEntitySelectMenu();

    /**
     * Executes the specific logic when an entity is selected from the menu.
     *
     * @param event The {@link EntitySelectInteractionEvent} triggered by the user.
     */
    void execute(EntitySelectInteractionEvent event);
}