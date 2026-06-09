package com.dianxin.tori.base.utils;

/**
 * Utility class for querying and validating the Java runtime version.
 *
 * <p>
 * This class provides helper methods to detect the Java version currently
 * running the application and to verify compatibility against a required
 * minimum Java version.
 * </p>
 *
 * <p>
 * It is primarily intended to be used during application startup or
 * bootstrap phases to ensure the runtime environment meets the minimum
 * requirements of the framework.
 * </p>
 *
 * <h2>Example usage</h2>
 * <pre>{@code
 * int current = VersionManager.getJavaVersionRunning();
 *
 * if (!VersionManager.isCompatibleJavaVersion(21)) {
 *     throw new UnsupportedOperationException(
 *         "Java 21 or newer is required"
 *     );
 * }
 * }</pre>
 *
 * <p>
 * This class is a pure utility class and must not be instantiated.
 * </p>
 */
@SuppressWarnings("unused")
public final class VersionManager {
    public static final String DIANXIN_API_VERSION = "2.4";

    private VersionManager() { }

    /**
     * Returns the major feature version of the Java runtime currently
     * running the application.
     *
     * <p>
     * For example:
     * </p>
     * <ul>
     *     <li>Java 8  → returns {@code 8}</li>
     *     <li>Java 11 → returns {@code 11}</li>
     *     <li>Java 17 → returns {@code 17}</li>
     *     <li>Java 21 → returns {@code 21}</li>
     * </ul>
     *
     * @return the major Java version (feature version)
     */
    public static int getJavaVersionRunning() {
        return Runtime.version().feature();
    }

    /**
     * Returns the full Java runtime version string.
     *
     * @return the Java runtime version (e.g. "21.0.2")
     */
    public static String getJavaVersionString() {
        return Runtime.version().toString();
    }

    /**
     * Checks whether the currently running Java version is compatible
     * with the specified minimum required version.
     *
     * <p>
     * A version is considered compatible if:
     * </p>
     * <pre>{@code
     * currentVersion >= requiredVersion
     * }</pre>
     *
     * @param requiredVersion the minimum required Java version
     * @return {@code true} if the current Java version is equal to or
     *         greater than the required version, {@code false} otherwise
     */
    public static boolean isCompatibleJavaVersion(int requiredVersion) {
        int current = getJavaVersionRunning();
        return current >= requiredVersion;
    }

    /**
     * Ensures that the current Java version meets the required minimum version.
     *
     * @param requiredVersion the minimum required Java version
     * @throws UnsupportedOperationException if the Java version is incompatible
     */
    public static void requireJavaVersion(int requiredVersion) {
        int current = getJavaVersionRunning();
        if (current < requiredVersion) {
            throw new UnsupportedOperationException(
                    "Java " + requiredVersion + "+ is required, but running on Java " + current
            );
        }
    }

    /**
     * Ensures that the current Java version meets the required minimum version.
     *
     * @param requiredVersion the minimum required Java version
     * @throws UnsupportedOperationException if the Java version is incompatible
     */
    public static void requireJavaVersion(int requiredVersion, boolean shutdown) {
        requireJavaVersion(requiredVersion);
        if(shutdown) System.exit(-1);
    }

    /**
     * Checks whether the current Java version falls within the given range.
     *
     * @param min minimum Java version (inclusive)
     * @param max maximum Java version (inclusive)
     * @return true if current version is within range
     */
    public static boolean isJavaVersionInRange(int min, int max) {
        int v = getJavaVersionRunning();
        return v >= min && v <= max;
    }

    /**
     * Checks whether the current Java version is a Long-Term Support (LTS) release.
     *
     * @return true if the Java version is LTS
     */
    public static boolean isLtsVersion() {
        int v = getJavaVersionRunning();
        return v == 8 || v == 11 || v == 17 || v == 21 || v == 25;
    }
}