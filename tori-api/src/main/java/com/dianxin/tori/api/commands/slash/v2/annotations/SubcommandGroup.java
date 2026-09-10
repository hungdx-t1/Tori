package com.dianxin.tori.api.commands.slash.v2.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares a nested static inner class as a Discord Subcommand Group.
 *
 * <p>A subcommand group acts as an intermediate organizational layer inside a {@link Command},
 * grouping multiple related {@link Subcommand} classes together (e.g., {@code /settings roles add}).</p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SubcommandGroup {

    /**
     * The name of the subcommand group. Must be lowercase and match Discord's naming requirements.
     *
     * @return the subcommand group name
     */
    String name();

    /**
     * A short summary describing the category of subcommands grouped under this name.
     *
     * @return the group description
     */
    String description();
}