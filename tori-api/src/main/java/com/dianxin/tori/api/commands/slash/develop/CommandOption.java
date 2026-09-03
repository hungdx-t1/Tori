package com.dianxin.tori.api.commands.slash.develop;

import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.jetbrains.annotations.ApiStatus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ApiStatus.Experimental // đang trong quá trình phát triển
public @interface CommandOption {
    OptionType type();
    String name();
    String description() default "";
    boolean required() default false;
}
