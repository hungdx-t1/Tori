package com.dianxin.tori.api.commands.slash.v2.controller;

import com.dianxin.tori.api.bot.JavaDiscordBot;
import com.dianxin.tori.api.commands.slash.v2.annotations.Command;
import com.dianxin.tori.api.commands.slash.v2.example.ExampleCommand;
import com.dianxin.tori.api.commands.slash.v2.example.WarnCommand;
import com.dianxin.tori.api.exceptions.MissingAnnotationException;
import com.dianxin.tori.base.annotations.ReleasedSince;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Manages registration, synchronization, and interaction routing
 * for annotation-driven slash commands in the Tori framework.
 *
 * <p>For practical implementation patterns, refer to the reference examples:</p>
 * <ul>
 *   <li>{@link ExampleCommand} - Basic flat slash command declaration with optional parameters.</li>
 *   <li>{@link WarnCommand} - Advanced structure using nested static subcommands with permission restrictions.</li>
 * </ul>
 *
 * @see ExampleCommand
 * @see WarnCommand
 */
@ReleasedSince("26.8.301")
@SuppressWarnings("unused")
public class CommandRegistrar extends ListenerAdapter {
    private static final Logger log = LoggerFactory.getLogger(CommandRegistrar.class);

    private final JavaDiscordBot bot;
    private final Map<String, Class<?>> registeredCommands = new HashMap<>();

    /**
     * Initializes the registrar and hooks it into the bot's JDA event pipeline.
     *
     * @param bot the bot instance owning this registrar
     */
    public CommandRegistrar(JavaDiscordBot bot) {
        this.bot = bot;
        bot.getJda().addEventListener(this);
    }

    /**
     * Registers one or more command classes annotated with {@link Command}.
     *
     * @param commandClasses the command classes to register
     */
    public void registerSlash(Class<?>... commandClasses) {
        for (Class<?> clazz : commandClasses) {
            Command meta = clazz.getAnnotation(Command.class);
            if (meta == null) {
                MissingAnnotationException ex = new MissingAnnotationException(Command.class, clazz);
                log.warn("Cannot register command class {}, ignoring...", clazz.getSimpleName(), ex);
                continue;
            }
            registeredCommands.put(meta.name().toLowerCase(Locale.ROOT), clazz);
            log.debug("Slash Command /{} registered successfully.", meta.name());
        }
    }

    /**
     * Commits all registered commands to Discord.
     *
     * <p>If target guilds are specified, updates commands exclusively for those guilds
     * (immediate update, ideal for development/testing). If empty or null, commands are
     * published globally.</p>
     *
     * @param targetGuilds optional guild(s) for guild-scoped updates; pass empty or {@code null} for global
     */
    public void commitAllCommands(@Nullable Guild... targetGuilds) {
        List<SlashCommandData> payload = new ArrayList<>();

        for (Class<?> clazz : registeredCommands.values()) {
            try {
                SlashCommandData data = CommandDataBuilder.build(clazz);
                payload.add(data);
            } catch (Exception e) {
                log.error("Failed to build metadata for command {}", clazz.getSimpleName(), e);
            }
        }

        if (targetGuilds != null && targetGuilds.length > 0) {
            for (Guild guild : targetGuilds) {
                if (guild == null) continue;
                guild.updateCommands().addCommands(payload).queue(
                        success -> log.info("Successfully synchronized {} slash command(s) for guild: {}", success.size(), guild.getName()),
                        error -> log.error("Failed to synchronize commands for guild: {}", guild.getName(), error)
                );
            }
        } else {
            bot.getJda().updateCommands().addCommands(payload).queue(
                    success -> log.info("Successfully synchronized {} global slash command(s)!", success.size()),
                    error -> log.error("Failed to synchronize global slash commands.", error)
            );
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String cmdName = event.getName().toLowerCase(Locale.ROOT);
        Class<?> targetClass = registeredCommands.get(cmdName);

        if (targetClass != null) {
            CommandExecutor.execute(targetClass, event);
        }
    }
}