package com.dianxin.tori.api.commands.slash.v2;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.util.*;

public class CommandRegistry {
    private final Map<String, BaseCommand> commands = new HashMap<>();
    private final Map<String, String> aliasMap = new HashMap<>();

    public void register(BaseCommand command) {
        commands.put(command.getName(), command);
        for (String alias : command.getAliases()) {
            aliasMap.put(alias.toLowerCase(), command.getName());
        }
    }

    public BaseCommand getCommand(String name) {
        String direct = commands.containsKey(name) ? name : aliasMap.get(name);
        return direct != null ? commands.get(direct) : null;
    }

    /** Đẩy toàn bộ Slash Command đã đăng ký lên Discord API */
    public void pushSlashCommands(JDA jda) {
        List<CommandData> slashCommands = commands.values().stream()
                .map(BaseCommand::buildSlashData)
                .map(sc -> (CommandData) sc)
                .toList();

        jda.updateCommands().addCommands(slashCommands).queue();
    }

    public Collection<BaseCommand> getAll() {
        return commands.values();
    }
}