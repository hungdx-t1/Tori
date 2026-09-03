package com.dianxin.tori.api.commands.slash.v2;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.*;

public abstract class BaseCommand {
    private final String name;
    private final String description;
    private final List<String> aliases = new ArrayList<>();
    private final Set<Permission> requiredPermissions = EnumSet.noneOf(Permission.class);
    private boolean guildOnly = false;
    private boolean autoDefer = false;

    public BaseCommand(String name, String description) {
        this.name = name.toLowerCase();
        this.description = description;
    }

    // Builder helpers
    public BaseCommand addAliases(String... aliases) {
        this.aliases.addAll(Arrays.asList(aliases));
        return this;
    }

    public BaseCommand requirePermissions(Permission... perms) {
        this.requiredPermissions.addAll(Arrays.asList(perms));
        return this;
    }

    public BaseCommand setGuildOnly(boolean guildOnly) {
        this.guildOnly = guildOnly;
        return this;
    }

    public BaseCommand setAutoDefer(boolean autoDefer) {
        this.autoDefer = autoDefer;
        return this;
    }

    /** Tạo đối tượng Slash Command tương thích với JDA */
    public SlashCommandData buildSlashData() {
        SlashCommandData data = Commands.slash(this.name, this.description);
        //data.setGuildOnly(this.guildOnly);
        return data;
    }

    /** Phương thức thực thi bắt buộc của lệnh */
    public abstract void execute(ICommandContext ctx);

    // Getters
    public String getName() { return name; }
    public String getDescription() { return description; }
    public List<String> getAliases() { return aliases; }
    public Set<Permission> getRequiredPermissions() { return requiredPermissions; }
    public boolean isGuildOnly() { return guildOnly; }
    public boolean isAutoDefer() { return autoDefer; }
}