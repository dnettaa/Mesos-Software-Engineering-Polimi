package it.polimi.ingsw.view.GUI;

import it.polimi.ingsw.network.rmi.RMIClientAdapter;
import it.polimi.ingsw.network.socket.VirtualSocketServer;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

/**
 * JavaFX controller for the welcome screen.
 * Plays an intro animation (logo fade-in and slide-up, then input panel slide-in)
 * and handles the connection to the server via Socket or RMI.
 */
public class GUIWelcomeController {

    @FXML private ImageView logoImage;
    @FXML private StackPane inputPanel;
    @FXML private RadioButton socketRadio;
    @FXML private RadioButton rmiRadio;
    @FXML private TextField hostField;
    @FXML private Button connectButton;
    @FXML private Label errorLabel;

    private GUI gui;

    /**
     * Binds this controller to the main {@link GUI} instance.
     *
     * @param gui the application GUI
     */
    public void setGUI(GUI gui) {
        this.gui = gui;
    }

    /**
     * Called automatically by JavaFX after the FXML is loaded.
     * Configures the radio button group, default field values and plays the intro animation.
     */
    @FXML
    public void initialize() {
        ToggleGroup group = new ToggleGroup();
        socketRadio.setToggleGroup(group);
        rmiRadio.setToggleGroup(group);
        socketRadio.setSelected(true);
        hostField.setText("localhost");
        errorLabel.setVisible(false);

        // Phase 1: logo fades in and scales up
        FadeTransition logoFade = new FadeTransition(Duration.seconds(1.2), logoImage);
        logoFade.setFromValue(0);
        logoFade.setToValue(1);

        ScaleTransition logoScaleIn = new ScaleTransition(Duration.seconds(1.2), logoImage);
        logoScaleIn.setFromX(0.8);
        logoScaleIn.setFromY(0.8);
        logoScaleIn.setToX(1.0);
        logoScaleIn.setToY(1.0);

        ParallelTransition logoIntro = new ParallelTransition(logoFade, logoScaleIn);

        // Phase 2: logo slides up and scales down
        TranslateTransition logoMove = new TranslateTransition(Duration.seconds(1.0), logoImage);
        logoMove.setToY(-280);

        ScaleTransition logoScaleDown = new ScaleTransition(Duration.seconds(1.0), logoImage);
        logoScaleDown.setToX(0.65);
        logoScaleDown.setToY(0.65);

        ParallelTransition logoOutro = new ParallelTransition(logoMove, logoScaleDown);

        // Phase 3: input panel fades in and slides up from below
        FadeTransition inputFade = new FadeTransition(Duration.seconds(1.0), inputPanel);
        inputFade.setFromValue(0);
        inputFade.setToValue(1);

        TranslateTransition inputSlide = new TranslateTransition(Duration.seconds(1.0), inputPanel);
        inputSlide.setFromY(150);
        inputSlide.setToY(100);

        ParallelTransition inputIntro = new ParallelTransition(inputFade, inputSlide);

        new SequentialTransition(
                logoIntro,
                new PauseTransition(Duration.seconds(0.8)),
                new ParallelTransition(logoOutro, inputIntro)
        ).play();
    }

    /**
     * Handles the connect button click.
     * Validates the host field, creates the appropriate network adapter (Socket or RMI),
     * connects to the server and navigates to the lobby screen.
     */
    @FXML
    public void onConnect() {
        String host = hostField.getText().trim();
        if (host.isEmpty()) {
            errorLabel.setText("Please enter a host address.");
            errorLabel.setVisible(true);
            return;
        }

        connectButton.setDisable(true);
        errorLabel.setVisible(false);

        try {
            if (socketRadio.isSelected()) {
                VirtualSocketServer socketServer = new VirtualSocketServer(gui);
                gui.setVirtualServer(socketServer);
                socketServer.connect(host, 12345);
            } else {
                RMIClientAdapter rmiClient = new RMIClientAdapter(gui);
                gui.setVirtualServer(rmiClient);
                rmiClient.connect(host, 1099);
            }
            gui.showLobbyScreen();
        } catch (Exception e) {
            errorLabel.setText("Connection failed: " + e.getMessage());
            errorLabel.setVisible(true);
            connectButton.setDisable(false);
        }
    }
}