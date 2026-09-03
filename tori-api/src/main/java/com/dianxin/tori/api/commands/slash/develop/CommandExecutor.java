package com.dianxin.tori.api.commands.slash.develop;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class CommandExecutor {

    public static void execute(Class<?> cmdClass, SlashCommandInteractionEvent event) {
        try {
            // 1. Tạo instance mới cho mỗi lần gọi (tránh thread-safety issue)
            Object instance = cmdClass.getDeclaredConstructor().newInstance();

            // 2. Inject dữ liệu từ SlashCommandInteractionEvent vào các Field
            for (Field field : cmdClass.getDeclaredFields()) {
                CommandOption opt = field.getAnnotation(CommandOption.class);
                if (opt == null) continue;

                OptionMapping mapping = event.getOption(opt.name());
                if (mapping == null) continue;

                field.setAccessible(true);
                Object val = extractValue(field.getType(), mapping);
                if (val != null) {
                    field.set(instance, val);
                }
            }

            // 3. Tìm method có @Execute để chạy
            for (Method method : cmdClass.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Execute.class)) {
                    Execute execConfig = method.getAnnotation(Execute.class);
                    if (execConfig.defer()) {
                        event.deferReply(execConfig.ephemeral()).queue();
                    }

                    method.setAccessible(true);
                    // Hỗ trợ method có nhận tham số event hoặc không
                    if (method.getParameterCount() == 1 && method.getParameterTypes()[0].isAssignableFrom(SlashCommandInteractionEvent.class)) {
                        method.invoke(instance, event);
                    } else if (method.getParameterCount() == 0) {
                        method.invoke(instance);
                    }
                    return;
                }
            }
        } catch (Exception e) {
            event.getHook().sendMessage("❌ Lỗi thực thi lệnh: " + e.getMessage()).queue();
        }
    }

    /** Tự map kiểu dữ liệu của Field sang JDA OptionMapping */
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