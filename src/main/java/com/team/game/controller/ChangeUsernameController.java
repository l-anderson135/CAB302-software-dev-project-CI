package main.java.com.team.game.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import main.java.com.team.game.Main;
import main.java.com.team.game.model.User;
import main.java.com.team.game.service.GameService;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the Change Username dialog/view.
 * <p>
 * Displays the current username, validates user input,
 * and delegates username updates to {@link GameService}.
 * Uses {@code Main.MenuApp} to access the active {@link User} and game service.
 */
public class ChangeUsernameController implements Initializable {

    @FXML
    private Label currentUsernameLabel;

    @FXML
    private TextField newUsernameField;

    @FXML
    private Label statusLabel;

    @FXML
    private Button updateButton;

    @FXML
    private Button cancelButton;

    private GameService gameService;
    private User currentUser;

    /**
     * JavaFX lifecycle hook. Initializes the current user and game service
     * and displays the active username in the label.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        gameService = Main.MenuApp.getGameService();
        currentUser = Main.MenuApp.getCurrentUser();

        if (currentUser != null) {
            currentUsernameLabel.setText(currentUser.getUsername());
        }

        newUsernameField.setOnAction(this::handleUpdateUsername);
    }

    /**
     * Handles the update button or Enter key event for changing a username.
     * Performs validation before calling {@link GameService#updateUsername(User, String)}.
     * <p>
     * Validation rules:
     * <ul>
     *   <li>New username cannot be empty</li>
     *   <li>New username must differ from the current one</li>
     * </ul>
     *
     * @param actionEvent the originating UI event
     */
    @FXML
    public void handleUpdateUsername(ActionEvent actionEvent) {
        String newUsername = newUsernameField.getText().trim();

        if (newUsername.isEmpty()) {
            showStatus("Username cannot be empty.", false);
            return;
        }

        if (newUsername.equals(currentUser.getUsername())) {
            showStatus("New username is the same as current username.", false);
            return;
        }

        try {
            gameService.updateUsername(currentUser, newUsername);

            // Create a new user instance to reflect the change
            currentUser = new User(currentUser.getId(), newUsername, currentUser.getRegisteredAt());

            Main.MenuApp.setUserData(gameService, currentUser);
            currentUsernameLabel.setText(newUsername);
            newUsernameField.clear();

            showStatus("Updated.", true);

        } catch (IllegalStateException ex) {
            showStatus("That name is taken.", false);
        } catch (Exception ex) {
            showStatus("Error updating username: " + ex.getMessage(), false);
        }
    }

    /**
     * Closes the window when the cancel button is pressed.
     *
     * @param actionEvent the originating UI event
     */
    @FXML
    public void handleCancel(ActionEvent actionEvent) {
        System.out.println("Cancel username change");
        closeWindow();
    }

    /**
     * Displays a feedback message styled by success or error.
     *
     * @param message   the message text
     * @param isSuccess true for success styling, false for error styling
     */
    private void showStatus(String message, boolean isSuccess) {
        statusLabel.setText(message);
        statusLabel.setVisible(true);

        if (isSuccess) {
            statusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #4CAF50; -fx-font-weight: bold;");
        } else {
            statusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #f44336; -fx-font-weight: bold;");
        }
    }

    /**
     * Utility to close the current stage (window).
     */
    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}
