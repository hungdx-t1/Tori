package com.dianxin.tori.api.commands.usercontext;

import com.dianxin.tori.api.commands.CommandReplyConfig;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;

/**
 * An abstract base class for modern User Context Menus.
 * <p>
 * Subclasses extending this class must be annotated with {@link com.dianxin.tori.api.annotations.contextmenu.ContextMenu}
 * to provide the necessary metadata (like the interaction name) to the {@link com.dianxin.tori.api.commands.CommandRegistrar}.
 */
public abstract class ModernBaseUserContextMenu implements IUserContextMenu {

    @Override
    public final void handle(UserContextInteractionEvent event, CommandReplyConfig replyConfig) {}
}