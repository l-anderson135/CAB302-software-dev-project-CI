package main.java.com.team.game.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import main.java.com.team.game.Main;
import main.java.com.team.game.ui.Windows;

/**
 * Controller for the Game Mode selection screen.
 * <p>
 * Provides navigation buttons for launching the different game modules
 * (Basics, Trig, and Target) or returning to the main menu.
 * Each button opens the corresponding game window using {@link Windows}.
 */
public class GameModeController {

    @FXML
    private Button basicsButton;

    @FXML
    private Button trigButton;

    @FXML
    private Button targetButton;

    @FXML
    private Button backButton;

    /**
     * Launches the Basics game mode and closes this selection window.
     *
     * @param actionEvent the originating UI event
     */
    @FXML
    public void handleBasicsMode(ActionEvent actionEvent) {
        System.out.println("Basics Mode selected");
        Windows.openBasics(Main.MenuApp.getGameService(), Main.MenuApp.getCurrentUser());
        closeWindow();
    }

    /**
     * Launches the Trig game mode and closes this selection window.
     *
     * @param actionEvent the originating UI event
     */
    @FXML
    public void handleTrigMode(ActionEvent actionEvent) {
        System.out.println("Trig Mode selected");
        Windows.openTrig(Main.MenuApp.getGameService(), Main.MenuApp.getCurrentUser());
        closeWindow();
    }

    /**
     * Launches the Target game mode and closes this selection window.
     *
     * @param actionEvent the originating UI event
     */
    @FXML
    public void handleTargetMode(ActionEvent actionEvent) {
        System.out.println("Target Mode selected");
        Windows.openTarget(Main.MenuApp.getGameService(), Main.MenuApp.getCurrentUser());
        closeWindow();
    }

    /**
     * Returns to the previous menu screen without launching a game.
     *
     * @param actionEvent the originating UI event
     */
    @FXML
    public void handleBack(ActionEvent actionEvent) {
        System.out.println("Back to menu");
        closeWindow();
    }

    /**
     * Utility method to close the current window (stage).
     */
    private void closeWindow() {
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.close();
    }
}
