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
import main.java.com.team.game.model.GameMode;
import main.java.com.team.game.model.ScoreRow;
import main.java.com.team.game.service.GameService;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for the Leaderboard screen.
 * <p>
 * Displays the top players for each {@link GameMode}.
 * Fetches leaderboard data from {@link GameService}, formats it for display,
 * and allows users to switch between different game mode leaderboards.
 */
public class LeaderboardController implements Initializable {

    @FXML
    private Button basicsButton;

    @FXML
    private Button trigButton;

    @FXML
    private Button targetButton;

    @FXML
    private Button backButton;

    @FXML
    private Label selectedModeLabel;

    @FXML
    private TableView<LeaderboardRow> leaderboardTable;

    @FXML
    private TableColumn<LeaderboardRow, Integer> rankColumn;

    @FXML
    private TableColumn<LeaderboardRow, String> usernameColumn;

    @FXML
    private TableColumn<LeaderboardRow, Integer> highScoreColumn;

    @FXML
    private Label noScoresLabel;

    @FXML
    private Label instructionLabel;

    private GameService gameService;

    /**
     * JavaFX lifecycle hook.
     * Initializes the leaderboard table columns and binds them to data properties.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        gameService = Main.MenuApp.getGameService();

        rankColumn.setCellValueFactory(new PropertyValueFactory<>("rank"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        highScoreColumn.setCellValueFactory(new PropertyValueFactory<>("highScore"));
    }

    /**
     * Displays the leaderboard for the Basics game mode.
     *
     * @param actionEvent the originating UI event
     */
    @FXML
    public void handleBasicsLeaderboard(ActionEvent actionEvent) {
        System.out.println("Basics leaderboard selected");
        loadLeaderboard(GameMode.BASICS);
    }

    /**
     * Displays the leaderboard for the Trig game mode.
     *
     * @param actionEvent the originating UI event
     */
    @FXML
    public void handleTrigLeaderboard(ActionEvent actionEvent) {
        System.out.println("Trig leaderboard selected");
        loadLeaderboard(GameMode.TRIG);
    }

    /**
     * Displays the leaderboard for the Target game mode.
     *
     * @param actionEvent the originating UI event
     */
    @FXML
    public void handleTargetLeaderboard(ActionEvent actionEvent) {
        System.out.println("Target leaderboard selected");
        loadLeaderboard(GameMode.TARGET);
    }

    /**
     * Loads leaderboard data for the specified game mode from {@link GameService}.
     * If no data exists, displays a "no scores" message.
     *
     * @param mode the game mode whose leaderboard should be displayed
     */
    private void loadLeaderboard(GameMode mode) {
        if (gameService == null) {
            showNoScores();
            return;
        }

        selectedModeLabel.setText(mode.toString() + " LEADERBOARD");
        selectedModeLabel.setVisible(true);
        instructionLabel.setVisible(false);

        List<ScoreRow> rows = gameService.leaderboard(mode, 10);

        if (rows.isEmpty()) {
            showNoScores();
        } else {
            showLeaderboard(rows);
        }
    }

    /**
     * Populates the leaderboard table with player rankings and scores.
     *
     * @param scoreRows list of scores retrieved from the service
     */
    private void showLeaderboard(List<ScoreRow> scoreRows) {
        noScoresLabel.setVisible(false);

        ObservableList<LeaderboardRow> leaderboardRows = FXCollections.observableArrayList();

        int rank = 1;
        for (ScoreRow scoreRow : scoreRows) {
            LeaderboardRow row = new LeaderboardRow(rank, scoreRow.getUsername(), scoreRow.getHighScore());
            leaderboardRows.add(row);
            rank++;
        }

        leaderboardTable.setItems(leaderboardRows);
        leaderboardTable.setVisible(true);
    }

    /**
     * Displays a message indicating that no scores were found.
     */
    private void showNoScores() {
        leaderboardTable.setVisible(false);
        noScoresLabel.setVisible(true);
    }

    /**
     * Closes the leaderboard window and returns to the previous screen.
     *
     * @param actionEvent the originating UI event
     */
    @FXML
    public void handleBack(ActionEvent actionEvent) {
        System.out.println("Back to menu");
        closeWindow();
    }

    /**
     * Utility method to close the current stage (window).
     */
    private void closeWindow() {
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.close();
    }

    /**
     * Represents a single row in the leaderboard table.
     * Stores a player’s rank, username, and high score.
     */
    public static class LeaderboardRow {
        private Integer rank;
        private String username;
        private Integer highScore;

        /**
         * Constructs a leaderboard row entry.
         *
         * @param rank       the player’s position
         * @param username   the player’s username
         * @param highScore  the player’s top score
         */
        public LeaderboardRow(Integer rank, String username, Integer highScore) {
            this.rank = rank;
            this.username = username;
            this.highScore = highScore;
        }

        /** @return the player’s rank in the leaderboard */
        public Integer getRank() { return rank; }

        /** @return the player’s username */
        public String getUsername() { return username; }

        /** @return the player’s highest score */
        public Integer getHighScore() { return highScore; }

        public void setRank(Integer rank) { this.rank = rank; }
        public void setUsername(String username) { this.username = username; }
        public void setHighScore(Integer highScore) { this.highScore = highScore; }
    }
}
