package main.java.com.team.game.model;

import java.util.List;

/**
 * Represents a single quiz question used in the game.
 * <p>
 * Each question has a text prompt, a correct answer,
 * and an optional list of multiple-choice options (used for Basics mode).
 */
public class Question {
    private String text;
    private String answer;
    private List<String> options;  // optional (null for free-response or Trig mode)

    /**
     * Constructs a free-response question with only text and an answer.
     *
     * @param text   the question text
     * @param answer the correct answer
     */
    public Question(String text, String answer) {
        this.text = text;
        this.answer = answer;
        this.options = null;
    }

    /**
     * Constructs a multiple-choice question with a list of options.
     *
     * @param text    the question text
     * @param answer  the correct answer
     * @param options list of possible answers (choices)
     */
    public Question(String text, String answer, List<String> options) {
        this.text = text;
        this.answer = answer;
        this.options = options;
    }

    /** @return the text of the question */
    public String getText() {
        return text;
    }

    /** @return the correct answer */
    public String getAnswer() {
        return answer;
    }

    /** @return the list of options for multiple-choice questions, or {@code null} if none */
    public List<String> getOptions() {
        return options;
    }

    /**
     * Returns the question text as a string representation.
     */
    @Override
    public String toString() {
        return text;
    }
}
