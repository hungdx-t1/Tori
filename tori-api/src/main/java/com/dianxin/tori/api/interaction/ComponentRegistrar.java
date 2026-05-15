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

/**
 * A registry class responsible for managing and routing Discord UI Component interactions
 * such as Buttons, String Select Menus, and Entity Select Menus.
 */
@SuppressWarnings("unused")
public class ComponentRegistrar {
    private static final Logger logger = LoggerFactory.getLogger(ComponentRegistrar.class);
    private final JDA jda;
    private final JavaDiscordBot bot;

    private final Map<String, IStringSelectMenuHandler> stringSelectMenuHandlerMap = new HashMap<>();
    private final Map<String, IEntitySelectMenuHandler> entitySelectMenuHandlerMap = new HashMap<>();
    private final Map<String, IButtonHandler> buttonHandlerMap = new HashMap<>();

    /**
     * Constructs a new ComponentRegistrar for the specified bot.
     *
     * @param bot The {@link JavaDiscordBot} instance that owns this registrar.
     */
    public ComponentRegistrar(JavaDiscordBot bot) {
        this.bot = bot;
        this.jda = bot.getJda();
    }

    /**
     * Registers a new entity select menu handler into the registry.
     *
     * @param handler The {@link IEntitySelectMenuHandler} to register.
     */
    public void register(IEntitySelectMenuHandler handler) {
        String id = handler.getEntitySelectMenu().getCustomId();
        entitySelectMenuHandlerMap.put(id, handler);
        logger.debug("Registered entity select menu with id '{}'", id);
    }

    /**
     * Registers a new string select menu handler into the registry.
     *
     * @param handler The {@link IStringSelectMenuHandler} to register.
     */
    public void register(IStringSelectMenuHandler handler) {
        String id = handler.getStringSelectMenu().getCustomId();
        stringSelectMenuHandlerMap.put(id, handler);
        logger.debug("Registered string select menu with id '{}'", id);
    }

    /**
     * Registers a new button handler into the registry.
     *
     * @param handler The {@link IButtonHandler} to register.
     */
    public void register(IButtonHandler handler) {
        String id = handler.getButton().getCustomId();
        buttonHandlerMap.put(id, handler);
        logger.debug("Registered button handler with id '{}'", id);
    }

    /**
     * Processes an incoming entity select menu event and routes it to the corresponding registered handler.
     *
     * @param event The {@link EntitySelectInteractionEvent} triggered by Discord.
     */
    public void onEntity(EntitySelectInteractionEvent event) {
        String id = event.getComponentId();
        IEntitySelectMenuHandler entitySelectMenu = entitySelectMenuHandlerMap.get(id);
        if(entitySelectMenu == null) {
            event.reply("This interaction ID is not exist!").setEphemeral(true).queue();
            return;
        }
        entitySelectMenu.execute(event);
    }

    /**
     * Processes an incoming string select menu event and routes it to the corresponding registered handler.
     *
     * @param event The {@link StringSelectInteractionEvent} triggered by Discord.
     */
    public void onString(StringSelectInteractionEvent event) {
        String id = event.getComponentId();
        IStringSelectMenuHandler stringSelectMenu = stringSelectMenuHandlerMap.get(id);
        if(stringSelectMenu == null) {
            event.reply("This interaction ID is not exist!").setEphemeral(true).queue();
            return;
        }
        stringSelectMenu.execute(event);
    }

    /**
     * Processes an incoming button click event and routes it to the corresponding registered handler.
     *
     * @param event The {@link ButtonInteractionEvent} triggered by Discord.
     */
    public void onButton(ButtonInteractionEvent event) {
        String id = event.getComponentId();
        IButtonHandler buttonHandler = buttonHandlerMap.get(id);
        if(buttonHandler == null) {
            event.reply("This interaction ID is not exist!").setEphemeral(true).queue();
            return;
        }
        buttonHandler.execute(event);
    }

    /**
     * Gets the JDA instance associated with this registrar.
     *
     * @return The {@link JDA} instance.
     */
    public JDA getJda() {
        return jda;
    }

    /**
     * Gets the base bot instance associated with this registrar.
     *
     * @return The {@link JavaDiscordBot} instance.
     */
    public JavaDiscordBot getBaseBot() {
        return bot;
    }

    /**
     * Retrieves a registered String Select Menu by its custom ID.
     *
     * @param id The custom ID of the menu to retrieve.
     * @return The {@link StringSelectMenu} if found, or {@code null} if not registered.
     */
    @Nullable
    public StringSelectMenu getStringSelectMenu(String id) {
        var selectMenu = stringSelectMenuHandlerMap.get(id);
        if(selectMenu == null) return null;
        return selectMenu.getStringSelectMenu();
    }

    /**
     * Retrieves a registered Entity Select Menu by its custom ID.
     *
     * @param id The custom ID of the menu to retrieve.
     * @return The {@link EntitySelectMenu} if found, or {@code null} if not registered.
     */
    @Nullable
    public EntitySelectMenu getEntitySelectMenu(String id) {
        var selectMenu = entitySelectMenuHandlerMap.get(id);
        if(selectMenu == null) return null;
        return selectMenu.getEntitySelectMenu();
    }
}