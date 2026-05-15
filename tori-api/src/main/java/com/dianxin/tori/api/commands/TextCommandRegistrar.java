package com.dianxin.tori.api.commands;

import com.dianxin.tori.api.annotations.commands.TextCommand;
import com.dianxin.tori.api.commands.text.ITextCommand;
import com.dianxin.tori.api.commands.text.ModernBaseTextCommand;
import com.dianxin.tori.api.commands.text.BaseTextCommand;
import com.dianxin.tori.api.exceptions.MissingAnnotationException;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * The registrar for legacy text-based commands.
 */
@ApiStatus.Obsolete(since = "Discord Message Content Intent restrictions")
@SuppressWarnings({"unused", "LoggingSimilarMessage"})
public class TextCommandRegistrar {
    private static final Logger logger = LoggerFactory.getLogger(TextCommandRegistrar.class);

    private final String prefix;
    private final Map<String, ITextCommand> textCommandMap = new HashMap<>();

    private CommandReplyConfig replyConfig = new DefaultEnglishReplyConfig();

    /**
     * Constructs a new TextCommandRegistrar.
     *
     * @param prefix The prefix used to trigger commands (e.g., "!").
     */
    public TextCommandRegistrar(String prefix) {
        this.prefix = prefix;
    }

    /**
     * Sets the reply configuration for permission denials.
     *
     * @param config The new {@link CommandReplyConfig}.
     * @return This registrar instance.
     */
    public TextCommandRegistrar setReplyConfig(CommandReplyConfig config) {
        this.replyConfig = config;
        return this;
    }

    /**
     * Registers statically configured base text commands.
     *
     * @param cmds The commands to register.
     * @return This registrar instance.
     */
    public TextCommandRegistrar register(@NotNull BaseTextCommand... cmds) {
        if(cmds == null) return this;
        for(BaseTextCommand cmd : cmds) {
            String commandName = cmd.getCommandName().toLowerCase();
            textCommandMap.putIfAbsent(commandName, cmd);

            for (String alias : cmd.getAliases()) {
                textCommandMap.putIfAbsent(alias.toLowerCase(), cmd);
            }

            logger.debug("✅ Registered text command: **{}** {}", commandName,
                    cmd.getAliases().isEmpty() ? "" : "(Aliases: " + String.join(", ", cmd.getAliases()) + ")");
        }
        return this;
    }

    /**
     * Registers annotation-driven modern text commands.
     *
     * @param cmds The commands to register.
     * @return This registrar instance.
     */
    public TextCommandRegistrar register(@NotNull ModernBaseTextCommand... cmds) {
        if(cmds == null) return this;
        for(ModernBaseTextCommand cmd : cmds) {
            Class<?> targetClass = cmd.getClass();
            if(!targetClass.isAnnotationPresent(TextCommand.class)) {
                throw new MissingAnnotationException(TextCommand.class, targetClass);
            }

            TextCommand ann = targetClass.getAnnotation(TextCommand.class);
            @NotNull String commandName = ann.commandName().toLowerCase();
            if(commandName.isEmpty()) {
                throw new IllegalStateException("Command Name in " + targetClass.getSimpleName() +
                        " must not be empty!");
            }

            textCommandMap.putIfAbsent(commandName, cmd);

            for (String alias : ann.aliases()) {
                textCommandMap.putIfAbsent(alias.toLowerCase(), cmd);
            }

            logger.debug("✅ Registered text command: **{}** {}", commandName,
                    ann.aliases().length == 0 ? "" : "(Aliases: " + String.join(", ", ann.aliases()) + ")");
        }
        return this;
    }

    /**
     * Handles the incoming message event, parses the command, and delegates execution.
     *
     * @param event The {@link MessageReceivedEvent}.
     */
    public void onEvent(@NotNull MessageReceivedEvent event) {
        // Ignore bot messages
        if (event.getAuthor().isBot()) return;

        String message = event.getMessage().getContentRaw();
        if(!message.startsWith(prefix)) return;

        // Split by spaces (handles multiple spaces natively)
        String[] split = message.substring(prefix.length()).trim().split("\\s+");
        if (split.length == 0 || split[0].isEmpty()) return;

        String commandName = split[0].toLowerCase();
        ITextCommand command = textCommandMap.get(commandName);

        if (command == null) {
            // Optional: You could add a 'command not found' message here using replyConfig
            return;
        }

        // Extract arguments (everything after the command name)
        String[] args = Arrays.copyOfRange(split, 1, split.length);

        command.handle(event, args, replyConfig);
    }
}
