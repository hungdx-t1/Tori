package com.dianxin.tori.api.commands.slash.v2.annotations;

import com.dianxin.tori.base.annotations.ReleasedSince;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Binds an interaction argument/option to a class field.
 *
 * <p>During interaction execution, the framework extracts the matching Discord option by {@link #name()}
 * and injects its mapped value directly into the annotated field via reflection before invoking the {@link Execute} method.</p>
 */
@ReleasedSince("26.8.301")
@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface CommandOption {

    /**
     * The expected Discord data type for this option.
     *
     * @return the option type
     */
    OptionType type();

    /**
     * The unique identifier/name for this option displayed in the Discord client.
     *
     * @return the option name
     */
    String name();

    /**
     * A helpful description of the argument shown to users. Defaults to empty string (which falls back to the option name).
     *
     * @return the option description
     */
    String description() default "";

    /**
     * Specifies whether the user is required to supply this option when executing the command.
     *
     * @return {@code true} if mandatory, {@code false} if optional (defaults to {@code false})
     */
    boolean required() default false;
}