package test.java.com.team.game.controller;

import main.java.com.team.game.controller.BasicsGameController;
import main.java.com.team.game.service.GameService;
import main.java.com.team.game.model.User;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class BasicsControllerTest {

    private final BasicsGameController controller = new BasicsGameController();

    @Test
    void testIsAnswerCorrect_MultipleChoiceExactMatch() {
        assertTrue(controller.isAnswerCorrect("v = u + at", "v = u + at", true));
    }

    @Test
    void testIsAnswerCorrect_MultipleChoiceCaseInsensitive() {
        assertTrue(controller.isAnswerCorrect("V = U + AT", "v = u + at", true));
    }

    @Test
    void testIsAnswerCorrect_MultipleChoiceWhitespaceInsensitive() {
        assertTrue(controller.isAnswerCorrect("  v=u+at  ", "v = u + at", true));
    }

    @Test
    void testIsAnswerCorrect_NumericExactMatch() {
        assertTrue(controller.isAnswerCorrect("5", "5", false));
    }

    @Test
    void testIsAnswerCorrect_NumericWithTolerance() {
        assertTrue(controller.isAnswerCorrect("5.01", "5", false));
        assertTrue(controller.isAnswerCorrect("4.99", "5", false));
    }

    @Test
    void testIsAnswerCorrect_NumericOutsideTolerance() {
        assertFalse(controller.isAnswerCorrect("5.02", "5", false));
        assertFalse(controller.isAnswerCorrect("4.98", "5", false));
    }

    @Test
    void testIsAnswerCorrect_NumericDecimalEquivalence() {
        assertTrue(controller.isAnswerCorrect("0.5", "1/2", false));
    }

    @Test
    void testNormalizeAnswer_MultipleChoice() throws Exception {
        var method = BasicsGameController.class.getDeclaredMethod("normalizeAnswer", String.class, boolean.class);
        method.setAccessible(true);

        String result = (String) method.invoke(controller, "  V = U + AT  ", true);
        assertEquals("v=u+at", result);
    }

    @Test
    void testNormalizeAnswer_Numeric() throws Exception {
        var method = BasicsGameController.class.getDeclaredMethod("normalizeAnswer", String.class, boolean.class);
        method.setAccessible(true);

        String result = (String) method.invoke(controller, "  5.0  ", false);
        assertEquals("5.0", result);
    }

    @Test
    void testSetDependencies_WithUserAndService() {
        GameService mockService = mock(GameService.class);
        User mockUser = mock(User.class);
        when(mockUser.getUsername()).thenReturn("Alice");

        controller.setDependencies(mockService, mockUser);

        // Verify dependencies were set by testing answer checking still works
        assertTrue(controller.isAnswerCorrect("5", "5", false));
    }

    @Test
    void testSetDependencies_NoUser() {
        GameService mockService = mock(GameService.class);

        controller.setDependencies(mockService, null);

        // Verify answer checking still works without a user
        assertTrue(controller.isAnswerCorrect("5", "5", false));
    }
}

//refactoring and tests added