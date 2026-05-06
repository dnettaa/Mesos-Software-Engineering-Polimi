package it.polimi.ingsw.view.GUI;

import it.polimi.ingsw.model.player.TotemColor;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JavaFX controller for the lobby screen.
 * Handles player input (nickname, color, create/join), displays the list of
 * connected players and transitions to a waiting view once the user has confirmed.
 */
public class GUILobbyController {

    @FXML private HBox mainContentBox;
    @FXML private VBox inputColumn;
    @FXML private VBox lobbyListColumn;

    @FXML private TextField nicknameField;
    @FXML private ComboBox<TotemColor> colorComboBox;
    @FXML private RadioButton createRadio;
    @FXML private RadioButton joinRadio;
    @FXML private Spinner<Integer> playersSpinner;
    @FXML private VBox playersSpinnerBox;
    @FXML private Button confirmButton;
    @FXML private ListView<String> lobbyList;
    @FXML private Label statusLabel;
    @FXML private Label errorLabel;

    private GUI gui;

    /** Keeps track of the totem color assigned to each player in the lobby list. */
    private Map<String, TotemColor> currentColorsMap = new HashMap<>();

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
     * Configures radio buttons, the color combo box and the player list cell factory.
     */
    @FXML
    public void initialize() {
        ToggleGroup group = new ToggleGroup();
        createRadio.setToggleGroup(group);
        joinRadio.setToggleGroup(group);
        createRadio.setSelected(true);

        playersSpinnerBox.setVisible(true);
        playersSpinnerBox.setManaged(true);
        createRadio.selectedProperty().addListener((obs, oldVal, newVal) -> {
            playersSpinnerBox.setVisible(newVal);
            playersSpinnerBox.setManaged(newVal);
        });

        errorLabel.setVisible(false);

        colorComboBox.getItems().addAll(TotemColor.values());
        colorComboBox.setCellFactory(lv -> new TotemCell());
        colorComboBox.setButtonCell(new TotemCell());
        colorComboBox.getSelectionModel().selectFirst();

        lobbyList.setCellFactory(lv -> new ListCell<>() {
            private final ImageView imageView = new ImageView();

            @Override
            protected void updateItem(String playerName, boolean empty) {
                super.updateItem(playerName, empty);
                if (empty || playerName == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(playerName);
                    TotemColor color = currentColorsMap.get(playerName);
                    if (color != null) {
                        Image img = getTotemImage(color);
                        if (img != null) {
                            imageView.setImage(img);
                            imageView.setFitHeight(40);
                            imageView.setPreserveRatio(true);
                            setGraphic(imageView);
                        }
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });
    }

    /**
     * Handles the confirm button click.
     * Validates input and sends either a {@code createLobby} or {@code joinLobby}
     * request to the server.
     */
    @FXML
    public void onConfirm() {
        String nickname = nicknameField.getText().trim();
        if (nickname.isEmpty()) {
            showError("Enter a nickname.");
            return;
        }

        TotemColor color = colorComboBox.getValue();
        if (color == null) {
            showError("Select a color.");
            return;
        }

        confirmButton.setDisable(true);
        errorLabel.setVisible(false);
        gui.setNickname(nickname);

        if (createRadio.isSelected()) {
            int numPlayers = playersSpinner.getValue();
            gui.getVirtualServer().createLobby(nickname, color, numPlayers);
        } else {
            gui.getVirtualServer().joinLobby(nickname, color);
        }
    }

    /**
     * Updates the player list and the status label with the current lobby state.
     *
     * @param players        ordered list of connected player nicknames
     * @param colorsByPlayer map from nickname to assigned totem color
     * @param expected       total number of players required to start
     */
    public void updateLobby(List<String> players,
                            Map<String, TotemColor> colorsByPlayer,
                            int expected) {
        this.currentColorsMap = colorsByPlayer;

        lobbyList.getItems().clear();
        lobbyList.getItems().addAll(players);

        if (players.size() == expected) {
            statusLabel.setText("Tribe is complete! Starting...");
            statusLabel.setStyle("-fx-font-size: 28; -fx-font-weight: bold; -fx-text-fill: #8b2500;");
        } else {
            statusLabel.setText("Warriors: " + players.size() + " / " + expected);
            statusLabel.setStyle("-fx-font-size: 28; -fx-font-weight: bold; -fx-text-fill: #3b1e00;");
        }
    }

    /**
     * Hides the input form and shows only the waiting player list.
     * Called once the player has successfully joined or created a lobby.
     */
    public void switchToWaitingMode() {
        inputColumn.setVisible(false);
        inputColumn.setManaged(false);

        lobbyListColumn.setVisible(true);
        lobbyListColumn.setManaged(true);

        statusLabel.setStyle("-fx-font-size: 28; -fx-font-weight: bold; -fx-text-fill: #3b1e00;");
    }

    /**
     * Displays an error message and re-enables the confirm button.
     *
     * @param msg the error message to display
     */
    public void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
        confirmButton.setDisable(false);
    }

    // ── Image helpers ─────────────────────────────────────────────────────────

    /**
     * Loads the totem image for the given color from the GUI resources.
     * Returns {@code null} if the image file is not found.
     *
     * @param color the totem color whose image should be loaded
     * @return the loaded {@link Image}, or {@code null} if not found
     */
    private Image getTotemImage(TotemColor color) {
        if (color == null) return null;
        String path = "/GUI-resources/totem/" + color.name().toLowerCase() + ".png";
        try {
            return new Image(getClass().getResourceAsStream(path));
        } catch (Exception e) {
            System.err.println("Totem image not found: " + path);
            return null;
        }
    }

    /**
     * Custom {@link ListCell} that displays a totem color as an image inside
     * the {@link ComboBox}. Falls back to the color name if the image is missing.
     */
    private class TotemCell extends ListCell<TotemColor> {
        private final ImageView imageView = new ImageView();

        @Override
        protected void updateItem(TotemColor item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                Image img = getTotemImage(item);
                if (img != null) {
                    imageView.setImage(img);
                    imageView.setFitHeight(30);
                    imageView.setPreserveRatio(true);
                    setText(null);
                    setGraphic(imageView);
                } else {
                    setText(item.name());
                    setGraphic(null);
                }
            }
        }
    }
}