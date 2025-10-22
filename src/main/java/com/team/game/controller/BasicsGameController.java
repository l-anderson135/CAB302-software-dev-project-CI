package main.java.com.team.game.controller;

import main.java.com.team.game.model.Question;
import main.java.com.team.game.model.GameMode;
import main.java.com.team.game.model.GameSession;
import main.java.com.team.game.model.User;
import main.java.com.team.game.service.GameService;
import main.java.com.team.game.Main;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for the BASICS game screen.
 * <p>
 * Displays multiple-choice and numeric questions, manages per-question timers,
 * records score/strikes, and coordinates persistence through {@link GameService}.
 * A round is started and finished against a {@link GameSession} for the
 * currently logged-in {@link User}.
 */
public class BasicsGameController implements Initializable {
    @FXML
    private Label scoreLabel;

    @FXML
    private Label strikesLabel;

    @FXML
    private Label questionLabel;

    @FXML
    private Label feedbackLabel;

    @FXML
    private Button optionAButton;

    @FXML
    private Button optionBButton;

    @FXML
    private Button optionCButton;

    @FXML
    private Button optionDButton;

    @FXML
    private TextField numericAnswerField;

    @FXML
    private Button submitNumericButton;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Button nextButton;

    // Game service and user
    private GameService gameService;
    private User currentUser;
    private GameSession currentGameSession;

    // Game state variables
    private List<Question> gameQuestions;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private int strikes = 0;
    private Question currentQuestion;
    private boolean gameActive = true;

    // Timer
    private Timeline questionTimer;
    private final int QUESTION_TIME = 30;
    private int timeRemaining = QUESTION_TIME;

    /**
     * JavaFX lifecycle hook.
     * Wires dependencies (from Main), starts a session, loads questions,
     * renders the first question, and binds button handlers.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeFromLogin();
        startGameSession();
        initializeQuestions();
        displayCurrentQuestion();
        setupButtonActions();
    }

    /**
     * Allows tests to inject a service and user without going through the JavaFX login flow.
     *
     * @param service game service to use
     * @param user    active user for the session
     */
    // Method for testing or dependency injection
    public void setDependencies(GameService service, User user) {
        this.gameService = service;
        this.currentUser = user;
    }

    /**
     * Pulls references from the JavaFX launcher (Main.BasicsApp).
     * Any failure here is logged and leaves the controller in a no-DB fallback mode.
     */
    private void initializeFromLogin() {
        try {
            this.gameService = Main.BasicsApp.getGameService();
            // This line was causing the error - we need to add getCurrentUser() to BasicsApp
            this.currentUser = Main.BasicsApp.getCurrentUser();
        } catch (Exception e) {
            System.err.println("Error initializing from login: " + e.getMessage());
        }
    }

    /**
     * Starts a new persisted BASICS round for the current user.
     * Logs (but does not rethrow) failures to avoid crashing the UI.
     */
    private void startGameSession() {
        if (gameService != null && currentUser != null) {
            try {
                currentGameSession = gameService.startRound(currentUser, GameMode.BASICS);
                System.out.println("Started new basics game session with ID: " + currentGameSession.getId());
            } catch (Exception e) {
                System.err.println("Failed to start game session: " + e.getMessage());
            }
        }
    }

    /**
     * Loads questions from {@link GameService}. If the service is unavailable,
     * seeds a small in-memory fallback set so the screen remains usable.
     */
    private void initializeQuestions() {
        if (gameService != null) {
            gameQuestions = new ArrayList<>(gameService.getBasicsQuestions());
        } else {
            gameQuestions = new ArrayList<>();
            gameQuestions.add(new Question(
                    "u = 20 m/s, t = 12s, a = 10 m/s². Which formula gives v?",
                    "v = u + at",
                    Arrays.asList("v = u + at", "s = ut + 1/2 at²", "v² = u² + 2as", "s = 1/2 (u+v)t")
            ));
            gameQuestions.add(new Question(
                    "Which formula represents displacement with initial velocity?",
                    "s = ut + 1/2 at²",
                    Arrays.asList("s = ut + 1/2 at²", "v = u + at", "F = ma", "E = mc²")
            ));
            gameQuestions.add(new Question(
                    "Which of these equations is derived from Newton's second law?",
                    "F = ma",
                    Arrays.asList("F = ma", "v = u + at", "s = ut + 1/2 at²", "p = mv")
            ));
            gameQuestions.add(new Question(
                    "Which formula relates velocity squared to displacement?",
                    "v² = u² + 2as",
                    Arrays.asList("v² = u² + 2as", "s = ut + 1/2 at²", "p = mv", "v = u + at")
            ));
            gameQuestions.add(new Question(
                    "u = 15 m/s, v = 35 m/s, t = 4s. What is a?",
                    "5"
            ));
            gameQuestions.add(new Question(
                    "A car accelerates from rest at 2 m/s² for 6s. Find v.",
                    "12"
            ));
        }

        Collections.shuffle(gameQuestions);
        System.out.println("Loaded " + gameQuestions.size() + " questions for basics game");
    }

    /**
     * Binds UI controls to their corresponding event handlers.
     */
    private void setupButtonActions() {
        optionAButton.setOnAction(this::handleMultipleChoiceAnswer);
        optionBButton.setOnAction(this::handleMultipleChoiceAnswer);
        optionCButton.setOnAction(this::handleMultipleChoiceAnswer);
        optionDButton.setOnAction(this::handleMultipleChoiceAnswer);
        submitNumericButton.setOnAction(this::handleNumericAnswer);
        nextButton.setOnAction(this::handleNextQuestion);
    }

    /**
     * Renders the current question and toggles UI for MCQ vs numeric mode.
     * Starts (or restarts) the per-question timer. Ends the game when out of questions.
     */
    private void displayCurrentQuestion() {
        if (currentQuestionIndex < gameQuestions.size()) {
            currentQuestion = gameQuestions.get(currentQuestionIndex);
            questionLabel.setText("Question " + (currentQuestionIndex + 1) + ": " + currentQuestion.getText());

            if (currentQuestion.getOptions() != null && currentQuestion.getOptions().size() >= 4) {
                optionAButton.setVisible(true);
                optionBButton.setVisible(true);
                optionCButton.setVisible(true);
                optionDButton.setVisible(true);
                numericAnswerField.setVisible(false);
                submitNumericButton.setVisible(false);

                List<String> options = currentQuestion.getOptions();
                optionAButton.setText("A: " + options.get(0));
                optionBButton.setText("B: " + options.get(1));
                optionCButton.setText("C: " + options.get(2));
                optionDButton.setText("D: " + options.get(3));
            } else {
                optionAButton.setVisible(false);
                optionBButton.setVisible(false);
                optionCButton.setVisible(false);
                optionDButton.setVisible(false);
                numericAnswerField.setVisible(true);
                submitNumericButton.setVisible(true);
                numericAnswerField.clear();
            }

            resetOptionButtons();
            nextButton.setDisable(true);
            startQuestionTimer();
        } else {
            endGame("Game Completed! Final Score: " + score + "/" + gameQuestions.size());
        }
    }

    /**
     * Starts a 30-second countdown for the current question and updates the progress bar.
     * When the timer elapses, the question is marked as missed.
     */
    private void startQuestionTimer() {
        timeRemaining = QUESTION_TIME;
        updateProgressBar();

        if (questionTimer != null) {
            questionTimer.stop();
        }

        questionTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            timeRemaining--;
            updateProgressBar();

            if (timeRemaining <= 0) {
                handleTimeUp();
            }
        }));

        questionTimer.setCycleCount(Timeline.INDEFINITE);
        questionTimer.play();
    }

    /**
     * Updates the progress bar and tint based on remaining time.
     */
    private void updateProgressBar() {
        double progress = (double) timeRemaining / QUESTION_TIME;
        progressBar.setProgress(progress);

        if (timeRemaining <= 10) {
            progressBar.setStyle("-fx-accent: red;");
        } else if (timeRemaining <= 20) {
            progressBar.setStyle("-fx-accent: orange;");
        } else {
            progressBar.setStyle("-fx-accent: green;");
        }
    }

    /**
     * Handles timeout for a question: increments strikes, saves a wrong attempt,
     * shows the correct answer, and enables moving to the next question.
     */
    private void handleTimeUp() {
        questionTimer.stop();
        feedbackLabel.setText("Time's up! The correct answer was: " + currentQuestion.getAnswer());
        feedbackLabel.setStyle("-fx-text-fill: red; -fx-font-size: 16px; -fx-font-weight: bold;");

        strikes++;
        updateStrikesDisplay();

        if (gameService != null && currentGameSession != null) {
            try {
                gameService.submitWrong(currentGameSession);
            } catch (Exception e) {
                System.err.println("Failed to save wrong answer: " + e.getMessage());
            }
        }

        highlightCorrectAnswer();
        nextButton.setDisable(false);

        if (strikes >= 3) {
            endGame("Game Over - 3 Strikes!");
        }
    }

    /**
     * Handles a click on a multiple-choice option and records the result.
     */
    @FXML
    private void handleMultipleChoiceAnswer(ActionEvent event) {
        if (!gameActive) return;

        questionTimer.stop();
        Button selectedButton = (Button) event.getSource();
        String selectedAnswer = extractAnswerFromButton(selectedButton.getText());

        // Fixed: For multiple choice questions, we know it's always multiple choice
        boolean isCorrect = isAnswerCorrect(selectedAnswer, currentQuestion.getAnswer(), true);

        processAnswer(isCorrect);

        if (isCorrect) {
            selectedButton.setStyle("-fx-background-color: #90EE90; -fx-border-color: #006400;");
        } else {
            selectedButton.setStyle("-fx-background-color: #FFCCCB; -fx-border-color: #8B0000;");
            highlightCorrectAnswer();
        }

        nextButton.setDisable(false);
    }

    /**
     * Handles submission of a numeric answer and records the result.
     */
    @FXML
    private void handleNumericAnswer(ActionEvent event) {
        if (!gameActive) return;

        questionTimer.stop();
        String userAnswer = numericAnswerField.getText().trim();

        if (userAnswer.isEmpty()) {
            feedbackLabel.setText("Please enter an answer!");
            feedbackLabel.setStyle("-fx-text-fill: orange; -fx-font-size: 16px; -fx-font-weight: bold;");
            return;
        }

        // Fixed: For numeric questions, we know it's always numeric (not multiple choice)
        boolean isCorrect = isAnswerCorrect(userAnswer, currentQuestion.getAnswer(), false);
        processAnswer(isCorrect);

        if (!isCorrect) {
            feedbackLabel.setText("Incorrect! The correct answer was: " + currentQuestion.getAnswer());
            feedbackLabel.setStyle("-fx-text-fill: red; -fx-font-size: 16px; -fx-font-weight: bold;");
        }

        nextButton.setDisable(false);
    }

    /**
     * Compares a user answer with the expected answer.
     * For MCQ, comparison is case/whitespace-insensitive.
     * For numeric answers, allows a small tolerance when parsing as doubles.
     *
     * @param userInput      text entered or option chosen
     * @param correctAnswer   expected answer
     * @param isMultipleChoice true if MCQ, false if numeric
     * @return true if considered correct
     */
    // Method to check if an answer is correct
    public boolean isAnswerCorrect(String userInput, String correctAnswer, boolean isMultipleChoice) {
        String uNorm = normalizeAnswer(userInput, isMultipleChoice);
        String cNorm = normalizeAnswer(correctAnswer, isMultipleChoice);

        if (isMultipleChoice) {
            return uNorm.equals(cNorm);
        } else {
            // decimal or fraction with tolerance
            Double u = parseNumber(uNorm);
            Double c = parseNumber(cNorm);
            if (u != null && c != null) {
                // +/- 0.01 tolerance
                return Math.abs(u - c) <= 1e-2;
            }
            return uNorm.equals(cNorm);
        }
    }

    /**
     * Normalizes answers for flexible matching.
     * MCQ: lowercase and strip whitespace. Numeric/free text: trim only.
     */
    // Method to normalize answers for flexible matching
    private String normalizeAnswer(String answer, boolean isMultipleChoice) {
        if (answer == null) {
            return "";
        }
        answer = answer.trim();

        if (isMultipleChoice) {
            return answer.toLowerCase().replaceAll("\\s+", "");
        } else {
            return answer.replaceAll("\\answer*/\\answer*", "/").trim();
        }
    }

    private Double parseNumber(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }

        if (s.matches("-?\\d+(?:\\.\\d+)?/\\s*-?\\d+(?:\\.\\d+)?")) {
            String[] ab = s.split("/");
            try {
                double a = Double.parseDouble(ab[0].trim());
                double b = Double.parseDouble(ab[1].trim());
                if (b == 0.0) {
                    return null;
                }
                return a / b;
            } catch (NumberFormatException e) {
                return null;
            }
        }

        // decimal
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Updates score/strikes, persists result, and styles feedback.
     * Ends the game on three strikes.
     *
     * @param isCorrect whether the submitted answer was correct
     */
    private void processAnswer(boolean isCorrect) {
        if (isCorrect) {
            score++;
            updateScoreDisplay();
            feedbackLabel.setText("Correct!");
            feedbackLabel.setStyle("-fx-text-fill: green; -fx-font-size: 16px; -fx-font-weight: bold;");

            if (gameService != null && currentGameSession != null) {
                try {
                    gameService.submitCorrect(currentGameSession);
                } catch (Exception e) {
                    System.err.println("Failed to save correct answer: " + e.getMessage());
                }
            }
        } else {
            strikes++;
            updateStrikesDisplay();
            feedbackLabel.setText("Incorrect! The correct answer was: " + currentQuestion.getAnswer());
            feedbackLabel.setStyle("-fx-text-fill: red; -fx-font-size: 16px; -fx-font-weight: bold;");

            if (gameService != null && currentGameSession != null) {
                try {
                    gameService.submitWrong(currentGameSession);
                } catch (Exception e) {
                    System.err.println("Failed to save wrong answer: " + e.getMessage());
                }
            }

            if (strikes >= 3) {
                endGame("Game Over - 3 Strikes!");
            }
        }
    }

    /**
     * Strips the "X: " prefix from a button label to get the answer text.
     */
    private String extractAnswerFromButton(String buttonText) {
        return buttonText.substring(3);
    }

    /**
     * Highlights the correct option on screen for an MCQ.
     */
    private void highlightCorrectAnswer() {
        if (currentQuestion.getOptions() != null) {
            List<String> options = currentQuestion.getOptions();
            String correctAnswer = currentQuestion.getAnswer();

            if (options.get(0).equals(correctAnswer)) {
                optionAButton.setStyle("-fx-background-color: #90EE90; -fx-border-color: #006400;");
            } else if (options.get(1).equals(correctAnswer)) {
                optionBButton.setStyle("-fx-background-color: #90EE90; -fx-border-color: #006400;");
            } else if (options.get(2).equals(correctAnswer)) {
                optionCButton.setStyle("-fx-background-color: #90EE90; -fx-border-color: #006400;");
            } else if (options.get(3).equals(correctAnswer)) {
                optionDButton.setStyle("-fx-background-color: #90EE90; -fx-border-color: #006400;");
            }
        }
    }

    /**
     * Resets option button styles to default before showing the next question.
     */
    private void resetOptionButtons() {
        String defaultStyle = "";
        optionAButton.setStyle(defaultStyle);
        optionBButton.setStyle(defaultStyle);
        optionCButton.setStyle(defaultStyle);
        optionDButton.setStyle(defaultStyle);
    }

    /**
     * Advances to the next question and refreshes the UI.
     */
    @FXML
    private void handleNextQuestion(ActionEvent event) {
        currentQuestionIndex++;
        feedbackLabel.setText("");
        displayCurrentQuestion();
    }

    /**
     * Updates the on-screen score label.
     */
    private void updateScoreDisplay() {
        scoreLabel.setText("Score: " + score);
    }

    /**
     * Updates the on-screen strikes label.
     */
    private void updateStrikesDisplay() {
        strikesLabel.setText("Strikes: " + strikes + "/3");
    }

    /**
     * Finalizes the round: disables inputs, persists completion, stops timers,
     * and switches the Next button into a New Game button.
     *
     * @param message final message to display to the user
     */
    private void endGame(String message) {
        gameActive = false;

        if (gameService != null && currentGameSession != null) {
            try {
                gameService.finishRound(currentGameSession);
            } catch (Exception e) {
                System.err.println("Failed to finish game session: " + e.getMessage());
            }
        }

        if (questionTimer != null) {
            questionTimer.stop();
        }

        optionAButton.setDisable(true);
        optionBButton.setDisable(true);
        optionCButton.setDisable(true);
        optionDButton.setDisable(true);
        submitNumericButton.setDisable(true);
        numericAnswerField.setDisable(true);

        nextButton.setText("New Game");
        nextButton.setDisable(false);
        nextButton.setOnAction(this::handleNewGame);

        feedbackLabel.setText(message);
        feedbackLabel.setStyle("-fx-text-fill: blue; -fx-font-size: 18px; -fx-font-weight: bold;");

        questionLabel.setText("Game Over! Final Score: " + score + "/" + gameQuestions.size());

        System.out.println("Basics game ended: " + message);
    }

    /**
     * Resets UI and in-memory state, shuffles questions, starts a fresh persisted round,
     * and returns the Next button to its normal behavior.
     */
    @FXML
    private void handleNewGame(ActionEvent event) {
        score = 0;
        strikes = 0;
        currentQuestionIndex = 0;
        gameActive = true;

        updateScoreDisplay();
        updateStrikesDisplay();
        feedbackLabel.setText("Ready...");
        feedbackLabel.setStyle("");

        optionAButton.setDisable(false);
        optionBButton.setDisable(false);
        optionCButton.setDisable(false);
        optionDButton.setDisable(false);
        submitNumericButton.setDisable(false);
        numericAnswerField.setDisable(false);

        nextButton.setText("Next Question");
        nextButton.setOnAction(this::handleNextQuestion);

        startGameSession();
        Collections.shuffle(gameQuestions);
        displayCurrentQuestion();
    }
}

//changes
