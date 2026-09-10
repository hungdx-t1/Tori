package com.dianxin.tori.api.commands.slash.v2.controller;

import com.dianxin.tori.api.commands.slash.v2.annotations.Command;
import com.dianxin.tori.api.commands.slash.v2.annotations.CommandOption;
import com.dianxin.tori.api.commands.slash.v2.annotations.Subcommand;
import com.dianxin.tori.api.commands.slash.v2.annotations.SubcommandGroup;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;

import java.lang.reflect.Field;

/**
 * Utility class responsible for parsing annotation-based command declarations
 * and building JDA {@link SlashCommandData} models.
 */
class CommandDataBuilder { // package private class
    private CommandDataBuilder() {
        throw new AssertionError();
    }

    /**
     * Builds a {@link SlashCommandData} instance from a root class annotated with {@link Command}.
     *
     * @param rootClass the command class to introspect
     * @return fully configured {@link SlashCommandData}
     * @throws IllegalArgumentException if the provided class is missing the {@link Command} annotation
     */
    public static SlashCommandData build(Class<?> rootClass) {
        Command cmd = rootClass.getAnnotation(Command.class);
        if (cmd == null) {
            throw new IllegalArgumentException("Class " + rootClass.getSimpleName() + " thiếu annotation @Command");
        }

        SlashCommandData slashData = Commands.slash(cmd.name(), cmd.description());
        Class<?>[] declaredClasses = rootClass.getDeclaredClasses();

        // case 1 - Command contains Subcommands or SubcommandGroups
        if (declaredClasses.length > 0) {
            for (Class<?> subClass : declaredClasses) {
                if (subClass.isAnnotationPresent(Subcommand.class)) {
                    slashData.addSubcommands(buildSubcommandData(subClass));
                } else if (subClass.isAnnotationPresent(SubcommandGroup.class)) {
                    slashData.addSubcommandGroups(buildGroupData(subClass));
                }
            }
        }

        // case 2 - Flat command without subcommands
        else {
            populateOptions(slashData, rootClass);
        }

        return slashData;
    }

    /**
     * Constructs a {@link SubcommandData} instance by scanning an annotated inner class.
     *
     * @param subClass the subcommand class
     * @return configured {@link SubcommandData}
     */
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

    /**
     * Constructs a {@link SubcommandGroupData} instance by scanning an annotated inner class.
     *
     * @param groupClass the subcommand group class
     * @return configured {@link SubcommandGroupData}
     */
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

    /**
     * Populates command options onto a top-level command.
     *
     * @param data  the slash command data to populate
     * @param clazz the class containing annotated option fields
     */
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