package com.dianxin.tori.api.utils;

import net.dv8tion.jda.api.entities.Activity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unused")
public final class ActivityContext {
    private static final Logger logger = LoggerFactory.getLogger(ActivityContext.class);

    private ActivityContext() { }

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
