package com.dianxin.tori.base.lifecycle;

import org.jetbrains.annotations.ApiStatus;

/**
 * Base Interface with any Exception Handler.
 * @param <E> Exception type that this handler could handing.
 * @param <C> Context (ví dụ: SlashCommandInteractionEvent) to reply to Discord.
 */
@ApiStatus.Experimental
@FunctionalInterface
@SuppressWarnings("unused")
public interface BotExceptionHandler<E extends Throwable, C> {

    /**
     * @param exception An exception
     * @param context Environment that can cause exception (ex: Discord event)
     */
    void handle(E exception, C context);
}