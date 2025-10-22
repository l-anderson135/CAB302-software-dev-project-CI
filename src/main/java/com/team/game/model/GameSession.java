package main.java.com.team.game.model;

import java.time.Instant;

/**
 * Immutable data model representing a single game session.
 * <p>
 * Each session belongs to a user and tracks progress (score, strikes),
 * the mode played, and timestamps for when the session started and ended.
 */
public final class GameSession {

    private final int id;
    private final int userId;
    private final GameMode mode;
    private final Instant startedAt;
    private final Instant endedAt;   // null until the session is finished
    private final int score;
    private final int strikes;
    private final boolean completed;

    /**
     * Constructs a new {@code GameSession} instance.
     *
     * @param id         unique identifier for this session
     * @param userId     ID of the user who owns this session
     * @param mode       the {@link GameMode} played
     * @param startedAt  timestamp when the session began
     * @param endedAt    timestamp when the session ended (nullable)
     * @param score      total score achieved
     * @param strikes    number of incorrect answers recorded
     * @param completed  true if the session has ended, false if ongoing
     */
    public GameSession(int id, int userId, GameMode mode, Instant startedAt,
                       Instant endedAt, int score, int strikes, boolean completed) {
        this.id = id;
        this.userId = userId;
        this.mode = mode;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.score = score;
        this.strikes = strikes;
        this.completed = completed;
    }

    /** @return the unique session ID */
    public int getId() { return id; }

    /** @return the ID of the user who played this session */
    public int getUserId() { return userId; }

    /** @return the game mode (e.g., BASICS, TRIG, TARGET) */
    public GameMode getMode() { return mode; }

    /** @return timestamp of when the session started */
    public Instant getStartedAt() { return startedAt; }

    /** @return timestamp of when the session ended, or {@code null} if still in progress */
    public Instant getEndedAt() { return endedAt; }

    /** @return the score accumulated in this session */
    public int getScore() { return score; }

    /** @return the number of wrong answers recorded */
    public int getStrikes() { return strikes; }

    /** @return true if the session is completed, false otherwise */
    public boolean isCompleted() { return completed; }
}
