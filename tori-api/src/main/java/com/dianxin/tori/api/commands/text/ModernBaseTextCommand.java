package com.dianxin.tori.api.commands.text;

import com.dianxin.tori.api.annotations.commands.*;
import com.dianxin.tori.api.bot.IBotMeta;
import com.dianxin.tori.api.commands.CommandReplyConfig;
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

/**
 * An annotation-driven base class for legacy text commands.
 * @deprecated Discord is shifting toward Slash Commands to improve user privacy, security, and discoverability.
 * So we need to deprecate any text command classes to align with Discord's requirements.
 * It is recommended to use Slash command registrars for better experience.
 */
@ReleasedSince("26.4.234")
@Deprecated
@ApiStatus.ScheduledForRemoval(inVersion = "26.10.0")
@SuppressWarnings("unused")
@ApiStatus.Obsolete(since = "Discord Message Content Intent restrictions")
public abstract class ModernBaseTextCommand implements ITextCommand {
    private final Logger logger;
    private final JDA jda;
    private final IBotMeta botMeta;

    public ModernBaseTextCommand(JDA jda, IBotMeta meta) {
        this.logger = LoggerFactory.getLogger(this.getClass());
        this.jda = jda;
        this.botMeta = meta;
    }

    /**
     * Retrieves the logger instance for the current command.
     * * @return The {@link Logger} instance.
     */
    protected Logger getLogger() {
        return logger;
    }

    /**
     * Retrieves the JDA instance associated with this command.
     * * @return The {@link JDA} instance.
     */
    protected JDA getJda() {
        return jda;
    }

    @Override
    public final void handle(MessageReceivedEvent event, String[] args, CommandReplyConfig replyConfig) {
        if (!checkOwnerOnly(event, replyConfig)) return;
        if (!checkDMOnly(event, replyConfig)) return;
        if (!checkPrivateChannelOnly(event, replyConfig)) return;
        if (!checkGuildOnly(event, replyConfig)) return;
        if (!checkUserPermissions(event, replyConfig)) return;
        if (!checkBotPermissions(event, replyConfig)) return;

        try {
            execute(event, args);
        } catch (Exception e) {
            TextCommand ann = getClass().getAnnotation(TextCommand.class);
            logger.error("❌ An error occured when trying to handle command `{}`!", (ann == null || ann.commandName().isEmpty()) ? "Unknown" : ann.commandName(), e);
        }

        logDebug(event);
    }

    // =========================================
    // Checkers
    // =========================================

    private boolean checkOwnerOnly(MessageReceivedEvent event, CommandReplyConfig replyConfig) {
        if (!getClass().isAnnotationPresent(OwnerOnly.class)) return true;

        if (!event.getAuthor().getId().equals(botMeta.botOwnerId())) {
            event.getChannel().sendMessage(replyConfig.getOwnerOnlyMessage()).queue();
            return false;
        }

        return true;
    }

    private boolean checkDMOnly(MessageReceivedEvent event, CommandReplyConfig replyConfig) {
        if (!getClass().isAnnotationPresent(DirectMessageOnly.class)) return true;

        if (event.isFromGuild()) {
            event.getChannel().sendMessage(replyConfig.getDmOnlyMessage()).queue();
            return false;
        }
        return true;
    }

    private boolean checkPrivateChannelOnly(MessageReceivedEvent event, CommandReplyConfig replyConfig) {
        if (!getClass().isAnnotationPresent(PrivateChannelOnly.class)) return true;

        if (!event.isFromGuild() || event.getChannelType() == ChannelType.PRIVATE) return true;

        event.getChannel().sendMessage(replyConfig.getPrivateChannelOnlyMessage()).queue();
        return false;
    }

    private boolean checkGuildOnly(MessageReceivedEvent event, CommandReplyConfig replyConfig) {
        if (!getClass().isAnnotationPresent(GuildOnly.class)) return true;

        if (!event.isFromGuild()) {
            event.getChannel().sendMessage(replyConfig.getGuildOnlyMessage()).queue();
            return false;
        }
        return true;
    }

    private boolean checkUserPermissions(MessageReceivedEvent event, CommandReplyConfig replyConfig) {
        RequirePermissions ann = getClass().getAnnotation(RequirePermissions.class);
        if (ann == null) return true;

        if (!event.isFromGuild()) return false;

        Member member = event.getMember();
        if (member == null) {
            event.getChannel().sendMessage("⚠️ Cannot handle command when member not found.").queue();
            return false;
        }

        for (Permission p : ann.value()) {
            if (!member.hasPermission(p)) {
                event.getChannel().sendMessage(replyConfig.getMissingUserPermissionMessage(p)).queue();
                return false;
            }
        }
        return true;
    }

    private boolean checkBotPermissions(MessageReceivedEvent event, CommandReplyConfig replyConfig) {
        RequireSelfPermissions ann = getClass().getAnnotation(RequireSelfPermissions.class);
        if (ann == null) return true;

        if (!event.isFromGuild()) return false;

        Guild guild = event.getGuild();
        Member self = guild.getSelfMember();
        for (Permission p : ann.value()) {
            if (!self.hasPermission(p)) {
                event.getChannel().sendMessage(replyConfig.getMissingBotPermissionMessage(p)).queue();
                return false;
            }
        }
        return true;
    }

    private void logDebug(MessageReceivedEvent event) {
        if (!getClass().isAnnotationPresent(DebugCommand.class)) return;
        TextCommand ann = getClass().getAnnotation(TextCommand.class);
        if (ann == null) return;

        logger.debug("[Command] {} by {} | {}",
                ann.commandName(),
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
