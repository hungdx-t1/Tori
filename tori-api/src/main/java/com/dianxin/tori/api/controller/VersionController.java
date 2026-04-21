package com.dianxin.tori.api.controller;

import com.dianxin.core.api.utils.VersionManager;
import net.dv8tion.jda.api.JDAInfo;

import java.util.Arrays;

import static com.dianxin.tori.api.base.Constants.JAVA_REQUIRED_VERSION;
import static com.dianxin.tori.api.base.Constants.JDA_REQUIRED_VERSION;

/**
 * A utility controller responsible for verifying the runtime environment's compatibility.
 * This includes validating both the running Java version and the implemented JDA
 * (Java Discord API) version against the system's minimum requirements.
 */
@SuppressWarnings("unused")
public final class VersionController {
    private VersionController() { }

    /**
     * Checks if the currently running Java version meets the minimum required version.
     *
     * @throws UnsupportedOperationException if the running Java version is lower than the required version.
     */
    public static void checkCompatibilityOrThrow() {
        int javaVersion = VersionManager.getJavaVersionRunning();

        if (javaVersion < JAVA_REQUIRED_VERSION) {
            throw new UnsupportedOperationException(
                    "Java version is incompatible, must use Java " + JAVA_REQUIRED_VERSION +
                            "or higher instead " + javaVersion + "!"
            );
        }
    }

    /**
     * Retrieves the version of JDA (Java Discord API) currently implemented at runtime.
     *
     * @return A string representing the runtime JDA version.
     */
    public static String getJdaVersionImplemented() {
        return JDAInfo.VERSION;
    }

    /**
     * Checks if the currently implemented JDA version meets the minimum required version.
     *
     * @throws UnsupportedOperationException if the running JDA version is incompatible (lower than required).
     */
    public static void checkJDACompatibilityOrThrow() {
        String jdaVersion = getJdaVersionImplemented();

        if (!isCompatibleVersion(jdaVersion)) {
            throw new UnsupportedOperationException(
                    "JDA version is incompatible, must use JDA " + JAVA_REQUIRED_VERSION +
                            "or higher instead " + jdaVersion + "!"
            );
        }
    }

    /**
     * Compares an implemented version string against the required JDA version
     * defined in the system constants.
     *
     * @param implemented The version string currently implemented.
     * @return {@code true} if the implemented version is greater than or equal to the required version, {@code false} otherwise.
     */
    static boolean isCompatibleVersion(String implemented) {
        int[] impl = parse(implemented);
        int[] req  = parse(JDA_REQUIRED_VERSION);

        for (int i = 0; i < Math.max(impl.length, req.length); i++) {
            int a = i < impl.length ? impl[i] : 0;
            int b = i < req.length ? req[i] : 0;

            if (a > b) return true;
            if (a < b) return false;
        }
        return true; // equal
    }

    private static int[] parse(String v) {
        return Arrays.stream(v.split("\\."))
                .map(s -> s.replaceAll("[^0-9]", "")) // prevent -alpha
                .mapToInt(Integer::parseInt)
                .toArray();
    }
}