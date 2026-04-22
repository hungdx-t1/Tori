package com.dianxin.tori.api.commands.messagecontext;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseMessageContextMenu implements IMessageContextMenu {
    protected final Logger logger;

    private final String title;
    private final JDA jda;

    public BaseMessageContextMenu(String title, JDA jda) {
        this.title = title;
        this.jda = jda;
        this.logger = LoggerFactory.getLogger(getClass());
    }

    protected JDA getJda() {
        return jda;
    }

    public String getTitle() {
        return title;
    }

    public void execute(UserContextInteractionEvent event) {

    }
}
