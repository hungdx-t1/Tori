package com.dianxin.tori.server.commands.console;

import com.dianxin.core.api.console.commands.AbstractConsoleCommand;
import com.dianxin.tori.api.ToriProvider;
import com.dianxin.tori.api.bot.IBotLoader;

/**
 * Console command to dynamically disable (stop and unload) a bot at runtime.
 * <p>
 * Usage: disablebot BotName
 */
public class DisableBotConsoleCommand extends AbstractConsoleCommand {
    public DisableBotConsoleCommand() {
        super("disablebot");
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 1) {
            getLogger().error("❌ Usage: disablebot <bot_name>");
            getLogger().info("Example: disablebot MyBot");
            return;
        }

        String botName = args[0];

        IBotLoader botLoader = ToriProvider.getBotLoader();
        if (botLoader.disableBot(botName)) {
            getLogger().info("✅ Bot '{}' has been disabled!", botName);
        } else {
            getLogger().error("❌ Failed to disable bot '{}'. Bot not found or not active.", botName);
        }
    }
}
