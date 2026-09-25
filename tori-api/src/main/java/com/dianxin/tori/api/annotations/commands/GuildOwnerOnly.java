package com.dianxin.tori.api.annotations.commands;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Restricts the execution of the annotated command to Owner of Discord Guilds (Server) only.
 * The command will not be executable in Direct Messages or another member on guild.
 */
@SuppressWarnings("unused")
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface GuildOwnerOnly {
}
