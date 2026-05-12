package it.polimi.ingsw.view.GUI;

import it.polimi.ingsw.model.player.TotemColor;
import it.polimi.ingsw.network.VirtualServer;
import it.polimi.ingsw.view.ClientModel;
import it.polimi.ingsw.view.View;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Map;

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

    private GUILobbyController lobbyController;
    private GUIGameController gameController;
    private GUIEndGameController endGameController;

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
            if (isEndGamePhase()) {
                showEndGameScreen();
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

            /*
             * If the server says that the game is finished, we move to the final screen.
             */
            if (isEndGamePhase()) {
                if (endGameController == null) {
                    showEndGameScreen();
                } else {
                    endGameController.render();
                }
                return;
            }

            /*
             * Otherwise we render the normal game screen.
             */
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
     * Notifies the user of a disconnection, shows an error dialog and exits the application.
     *
     * @param reason human-readable disconnection reason
     */
    @Override
    public void notifyDisconnection(String reason) {
        Platform.runLater(() -> {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.ERROR
            );
            alert.setTitle("Disconnected");
            alert.setHeaderText("Connection lost");
            alert.setContentText(reason);
            alert.showAndWait();
            Platform.exit();
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
}