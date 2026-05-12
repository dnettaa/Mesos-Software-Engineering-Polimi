package it.polimi.ingsw.view.GUI;

import it.polimi.ingsw.model.game.DTO.OfferSlotData;
import it.polimi.ingsw.model.game.DTO.PlayerData;
import it.polimi.ingsw.model.player.TotemColor;
import it.polimi.ingsw.view.CardCatalog;
import it.polimi.ingsw.view.ClientModel;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import javafx.util.Duration;

import java.util.*;

/**
 * JavaFX controller for the main game screen.
 * <p>
 * This controller is intentionally a "dumb" GUI controller: it reads the local
 * {@link ClientModel}, renders it, collects user clicks, and forwards actions to
 * the server through {@link it.polimi.ingsw.network.VirtualServer}.
 * It does not validate game rules — the server remains the single source of truth.
 * </p>
 *
 * @author Diana
 */
public class GUIGameController {

    // ── Header ────────────────────────────────────────────────────────────────
    @FXML private Label roundLabel;
    @FXML private Label eraLabel;
    @FXML private Label phaseLabel;
    @FXML private Label activePlayerLabel;
    @FXML private Label nicknameLabel;

    // ── Board ─────────────────────────────────────────────────────────────────
    @FXML private HBox upperRowBox;
    @FXML private HBox lowerRowBox;
    @FXML private HBox offerTrackBox;
    @FXML private HBox turnOrderBox;

    // ── Right panel (local player stats) ─────────────────────────────────────
    @FXML private VBox rightPanel;
    @FXML private Label myFoodLabel;
    @FXML private Label myPPLabel;
    @FXML private VBox myTribeBox;

    // ── Other players list ────────────────────────────────────────────────────
    @FXML private VBox playersBox;

    // ── Action area ───────────────────────────────────────────────────────────
    @FXML private Label messageLabel;
    @FXML private Label errorLabel;
    @FXML private Button confirmActionButton;
    @FXML private Button clearSelectionButton;
    @FXML private TextArea eventLogArea;

    private GUI gui;

    /**
     * Upper-row cards currently selected by the local player.
     * Kept separate from lower because the server API expects two distinct lists in takeCards().
     */
    private final List<String> selectedUpperCards = new ArrayList<>();

    /** Lower-row cards currently selected by the local player. */
    private final List<String> selectedLowerCards = new ArrayList<>();

    /** Card selected during ExtraCardPhase. */
    private String selectedExtraCard;

    /**
     * Maps card ID to its StackPane node so that pick animations can target it after render().
     * Rebuilt on every render() call.
     */
    private final Map<String, StackPane> cardNodeMap = new HashMap<>();

    /** Currently open player stats popup, if any. */
    private Popup currentPopup;

    /** Character types shown in the stats panel, in display order. */
    private static final List<String> CHAR_TYPES =
            List.of("HUNTER", "SHAMAN", "BUILDER", "INVENTOR", "ARTIST", "GATHERER");

    // ── Setup ─────────────────────────────────────────────────────────────────

    /**
     * Connects this controller to the main {@link GUI} instance.
     *
     * @param gui the application GUI
     */
    public void setGUI(GUI gui) {
        this.gui = gui;
    }

    /**
     * Sets the local player nickname label.
     *
     * @param nickname the local player's nickname
     */
    public void setNickname(String nickname) {
        if (nicknameLabel != null) nicknameLabel.setText(nickname);
    }

    /**
     * Called automatically by JavaFX after the FXML is loaded.
     * Hides action buttons until renderActionArea() decides to show them.
     */
    @FXML
    public void initialize() {
        hideError();
        confirmActionButton.setVisible(false);
        confirmActionButton.setManaged(false);
        clearSelectionButton.setVisible(false);
        clearSelectionButton.setManaged(false);
        eventLogArea.setText("");
    }

    // ── Main render ───────────────────────────────────────────────────────────

    /**
     * Rebuilds the whole game screen from the current {@link ClientModel}.
     * Called on every server update; safe to call multiple times.
     *
     * @param model the latest client model snapshot
     */
    public void render(ClientModel model) {
        if (model == null) return;
        cardNodeMap.clear();
        renderHeader(model);
        renderCardRows(model);
        renderOfferTrack(model);
        renderTurnOrder(model);
        renderOtherPlayers(model);
        renderOwnStats(model);
        renderActionArea(model);
    }

    // ── Render helpers ────────────────────────────────────────────────────────

    /**
     * Updates the top header labels (round, era, phase, active player, local nickname).
     */
    private void renderHeader(ClientModel model) {
        roundLabel.setText("Round " + model.getCurrentRound());
        eraLabel.setText("Era " + model.getCurrentEra());
        phaseLabel.setText("Phase: " + model.getCurrentPhaseName());
        activePlayerLabel.setText("Turn: " + safeText(model.getCurrentPlayerNickname()));
        nicknameLabel.setText(safeText(gui.getNickname()));
    }

    /**
     * Draws upper and lower card rows with click handling and hover effects.
     */
    private void renderCardRows(ClientModel model) {
        upperRowBox.getChildren().clear();
        lowerRowBox.getChildren().clear();

        for (String cardID : model.getUpperRowCardIDs()) {
            StackPane node = createCardNode(cardID, true);
            cardNodeMap.put(cardID, node);
            upperRowBox.getChildren().add(node);
        }

        for (String cardID : model.getLowerRowCardIDs()) {
            StackPane node = createCardNode(cardID, false);
            cardNodeMap.put(cardID, node);
            lowerRowBox.getChildren().add(node);
        }
    }

    /**
     * Draws the offer track. Occupied slots show the occupant's totem.
     * Free slots pulse with a gold glow during TotemPlacementPhase to hint interactivity.
     * Clicking a free slot during TotemPlacementPhase sends placeTotem to the server.
     */
    private void renderOfferTrack(ClientModel model) {
        offerTrackBox.getChildren().clear();

        for (OfferSlotData slot : model.getOfferSlots()) {
            StackPane slotPane = new StackPane();
            slotPane.setPrefSize(95, 105);

            ImageView tileImage = new ImageView(
                    loadImage("/GUI-resources/tiles/offer/offer_" + slot.slotID() + ".png"));
            tileImage.setFitWidth(88);
            tileImage.setPreserveRatio(true);

            VBox content = new VBox(2);
            content.setAlignment(Pos.CENTER);

            if (slot.occupantNickname() != null) {
                PlayerData occupant = model.getPlayers().get(slot.occupantNickname());
                if (occupant != null) {
                    ImageView totem = new ImageView(loadTotemImage(occupant.totemColor()));
                    totem.setFitHeight(38);
                    totem.setPreserveRatio(true);
                    content.getChildren().add(totem);
                }
            } else if ("TotemPlacementPhase".equals(model.getCurrentPhaseName()) && isMyTurn(model)) {
                DropShadow glow = new DropShadow(12, Color.GOLD);
                tileImage.setEffect(glow);

                ScaleTransition pulse = new ScaleTransition(Duration.millis(700), tileImage);
                pulse.setFromX(1.0);
                pulse.setFromY(1.0);
                pulse.setToX(1.06);
                pulse.setToY(1.06);
                pulse.setAutoReverse(true);
                pulse.setCycleCount(Animation.INDEFINITE);
                pulse.play();
            }

            content.getChildren().add(tileImage);
            slotPane.getChildren().add(content);
            slotPane.setOnMouseClicked(e -> handleOfferSlotClick(slot));
            offerTrackBox.getChildren().add(slotPane);
        }
    }

    /**
     * Draws the turn order track using the correct tile image for the current player count,
     * with totem icons overlaid in order.
     */
    private void renderTurnOrder(ClientModel model) {
        turnOrderBox.getChildren().clear();

        int numPlayers = model.getPlayers().size();
        int clampedNum = Math.max(2, Math.min(5, numPlayers));

        StackPane trackPane = new StackPane();

        ImageView trackImage = new ImageView(
                loadImage("/GUI-resources/tiles/order/order_" + clampedNum + ".png"));
        trackImage.setFitWidth(160);
        trackImage.setPreserveRatio(true);
        trackPane.getChildren().add(trackImage);

        VBox totemColumn = new VBox(4);
        totemColumn.setAlignment(Pos.CENTER);

        for (String playerName : model.getTurnOrder()) {
            PlayerData player = model.getPlayers().get(playerName);
            if (player == null) continue;

            HBox row = new HBox(4);
            row.setAlignment(Pos.CENTER_LEFT);

            ImageView totem = new ImageView(loadTotemImage(player.totemColor()));
            totem.setFitHeight(24);
            totem.setPreserveRatio(true);

            Label name = new Label(playerName);
            name.setStyle("-fx-font-size: 10; -fx-font-weight: bold; -fx-text-fill: #3b1e00;");

            row.getChildren().addAll(totem, name);
            totemColumn.getChildren().add(row);
        }

        trackPane.getChildren().add(totemColumn);
        turnOrderBox.getChildren().add(trackPane);
    }

    /**
     * Renders the right panel showing the local player's food, PP and tribe cards grouped by type.
     */
    private void renderOwnStats(ClientModel model) {
        if (gui.getNickname() == null) return;
        PlayerData me = model.getPlayers().get(gui.getNickname());
        if (me == null) return;

        myFoodLabel.setText("Food: " + me.food());
        myPPLabel.setText("PP: " + me.prestigePoints());
        myTribeBox.getChildren().clear();

        Map<String, List<String>> byType = groupCardsByType(me.tribeCardID());

        for (String type : CHAR_TYPES) {
            List<String> cards = byType.getOrDefault(type, new ArrayList<>());
            if (cards.isEmpty()) continue;

            VBox section = new VBox(4);
            section.setStyle("-fx-padding: 4 0 4 0;");

            HBox header = new HBox(6);
            header.setAlignment(Pos.CENTER_LEFT);

            // TODO: replace emoji with ImageView once type icon images are available in resources
            Label icon = new Label(typeEmoji(type));
            icon.setStyle("-fx-font-size: 16;");
            Label typeLabel = new Label(type + " ×" + cards.size());
            typeLabel.setStyle("-fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: #3b1e00;");

            header.getChildren().addAll(icon, typeLabel);
            section.getChildren().add(header);

            FlowPane cardFlow = new FlowPane(4, 4);
            for (String cardID : cards) cardFlow.getChildren().add(createMiniCard(cardID));
            section.getChildren().add(cardFlow);
            myTribeBox.getChildren().add(section);
        }

        if (!me.buildingID().isEmpty()) {
            VBox buildSection = new VBox(4);
            Label buildHeader = new Label("🏛 Buildings ×" + me.buildingID().size());
            buildHeader.setStyle("-fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: #3b1e00;");
            buildSection.getChildren().add(buildHeader);

            FlowPane buildFlow = new FlowPane(4, 4);
            for (String cardID : me.buildingID()) buildFlow.getChildren().add(createMiniCard(cardID));
            buildSection.getChildren().add(buildFlow);
            myTribeBox.getChildren().add(buildSection);
        }
    }

    /**
     * Renders the list of other players with clickable nicknames that open a stats popup.
     */
    private void renderOtherPlayers(ClientModel model) {
        playersBox.getChildren().clear();

        for (PlayerData player : model.getPlayers().values()) {
            if (player.nickname().equals(gui.getNickname())) continue;

            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(6));
            row.setStyle("""
                    -fx-background-color: rgba(255,245,220,0.75);
                    -fx-background-radius: 8;
                    -fx-border-color: #8b5c2a;
                    -fx-border-radius: 8;
                    """);

            ImageView totem = new ImageView(loadTotemImage(player.totemColor()));
            totem.setFitHeight(28);
            totem.setPreserveRatio(true);

            Button nameBtn = new Button(player.nickname());
            nameBtn.setStyle("""
                    -fx-background-color: transparent;
                    -fx-text-fill: #3b1e00;
                    -fx-font-weight: bold;
                    -fx-font-size: 14;
                    -fx-cursor: hand;
                    -fx-underline: true;
                    -fx-padding: 0;
                    """);

            if (player.nickname().equals(model.getCurrentPlayerNickname())) {
                nameBtn.setText(player.nickname() + " ◀");
            }

            nameBtn.setOnAction(e -> showPlayerPopup(player, nameBtn));

            Label stats = new Label("Food: " + player.food() + "  PP: " + player.prestigePoints());
            stats.setStyle("-fx-font-size: 12; -fx-text-fill: #5c3a21;");

            row.getChildren().addAll(totem, nameBtn, stats);
            playersBox.getChildren().add(row);
        }
    }

    /**
     * Shows an animated popup with a player's full tribe stats.
     * Closes any previously open popup first.
     *
     * @param player the player whose stats to display
     * @param anchor the node to position the popup near
     */
    private void showPlayerPopup(PlayerData player, Node anchor) {
        if (currentPopup != null) currentPopup.hide();

        VBox content = new VBox(8);
        content.setPadding(new Insets(16));
        content.setMaxWidth(380);
        content.setStyle("""
                -fx-background-color: #f5f5dc;
                -fx-background-radius: 12;
                -fx-border-color: #5c3a21;
                -fx-border-width: 3;
                -fx-border-radius: 12;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 20, 0, 0, 4);
                """);

        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        ImageView totem = new ImageView(loadTotemImage(player.totemColor()));
        totem.setFitHeight(36);
        totem.setPreserveRatio(true);
        Label title = new Label(player.nickname() + "'s Tribe");
        title.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #3b1e00;");
        Button closeBtn = new Button("✕");
        closeBtn.setStyle("""
                -fx-background-color: transparent;
                -fx-text-fill: #8b0000;
                -fx-font-weight: bold;
                -fx-cursor: hand;
                """);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(totem, title, spacer, closeBtn);
        content.getChildren().add(header);

        Label res = new Label("Food: " + player.food() + "   PP: " + player.prestigePoints());
        res.setStyle("-fx-font-size: 13; -fx-text-fill: #5c3a21;");
        content.getChildren().add(res);
        content.getChildren().add(new Separator());

        Map<String, List<String>> byType = groupCardsByType(player.tribeCardID());
        for (String type : CHAR_TYPES) {
            List<String> cards = byType.getOrDefault(type, new ArrayList<>());
            if (cards.isEmpty()) continue;

            VBox section = new VBox(4);
            HBox typeHeader = new HBox(6);
            typeHeader.setAlignment(Pos.CENTER_LEFT);
            Label icon = new Label(typeEmoji(type));
            icon.setStyle("-fx-font-size: 15;");
            Label typeName = new Label(type + " ×" + cards.size());
            typeName.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: #3b1e00;");
            typeHeader.getChildren().addAll(icon, typeName);

            FlowPane cardFlow = new FlowPane(4, 4);
            for (String cardID : cards) cardFlow.getChildren().add(createMiniCard(cardID));
            section.getChildren().addAll(typeHeader, cardFlow);
            content.getChildren().add(section);
        }

        if (!player.buildingID().isEmpty()) {
            VBox buildSection = new VBox(4);
            Label buildHdr = new Label("🏛 Buildings ×" + player.buildingID().size());
            buildHdr.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: #3b1e00;");
            FlowPane buildFlow = new FlowPane(4, 4);
            for (String id : player.buildingID()) buildFlow.getChildren().add(createMiniCard(id));
            buildSection.getChildren().addAll(buildHdr, buildFlow);
            content.getChildren().add(buildSection);
        }

        Popup popup = new Popup();
        popup.getContent().add(content);
        popup.setAutoHide(true);
        currentPopup = popup;
        closeBtn.setOnAction(e -> animatePopupOut(content, popup));

        javafx.geometry.Bounds bounds = anchor.localToScreen(anchor.getBoundsInLocal());
        popup.show(anchor.getScene().getWindow(), bounds.getMinX() + 30, bounds.getMinY() - 20);

        content.setOpacity(0);
        content.setScaleX(0.7);
        content.setScaleY(0.7);
        new ParallelTransition(
                makeFade(content, 0, 1, 250),
                makeScale(content, 0.7, 1.0, 250)
        ).play();
    }

    /**
     * Animates a popup out (fade + shrink) then hides it.
     */
    private void animatePopupOut(VBox content, Popup popup) {
        ParallelTransition anim = new ParallelTransition(
                makeFade(content, 1, 0, 180),
                makeScale(content, 1.0, 0.7, 180)
        );
        anim.setOnFinished(e -> popup.hide());
        anim.play();
    }

    // ── Action area ───────────────────────────────────────────────────────────

    /**
     * Updates the action panel based on the current phase and whether it is the local player's turn.
     */
    private void renderActionArea(ClientModel model) {
        hideError();
        confirmActionButton.setVisible(false);
        confirmActionButton.setManaged(false);
        clearSelectionButton.setVisible(false);
        clearSelectionButton.setManaged(false);

        if (!isMyTurn(model)) {
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
                messageLabel.setText("Select cards from the rows, then confirm.");
                showActionButtons("Take selected cards");
            }
            case "ExtraCardPhase" -> {
                messageLabel.setText("Extra card: select one card, then confirm.");
                showActionButtons("Take extra card");
            }
            default -> {
                messageLabel.setText("Automatic phase. Waiting for the server...");
                clearSelections();
            }
        }
    }

    private void showActionButtons(String confirmText) {
        confirmActionButton.setText(confirmText);
        confirmActionButton.setVisible(true);
        confirmActionButton.setManaged(true);
        clearSelectionButton.setVisible(true);
        clearSelectionButton.setManaged(true);
    }

    // ── Card node creation ────────────────────────────────────────────────────

    /**
     * Creates a clickable card node with hover glow and selection animation.
     *
     * @param cardID   card identifier (e.g. "CH05", "BU02", "EV01")
     * @param upperRow true if the card belongs to the upper row
     * @return the assembled {@link StackPane} node
     */
    private StackPane createCardNode(String cardID, boolean upperRow) {
        StackPane cardPane = new StackPane();
        cardPane.setPrefSize(86, 130);

        ImageView imageView = new ImageView(loadImage(getCardImagePath(cardID)));
        imageView.setFitWidth(78);
        imageView.setPreserveRatio(true);
        cardPane.getChildren().add(imageView);

        if (imageView.getImage() == null || imageView.getImage().isError()) {
            Label fallback = new Label(cardID);
            fallback.setStyle("-fx-font-weight: bold; -fx-text-fill: #3b1e00; -fx-font-size: 10;");
            cardPane.getChildren().add(fallback);
        }

        Tooltip.install(cardPane, new Tooltip(cardID + "\n" + CardCatalog.getDescription(cardID)));

        DropShadow hoverGlow = new DropShadow(10, Color.GOLD);
        cardPane.setOnMouseEntered(e -> {
            if (!selectedUpperCards.contains(cardID) && !selectedLowerCards.contains(cardID)
                    && !cardID.equals(selectedExtraCard)) {
                cardPane.setEffect(hoverGlow);
            }
        });
        cardPane.setOnMouseExited(e -> {
            if (!selectedUpperCards.contains(cardID) && !selectedLowerCards.contains(cardID)
                    && !cardID.equals(selectedExtraCard)) {
                cardPane.setEffect(null);
            }
        });

        cardPane.setOnMouseClicked(e -> handleCardClick(cardID, upperRow, cardPane));
        return cardPane;
    }

    /**
     * Creates a small card thumbnail with tooltip, used in stats panels and popups.
     *
     * @param cardID card identifier
     * @return mini card {@link StackPane}
     */
    private StackPane createMiniCard(String cardID) {
        StackPane mini = new StackPane();
        mini.setPrefSize(48, 70);

        ImageView img = new ImageView(loadImage(getCardImagePath(cardID)));
        img.setFitWidth(44);
        img.setPreserveRatio(true);
        mini.getChildren().add(img);

        if (img.getImage() == null || img.getImage().isError()) {
            Label fallback = new Label(cardID);
            fallback.setStyle("-fx-font-size: 8; -fx-text-fill: #3b1e00;");
            mini.getChildren().add(fallback);
        }

        Tooltip.install(mini, new Tooltip(cardID + "\n" + CardCatalog.getDescription(cardID)));
        return mini;
    }

    // ── Click handlers ────────────────────────────────────────────────────────

    /**
     * Handles a click on a board card during OfferResolutionPhase or ExtraCardPhase.
     */
    private void handleCardClick(String cardID, boolean upperRow, StackPane cardPane) {
        ClientModel model = gui.getClientModel();
        if (model == null || !isMyTurn(model)) return;

        String phase = model.getCurrentPhaseName();
        if ("OfferResolutionPhase".equals(phase)) {
            toggleCardSelection(cardID, upperRow, cardPane);
        } else if ("ExtraCardPhase".equals(phase)) {
            clearCardSelectionStyles();
            selectedUpperCards.clear();
            selectedLowerCards.clear();
            selectedExtraCard = cardID;
            animateSelect(cardPane);
        }
    }

    /**
     * Toggles the selection state of a card during OfferResolutionPhase.
     */
    private void toggleCardSelection(String cardID, boolean upperRow, StackPane cardPane) {
        List<String> target = upperRow ? selectedUpperCards : selectedLowerCards;
        if (target.contains(cardID)) {
            target.remove(cardID);
            animateDeselect(cardPane);
        } else {
            target.add(cardID);
            animateSelect(cardPane);
        }
    }

    /**
     * Handles a click on an offer slot during TotemPlacementPhase.
     * The server validates whether the slot is truly free and whether it is the player's turn.
     */
    private void handleOfferSlotClick(OfferSlotData slot) {
        ClientModel model = gui.getClientModel();
        if (model == null || !isMyTurn(model)) return;
        if (!"TotemPlacementPhase".equals(model.getCurrentPhaseName())) return;
        if (slot.occupantNickname() != null) {
            showError("This slot is already occupied.");
            return;
        }
        gui.getVirtualServer().placeTotem(gui.getNickname(), slot.slotID());
    }

    // ── Action buttons ────────────────────────────────────────────────────────

    /**
     * Called by the Confirm button.
     * Plays pick animations on selected cards then sends the action to the server.
     */
    @FXML
    public void onConfirmAction() {
        ClientModel model = gui.getClientModel();
        if (model == null || !isMyTurn(model)) return;

        switch (model.getCurrentPhaseName()) {
            case "OfferResolutionPhase" -> {
                List<String> all = new ArrayList<>();
                all.addAll(selectedUpperCards);
                all.addAll(selectedLowerCards);
                List<String> upper = new ArrayList<>(selectedUpperCards);
                List<String> lower = new ArrayList<>(selectedLowerCards);
                clearSelections();
                animatePickCards(all, () ->
                        gui.getVirtualServer().takeCards(gui.getNickname(), upper, lower));
            }
            case "ExtraCardPhase" -> {
                if (selectedExtraCard == null) {
                    showError("Select one card first.");
                    return;
                }
                String extra = selectedExtraCard;
                clearSelections();
                animatePickCards(List.of(extra), () ->
                        gui.getVirtualServer().takeExtraCard(gui.getNickname(), extra));
            }
            default -> showError("Nothing to confirm in this phase.");
        }
    }

    /**
     * Called by the Clear selection button. Removes all current selections.
     */
    @FXML
    public void onClearSelection() {
        clearSelections();
        clearCardSelectionStyles();
    }

    // ── Animations ────────────────────────────────────────────────────────────

    /**
     * Plays a bounce and gold-glow animation to mark a card as selected.
     *
     * @param cardPane the card node to animate
     */
    private void animateSelect(StackPane cardPane) {
        cardPane.setStyle("-fx-border-color: gold; -fx-border-width: 3; -fx-border-radius: 6;");
        cardPane.setEffect(new Glow(0.8));

        ScaleTransition bounce = new ScaleTransition(Duration.millis(150), cardPane);
        bounce.setFromX(1.0);
        bounce.setFromY(1.0);
        bounce.setToX(1.12);
        bounce.setToY(1.12);
        bounce.setAutoReverse(true);
        bounce.setCycleCount(2);
        bounce.play();
    }

    /**
     * Removes the selection style and glow from a card node.
     *
     * @param cardPane the card node to deselect
     */
    private void animateDeselect(StackPane cardPane) {
        cardPane.setStyle("");
        cardPane.setEffect(null);
    }

    /**
     * Animates selected cards flying upward (pick animation), then invokes {@code onComplete}.
     *
     * @param cardIDs    card IDs whose nodes should be animated
     * @param onComplete callback executed after all animations finish
     */
    private void animatePickCards(List<String> cardIDs, Runnable onComplete) {
        if (cardIDs.isEmpty()) {
            onComplete.run();
            return;
        }

        List<Animation> animations = new ArrayList<>();
        for (String cardID : cardIDs) {
            StackPane node = cardNodeMap.get(cardID);
            if (node == null) continue;

            TranslateTransition slide = new TranslateTransition(Duration.millis(350), node);
            slide.setToY(-200);

            FadeTransition fade = new FadeTransition(Duration.millis(350), node);
            fade.setToValue(0);

            ScaleTransition scale = new ScaleTransition(Duration.millis(350), node);
            scale.setToX(1.3);
            scale.setToY(1.3);

            animations.add(new ParallelTransition(slide, fade, scale));
        }

        if (animations.isEmpty()) {
            onComplete.run();
            return;
        }

        ParallelTransition all = new ParallelTransition(animations.toArray(new Animation[0]));
        all.setOnFinished(e -> onComplete.run());
        all.play();
    }

    private void clearCardSelectionStyles() {
        upperRowBox.getChildren().forEach(n -> {
            n.setStyle("");
            n.setEffect(null);
        });
        lowerRowBox.getChildren().forEach(n -> {
            n.setStyle("");
            n.setEffect(null);
        });
    }

    // ── Error and event log ───────────────────────────────────────────────────

    /**
     * Displays an in-game error message.
     *
     * @param message the error text to show
     */
    public void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void hideError() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    /**
     * Appends an event resolution summary to the event log text area.
     *
     * @param eventCardID the resolved event card ID
     * @param eventType   the event type name
     * @param ppDelta     prestige point changes per player
     * @param foodDelta   food changes per player
     */
    public void showEvent(String eventCardID, String eventType,
                          Map<String, Integer> ppDelta,
                          Map<String, Integer> foodDelta) {
        StringBuilder sb = new StringBuilder();
        sb.append("▶ ").append(eventType).append(" (").append(eventCardID).append(")\n");
        for (String player : ppDelta.keySet()) {
            sb.append("  ").append(player)
                    .append("  PP ").append(formatDelta(ppDelta.getOrDefault(player, 0)))
                    .append("  Food ").append(formatDelta(foodDelta.getOrDefault(player, 0)))
                    .append("\n");
        }
        sb.append("\n");
        eventLogArea.appendText(sb.toString());
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    private boolean isMyTurn(ClientModel model) {
        return gui.getNickname() != null
                && gui.getNickname().equals(model.getCurrentPlayerNickname());
    }

    private void clearSelections() {
        selectedUpperCards.clear();
        selectedLowerCards.clear();
        selectedExtraCard = null;
    }

    /**
     * Groups a list of card IDs by their character type using {@link CardCatalog#getType(String)}.
     *
     * @param cardIDs list of card IDs to group
     * @return ordered map from type string to list of card IDs
     */
    private Map<String, List<String>> groupCardsByType(List<String> cardIDs) {
        Map<String, List<String>> map = new LinkedHashMap<>();
        if (cardIDs == null) return map;
        for (String id : cardIDs) {
            if (id == null) continue;
            String type = CardCatalog.getType(id);
            map.computeIfAbsent(type, k -> new ArrayList<>()).add(id);
        }
        return map;
    }

    /**
     * Returns a placeholder emoji for a character type.
     * Replace with an {@link ImageView} once type icon images are available in resources.
     *
     * @param type the character type string
     * @return emoji string representing the type
     */
    private String typeEmoji(String type) {
        return switch (type) {
            case "HUNTER"   -> "🏹";
            case "SHAMAN"   -> "⭐";
            case "BUILDER"  -> "🪵";
            case "INVENTOR" -> "⚙";
            case "ARTIST"   -> "🎨";
            case "GATHERER" -> "🌿";
            default         -> "?";
        };
    }

    private String getCardImagePath(String cardID) {
        if (cardID == null) return "";
        if (cardID.startsWith("CH")) return "/GUI-resources/characterCards/" + cardID + ".png";
        if (cardID.startsWith("BU")) return "/GUI-resources/buildingCards/" + cardID + ".png";
        if (cardID.startsWith("EV")) return "/GUI-resources/eventCards/" + cardID + ".png";
        return "";
    }

    private Image loadTotemImage(TotemColor color) {
        if (color == null) return null;
        return loadImage("/GUI-resources/totem/" + color.name().toLowerCase() + ".png");
    }

    private Image loadImage(String path) {
        try {
            var stream = getClass().getResourceAsStream(path);
            if (stream == null) return null;
            return new Image(stream);
        } catch (Exception e) {
            System.err.println("GUI image not found: " + path);
            return null;
        }
    }

    private FadeTransition makeFade(Node node, double from, double to, int millis) {
        FadeTransition ft = new FadeTransition(Duration.millis(millis), node);
        ft.setFromValue(from);
        ft.setToValue(to);
        return ft;
    }

    private ScaleTransition makeScale(Node node, double from, double to, int millis) {
        ScaleTransition st = new ScaleTransition(Duration.millis(millis), node);
        st.setFromX(from);
        st.setFromY(from);
        st.setToX(to);
        st.setToY(to);
        return st;
    }

    private String safeText(String text) {
        return text == null ? "-" : text;
    }

    private String formatDelta(int value) {
        return value >= 0 ? "+" + value : String.valueOf(value);
    }
}
