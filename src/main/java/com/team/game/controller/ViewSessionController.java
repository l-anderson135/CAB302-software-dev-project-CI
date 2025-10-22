package main.java.com.team.game.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import main.java.com.team.game.Main;
import main.java.com.team.game.model.GameSession;
import main.java.com.team.game.service.GameService;
import main.java.com.team.game.model.User;

import java.net.URL;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for the "My Sessions" screen.
 * <p>
 * Displays all saved {@link GameSession}s for the current user,
 * including score, strikes, completion status, and timestamps.
 * Supports refreshing and returning to the main menu.
 */
public class ViewSessionController implements Initializable {

    private static final ZoneId LOCAL_TZ = ZoneId.of("Australia/Brisbane");
    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");

    @FXML private TableView<SessionRow> sessionsTable;
    @FXML private TableColumn<SessionRow, Integer> idColumn;
    @FXML private TableColumn<SessionRow, String> modeColumn;
    @FXML private TableColumn<SessionRow, Integer> scoreColumn;
    @FXML private TableColumn<SessionRow, Integer> strikesColumn;
    @FXML private TableColumn<SessionRow, String> completedColumn;
    @FXML private TableColumn<SessionRow, String> startedColumn;
    @FXML private TableColumn<SessionRow, String> endedColumn;
    @FXML private Label noSessionsLabel;
    @FXML private Button refreshButton;
    @FXML private Button backButton;

    private GameService gameService;
    private User currentUser;

    /**
     * Initializes the controller after FXML loading.
     * <p>
     * Retrieves the active {@link GameService} and {@link User}
     * from {@link Main.MenuApp}, configures the table, and loads sessions.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        gameService = Main.MenuApp.getGameService();
        currentUser = Main.MenuApp.getCurrentUser();

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        modeColumn.setCellValueFactory(new PropertyValueFactory<>("mode"));
        scoreColumn.setCellValueFactory(new PropertyValueFactory<>("score"));
        strikesColumn.setCellValueFactory(new PropertyValueFactory<>("strikes"));
        completedColumn.setCellValueFactory(new PropertyValueFactory<>("completed"));
        startedColumn.setCellValueFactory(new PropertyValueFactory<>("started"));
        endedColumn.setCellValueFactory(new PropertyValueFactory<>("ended"));

        loadSessions();
    }

    /**
     * Loads all sessions for the current user and populates the table.
     * Displays a message if no sessions are found.
     */
    private void loadSessions() {
        if (gameService == null || currentUser == null) {
            showNoSessions();
            return;
        }

        List<GameSession> sessions = gameService.listSessionsByUser(currentUser);

        if (sessions.isEmpty()) {
            showNoSessions();
        } else {
            hideNoSessions();
            ObservableList<SessionRow> sessionRows = FXCollections.observableArrayList();

            for (GameSession session : sessions) {
                String startedLocal = session.getStartedAt().atZone(LOCAL_TZ).format(DT_FMT);
                String endedLocal = (session.getEndedAt() == null)
                        ? "-" : session.getEndedAt().atZone(LOCAL_TZ).format(DT_FMT);

                SessionRow row = new SessionRow(
                        session.getId(),
                        session.getMode().toString(),
                        session.getScore(),
                        session.getStrikes(),
                        session.isCompleted() ? "Yes" : "No",
                        startedLocal,
                        endedLocal
                );

                sessionRows.add(row);
            }

            sessionsTable.setItems(sessionRows);
        }
    }

    /**
     * Displays the "no sessions" placeholder view.
     */
    private void showNoSessions() {
        sessionsTable.setVisible(false);
        noSessionsLabel.setVisible(true);
    }

    /**
     * Shows the session table and hides the placeholder message.
     */
    private void hideNoSessions() {
        sessionsTable.setVisible(true);
        noSessionsLabel.setVisible(false);
    }

    /**
     * Reloads session data from the database when the user clicks "Refresh".
     *
     * @param actionEvent the originating UI event
     */
    @FXML
    public void handleRefresh(ActionEvent actionEvent) {
        System.out.println("Refreshing sessions...");
        loadSessions();
    }

    /**
     * Closes the current window and returns to the main menu.
     *
     * @param actionEvent the originating UI event
     */
    @FXML
    public void handleBack(ActionEvent actionEvent) {
        System.out.println("Back to menu");
        closeWindow();
    }

    /**
     * Utility method to close the current window.
     */
    private void closeWindow() {
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.close();
    }

    /**
     * Inner class representing a single table row in the sessions view.
     * Used to bind {@link GameSession} data to the JavaFX TableView.
     */
    public static class SessionRow {
        private Integer id;
        private String mode;
        private Integer score;
        private Integer strikes;
        private String completed;
        private String started;
        private String ended;

        /**
         * Constructs a new session row.
         *
         * @param id         session ID
         * @param mode       game mode name
         * @param score      score achieved
         * @param strikes    number of strikes
         * @param completed  completion status ("Yes"/"No")
         * @param started    formatted start time
         * @param ended      formatted end time
         */
        public SessionRow(Integer id, String mode, Integer score, Integer strikes,
                          String completed, String started, String ended) {
            this.id = id;
            this.mode = mode;
            this.score = score;
            this.strikes = strikes;
            this.completed = completed;
            this.started = started;
            this.ended = ended;
        }

        public Integer getId() { return id; }
        public String getMode() { return mode; }
        public Integer getScore() { return score; }
        public Integer getStrikes() { return strikes; }
        public String getCompleted() { return completed; }
        public String getStarted() { return started; }
        public String getEnded() { return ended; }

        public void setId(Integer id) { this.id = id; }
        public void setMode(String mode) { this.mode = mode; }
        public void setScore(Integer score) { this.score = score; }
        public void setStrikes(Integer strikes) { this.strikes = strikes; }
        public void setCompleted(String completed) { this.completed = completed; }
        public void setStarted(String started) { this.started = started; }
        public void setEnded(String ended) { this.ended = ended; }
    }
}
