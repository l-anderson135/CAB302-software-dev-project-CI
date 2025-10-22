package main.java.com.team.game.ui;

import java.util.concurrent.atomic.AtomicBoolean;
import javafx.application.Platform;

/**
 * Ensures that the JavaFX runtime is started exactly once,
 * even when launched from a non-JavaFX context (such as tests or console apps).
 * <p>
 * This helper is used by the application’s backend or test setup to safely
 * initialize JavaFX platform features like {@code Stage} or {@code Scene}
 * without triggering multiple startup errors.
 */
public final class FxRuntime {

    /** Tracks whether the JavaFX platform has already been started. */
    private static final AtomicBoolean started = new AtomicBoolean(false);

    /** Private constructor to prevent instantiation. */
    private FxRuntime() {}

    /**
     * Starts the JavaFX runtime if it hasn’t been started yet.
     * <p>
     * Calling this method multiple times is safe; the startup routine
     * will only execute once. It also disables implicit exit so that
     * closing a single stage doesn’t terminate the entire runtime.
     */
    public static void ensureStarted() {
        if (started.compareAndSet(false, true)) {
            Platform.startup(() -> {});
            Platform.setImplicitExit(false);
        }
    }
}
