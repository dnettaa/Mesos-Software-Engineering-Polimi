package it.polimi.ingsw.view.GUI;

import it.polimi.ingsw.leaderboard.MatchResult;
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
    private ClientModel clientModel;
    private Stage primaryStage;
    private String nickname;

    private GUILobbyController lobbyController;
    // private GUIGameController gameController;
    // private GUIEndGameController endGameController;

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
     * Loads and displays the in-game screen.
     * To be uncommented once {@code GUIGameController} is implemented.
     */
    public void showGameScreen() {
        // TODO: implement when GUIGameController is ready
    }

    /**
     * Loads and displays the end-game screen with final scores and ranking.
     * To be uncommented once {@code GUIEndGameController} is implemented.
     */
    public void showEndGameScreen() {
        // TODO: implement when GUIEndGameController is ready
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
        // if (gameController == null) showGameScreen();
    }

    /**
     * Re-renders the current screen by reading the latest state from the client model.
     * Must be called on or delegated to the JavaFX Application Thread.
     */
    @Override
    public void render() {
        // TODO: implement when GUIGameController is ready
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
            if (lobbyController != null) {
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
        // TODO: implement when GUIGameController is ready
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

            scalablePane.setManaged(false);
            scalablePane.setLayoutX((windowWidth - 1280) / 2);
            scalablePane.setLayoutY((windowHeight - 800) / 2);
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

    @Override
    public void showLeaderboard(List<MatchResult> ranking, int position) {
        // da implementare per gui
    }
}