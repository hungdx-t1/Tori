package com.dianxin.tori.api.commands.slash.develop;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class CommandExecutor {

    public static void execute(Class<?> rootClass, SlashCommandInteractionEvent event) {
        String subName = event.getSubcommandName();
        String groupName = event.getSubcommandGroup();

        Class<?> targetClass = rootClass;

        // Nếu lệnh có Subcommand, tìm đúng Inner Class tương ứng
        if (subName != null) {
            targetClass = findSubcommandClass(rootClass, groupName, subName);
            if (targetClass == null) {
                event.reply("❌ Không tìm thấy bộ xử lý cho subcommand: " + subName).setEphemeral(true).queue();
                return;
            }
        }

        dispatch(targetClass, event);
    }

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

    private static void dispatch(Class<?> targetClass, SlashCommandInteractionEvent event) {
        try {
            Object instance = targetClass.getDeclaredConstructor().newInstance();

            // Inject các Field từ Option
            for (Field field : targetClass.getDeclaredFields()) {
                CommandOption opt = field.getAnnotation(CommandOption.class);
                if (opt == null) continue;

                OptionMapping mapping = event.getOption(opt.name());
                if (mapping == null) continue;

                field.setAccessible(true);
                field.set(instance, extractValue(field.getType(), mapping));
            }

            // Thực thi method @Execute
            for (Method method : targetClass.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Execute.class)) {
                    Execute config = method.getAnnotation(Execute.class);
                    if (config.defer() && !event.isAcknowledged()) {
                        event.deferReply(config.ephemeral()).queue();
                    }

                    method.setAccessible(true);
                    if (method.getParameterCount() == 1 && method.getParameterTypes()[0].isAssignableFrom(SlashCommandInteractionEvent.class)) {
                        method.invoke(instance, event);
                    } else {
                        method.invoke(instance);
                    }
                    return;
                }
            }
        } catch (Exception e) {
            event.getHook().sendMessage("❌ Lỗi thực thi subcommand: " + e.getMessage()).queue();
        }
    }

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