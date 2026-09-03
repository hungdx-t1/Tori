package com.dianxin.tori.api.commands.slash.v3;

import net.dv8tion.jda.api.Permission;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SlashInfo {
    String name();
    String description();
    boolean guildOnly() default true;
    boolean autoDefer() default true;
    boolean ephemeral() default false;
    long cooldownSeconds() default 0;
    Permission[] requiredPermissions() default {};
}