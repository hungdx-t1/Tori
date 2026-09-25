package com.dianxin.tori.api.interaction;

import com.dianxin.tori.base.annotations.ReleasedSince;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a handler for a specific Discord String Select Menu interaction.
 * Implement this interface to define the behavior when a user selects a predefined string option.
 */
@ReleasedSince("26.5.123")
public interface IStringSelectMenuHandler {

    /**
     * Gets the Discord String Select Menu component associated with this handler.
     *
     * @return The configured {@link StringSelectMenu} instance.
     */
    @NotNull StringSelectMenu getStringSelectMenu();

    /**
     * Executes the specific logic when a string option is selected from the menu.
     *
     * @param event The {@link StringSelectInteractionEvent} triggered by the user.
     */
    void execute(StringSelectInteractionEvent event);
}