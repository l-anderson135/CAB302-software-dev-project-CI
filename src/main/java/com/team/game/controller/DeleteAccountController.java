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
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller for the Delete Account dialog/view.
 * <p>
 * Handles final account deletion confirmation and user interaction.
 * Requires the user to type "DELETE" and confirm through a modal alert.
 * Delegates the actual deletion logic to {@link GameService}.
 */
public class DeleteAccountController implements Initializable {

    @FXML
    private Label currentUserLabel;

    @FXML
    private TextField confirmationField;

    @FXML
    private Label statusLabel;

    @FXML
    private Button deleteButton;

    @FXML
    private Button cancelButton;

    private GameService gameService;
    private User currentUser;

    /**
     * JavaFX lifecycle hook. Initializes the current user and service from {@code Main.MenuApp}
     * and updates the UI label to display the current user’s details.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        gameService = Main.MenuApp.getGameService();
        currentUser = Main.MenuApp.getCurrentUser();

        if (currentUser != null) {
            currentUserLabel.setText("Username: " + currentUser.getUsername() + " (ID: " + currentUser.getId() + ")");
        }

        confirmationField.setOnAction(this::handleDeleteAccount);
    }

    /**
     * Handles account deletion when the user confirms with the keyword "DELETE".
     * <p>
     * Displays a final confirmation dialog before calling
     * {@link GameService#deleteUser(User)}. On success, a final message is shown
     * and the application exits.
     *
     * @param actionEvent the originating UI event
     */
    @FXML
    public void handleDeleteAccount(ActionEvent actionEvent) {
        String confirmation = confirmationField.getText().trim();

        if (!"DELETE".equals(confirmation)) {
            showStatus("You must type 'DELETE' exactly to confirm account deletion.", false);
            return;
        }

        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Final Confirmation");
        confirmationAlert.setHeaderText("Delete Account - Final Warning");
        confirmationAlert.setContentText("Are you absolutely sure you want to delete your account?\n\n" +
                "Username: " + currentUser.getUsername() + "\n" +
                "This action cannot be undone!");

        Optional<ButtonType> result = confirmationAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean ok = gameService.deleteUser(currentUser);

            if (ok) {
                showStatus("Account deleted.", true);
                showFinalDeletionMessage();

                javafx.application.Platform.runLater(() -> {
                    closeWindow();
                    System.exit(0);
                });

            } else {
                showStatus("Delete failed.", false);
            }
        }
    }

    /**
     * Closes the window when the user cancels the deletion.
     *
     * @param actionEvent the originating UI event
     */
    @FXML
    public void handleCancel(ActionEvent actionEvent) {
        System.out.println("Account deletion cancelled");
        closeWindow();
    }

    /**
     * Displays a final information alert confirming successful deletion
     * before the app closes.
     */
    private void showFinalDeletionMessage() {
        Alert infoAlert = new Alert(Alert.AlertType.INFORMATION);
        infoAlert.setTitle("Account Deleted");
        infoAlert.setHeaderText("Account Successfully Deleted");
        infoAlert.setContentText("Your account has been permanently deleted.\n" +
                "All your data has been removed from the system.\n" +
                "The application will now close.");
        infoAlert.showAndWait();
    }

    /**
     * Updates the status label with a styled message.
     *
     * @param message   text to display
     * @param isSuccess true for success style, false for error style
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
