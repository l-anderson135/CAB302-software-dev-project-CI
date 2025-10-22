package main.java.com.team.game.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import main.java.com.team.game.model.User;
import main.java.com.team.game.service.GameService;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Controller for the Login screen.
 * <p>
 * Handles user authentication and registration, validating input
 * and delegating logic to {@link GameService}. On success, passes
 * the logged-in user to a callback so the next view can be loaded.
 */
public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label feedbackLabel;

    private GameService svc;
    private Consumer<User> onSuccess;

    /** JavaFX lifecycle hook. */
    @FXML
    private void initialize() { }

    /**
     * Injects dependencies for authentication logic.
     *
     * @param svc        the game service instance
     * @param onSuccess  callback invoked on successful login
     */
    public void setDependencies(GameService svc, Consumer<User> onSuccess) {
        this.svc = svc;
        this.onSuccess = (onSuccess != null) ? onSuccess : (u -> {});
    }

    // =====================================================
    // === TESTABLE CORE LOGIC METHODS =====================
    // =====================================================

    /**
     * Checks login credentials and returns an Optional containing the authenticated user.
     *
     * @param username the username entered
     * @param password the password entered
     * @return {@code Optional<User>} if credentials are valid, otherwise {@code Optional.empty()}     */
    public Optional<User> checkUser(String username, char[] password) {
        if (svc == null) throw new IllegalStateException("GameService not set");
        if (username == null || password == null) return Optional.empty();
        return svc.login(username.trim(), password);
    }

    /**
     * Registers a new user.
     *
     * @param username desired username
     * @param password chosen password
     * @return the created User
     * @throws IllegalStateException if username already exists
     */
    public User registerUser(String username, char[] password) {
        if (svc == null) throw new IllegalStateException("GameService not set");
        String u = (username == null) ? "" : username.trim();
        if (u.isEmpty() || password == null || password.length == 0)
            throw new IllegalArgumentException("Username and password required");
        return svc.register(u, password);
    }

    // =====================================================
    // === GUI HANDLERS ===================================
    // =====================================================

    /** Handles login button click. */
    @FXML
    private void handleLogin() {
        String u = safe(usernameField.getText());
        char[] pw = toChars(passwordField.getText());

        if (u.isEmpty() || pw.length == 0) {
            feedbackLabel.setText("Enter username and password");
            return;
        }

        try {
            Optional<User> user = checkUser(u, pw);
            if (user.isPresent()) {
                onSuccess.accept(user.get());
                closeWindow();
            } else {
                feedbackLabel.setText("Incorrect username or password");
            }
        } finally {
            Arrays.fill(pw, '\0');
        }
    }

    /** Handles register button click. */
    @FXML
    private void handleRegister() {
        String u = safe(usernameField.getText());
        char[] pw = toChars(passwordField.getText());

        if (u.isEmpty() || pw.length == 0) {
            feedbackLabel.setText("Enter username and password");
            return;
        }

        try {
            User user = registerUser(u, pw);
            onSuccess.accept(user);
            closeWindow();
        } catch (IllegalStateException dup) {
            feedbackLabel.setText("Username already exists or invalid");
        } finally {
            Arrays.fill(pw, '\0');
        }
    }

    /** Closes the login window. */
    private void closeWindow() {
        if (usernameField == null) return; // for tests that don’t use GUI
        Stage stage = (Stage) usernameField.getScene().getWindow();
        if (stage != null) stage.close();
    }

    private static String safe(String s) { return s == null ? "" : s.trim(); }
    private static char[] toChars(String s) { return s == null ? new char[0] : s.toCharArray(); }
}
