package com.dianxin.tori.base.env;

import io.github.cdimascio.dotenv.Dotenv;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;

@NullMarked
@SuppressWarnings("unused")
public final class VirtualEnvironmentConfiguration {
    private static final Dotenv dotenv = VirtualEnvironmentConfiguration.load();

    private static Dotenv load() {
        String dir = System.getenv().getOrDefault("DOTENV_DIR", ".");
        return Dotenv.configure().directory(dir).ignoreIfMissing().load();
    }

    private VirtualEnvironmentConfiguration() { }

    @Nullable
    public static String get(String key) {
        // Ưu tiên env hệ thống trước
        String sys = System.getenv(key);
        return sys != null ? sys : dotenv.get(key);
    }

    public static String getOrDefault(String key, String defaultValue) {
        String value = get(key);
        return value != null ? value : defaultValue;
    }

    public static String getOrThrow(String key) {
        String value = get(key);
        if (value == null) {
            throw new RuntimeException("Key %s is not provided".formatted(key));
        }
        return value;
    }

    public static String getOrThrow(String key, Throwable th) throws Throwable {
        String value = get(key);
        if (value == null) {
            throw th;
        }
        return value;
    }
}
