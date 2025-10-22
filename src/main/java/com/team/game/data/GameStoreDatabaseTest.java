package main.java.com.team.game.data;

import main.java.com.team.game.model.GameMode;
import main.java.com.team.game.model.GameSession;
import main.java.com.team.game.model.ScoreRow;
import main.java.com.team.game.model.User;

import org.junit.jupiter.api.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link GameStore} backed by the real SQLite DB.
 * <p>
 * Uses a temporary working directory and clears all tables between tests.
 * Verifies schema creation, user auth and updates, session lifecycle,
 * high score queries, leaderboard ranking, ordering, and foreign key behavior.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GameStoreDatabaseTest {

    private static Path tempRoot;
    private static GameStore store;

    /**
     * Creates a temp working directory, initializes the DB (via {@link Database}),
     * constructs a {@link GameStore}, and truncates all tables once before the suite.
     */
    @BeforeAll
    static void setupWorkingDirAndInit() throws Exception {
        tempRoot = Files.createTempDirectory("cab302-dbtests-");
        System.setProperty("user.dir", tempRoot.toAbsolutePath().toString());
        Class.forName(Database.class.getName());   // load/initialize once
        store = new GameStore();
        truncateAll();
    }

    /**
     * Clears all tables after each test to guarantee isolation.
     */
    @AfterEach
    void cleanBetweenTests() throws Exception { truncateAll(); }

    /**
     * Utility: removes all rows from {@code users} and {@code game_session}.
     */
    private static void truncateAll() throws SQLException {
        try (Connection c = Database.open()) {
            c.createStatement().execute("DELETE FROM game_session");
            c.createStatement().execute("DELETE FROM users");
        }
    }

    /**
     * Verifies the schema (tables) were created by {@link GameStore#GameStore()}.
     */
    @Test @Order(1)
    void testSchemaWasCreated() throws Exception {
        try (Connection c = Database.open()) {
            assertTrue(c.getMetaData().getTables(null, null, "users", null).next());
            assertTrue(c.getMetaData().getTables(null, null, "game_session", null).next());
        }
    }

    /**
     * Ensures users are listed in alphabetical order and registration timestamps look sane.
     */
    @Test @Order(2)
    void testCreateUserAndListUsers_orderedByUsername() {
        store.createUser("bob", "pw".toCharArray());
        store.createUser("alice", "pw".toCharArray());
        store.createUser("charlie", "pw".toCharArray());

        List<User> users = store.listUsers();
        assertEquals(3, users.size());
        assertEquals("alice", users.get(0).getUsername());
        assertEquals("bob", users.get(1).getUsername());
        assertEquals("charlie", users.get(2).getUsername());

        for (User u : users) {
            assertNotNull(u.getRegisteredAt());
            assertTrue(u.getRegisteredAt().isBefore(Instant.now().plusSeconds(5)));
        }
    }

    /**
     * Validates case-insensitive uniqueness on usernames.
     */
    @Test @Order(3)
    void testUsernameUniqueness_caseInsensitive() {
        store.createUser("Alice", "x".toCharArray());
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> store.createUser("alice", "y".toCharArray()));
        assertTrue(ex.getMessage().toLowerCase().contains("taken"));
    }

    /**
     * Checks username/password authentication including case-insensitive username match.
     */
    @Test @Order(4)
    void testAuthenticate_successAndFailure() {
        store.createUser("sam", "123".toCharArray());
        assertTrue(store.authenticate("sam", "123".toCharArray()).isPresent());
        assertTrue(store.authenticate("SAM", "123".toCharArray()).isPresent());
        assertTrue(store.authenticate("sam", "wrong".toCharArray()).isEmpty());
        assertTrue(store.authenticate("unknown", "123".toCharArray()).isEmpty());
    }

    /**
     * Ensures updateUsername enforces uniqueness and can successfully change non-conflicting names.
     */
    @Test @Order(5)
    void testUpdateUsername_conflictAndSuccess() {
        User u1 = store.createUser("eva", "x".toCharArray());
        User u2 = store.createUser("mike", "x".toCharArray());

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> store.updateUsername(u2.getId(), "EVA"));
        assertTrue(ex.getMessage().toLowerCase().contains("taken"));

        store.updateUsername(u2.getId(), "michael");
        var listed = store.listUsers();
        assertTrue(listed.stream().anyMatch(u -> "michael".equals(u.getUsername())));
    }

    /**
     * Verifies password update changes the credential used by authenticate().
     */
    @Test @Order(6)
    void testUpdatePassword() {
        User u = store.createUser("pat", "old".toCharArray());
        assertTrue(store.authenticate("pat", "old".toCharArray()).isPresent());

        store.updatePassword(u.getId(), "new".toCharArray());
        assertTrue(store.authenticate("pat", "old".toCharArray()).isEmpty());
        assertTrue(store.authenticate("pat", "new".toCharArray()).isPresent());
    }

    /**
     * Confirms that deleting a user cascades and removes their sessions (FK ON DELETE CASCADE).
     */
    @Test @Order(7)
    void testDeleteUser_cascadesSessions() {
        User u = store.createUser("cascadee", "pw".toCharArray());
        GameSession s1 = store.startSession(u.getId(), GameMode.BASICS);
        GameSession s2 = store.startSession(u.getId(), GameMode.TRIG);

        assertEquals(u.getId(), s1.getUserId());
        assertEquals(u.getId(), s2.getUserId());
        assertEquals(2, store.listSessionsByUser(u.getId()).size());

        assertTrue(store.deleteUser(u.getId()));
        assertEquals(0, store.listSessionsByUser(u.getId()).size());
    }

    /**
     * Exercises the session lifecycle: start → submit (correct/wrong) → auto-complete at 3 strikes.
     */
    @Test @Order(8)
    void testStartSubmitFinishSession_flowAndStrikesAutoComplete() {
        User u = store.createUser("runner", "pw".toCharArray());
        GameSession s = store.startSession(u.getId(), GameMode.BASICS);

        assertNotNull(s);
        assertEquals(0, s.getScore());
        assertEquals(0, s.getStrikes());
        assertFalse(s.isCompleted());
        assertNotNull(s.getStartedAt());
        assertNull(s.getEndedAt());

        store.submitCorrect(s.getId());
        store.submitWrong(s.getId());
        store.submitWrong(s.getId());
        store.submitWrong(s.getId());

        GameSession after = store.listSessionsByUser(u.getId())
                .stream().filter(gs -> gs.getId() == s.getId())
                .findFirst().orElseThrow();

        assertEquals(1, after.getScore());
        assertEquals(3, after.getStrikes());
        assertTrue(after.isCompleted());
        assertNotNull(after.getEndedAt());
    }

    /**
     * Ensures finishSession() sets completed=1 and stamps an end time.
     */
    @Test @Order(9)
    void testFinishSession_setsCompletedAndEndTime() {
        User u = store.createUser("finisher", "pw".toCharArray());
        GameSession s = store.startSession(u.getId(), GameMode.TRIG);

        store.finishSession(s.getId());

        GameSession reloaded = store.listSessionsByUser(u.getId()).get(0);
        assertTrue(reloaded.isCompleted());
        assertNotNull(reloaded.getEndedAt());
    }

    /**
     * Validates ordering by started time desc (and ID to break ties).
     */
    @Test @Order(10)
    void testListSessionsByUser_sortedByStartedDesc() throws InterruptedException {
        User u = store.createUser("chronos", "pw".toCharArray());
        GameSession s1 = store.startSession(u.getId(), GameMode.BASICS);
        Thread.sleep(5);
        GameSession s2 = store.startSession(u.getId(), GameMode.TRIG);

        List<GameSession> list = store.listSessionsByUser(u.getId());
        assertEquals(2, list.size());
        assertEquals(s2.getId(), list.get(0).getId());
        assertEquals(s1.getId(), list.get(1).getId());
    }

    /**
     * Confirms deleteSession() returns true and removes the record.
     */
    @Test @Order(11)
    void testDeleteSession_returnsTrueWhenDeleted() {
        User u = store.createUser("deleter", "pw".toCharArray());
        GameSession s = store.startSession(u.getId(), GameMode.BASICS);
        assertEquals(1, store.listSessionsByUser(u.getId()).size());

        assertTrue(store.deleteSession(s.getId()));
        assertEquals(0, store.listSessionsByUser(u.getId()).size());
    }

    /**
     * Verifies getHighScore() returns empty when none exist and updates to the correct max value.
     */
    @Test @Order(12)
    void testGetHighScore_emptyAndValue() {
        User u = store.createUser("scorer", "pw".toCharArray());
        assertTrue(store.getHighScore(u.getId(), GameMode.BASICS).isEmpty());

        GameSession s1 = store.startSession(u.getId(), GameMode.BASICS);
        store.submitCorrect(s1.getId());
        store.finishSession(s1.getId());

        GameSession s2 = store.startSession(u.getId(), GameMode.BASICS);
        store.submitCorrect(s2.getId());
        store.submitCorrect(s2.getId());
        store.finishSession(s2.getId());

        var hs = store.getHighScore(u.getId(), GameMode.BASICS);
        assertTrue(hs.isPresent());
        assertEquals(2, hs.getAsInt());
    }

    /**
     * Ensures leaderboard is per-mode, sorted by high score desc, and respects the limit.
     */
    @Test @Order(13)
    void testLeaderboard_sortedAndLimited_perMode() {
        User u1 = store.createUser("lb_alice", "pw".toCharArray());
        User u2 = store.createUser("lb_bob", "pw".toCharArray());
        User u3 = store.createUser("lb_cara", "pw".toCharArray());

        GameSession a = store.startSession(u1.getId(), GameMode.BASICS);
        store.submitCorrect(a.getId()); store.submitCorrect(a.getId()); store.submitCorrect(a.getId());
        store.finishSession(a.getId());

        GameSession b = store.startSession(u2.getId(), GameMode.BASICS);
        store.submitCorrect(b.getId()); store.finishSession(b.getId());

        GameSession c = store.startSession(u3.getId(), GameMode.TRIG);
        for (int i = 0; i < 5; i++) store.submitCorrect(c.getId());
        store.finishSession(c.getId());

        List<ScoreRow> basicsTop = store.leaderboard(GameMode.BASICS, 10);
        assertEquals(2, basicsTop.size());
        assertEquals("lb_alice", basicsTop.get(0).getUsername());
        assertEquals(3, basicsTop.get(0).getHighScore());
        assertEquals("lb_bob", basicsTop.get(1).getUsername());
        assertEquals(1, basicsTop.get(1).getHighScore());

        List<ScoreRow> limited = store.leaderboard(GameMode.BASICS, 1);
        assertEquals(1, limited.size());
        assertEquals("lb_alice", limited.get(0).getUsername());
        assertEquals(3, limited.get(0).getHighScore());
    }

    /**
     * Confirms SQLite foreign_keys pragma is ON for every connection opened by {@link Database}.
     */
    @Test @Order(14)
    void testForeignKeysPragmaOn() throws Exception {
        try (Connection c = Database.open();
             var rs = c.createStatement().executeQuery("PRAGMA foreign_keys")) {
            assertTrue(rs.next());
            assertEquals(1, rs.getInt(1));
        }
    }
}
