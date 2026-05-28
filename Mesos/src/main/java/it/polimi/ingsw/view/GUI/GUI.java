package it.polimi.ingsw.view.GUI;

import it.polimi.ingsw.leaderboard.MatchResult;
import it.polimi.ingsw.model.player.TotemColor;
import it.polimi.ingsw.network.VirtualServer;
import it.polimi.ingsw.view.ClientModel;
import it.polimi.ingsw.view.View;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Main GUI class for the MESOS game client.
 * Extends {@link Application} to integrate with the JavaFX lifecycle and implements
 * {@link View} to receive updates from the network layer.
 * Acts as a singleton so that controllers can access it via {@link #getInstance()}.
 */
public class GUI extends Application implements View {

    private static GUI instance;

    private VirtualServer virtualServer;
    private ClientModel clientModel = new ClientModel();
    private Stage primaryStage;
    private String nickname;
    private List<MatchResult> globalLeaderboard = List.of();
    private int globalLeaderboardPosition = -1;

    private GUILobbyController lobbyController;
    private GUIGameController gameController;
    private GUIEndGameController endGameController;

    private StackPane recoveryOverlay;
    private Label recoveryTitleLabel;
    private Label recoveryMessageLabel;
    private Label recoveryDetailsLabel;
    private StackPane rulesOverlay;

    // ── JavaFX entry point ────────────────────────────────────────────────────

    /**
     * JavaFX entry point. Stores the singleton instance, configures the primary stage
     * and shows the welcome screen.
     *
     * @param stage the primary stage provided by the JavaFX runtime
     */
    @Override
    public void start(Stage stage) {
        instance = this;
        this.primaryStage = stage;
        primaryStage.setTitle("MESOS");
        primaryStage.setOnCloseRequest(e -> Platform.exit());
        showWelcomeScreen();
    }

    /**
     * Application main method. Required by some IDEs and launchers.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    // ── Singleton ─────────────────────────────────────────────────────────────

    /**
     * Returns the singleton instance of this GUI.
     *
     * @return the current {@link GUI} instance
     */
    public static GUI getInstance() {
        return instance;
    }

    // ── Screen navigation ─────────────────────────────────────────────────────

    /**
     * Loads and displays the welcome screen.
     * Sets up background scaling listeners so the image fills the window at any size.
     */
    public void showWelcomeScreen() {
        try {
            clearRecoveryOverlay();
            this.lobbyController = null;
            this.gameController = null;
            this.endGameController = null;

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/WelcomeScreen.fxml"));
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/fxml/style.css").toExternalForm());

            GUIWelcomeController controller = loader.getController();
            controller.setGUI(this);

            primaryStage.setScene(scene);
            primaryStage.setWidth(1280);
            primaryStage.setHeight(800);
            primaryStage.centerOnScreen();

            primaryStage.show();

            scene.widthProperty().addListener((obs, oldVal, newVal) -> {
                ImageView bg = (ImageView) scene.lookup("#backgroundImage");
                if (bg != null) bg.setFitWidth(newVal.doubleValue());
                updateScale(scene);
            });
            scene.heightProperty().addListener((obs, oldVal, newVal) -> {
                ImageView bg = (ImageView) scene.lookup("#backgroundImage");
                if (bg != null) bg.setFitHeight(newVal.doubleValue());
                updateScale(scene);
            });

            updateScale(scene);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads and displays the lobby screen on the JavaFX Application Thread.
     * Sets up background scaling listeners.
     */
    public void showLobbyScreen() {
        try {
            clearRecoveryOverlay();
            this.gameController = null;
            this.endGameController = null;
            this.clientModel = new ClientModel();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LobbyScreen.fxml"));
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/fxml/style.css").toExternalForm());

            this.lobbyController = loader.getController();
            this.lobbyController.setGUI(this);

            primaryStage.setScene(scene);

            // 1. IMPORTANTE: Mostra lo stage prima dei listener
            primaryStage.show();

            // 2. Configura i listener per gestire il ridimensionamento dello sfondo e del contenuto
            scene.widthProperty().addListener((obs, oldVal, newVal) -> {
                ImageView bg = (ImageView) scene.lookup("#backgroundImage"); // ID definito nel FXML
                if (bg != null) bg.setFitWidth(newVal.doubleValue());
                updateScale(scene);
            });
            scene.heightProperty().addListener((obs, oldVal, newVal) -> {
                ImageView bg = (ImageView) scene.lookup("#backgroundImage"); // ID definito nel FXML
                if (bg != null) bg.setFitHeight(newVal.doubleValue());
                updateScale(scene);
            });

            // 3. Esegui il primo calcolo della scala immediatamente dopo lo show
            updateScale(scene);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads and displays the main game screen.
     * This method:
     * - load the FXML;
     * - attach the CSS;
     * - store the controller;
     * - pass this GUI instance to the controller;
     * - configure background/content scaling.
     */
    public void showGameScreen() {
        Platform.runLater(() -> {
            try {
                clearRecoveryOverlay();

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/GameScreen.fxml"));
                Scene scene = new Scene(loader.load());
                scene.getStylesheets().add(getClass().getResource("/fxml/style.css").toExternalForm());

                this.gameController = loader.getController();
                this.gameController.setGUI(this);

                primaryStage.setScene(scene);
                primaryStage.show();

                scene.widthProperty().addListener((obs, oldVal, newVal) -> {
                    ImageView bg = (ImageView) scene.lookup("#backgroundImage");
                    if (bg != null) bg.setFitWidth(newVal.doubleValue());
                    updateScale(scene);
                });

                scene.heightProperty().addListener((obs, oldVal, newVal) -> {
                    ImageView bg = (ImageView) scene.lookup("#backgroundImage");
                    if (bg != null) bg.setFitHeight(newVal.doubleValue());
                    updateScale(scene);
                });

                updateScale(scene);

                /*
                 * The controller is now ready, so we immediately draw the latest state.
                 * This is important because the server may have already sent the first snapshot.
                 */
                this.gameController.render(this.clientModel);

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Loads and displays the final ranking screen.
     * It is shown when the ClientModel enters the EndGame phase.
     */
    public void showEndGameScreen() {
        Platform.runLater(() -> {
            try {
                clearRecoveryOverlay();

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EndGameScreen.fxml"));
                Scene scene = new Scene(loader.load());
                scene.getStylesheets().add(getClass().getResource("/fxml/style.css").toExternalForm());

                this.endGameController = loader.getController();
                this.endGameController.setGUI(this);

                primaryStage.setScene(scene);
                primaryStage.show();

                scene.widthProperty().addListener((obs, oldVal, newVal) -> {
                    ImageView bg = (ImageView) scene.lookup("#backgroundImage");
                    if (bg != null) bg.setFitWidth(newVal.doubleValue());
                    updateScale(scene);
                });

                scene.heightProperty().addListener((obs, oldVal, newVal) -> {
                    ImageView bg = (ImageView) scene.lookup("#backgroundImage");
                    if (bg != null) bg.setFitHeight(newVal.doubleValue());
                    updateScale(scene);
                });

                updateScale(scene);

                /*
                 * Draw the final ranking as soon as the screen is loaded.
                 */
                this.endGameController.render();

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    // ── View interface ────────────────────────────────────────────────────────

    /**
     * Binds this view to the given network layer.
     *
     * @param vs the {@link VirtualServer} to use for outgoing messages
     */
    @Override
    public void setVirtualServer(VirtualServer vs) {
        this.virtualServer = vs;
    }

    /**
     * Returns the network layer currently bound to this view.
     *
     * @return the current {@link VirtualServer}
     */
    public VirtualServer getVirtualServer() {
        return virtualServer;
    }

    /**
     * Returns the local replica of the game state.
     *
     * @return the current {@link ClientModel}
     */
    @Override
    public ClientModel getClientModel() {
        return clientModel;
    }

    /**
     * Sets the client model. If the game screen has not been shown yet, shows it.
     *
     * @param model the new {@link ClientModel} received from the server
     */
    @Override
    public void setClientModel(ClientModel model) {
        this.clientModel = model;
        /*
         * A new full model usually means that the game has just started.
         * We switch from the lobby to the game screen, unless the model already says
         * that the game is over.
         */
        Platform.runLater(() -> {
            clearRecoveryOverlayIfConnected();

            if (isEndGamePhase()) {
                if (gameController != null) {
                    gameController.scheduleEndGame();
                } else {
                    showEndGameScreen();
                }
            } else if (gameController == null) {
                showGameScreen();
            } else {
                gameController.render(this.clientModel);
            }
        });
    }

    /**
     * Re-renders the current screen by reading the latest state from the client model.
     * Must be called on or delegated to the JavaFX Application Thread.
     */
    @Override
    public void render() {
        Platform.runLater(() -> {
            if (clientModel == null) {
                return;
            }

            clearRecoveryOverlayIfConnected();

            if (isEndGamePhase()) {
                if (endGameController != null) {
                    // EndGame screen already visible — just re-render it.
                    endGameController.render();
                } else if (gameController != null) {
                    // Game screen is still up: let the game controller drain its event queue
                    // and show the final-scoring overlay before switching screens.
                    gameController.scheduleEndGame();
                } else {
                    showEndGameScreen();
                }
                return;
            }

            if (gameController == null) {
                showGameScreen();
            } else {
                gameController.render(this.clientModel);
            }
        });
    }

    /**
     * Stores the nickname assigned by the server after a successful join.
     *
     * @param nickname the confirmed nickname
     * @param color    the assigned totem color
     */
    @Override
    public void showJoinSuccess(String nickname, TotemColor color) {
        Platform.runLater(() -> {
            this.nickname = nickname;
            if (lobbyController != null) {
                lobbyController.switchToWaitingMode();
            }
        });
    }

    /**
     * Updates the lobby player list. If the lobby screen is not yet visible, shows it first.
     *
     * @param players        ordered list of player nicknames
     * @param colorsByPlayer map from nickname to assigned totem color
     * @param expected       total number of players expected to start the game
     */
    @Override
    public void showLobbyUpdate(List<String> players,
                                Map<String, TotemColor> colorsByPlayer,
                                int expected) {
        Platform.runLater(() -> {
            if (lobbyController != null) {
                lobbyController.updateLobby(players, colorsByPlayer, expected);
            }
        });
    }

    /**
     * Displays a login or lobby error message to the user.
     *
     * @param description human-readable error description
     */
    @Override
    public void showLoginError(String description) {
        Platform.runLater(() -> {
            if (lobbyController != null) {
                lobbyController.showError(description);
            }
        });
    }

    /**
     * Displays an in-game error message to the user.
     *
     * @param description human-readable error description
     */
    @Override
    public void showGameError(String description) {
        Platform.runLater(() -> {
            /*
             * During the game, errors must be shown on the game screen.
             * If the game screen is not ready yet, we fallback to the lobby controller.
             */
            if (gameController != null) {
                gameController.showError(description);
            } else if (lobbyController != null) {
                lobbyController.showError(description);
            }
        });
    }

    /**
     * Appends an event resolution summary to the game event log.
     *
     * @param eventCardID the ID of the resolved event card
     * @param eventType   the type of the event
     * @param ppDelta     prestige point changes per player
     * @param foodDelta   food changes per player
     */
    @Override
    public void showEventResolved(String eventCardID, String eventType,
                                  Map<String, Integer> ppDelta,
                                  Map<String, Integer> foodDelta) {
        Platform.runLater(() -> {
            /*
             * Events are part of the game log, so we forward them to the game controller.
             * The model update is handled elsewhere by ClientModel.applyEventResolved().
             */
            if (gameController != null) {
                gameController.showEventResolved(eventCardID, eventType, ppDelta, foodDelta);
            }
        });
    }

    /**
     * Notifies the user of a temporary disconnection and keeps the GUI open while
     * the network adapter tries to reconnect in the background.
     *
     * @param reason human-readable disconnection reason
     */
    @Override
    public void notifyDisconnection(String reason) {
        Platform.runLater(() -> {
            if (!isRecoverableDisconnection(reason)) {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.ERROR
                );
                alert.setTitle("Disconnected");
                alert.setHeaderText("Connection closed");
                alert.setContentText(reason);
                alert.showAndWait();
                Platform.exit();
                return;
            }

            showRecoveryOverlay(
                    "Server connection lost",
                    reason == null || reason.isBlank()
                            ? "The server is offline. Waiting for it to come back..."
                            : reason,
                    "The game is paused locally. This window will update automatically."
            );
        });
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Checks whether the current local model represents the final game phase.
     *
     * @return true if the model is in the end-game phase
     */
    private boolean isEndGamePhase() {
        return clientModel != null && "EndGame".equals(clientModel.getCurrentPhaseName());
    }

    /**
     * Scales the content pane proportionally to fit the current window size,
     * preserving the original 1280x800 aspect ratio.
     *
     * @param scene the scene whose scalable pane should be resized
     */
    private void updateScale(Scene scene) {
        StackPane scalablePane = (StackPane) scene.lookup("#scalablePane");
        ImageView bg = (ImageView) scene.lookup("#backgroundImage");

        double windowWidth = scene.getWidth();
        double windowHeight = scene.getHeight();

        if (bg != null && windowWidth > 0 && windowHeight > 0) {
            bg.setFitWidth(windowWidth);
            bg.setFitHeight(windowHeight);
        }

        if (scalablePane != null && windowWidth > 0 && windowHeight > 0) {
            ScrollPane endGameScrollPane = (ScrollPane) scene.lookup("#endGameScrollPane");
            if (endGameScrollPane != null) {
                scalablePane.setScaleX(1);
                scalablePane.setScaleY(1);
                scalablePane.setPrefSize(windowWidth, windowHeight);
                scalablePane.setMaxSize(windowWidth, windowHeight);
                endGameScrollPane.setPrefSize(windowWidth, windowHeight);
                endGameScrollPane.setMaxSize(windowWidth, windowHeight);
                StackPane.setAlignment(scalablePane, javafx.geometry.Pos.CENTER);
                return;
            }

            double scale = Math.min(windowWidth / 1280.0, windowHeight / 800.0);

            scalablePane.setScaleX(scale);
            scalablePane.setScaleY(scale);

            scalablePane.setManaged(true);
            StackPane.setAlignment(scalablePane, javafx.geometry.Pos.CENTER);
        }
    }
    // ── Getters / Setters ─────────────────────────────────────────────────────

    /**
     * Returns the nickname of the local player.
     *
     * @return the player's nickname
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * Sets the nickname of the local player.
     *
     * @param nickname the nickname to set
     */
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void openRulesPdf() {
        Platform.runLater(() -> {
            Scene scene = primaryStage == null ? null : primaryStage.getScene();
            if (scene == null || !(scene.getRoot() instanceof StackPane root)) {
                return;
            }

            if (rulesOverlay != null && root.getChildren().contains(rulesOverlay)) {
                return;
            }

            VBox pagesBox = new VBox(18);
            pagesBox.setAlignment(Pos.TOP_CENTER);
            pagesBox.setPadding(new Insets(12, 18, 18, 18));

            for (int i = 1; i <= 8; i++) {
                String path = String.format("/GUI-resources/rules/page-%02d.png", i);
                var stream = getClass().getResourceAsStream(path);
                if (stream == null) {
                    showRulesError("Rules page not found.\nExpected path: " + path);
                    return;
                }

                ImageView page = new ImageView(new Image(stream));
                page.setFitWidth(820);
                page.setPreserveRatio(true);
                page.setSmooth(true);
                page.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.45), 12, 0, 0, 3);");
                pagesBox.getChildren().add(page);
            }

            ScrollPane scroll = new ScrollPane(pagesBox);
            scroll.setFitToWidth(true);
            scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            scroll.setPrefViewportHeight(610);
            scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
            VBox.setVgrow(scroll, Priority.ALWAYS);

            Button closeButton = new Button("Close");
            closeButton.getStyleClass().add("rules-button");

            HBox header = new HBox(16);
            header.setAlignment(Pos.CENTER_RIGHT);
            Label title = new Label("Rules");
            title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #fde8b0;");
            HBox.setHgrow(title, Priority.ALWAYS);
            header.getChildren().addAll(title, closeButton);

            VBox panel = new VBox(12, header, scroll);
            panel.setMaxWidth(900);
            panel.setMaxHeight(720);
            panel.setPadding(new Insets(18));
            panel.setStyle(
                    "-fx-background-color: rgba(22,10,4,0.96); " +
                    "-fx-background-radius: 10; " +
                    "-fx-border-color: #c8860a; -fx-border-width: 2; -fx-border-radius: 10;"
            );

            rulesOverlay = new StackPane(panel);
            rulesOverlay.setStyle("-fx-background-color: rgba(0,0,0,0.72);");
            rulesOverlay.setPickOnBounds(true);

            closeButton.setOnAction(event -> closeRulesOverlay());
            rulesOverlay.setOnMouseClicked(event -> {
                if (event.getTarget() == rulesOverlay) {
                    closeRulesOverlay();
                }
            });

            root.getChildren().add(rulesOverlay);
            StackPane.setAlignment(rulesOverlay, Pos.CENTER);
        });
    }

    private void closeRulesOverlay() {
        if (primaryStage != null && primaryStage.getScene() != null
                && primaryStage.getScene().getRoot() instanceof StackPane root
                && rulesOverlay != null) {
            root.getChildren().remove(rulesOverlay);
        }
        rulesOverlay = null;
    }

    private void showRulesError(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.INFORMATION
        );
        alert.setTitle("Rules");
        alert.setHeaderText("Rules PDF unavailable");
        alert.setContentText(message);
        alert.showAndWait();
    }

    public List<MatchResult> getGlobalLeaderboard() {
        return globalLeaderboard;
    }

    public int getGlobalLeaderboardPosition() {
        return globalLeaderboardPosition;
    }

    @Override
    public void showLeaderboard(List<MatchResult> ranking, int position) {
        this.globalLeaderboard = ranking == null ? List.of() : new ArrayList<>(ranking);
        this.globalLeaderboardPosition = position;

        Platform.runLater(() -> {
            if (endGameController != null) {
                endGameController.render();
            }
        });
    }

    @Override
    public boolean askRecoveryChoice() {
        CompletableFuture<Boolean> answer = new CompletableFuture<>();

        Platform.runLater(() -> showRecoveryChoice(answer));

        try {
            return answer.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void showRecoveryUpdate(List<String> reconnectedPlayers, List<String> missingPlayers) {
        Platform.runLater(() -> {
            String joined = formatPlayers(reconnectedPlayers);
            String missing = formatPlayers(missingPlayers);

            showRecoveryOverlay(
                    "Recovering saved game",
                    "Waiting for every player to reconnect.",
                    "Back online: " + joined + "\nMissing: " + missing
            );
        });
    }

    @Override
    public void showRecoveryCancelled(String reason) {
        Platform.runLater(() -> {
            clearRecoveryOverlay();
            showLobbyScreen();
            if (lobbyController != null && reason != null && !reason.isBlank()) {
                lobbyController.showError(reason);
            }
        });
    }

    private void showRecoveryChoice(CompletableFuture<Boolean> answer) {
        Button yesButton = new Button("Yes");
        yesButton.getStyleClass().add("recovery-primary-button");
        yesButton.setMinWidth(130);

        Button noButton = new Button("No");
        noButton.getStyleClass().add("recovery-secondary-button");
        noButton.setMinWidth(130);

        HBox buttons = new HBox(16, yesButton, noButton);
        buttons.setAlignment(Pos.CENTER);

        showRecoveryOverlay(
                "Server is back online",
                "Do you want to reconnect to the saved game?",
                "Choose Yes to resume this match. Choose No to return to the lobby.",
                buttons
        );

        yesButton.setOnAction(event -> {
            showRecoveryOverlay(
                    "Recovering saved game",
                    "Your choice has been sent.",
                    "Waiting for the other players..."
            );
            answer.complete(true);
        });

        noButton.setOnAction(event -> {
            showRecoveryOverlay(
                    "Leaving saved game",
                    "Returning to the lobby.",
                    "You will be able to create a new game or join a lobby."
            );
            answer.complete(false);
        });
    }

    private void showRecoveryOverlay(String title, String message, String details) {
        showRecoveryOverlay(title, message, details, null);
    }

    private void showRecoveryOverlay(String title, String message, String details, HBox buttons) {
        Scene scene = primaryStage == null ? null : primaryStage.getScene();
        if (scene == null || !(scene.getRoot() instanceof StackPane root)) {
            return;
        }

        if (recoveryOverlay == null || recoveryOverlay.getScene() != scene) {
            recoveryOverlay = new StackPane();
            recoveryOverlay.getStyleClass().add("recovery-overlay");
            recoveryOverlay.setPickOnBounds(true);

            VBox panel = new VBox(16);
            panel.getStyleClass().add("recovery-panel");
            panel.setAlignment(Pos.CENTER);
            panel.setPrefWidth(500);
            panel.setPrefHeight(220);
            panel.setMaxWidth(500);
            panel.setMaxHeight(260);
            panel.setPadding(new Insets(22, 30, 22, 30));

            recoveryTitleLabel = new Label();
            recoveryTitleLabel.getStyleClass().add("recovery-title");
            recoveryTitleLabel.setWrapText(true);
            recoveryTitleLabel.setAlignment(Pos.CENTER);

            recoveryMessageLabel = new Label();
            recoveryMessageLabel.getStyleClass().add("recovery-message");
            recoveryMessageLabel.setWrapText(true);
            recoveryMessageLabel.setAlignment(Pos.CENTER);

            recoveryDetailsLabel = new Label();
            recoveryDetailsLabel.getStyleClass().add("recovery-details");
            recoveryDetailsLabel.setWrapText(true);
            recoveryDetailsLabel.setAlignment(Pos.CENTER);

            panel.getChildren().addAll(recoveryTitleLabel, recoveryMessageLabel, recoveryDetailsLabel);
            recoveryOverlay.getChildren().add(panel);
        }

        VBox panel = (VBox) recoveryOverlay.getChildren().get(0);
        panel.getChildren().removeIf(node -> node instanceof HBox);
        if (buttons != null) {
            panel.getChildren().add(buttons);
        }

        recoveryTitleLabel.setText(title);
        recoveryMessageLabel.setText(message);
        recoveryDetailsLabel.setText(details == null ? "" : details);
        recoveryOverlay.setMouseTransparent(false);

        if (!root.getChildren().contains(recoveryOverlay)) {
            root.getChildren().add(recoveryOverlay);
        }
    }

    private void clearRecoveryOverlay() {
        if (primaryStage != null && primaryStage.getScene() != null
                && primaryStage.getScene().getRoot() instanceof StackPane root
                && recoveryOverlay != null) {
            root.getChildren().remove(recoveryOverlay);
        }
        recoveryOverlay = null;
        recoveryTitleLabel = null;
        recoveryMessageLabel = null;
        recoveryDetailsLabel = null;
    }

    private void clearRecoveryOverlayIfConnected() {
        if (virtualServer == null || virtualServer.isConnected()) {
            clearRecoveryOverlay();
        }
    }

    private String formatPlayers(List<String> players) {
        if (players == null || players.isEmpty()) {
            return "none";
        }
        return String.join(", ", players);
    }

    private boolean isRecoverableDisconnection(String reason) {
        if (reason == null) {
            return false;
        }

        String normalized = reason.toLowerCase();
        return normalized.contains("server offline")
                || normalized.contains("waiting for recovery")
                || normalized.contains("server lost");
    }

    @Override
    public void shutdown(String reason) {
        Platform.runLater(() -> {
            System.out.println("[CLIENT] " + reason);
            Platform.exit();
            System.exit(0);
        });
    }
}
