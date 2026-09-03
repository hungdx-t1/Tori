package com.dianxin.tori.api.commands.slash.pending;

import com.dianxin.tori.api.bot.JavaDiscordBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandRegistrarV2 {
    private static final Logger LOGGER = LoggerFactory.getLogger("CommandRegistrarV2");
    private final JavaDiscordBot bot;

    public CommandRegistrarV2(JavaDiscordBot bot) {
        this.bot = bot;
    }

    public <T extends Command4Bot> boolean registerCommand(T command) {
        return true;
    }

    public static class SubCommandFamily {
        private void test() {
        }
    }
}
