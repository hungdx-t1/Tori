package com.dianxin.tori.api.commands.usercontext;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * An abstract base class for legacy-style User Context Menus.
 * This class relies on constructor parameters to define its metadata (such as the title)
 * instead of class-level annotations.
 */
public abstract class BaseUserContextMenu implements IUserContextMenu {
    private final Logger logger;
    private final String title;
    private final JDA jda;

    // Command config
    private final boolean isDefer; // default false
    private final boolean guildOnly; // default false
    private final boolean ownerOnly; // default false
    private final boolean privateChannelOnly; // default false
    private final boolean directMessageOnly; // (DM = direct message), default false
    private final List<Permission> selfPermissionsRequired; // nullable or empty
    private final List<Permission> permissionsRequired; // nullable or empty
    private final boolean isDebug; // default false

    /**
     * Constructs a new BaseUserContextMenu.
     *
     * @param title The name of the context menu displayed in Discord.
     * @param type  The type of command (should typically be {@link Command.Type#USER}).
     * @param jda   The {@link JDA} instance running this command.
     */
    public BaseUserContextMenu(String title, Command.Type type, JDA jda) {
        this.title = title;
        this.jda = jda;
        this.logger = LoggerFactory.getLogger(getClass());
    }

    /**
     * Retrieves the JDA instance associated with this command.
     *
     * @return The {@link JDA} instance.
     */
    protected JDA getJda() {
        return jda;
    }

    /**
     * Retrieves the title/name of the context menu.
     *
     * @return The title string.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Executes the user context menu logic.
     * This method must be overridden by subclasses to provide actual functionality.
     *
     * @param event The {@link UserContextInteractionEvent} triggered by Discord.
     */
    @Override
    public void execute(UserContextInteractionEvent event) {
        // To be implemented by subclasses
    }
}