package main.java.com.team.game.ui;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import main.java.com.team.game.Main;
import main.java.com.team.game.model.User;
import main.java.com.team.game.service.GameService;

/**
 * Centralized factory for opening all application windows.
 * <p>
 * This class standardizes how new JavaFX stages are created and displayed.
 * Each method ensures the JavaFX runtime is active, loads the appropriate FXML
 * view, sets dependencies (e.g. {@link GameService}, {@link User}), and opens
 * the scene on the JavaFX Application Thread.
 */
public final class Windows {

    /** Private constructor to prevent instantiation. */
    private Windows() {}

    // ---------------------------------------------------------------------
    // AUTHENTICATION & MENU WINDOWS
    // ---------------------------------------------------------------------

    /**
     * Opens the login window and injects dependencies into its controller.
     *
     * @param svc       the {@link GameService} instance
     * @param onSuccess callback executed when the user successfully logs in
     */
    public static void openLogin(GameService svc, java.util.function.Consumer<User> onSuccess) {
        FxRuntime.ensureStarted();
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(Windows.class.getResource("/login/login.fxml"));
                Parent root = loader.load();
                main.java.com.team.game.controller.LoginController c = loader.getController();
                c.setDependencies(svc, onSuccess);

                Stage s = new Stage();
                s.setTitle("Sign in");
                s.setScene(new Scene(root));
                s.show();
            } catch (Exception e) {
                System.err.println("Error opening login window: " + e.getMessage());
            }
        });
    }

    /** Opens the main menu window. */
    public static void openMenu(GameService svc, User user) {
        openStage("/menu/menu-main.fxml", "Main Menu", svc, user);
    }

    /** Opens the mode selection screen. */
    public static void openGameMode(GameService svc, User user) {
        openStage("/menu/game-mode.fxml", "Select Game Mode", svc, user);
    }

    // ---------------------------------------------------------------------
    // GAME WINDOWS
    // ---------------------------------------------------------------------

    /** Opens the Basics game window. */
    public static void openBasics(GameService svc, User user) {
        FxRuntime.ensureStarted();
        Platform.runLater(() -> {
            try {
                Main.BasicsApp.setUserData(svc, user);
                Parent root = FXMLLoader.load(Main.BasicsApp.class.getResource("/basicsgame/basics-game.fxml"));
                Stage s = new Stage();
                s.setTitle("Basics Game");
                s.setScene(new Scene(root));
                s.show();
            } catch (Exception e) {
                System.err.println("Error opening Basics game window: " + e.getMessage());
            }
        });
    }

    /** Opens the Trigonometry game window. */
    public static void openTrig(GameService svc, User user) {
        openStage("/trigofun/trigofun-main.fxml", "Trig Game", svc, user);
    }

    /** Opens the Target game window. */
    public static void openTarget(GameService svc, User user) {
        openStage("/target/target-main.fxml", "Target Game", svc, user);
    }

    // ---------------------------------------------------------------------
    // USER & SESSION MANAGEMENT WINDOWS
    // ---------------------------------------------------------------------

    /** Opens the View Sessions window. */
    public static void openViewSessions(GameService svc, User user) {
        openStage("/menu/view-session.fxml", "My Game Sessions", svc, user);
    }

    /** Opens the Delete Session window. */
    public static void openDeleteSession(GameService svc, User user) {
        openStage("/menu/delete-session.fxml", "Delete Session", svc, user);
    }

    /** Opens the Change Username window. */
    public static void openChangeUsername(GameService svc, User user) {
        openStage("/menu/change-username.fxml", "Change Username", svc, user);
    }

    /** Opens the Change Password window. */
    public static void openChangePassword(GameService svc, User user) {
        openStage("/menu/change-password.fxml", "Change Password", svc, user);
    }

    /** Opens the Leaderboard window. */
    public static void openLeaderboard(GameService svc, User user) {
        openStage("/menu/leaderboard.fxml", "Leaderboard", svc, user);
    }

    /** Opens the All Users list window. */
    public static void openUsersList(GameService svc, User user) {
        openStage("/menu/users-list.fxml", "All Users", svc, user);
    }

    /** Opens the Delete Account window. */
    public static void openDeleteAccount(GameService svc, User user) {
        openStage("/menu/delete-account.fxml", "Delete Account", svc, user);
    }

    // ---------------------------------------------------------------------
    // INTERNAL UTILITY
    // ---------------------------------------------------------------------

    /**
     * Helper for consistent stage creation and error handling.
     */
    private static void openStage(String fxmlPath, String title, GameService svc, User user) {
        FxRuntime.ensureStarted();
        Platform.runLater(() -> {
            try {
                Main.MenuApp.setUserData(svc, user);
                Parent root = FXMLLoader.load(Main.MenuApp.class.getResource(fxmlPath));
                Stage s = new Stage();
                s.setTitle(title);
                s.setScene(new Scene(root));
                s.show();
            } catch (Exception e) {
                System.err.println("Error opening window (" + title + "): " + e.getMessage());
            }
        });
    }
}
