package com.dianxin.tori.api.commands.slash;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A statically optimized version of ModernBaseCommand.
 * Completely removes the Annotation scanning (Reflection) process to ensure maximum
 * execution speed, helping to prevent Discord's 3-second "Bot is thinking" timeout error.
 */
@SuppressWarnings("unused")
public abstract class BaseCommand implements ISlashCommand {
    private final Logger logger;
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

    /**
     * Constructs a statically configured BaseCommand.
     *
     * @param jda                     The {@link JDA} instance running this command.
     * @param meta                    The {@link IBotMeta} containing the bot's metadata.
     * @param isDefer                 Whether the reply should be automatically deferred.
     * @param guildOnly               Whether the command is restricted to guilds.
     * @param ownerOnly               Whether the command is restricted to the bot owner.
     * @param privateChannelOnly      Whether the command is restricted to private channels.
     * @param directMessageOnly       Whether the command is restricted to direct messages.
     * @param permissionsRequired     A list of permissions required by the user.
     * @param selfPermissionsRequired A list of permissions required by the bot.
     * @param isDebug                 Whether to log debug information when executed.
     */
    public BaseCommand(JDA jda, IBotMeta meta, boolean isDefer, boolean guildOnly, boolean ownerOnly,
                       boolean privateChannelOnly, boolean directMessageOnly,
                       List<Permission> permissionsRequired,
                       List<Permission> selfPermissionsRequired,
                       boolean isDebug) {
        this.logger = LoggerFactory.getLogger(this.getClass());
        this.jda = jda;
        this.botMeta = meta;

        this.isDefer = isDefer;
        this.guildOnly = guildOnly;
        this.ownerOnly = ownerOnly;
        this.privateChannelOnly = privateChannelOnly;
        this.directMessageOnly = directMessageOnly;
        this.permissionsRequired = permissionsRequired;
        this.selfPermissionsRequired = selfPermissionsRequired;
        this.isDebug = isDebug;
    }

    /**
     * Constructs a BaseCommand using the {@link LegacyCommandBuilder}.
     *
     * @param jda     The {@link JDA} instance running this command.
     * @param meta    The {@link IBotMeta} containing the bot's metadata.
     * @param builder The configured {@link LegacyCommandBuilder}.
     */
    public BaseCommand(JDA jda, IBotMeta meta, LegacyCommandBuilder builder) {
        this.logger = LoggerFactory.getLogger(this.getClass());
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
     * Validates all statically defined conditions before delegating to {@link #execute(SlashCommandInteractionEvent)}.
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
    // Begin of Checkers
    // =========================================

    private boolean checkOwnerOnly(SlashCommandInteractionEvent event, CommandReplyConfig replyConfig) {
        if (!this.ownerOnly) return true;

        if (!event.getUser().getId().equals(botMeta.botOwnerId())) {
            event.reply(replyConfig.getOwnerOnlyMessage()).setEphemeral(true).queue();
            return false;
        }

        return true;
    }

    private boolean checkDMOnly(SlashCommandInteractionEvent event, CommandReplyConfig replyConfig) {
        if (!this.directMessageOnly) return true;

        if (event.getGuild() != null) {
            event.reply(replyConfig.getDmOnlyMessage()).setEphemeral(true).queue();
            return false;
        }

        return true;
    }

    private boolean checkPrivateChannelOnly(SlashCommandInteractionEvent event, CommandReplyConfig replyConfig) {
        if (!this.privateChannelOnly) return true;

        if (event.getChannelType() == ChannelType.PRIVATE) return true;

        event.reply(replyConfig.getPrivateChannelOnlyMessage()).setEphemeral(true).queue();
        return false;
    }

    private boolean checkGuildOnly(SlashCommandInteractionEvent event, CommandReplyConfig replyConfig) {
        if (!this.guildOnly) return true;

        if (event.getGuild() == null) {
            event.reply(replyConfig.getGuildOnlyMessage()).setEphemeral(true).queue();
            return false;
        }
        return true;
    }

    private boolean checkUserPermissions(SlashCommandInteractionEvent event, CommandReplyConfig replyConfig) {
        if (this.permissionsRequired == null || this.permissionsRequired.isEmpty()) return true;

        Member member = event.getMember();
        if (member == null) {
            event.reply("⚠️ Cannot execute command when member not found.").setEphemeral(true).queue();
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

    private boolean checkBotPermissions(SlashCommandInteractionEvent event, CommandReplyConfig replyConfig) {
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

    private void applyDeferIfNeeded(SlashCommandInteractionEvent event) {
        if (this.isDefer) {
            event.deferReply().queue();
        }
    }

    private void logDebug(SlashCommandInteractionEvent event) {
        if (!this.isDebug) return;

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

    /**
     * A utility builder class designed to construct configurations for {@link BaseCommand}.
     * This helps keep the code clean, readable, and manageable during command initialization.
     */
    public static class LegacyCommandBuilder {
        private boolean isDefer = false;
        private boolean guildOnly = false;
        private boolean ownerOnly = false;
        private boolean privateChannelOnly = false;
        private boolean directMessageOnly = false;
        private boolean isDebug = false;
        private final List<Permission> permissionsRequired = new ArrayList<>();
        private final List<Permission> selfPermissionsRequired = new ArrayList<>();

        /**
         * Sets whether the command reply should be automatically deferred.
         * Deferring gives the bot up to 15 minutes to process the request before responding.
         *
         * @param defer {@code true} to automatically defer the reply, {@code false} otherwise.
         * @return This builder instance for method chaining.
         */
        public LegacyCommandBuilder setDefer(boolean defer) {
            this.isDefer = defer;
            return this;
        }
        /**
         * Restricts the command to be executable only within Discord guilds (servers).
         *
         * @param guildOnly {@code true} to restrict to guilds, {@code false} to allow elsewhere.
         * @return This builder instance for method chaining.
         */
        public LegacyCommandBuilder setGuildOnly(boolean guildOnly) {
            this.guildOnly = guildOnly;
            return this;
        }

        /**
         * Restricts the command exclusively to the bot owner.
         *
         * @param ownerOnly {@code true} to restrict to the bot owner, {@code false} otherwise.
         * @return This builder instance for method chaining.
         */
        public LegacyCommandBuilder setOwnerOnly(boolean ownerOnly) {
            this.ownerOnly = ownerOnly;
            return this;
        }

        /**
         * Restricts the command to be executable only in private channels.
         *
         * @param privateChannelOnly {@code true} to restrict to private channels, {@code false} otherwise.
         * @return This builder instance for method chaining.
         */
        public LegacyCommandBuilder setPrivateChannelOnly(boolean privateChannelOnly) {
            this.privateChannelOnly = privateChannelOnly;
            return this;
        }

        /**
         * Restricts the command to be executable only in Direct Messages (DMs).
         *
         * @param directMessageOnly {@code true} to restrict to DMs, {@code false} otherwise.
         * @return This builder instance for method chaining.
         */
        public LegacyCommandBuilder setDirectMessageOnly(boolean directMessageOnly) {
            this.directMessageOnly = directMessageOnly;
            return this;
        }

        /**
         * Sets whether debug logging should be enabled for this command when it is executed.
         *
         * @param debug {@code true} to enable debug logging, {@code false} otherwise.
         * @return This builder instance for method chaining.
         */
        public LegacyCommandBuilder setDebug(boolean debug) {
            this.isDebug = debug;
            return this;
        }

        /**
         * Adds one or more Discord permissions that the user invoking the command must possess.
         *
         * @param permissions The {@link Permission}(s) required by the user.
         * @return This builder instance for method chaining.
         */
        public LegacyCommandBuilder addRequiredPermissions(Permission... permissions) {
            this.permissionsRequired.addAll(Arrays.asList(permissions));
            return this;
        }

        /**
         * Adds one or more Discord permissions that the bot itself must possess to execute the command.
         *
         * @param permissions The {@link Permission}(s) required by the bot.
         * @return This builder instance for method chaining.
         */
        public LegacyCommandBuilder addSelfPermissions(Permission... permissions) {
            this.selfPermissionsRequired.addAll(Arrays.asList(permissions));
            return this;
        }

        /**
         * @return {@code true} if the command reply is set to be deferred.
         */
        public boolean isDefer() { return isDefer; }

        /**
         * @return {@code true} if the command is restricted to guilds.
         */
        public boolean isGuildOnly() { return guildOnly; }

        /**
         * @return {@code true} if the command is restricted to the bot owner.
         */
        public boolean isOwnerOnly() { return ownerOnly; }

        /**
         * @return {@code true} if the command is restricted to private channels.
         */
        public boolean isPrivateChannelOnly() { return privateChannelOnly; }

        /**
         * @return {@code true} if the command is restricted to direct messages.
         */
        public boolean isDirectMessageOnly() { return directMessageOnly; }

        /**
         * @return {@code true} if debug logging is enabled for this command.
         */
        public boolean isDebug() { return isDebug; }

        /**
         * @return A list of permissions required by the user.
         */
        public List<Permission> getPermissionsRequired() { return permissionsRequired; }

        /**
         * @return A list of permissions required by the bot itself.
         */
        public List<Permission> getSelfPermissionsRequired() { return selfPermissionsRequired; }

        /**
         * A convenience factory method to create a builder pre-configured to automatically defer
         * the reply and restrict the command execution strictly to Discord guilds.
         *
         * @return A new, pre-configured {@link LegacyCommandBuilder} instance.
         */
        public static LegacyCommandBuilder deferAndOnlyGuild() {
            return new LegacyCommandBuilder().setDefer(true).setGuildOnly(true);
        }

        /**
         * A convenience factory method to create a builder pre-configured to require the
         * {@link Permission#ADMINISTRATOR} permission from the user.
         *
         * @return A new, pre-configured {@link LegacyCommandBuilder} instance.
         */
        public static LegacyCommandBuilder guildAdminOnly() {
            return new LegacyCommandBuilder().setGuildOnly(false)
                    .addRequiredPermissions(Permission.ADMINISTRATOR);
        }
    }
}
