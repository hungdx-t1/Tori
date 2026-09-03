package com.dianxin.tori.api.commands.slash.develop;

import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.lang.reflect.Field;

public class CommandDataBuilder {
    public static SlashCommandData build(Class<?> clazz) {
        Command cmd = clazz.getAnnotation(Command.class);
        if (cmd == null) {
            throw new IllegalArgumentException("Class " + clazz.getSimpleName() + " thiếu annotation @Command");
        }

        SlashCommandData data = Commands.slash(cmd.name(), cmd.description());

        for (Field field : clazz.getDeclaredFields()) {
            CommandOption opt = field.getAnnotation(CommandOption.class);
            if (opt != null) {
                String desc = opt.description().isBlank() ? opt.name() : opt.description();
                data.addOption(opt.type(), opt.name(), desc, opt.required());
            }
        }
        return data;
    }
}