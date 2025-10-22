
package test.java.com.team.game.controller;

import main.java.com.team.game.controller.LoginController;
import main.java.com.team.game.service.GameService;
import main.java.com.team.game.model.User;
import main.java.com.team.game.data.GameStore;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class LoginControllerTest {

    private LoginController controller;
    private GameService gameService;

    @BeforeEach
    public void setUp() {
        controller = new LoginController();
        gameService = new GameService(new GameStore());
        controller.setDependencies(gameService, user -> {});
    }

    @Test
    void testRegisterUser_NewUser() {

        String username = "testUser123";
        char[] password = "password123".toCharArray();


        User result = controller.registerUser(username, password);

        assertNotNull(result);
        assertEquals(username, result.getUsername());
        assertTrue(result.getId() > 0);
        assertNotNull(result.getRegisteredAt());
    }

    @Test
    void testRegisterUser_DuplicateUsername() {

        String username = "duplicateUser";
        char[] password = "password123".toCharArray();

        controller.registerUser(username, password);
        assertThrows(IllegalStateException.class, () -> {
            controller.registerUser(username, password);
        });
    }

    @Test
    void testCheckUser_ValidCredentials() {

        String username = "validUser";
        char[] password = "validPassword".toCharArray();

        controller.registerUser(username, password);

        Optional<User> result = controller.checkUser(username, password);

        assertTrue(result.isPresent());
        assertEquals(username, result.get().getUsername());
    }

    @Test
    void testCheckUser_InvalidCredentials() {
        String username = "nonexistentUser";
        char[] password = "wrongPassword".toCharArray();


        Optional<User> result = controller.checkUser(username, password);

        assertFalse(result.isPresent());
    }

    @Test
    void testCheckUser_WrongPassword() {
        String username = "testUser";
        char[] correctPassword = "correctPassword".toCharArray();
        char[] wrongPassword = "wrongPassword".toCharArray();

        controller.registerUser(username, correctPassword);


        Optional<User> result = controller.checkUser(username, wrongPassword);
        assertFalse(result.isPresent());
    }
}
