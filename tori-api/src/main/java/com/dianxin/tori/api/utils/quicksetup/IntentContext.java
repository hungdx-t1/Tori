package com.dianxin.tori.api.utils.quicksetup;

import net.dv8tion.jda.api.requests.GatewayIntent;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;

/**
 * A utility class providing quick access to predefined sets of {@link GatewayIntent}s.
 * This is useful for quickly configuring the JDA builder during bot startup.
 */
@SuppressWarnings("unused")
public final class IntentContext {
    private IntentContext() { }

    /**
     * Retrieves an {@link EnumSet} containing every available {@link GatewayIntent}.
     * <p>
     * <b>Warning:</b> Using all intents requires enabling Privileged Gateway Intents
     * (Presence, Server Members, and Message Content) in your Discord Developer Portal.
     *
     * @return An {@link EnumSet} of all intents.
     */
    @NotNull
    public static EnumSet<GatewayIntent> getAllIntents() {
        return EnumSet.allOf(GatewayIntent.class);
    }

    /**
     * Retrieves a standard, commonly used set of default {@link GatewayIntent}s.
     * <p>
     * This set includes most standard intents along with necessary privileged intents
     * like {@code GUILD_MEMBERS} and {@code MESSAGE_CONTENT}. Ensure these are enabled
     * in your Discord Developer Portal.
     *
     * @return An {@link EnumSet} containing default intents.
     */
    @NotNull
    public static EnumSet<GatewayIntent> getDefaultIntents() {
        return EnumSet.of(
                GatewayIntent.GUILD_MEMBERS,
                GatewayIntent.GUILD_MODERATION,
                GatewayIntent.GUILD_EXPRESSIONS,
                GatewayIntent.GUILD_WEBHOOKS,
                GatewayIntent.GUILD_INVITES,
                GatewayIntent.GUILD_VOICE_STATES,
                GatewayIntent.GUILD_PRESENCES,
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.GUILD_MESSAGE_REACTIONS,
                GatewayIntent.GUILD_MESSAGE_TYPING,
                GatewayIntent.DIRECT_MESSAGES,
                GatewayIntent.DIRECT_MESSAGE_REACTIONS,
                GatewayIntent.DIRECT_MESSAGE_TYPING,
                GatewayIntent.MESSAGE_CONTENT,
                GatewayIntent.SCHEDULED_EVENTS,
                GatewayIntent.AUTO_MODERATION_CONFIGURATION,
                GatewayIntent.AUTO_MODERATION_EXECUTION,
                GatewayIntent.GUILD_MESSAGE_POLLS,
                GatewayIntent.DIRECT_MESSAGE_POLLS
        );
    }
}