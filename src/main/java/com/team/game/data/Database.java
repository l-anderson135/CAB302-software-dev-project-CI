package main.java.com.team.game.data;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Utility class responsible for managing the SQLite database connection.
 * <p>
 * Ensures the database directory exists, loads the SQLite JDBC driver,
 * and provides a static method for obtaining a connection with
 * foreign key support enabled.
 */
public final class Database {

    /** Path to the SQLite database file. */
    private static final String URL = "jdbc:sqlite:data/game.db";

    // Static initializer: creates directory and loads JDBC driver once
    static {
        try {
            Files.createDirectories(Path.of("data"));
            Class.forName("org.sqlite.JDBC");
        } catch (Exception e) {
            throw new RuntimeException("DB init failed", e);
        }
    }

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private Database() {}

    /**
     * Opens a new connection to the SQLite database.
     * <p>
     * Automatically enables foreign key constraints to ensure referential integrity.
     *
     * @return a new {@link Connection} to the database
     * @throws SQLException if the connection cannot be established
     */
    public static Connection open() throws SQLException {
        Connection c = DriverManager.getConnection(URL);
        try (var st = c.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON");
        }
        return c;
    }
}
