package com.dianxin.tori.api.commands.slash.v2.annotations;

import com.dianxin.tori.base.annotations.ReleasedSince;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as a top-level Discord Slash Command.
 *
 * <p>Classes annotated with {@code @Command} serve as the entry point for command registration
 * in the Tori framework. A command class can either directly define options and an {@link Execute}
 * handler, or contain nested static classes annotated with {@link Subcommand} or {@link SubcommandGroup}.</p>
 */
@ReleasedSince("26.8.301")
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Command {

    /**
     * The name of the slash command. Must be lowercase and match Discord's command naming rules (1-32 characters, no spaces).
     *
     * @return the command name
     */
    String name();

    /**
     * A brief description explaining what the command does (1-100 characters).
     *
     * @return the command description
     */
    String description();
}