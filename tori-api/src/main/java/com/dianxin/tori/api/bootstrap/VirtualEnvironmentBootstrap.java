package com.dianxin.tori.api.bootstrap;

import io.github.cdimascio.dotenv.Dotenv;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

/**
 * Bootstraps the virtual environment configuration for the application.
 *
 * <p>
 * This class is responsible for loading environment variables from a
 * <code>.env</code> file and exposing them as JVM system properties
 * <b>before</b> any logging framework (e.g. Logback) or other components
 * are initialized.
 * </p>
 *
 * <h2>Why this class exists</h2>
 * <ul>
 *     <li>Logging frameworks such as Logback are initialized lazily
 *     at the first call to {@link org.slf4j.LoggerFactory#getLogger(Class)}.</li>
 *     <li>If environment variables (e.g. <code>LOG_LEVEL</code>) are not
 *     available at that moment, default or fallback values will be used.</li>
 *     <li>This bootstrap guarantees that values defined in
 *     <code>.env</code> are available as {@link System#getProperty(String)}
 *     before Logback is configured.</li>
 * </ul>
 *
 * <h2>How it works</h2>
 * <p>
 * A static initializer block loads the <code>.env</code> file using
 * {@link Dotenv} and copies all entries into JVM system properties.
 * </p>
 *
 * <pre>{@code
 * LOG_LEVEL=DEBUG
 * DISCORD_TOKEN=xxxxxxxx
 * }</pre>
 *
 * <p>
 * After this class is loaded, the above values can be accessed via:
 * </p>
 *
 * <pre>{@code
 * System.getProperty("LOG_LEVEL");
 * }</pre>
 *
 * <h2>Usage</h2>
 * <p>
 * This class must be loaded <b>explicitly</b> and <b>before</b> any
 * SLF4J logger is created.
 * </p>
 *
 * <pre>{@code
 * public static void main(String[] args) {
 *     Class.forName(
 *         "com.dianxin.core.api.lifecycle.bootstrap.VirtualEnvironmentBootstrap"
 *     );
 *
 *     new MyBot().start();
 * }
 * }</pre>
 *
 * <p>
 * Failing to load this class early may result in incorrect logging
 * configuration (e.g. <code>LOG_LEVEL</code> being ignored).
 * </p>
 *
 * <h2>Notes</h2>
 * <ul>
 *     <li>The <code>.env</code> file is optional. If it does not exist,
 *     this bootstrap will silently do nothing.</li>
 *     <li>System environment variables always take precedence over
 *     values defined in <code>.env</code>.</li>
 *     <li>This class is intended to be used as a low-level runtime bootstrap
 *     and should not be referenced directly elsewhere.</li>
 * </ul>
 *
 * @apiNote
 * This class is part of the application lifecycle bootstrap phase and is
 * designed for framework-level initialization only.
 */
@SuppressWarnings("unused")
@NullMarked
public final class VirtualEnvironmentBootstrap {
    static {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
    }

    private VirtualEnvironmentBootstrap() { }
}
