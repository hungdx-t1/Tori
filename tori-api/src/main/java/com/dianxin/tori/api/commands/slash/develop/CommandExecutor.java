package com.dianxin.tori.api.commands.slash.develop;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

class CommandExecutor {

    public static void execute(Class<?> rootClass, SlashCommandInteractionEvent event) {
        String subName = event.getSubcommandName();
        String groupName = event.getSubcommandGroup();

        Class<?> targetClass = rootClass;

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
            // Tìm method có gắn @Execute trước để kiểm tra quyền
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

            // 1. Kiểm tra Permission nếu lệnh chạy trong Guild
            Guild guild = event.getGuild();
            Member member = event.getMember();

            if (guild != null && member != null) {
                // Kiểm tra quyền của người dùng
                List<Permission> missingUserPerms = getMissingPermissions(member, config.permissions());
                if (!missingUserPerms.isEmpty()) {
                    event.reply("🚫 Bạn không có quyền thực hiện lệnh này! Cần quyền: `"
                                    + formatPermissions(missingUserPerms) + "`")
                            .setEphemeral(true).queue();
                    return;
                }

                // Kiểm tra quyền của Bot
                List<Permission> missingBotPerms = getMissingPermissions(guild.getSelfMember(), config.selfPermissions());
                if (!missingBotPerms.isEmpty()) {
                    event.reply("⚠️ Bot thiếu quyền để chạy lệnh này! Vui lòng cấp quyền: `"
                                    + formatPermissions(missingBotPerms) + "`")
                            .setEphemeral(true).queue();
                    return;
                }
            }

            // 2. Tự động Defer nếu được cấu hình
            if (config.defer() && !event.isAcknowledged()) {
                event.deferReply(config.ephemeral()).queue();
            }

            // 3. Khởi tạo instance và inject fields
            Object instance = targetClass.getDeclaredConstructor().newInstance();
            for (Field field : targetClass.getDeclaredFields()) {
                CommandOption opt = field.getAnnotation(CommandOption.class);
                if (opt == null) continue;

                OptionMapping mapping = event.getOption(opt.name());
                if (mapping == null) continue;

                field.setAccessible(true);
                field.set(instance, extractValue(field.getType(), mapping));
            }

            // 4. Thực thi method
            targetMethod.setAccessible(true);
            if (targetMethod.getParameterCount() == 1 && targetMethod.getParameterTypes()[0].isAssignableFrom(SlashCommandInteractionEvent.class)) {
                targetMethod.invoke(instance, event);
            } else {
                targetMethod.invoke(instance);
            }

        } catch (Exception e) {
            String errorMsg = "❌ Lỗi thực thi: " + e.getMessage();
            if (event.isAcknowledged()) {
                event.getHook().sendMessage(errorMsg).queue();
            } else {
                event.reply(errorMsg).setEphemeral(true).queue();
            }
        }
    }

    private static List<Permission> getMissingPermissions(Member member, Permission[] required) {
        List<Permission> missing = new ArrayList<>();
        for (Permission perm : required) {
            if (!member.hasPermission(perm)) {
                missing.add(perm);
            }
        }
        return missing;
    }

    private static String formatPermissions(List<Permission> permissions) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < permissions.size(); i++) {
            sb.append(permissions.get(i).getName());
            if (i < permissions.size() - 1) sb.append(", ");
        }
        return sb.toString();
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