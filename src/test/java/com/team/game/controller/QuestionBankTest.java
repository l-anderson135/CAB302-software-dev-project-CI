package test.java.com.team.game.controller;

import main.java.com.team.game.model.Question;
import main.java.com.team.game.model.QuestionBank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class QuestionBankTest {
    private QuestionBank questionBank;

    @BeforeEach
    void setUp() {
        questionBank = new QuestionBank();
    }

    @Test
    void basicsQuestionsShouldNotBeEmpty() {
        List<Question> basics = questionBank.getBasics();
        assertNotNull(basics, "Basics questions list should not be null");
        assertFalse(basics.isEmpty(), "Basics questions list should not be empty");
    }

    @Test
    void trigoQuestionsShouldNotBeEmpty() {
        List<Question> trigo = questionBank.getTrigo();
        assertNotNull(trigo, "Trigo questions list should not be null");
        assertFalse(trigo.isEmpty(), "Trigo questions list should not be empty");
    }

    @Test
    void basicsShouldContainExpectedNumberOfQuestions() {
        List<Question> basics = questionBank.getBasics();
        assertEquals(10, basics.size(),
                "Expected 10 basic questions but found only " + basics.size());
    }

    @Test
    void allQuestionsShouldHaveValidText() {
        List<Question> basics = questionBank.getBasics();
        List<Question> trigo = questionBank.getTrigo();

        for (Question q : basics) {
            assertNotNull(q.getText(), "Question text should not be null");
            assertFalse(q.getText().trim().isEmpty(), "Question text should not be empty");
        }

        for (Question q : trigo) {
            assertNotNull(q.getText(), "Question text should not be null");
            assertFalse(q.getText().trim().isEmpty(), "Question text should not be empty");
        }
    }

    @Test
    void questionsShouldHaveAtLeastOneAnswer() {
        List<Question> basics = questionBank.getBasics();
        List<Question> trigo = questionBank.getTrigo();

        for (Question q : basics) {
            assertNotNull(q.getAnswer(), "Answers list should not be null");
            assertFalse(q.getAnswer().isEmpty(), "Answers list should not be empty");
        }

        for (Question q : trigo) {
            assertNotNull(q.getAnswer(), "Answers list should not be null");
            assertFalse(q.getAnswer().isEmpty(), "Answers list should not be empty");
        }
    }

    @Test
    void questionsShouldBeUniqueInEachBank() {
        List<Question> basics = questionBank.getBasics();
        List<Question> trigo = questionBank.getTrigo();

        Set<String> basicQuestionsText = new HashSet<>();
        for (Question q : basics) {
            assertTrue(basicQuestionsText.add(q.getText()), "Duplicate question found in basics");
        }

        Set<String> trigoQuestionsText = new HashSet<>();
        for (Question q : trigo) {
            assertTrue(trigoQuestionsText.add(q.getText()), "Duplicate question found in trigo");
        }
    }

    @Test
    void basicsAndTrigoShouldNotOverlap() {
        List<Question> basics = questionBank.getBasics();
        List<Question> trigo = questionBank.getTrigo();

        Set<String> basicsText = new HashSet<>();
        for (Question q : basics) {
            basicsText.add(q.getText());
        }

        for (Question q : trigo) {
            assertFalse(basicsText.contains(q.getText()),
                    "Question appears in both basics and trigo: " + q.getText());
        }
    }
}