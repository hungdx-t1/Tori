package com.dianxin.tori.api.commands.slash.develop;

import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;

import java.lang.reflect.Field;

class CommandDataBuilder {
    private CommandDataBuilder() {
        throw new AssertionError();
    }

    public static SlashCommandData build(Class<?> rootClass) {
        Command cmd = rootClass.getAnnotation(Command.class);
        if (cmd == null) {
            throw new IllegalArgumentException("Class " + rootClass.getSimpleName() + " thiếu annotation @Command");
        }

        SlashCommandData slashData = Commands.slash(cmd.name(), cmd.description());
        Class<?>[] declaredClasses = rootClass.getDeclaredClasses();

        // TRƯỜNG HỢP 1: Lệnh có Subcommands / SubcommandGroups
        if (declaredClasses.length > 0) {
            for (Class<?> subClass : declaredClasses) {
                if (subClass.isAnnotationPresent(Subcommand.class)) {
                    slashData.addSubcommands(buildSubcommandData(subClass));
                } else if (subClass.isAnnotationPresent(SubcommandGroup.class)) {
                    slashData.addSubcommandGroups(buildGroupData(subClass));
                }
            }
        }
        // TRƯỜNG HỢP 2: Lệnh đơn thuần không có subcommands
        else {
            populateOptions(slashData, rootClass);
        }

        return slashData;
    }

    private static SubcommandData buildSubcommandData(Class<?> subClass) {
        Subcommand sub = subClass.getAnnotation(Subcommand.class);
        SubcommandData subData = new SubcommandData(sub.name(), sub.description());

        for (Field field : subClass.getDeclaredFields()) {
            CommandOption opt = field.getAnnotation(CommandOption.class);
            if (opt != null) {
                String desc = opt.description().isBlank() ? opt.name() : opt.description();
                subData.addOption(opt.type(), opt.name(), desc, opt.required());
            }
        }
        return subData;
    }

    private static SubcommandGroupData buildGroupData(Class<?> groupClass) {
        SubcommandGroup group = groupClass.getAnnotation(SubcommandGroup.class);
        SubcommandGroupData groupData = new SubcommandGroupData(group.name(), group.description());

        for (Class<?> nestedSub : groupClass.getDeclaredClasses()) {
            if (nestedSub.isAnnotationPresent(Subcommand.class)) {
                groupData.addSubcommands(buildSubcommandData(nestedSub));
            }
        }
        return groupData;
    }

    private static void populateOptions(SlashCommandData data, Class<?> clazz) {
        for (Field field : clazz.getDeclaredFields()) {
            CommandOption opt = field.getAnnotation(CommandOption.class);
            if (opt != null) {
                String desc = opt.description().isBlank() ? opt.name() : opt.description();
                data.addOption(opt.type(), opt.name(), desc, opt.required());
            }
        }
    }
}