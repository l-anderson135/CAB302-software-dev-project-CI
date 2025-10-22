package main.java.com.team.game.controller;

import javafx.event.ActionEvent;
import main.java.com.team.game.Main;
import main.java.com.team.game.ui.Windows;

/**
 * Controller for the main menu screen.
 * <p>
 * Handles navigation between the various sections of the game,
 * such as starting a new round, managing sessions, updating user details,
 * viewing leaderboards, and exiting the application.
 */
public class MenuController {

    /**
     * Opens the Game Mode selection screen to start a new round.
     *
     * @param actionEvent the originating UI event
     */
    public void handleStartRound(ActionEvent actionEvent) {
        System.out.println("Start Round clicked");
        Windows.openGameMode(Main.MenuApp.getGameService(), Main.MenuApp.getCurrentUser());
    }

    /**
     * Opens the view that displays all of the user’s previous sessions.
     *
     * @param actionEvent the originating UI event
     */
    public void handleMySessions(ActionEvent actionEvent) {
        System.out.println("My Sessions clicked");
        Windows.openViewSessions(Main.MenuApp.getGameService(), Main.MenuApp.getCurrentUser());
    }

    /**
     * Opens the interface for deleting a previously saved session.
     *
     * @param actionEvent the originating UI event
     */
    public void handleDeleteSession(ActionEvent actionEvent) {
        System.out.println("Delete Session clicked");
        Windows.openDeleteSession(Main.MenuApp.getGameService(), Main.MenuApp.getCurrentUser());
    }

    /**
     * Opens the Change Username window.
     *
     * @param actionEvent the originating UI event
     */
    public void handleChangeUsername(ActionEvent actionEvent) {
        System.out.println("Change Username clicked");
        Windows.openChangeUsername(Main.MenuApp.getGameService(), Main.MenuApp.getCurrentUser());
    }

    /**
     * Opens the Change Password window.
     *
     * @param actionEvent the originating UI event
     */
    public void handleChangePassword(ActionEvent actionEvent) {
        System.out.println("Change Password clicked");
        Windows.openChangePassword(Main.MenuApp.getGameService(), Main.MenuApp.getCurrentUser());
    }

    /**
     * Opens the Delete Account confirmation screen.
     *
     * @param actionEvent the originating UI event
     */
    public void handleDeleteAccount(ActionEvent actionEvent) {
        System.out.println("Delete Account clicked");
        Windows.openDeleteAccount(Main.MenuApp.getGameService(), Main.MenuApp.getCurrentUser());
    }

    /**
     * Opens the Leaderboard view showing top scores.
     *
     * @param actionEvent the originating UI event
     */
    public void handleLeaderboard(ActionEvent actionEvent) {
        System.out.println("Leaderboard clicked");
        Windows.openLeaderboard(Main.MenuApp.getGameService(), Main.MenuApp.getCurrentUser());
    }

    /**
     * Opens a list of all registered users.
     *
     * @param actionEvent the originating UI event
     */
    public void handleListUsers(ActionEvent actionEvent) {
        System.out.println("List Users clicked");
        Windows.openUsersList(Main.MenuApp.getGameService(), Main.MenuApp.getCurrentUser());
    }

    /**
     * Exits the application when the user chooses to quit.
     *
     * @param actionEvent the originating UI event
     */
    public void handleExit(ActionEvent actionEvent) {
        System.out.println("Exit clicked");
        System.exit(0);
    }
}
