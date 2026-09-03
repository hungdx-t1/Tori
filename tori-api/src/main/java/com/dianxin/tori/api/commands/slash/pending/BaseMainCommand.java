package com.dianxin.tori.api.commands.slash.pending;

import com.dianxin.tori.api.bot.IBotMeta;
import com.dianxin.tori.api.bot.JavaDiscordBot;
import com.dianxin.tori.api.commands.LegacyCommandBuilder;
import com.dianxin.tori.api.commands.slash.ISlashCommand;
import net.dv8tion.jda.api.JDA;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseMainCommand implements ISlashCommand {
    private final Logger logger;
    private final JDA jda;
    private final IBotMeta botMeta;
    private final LegacyCommandBuilder settings;

    public BaseMainCommand(@NonNull JavaDiscordBot bot, @NonNull LegacyCommandBuilder builder) {
        this.jda = bot.getJda();
        this.botMeta = bot.getMeta();
        this.logger = LoggerFactory.getLogger(this.getClass());
        this.settings = builder;
    }
}
