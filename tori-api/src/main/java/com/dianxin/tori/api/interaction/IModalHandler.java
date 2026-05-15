package com.dianxin.tori.api.interaction;

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.modals.Modal;
import org.jetbrains.annotations.NotNull;

public interface IModalHandler {
    @NotNull Modal getModal();
    void execute(ModalInteractionEvent event);
}
