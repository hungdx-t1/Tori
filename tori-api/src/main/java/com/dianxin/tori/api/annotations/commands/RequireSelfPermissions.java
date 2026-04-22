package com.dianxin.tori.api.annotations.commands;

import net.dv8tion.jda.api.Permission;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies the Discord {@link Permission}s that the bot itself must possess
 * in the current channel or guild to successfully handle the annotated command.
 */
@SuppressWarnings("unused")
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RequireSelfPermissions {

    /**
     * An array of {@link Permission}s required by the bot.
     *
     * @return The required self-permissions.
     */
    Permission[] value();
}