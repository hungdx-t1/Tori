package com.dianxin.tori.api.utils.jda;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.OffsetDateTime;

/**
 * A utility class providing convenient methods to extract information from and interact
 * with JDA {@link User} objects.
 * <p>
 * <b>Note:</b> Methods that retrieve users from the API or cache require this class
 * to be initialized via {@link #initialize(JDA)} first.
 */
@SuppressWarnings("unused")
public final class UserUtils {
    private static JDA jda;

    private UserUtils() { }

    /**
     * Initializes the UserUtils with a JDA instance.
     * This must be called before using {@link #retrieveUser(String)} or {@link #getCachedUser(String)}.
     *
     * @param jda The JDA instance to be used for user lookups.
     */
    public static void initialize(@NotNull JDA jda) {
        if(UserUtils.jda != null) return;
        UserUtils.jda = jda;
    }

    /**
     * Gets the mention string for the specified user.
     *
     * @param user The user to mention.
     * @return The mention string (e.g., {@code <@123456789>}).
     */
    @NotNull
    public static String mention(@NotNull User user) {
        return user.getAsMention();
    }

    /**
     * Gets the snowflake ID of the user.
     *
     * @param user The target user.
     * @return The user's ID as a String.
     */
    @NotNull
    public static String id(@NotNull User user) {
        return user.getId();
    }

    /**
     * Gets the raw username of the user.
     *
     * @param user The target user.
     * @return The user's account username.
     */
    @NotNull
    public static String username(@NotNull User user) {
        return user.getName();
    }

    /**
     * Gets the global display name of the user, if they have set one.
     *
     * @param user The target user.
     * @return The global name, or {@code null} if not set.
     */
    @Nullable
    public static String globalName(@NotNull User user) {
        return user.getGlobalName();
    }

    /**
     * Gets the effective display name of the user.
     * Returns the global name if present; otherwise, falls back to their standard username.
     *
     * @param user The target user.
     * @return The effective display name.
     */
    @NotNull
    public static String displayName(@NotNull User user) { // relative User#getEffectiveName
        String global = user.getGlobalName();
        return global != null ? global : user.getName();
    }

    /**
     * Checks if the user has configured a global display name.
     *
     * @param user The target user.
     * @return {@code true} if a global name is set, {@code false} otherwise.
     */
    public static boolean hasGlobalName(@NotNull User user) {
        return user.getGlobalName() != null;
    }

    /**
     * Gets the user's tag (Username#Discriminator).
     *
     * @param user The target user.
     * @return The user's tag.
     * @deprecated Discord has removed discriminators. Use {@link #username(User)} or {@link #displayName(User)} instead.
     */
    @NotNull
    @Deprecated
    public static String tag(@NotNull User user) {
        return user.getAsTag();
    }

    /**
     * Gets a formatted string useful for logging or debugging, combining the user's
     * display name and ID.
     *
     * @param user The target user.
     * @return A string formatted as "DisplayName (ID)".
     */
    @NotNull
    public static String debugTag(@NotNull User user) {
        return displayName(user) + " (" + user.getId() + ")";
    }

    /**
     * Gets the URL of the user's custom avatar.
     *
     * @param user The target user.
     * @return The avatar URL, or {@code null} if they are using a default avatar.
     */
    @Nullable
    public static String avatarLink(@NotNull User user) {
        return user.getAvatarUrl();
    }

    /**
     * Gets the ID hash of the user's custom avatar.
     *
     * @param user The target user.
     * @return The avatar ID, or {@code null} if they are using a default avatar.
     */
    @Nullable
    public static String avatarId(@NotNull User user) {
        return user.getAvatarId();
    }

    /**
     * Gets the effective avatar URL for the user.
     * This will return their custom avatar if present, or their default avatar otherwise.
     *
     * @param user The target user.
     * @return The effective avatar URL.
     */
    @NotNull
    public static String effectiveAvatarLink(@NotNull User user) {
        return user.getEffectiveAvatarUrl();
    }

    /**
     * Gets the URL of the user's default Discord avatar.
     *
     * @param user The target user.
     * @return The default avatar URL.
     */
    @NotNull
    public static String defaultAvatarLink(@NotNull User user) {
        return user.getDefaultAvatarUrl();
    }

    /**
     * Gets the identifier for the user's default Discord avatar.
     *
     * @param user The target user.
     * @return The default avatar ID.
     */
    @NotNull
    public static String defaultAvatarId(@NotNull User user) {
        return user.getDefaultAvatarId();
    }

    /**
     * Checks if the bot currently has an open Private Channel (DM) with this user.
     *
     * @param user The target user.
     * @return {@code true} if a private channel exists in cache.
     */
    public static boolean hasPrivateChannel(@NotNull User user) {
        return user.hasPrivateChannel();
    }

    /**
     * Retrieves the URL of the user's profile banner.
     * This operation requires an API request.
     *
     * @param user The target user.
     * @return A {@link RestAction} resolving to the banner URL, or {@code null} if no banner is set.
     */
    @NotNull
    public static RestAction<@Nullable String> bannerLink(@NotNull User user) {
        return user.retrieveProfile().map(User.Profile::getBannerUrl);
    }

    /**
     * Retrieves the ID hash of the user's profile banner.
     * This operation requires an API request.
     *
     * @param user The target user.
     * @return A {@link RestAction} resolving to the banner ID, or {@code null} if no banner is set.
     */
    @NotNull
    public static RestAction<@Nullable String> bannerId(@NotNull User user) {
        return user.retrieveProfile().map(User.Profile::getBannerId);
    }

    /**
     * Retrieves a user by their ID from the Discord API.
     * <p>
     * Requires {@link #initialize(JDA)} to have been called previously.
     *
     * @param id The snowflake ID of the user.
     * @return A {@link RestAction} resolving to the requested {@link User}.
     */
    @NotNull
    public static RestAction<User> retrieveUser(@NotNull String id) {
        return jda.retrieveUserById(id);
    }

    /**
     * Retrieves a user by their ID directly from the internal JDA cache.
     * <p>
     * Requires {@link #initialize(JDA)} to have been called previously.
     *
     * @param id The snowflake ID of the user.
     * @return The cached {@link User}, or {@code null} if the user is not in the cache.
     */
    @Nullable
    public static User getCachedUser(@NotNull String id) {
        return jda.getUserById(id);
    }

    /**
     * Gets the exact time the user's Discord account was created.
     *
     * @param user The target user.
     * @return An {@link OffsetDateTime} representing the account creation timestamp.
     */
    @NotNull
    public static OffsetDateTime createdTime(@NotNull User user) {
        return user.getTimeCreated();
    }
}