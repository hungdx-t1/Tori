package com.dianxin.tori.api.commands;

import com.dianxin.tori.api.annotations.contextmenu.ContextMenu;
import com.dianxin.tori.api.bot.JavaDiscordBot;
import com.dianxin.tori.api.commands.messagecontext.BaseMessageContextMenu;
import com.dianxin.tori.api.commands.messagecontext.IMessageContextMenu;
import com.dianxin.tori.api.commands.messagecontext.ModernBaseMessageContextMenu;
import com.dianxin.tori.api.commands.registry.MaincommandRegistry;
import com.dianxin.tori.api.commands.slash.BaseCommand;
import com.dianxin.tori.api.commands.slash.LegacyBaseCommand;
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

@SuppressWarnings({"removal", "unused", "FieldCanBeLocal", "ExtractMethodRecommender", "LoggingSimilarMessage"})
public class CommandRegistrar {
    private static final Logger logger = LoggerFactory.getLogger(CommandRegistrar.class);
    private final JDA jda;
    private final JavaDiscordBot bot;

    // Lưu trữ các lệnh đã đăng ký.
    // Key là tên lệnh (vd: "play", "ban"), Value là class thực thi lệnh đó.
    private final Map<String, LegacyBaseCommand> slashCmds = new HashMap<>();
    private final Map<String, IUserContextMenu> userContextCmds = new HashMap<>();
    private final Map<String, IMessageContextMenu> messageContextCmds = new HashMap<>();

    private final AtomicBoolean commitedAll = new AtomicBoolean(false);

    public CommandRegistrar(JavaDiscordBot bot) {
        this.jda = bot.getJda();
        this.bot = bot;
    }

    /**
     * Đăng ký một hoặc nhiều lệnh vào bộ nhớ của bot.
     */
    public CommandRegistrar registerSlash(LegacyBaseCommand... cmdInstances) {
        if (commitedAll.get()) {
            throw new IllegalStateException("Cannot register more command after you've commited all!");
        }

        for (LegacyBaseCommand cmd : cmdInstances) {
            if (cmd instanceof MaincommandRegistry registry) {
                String commandName = registry.getCommand().getName(); // Tự động lấy tên lệnh từ CommandData để làm Key
                slashCmds.put(commandName, cmd);
            } else {
                throw new IllegalArgumentException("Lệnh " + cmd.getClass().getSimpleName() + " phải implements MaincommandRegistry!");
            }
        }
        return this;
    }

    @ApiStatus.AvailableSince("26.2.226")
    public CommandRegistrar registerSlash(BaseCommand... cmds) {
        // TODO
        return this;
    }

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
     * Gửi toàn bộ danh sách lệnh lên máy chủ Discord.
     */
    public void commitAllCommands(@Nullable Guild guild) {
        if (commitedAll.getAndSet(true)) return; // prevent call twice

        CommandListUpdateAction updateAction = guild == null ? jda.updateCommands() : guild.updateCommands();
        List<CommandData> commandDataList = new ArrayList<>();

        for (LegacyBaseCommand cmd : slashCmds.values()) {
            CommandData data = ((MaincommandRegistry) cmd).getCommand();
            commandDataList.add(data);
        }

        for (Map.Entry<String, IUserContextMenu> entry1 : userContextCmds.entrySet()) {
            CommandData data = Commands.message(entry1.getKey());
            commandDataList.add(data);
        }

        for (Map.Entry<String, IMessageContextMenu> entry2 : messageContextCmds.entrySet()) {
            CommandData data = Commands.message(entry2.getKey());
            commandDataList.add(data);
        }

        updateAction.addCommands(commandDataList).queue(
                commands -> System.out.println("✅ Đã cập nhật thành công " + commandDataList.size() + " lệnh lên Discord!"),
                error -> System.err.println("❌ Lỗi cập nhật lệnh: " + error.getMessage())
        );
    }

    // ===== Handle events =====

    /**
     * Hàm này sẽ được gọi từ một ListenerAdapter để xử lý khi có người dùng gõ lệnh.
     */
    public void onSlashCommandEvent(SlashCommandInteractionEvent event) {
        String commandName = event.getName();

        LegacyBaseCommand command = slashCmds.get(commandName);
        if (command == null) {
            event.reply("❌ This command is not exist or is not loaded on machine.").setEphemeral(true).queue();
            return;
        }

        command.handle(event); // execute command
    }

    public void onUserContextEvent(UserContextInteractionEvent event) {
        IUserContextMenu menu = userContextCmds.get(event.getName());
        if (menu == null) {
            event.reply("❌ This command is not exist or is not loaded on machine.").setEphemeral(true).queue();
            return;
        }

        menu.execute(event);
    }

    public void onMessageContextEvent(MessageContextInteractionEvent event) {
        IMessageContextMenu menu = messageContextCmds.get(event.getName());
        if (menu == null) {
            event.reply("❌ This command is not exist or is not loaded on machine.").setEphemeral(true).queue();
            return;
        }

        menu.execute(event);
    }
}
