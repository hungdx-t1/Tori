package com.dianxin.tori.api.interaction;

import com.dianxin.tori.api.bot.JavaDiscordBot;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.modals.Modal;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * A registry class responsible for managing and routing Discord Modal interactions.
 * It stores registered modal handlers and delegates incoming events to the appropriate handler based on the Modal ID.
 */
@SuppressWarnings("unused")
public class ModalRegistrar {
    private static final Logger logger = LoggerFactory.getLogger(ModalRegistrar.class);
    private final JDA jda;
    private final JavaDiscordBot bot;
    private final Map<String, IModalHandler> modalHandlerMap = new HashMap<>();

    /**
     * Constructs a new ModalRegistrar for the specified bot.
     *
     * @param bot The {@link JavaDiscordBot} instance that owns this registrar.
     */
    public ModalRegistrar(JavaDiscordBot bot) {
        this.bot = bot;
        this.jda = bot.getJda();
    }

    /**
     * Registers a new modal handler into the registry.
     *
     * @param modalHandler The {@link IModalHandler} to register.
     */
    public void register(IModalHandler modalHandler) {
        String id = modalHandler.getModal().getId();
        modalHandlerMap.put(id, modalHandler);
        logger.debug("Registered modal handler with id '{}'", id);
    }

    /**
     * Processes an incoming modal submission event and routes it to the corresponding registered handler.
     * If the ID is not found, an ephemeral error message is sent to the user.
     *
     * @param event The {@link ModalInteractionEvent} triggered by Discord.
     */
    public void onModal(ModalInteractionEvent event) {
        String id = event.getModalId();
        IModalHandler modalHandler = modalHandlerMap.get(id);
        if(modalHandler == null) {
            event.reply("This modal ID is not exist!").setEphemeral(true).queue();
            return;
        }
        modalHandler.execute(event);
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
     * Retrieves a registered Discord Modal by its custom ID.
     *
     * @param id The custom ID of the modal to retrieve.
     * @return The {@link Modal} if found, or {@code null} if not registered.
     */
    @Nullable
    public Modal getModal(String id) {
        IModalHandler modalHandler = modalHandlerMap.get(id);
        if(modalHandler == null) return null;
        return modalHandler.getModal();
    }
}