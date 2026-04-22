package com.dianxin.tori.api.commands;

import net.dv8tion.jda.api.Permission;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A utility builder class designed to construct configurations for any commands.
 * This helps keep the code clean, readable, and manageable during command initialization.
 */
@SuppressWarnings("unused")
public class LegacyCommandBuilder {
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
     * Adds one or more Discord permissions that the bot itself must possess to handle the command.
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
