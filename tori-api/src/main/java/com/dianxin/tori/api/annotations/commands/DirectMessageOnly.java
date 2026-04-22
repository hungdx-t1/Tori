package com.dianxin.tori.api.annotations.commands;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Restricts the execution of the annotated command to Direct Messages (DMs) only.
 * If a user attempts to handle this command within a Guild (Server), it should be blocked or ignored.
 */
@SuppressWarnings("unused")
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DirectMessageOnly {
}