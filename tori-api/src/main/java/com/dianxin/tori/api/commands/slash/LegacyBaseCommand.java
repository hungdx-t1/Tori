package com.dianxin.tori.api.commands.slash;

import com.dianxin.tori.api.bot.IBotMeta;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * BaseCommand phiên bản tối ưu hóa hiệu suất tĩnh.
 * Loại bỏ hoàn toàn quá trình quét Annotation (Reflection) để đảm bảo tốc độ
 * thực thi nhanh nhất, tránh lỗi "Bot is thinking" quá 3 giây của Discord.
 *
 */
@SuppressWarnings("unused")
public abstract class LegacyBaseCommand {
    private final Logger logger;
    private final JDA jda;
    private final IBotMeta botMeta;

    // Cấu hình lệnh
    private final boolean isDefer; // mặc định false
    private final boolean guildOnly; // mặc định false
    private final boolean ownerOnly; // mặc định false
    private final boolean privateChannelOnly; // mặc định false
    private final boolean directMessageOnly; // (DM = direct message), mặc định false
    private final List<Permission> selfPermissionsRequired; // nullable
    private final List<Permission> permissionsRequired; // nullable
    private final boolean isDebug; // mặc định false

    /**
     * Khởi tạo cấu hình lệnh với JDA và BotMeta thủ công
     */
    public LegacyBaseCommand(JDA jda, IBotMeta meta, boolean isDefer, boolean guildOnly, boolean ownerOnly,
                             boolean privateChannelOnly, boolean directMessageOnly,
                             List<Permission> permissionsRequired,
                             List<Permission> selfPermissionsRequired,
                             boolean isDebug) {
        this.logger = LoggerFactory.getLogger(this.getClass());
        this.jda = jda;
        this.botMeta = meta;

        this.isDefer = isDefer;
        this.guildOnly = guildOnly;
        this.ownerOnly = ownerOnly;
        this.privateChannelOnly = privateChannelOnly;
        this.directMessageOnly = directMessageOnly;
        this.permissionsRequired = permissionsRequired;
        this.selfPermissionsRequired = selfPermissionsRequired;
        this.isDebug = isDebug;
    }

    /**
     * Khởi tạo cấu hình lệnh thông qua Builder với JDA và BotMeta thủ công
     */
    public LegacyBaseCommand(JDA jda, IBotMeta meta, LegacyCommandBuilder builder) {
        this.logger = LoggerFactory.getLogger(this.getClass());
        this.jda = jda;
        this.botMeta = meta;

        // Trích xuất cấu hình từ Builder
        this.isDefer = builder.isDefer();
        this.guildOnly = builder.isGuildOnly();
        this.ownerOnly = builder.isOwnerOnly();
        this.privateChannelOnly = builder.isPrivateChannelOnly();
        this.directMessageOnly = builder.isDirectMessageOnly();
        this.permissionsRequired = builder.getPermissionsRequired();
        this.selfPermissionsRequired = builder.getSelfPermissionsRequired();
        this.isDebug = builder.isDebug();
    }

    /**
     * @return Logger của command hiện tại
     */
    protected Logger getLogger() {
        return logger;
    }

    /**
     * @return Java discord bot chính
     */
    protected JDA getJda() {
        return jda;
    }

    /**
     * Phương thức vận hành command
     * @param event SlashCommandInteractionEvent được truyền
     */
    public final void handle(SlashCommandInteractionEvent event) {
        if (!checkOwnerOnly(event)) return;
        if (!checkDMOnly(event)) return;
        if (!checkPrivateChannelOnly(event)) return;
        if (!checkGuildOnly(event)) return;
        if (!checkUserPermissions(event)) return;
        if (!checkBotPermissions(event)) return;

        applyDeferIfNeeded(event);

        try {
            execute(event);
        } catch (Exception e) {
            logger.error("❌ Lỗi khi thực thi command {}", event.getName(), e);
        }

        logDebug(event);
    }

    // =========================================
    // Begin of Checkers
    // =========================================

    private boolean checkOwnerOnly(SlashCommandInteractionEvent event) {
        if (!this.ownerOnly) return true;

        if (!event.getUser().getId().equals(botMeta.getBotOwnerId())) {
            event.reply("❌ Chỉ owner mới được dùng lệnh này.").setEphemeral(true).queue();
            return false;
        }

        return true;
    }

    private boolean checkDMOnly(SlashCommandInteractionEvent event) {
        if (!this.directMessageOnly) return true;

        if (event.getGuild() != null) {
            event.reply("❌ Lệnh này chỉ được dùng khi DMs (nhắn riêng).").setEphemeral(true).queue();
            return false;
        }

        return true;
    }

    private boolean checkPrivateChannelOnly(SlashCommandInteractionEvent event) {
        if (!this.privateChannelOnly) return true;

        if (event.getChannelType() == ChannelType.PRIVATE) return true;

        event.reply("❌ Lệnh này chỉ được dùng trong DMs/Private Channel.").setEphemeral(true).queue();
        return false;
    }

    private boolean checkGuildOnly(SlashCommandInteractionEvent event) {
        if (!this.guildOnly) return true;

        if (event.getGuild() == null) {
            event.reply("❌ Lệnh này chỉ dùng trong server.").setEphemeral(true).queue();
            return false;
        }
        return true;
    }

    private boolean checkUserPermissions(SlashCommandInteractionEvent event) {
        if (this.permissionsRequired == null || this.permissionsRequired.isEmpty()) return true;

        Member member = event.getMember();
        if (member == null) {
            event.reply("⚠️ Không xác định được người dùng.").setEphemeral(true).queue();
            return false;
        }

        for (Permission p : this.permissionsRequired) {
            if (!member.hasPermission(p)) {
                event.reply("❌ Bạn thiếu quyền `" + p.getName() + "`.").setEphemeral(true).queue();
                return false;
            }
        }
        return true;
    }

    private boolean checkBotPermissions(SlashCommandInteractionEvent event) {
        if (this.selfPermissionsRequired == null || this.selfPermissionsRequired.isEmpty()) return true;

        Guild guild = event.getGuild();
        if (guild == null) return false;

        Member self = guild.getSelfMember();

        for (Permission p : this.selfPermissionsRequired) {
            if (!self.hasPermission(p)) {
                event.reply("❌ Bot thiếu quyền `" + p.getName() + "`.").setEphemeral(true).queue();
                return false;
            }
        }
        return true;
    }

    private void applyDeferIfNeeded(SlashCommandInteractionEvent event) {
        if (this.isDefer) {
            event.deferReply().queue();
        }
    }

    private void logDebug(SlashCommandInteractionEvent event) {
        if (!this.isDebug) return;

        logger.debug("[CMD] {} by {} | {}",
                event.getName(),
                event.getUser().getAsTag(),
                event.getCommandString()
        );
    }

    // abstract func
    protected abstract void execute(SlashCommandInteractionEvent event);

    /**
     * Lớp hỗ trợ xây dựng cấu hình cho LegacyBaseCommand.
     * Giúp code gọn gàng và dễ đọc hơn khi khởi tạo lệnh.
     */
    public static class LegacyCommandBuilder {
        private boolean isDefer = false;
        private boolean guildOnly = false;
        private boolean ownerOnly = false;
        private boolean privateChannelOnly = false;
        private boolean directMessageOnly = false;
        private boolean isDebug = false;
        private final List<Permission> permissionsRequired = new ArrayList<>();
        private final List<Permission> selfPermissionsRequired = new ArrayList<>();

        // Các hàm Setter mang phong cách Fluent Interface (Return this)

        public LegacyCommandBuilder setDefer(boolean defer) {
            this.isDefer = defer;
            return this;
        }

        public LegacyCommandBuilder setGuildOnly(boolean guildOnly) {
            this.guildOnly = guildOnly;
            return this;
        }

        public LegacyCommandBuilder setOwnerOnly(boolean ownerOnly) {
            this.ownerOnly = ownerOnly;
            return this;
        }

        public LegacyCommandBuilder setPrivateChannelOnly(boolean privateChannelOnly) {
            this.privateChannelOnly = privateChannelOnly;
            return this;
        }

        public LegacyCommandBuilder setDirectMessageOnly(boolean directMessageOnly) {
            this.directMessageOnly = directMessageOnly;
            return this;
        }

        public LegacyCommandBuilder setDebug(boolean debug) {
            this.isDebug = debug;
            return this;
        }

        public LegacyCommandBuilder addRequiredPermissions(Permission... permissions) {
            this.permissionsRequired.addAll(Arrays.asList(permissions));
            return this;
        }

        public LegacyCommandBuilder addSelfPermissions(Permission... permissions) {
            this.selfPermissionsRequired.addAll(Arrays.asList(permissions));
            return this;
        }

        // Các hàm Getter để BaseCommand đọc cấu hình
        public boolean isDefer() { return isDefer; }
        public boolean isGuildOnly() { return guildOnly; }
        public boolean isOwnerOnly() { return ownerOnly; }
        public boolean isPrivateChannelOnly() { return privateChannelOnly; }
        public boolean isDirectMessageOnly() { return directMessageOnly; }
        public boolean isDebug() { return isDebug; }
        public List<Permission> getPermissionsRequired() { return permissionsRequired; }
        public List<Permission> getSelfPermissionsRequired() { return selfPermissionsRequired; }

        public static LegacyCommandBuilder deferAndOnlyGuild() {
            return new LegacyCommandBuilder().setDefer(true).setGuildOnly(true);
        }

        public static LegacyCommandBuilder guildAdminOnly() {
            return new LegacyCommandBuilder().setGuildOnly(false)
                    .addRequiredPermissions(Permission.ADMINISTRATOR);
        }
    }
}
