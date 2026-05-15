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

@SuppressWarnings("unused")
public class ModalRegistrar {
    private static final Logger logger = LoggerFactory.getLogger(ModalRegistrar.class);
    private final JDA jda;
    private final JavaDiscordBot bot;
    private final Map<String, IModalHandler> modalHandlerMap =  new HashMap<>();

    public ModalRegistrar(JavaDiscordBot bot) {
        this.bot = bot;
        this.jda = bot.getJda();
    }

    public void register(IModalHandler modalHandler) {
        String id = modalHandler.getModal().getId();
        modalHandlerMap.put(id, modalHandler);
        logger.debug("Registered modal handler with id '{}'", id);

    }

    public void onModal(ModalInteractionEvent event) {
        String id = event.getModalId();
        IModalHandler modalHandler = modalHandlerMap.get(id);
        if(modalHandler == null) {
            event.reply("This modal ID is not exist!").setEphemeral(true).queue();
            return;
        }
        modalHandler.execute(event);
    }

    public JDA getJda() {
        return jda;
    }

    public JavaDiscordBot getBaseBot() {
        return bot;
    }

    @Nullable
    public Modal getModal(String id) {
        IModalHandler modalHandler = modalHandlerMap.get(id);
        if(modalHandler == null) return null;
        return modalHandler.getModal();
    }
}
