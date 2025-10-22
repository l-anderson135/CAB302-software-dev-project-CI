package main.java.com.team.game;

import main.java.com.team.game.data.GameStore;
import main.java.com.team.game.model.GameMode;
import main.java.com.team.game.model.GameSession;
import main.java.com.team.game.model.Question;
import main.java.com.team.game.model.User;
import main.java.com.team.game.model.*;
import main.java.com.team.game.service.GameService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import main.java.com.team.game.ui.Windows;


public class 1Main {
    private static final ZoneId LOCAL_TZ = ZoneId.of("Australia/Brisbane"); // or ZoneId.systemDefault()
    private static final DateTimeFormatter DT_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");



    public static void main(String[] args) {
        GameStore store = new GameStore();
        GameService svc = new GameService(store);

        if (args.length > 0 && "--console".equals(args[0])) {
            runConsole(svc);  // old console stuff in here
        } else {
            Windows.openLogin(svc, user -> {
                Thread t = new Thread(() -> runConsoleMenu(svc, user), "ConsoleMenu");
                t.setDaemon(false);
                t.start();
            });
        }
    }


    private static GameMode chooseMode(Scanner in) {
        System.out.println("Pick game mode:");
        System.out.println("1) BASICS");
        System.out.println("2) TRIG");
        System.out.println("3) TARGET");
        System.out.println("0) Cancel");
        System.out.print("Choose: ");
        String m = in.nextLine().trim();
        return switch (m) {
            case "1" -> GameMode.BASICS;
            case "2" -> GameMode.TRIG;
            case "3" -> GameMode.TARGET;
            case "0" -> null;
            default -> { System.out.println("Invalid."); yield null; }
        };
    }

    private static void startRoundFlow(Scanner in, GameService svc, User user) {
        GameMode mode = chooseMode(in);
        if (mode == null) return;

        // If TRIG or BASICS mode is selected, launch the GUI
        if (mode == GameMode.TRIG) {
            System.out.println("Launching TRIG GUI...");
            launchTrigoGUI(svc, user);
            return;
        }
        else if (mode == GameMode.BASICS) {
            System.out.println("Launching Basics GUI...");
            launchBasicsGUI(svc, user);
            return;
        }

        else if (mode == GameMode.TARGET) {
            System.out.println("Launching Target GUI...");
            launchTargetGUI(svc, user);
            return;
        }

        // For other modes, use the existing console-based gameplay
        GameSession s = svc.startRound(user, mode);
        int score = 0, strikes = 0;
        System.out.printf("Started %s round (session id=%d).%n", mode, s.getId());

        // Get questions for the selected mode
        List<Question> questions = switch (mode) {
            case BASICS -> svc.getBasicsQuestions();
            case TARGET -> svc.getTargetQuestions();
            case TRIG -> svc.getTrigoQuestions(); // This won't be reached due to GUI check above
        };

        // Shuffle questions for variety
        java.util.Collections.shuffle(questions);
        int questionIndex = 0;

        while (questionIndex < questions.size() && strikes < 3) {
            Question currentQuestion = questions.get(questionIndex);
            System.out.printf("%n[Question %d] [Score=%d, Strikes=%d]%n", questionIndex + 1, score, strikes);
            System.out.println(currentQuestion.getText());

            // Display options if it's a multiple choice question
            if (currentQuestion.getOptions() != null) {
                List<String> options = currentQuestion.getOptions();
                for (int i = 0; i < options.size(); i++) {
                    System.out.printf("%d) %s%n", i + 1, options.get(i));
                }
                System.out.print("Enter your choice (1-" + options.size() + ") or 'skip' to skip: ");
            } else {
                System.out.print("Enter your answer or 'skip' to skip: ");
            }

            String userInput = in.nextLine().trim();

            if (userInput.equalsIgnoreCase("skip")) {
                System.out.println("Question skipped.");
                questionIndex++;
                continue;
            }

            boolean isCorrect = false;

            // Check answer based on question type
            if (currentQuestion.getOptions() != null) {
                // Multiple choice question
                try {
                    int choice = Integer.parseInt(userInput);
                    if (choice >= 1 && choice <= currentQuestion.getOptions().size()) {
                        String selectedAnswer = currentQuestion.getOptions().get(choice - 1);
                        isCorrect = selectedAnswer.equals(currentQuestion.getAnswer());
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Please enter a number.");
                    continue; // Don't advance question, let them try again
                }
            } else {
                // Direct answer question
                isCorrect = userInput.equalsIgnoreCase(currentQuestion.getAnswer().trim());
            }

            if (isCorrect) {
                System.out.println("✓ Correct!");
                svc.submitCorrect(s);
                score++;
            } else {
                System.out.println("✗ Wrong! The correct answer was: " + currentQuestion.getAnswer());
                svc.submitWrong(s);
                strikes++;
                if (strikes >= 3) {
                    System.out.println("3 strikes reached — round finished automatically.");
                    break;
                }
            }

            questionIndex++;
        }

        // Check end condition
        if (strikes >= 3) {
            System.out.println("Game over due to 3 strikes!");
        } else if (questionIndex >= questions.size()) {
            System.out.println("Congratulations! You've completed all questions!");
        }

        svc.finishRound(s);
        int hs = svc.highScore(user, mode).orElse(0);
        System.out.println("Round finished. Your high score (" + mode + ") = " + hs);
    }



    // login/rego first
    private static void runConsole(GameService svc) {
        System.out.println("cwd = " + System.getProperty("user.dir"));
        System.out.println("db  = data/game.db");

        Scanner in = new Scanner(System.in);
        System.out.println("1) Register   2) Login");
        System.out.print("Choose: ");
        String choice = in.nextLine().trim();

        System.out.print("Username: ");
        String username = in.nextLine().trim();
        System.out.print("Password: ");
        char[] pw = in.nextLine().toCharArray();

        User user;
        if ("1".equals(choice)) {
            try {
                user = svc.register(username, pw);
                System.out.println("Registered as " + user.getUsername());
            } catch (IllegalStateException dup) {
                System.out.println("Username is taken.");
                return;
            }
        } else {
            Optional<User> maybe = svc.login(username, pw);
            if (maybe.isEmpty()) { System.out.println("Invalid credentials."); return; }
            user = maybe.get();
            System.out.println("Welcome back, " + user.getUsername());
        }

        runConsoleMenu(svc, user);
    }


    // Gabby's console ui (do we wanna replace all this with one unified GUI eventually?)
    private static void runConsoleMenu(GameService svc, User user) {
        Scanner in = new Scanner(System.in);

        while (true) {
            System.out.println("\n== MENU ==");
            System.out.println("1) Start round (pick game mode)");
            System.out.println("2) My sessions (Read)");
            System.out.println("3) Delete a session (Delete)");
            System.out.println("4) Change username (Update)");
            System.out.println("5) Change password (Update)");
            System.out.println("6) Delete my account (Delete)");
            System.out.println("7) Leaderboard (pick mode)");
            System.out.println("8) List all users (Read)");
            System.out.println("0) Exit");
            System.out.print("Pick: ");
            String op = in.nextLine().trim();

            switch (op) {
                case "1" -> startRoundFlow(in, svc, user);

                case "2" -> {
                    var sessions = svc.listSessionsByUser(user);
                    if (sessions.isEmpty()) System.out.println("(no sessions)");
                    for (var s : sessions) {
                        var startedLocal = s.getStartedAt().atZone(LOCAL_TZ).format(DT_FMT);
                        var endedLocal = (s.getEndedAt() == null) ? "-" : s.getEndedAt().atZone(LOCAL_TZ).format(DT_FMT);
                        System.out.printf(
                                "id=%d mode=%s score=%d strikes=%d done=%s started=%s ended=%s%n",
                                s.getId(), s.getMode(), s.getScore(), s.getStrikes(),
                                s.isCompleted(), startedLocal, endedLocal
                        );
                    }
                }

                case "3" -> {
                    System.out.print("Session id to delete: ");
                    int id = Integer.parseInt(in.nextLine());
                    boolean ok = svc.deleteSession(id);
                    System.out.println(ok ? "Deleted." : "Not found.");
                }

                case "4" -> {
                    System.out.print("New username: ");
                    String nn = in.nextLine().trim();
                    try {
                        svc.updateUsername(user, nn);
                        user = new User(user.getId(), nn, user.getRegisteredAt());
                        System.out.println("Updated.");
                    } catch (IllegalStateException ex) {
                        System.out.println("That name is taken.");
                    }
                }

                case "5" -> {
                    System.out.print("New password: ");
                    char[] npw = in.nextLine().toCharArray();
                    svc.updatePassword(user, npw);
                    System.out.println("Password changed.");
                }

                case "6" -> {
                    System.out.print("Type DELETE to confirm account deletion: ");
                    if ("DELETE".equals(in.nextLine().trim())) {
                        boolean ok = svc.deleteUser(user);
                        System.out.println(ok ? "Account deleted." : "Delete failed.");
                        return;
                    }
                }

                case "7" -> {
                    GameMode mode = chooseMode(in);
                    if (mode == null) break;
                    var rows = svc.leaderboard(mode, 10);
                    if (rows.isEmpty()) System.out.println("(no scores yet)");
                    for (var r : rows) System.out.printf("- %s: %d%n", r.getUsername(), r.getHighScore());
                }

                case "8" -> {
                    var users = svc.listUsers();
                    for (var u : users) {
                        System.out.printf("%d  %s  (%s)%n", u.getId(), u.getUsername(), u.getRegisteredAt());
                    }
                }

                case "0" -> { return; }

                default -> System.out.println("Unknown option.");
            }
        }
    }








    // JavaFX Application class for TRIG GUI
    public static class TrigoApp extends Application {
        private static GameService gameService;
        private static User currentUser;

        public static void setUserData(GameService service, User user) {
            gameService = service;
            currentUser = user;
        }

        public static GameService getGameService() {
            return gameService;
        }

        public static User getCurrentUser() {
            return currentUser;
        }

        @Override
        public void start(Stage primaryStage) throws Exception {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/trigofun/trigofun-main.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, 1200, 800);
            primaryStage.setTitle("Trigo Game");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();
        }
    }

    // JavaFX Application class for Basics GUI
    public static class BasicsApp extends Application {
        private static GameService gameService;
        private static User currentUser;

        public static void setUserData(GameService service, User user) {
            gameService = service;
            currentUser = user;
        }

        public static GameService getGameService() {
            return gameService;
        }

        public static User getCurrentUser() {
            return currentUser;
        }

        @Override
        public void start(Stage primaryStage) throws Exception {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/basicsgame/basics-game.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, 1000, 600);
            primaryStage.setTitle("Basics Game");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();


        }
    }

    // javafx app class for Target game GUI
    public static class TargetApp extends Application {
        private static GameService gameService;
        private static User currentUser;

        public static void setUserData(GameService svc, User user) {
            gameService = svc;
            currentUser = user;
        }
        public static GameService getGameService() { return gameService; }
        public static User getCurrentUser() { return currentUser; }

        @Override
        public void start(Stage stage) throws Exception {
            FXMLLoader loader = new FXMLLoader(TargetApp.class.getResource("/target/target-main.fxml"));
            Parent root = loader.load();
            stage.setTitle("Target Game");
            stage.setScene(new Scene(root));
            stage.show();
        }
    }





    // TRIG
    private static void launchTrigoGUI(GameService svc, User user) {
        main.java.com.team.game.ui.Windows.openTrig(svc, user);
    }

    // BASICS
    private static void launchBasicsGUI(GameService svc, User user) {
    }

    // TARGET
    private static void launchTargetGUI(GameService svc, User user) {
        main.java.com.team.game.ui.Windows.openTarget(svc, user);
    }



}
//