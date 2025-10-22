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
import main.java.com.team.game.model.User;
import main.java.com.team.game.service.GameService;

import java.net.URL;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for the Users List screen.
 * <p>
 * Fetches all users via {@link GameService}, displays them in a table,
 * and shows summary info (total users). Supports refresh and returning to the menu.
 */
public class UsersListController implements Initializable {

    private static final ZoneId LOCAL_TZ = ZoneId.of("Australia/Brisbane");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @FXML
    private TableView<UserRow> usersTable;

    @FXML
    private TableColumn<UserRow, Integer> idColumn;

    @FXML
    private TableColumn<UserRow, String> usernameColumn;

    @FXML
    private TableColumn<UserRow, String> registeredColumn;

    @FXML
    private Label noUsersLabel;

    @FXML
    private Label totalUsersLabel;

    @FXML
    private Button refreshButton;

    @FXML
    private Button backButton;

    private GameService gameService;

    /**
     * JavaFX lifecycle hook.
     * Initializes service reference, binds table columns, and loads users.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        gameService = Main.MenuApp.getGameService();

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        registeredColumn.setCellValueFactory(new PropertyValueFactory<>("registeredAt"));

        loadUsers();
    }

    /**
     * Loads users from the service and shows them in the table.
     * Displays a placeholder message if none are found or service is unavailable.
     */
    private void loadUsers() {
        if (gameService == null) {
            showNoUsers();
            return;
        }

        List<User> users = gameService.listUsers();

        if (users.isEmpty()) {
            showNoUsers();
        } else {
            showUsers(users);
        }
    }

    /**
     * Populates the table with formatted user rows and updates the total count label.
     *
     * @param users list of users retrieved from the service
     */
    private void showUsers(List<User> users) {
        noUsersLabel.setVisible(false);

        ObservableList<UserRow> userRows = FXCollections.observableArrayList();

        for (User user : users) {
            String formattedDate = user.getRegisteredAt().atZone(LOCAL_TZ).format(DATE_FORMATTER);

            UserRow row = new UserRow(
                    user.getId(),
                    user.getUsername(),
                    formattedDate
            );

            userRows.add(row);
        }

        usersTable.setItems(userRows);
        usersTable.setVisible(true);

        totalUsersLabel.setText("Total users: " + users.size());
        totalUsersLabel.setVisible(true);
    }

    /**
     * Shows the empty state and zero count when there are no users or service is unavailable.
     */
    private void showNoUsers() {
        usersTable.setVisible(false);
        noUsersLabel.setVisible(true);
        totalUsersLabel.setText("Total users: 0");
        totalUsersLabel.setVisible(true);
    }

    /**
     * Refreshes the list of users from the database.
     *
     * @param actionEvent the originating UI event
     */
    @FXML
    public void handleRefresh(ActionEvent actionEvent) {
        System.out.println("Refreshing users list...");
        loadUsers();
    }

    /**
     * Closes the users list window and returns to the previous menu.
     *
     * @param actionEvent the originating UI event
     */
    @FXML
    public void handleBack(ActionEvent actionEvent) {
        System.out.println("Back to menu");
        closeWindow();
    }

    /**
     * Utility to close the current stage (window).
     */
    private void closeWindow() {
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.close();
    }

    /**
     * Row model used by the users table.
     */
    public static class UserRow {
        private Integer id;
        private String username;
        private String registeredAt;

        /**
         * Constructs a table row entry for a user.
         *
         * @param id           user ID
         * @param username     username
         * @param registeredAt formatted registration timestamp (local time)
         */
        public UserRow(Integer id, String username, String registeredAt) {
            this.id = id;
            this.username = username;
            this.registeredAt = registeredAt;
        }

        /** @return user ID */
        public Integer getId() { return id; }
        /** @return username */
        public String getUsername() { return username; }
        /** @return registration timestamp (formatted) */
        public String getRegisteredAt() { return registeredAt; }

        public void setId(Integer id) { this.id = id; }
        public void setUsername(String username) { this.username = username; }
        public void setRegisteredAt(String registeredAt) { this.registeredAt = registeredAt; }
    }
}
