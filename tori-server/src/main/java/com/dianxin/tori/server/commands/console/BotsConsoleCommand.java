package com.dianxin.tori.server.commands.console;

import com.dianxin.core.api.console.commands.AbstractConsoleCommand;
import com.dianxin.tori.api.bot.IBotMeta;
import com.dianxin.tori.api.bot.JavaDiscordBot;
import com.dianxin.tori.server.Main;

import java.util.List;

public class BotsConsoleCommand extends AbstractConsoleCommand {
    public BotsConsoleCommand() {
        super("bots");
    }

    @Override
    public void execute(String[] args) {
        List<JavaDiscordBot> bots = Main.getServer().getBotLoader().getActiveBots();
        if (bots.isEmpty()) {
            getLogger().info("📦 No bots are loaded on this server.");
            return;
        }

        // use StringBuilder to format list
        StringBuilder sb = new StringBuilder();
        sb.append("📦 Running bots (").append(bots.size()).append("):\n");

        for (JavaDiscordBot bot : bots) {
            IBotMeta meta = bot.getMeta();
            sb.append("  ✅ ").append(meta.botName())
                    .append(" v").append(meta.botVersion())
                    .append(" | Author: ").append(meta.botAuthor());

            if (meta.botDescription() != null && !meta.botDescription().isBlank()) {
                sb.append(" - ").append(meta.botDescription());
            }
            sb.append("\n");
        }

        getLogger().info(sb.toString());
    }
}
