package com.dianxin.tori.api.commands.slash.v2.controller;

import com.dianxin.tori.api.commands.slash.v2.annotations.CommandOption;
import com.dianxin.tori.api.commands.slash.v2.annotations.Execute;
import com.dianxin.tori.api.commands.slash.v2.annotations.Subcommand;
import com.dianxin.tori.api.commands.slash.v2.annotations.SubcommandGroup;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles the dispatching, dependency injection, permission checking,
 * and execution of slash command classes.
 */
class CommandExecutor { // package private class
    private CommandExecutor() {
        throw new AssertionError();
    }

    /**
     * Dispatches an incoming slash command interaction to the appropriate class handler.
     *
     * @param rootClass the root command class
     * @param event     the received interaction event
     */
    public static void execute(Class<?> rootClass, SlashCommandInteractionEvent event) {
        String subName = event.getSubcommandName();
        String groupName = event.getSubcommandGroup();

        Class<?> targetClass = rootClass;

        if (subName != null) {
            targetClass = findSubcommandClass(rootClass, groupName, subName);
            if (targetClass == null) {
                event.reply("❌ Handler not found for subcommand: " + subName).setEphemeral(true).queue();
                return;
            }
        }

        dispatch(targetClass, event);
    }

    /**
     * Traverses declared inner classes to locate the matching subcommand handler.
     *
     * @param root  the base class to search from
     * @param group the subcommand group name (if any)
     * @param sub   the subcommand name
     * @return the matching target class, or {@code null} if none match
     */
    private static Class<?> findSubcommandClass(Class<?> root, String group, String sub) {
        for (Class<?> inner : root.getDeclaredClasses()) {
            if (group != null && inner.isAnnotationPresent(SubcommandGroup.class)) {
                SubcommandGroup g = inner.getAnnotation(SubcommandGroup.class);
                if (g.name().equalsIgnoreCase(group)) {
                    return findSubcommandClass(inner, null, sub);
                }
            } else if (inner.isAnnotationPresent(Subcommand.class)) {
                Subcommand s = inner.getAnnotation(Subcommand.class);
                if (s.name().equalsIgnoreCase(sub)) {
                    return inner;
                }
            }
        }
        return null;
    }

    /**
     * Validates permissions, instantiates the target handler, injects option fields,
     * and invokes the execution entrypoint.
     *
     * @param targetClass the class containing the {@link Execute} method
     * @param event       the interaction event
     */
    private static void dispatch(Class<?> targetClass, SlashCommandInteractionEvent event) {
        try {
            // find method has @Execute first to check perm
            Method targetMethod = null;
            Execute config = null;
            for (Method method : targetClass.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Execute.class)) {
                    targetMethod = method;
                    config = method.getAnnotation(Execute.class);
                    break;
                }
            }

            if (targetMethod == null) return;

            // 1. Permission checks when executed within a guild
            Guild guild = event.getGuild();
            Member member = event.getMember();

            if (guild != null && member != null) {
                // Check user permissions
                List<Permission> missingUserPerms = getMissingPermissions(member, config.permissions());
                if (!missingUserPerms.isEmpty()) {
                    event.reply("🚫 You lack the required permission(s) to execute this command: `"
                                    + formatPermissions(missingUserPerms) + "`")
                            .setEphemeral(true).queue();
                    return;
                }

                // Check bot self permissions
                List<Permission> missingBotPerms = getMissingPermissions(guild.getSelfMember(), config.selfPermissions());
                if (!missingBotPerms.isEmpty()) {
                    event.reply("⚠️ The bot lacks required permission(s) to run this command: `"
                                    + formatPermissions(missingBotPerms) + "`")
                            .setEphemeral(true).queue();
                    return;
                }
            }

            // 2. Automatic deferral if configured
            if (config.defer() && !event.isAcknowledged()) {
                event.deferReply(config.ephemeral()).queue();
            }

            // 3. Instantiate and inject annotated fields
            Object instance = targetClass.getDeclaredConstructor().newInstance();
            for (Field field : targetClass.getDeclaredFields()) {
                CommandOption opt = field.getAnnotation(CommandOption.class);
                if (opt == null) continue;

                OptionMapping mapping = event.getOption(opt.name());
                if (mapping == null) continue;

                field.setAccessible(true);
                field.set(instance, extractValue(field.getType(), mapping));
            }

            // 4. Invoke target execution method
            targetMethod.setAccessible(true);
            if (targetMethod.getParameterCount() == 1 && targetMethod.getParameterTypes()[0].isAssignableFrom(SlashCommandInteractionEvent.class)) {
                targetMethod.invoke(instance, event);
            } else {
                targetMethod.invoke(instance);
            }

        } catch (Exception e) {
            String errorMsg = "❌ Execution failure: " + e.getMessage();
            if (event.isAcknowledged()) {
                event.getHook().sendMessage(errorMsg).queue();
            } else {
                event.reply(errorMsg).setEphemeral(true).queue();
            }
        }
    }

    /**
     * Determines which permissions the specified member is missing.
     *
     * @param member   the member to verify
     * @param required the array of required permissions
     * @return a list of missing permissions
     */
    private static List<Permission> getMissingPermissions(Member member, Permission[] required) {
        List<Permission> missing = new ArrayList<>();
        for (Permission perm : required) {
            if (!member.hasPermission(perm)) {
                missing.add(perm);
            }
        }
        return missing;
    }

    /**
     * Formats a list of permissions into a human-readable comma-separated string.
     *
     * @param permissions the permissions to format
     * @return comma-separated permission names
     */
    private static String formatPermissions(List<Permission> permissions) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < permissions.size(); i++) {
            sb.append(permissions.get(i).getName());
            if (i < permissions.size() - 1) sb.append(", ");
        }
        return sb.toString();
    }

    /**
     * Extracts and converts the option mapping value to the target field type.
     *
     * @param targetType the expected field type
     * @param mapping    the option mapping supplied by JDA
     * @return converted value, or {@code null} if unsupported
     */
    private static Object extractValue(Class<?> targetType, OptionMapping mapping) {
        if (targetType == String.class) return mapping.getAsString();
        if (targetType == Long.class || targetType == long.class) return mapping.getAsLong();
        if (targetType == Integer.class || targetType == int.class) return mapping.getAsInt();
        if (targetType == Boolean.class || targetType == boolean.class) return mapping.getAsBoolean();
        if (targetType == net.dv8tion.jda.api.entities.Member.class) return mapping.getAsMember();
        if (targetType == net.dv8tion.jda.api.entities.User.class) return mapping.getAsUser();
        if (targetType == net.dv8tion.jda.api.entities.Role.class) return mapping.getAsRole();
        if (targetType == net.dv8tion.jda.api.entities.channel.Channel.class) return mapping.getAsChannel();
        return null;
    }
}