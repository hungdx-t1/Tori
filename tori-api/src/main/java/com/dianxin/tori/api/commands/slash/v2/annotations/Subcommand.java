package com.dianxin.tori.api.commands.slash.v2.annotations;

import com.dianxin.tori.base.annotations.ReleasedSince;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares a nested static inner class as a runnable Discord Subcommand.
 *
 * <p>Can be placed directly inside a {@link Command} class or nested inside a {@link SubcommandGroup} class.
 * The class must define an execution method annotated with {@link Execute}.</p>
 */
@ReleasedSince("26.8.301")
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Subcommand {

    /**
     * The name of the subcommand. Must be lowercase and match Discord's naming requirements.
     *
     * @return the subcommand name
     */
    String name();

    /**
     * A concise description explaining the action performed by this subcommand.
     *
     * @return the subcommand description
     */
    String description();
}