package com.dianxin.tori.api.commands.slash.v2.annotations;

import com.dianxin.tori.base.annotations.ReleasedSince;
import net.dv8tion.jda.api.Permission;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Identifies the handler method to invoke when an interaction is triggered.
 *
 * <p>The annotated method can either take zero parameters or accept a single
 * {@link net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent} argument.</p>
 */
@ReleasedSince("26.8.301")
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Execute {

    /**
     * Indicates whether the framework should automatically defer the interaction before executing the method.
     * Prevents interaction timeout errors for long-running operations.
     *
     * @return {@code true} to automatically call {@code deferReply()}, defaults to {@code true}
     */
    boolean defer() default true;

    /**
     * Defines whether the deferred response should be visible only to the command invoker (ephemeral).
     * Ignored if {@link #defer()} is set to {@code false}.
     *
     * @return {@code true} for ephemeral deferral, defaults to {@code false}
     */
    boolean ephemeral() default false;

    /**
     * The required permissions the invoking member must possess in the guild to run this command.
     *
     * @return an array of required user permissions, defaults to empty
     */
    Permission[] permissions() default {};

    /**
     * The required permissions the bot itself must possess in the guild to execute this command.
     *
     * @return an array of required bot permissions, defaults to empty
     */
    Permission[] selfPermissions() default {};
}