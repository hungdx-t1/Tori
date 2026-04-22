package com.dianxin.tori.api.commands.usercontext;

import com.dianxin.tori.api.bot.IBotMeta;
import com.dianxin.tori.api.commands.CommandReplyConfig;
import com.dianxin.tori.api.commands.LegacyCommandBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * An abstract base class for legacy-style User Context Menus.
 * This class relies on constructor parameters to define its metadata (such as the title)
 * instead of class-level annotations.
 */
@SuppressWarnings("unused")
public abstract class BaseUserContextMenu implements IUserContextMenu {
    private final Logger logger;
    private final String title;
    private final JDA jda;
    private final IBotMeta botMeta;

    // Command config
    private final boolean isDefer; // default false
    private final boolean guildOnly; // default false
    private final boolean ownerOnly; // default false
    private final boolean privateChannelOnly; // default false
    private final boolean directMessageOnly; // (DM = direct message), default false
    private final List<Permission> selfPermissionsRequired; // nullable or empty
    private final List<Permission> permissionsRequired; // nullable or empty
    private final boolean isDebug; // default false

    public BaseUserContextMenu(JDA jda, IBotMeta meta, String title, boolean isDefer, boolean guildOnly, boolean ownerOnly,
                       boolean privateChannelOnly, boolean directMessageOnly,
                       List<Permission> permissionsRequired,
                       List<Permission> selfPermissionsRequired,
                       boolean isDebug) {
        this.logger = LoggerFactory.getLogger(this.getClass());
        this.jda = jda;
        this.botMeta = meta;
        this.title = title;

        this.isDefer = isDefer;
        this.guildOnly = guildOnly;
        this.ownerOnly = ownerOnly;
        this.privateChannelOnly = privateChannelOnly;
        this.directMessageOnly = directMessageOnly;
        this.permissionsRequired = permissionsRequired;
        this.selfPermissionsRequired = selfPermissionsRequired;
        this.isDebug = isDebug;
    }

    public BaseUserContextMenu(String title, JDA jda, IBotMeta meta, LegacyCommandBuilder builder) {
        this.title = title;
        this.jda = jda;
        this.botMeta = meta;

        this.isDefer = builder.isDefer();
        this.guildOnly = builder.isGuildOnly();
        this.ownerOnly = builder.isOwnerOnly();
        this.privateChannelOnly = builder.isPrivateChannelOnly();
        this.directMessageOnly = builder.isDirectMessageOnly();
        this.permissionsRequired = builder.getPermissionsRequired();
        this.selfPermissionsRequired = builder.getSelfPermissionsRequired();
        this.isDebug = builder.isDebug();

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
     * @return The logger instance for the current command.
     */
    protected Logger getLogger() {
        return logger;
    }

    /**
     * Executes the user context menu logic.
     * This method must be overridden by subclasses to provide actual functionality.
     *
     * @param event The {@link UserContextInteractionEvent} triggered by Discord.
     */
    @Override
    public final void handle(UserContextInteractionEvent event, CommandReplyConfig replyConfig) {
        if (!checkOwnerOnly(event, replyConfig)) return;
        if (!checkDMOnly(event, replyConfig)) return;
        if (!checkPrivateChannelOnly(event, replyConfig)) return;
        if (!checkGuildOnly(event, replyConfig)) return;
        if (!checkUserPermissions(event, replyConfig)) return;
        if (!checkBotPermissions(event, replyConfig)) return;

        applyDeferIfNeeded(event);

        try {
            execute(event);
        } catch (Exception e) {
            logger.error("❌ An error occured when trying to handle command `{}`!", event.getName(), e);
        }

        logDebug(event);
    }

    // =========================================
    // Begin of Checkers
    // =========================================

    private boolean checkOwnerOnly(UserContextInteractionEvent event, CommandReplyConfig replyConfig) {
        if (!this.ownerOnly) return true;

        if (!event.getUser().getId().equals(botMeta.botOwnerId())) {
            event.reply(replyConfig.getOwnerOnlyMessage()).setEphemeral(true).queue();
            return false;
        }

        return true;
    }

    private boolean checkDMOnly(UserContextInteractionEvent event, CommandReplyConfig replyConfig) {
        if (!this.directMessageOnly) return true;

        if (event.getGuild() != null) {
            event.reply(replyConfig.getDmOnlyMessage()).setEphemeral(true).queue();
            return false;
        }

        return true;
    }

    private boolean checkPrivateChannelOnly(UserContextInteractionEvent event, CommandReplyConfig replyConfig) {
        if (!this.privateChannelOnly) return true;

        if (event.getChannelType() == ChannelType.PRIVATE) return true;

        event.reply(replyConfig.getPrivateChannelOnlyMessage()).setEphemeral(true).queue();
        return false;
    }

    private boolean checkGuildOnly(UserContextInteractionEvent event, CommandReplyConfig replyConfig) {
        if (!this.guildOnly) return true;

        if (event.getGuild() == null) {
            event.reply(replyConfig.getGuildOnlyMessage()).setEphemeral(true).queue();
            return false;
        }
        return true;
    }

    private boolean checkUserPermissions(UserContextInteractionEvent event, CommandReplyConfig replyConfig) {
        if (this.permissionsRequired == null || this.permissionsRequired.isEmpty()) return true;

        Member member = event.getMember();
        if (member == null) {
            event.reply("⚠️ Cannot handle command when member not found.").setEphemeral(true).queue();
            return false;
        }

        for (Permission p : this.permissionsRequired) {
            if (!member.hasPermission(p)) {
                event.reply(replyConfig.getMissingUserPermissionMessage(p)).setEphemeral(true).queue();
                return false;
            }
        }
        return true;
    }

    private boolean checkBotPermissions(UserContextInteractionEvent event, CommandReplyConfig replyConfig) {
        if (this.selfPermissionsRequired == null || this.selfPermissionsRequired.isEmpty()) return true;

        Guild guild = event.getGuild();
        if (guild == null) return false;

        Member self = guild.getSelfMember();

        for (Permission p : this.selfPermissionsRequired) {
            if (!self.hasPermission(p)) {
                event.reply(replyConfig.getMissingBotPermissionMessage(p)).setEphemeral(true).queue();
                return false;
            }
        }
        return true;
    }

    private void applyDeferIfNeeded(UserContextInteractionEvent event) {
        if (this.isDefer) {
            event.deferReply().queue();
        }
    }

    private void logDebug(UserContextInteractionEvent event) {
        if (!this.isDebug) return;

        logger.debug("[Command] {} by {} | {}",
                event.getName(),
                event.getUser().getAsTag(),
                event.getCommandString()
        );
    }

    protected abstract void execute(UserContextInteractionEvent event);
}