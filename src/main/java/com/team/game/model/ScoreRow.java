package main.java.com.team.game.model;

/**
 * Represents a leaderboard entry showing a user's high score for a specific game mode.
 * <p>
 * Each {@code ScoreRow} corresponds to one user and one mode, containing
 * their highest recorded score used for ranking in leaderboards.
 */
public final class ScoreRow {

    private final int userId;
    private final String username;
    private final GameMode mode;
    private final int highScore;

    /**
     * Constructs a new {@code ScoreRow}.
     *
     * @param userId    unique ID of the user
     * @param username  username associated with the score
     * @param mode      game mode the score was achieved in
     * @param highScore the user’s best score for this mode
     */
    public ScoreRow(int userId, String username, GameMode mode, int highScore) {
        this.userId = userId;
        this.username = username;
        this.mode = mode;
        this.highScore = highScore;
    }

    /** @return the user’s unique ID */
    public int getUserId() { return userId; }

    /** @return the username associated with this score entry */
    public String getUsername() { return username; }

    /** @return the game mode this score applies to */
    public GameMode getMode() { return mode; }

    /** @return the user’s highest recorded score */
    public int getHighScore() { return highScore; }
}
