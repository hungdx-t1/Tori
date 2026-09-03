package com.dianxin.tori.api.commands.slash.v3;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class CommandManager extends ListenerAdapter {
    private static final Logger log = LoggerFactory.getLogger(CommandManager.class);

    private final Map<String, ICommand> commands = new HashMap<>();
    private final List<CommandMiddleware> middlewares = new ArrayList<>();

    public CommandManager() {
        // Mặc định đăng ký security middleware
        registerMiddleware(new SecurityMiddleware());
    }

    public void registerCommand(ICommand... cmds) {
        for (ICommand cmd : cmds) {
            SlashInfo info = cmd.getClass().getAnnotation(SlashInfo.class);
            if (info != null) {
                commands.put(info.name().toLowerCase(), cmd);
            }
        }
    }

    public void registerMiddleware(CommandMiddleware middleware) {
        middlewares.add(middleware);
    }

    /** Đồng bộ lệnh lên Discord (Guild hoặc Global) */
    public void pushCommands(JDA jda) {
        List<CommandData> payload = commands.values().stream()
                .map(ICommand::buildCommandData)
                .map(CommandData.class::cast)
                .toList();
        jda.updateCommands().addCommands(payload).queue(
                success -> log.info("✅ Đã cập nhật {} slash commands thành công!", success.size()),
                error -> log.error("❌ Thất bại khi cập nhật commands", error)
        );
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        ICommand cmd = commands.get(event.getName().toLowerCase());
        if (cmd == null) return;

        SlashInfo info = cmd.getClass().getAnnotation(SlashInfo.class);
        if (info != null && info.autoDefer()) {
            event.deferReply(info.ephemeral()).queue();
        }

        SlashContext ctx = new SlashContext(event);

        // Chạy qua pipeline middlewares
        for (CommandMiddleware mw : middlewares) {
            if (!mw.handle(ctx, cmd)) return;
        }

        try {
            cmd.execute(ctx);
        } catch (Exception e) {
            log.error("Lỗi thực thi lệnh {}", event.getName(), e);
            ctx.reply("❌ Đã xảy ra lỗi nội bộ: " + e.getMessage());
        }
    }
}