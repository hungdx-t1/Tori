package com.dianxin.tori.api.commands.text;

import com.dianxin.tori.api.bot.IBotMeta;
import com.dianxin.tori.api.commands.CommandReplyConfig;
import com.dianxin.tori.api.commands.LegacyCommandBuilder;
import com.dianxin.tori.base.annotations.ReleasedSince;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * A statically configured base class for legacy text commands.
 * @deprecated Discord is shifting toward Slash Commands to improve user privacy, security, and discoverability.
 * So we need to deprecate any text command classes to align with Discord's requirements.
 * It is recommended to use Slash command registrars for better experience.
 */
@ReleasedSince("26.4.234")
@Deprecated
@ApiStatus.ScheduledForRemoval(inVersion = "26.10.0")
@ApiStatus.Obsolete(since = "Discord Message Content Intent restrictions")
@SuppressWarnings("unused")
public abstract class BaseTextCommand implements ITextCommand {
    private final Logger logger;
    private final String commandName;
    private final List<String> aliases;
    private final JDA jda;
    private final IBotMeta botMeta;

    // Command config
    private final boolean guildOnly; // default false
    private final boolean ownerOnly; // default false
    private final boolean privateChannelOnly; // default false
    private final boolean directMessageOnly; // (DM = direct message), default false
    private final List<Permission> selfPermissionsRequired; // nullable or empty
    private final List<Permission> permissionsRequired; // nullable or empty
    private final boolean isDebug; // default false

    public BaseTextCommand(String title, List<String> aliases, JDA jda, IBotMeta meta, boolean guildOnly, boolean ownerOnly,
                               boolean privateChannelOnly, boolean directMessageOnly,
                               List<Permission> permissionsRequired,
                               List<Permission> selfPermissionsRequired,
                               boolean isDebug) {
        this.logger = LoggerFactory.getLogger(this.getClass());
        this.jda = jda;
        this.botMeta = meta;
        this.commandName = title;
        this.aliases = aliases;

        this.guildOnly = guildOnly;
        this.ownerOnly = ownerOnly;
        this.privateChannelOnly = privateChannelOnly;
        this.directMessageOnly = directMessageOnly;
        this.permissionsRequired = permissionsRequired;
        this.selfPermissionsRequired = selfPermissionsRequired;
        this.isDebug = isDebug;
    }

    public BaseTextCommand(String title, List<String> aliases, JDA jda, IBotMeta meta, LegacyCommandBuilder builder) {
        this.commandName = title;
        this.jda = jda;
        this.botMeta = meta;
        this.aliases = aliases;

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

    public String getCommandName() {
        return commandName;
    }

    public List<String> getAliases() {
        return aliases;
    }

    /**
     * Retrieves the logger instance for the current command.
     *
     * @return The {@link Logger} instance.
     */
    protected Logger getLogger() {
        return logger;
    }

    @Override
    public void handle(MessageReceivedEvent event, String[] args, CommandReplyConfig replyConfig) {
        if (!checkOwnerOnly(event, replyConfig)) return;
        if (!checkDMOnly(event, replyConfig)) return;
        if (!checkPrivateChannelOnly(event, replyConfig)) return;
        if (!checkGuildOnly(event, replyConfig)) return;
        if (!checkUserPermissions(event, replyConfig)) return;
        if (!checkBotPermissions(event, replyConfig)) return;

        try {
            execute(event, args);
        } catch (Exception e) {
            logger.error("❌ An error occured when trying to handle command `{}`!", commandName, e);
        }

        logDebug(event);
    }

    private boolean checkOwnerOnly(MessageReceivedEvent event, CommandReplyConfig replyConfig) {
        if (!this.ownerOnly) return true;

        if (!event.getAuthor().getId().equals(botMeta.botOwnerId())) {
            event.getChannel().sendMessage(replyConfig.getOwnerOnlyMessage()).queue();
            return false;
        }
        return true;
    }

    private boolean checkDMOnly(MessageReceivedEvent event, CommandReplyConfig replyConfig) {
        if (!this.directMessageOnly) return true;

        if (event.isFromGuild()) {
            event.getChannel().sendMessage(replyConfig.getDmOnlyMessage()).queue();
            return false;
        }
        return true;
    }

    private boolean checkPrivateChannelOnly(MessageReceivedEvent event, CommandReplyConfig replyConfig) {
        if (!this.privateChannelOnly) return true;

        if (!event.isFromGuild() || event.getChannelType() == ChannelType.PRIVATE) return true;

        event.getChannel().sendMessage(replyConfig.getPrivateChannelOnlyMessage()).queue();
        return false;
    }

    private boolean checkGuildOnly(MessageReceivedEvent event, CommandReplyConfig replyConfig) {
        if (!this.guildOnly) return true;

        if (!event.isFromGuild()) {
            event.getChannel().sendMessage(replyConfig.getGuildOnlyMessage()).queue();
            return false;
        }
        return true;
    }

    private boolean checkUserPermissions(MessageReceivedEvent event, CommandReplyConfig replyConfig) {
        if (this.permissionsRequired == null || this.permissionsRequired.isEmpty()) return true;

        if (!event.isFromGuild()) return false;

        Member member = event.getMember();
        if (member == null) {
            event.getChannel().sendMessage("⚠️ Cannot handle command when member not found.").queue();
            return false;
        }

        for (Permission p : this.permissionsRequired) {
            if (!member.hasPermission(p)) {
                event.getChannel().sendMessage(replyConfig.getMissingUserPermissionMessage(p)).queue();
                return false;
            }
        }
        return true;
    }

    private boolean checkBotPermissions(MessageReceivedEvent event, CommandReplyConfig replyConfig) {
        if (this.selfPermissionsRequired == null || this.selfPermissionsRequired.isEmpty()) return true;

        if (!event.isFromGuild()) return false;

        Guild guild = event.getGuild();
        Member self = guild.getSelfMember();
        for (Permission p : this.selfPermissionsRequired) {
            if (!self.hasPermission(p)) {
                event.getChannel().sendMessage(replyConfig.getMissingBotPermissionMessage(p)).queue();
                return false;
            }
        }
        return true;
    }

    private void logDebug(MessageReceivedEvent event) {
        if (!this.isDebug) return;

        logger.debug("[Command] {} by {} | {}",
                commandName,
                event.getAuthor().getAsTag(),
                event.getMessage().getContentRaw()
        );
    }

    /**
     * The core execution logic of the text command.
     *
     * @param event The valid {@link MessageReceivedEvent}.
     * @param args  The parsed command arguments.
     */
    protected abstract void execute(MessageReceivedEvent event, String[] args);
}
