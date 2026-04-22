package com.dianxin.tori.api.archieve;

import com.dianxin.tori.api.bot.IBotMeta;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * ModernBaseCommand dành cho Text Command (Lệnh truyền thống qua tin nhắn).
 * Discord không còn ưu tiên dạng lệnh này do giới hạn của Message Content Intent.
 */
@Deprecated(forRemoval = true)
@ApiStatus.ScheduledForRemoval(inVersion = "2.2.5")
@ApiStatus.Obsolete(since = "Discord Message Content Intent restrictions")
@SuppressWarnings({"unused", "removal"})
public abstract class TextBaseCommand {
    private final Logger logger;
    private final JDA jda;
    private final IBotMeta botMeta;

    // Cấu hình lệnh
    private final String name;
    private final List<String> aliases;
    private final boolean guildOnly;
    private final boolean ownerOnly;
    private final boolean privateChannelOnly;
    private final List<Permission> permissionsRequired;
    private final List<Permission> selfPermissionsRequired;

    public TextBaseCommand(JDA jda, IBotMeta meta, TextCommandBuilder builder) {
        this.logger = LoggerFactory.getLogger(this.getClass());
        this.jda = jda;
        this.botMeta = meta;

        this.name = builder.getName();
        this.aliases = builder.getAliases();
        this.guildOnly = builder.isGuildOnly();
        this.ownerOnly = builder.isOwnerOnly();
        this.privateChannelOnly = builder.isPrivateChannelOnly();
        this.permissionsRequired = builder.getPermissionsRequired();
        this.selfPermissionsRequired = builder.getSelfPermissionsRequired();
    }

    public String getName() { return name; }
    public List<String> getAliases() { return aliases; }
    protected Logger getLogger() { return logger; }
    protected JDA getJda() { return jda; }

    /**
     * Phương thức vận hành command. Event Listener chính của bạn sẽ gọi hàm này.
     * @param event MessageReceivedEvent
     * @param args Các tham số phía sau lệnh (VD: !ban @user lý_do -> args = ["@user", "lý_do"])
     */
    public final void handle(MessageReceivedEvent event, String[] args) {
        if (!checkOwnerOnly(event)) return;
        if (!checkPrivateChannelOnly(event)) return;
        if (!checkGuildOnly(event)) return;
        if (!checkUserPermissions(event)) return;
        if (!checkBotPermissions(event)) return;

        try {
            execute(event, args);
        } catch (Exception e) {
            logger.error("❌ Lỗi khi thực thi text command {}", this.name, e);
            event.getMessage().reply("❌ Đã xảy ra lỗi hệ thống khi chạy lệnh này.").queue();
        }
    }

    // =========================================
    // Các hàm kiểm tra (Checkers)
    // Lưu ý: JDA trả về reply bằng event.getMessage().reply() thay vì event.reply()
    // =========================================

    private boolean checkOwnerOnly(MessageReceivedEvent event) {
        if (!this.ownerOnly) return true;

        if (!event.getAuthor().getId().equals(botMeta.botOwnerId())) {
            event.getMessage().reply("❌ Chỉ owner mới được dùng lệnh này.").queue();
            return false;
        }
        return true;
    }

    private boolean checkPrivateChannelOnly(MessageReceivedEvent event) {
        if (!this.privateChannelOnly) return true;

        if (event.getChannelType() == ChannelType.PRIVATE) return true;

        event.getMessage().reply("❌ Lệnh này chỉ được dùng trong DMs/Private Channel.").queue();
        return false;
    }

    private boolean checkGuildOnly(MessageReceivedEvent event) {
        if (!this.guildOnly) return true;

        if (!event.isFromGuild()) {
            event.getMessage().reply("❌ Lệnh này chỉ dùng trong server.").queue();
            return false;
        }
        return true;
    }

    private boolean checkUserPermissions(MessageReceivedEvent event) {
        if (this.permissionsRequired == null || this.permissionsRequired.isEmpty()) return true;
        if (!event.isFromGuild()) return true;

        Member member = event.getMember();
        if (member == null) return false;

        for (Permission p : this.permissionsRequired) {
            if (!member.hasPermission(p)) {
                event.getMessage().reply("❌ Bạn thiếu quyền `" + p.getName() + "`.").queue();
                return false;
            }
        }
        return true;
    }

    private boolean checkBotPermissions(MessageReceivedEvent event) {
        if (this.selfPermissionsRequired == null || this.selfPermissionsRequired.isEmpty()) return true;
        if (!event.isFromGuild()) return true;

        Guild guild = event.getGuild();
        Member self = guild.getSelfMember();

        for (Permission p : this.selfPermissionsRequired) {
            if (!self.hasPermission(p)) {
                event.getMessage().reply("❌ Bot thiếu quyền `" + p.getName() + "`.").queue();
                return false;
            }
        }
        return true;
    }

    /**
     * Hàm abstract để các class kế thừa triển khai logic lệnh.
     */
    protected abstract void execute(MessageReceivedEvent event, String[] args);
}