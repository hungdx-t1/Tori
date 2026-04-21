package com.dianxin.tori.api.utils.quicksetup;

import net.dv8tion.jda.api.entities.Activity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A utility class for parsing and constructing JDA {@link Activity} instances from raw strings.
 */
@SuppressWarnings("unused")
public final class ActivityContext {
    private static final Logger logger = LoggerFactory.getLogger(ActivityContext.class);

    private ActivityContext() { }

    /**
     * Parses the provided parameters to construct a corresponding {@link Activity} object.
     * <p>
     * If the {@code rawType} is invalid, it defaults to {@link Activity.ActivityType#PLAYING}.
     * If the type is {@code STREAMING} but the URL is missing or blank, it also falls back to {@code PLAYING}.
     *
     * @param rawType The string representation of the ActivityType (e.g., "PLAYING", "WATCHING").
     * @param context The main text/context of the activity.
     * @param url     The URL for the activity, required only if the type is {@code STREAMING}.
     * @return The constructed {@link Activity}, or {@code null} if the {@code rawType} is null.
     */
    @Nullable
    public static Activity parseActivity(@Nullable String rawType, @NotNull String context, @Nullable String url) {
        if (rawType == null) return null;

        Activity.ActivityType type;
        try {
            type = Activity.ActivityType.valueOf(rawType.toUpperCase());
        } catch (IllegalArgumentException e) {
            logger.warn("Activity type '{}' is invalid, fallback PLAYING...", rawType);
            type = Activity.ActivityType.PLAYING;
        }

        return switch (type) {
            case STREAMING -> {
                if (url == null || url.isBlank()) {
                    logger.warn("Activity STREAMING is activated but url is empty, fallback PLAYING...");
                    yield Activity.playing(context);
                }
                yield Activity.streaming(context, url);
            }

            case LISTENING  -> Activity.listening(context);
            case WATCHING   -> Activity.watching(context);
            case COMPETING  -> Activity.competing(context);
            case CUSTOM_STATUS -> Activity.customStatus(context);
            default -> Activity.playing(context);
        };
    }
}