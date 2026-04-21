package com.dianxin.tori.api.exceptions;

/**
 * An exception thrown to indicate that a required annotation is missing on a specific class.
 * This is typically thrown during the command registration phase if a modern command class
 * forgets to declare its metadata via annotations.
 */
@SuppressWarnings("unused")
public class MissingAnnotationException extends IllegalStateException {

    /**
     * Constructs a new {@code MissingAnnotationException}.
     *
     * @param annotationClazz The class of the annotation that is missing.
     * @param targetClazz     The class that was expected to have the annotation.
     */
    public MissingAnnotationException(Class<?> annotationClazz, Class<?> targetClazz) {
        super("Missing @" + annotationClazz.getSimpleName() + " annotation on " + targetClazz.getSimpleName());
    }
}