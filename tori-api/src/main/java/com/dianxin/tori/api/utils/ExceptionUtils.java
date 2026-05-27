package com.dianxin.tori.api.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility class for sanitizing and formatting exception stack traces.
 * <p>
 * This class implements a thread-safe Singleton pattern and allows dynamic
 * registration of package prefixes that should be filtered out (suppressed)
 * from stack traces. This is particularly useful for keeping server logs clean
 * from deeply nested internal framework calls (e.g., Reactor, Netty, or Jsoup).
 */
@SuppressWarnings({"unused", "ThrowableNotThrown", "UnusedReturnValue"})
public class ExceptionUtils {
    private static volatile ExceptionUtils instance; // use volatile to feel-safe on multithreading

    private final Set<String> suppressedPackages = ConcurrentHashMap.newKeySet();

    private ExceptionUtils() {
        // load some redundant package
        suppressedPackages.add("java.util.concurrent");
        suppressedPackages.add("java.lang.Thread");
    }

    /**
     * Retrieves the singleton instance of {@link ExceptionUtils}.
     * <p>
     * Utilizes double-checked locking to ensure thread safety without
     * synchronization overhead on every call.
     *
     * @return The globally available instance of {@link ExceptionUtils}.
     */
    @NotNull
    public static ExceptionUtils getInstance() {
        if (instance == null) {
            synchronized (ExceptionUtils.class) {
                if (instance == null) {
                    instance = new ExceptionUtils();
                }
            }
        }
        return instance;
    }

    /**
     * Checks if the specified class name belongs to any currently registered
     * suppressed package.
     *
     * @param className The full class name to check (e.g., "org.jsoup.Connection").
     * @return {@code true} if the class belongs to a suppressed package, {@code false} otherwise.
     */
    public boolean isSuppressedPackage(@NotNull String className) {
        for (String pkg : suppressedPackages) {
            if (className.startsWith(pkg)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Dynamically registers a package prefix to be suppressed from future stack traces.
     * <p>
     * Bots or plugins can use this method to hide their internal noisy dependencies
     * from the main server logs.
     *
     * @param packageName The package prefix to suppress (e.g., "org.jsoup").
     * @return {@code true} if the package was successfully added, {@code false} if it was already registered.
     */
    public boolean registerSupressedPackage(@NotNull String packageName) {
        return suppressedPackages.add(packageName);
    }

    /**
     * Unregisters a previously suppressed package prefix.
     *
     * @param packageName The package prefix to remove from the suppression list.
     * @return {@code true} if the package was successfully removed, {@code false} if it was not found.
     */
    public boolean unregisterSupressedPackage(@NotNull String packageName) {
        return suppressedPackages.remove(packageName);
    }

    /**
     * Sanitizes the given {@link Throwable} by intercepting its stack trace and
     * removing any elements that belong to registered suppressed packages.
     * <p>
     * This method operates recursively to ensure that all nested errors (Caused by)
     * are also thoroughly sanitized.
     *
     * @param throwable The exception to sanitize. Can be null.
     * @return The sanitized exception, or {@code null} if the input was null.
     */
    public Throwable sanitize(@UnknownNullability Throwable throwable) {
        if (throwable == null) return null;

        // Filter the current error's StackTrace
        StackTraceElement[] originalTrace = throwable.getStackTrace();
        StackTraceElement[] cleanedTrace = Arrays.stream(originalTrace)
                .filter(element -> suppressedPackages.stream()
                        .noneMatch(pkg -> element.getClassName().startsWith(pkg)))
                .toArray(StackTraceElement[]::new);

        // Overwrite the filtered StackTrace
        throwable.setStackTrace(cleanedTrace);

        // Recursion to always clean up the original errors (Caused by)
        if (throwable.getCause() != null) {
            sanitize(throwable.getCause());
        }

        return throwable;
    }
}
