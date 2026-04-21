package com.dianxin.tori.api.controller;

import com.dianxin.core.api.utils.VersionManager;
import net.dv8tion.jda.api.JDAInfo;

import java.util.Arrays;

import static com.dianxin.tori.api.base.Constants.JAVA_REQUIRED_VERSION;
import static com.dianxin.tori.api.base.Constants.JDA_REQUIRED_VERSION;

@SuppressWarnings("unused")
public final class VersionController {
    private VersionController() { }

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
     * get JDA version developer using (runtime)
     */
    public static String getJdaVersionImplemented() {
        return JDAInfo.VERSION;
    }

    /**
     * check compatible JDA version
     *
     * @throws UnsupportedOperationException if version is not meet requirement
     */
    public static void checkCompatibilityOrThrow2() {
        int javaVersion = VersionManager.getJavaVersionRunning();
        String jdaVersion = getJdaVersionImplemented();

        if (javaVersion < JAVA_REQUIRED_VERSION) {
            throw new UnsupportedOperationException(
                    "Java version is incompatible, must use Java " + JAVA_REQUIRED_VERSION +
                            "or higher instead " + javaVersion + "!"
            );
        }

        if (!isCompatibleVersion(jdaVersion)) {
            throw new UnsupportedOperationException(
                    "JDA version is incompatible, must use JDA " + JAVA_REQUIRED_VERSION +
                            "or higher instead " + javaVersion + "!"
            );
        }
    }

    /**
     * implemented >= required ?
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
        return true; // bằng nhau
    }

    private static int[] parse(String v) {
        return Arrays.stream(v.split("\\."))
                .map(s -> s.replaceAll("[^0-9]", "")) // prevent -alpha
                .mapToInt(Integer::parseInt)
                .toArray();
    }
}
