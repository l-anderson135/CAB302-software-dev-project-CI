package main.java.com.team.game.service;

import main.java.com.team.game.data.GameStore;
import main.java.com.team.game.model.*;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * Provides a clean, high-level interface between the UI layer and the database layer.
 * <p>
 * The {@code GameService} acts as a facade around {@link GameStore},
 * handling user management, session tracking, score queries, and access
 * to static question banks. It’s used by controllers throughout the app
 * to keep UI logic separate from persistence and data concerns.
 */
public final class GameService {

    private final GameStore store;

    /**
     * Constructs a {@code GameService} that wraps the given {@link GameStore}.
     *
     * @param store the underlying data store that manages persistence
     */
    public GameService(GameStore store) {
        this.store = store;
    }

    // ---------------------------------------------------------------------
    // AUTHENTICATION & USER MANAGEMENT
    // ---------------------------------------------------------------------

    /** Registers a new user account. */
    public User register(String username, char[] password) {
        return store.createUser(username, password);
    }

    /** Attempts to authenticate an existing user by username and password. */
    public Optional<User> login(String username, char[] password) {
        return store.authenticate(username, password);
    }

    /** Returns all users currently stored in the database. */
    public List<User> listUsers() {
        return store.listUsers();
    }

    /** Updates a user’s username, enforcing uniqueness checks. */
    public void updateUsername(User user, String newName) {
        store.updateUsername(user.getId(), newName);
    }

    /** Updates a user’s password. */
    public void updatePassword(User user, char[] newPw) {
        store.updatePassword(user.getId(), newPw);
    }

    /** Deletes a user and all associated sessions (cascade delete). */
    public boolean deleteUser(User user) {
        return store.deleteUser(user.getId());
    }

    // ---------------------------------------------------------------------
    // GAME SESSION MANAGEMENT
    // ---------------------------------------------------------------------

    /** Starts a new round/session for the given user and game mode. */
    public GameSession startRound(User user, GameMode mode) {
        return store.startSession(user.getId(), mode);
    }

    /** Records a correct answer in the current game session. */
    public void submitCorrect(GameSession s) {
        store.submitCorrect(s.getId());
    }

    /** Records an incorrect answer in the current game session. */
    public void submitWrong(GameSession s) {
        store.submitWrong(s.getId());
    }

    /** Marks a game session as finished and timestamps its completion. */
    public void finishRound(GameSession s) {
        store.finishSession(s.getId());
    }

    /** Returns all sessions belonging to a particular user, ordered by date. */
    public List<GameSession> listSessionsByUser(User user) {
        return store.listSessionsByUser(user.getId());
    }

    /** Deletes a single session by ID. */
    public boolean deleteSession(int sessionId) {
        return store.deleteSession(sessionId);
    }

    // ---------------------------------------------------------------------
    // SCORING & LEADERBOARD
    // ---------------------------------------------------------------------

    /** Retrieves the user’s highest recorded score for the given mode. */
    public OptionalInt highScore(User user, GameMode mode) {
        return store.getHighScore(user.getId(), mode);
    }

    /** Returns a leaderboard of top scores for the specified game mode. */
    public List<ScoreRow> leaderboard(GameMode mode, int limit) {
        return store.leaderboard(mode, limit);
    }

    // ---------------------------------------------------------------------
    // QUESTION BANK ACCESS
    // ---------------------------------------------------------------------

    /** Centralized in-memory question bank shared across all game modes. */
    private final QuestionBank qb = new QuestionBank();

    /** @return list of questions for the Basics game mode */
    public List<Question> getBasicsQuestions() {
        return qb.getBasics();
    }

    /** @return list of questions for the Trigonometry game mode */
    public List<Question> getTrigoQuestions() {
        return qb.getTrigo();
    }

    /** @return list of questions for the Target (projectile) game mode */
    public List<Question> getTargetQuestions() {
        return qb.getTarget();
    }
}
