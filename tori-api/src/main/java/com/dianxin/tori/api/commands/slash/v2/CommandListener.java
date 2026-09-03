package com.dianxin.tori.api.commands.slash.v2;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class CommandListener extends ListenerAdapter {
    private final CommandRegistry registry;
    private final String prefix;

    public CommandListener(CommandRegistry registry, String prefix) {
        this.registry = registry;
        this.prefix = prefix;
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        BaseCommand cmd = registry.getCommand(event.getName());
        if (cmd == null) return;

       // ICommandContext ctx = new SlashCommandContext(event);
        //handleExecution(cmd, ctx);
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        String raw = event.getMessage().getContentRaw();
        if (!raw.startsWith(prefix)) return;

        String[] parts = raw.substring(prefix.length()).trim().split("\\s+", 2);
        String label = parts[0].toLowerCase();

        BaseCommand cmd = registry.getCommand(label);
        if (cmd == null) return;

        String args = parts.length > 1 ? parts[1] : "";
       // ICommandContext ctx = new TextCommandContext(event, args);
      //  handleExecution(cmd, ctx);
    }

    private void handleExecution(BaseCommand cmd, ICommandContext ctx) {
        // 1. Kiểm tra Guild Only
        if (cmd.isGuildOnly() && ctx.getGuild() == null) {
            ctx.reply("❌ Lệnh này chỉ có thể sử dụng trong Server!");
            return;
        }

        // 2. Kiểm tra Permissions của User
        if (ctx.getMember() != null && !cmd.getRequiredPermissions().isEmpty()) {
            for (Permission perm : cmd.getRequiredPermissions()) {
                if (!ctx.getMember().hasPermission(perm)) {
                    ctx.reply("🚫 Bạn thiếu quyền `" + perm.getName() + "` để dùng lệnh này.");
                    return;
                }
            }
        }

        // 3. Tự động Defer nếu được cấu hình
        if (cmd.isAutoDefer()) {
            ctx.defer(false);
        }

        // 4. Chạy lệnh
        try {
            cmd.execute(ctx);
        } catch (Exception ex) {
            ctx.reply("⚠️ Đã xảy ra lỗi nội bộ khi chạy lệnh: " + ex.getMessage());
        }
    }
}