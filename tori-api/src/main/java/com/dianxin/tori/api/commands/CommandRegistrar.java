package com.dianxin.tori.api.commands;

import com.dianxin.tori.api.annotations.contextmenu.ContextMenu;
import com.dianxin.tori.api.bot.JavaDiscordBot;
import com.dianxin.tori.api.commands.messagecontext.BaseMessageContextMenu;
import com.dianxin.tori.api.commands.messagecontext.IMessageContextMenu;
import com.dianxin.tori.api.commands.messagecontext.ModernBaseMessageContextMenu;
import com.dianxin.tori.api.commands.registry.MaincommandRegistry;
import com.dianxin.tori.api.commands.slash.BaseCommand;
import com.dianxin.tori.api.commands.slash.ModernBaseCommand;
import com.dianxin.tori.api.commands.usercontext.BaseUserContextMenu;
import com.dianxin.tori.api.commands.usercontext.IUserContextMenu;
import com.dianxin.tori.api.commands.usercontext.ModernBaseUserContextMenu;
import com.dianxin.tori.api.exceptions.MissingAnnotationException;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The central registrar for all Discord interactions including Slash Commands,
 * User Context Menus, and Message Context Menus.
 * <p>
 * This class handles loading the commands into the bot's memory, validating their
 * annotations/metadata, committing them to the Discord API, and routing incoming
 * interaction events to their respective execution classes.
 */
@SuppressWarnings({"unused", "FieldCanBeLocal", "ExtractMethodRecommender", "LoggingSimilarMessage"})
public class CommandRegistrar {
    private static final Logger logger = LoggerFactory.getLogger(CommandRegistrar.class);
    private final JDA jda;
    private final JavaDiscordBot bot;

    private CommandReplyConfig replyConfig = new DefaultEnglishReplyConfig();

    private final Map<String, BaseCommand> slashCmds = new HashMap<>();
    private final Map<String, IUserContextMenu> userContextCmds = new HashMap<>();
    private final Map<String, IMessageContextMenu> messageContextCmds = new HashMap<>();

    private final AtomicBoolean commitedAll = new AtomicBoolean(false);

    public CommandRegistrar(JavaDiscordBot bot) {
        this.jda = bot.getJda();
        this.bot = bot;
    }

    /**
     * Sets the custom reply configuration for command interactions.
     * <p>
     * This configuration allows developers to customize the messages sent by the registrar
     * when command requirements—such as those defined by annotations like {@code @OwnerOnly}
     * or {@code @RequirePermissions}—are not met.
     *
     * @param config The {@link CommandReplyConfig} instance containing the custom messages.
     * @return This {@link CommandRegistrar} instance to allow for method chaining.
     */
    public CommandRegistrar setReplyConfig(CommandReplyConfig config) {
        this.replyConfig = config;
        return this;
    }

    /**
     * Registers one or more legacy slash commands into the system.
     *
     * @param cmdInstances An array of {@link BaseCommand} instances to register.
     * @return This {@link CommandRegistrar} instance for method chaining.
     * @throws IllegalStateException    If commands have already been committed to Discord.
     * @throws IllegalArgumentException If a command does not implement {@link MaincommandRegistry}.
     */
    public CommandRegistrar registerSlash(BaseCommand... cmdInstances) {
        if (commitedAll.get()) {
            throw new IllegalStateException("Cannot register more command after you've commited all!");
        }

        for (BaseCommand cmd : cmdInstances) {
            if (cmd instanceof MaincommandRegistry registry) {
                String commandName = registry.getCommand().getName();
                slashCmds.put(commandName, cmd);
            } else {
                throw new IllegalArgumentException("Command " + cmd.getClass().getSimpleName() + " must implements MaincommandRegistry!");
            }
        }
        return this;
    }

    @ApiStatus.AvailableSince("26.2.226")
    public CommandRegistrar registerSlash(ModernBaseCommand... cmds) {
        // TODO
        throw new UnsupportedOperationException("Will support soon!");
        // return this;
    }

    /**
     * Registers one or more base user context menus.
     *
     * @param contextMenu An array of {@link BaseUserContextMenu} instances.
     * @return This {@link CommandRegistrar} instance for method chaining.
     * @throws IllegalStateException If commands have already been committed.
     */
    public CommandRegistrar registerUserContext(BaseUserContextMenu... contextMenu) {
        if (commitedAll.get()) {
            throw new IllegalStateException("Cannot register more command after you've commited all!");
        }

        for (BaseUserContextMenu cmd : contextMenu) {
            String title = cmd.getTitle();
            userContextCmds.putIfAbsent(title, cmd);
            logger.info("✅ Registered user context menu: **{}**", title);
        }

        return this;
    }

    /**
     * Registers one or more modern user context menus.
     * Modern context menus rely on the {@link ContextMenu} annotation for metadata.
     *
     * @param contextMenu An array of {@link ModernBaseUserContextMenu} instances.
     * @return This {@link CommandRegistrar} instance for method chaining.
     * @throws IllegalStateException      If commands have already been committed, or if the interaction name is empty.
     * @throws MissingAnnotationException If the required {@link ContextMenu} annotation is absent.
     */
    public CommandRegistrar registerUserContext(ModernBaseUserContextMenu... contextMenu) {
        if (commitedAll.get()) {
            throw new IllegalStateException("Cannot register more command after you've commited all!");
        }

        for (ModernBaseUserContextMenu cmd : contextMenu) {
            Class<?> tClass = cmd.getClass();
            if(!tClass.isAnnotationPresent(ContextMenu.class)) {
                throw new MissingAnnotationException(ContextMenu.class, tClass);
            }

            ContextMenu contextMenu1 = tClass.getAnnotation(ContextMenu.class);
            String interactionName = contextMenu1.interactionName();
            if(interactionName.isEmpty()) {
                throw new IllegalStateException("Interaction Name trong " + tClass.getSimpleName() +
                        " must not be empty!");
            }

            userContextCmds.putIfAbsent(interactionName, cmd);
            logger.info("✅ Registered user context menu: **{}**", interactionName);
        }

        return this;
    }

    /**
     * Registers one or more base message context menus.
     *
     * @param contextMenus An array of {@link BaseMessageContextMenu} instances.
     * @return This {@link CommandRegistrar} instance for method chaining.
     * @throws IllegalStateException If commands have already been committed.
     */
    public CommandRegistrar registerMessageContext(BaseMessageContextMenu... contextMenus) {
        if (commitedAll.get()) {
            throw new IllegalStateException("Cannot register more command after you've commited all!");
        }

        for (BaseMessageContextMenu cmd : contextMenus) {
            String title = cmd.getTitle();
            messageContextCmds.putIfAbsent(title, cmd);
            logger.info("✅ Registered message context menu: **{}**", title);
        }

        return this;
    }

    /**
     * Registers one or more modern message context menus.
     * Modern context menus rely on the {@link ContextMenu} annotation for metadata.
     *
     * @param contextMenus An array of {@link ModernBaseMessageContextMenu} instances.
     * @return This {@link CommandRegistrar} instance for method chaining.
     * @throws IllegalStateException      If commands have already been committed, or if the interaction name is empty.
     * @throws MissingAnnotationException If the required {@link ContextMenu} annotation is absent.
     */
    public CommandRegistrar registerMessageContext(ModernBaseMessageContextMenu... contextMenus) {
        if (commitedAll.get()) {
            throw new IllegalStateException("Cannot register more command after you've commited all!");
        }

        for (ModernBaseMessageContextMenu cmd : contextMenus) {
            Class<?> tClass = cmd.getClass();
            if(!tClass.isAnnotationPresent(ContextMenu.class)) {
                throw new MissingAnnotationException(ContextMenu.class, tClass);
            }

            ContextMenu contextMenu1 = tClass.getAnnotation(ContextMenu.class);
            String interactionName = contextMenu1.interactionName();
            if(interactionName.isEmpty()) {
                throw new IllegalStateException("Interaction Name trong " + tClass.getSimpleName() +
                        " must not be empty!");
            }

            messageContextCmds.putIfAbsent(interactionName, cmd);
            logger.info("✅ Registered message context menu: **{}**", interactionName);
        }

        return this;
    }

    // ===== Commit commands =====

    /**
     * Commits all registered commands (Slash, User Context, Message Context) to the Discord API.
     * <p>
     * If a guild is provided, the commands are registered instantly to that specific guild.
     * If the guild is {@code null}, they are registered globally (which may take up to an hour to cache on Discord's end).
     *
     * @param guild The target {@link Guild} to register commands to, or {@code null} for global registration.
     */
    public void commitAllCommands(@Nullable Guild guild) {
        if (commitedAll.getAndSet(true)) return; // prevent call twice

        CommandListUpdateAction updateAction = guild == null ? jda.updateCommands() : guild.updateCommands();
        List<CommandData> commandDataList = new ArrayList<>();

        for (BaseCommand cmd : slashCmds.values()) {
            CommandData data = ((MaincommandRegistry) cmd).getCommand();
            commandDataList.add(data);
        }

        for (Map.Entry<String, IUserContextMenu> entry1 : userContextCmds.entrySet()) {
            CommandData data = Commands.user(entry1.getKey());
            commandDataList.add(data);
        }

        for (Map.Entry<String, IMessageContextMenu> entry2 : messageContextCmds.entrySet()) {
            CommandData data = Commands.message(entry2.getKey());
            commandDataList.add(data);
        }

        updateAction.addCommands(commandDataList).queue(
                commands -> System.out.println("✅ Updated " + commandDataList.size() + " commands on Discord!"),
                error -> System.err.println("❌ An error occured when updating commands!" + error.getMessage())
        );
    }

    // ===== Handle events =====

    /**
     * Routes an incoming slash command event to the appropriate registered command handler.
     *
     * @param event The {@link SlashCommandInteractionEvent} triggered by Discord.
     */
    public void onSlashCommandEvent(SlashCommandInteractionEvent event) {
        String commandName = event.getName();

        BaseCommand command = slashCmds.get(commandName);
        if (command == null) {
            event.reply("❌ This command is not exist or is not loaded on machine.").setEphemeral(true).queue();
            return;
        }

        command.handle(event, replyConfig); // execute command
    }

    /**
     * Routes an incoming user context menu event to the appropriate registered handler.
     *
     * @param event The {@link UserContextInteractionEvent} triggered by Discord.
     */
    public void onUserContextEvent(UserContextInteractionEvent event) {
        IUserContextMenu menu = userContextCmds.get(event.getName());
        if (menu == null) {
            event.reply("❌ This command is not exist or is not loaded on machine.").setEphemeral(true).queue();
            return;
        }

        menu.execute(event);
    }

    /**
     * Routes an incoming message context menu event to the appropriate registered handler.
     *
     * @param event The {@link MessageContextInteractionEvent} triggered by Discord.
     */
    public void onMessageContextEvent(MessageContextInteractionEvent event) {
        IMessageContextMenu menu = messageContextCmds.get(event.getName());
        if (menu == null) {
            event.reply("❌ This command is not exist or is not loaded on machine.").setEphemeral(true).queue();
            return;
        }

        menu.execute(event);
    }
}
