package com.dianxin.tori.base.annotations;

import org.jetbrains.annotations.ApiStatus;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks the specific version of the Tori framework in which an element
 * (class, interface, method, constructor, or field) was officially introduced.
 * <p>
 * <b>Important Notice:</b> This is an internal metadata annotation intended solely
 * for maintenance, documentation tracking, and bytecode reflection within the Tori codebase.
 * Third-party bot developers must not use this annotation in external projects.
 *
 * @see ApiStatus.Internal
 */
@Documented
@SuppressWarnings({"UnusedReturnValue", "unused"})
@ApiStatus.Internal
@Retention(RetentionPolicy.RUNTIME)
@Target({
        ElementType.TYPE,
        ElementType.METHOD,
        ElementType.CONSTRUCTOR,
        ElementType.FIELD
})
public @interface ReleasedSince {

    /**
     * The release version of the framework where this element became available (e.g., {@code "26.9.3"}).
     *
     * @return The target release version string, or {@code "N/A"} if unspecified.
     */
    String value() default "N/A";
}