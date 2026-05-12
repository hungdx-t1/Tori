package com.dianxin.tori.server.commands.console;

import com.dianxin.core.api.console.commands.AbstractConsoleCommand;
import com.dianxin.tori.api.ToriProvider;
import com.dianxin.tori.api.bot.IBotLoader;

/**
 * Console command to dynamically enable (load and start) a bot at runtime.
 * <p>
 * Usage: enablebot MyBot.jar
 */
public class EnableBotConsoleCommand extends AbstractConsoleCommand {
    public EnableBotConsoleCommand() {
        super("enablebot");
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 1) {
            getLogger().error("❌ Usage: enablebot <bot_file.jar>");
            getLogger().info("Example: enablebot MyBot.jar");
            return;
        }

        String jarFileName = args[0];

        if (!jarFileName.endsWith(".jar")) {
            getLogger().error("❌ Bot file must be a JAR file (*.jar)");
            return;
        }

        IBotLoader botLoader = ToriProvider.getBotLoader();
        if (botLoader.enableBot(jarFileName)) {
            getLogger().info("✅ Bot '{}' is being loaded and started...", jarFileName);
        } else {
            getLogger().error("❌ Failed to enable bot '{}'", jarFileName);
        }
    }
}
