package main.java.com.team.game.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import main.java.com.team.game.Main;
import main.java.com.team.game.model.GameSession;
import main.java.com.team.game.service.GameService;
import main.java.com.team.game.model.User;

import java.net.URL;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller for the Delete Session view.
 * <p>
 * Allows users to select or manually enter a session ID to delete.
 * Lists all saved game sessions for the logged-in user and delegates
 * deletion logic to {@link GameService}.
 */
public class DeleteSessionController implements Initializable {

    private static final ZoneId LOCAL_TZ = ZoneId.of("Australia/Brisbane");
    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @FXML
    private ComboBox<SessionItem> sessionComboBox;

    @FXML
    private TextField sessionIdField;

    @FXML
    private Label statusLabel;

    @FXML
    private Button deleteButton;

    @FXML
    private Button refreshButton;

    @FXML
    private Button backButton;

    private GameService gameService;
    private User currentUser;

    /**
     * JavaFX lifecycle hook.
     * Initializes the game service and user references,
     * loads the user's saved sessions, and sets up event listeners.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        gameService = Main.MenuApp.getGameService();
        currentUser = Main.MenuApp.getCurrentUser();
        loadSessions();

        sessionComboBox.setOnAction(e -> {
            if (sessionComboBox.getValue() != null) {
                sessionIdField.clear();
            }
        });

        sessionIdField.textProperty().addListener((obs, oldText, newText) -> {
            if (!newText.trim().isEmpty()) {
                sessionComboBox.setValue(null);
            }
        });
    }

    /**
     * Loads all sessions belonging to the current user
     * and populates the combo box with formatted session details.
     */
    private void loadSessions() {
        if (gameService == null || currentUser == null) {
            showStatus("Error: Unable to load user data", false);
            return;
        }

        List<GameSession> sessions = gameService.listSessionsByUser(currentUser);
        ObservableList<SessionItem> sessionItems = FXCollections.observableArrayList();

        for (GameSession session : sessions) {
            String startedLocal = session.getStartedAt().atZone(LOCAL_TZ).format(DT_FMT);
            String displayText = String.format("ID: %d | %s | Score: %d | Started: %s",
                    session.getId(), session.getMode(), session.getScore(), startedLocal);
            sessionItems.add(new SessionItem(session.getId(), displayText));
        }

        sessionComboBox.setItems(sessionItems);

        if (sessions.isEmpty()) {
            showStatus("No sessions found to delete.", false);
            deleteButton.setDisable(true);
        } else {
            hideStatus();
            deleteButton.setDisable(false);
        }
    }

    /**
     * Handles deletion of the selected or entered session.
     * Displays a confirmation dialog before removing it via {@link GameService#deleteSession(int)}.
     *
     * @param actionEvent the originating UI event
     */
    @FXML
    public void handleDelete(ActionEvent actionEvent) {
        int sessionId = getSelectedSessionId();

        if (sessionId == -1) {
            showStatus("Please select a session or enter a session ID.", false);
            return;
        }

        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Confirm Deletion");
        confirmationAlert.setHeaderText("Delete Session");
        confirmationAlert.setContentText("Are you sure you want to delete session ID " + sessionId + "?");

        Optional<ButtonType> result = confirmationAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = gameService.deleteSession(sessionId);

            if (success) {
                showStatus("Session ID " + sessionId + " deleted successfully.", true);

                loadSessions();
                sessionComboBox.setValue(null);
                sessionIdField.clear();
            } else {
                showStatus("Session ID " + sessionId + " not found.", false);
            }
        }
    }

    /**
     * Reloads the list of sessions from the database and clears input fields.
     *
     * @param actionEvent the originating UI event
     */
    @FXML
    public void handleRefresh(ActionEvent actionEvent) {
        System.out.println("Refreshing session list...");
        loadSessions();
        sessionComboBox.setValue(null);
        sessionIdField.clear();
        hideStatus();
    }

    /**
     * Closes the delete session window.
     *
     * @param actionEvent the originating UI event
     */
    @FXML
    public void handleBack(ActionEvent actionEvent) {
        System.out.println("Back to menu");
        closeWindow();
    }

    /**
     * Retrieves the session ID from the combo box or text field.
     *
     * @return the selected/entered session ID, or -1 if invalid
     */
    private int getSelectedSessionId() {
        SessionItem selectedItem = sessionComboBox.getValue();
        if (selectedItem != null) {
            return selectedItem.getId();
        }

        String idText = sessionIdField.getText().trim();
        if (!idText.isEmpty()) {
            try {
                return Integer.parseInt(idText);
            } catch (NumberFormatException e) {
                showStatus("Invalid session ID format. Please enter a number.", false);
                return -1;
            }
        }

        return -1;
    }

    /**
     * Displays a styled status message.
     *
     * @param message   text to show
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
     * Hides the status label.
     */
    private void hideStatus() {
        statusLabel.setVisible(false);
    }

    /**
     * Utility to close the current stage (window).
     */
    private void closeWindow() {
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.close();
    }

    /**
     * Wrapper class for displaying session info in the combo box.
     */
    public static class SessionItem {
        private int id;
        private String displayText;

        /**
         * @param id           the session ID
         * @param displayText  formatted display string for the combo box
         */
        public SessionItem(int id, String displayText) {
            this.id = id;
            this.displayText = displayText;
        }

        /** @return the ID of this session */
        public int getId() {
            return id;
        }

        /** @return formatted display text shown in the combo box */
        @Override
        public String toString() {
            return displayText;
        }
    }
}
