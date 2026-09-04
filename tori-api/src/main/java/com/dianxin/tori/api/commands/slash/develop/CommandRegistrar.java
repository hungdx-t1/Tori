package com.dianxin.tori.api.commands.slash.develop;

import com.dianxin.tori.api.bot.JavaDiscordBot;
import com.dianxin.tori.api.exceptions.MissingAnnotationException;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class CommandRegistrar extends ListenerAdapter {
    private static final Logger log = LoggerFactory.getLogger(CommandRegistrar.class);

    private final JavaDiscordBot bot;
    private final Map<String, Class<?>> registeredCommands = new HashMap<>();

    public CommandRegistrar(JavaDiscordBot bot) {
        this.bot = bot;
        bot.getJda().addEventListener(this);
    }

    /**
     * Đăng ký một hoặc nhiều Class lệnh (có gắn @Command)
     */
    public void registerSlash(Class<?>... commandClasses) {
        for (Class<?> clazz : commandClasses) {
            Command meta = clazz.getAnnotation(Command.class);
            if (meta == null) {
                MissingAnnotationException ex = new MissingAnnotationException(Command.class, clazz);
                log.warn("Cannot register command with class {}, ignoring...", clazz.getSimpleName(), ex);
                continue;
            }
            registeredCommands.put(meta.name().toLowerCase(Locale.ROOT), clazz);
            log.debug("Slash Command /{} registered successfully.", meta.name());
        }
    }

    /**
     * Đẩy danh sách lệnh lên Discord:
     * - Nếu targetGuild != null: cập nhật cho riêng Guild đó (cập nhật tức thì, thích hợp debug).
     * - Nếu targetGuild == null: cập nhật toàn cầu (Global Commands).
     */
    public void commitAllCommands(@Nullable Guild... targetGuilds) {
        List<SlashCommandData> payload = new ArrayList<>();

        for (Class<?> clazz : registeredCommands.values()) {
            try {
                SlashCommandData data = CommandDataBuilder.build(clazz);
                payload.add(data);
            } catch (Exception e) {
                log.error("Lỗi khi build metadata cho lệnh {}", clazz.getSimpleName(), e);
            }
        }

        if (targetGuilds != null) {
            List<Guild> guilds = Arrays.stream(targetGuilds).toList();
            if (!guilds.isEmpty()) {
                for (Guild guild : guilds) {
                    guild.updateCommands().addCommands(payload).queue(
                            success -> log.info("✅ Đã đồng bộ {} Slash Command cho Guild: {}", success.size(), guild.getName()),
                            error -> log.error("❌ Thất bại khi đồng bộ lệnh cho Guild: {}", guild.getName(), error)
                    );
                }
            }
        } else {
            bot.getJda().updateCommands().addCommands(payload).queue(
                    success -> log.info("✅ Đã đồng bộ {} Slash Command toàn cầu!", success.size()),
                    error -> log.error("❌ Thất bại khi đồng bộ lệnh toàn cầu", error)
            );
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String cmdName = event.getName().toLowerCase(Locale.ROOT);
        Class<?> targetClass = registeredCommands.get(cmdName);

        if (targetClass != null) {
            // Chuyển toàn bộ việc inject field và execute sang CommandExecutor
            CommandExecutor.execute(targetClass, event);
        }
    }
}