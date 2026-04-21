package com.dianxin.tori.api.utils.jda;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import org.jetbrains.annotations.NotNull;

/**
 * A utility class providing convenient methods to retrieve JDA {@link Channel} objects
 * from the internal cache.
 * <p>
 * <b>Note:</b> This class must be initialized via {@link #initialize(JDA)} before use.
 */
@SuppressWarnings({"unused"})
public final class ChannelUtils {
    private static JDA jda;

    private ChannelUtils() { }

    /**
     * Initializes the ChannelUtils with a JDA instance.
     * This must be called before attempting to retrieve any channels.
     *
     * @param jda The global JDA instance.
     */
    public static void initialize(@NotNull JDA jda) {
        if(ChannelUtils.jda != null) return;
        ChannelUtils.jda = jda;
    }

    /**
     * Retrieves a generic {@link Channel} by its snowflake ID from the JDA cache.
     *
     * @param id The snowflake ID of the channel.
     * @return The {@link Channel}, or {@code null} if not found in cache.
     */
    public static Channel getChannelById(String id) {
        return jda.getChannelById(Channel.class, id);
    }

    /**
     * Retrieves a specific {@link TextChannel} by its snowflake ID from the JDA cache.
     *
     * @param id The snowflake ID of the text channel.
     * @return The {@link TextChannel}, or {@code null} if not found in cache or if the ID belongs to a different channel type.
     */
    public static TextChannel getTextChannelById(String id) {
        return jda.getTextChannelById(id);
    }

    /**
     * Retrieves a specific {@link VoiceChannel} by its snowflake ID from the JDA cache.
     *
     * @param id The snowflake ID of the voice channel.
     * @return The {@link VoiceChannel}, or {@code null} if not found in cache or if the ID belongs to a different channel type.
     */
    public static VoiceChannel getVoiceChannelById(String id) {
        return jda.getVoiceChannelById(id);
    }
}