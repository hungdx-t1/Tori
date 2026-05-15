package com.dianxin.tori.api.interaction;

import com.dianxin.tori.api.bot.JavaDiscordBot;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class ComponentRegistrar {
    private static final Logger logger = LoggerFactory.getLogger(ComponentRegistrar.class);
    private final JDA jda;
    private final JavaDiscordBot bot;

    private final Map<String, IStringSelectMenuHandler> stringSelectMenuHandlerMap = new HashMap<>();
    private final Map<String, IEntitySelectMenuHandler> entitySelectMenuHandlerMap = new HashMap<>();
    private final Map<String, IButtonHandler> buttonHandlerMap = new HashMap<>();

    public ComponentRegistrar(JavaDiscordBot bot) {
        this.bot = bot;
        this.jda = bot.getJda();
    }

    public void register(IEntitySelectMenuHandler handler) {
        String id = handler.getEntitySelectMenu().getCustomId();
        entitySelectMenuHandlerMap.put(id, handler);
        logger.debug("Registered entity select menu with id '{}'", id);
    }

    public void register(IStringSelectMenuHandler handler) {
        String id = handler.getStringSelectMenu().getCustomId();
        stringSelectMenuHandlerMap.put(id, handler);
        logger.debug("Registered string select menu with id '{}'", id);
    }

    public void register(IButtonHandler handler) {
        String id = handler.getButton().getCustomId();
        buttonHandlerMap.put(id, handler);
        logger.debug("Registered button handler with id '{}'", id);
    }

    public void onEntity(EntitySelectInteractionEvent event) {
        String id = event.getComponentId();
        IEntitySelectMenuHandler entitySelectMenu = entitySelectMenuHandlerMap.get(id);
        if(entitySelectMenu == null) {
            event.reply("This interaction ID is not exist!").setEphemeral(true).queue();
            return;
        }
        entitySelectMenu.execute(event);
    }

    public void onString(StringSelectInteractionEvent event) {
        String id = event.getComponentId();
        IStringSelectMenuHandler stringSelectMenu = stringSelectMenuHandlerMap.get(id);
        if(stringSelectMenu == null) {
            event.reply("This interaction ID is not exist!").setEphemeral(true).queue();
            return;
        }
        stringSelectMenu.execute(event);
    }

    public void onButton(ButtonInteractionEvent event) {
        String id = event.getComponentId();
        IButtonHandler buttonHandler = buttonHandlerMap.get(id);
        if(buttonHandler == null) {
            event.reply("This interaction ID is not exist!").setEphemeral(true).queue();
            return;
        }
        buttonHandler.execute(event);
    }

    public JDA getJda() {
        return jda;
    }

    public JavaDiscordBot getBaseBot() {
        return bot;
    }

    @Nullable
    public StringSelectMenu getStringSelectMenu(String id) {
        var selectMenu = stringSelectMenuHandlerMap.get(id);
        if(selectMenu == null) return null;
        return selectMenu.getStringSelectMenu();
    }

    @Nullable
    public EntitySelectMenu getEntitySelectMenu(String id) {
        var selectMenu = entitySelectMenuHandlerMap.get(id);
        if(selectMenu == null) return null;
        return selectMenu.getEntitySelectMenu();
    }
}