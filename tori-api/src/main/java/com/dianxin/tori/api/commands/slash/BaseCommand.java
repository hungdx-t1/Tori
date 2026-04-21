package com.dianxin.tori.api.commands.slash;

import com.dianxin.tori.api.annotations.commands.*;
import com.dianxin.tori.api.bot.IBotMeta;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unused")
public abstract class BaseCommand {
    private final Logger logger;
    private final JDA jda;
    private final IBotMeta botMeta;

    /**
     * Khởi tạo Base Command, sử dụng jda thủ công
     * @param jda JDA thủ công được truyền vào
     * @param meta Bot Meta thủ công được truyền vòa
     */
    public BaseCommand(JDA jda, IBotMeta meta) {
        this.logger = LoggerFactory.getLogger(this.getClass());
        this.jda = jda;
        this.botMeta = meta;
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
    // begin of checker

    private boolean checkOwnerOnly(SlashCommandInteractionEvent event) {
        if (!getClass().isAnnotationPresent(OwnerOnly.class)) return true;

        if(!event.getUser().getId().equals(botMeta.getBotOwnerId())) {
            event.reply("❌ Chỉ owner mới được dùng lệnh này.").setEphemeral(true).queue();
            return false;
        }

        return true;
    }

    private boolean checkDMOnly(SlashCommandInteractionEvent event) {
        if(!getClass().isAnnotationPresent(DirectMessageOnly.class)) return true;

        if(event.getGuild() != null) {
            event.reply("❌ Lệnh này chỉ được dùng khi DMs (nhắn riêng).").setEphemeral(true).queue();
            return false;
        }

        return true;
    }

    private boolean checkPrivateChannelOnly(SlashCommandInteractionEvent event) {
        if(!getClass().isAnnotationPresent(PrivateChannelOnly.class)) return true;
        if (event.getChannelType() == ChannelType.PRIVATE) return true;
        event.reply("❌ Lệnh này chỉ được dùng trong DMs/Private Channel.").setEphemeral(true).queue();
        return false;
    }

    private boolean checkGuildOnly(SlashCommandInteractionEvent event) {
        if (!getClass().isAnnotationPresent(GuildOnly.class)) return true;

        if (event.getGuild() == null) {
            event.reply("❌ Lệnh này chỉ dùng trong server.").setEphemeral(true).queue();
            return false;
        }
        return true;
    }

    private boolean checkUserPermissions(SlashCommandInteractionEvent event) {
        RequirePermissions ann = getClass().getAnnotation(RequirePermissions.class);
        if (ann == null) return true;

        Member member = event.getMember();
        if (member == null) {
            event.reply("⚠️ Không xác định được người dùng.").setEphemeral(true).queue();
            return false;
        }

        for (Permission p : ann.value()) {
            if (!member.hasPermission(p)) {
                event.reply("❌ Bạn thiếu quyền `" + p.getName() + "`.").setEphemeral(true).queue();
                return false;
            }
        }
        return true;
    }

    private boolean checkBotPermissions(SlashCommandInteractionEvent event) {
        RequireSelfPermissions ann = getClass().getAnnotation(RequireSelfPermissions.class);
        if (ann == null) return true;

        Guild guild = event.getGuild();
        if (guild == null) return false;

        Member self = guild.getSelfMember();

        for (Permission p : ann.value()) {
            if (!self.hasPermission(p)) {
                event.reply("❌ Bot thiếu quyền `" + p.getName() + "`.").setEphemeral(true).queue();
                return false;
            }
        }
        return true;
    }

    private void applyDeferIfNeeded(SlashCommandInteractionEvent event) {
        if (getClass().isAnnotationPresent(DeferReply.class)) {
            event.deferReply().queue();
        }
    }

    private void logDebug(SlashCommandInteractionEvent event) {
        if (!getClass().isAnnotationPresent(DebugCommand.class)) return;

        logger.debug("[CMD] {} by {} | {}",
                event.getName(),
                event.getUser().getAsTag(),
                event.getCommandString()
        );
    }

    // abstract func
    protected abstract void execute(SlashCommandInteractionEvent event);
}
