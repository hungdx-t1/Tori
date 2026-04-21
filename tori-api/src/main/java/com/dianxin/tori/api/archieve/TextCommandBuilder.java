package com.dianxin.tori.api.archieve;

import net.dv8tion.jda.api.Permission;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Builder cấu hình cho các lệnh Text truyền thống.
 */
@Deprecated(forRemoval = true)
@ApiStatus.ScheduledForRemoval(inVersion = "2.2.5")
@SuppressWarnings("unused")
@ApiStatus.Obsolete(since = "Discord Message Content Intent restrictions")
public class TextCommandBuilder {
    private final String name;
    private final List<String> aliases = new ArrayList<>();
    private boolean guildOnly = false;
    private boolean ownerOnly = false;
    private boolean privateChannelOnly = false;
    private final List<Permission> permissionsRequired = new ArrayList<>();
    private final List<Permission> selfPermissionsRequired = new ArrayList<>();

    // Tên lệnh là bắt buộc nên đưa thẳng vào Constructor
    public TextCommandBuilder(String name) {
        this.name = name;
    }

    public TextCommandBuilder addAliases(String... aliases) {
        this.aliases.addAll(Arrays.asList(aliases));
        return this;
    }

    public TextCommandBuilder setGuildOnly(boolean guildOnly) {
        this.guildOnly = guildOnly;
        return this;
    }

    public TextCommandBuilder setOwnerOnly(boolean ownerOnly) {
        this.ownerOnly = ownerOnly;
        return this;
    }

    public TextCommandBuilder setPrivateChannelOnly(boolean privateChannelOnly) {
        this.privateChannelOnly = privateChannelOnly;
        return this;
    }

    public TextCommandBuilder addRequiredPermissions(Permission... permissions) {
        this.permissionsRequired.addAll(Arrays.asList(permissions));
        return this;
    }

    public TextCommandBuilder addSelfPermissions(Permission... permissions) {
        this.selfPermissionsRequired.addAll(Arrays.asList(permissions));
        return this;
    }

    // Getters
    public String getName() { return name; }
    public List<String> getAliases() { return aliases; }
    public boolean isGuildOnly() { return guildOnly; }
    public boolean isOwnerOnly() { return ownerOnly; }
    public boolean isPrivateChannelOnly() { return privateChannelOnly; }
    public List<Permission> getPermissionsRequired() { return permissionsRequired; }
    public List<Permission> getSelfPermissionsRequired() { return selfPermissionsRequired; }
}