package com.dianxin.tori.base.lifecycle;

import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@ApiStatus.Experimental
@SuppressWarnings("unused")
public class ExceptionManager {
    private static final Logger logger = LoggerFactory.getLogger(ExceptionManager.class);

    // storage [exception type -> How to Handle]
    private final Map<Class<? extends Throwable>, BotExceptionHandler<? extends Throwable, ?>> registry = new HashMap<>();

    /**
     * Register a handler for a specific Exception type.
     */
    public <E extends Throwable, C> void register(Class<E> exceptionClass, BotExceptionHandler<E, C> handler) {
        registry.put(exceptionClass, handler);
        logger.debug("ExceptionHandler has been registered for the error: {}", exceptionClass.getSimpleName());
    }

    /**
     * Coordinate the error to its correct handler.
     */
    @SuppressWarnings("unchecked")
    public <C> void dispatch(Throwable exception, C context) {
        Class<?> currentClass = exception.getClass();
        BotExceptionHandler<Throwable, C> handler = null;

        // The algorithm crawls back up the family tree to find the father Handler if there are no child Handlers
        while (currentClass != null && Throwable.class.isAssignableFrom(currentClass)) {
            if (registry.containsKey(currentClass)) {
                handler = (BotExceptionHandler<Throwable, C>) registry.get(currentClass);
                break;
            }
            currentClass = currentClass.getSuperclass(); // parent class
        }

        //If you find the Handler, give it the error to handle
        if (handler != null) {
            try {
                handler.handle(exception, context);
            } catch (Exception e) {
                logger.error("🚨 A critical error occurs RIGHT INSIDE the Exception Handler!", e);
            }
        } else {
            // Fallback By default, if no one bothers to point out this error
            logger.error("🔥 The error was caught, but there's no handler to handle it: ", exception);
            // TODO: The default code might send a "System Error" message to the Discord channel here
        }
    }
}
