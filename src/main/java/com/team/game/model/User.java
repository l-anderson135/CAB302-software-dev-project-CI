package main.java.com.team.game.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a user in the game system.
 * <p>
 * Each {@code User} has a unique ID, a username, and a registration timestamp.
 * This data is persisted in the database and used for authentication,
 * leaderboard tracking, and session management.
 */
public final class User {

    private final int id;
    private final String username;
    private final Instant registeredAt;

    /**
     * Constructs a new immutable {@code User}.
     *
     * @param id            the unique user ID
     * @param username      the user’s chosen username
     * @param registeredAt  the time the account was created
     */
    public User(int id, String username, Instant registeredAt) {
        this.id = id;
        this.username = Objects.requireNonNull(username);
        this.registeredAt = Objects.requireNonNull(registeredAt);
    }

    /** @return the user’s unique ID */
    public int getId() { return id; }

    /** @return the username associated with this account */
    public String getUsername() { return username; }

    /** @return the registration timestamp for the user */
    public Instant getRegisteredAt() { return registeredAt; }
}
