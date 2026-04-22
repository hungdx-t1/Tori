package com.dianxin.tori.api.commands.slash;

import com.dianxin.tori.api.annotations.commands.*;
import com.dianxin.tori.api.bot.IBotMeta;
import com.dianxin.tori.api.commands.CommandReplyConfig;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The modern, annotation-driven base class for all slash commands.
 * Developers can use annotations like {@code @GuildOnly} or {@code @RequirePermissions}
 * on subclasses to dynamically enforce execution rules.
 */
@SuppressWarnings("unused")
public abstract class ModernBaseCommand implements ISlashCommand {
    private final Logger logger;
    private final JDA jda;
    private final IBotMeta botMeta;

    /**
     * Constructs a new ModernBaseCommand.
     *
     * @param jda  The {@link JDA} instance running this command.
     * @param meta The {@link IBotMeta} containing the bot's metadata.
     */
    public ModernBaseCommand(JDA jda, IBotMeta meta) {
        this.logger = LoggerFactory.getLogger(this.getClass());
        this.jda = jda;
        this.botMeta = meta;
    }

    /**
     * @return The logger instance for the current command.
     */
    protected Logger getLogger() {
        return logger;
    }

    /**
     * @return The JDA instance associated with this command.
     */
    protected JDA getJda() {
        return jda;
    }

    /**
     * Handles the lifecycle and validation of the command execution.
     * Parses the command's class annotations to enforce restrictions before
     * delegating to {@link #execute(SlashCommandInteractionEvent)}.
     *
     * @param event       The {@link SlashCommandInteractionEvent} triggered by Discord.
     * @param replyConfig The configuration used for custom error/rejection messages.
     */
    public final void handle(SlashCommandInteractionEvent event, CommandReplyConfig replyConfig) {
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
            logger.error("❌ An error occured when trying to execute command `{}`!", event.getName(), e);
        }

        logDebug(event);
    }

    // =========================================
    // begin of checker

    private boolean checkOwnerOnly(SlashCommandInteractionEvent event, CommandReplyConfig replyConfig) {
        if (!getClass().isAnnotationPresent(OwnerOnly.class)) return true;

        if(!event.getUser().getId().equals(botMeta.botOwnerId())) {
            event.reply(replyConfig.getOwnerOnlyMessage()).setEphemeral(true).queue();
            return false;
        }

        return true;
    }

    private boolean checkDMOnly(SlashCommandInteractionEvent event, CommandReplyConfig replyConfig) {
        if(!getClass().isAnnotationPresent(DirectMessageOnly.class)) return true;

        if(event.getGuild() != null) {
            event.reply(replyConfig.getDmOnlyMessage()).setEphemeral(true).queue();
            return false;
        }

        return true;
    }

    private boolean checkPrivateChannelOnly(SlashCommandInteractionEvent event, CommandReplyConfig replyConfig) {
        if(!getClass().isAnnotationPresent(PrivateChannelOnly.class)) return true;
        if (event.getChannelType() == ChannelType.PRIVATE) return true;
        event.reply(replyConfig.getPrivateChannelOnlyMessage()).setEphemeral(true).queue();
        return false;
    }

    private boolean checkGuildOnly(SlashCommandInteractionEvent event, CommandReplyConfig replyConfig) {
        if (!getClass().isAnnotationPresent(GuildOnly.class)) return true;

        if (event.getGuild() == null) {
            event.reply(replyConfig.getGuildOnlyMessage()).setEphemeral(true).queue();
            return false;
        }
        return true;
    }

    private boolean checkUserPermissions(SlashCommandInteractionEvent event, CommandReplyConfig replyConfig) {
        RequirePermissions ann = getClass().getAnnotation(RequirePermissions.class);
        if (ann == null) return true;

        Member member = event.getMember();
        if (member == null) {
            event.reply("⚠️ Cannot execute command when member not found.").setEphemeral(true).queue();
            return false;
        }

        for (Permission p : ann.value()) {
            if (!member.hasPermission(p)) {
                event.reply(replyConfig.getMissingUserPermissionMessage(p)).setEphemeral(true).queue();
                return false;
            }
        }
        return true;
    }

    private boolean checkBotPermissions(SlashCommandInteractionEvent event, CommandReplyConfig replyConfig) {
        RequireSelfPermissions ann = getClass().getAnnotation(RequireSelfPermissions.class);
        if (ann == null) return true;

        Guild guild = event.getGuild();
        if (guild == null) return false;

        Member self = guild.getSelfMember();

        for (Permission p : ann.value()) {
            if (!self.hasPermission(p)) {
                event.reply(replyConfig.getMissingBotPermissionMessage(p)).setEphemeral(true).queue();
                return false;
            }
        }
        return true;
    }

    private void applyDeferIfNeeded(SlashCommandInteractionEvent event) {
        if (getClass().isAnnotationPresent(DeferReply.class)) {
            event.deferReply().queue();
        }
    }

    private void logDebug(SlashCommandInteractionEvent event) {
        if (!getClass().isAnnotationPresent(DebugCommand.class)) return;

        logger.debug("[Command] {} by {} | {}",
                event.getName(),
                event.getUser().getAsTag(),
                event.getCommandString()
        );
    }

    /**
     * The core execution logic of the command.
     * Developers must implement this method to define what the command actually does.
     *
     * @param event The valid {@link SlashCommandInteractionEvent} passed through all pre-checks.
     */
    protected abstract void execute(SlashCommandInteractionEvent event);
}
