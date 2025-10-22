package main.java.com.team.game.controller;

import main.java.com.team.game.model.Question;
import main.java.com.team.game.model.QuestionBank;
import main.java.com.team.game.model.GameMode;
import main.java.com.team.game.model.GameSession;
import main.java.com.team.game.model.User;
import main.java.com.team.game.service.GameService;
import main.java.com.team.game.Main;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * Controller for the Trigonometry mini-game.
 * <p>
 * Handles question display, answer checking, scoring, timing, and database
 * integration through {@link GameService}. This game challenges players
 * with trigonometric problems drawn from {@link QuestionBank}, tracking
 * their progress, accuracy, and performance within a time limit.
 */
public class TrigoFunController implements Initializable {

    @FXML private Label scoreLabel;
    @FXML private Label timeLabel;
    @FXML private Label questionLabel;
    @FXML private ImageView questionImage;
    @FXML private ImageView clockImage;
    @FXML private TextField answerField;
    @FXML private Label feedbackLabel;
    @FXML private Button newGameButton;

    // Question bank and list of questions
    private List<Question> gameQuestions;
    private QuestionBank questionBank;

    // Database integration
    private GameService gameService;
    private User currentUser;
    private GameSession currentGameSession;

    // Game state variables
    private int currentQuestionIndex = 0;
    private int totalScore = 0;
    private int wrongStrikes = 0;                // Tracks consecutive wrong answers
    private int highestConsecutiveCorrect = 0;   // Tracks best streak
    private int currentConsecutiveCorrect = 0;   // Tracks ongoing correct streak
    private Question currentQuestion;

    // Timer variables
    private Timeline gameTimer;
    private int timeRemaining = 300;             // 5-minute timer
    private boolean gameActive = true;

    /**
     * Called automatically when the FXML is loaded.
     * <p>
     * Initializes the game by linking to the logged-in user,
     * loading questions, starting a session, and beginning the timer.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeFromLogin();
        loadClockImage();
        initializeQuestionsFromBank();
        startGameSession();
        displayCurrentQuestion();
        startGameTimer();
    }

    /**
     * Injects the {@link GameService} and {@link User} for testing or manual setup.
     *
     * @param gameService active service façade
     * @param currentUser authenticated user (may be {@code null} in edge flows)
     */
    public void setDependencies(GameService gameService, User currentUser) {
        this.gameService = gameService;
        this.currentUser = currentUser;
    }

    /**
     * Retrieves the active {@link GameService} and {@link User}.
     * <p>
     * Prefers dependencies already injected via {@link #setDependencies(GameService, User)}
     * (useful for tests). If missing, falls back to {@link Main.TrigoApp}.
     * Always touches {@code currentUser.getUsername()} to confirm wiring.
     */
    public void initializeFromLogin() {
        // Only fall back to Main.TrigoApp if a dependency wasn't injected
        if (this.gameService == null || this.currentUser == null) {
            try {
                if (this.gameService == null) {
                    this.gameService = Main.TrigoApp.getGameService();
                }
                if (this.currentUser == null) {
                    this.currentUser = Main.TrigoApp.getCurrentUser();
                }
            } catch (Exception e) {
                System.err.println("Error initializing from login: " + e.getMessage());
            }
        }

        if (this.gameService == null) {
            System.err.println("GameService not available! This should not happen.");
            return;
        }
        if (this.currentUser == null) {
            System.err.println("No user logged in! This should not happen.");
            return;
        }

        // Touch the username so tests can verify the interaction
        String username = this.currentUser.getUsername();
        System.out.println("Using logged-in user: " + username);
    }

    /**
     * Starts a new game session in the database for the current user.
     */
    private void startGameSession() {
        if (gameService != null && currentUser != null) {
            try {
                currentGameSession = gameService.startRound(currentUser, GameMode.TRIG);
                System.out.println("Started new game session with ID: " + currentGameSession.getId());
            } catch (Exception e) {
                System.err.println("Failed to start game session: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Loads all trigonometry-related questions from {@link QuestionBank}.
     */
    private void initializeQuestionsFromBank() {
        questionBank = new QuestionBank();
        gameQuestions = new ArrayList<>(questionBank.getTrigo());
        System.out.println("Loaded " + gameQuestions.size() + " questions from QuestionBank");
    }

    /**
     * Loads and displays the clock image in the UI.
     */
    private void loadClockImage() {
        try {
            String imagePath = "/images/clock1.png";
            Image image = new Image(getClass().getResourceAsStream(imagePath));
            clockImage.setImage(image);
            System.out.println("Clock image loaded successfully");
        } catch (Exception e) {
            System.err.println("Failed to load clock image");
            e.printStackTrace();
        }
    }

    /**
     * Loads an image for the current question from {@code /images/}.
     *
     * @param imageName the filename of the image to load
     */
    public void loadQuestionImage(String imageName) {
        try {
            String imagePath = "/images/" + imageName;
            Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream(imagePath)));
            questionImage.setImage(image);
            System.out.println("Image loaded successfully: " + imageName);
        } catch (Exception e) {
            System.err.println("Failed to load image: " + imageName);
            e.printStackTrace();
            questionImage.setImage(null);
        }
    }

    /**
     * Handles submission of a user’s answer.
     * <p>
     * Validates input, checks correctness, updates the score,
     * saves results to the database, and transitions to the next question.
     */
    @FXML
    public void handleSubmitAnswer(ActionEvent actionEvent) {
        if (!gameActive) return;

        String userAnswer = answerField.getText().trim();
        if (userAnswer.isEmpty()) {
            feedbackLabel.setText("Please enter an answer!");
            feedbackLabel.setStyle("-fx-text-fill: orange; -fx-font-size: 18px; -fx-font-weight: bold;");
            return;
        }

        // Evaluate answer correctness
        if (isAnswerCorrect(userAnswer, currentQuestion.getAnswer())) {
            totalScore++;
            currentConsecutiveCorrect++;
            wrongStrikes = 0;
            updateScoreDisplay();
            feedbackLabel.setText("Correct!");
            feedbackLabel.setStyle("-fx-text-fill: green; -fx-font-size: 18px; -fx-font-weight: bold;");

            // Save correct result
            if (gameService != null && currentGameSession != null) {
                try {
                    gameService.submitCorrect(currentGameSession);
                } catch (Exception e) {
                    System.err.println("Failed to save correct answer: " + e.getMessage());
                }
            }
        } else {
            if (currentConsecutiveCorrect > highestConsecutiveCorrect) {
                highestConsecutiveCorrect = currentConsecutiveCorrect;
            }
            currentConsecutiveCorrect = 0;
            wrongStrikes++;
            feedbackLabel.setText("Wrong! Correct answer: " + currentQuestion.getAnswer() + " (Strikes: " + wrongStrikes + "/3)");
            feedbackLabel.setStyle("-fx-text-fill: red; -fx-font-size: 18px; -fx-font-weight: bold;");

            if (gameService != null && currentGameSession != null) {
                try {
                    gameService.submitWrong(currentGameSession);
                } catch (Exception e) {
                    System.err.println("Failed to save wrong answer: " + e.getMessage());
                }
            }

            // End game if 3 strikes reached
            if (wrongStrikes >= 3) {
                endGame("Game Over - 3 Strikes! Final Score: " + totalScore + "/" + currentQuestionIndex);
                return;
            }
        }

        // Clear field and move to next question
        answerField.clear();
        currentQuestionIndex++;
        if (currentQuestionIndex < gameQuestions.size()) {
            Timeline timeline = new Timeline(
                    new KeyFrame(Duration.seconds(2), e -> {
                        if (gameActive) {
                            displayCurrentQuestion();
                            feedbackLabel.setText("");
                        }
                    })
            );
            timeline.play();
        } else {
            endGame("Game Finished! Final Score: " + totalScore + "/" + gameQuestions.size());
        }
    }

    /**
     * Displays the current question and its options, if available.
     */
    private void displayCurrentQuestion() {
        if (currentQuestionIndex < gameQuestions.size()) {
            currentQuestion = gameQuestions.get(currentQuestionIndex);
            questionLabel.setText("Question " + (currentQuestionIndex + 1) + ": " + currentQuestion.getText());

            if (currentQuestion.getOptions() != null && !currentQuestion.getOptions().isEmpty()) {
                StringBuilder optionsText = new StringBuilder("Choices:\n");
                for (int i = 0; i < currentQuestion.getOptions().size(); i++) {
                    optionsText.append("\t").append((char) ('A' + i))
                            .append(". ").append(currentQuestion.getOptions().get(i)).append("\n");
                }
                questionLabel.setText(questionLabel.getText() + "\n" + optionsText);
            }

            loadQuestionImage("triangle.png"); // Default diagram for trig questions
        }
    }

    /**
     * Compares user and correct answers after normalization.
     *
     * @param userAnswer    raw user input
     * @param correctAnswer authoritative answer text
     * @return {@code true} if answers are equivalent after normalization
     */
    public boolean isAnswerCorrect(String userAnswer, String correctAnswer) {
        String normalizedUserAnswer = normalizeAnswer(userAnswer);
        String normalizedCorrectAnswer = normalizeAnswer(correctAnswer);
        return normalizedUserAnswer.equals(normalizedCorrectAnswer);
    }

    /**
     * Normalizes an answer string to handle whitespace, decimals,
     * radicals, and equivalent notations for flexible comparison.
     *
     * @param answer raw answer text
     * @return normalized comparison form
     */
    public String normalizeAnswer(String answer) {
        return answer.toLowerCase()
                .replaceAll("\\s+", "")
                .replace("sqrt(2)/2", "√2/2")
                .replace("sqrt(3)/3", "√3/3")
                .replace("sqrt(3)", "√3")
                .replace("0.5", "1/2")
                .replace("0.707", "√2/2")
                .replace("1.732", "√3")
                .replace("0.577", "√3/3");
    }

    /**
     * Updates the displayed score.
     */
    private void updateScoreDisplay() {
        scoreLabel.setText("Total Score: " + totalScore);
    }

    /**
     * Starts the countdown timer and ends the game when time runs out.
     */
    private void startGameTimer() {
        updateTimeDisplay();
        gameTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            timeRemaining--;
            updateTimeDisplay();

            if (timeRemaining <= 0) {
                endGame("Time's Up! Final Score: " + totalScore + "/" + gameQuestions.size());
            }
        }));

        gameTimer.setCycleCount(Timeline.INDEFINITE);
        gameTimer.play();
    }

    /**
     * Updates the timer label each second and changes its color
     * as time runs low.
     */
    private void updateTimeDisplay() {
        int minutes = timeRemaining / 60;
        int seconds = timeRemaining % 60;
        String timeText = String.format("%d:%02d", minutes, seconds);
        timeLabel.setText(timeText);

        if (timeRemaining <= 30) {
            timeLabel.setStyle("-fx-text-fill: red; -fx-font-family: 'Verdana'; -fx-font-size: 20px; -fx-font-weight: bold;");
        } else if (timeRemaining <= 60) {
            timeLabel.setStyle("-fx-text-fill: orange; -fx-font-family: 'Verdana'; -fx-font-size: 20px; -fx-font-weight: bold;");
        } else {
            timeLabel.setStyle("-fx-text-fill: #B56L37; -fx-font-family: 'Verdana'; -fx-font-size: 20px; -fx-font-weight: bold;");
        }
    }

    /**
     * Ends the game, stops the timer, finalizes the session in the database,
     * and displays the end-of-game message and final score.
     */
    private void endGame(String message) {
        gameActive = false;

        if (gameService != null && currentGameSession != null) {
            try {
                gameService.finishRound(currentGameSession);
                System.out.println("Finished session " + currentGameSession.getId() + " with score " + totalScore);
            } catch (Exception e) {
                System.err.println("Failed to finish game session: " + e.getMessage());
            }
        }

        if (gameTimer != null) gameTimer.stop();

        answerField.setDisable(true);
        newGameButton.setVisible(true);

        feedbackLabel.setText(message);
        feedbackLabel.setStyle("-fx-text-fill: blue; -fx-font-size: 20px; -fx-font-weight: bold;");
        questionLabel.setText("Game Over! Click 'New Game' to play again.");
        questionImage.setImage(null);
    }

    /**
     * Handles the “New Game” button action.
     * <p>
     * Resets all state variables, restarts the session, and begins a new game cycle.
     */
    @FXML
    public void handleNewGame(ActionEvent actionEvent) {
        currentQuestionIndex = 0;
        totalScore = 0;
        wrongStrikes = 0;
        highestConsecutiveCorrect = 0;
        currentConsecutiveCorrect = 0;
        timeRemaining = 300;
        gameActive = true;

        answerField.setDisable(false);
        answerField.clear();
        newGameButton.setVisible(false);
        updateScoreDisplay();
        feedbackLabel.setText("");
        feedbackLabel.setStyle("");

        startGameSession();
        initializeQuestionsFromBank();
        displayCurrentQuestion();
        startGameTimer();

        System.out.println("New game started!");
    }
}
