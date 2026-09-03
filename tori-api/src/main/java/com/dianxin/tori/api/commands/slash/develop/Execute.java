package com.dianxin.tori.api.commands.slash.develop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Execute {
    boolean defer() default true;
    boolean ephemeral() default false;
}