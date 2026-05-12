package it.polimi.ingsw.view.GUI;

import it.polimi.ingsw.model.game.DTO.OfferSlotData;
import it.polimi.ingsw.model.game.DTO.PlayerData;
import it.polimi.ingsw.model.player.TotemColor;
import it.polimi.ingsw.view.CardCatalog;
import it.polimi.ingsw.view.ClientModel;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * JavaFX controller for the main game screen.
 *
 * This controller is intentionally a "dumb" GUI controller:
 * it reads the local ClientModel, renders it, collects user clicks,
 * and sends requests to the server through VirtualServer.
 * It does not validate game rules: the server remains authoritative.
 *
 * @author Diana
 */
public class GUIGameController{

    @FXML private Label roundLabel;
    @FXML private Label eraLabel;
    @FXML private Label phaseLabel;
    @FXML private Label activePlayerLabel;
    @FXML private Label nicknameLabel;

    @FXML private HBox upperRowBox;
    @FXML private HBox lowerRowBox;
    @FXML private HBox offerTrackBox;
    @FXML private HBox turnOrderBox;
    @FXML private VBox playersBox;

    @FXML private Label messageLabel;
    @FXML private Label errorLabel;
    @FXML private Button confirmActionButton;
    @FXML private Button clearSelectionButton;
    @FXML private TextArea eventLogArea;

    private GUI gui;

    /*
     * These lists store the cards currently selected by the local player.
     * We keep upper and lower selections separated because the server API
     * expects two different lists in takeCards(...).
     */
    private final List<String> selectedUpperCards = new ArrayList<>();
    private final List<String> selectedLowerCards = new ArrayList<>();

    /*
     * Used only during ExtraCardPhase.
     */
    private String selectedExtraCard;

    /**
     * Connects this controller to the main GUI instance.
     *
     * @param gui main GUI object
     */
    public void setGUI(GUI gui){
        this.gui = gui;
    }

    /**
     * Called by JavaFX after FXML loading.
     * We start with hidden action buttons; render() will decide when to show them.
     */
    @FXML
    public void initialize(){
        hideError();

        confirmActionButton.setVisible(false);
        confirmActionButton.setManaged(false);

        clearSelectionButton.setVisible(false);
        clearSelectionButton.setManaged(false);

        eventLogArea.setText("");
    }

    /**
     * Rebuilds the whole game screen from the current ClientModel.
     * This is simple and safe: every server update redraws the board consistently.
     */
    public void render(){
        if(gui == null || gui.getClientModel() == null){
            return;
        }

        ClientModel model = gui.getClientModel();

        renderHeader(model);
        renderCardRows(model);
        renderOfferTrack(model);
        renderTurnOrder(model);
        renderPlayers(model);
        renderActionArea(model);
    }

    /**
     * Updates top labels: round, era, phase, active player and local nickname.
     */
    private void renderHeader(ClientModel model){
        roundLabel.setText("Round " + model.getCurrentRound());
        eraLabel.setText("Era " + model.getCurrentEra());
        phaseLabel.setText("Phase: " + model.getCurrentPhaseName());
        activePlayerLabel.setText("Turn: " + safeText(model.getCurrentPlayerNickname()));
        nicknameLabel.setText(safeText(gui.getNickname()));
    }

    /**
     * Draws upper and lower card rows.
     */
    private void renderCardRows(ClientModel model){
        upperRowBox.getChildren().clear();
        lowerRowBox.getChildren().clear();

        for(String cardID : model.getUpperRowCardIDs()){
            upperRowBox.getChildren().add(createCardNode(cardID, true));
        }

        for(String cardID : model.getLowerRowCardIDs()){
            lowerRowBox.getChildren().add(createCardNode(cardID, false));
        }
    }

    /**
     * Draws the offer track using the offer tile images.
     * If the slot is occupied, the player's totem is drawn above the tile.
     */
    private void renderOfferTrack(ClientModel model){
        offerTrackBox.getChildren().clear();

        for (OfferSlotData slot : model.getOfferSlots()){
            StackPane slotPane = new StackPane();
            slotPane.setPrefSize(95, 105);

            ImageView tileImage = new ImageView(loadImage("/GUI-resources/tiles/offer/offer_" + slot.slotID() + ".png"));
            tileImage.setFitWidth(88);
            tileImage.setPreserveRatio(true);

            VBox content = new VBox(2);
            content.setAlignment(Pos.CENTER);

            if(slot.occupantNickname() != null){
                PlayerData occupant = model.getPlayers().get(slot.occupantNickname());
                if (occupant != null) {
                    ImageView totem = new ImageView(loadTotemImage(occupant.totemColor()));
                    totem.setFitHeight(38);
                    totem.setPreserveRatio(true);
                    content.getChildren().add(totem);
                }
            } else {
                Label freeLabel = new Label("FREE");
                freeLabel.setStyle("-fx-font-size: 11; -fx-font-weight: bold;");
                content.getChildren().add(freeLabel);
            }

            content.getChildren().add(tileImage);
            slotPane.getChildren().add(content);

            /*
             * During TotemPlacementPhase, clicking a free slot immediately sends placeTotem.
             * We do not pre-validate too much here: the server checks the real rule.
             */
            slotPane.setOnMouseClicked(event -> handleOfferSlotClick(slot));

            offerTrackBox.getChildren().add(slotPane);
        }
    }

    /**
     * Draws the turn order track with small totems and nicknames.
     */
    private void renderTurnOrder(ClientModel model) {
        turnOrderBox.getChildren().clear();

        for (String playerName : model.getTurnOrder()) {
            PlayerData player = model.getPlayers().get(playerName);
            if (player == null) continue;

            VBox playerBox = new VBox(2);
            playerBox.setAlignment(Pos.CENTER);

            ImageView totem = new ImageView(loadTotemImage(player.totemColor()));
            totem.setFitHeight(32);
            totem.setPreserveRatio(true);

            Label name = new Label(playerName);
            name.setStyle("-fx-font-size: 11; -fx-font-weight: bold;");

            playerBox.getChildren().addAll(totem, name);
            turnOrderBox.getChildren().add(playerBox);
        }
    }

    /**
     * Draws every player panel.
     * This is required because every client must show both its own state and opponents' state.
     */
    private void renderPlayers(ClientModel model) {
        playersBox.getChildren().clear();

        for (PlayerData player : model.getPlayers().values()) {
            VBox card = new VBox(6);
            card.setStyle("""
                    -fx-background-color: rgba(255, 245, 220, 0.86);
                    -fx-background-radius: 10;
                    -fx-padding: 8;
                    -fx-border-color: #5b3211;
                    -fx-border-radius: 10;
                    """);

            HBox header = new HBox(8);
            header.setAlignment(Pos.CENTER_LEFT);

            ImageView totem = new ImageView(loadTotemImage(player.totemColor()));
            totem.setFitHeight(30);
            totem.setPreserveRatio(true);

            Label name = new Label(player.nickname());
            name.setStyle("-fx-font-size: 15; -fx-font-weight: bold;");

            if (player.nickname().equals(gui.getNickname())) {
                name.setText(player.nickname() + " (you)");
            }

            if (player.nickname().equals(model.getCurrentPlayerNickname())) {
                name.setText(name.getText() + " ◀");
            }

            header.getChildren().addAll(totem, name);

            Label resources = new Label("Food: " + player.food()
                    + "   PP: " + player.prestigePoints()
                    + "   Tribe: " + player.tribeCardID().size()
                    + "   Buildings: " + player.buildingID().size());

            FlowPane tribeCards = createSmallCardList("Tribe", player.tribeCardID());
            FlowPane buildings = createSmallCardList("Buildings", player.buildingID());

            card.getChildren().addAll(header, resources, tribeCards, buildings);
            playersBox.getChildren().add(card);
        }
    }

    /**
     * Updates the bottom action panel depending on the current phase and active player.
     */
    private void renderActionArea(ClientModel model) {
        hideError();

        confirmActionButton.setVisible(false);
        confirmActionButton.setManaged(false);
        clearSelectionButton.setVisible(false);
        clearSelectionButton.setManaged(false);

        boolean isMyTurn = gui.getNickname() != null
                && gui.getNickname().equals(model.getCurrentPlayerNickname());

        if (!isMyTurn) {
            messageLabel.setText("Waiting for " + safeText(model.getCurrentPlayerNickname()) + "...");
            clearSelections();
            return;
        }

        switch (model.getCurrentPhaseName()) {
            case "TotemPlacementPhase" -> {
                messageLabel.setText("Your turn: choose a free offer slot.");
                clearSelections();
            }

            case "OfferResolutionPhase" -> {
                messageLabel.setText("Your turn: select cards from the upper/lower row, then confirm.");
                confirmActionButton.setText("Take selected cards");
                confirmActionButton.setVisible(true);
                confirmActionButton.setManaged(true);

                clearSelectionButton.setVisible(true);
                clearSelectionButton.setManaged(true);
            }

            case "ExtraCardPhase" -> {
                messageLabel.setText("Extra card phase: select one card, then confirm.");
                confirmActionButton.setText("Take extra card");
                confirmActionButton.setVisible(true);
                confirmActionButton.setManaged(true);

                clearSelectionButton.setVisible(true);
                clearSelectionButton.setManaged(true);
            }

            default -> {
                messageLabel.setText("Automatic phase. Waiting for the server...");
                clearSelections();
            }
        }
    }

    /**
     * Creates a clickable visual card node for upper/lower rows.
     *
     * @param cardID card identifier, for example CH01, BU03, EV02
     * @param upperRow true if the card belongs to the upper row
     * @return JavaFX node representing the card
     */
    private StackPane createCardNode(String cardID, boolean upperRow) {
        StackPane cardPane = new StackPane();
        cardPane.setPrefSize(86, 130);

        ImageView imageView = new ImageView(loadImage(getCardImagePath(cardID)));
        imageView.setFitWidth(78);
        imageView.setPreserveRatio(true);

        Label fallbackLabel = new Label(cardID);
        fallbackLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #3b1e00;");

        cardPane.getChildren().add(imageView);

        Tooltip.install(cardPane, new Tooltip(cardID + "\n" + CardCatalog.getDescription(cardID)));

        cardPane.setOnMouseClicked(event -> handleCardClick(cardID, upperRow, cardPane));

        /*
         * If the image is missing, ImageView can still exist but show nothing.
         * The fallback label makes debugging resources easier.
         */
        if (imageView.getImage() == null || imageView.getImage().isError()) {
            cardPane.getChildren().add(fallbackLabel);
        }

        return cardPane;
    }

    /**
     * Handles a click on a board card.
     */
    private void handleCardClick(String cardID, boolean upperRow, StackPane cardPane) {
        ClientModel model = gui.getClientModel();
        if (model == null || !isMyTurn(model)) {
            return;
        }

        String phase = model.getCurrentPhaseName();

        if ("OfferResolutionPhase".equals(phase)) {
            toggleNormalCardSelection(cardID, upperRow, cardPane);
        } else if ("ExtraCardPhase".equals(phase)) {
            selectedExtraCard = cardID;
            selectedUpperCards.clear();
            selectedLowerCards.clear();

            clearCardSelectionStyles();
            markSelected(cardPane);
        }
    }

    /**
     * Toggles a card selection during OfferResolutionPhase.
     */
    private void toggleNormalCardSelection(String cardID, boolean upperRow, StackPane cardPane) {
        List<String> targetList = upperRow ? selectedUpperCards : selectedLowerCards;

        if (targetList.contains(cardID)) {
            targetList.remove(cardID);
            unmarkSelected(cardPane);
        } else {
            targetList.add(cardID);
            markSelected(cardPane);
        }
    }

    /**
     * Handles click on an offer slot during TotemPlacementPhase.
     */
    private void handleOfferSlotClick(OfferSlotData slot) {
        ClientModel model = gui.getClientModel();

        if (model == null || !isMyTurn(model)) {
            return;
        }

        if (!"TotemPlacementPhase".equals(model.getCurrentPhaseName())) {
            return;
        }

        if (slot.occupantNickname() != null) {
            showError("This offer slot is already occupied.");
            return;
        }

        gui.getVirtualServer().placeTotem(gui.getNickname(), slot.slotID());
    }

    /**
     * Called by the Confirm button.
     */
    @FXML
    public void onConfirmAction() {
        ClientModel model = gui.getClientModel();
        if (model == null || !isMyTurn(model)) {
            return;
        }

        switch (model.getCurrentPhaseName()) {
            case "OfferResolutionPhase" -> {
                gui.getVirtualServer().takeCards(
                        gui.getNickname(),
                        new ArrayList<>(selectedUpperCards),
                        new ArrayList<>(selectedLowerCards)
                );
                clearSelections();
            }

            case "ExtraCardPhase" -> {
                if (selectedExtraCard == null) {
                    showError("Select one card first.");
                    return;
                }

                gui.getVirtualServer().takeExtraCard(gui.getNickname(), selectedExtraCard);
                clearSelections();
            }

            default -> showError("There is no manual action to confirm in this phase.");
        }
    }

    /**
     * Called by the Clear selection button.
     */
    @FXML
    public void onClearSelection() {
        clearSelections();
        clearCardSelectionStyles();
    }

    /**
     * Shows an in-game error.
     *
     * @param message error text
     */
    public void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    /**
     * Adds an event resolution summary to the event log.
     */
    public void showEventResolved(String eventCardID,
                                  String eventType,
                                  Map<String, Integer> ppDelta,
                                  Map<String, Integer> foodDelta) {
        StringBuilder builder = new StringBuilder();

        builder.append("Event resolved: ")
                .append(eventCardID)
                .append(" (")
                .append(eventType)
                .append(")\n");

        for (String player : ppDelta.keySet()) {
            int pp = ppDelta.getOrDefault(player, 0);
            int food = foodDelta.getOrDefault(player, 0);

            builder.append(" - ")
                    .append(player)
                    .append(": PP ")
                    .append(formatDelta(pp))
                    .append(", Food ")
                    .append(formatDelta(food))
                    .append("\n");
        }

        builder.append("\n");
        eventLogArea.appendText(builder.toString());
    }

    private void hideError() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    private boolean isMyTurn(ClientModel model) {
        return gui.getNickname() != null
                && gui.getNickname().equals(model.getCurrentPlayerNickname());
    }

    private void clearSelections() {
        selectedUpperCards.clear();
        selectedLowerCards.clear();
        selectedExtraCard = null;
    }

    private void markSelected(StackPane cardPane) {
        cardPane.setStyle("""
                -fx-border-color: gold;
                -fx-border-width: 4;
                -fx-border-radius: 8;
                -fx-background-radius: 8;
                """);
    }

    private void unmarkSelected(StackPane cardPane) {
        cardPane.setStyle("");
    }

    private void clearCardSelectionStyles() {
        upperRowBox.getChildren().forEach(node -> node.setStyle(""));
        lowerRowBox.getChildren().forEach(node -> node.setStyle(""));
    }

    private FlowPane createSmallCardList(String title, List<String> cardIDs) {
        FlowPane pane = new FlowPane(4, 4);

        Label titleLabel = new Label(title + ":");
        titleLabel.setStyle("-fx-font-size: 11; -fx-font-weight: bold;");
        pane.getChildren().add(titleLabel);

        if (cardIDs == null || cardIDs.isEmpty()) {
            Label empty = new Label("-");
            empty.setStyle("-fx-font-size: 11;");
            pane.getChildren().add(empty);
            return pane;
        }

        for (String cardID : cardIDs) {
            Label label = new Label(cardID);
            label.setStyle("""
                    -fx-background-color: rgba(255,255,255,0.75);
                    -fx-background-radius: 5;
                    -fx-padding: 2 5 2 5;
                    -fx-font-size: 10;
                    """);
            Tooltip.install(label, new Tooltip(CardCatalog.getDescription(cardID)));
            pane.getChildren().add(label);
        }

        return pane;
    }

    private String getCardImagePath(String cardID) {
        if (cardID == null) {
            return "";
        }

        if (cardID.startsWith("CH")) {
            return "/GUI-resources/characterCards/" + cardID + ".png";
        }

        if (cardID.startsWith("BU")) {
            return "/GUI-resources/buildingCards/" + cardID + ".png";
        }

        if (cardID.startsWith("EV")) {
            return "/GUI-resources/eventCards/" + cardID + ".png";
        }

        return "";
    }

    private Image loadTotemImage(TotemColor color) {
        if (color == null) {
            return null;
        }

        return loadImage("/GUI-resources/totem/" + color.name().toLowerCase() + ".png");
    }

    private Image loadImage(String path) {
        try {
            return new Image(getClass().getResourceAsStream(path));
        } catch (Exception e) {
            System.err.println("GUI image not found: " + path);
            return null;
        }
    }

    private String safeText(String text) {
        return text == null ? "-" : text;
    }

    private String formatDelta(int value) {
        return value >= 0 ? "+" + value : String.valueOf(value);
    }
}