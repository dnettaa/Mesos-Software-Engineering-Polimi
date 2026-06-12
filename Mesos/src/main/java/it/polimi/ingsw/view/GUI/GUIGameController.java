package it.polimi.ingsw.view.GUI;

import it.polimi.ingsw.model.game.DTO.OfferSlotData;
import it.polimi.ingsw.model.game.DTO.PlayerData;
import it.polimi.ingsw.model.game.Era;
import it.polimi.ingsw.model.player.TotemColor;
import it.polimi.ingsw.view.CardCatalog;
import it.polimi.ingsw.view.ClientModel;
import javafx.animation.*;
import javafx.animation.Interpolator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
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
import javafx.scene.shape.Rectangle;
import javafx.stage.Popup;
import javafx.util.Duration;

import java.util.*;
import java.util.stream.Collectors;

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
    @FXML private StackPane scalablePane;
    @FXML private HBox upperRowBox;
    @FXML private HBox lowerRowBox;
    @FXML private HBox trackRowBox;

    // ── Top bar ───────────────────────────────────────────────────────────────
    @FXML private ImageView myTotemImage;

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

    /** Maps card ID → StackPane so pick animations can target it. Rebuilt every render(). */
    private final Map<String, StackPane> cardNodeMap = new HashMap<>();

    /** Maps nickname → totem ImageView node in the turn order display. Rebuilt every render(). */
    private final Map<String, ImageView> turnOrderTotemNodes = new HashMap<>();

    /** Maps occupant nickname → totem ImageView inside the offer slot. Rebuilt every render(). */
    private final Map<String, ImageView> offerSlotTotemByOccupant = new HashMap<>();

    /** Nicknames that had a totem on the offer track at the end of the last render(). */
    private final Set<String> prevOccupiedSlots = new HashSet<>();

    /** Round number from the last render — used to detect round transitions. */
    private int prevRound = 0;

    /** True while the end-of-round card animation is playing. */
    private boolean endRoundAnimating = false;

    /**
     * True between a Confirm click and the server's response.
     * While set, card clicks are ignored and the fly animation is deferred.
     */
    private boolean pendingConfirm = false;

    /** Cards submitted to the server but not yet confirmed: upper/lower row lists. */
    private final List<String> pendingConfirmUpper = new ArrayList<>();
    private final List<String> pendingConfirmLower = new ArrayList<>();

    /** Scene-coordinate bounds of each pending card, captured at confirm time. */
    private final Map<String, Bounds> pendingFlyBounds = new HashMap<>();

    /** Images of each pending card, captured at confirm time. */
    private final Map<String, Image> pendingFlyImages = new HashMap<>();

    /** Currently open player overlay (StackPane added to scalablePane), if any. */
    private StackPane playerOverlay;

    /** Currently open pick-confirmation popup, if any. */
    private Popup currentPickPopup;

    // ── Event animation queue ─────────────────────────────────────────────────
    private record PendingEvent(String cardID, String type,
                                Map<String, Integer> ppDelta, Map<String, Integer> foodDelta) {}
    private final Queue<PendingEvent> eventQueue = new LinkedList<>();
    private boolean eventAnimating = false;

    /**
     * Set by {@link #scheduleEndGame()} when the model has reached EndGame phase.
     * The final-scoring overlay is shown only after the event queue is fully drained.
     */
    private boolean endGamePending = false;

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
    }

    @FXML
    public void onOpenRules() {
        if (gui != null) {
            gui.openRulesPdf();
        }
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

        // Detect totems placed since the last render
        Set<String> currentOccupied = model.getOfferSlots().stream()
                .filter(s -> s.occupantNickname() != null)
                .map(OfferSlotData::occupantNickname)
                .collect(Collectors.toSet());

        Set<String> newlyPlaced = new HashSet<>(currentOccupied);
        newlyPlaced.removeAll(prevOccupiedSlots);

        // Capture source positions from the turn-order nodes BEFORE they are cleared
        Map<String, Bounds> sourceBoundsMap = new HashMap<>();
        for (String nick : newlyPlaced) {
            ImageView node = turnOrderTotemNodes.get(nick);
            if (node != null && node.getScene() != null)
                sourceBoundsMap.put(nick, node.localToScene(node.getBoundsInLocal()));
        }

        renderHeader(model);

        int currentRound = model.getCurrentRound();
        if (!endRoundAnimating && prevRound > 0 && currentRound > prevRound) {
            endRoundAnimating = true;
            prevRound = currentRound;
            playEndRoundTransition(model);
        } else if (!endRoundAnimating) {
            renderCardRows(model);
            prevRound = currentRound;

            // If we have a pending pick, check whether the server accepted it.
            // Acceptance is detected by the confirmed cards disappearing from both board rows.
            if (pendingConfirm) {
                List<String> allPending = new ArrayList<>(pendingConfirmUpper);
                allPending.addAll(pendingConfirmLower);
                boolean accepted = allPending.isEmpty() || allPending.stream().noneMatch(id ->
                        model.getUpperRowCardIDs().contains(id) || model.getLowerRowCardIDs().contains(id));
                if (accepted) {
                    pendingConfirm = false;
                    clearSelections();
                    clearCardSelectionStyles();
                    // Animate with the positions captured at confirm time
                    Map<String, Image>  imgs = new HashMap<>(pendingFlyImages);
                    Map<String, Bounds> bnds = new HashMap<>(pendingFlyBounds);
                    List<String>        ids  = new ArrayList<>(allPending);
                    pendingConfirmUpper.clear();
                    pendingConfirmLower.clear();
                    pendingFlyImages.clear();
                    pendingFlyBounds.clear();
                    if (!ids.isEmpty()) animateFlyToTribe(imgs, bnds, ids);
                }
            }
        }

        renderTurnOrder(model);   // clears trackRowBox and inserts turn-order tile first
        renderOfferTrack(model);  // appends offer tiles after it
        renderOtherPlayers(model);
        renderOwnStats(model);
        renderActionArea(model);

        // Hide newly placed totems NOW (same frame) to prevent a one-frame flash
        for (String nick : newlyPlaced) {
            ImageView t = offerSlotTotemByOccupant.get(nick);
            if (t != null) t.setOpacity(0);
        }

        // Fire flying animations once layout is settled (next pulse)
        if (!sourceBoundsMap.isEmpty()) {
            Platform.runLater(() -> {
                for (String nick : newlyPlaced) {
                    Bounds src = sourceBoundsMap.get(nick);
                    if (src == null) continue;
                    ImageView targetTotem = offerSlotTotemByOccupant.get(nick);
                    if (targetTotem == null || targetTotem.getScene() == null) continue;
                    animateTotemFly(src, targetTotem);
                }
            });
        }

        prevOccupiedSlots.clear();
        prevOccupiedSlots.addAll(currentOccupied);
    }

    // ── Render helpers ────────────────────────────────────────────────────────

    /**
     * Updates the top header labels (round, era, phase, active player, local nickname).
     */
    private void renderHeader(ClientModel model) {
        roundLabel.setText("ROUND " + model.getCurrentRound());
        eraLabel.setText("ERA: " + formatEra(model.getCurrentEra()));
        phaseLabel.setText(formatPhase(model.getCurrentPhaseName()));
        activePlayerLabel.setText("Turn: " + safeText(model.getCurrentPlayerNickname()));
        nicknameLabel.setText(safeText(gui.getNickname()));

        PlayerData me = model.getPlayers().get(gui.getNickname());
        if (me != null && myTotemImage != null)
            myTotemImage.setImage(loadTotemImage(me.totemColor()));
    }

    private String formatEra(Era era) {
        if (era == null) return "—";
        return era.name().replace("Era", "");
    }

    private String formatPhase(String phase) {
        if (phase == null || phase.isEmpty()) return "—";
        return phase.replace("Phase", "").replaceAll("([A-Z])", " $1").trim();
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
     * Three-phase animation played when the round counter increases:
     *  1. Lower row cards fade out and slide down (discarded).
     *  2. Upper row cards slide down to the lower row position and fade out.
     *  3. New upper row cards slide in from above (freshly dealt from the deck).
     * Phases are chained via onFinished callbacks so each phase starts only after the previous completes.
     */
    private void playEndRoundTransition(ClientModel newModel) {
        List<Node> oldLower = List.copyOf(lowerRowBox.getChildren());
        List<Node> oldUpper = List.copyOf(upperRowBox.getChildren());

        double slideDown = 220;
        if (!oldUpper.isEmpty() && upperRowBox.getScene() != null) {
            double upperY = upperRowBox.localToScene(upperRowBox.getBoundsInLocal()).getMinY();
            double lowerY = lowerRowBox.localToScene(lowerRowBox.getBoundsInLocal()).getMinY();
            slideDown = lowerY - upperY;
        }
        final double dy = slideDown;

        // Phase 3: rebuild rows then animate new upper cards in from above
        Runnable dealNewCards = () -> {
            renderCardRows(newModel);
            List<Node> newUpper = List.copyOf(upperRowBox.getChildren());
            for (Node n : newUpper) { n.setTranslateY(-150); n.setOpacity(0); }
            ParallelTransition phase3 = new ParallelTransition();
            for (Node n : newUpper) {
                TranslateTransition tt = new TranslateTransition(Duration.millis(400), n);
                tt.setToY(0);
                tt.setInterpolator(Interpolator.EASE_OUT);
                FadeTransition ft = new FadeTransition(Duration.millis(360), n);
                ft.setToValue(1);
                phase3.getChildren().add(new ParallelTransition(tt, ft));
            }
            phase3.setOnFinished(ev -> endRoundAnimating = false);
            if (newUpper.isEmpty()) endRoundAnimating = false;
            else phase3.play();
        };

        // Phase 2: upper row slides down to where lower row was and fades out
        Runnable runPhase2 = () -> {
            if (oldUpper.isEmpty()) { dealNewCards.run(); return; }
            ParallelTransition phase2 = new ParallelTransition();
            for (Node n : oldUpper) {
                TranslateTransition tt = new TranslateTransition(Duration.millis(500), n);
                tt.setByY(dy);
                tt.setInterpolator(Interpolator.EASE_BOTH);
                FadeTransition ft = new FadeTransition(Duration.millis(500), n);
                ft.setToValue(0);
                phase2.getChildren().add(new ParallelTransition(tt, ft));
            }
            phase2.setOnFinished(e -> dealNewCards.run());
            phase2.play();
        };

        // Phase 1: lower row cards fade out and drop, then trigger phase 2
        if (oldLower.isEmpty()) {
            runPhase2.run();
        } else {
            ParallelTransition phase1 = new ParallelTransition();
            for (Node n : oldLower) {
                FadeTransition ft = new FadeTransition(Duration.millis(260), n);
                ft.setToValue(0);
                TranslateTransition tt = new TranslateTransition(Duration.millis(260), n);
                tt.setByY(45);
                tt.setInterpolator(Interpolator.EASE_IN);
                phase1.getChildren().add(new ParallelTransition(ft, tt));
            }
            phase1.setOnFinished(e -> runPhase2.run());
            phase1.play();
        }
    }

    /**
     * Draws the offer track. Occupied slots show the occupant's totem.
     * Free slots pulse with a gold glow during TotemPlacementPhase to hint interactivity.
     * Clicking a free slot during TotemPlacementPhase sends placeTotem to the server.
     */
    private void renderOfferTrack(ClientModel model) {
        offerSlotTotemByOccupant.clear();
        // trackRowBox was already cleared by renderTurnOrder(); just append offer tiles here

        for (OfferSlotData slot : model.getOfferSlots()) {
            StackPane slotPane = new StackPane();

            ImageView tileImage = new ImageView(
                    loadImage("/GUI-resources/tiles/offer/offer_" + slot.slotID() + ".png"));
            tileImage.setFitHeight(165);
            tileImage.setPreserveRatio(true);
            slotPane.getChildren().add(tileImage);

            if (slot.occupantNickname() != null) {
                PlayerData occupant = model.getPlayers().get(slot.occupantNickname());
                if (occupant != null) {
                    ImageView totem = new ImageView(loadTotemImage(occupant.totemColor()));
                    totem.setFitHeight(40);
                    totem.setPreserveRatio(true);
                    StackPane.setAlignment(totem, Pos.TOP_CENTER);
                    StackPane.setMargin(totem, new Insets(8, 0, 0, 0));
                    slotPane.getChildren().add(totem);
                    offerSlotTotemByOccupant.put(slot.occupantNickname(), totem);
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

            // Tile hover: soft glow + gentle lift-scale (different feel from card zoom)
            ScaleTransition[] tileHoverAnim = {null};
            slotPane.setOnMouseEntered(e -> {
                if (tileHoverAnim[0] != null) tileHoverAnim[0].stop();
                slotPane.setEffect(new DropShadow(22, Color.web("#ffe580")));
                ScaleTransition st = new ScaleTransition(Duration.millis(200), slotPane);
                st.setToX(1.04);
                st.setToY(1.04);
                st.setInterpolator(Interpolator.EASE_OUT);
                tileHoverAnim[0] = st;
                st.play();
            });
            slotPane.setOnMouseExited(e -> {
                if (tileHoverAnim[0] != null) tileHoverAnim[0].stop();
                slotPane.setEffect(null);
                ScaleTransition st = new ScaleTransition(Duration.millis(200), slotPane);
                st.setToX(1.0);
                st.setToY(1.0);
                st.setInterpolator(Interpolator.EASE_OUT);
                tileHoverAnim[0] = st;
                st.play();
            });
            slotPane.setOnMouseClicked(e -> handleOfferSlotClick(slot));
            trackRowBox.getChildren().add(slotPane);
        }
    }

    /**
     * Draws the turn order track using the correct tile image for the current player count,
     * with totem icons overlaid in order.
     */
    private void renderTurnOrder(ClientModel model) {
        turnOrderTotemNodes.clear();
        trackRowBox.getChildren().clear();  // single clear point for the whole row

        int numPlayers = model.getPlayers().size();
        int clampedNum = Math.max(2, Math.min(5, numPlayers));

        Set<String> onOfferTrack = model.getOfferSlots().stream()
                .filter(s -> s.occupantNickname() != null)
                .map(OfferSlotData::occupantNickname)
                .collect(Collectors.toSet());

        StackPane trackPane = new StackPane();

        ImageView trackImage = new ImageView(
                loadImage("/GUI-resources/tiles/order/order_" + clampedNum + ".png"));
        trackImage.setFitHeight(165);
        trackImage.setPreserveRatio(true);
        trackPane.getChildren().add(trackImage);

        VBox totemColumn = new VBox(6);
        totemColumn.setAlignment(Pos.CENTER);
        StackPane.setMargin(totemColumn, new Insets(-25, 0, 0, 0));

        for (String playerName : model.getTurnOrder()) {
            if (onOfferTrack.contains(playerName)) continue;
            PlayerData player = model.getPlayers().get(playerName);
            if (player == null) continue;

            ImageView totem = new ImageView(loadTotemImage(player.totemColor()));
            totem.setFitHeight(22);
            totem.setPreserveRatio(true);
            turnOrderTotemNodes.put(playerName, totem);
            totemColumn.getChildren().add(totem);
        }

        trackPane.getChildren().add(totemColumn);
        trackRowBox.getChildren().add(trackPane);  // turn order tile is first child
    }

    /**
     * Renders the right panel showing the local player's food, PP and tribe stats.
     */
    private void renderOwnStats(ClientModel model) {
        if (gui.getNickname() == null) return;
        PlayerData me = model.getPlayers().get(gui.getNickname());
        if (me == null) return;

        setLabelIcon(myFoodLabel, "/GUI-resources/stats/food.png", 20);
        myFoodLabel.setText("  " + me.food());
        myFoodLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #5c3a00;");
        setLabelIcon(myPPLabel, "/GUI-resources/stats/points.png", 20);
        myPPLabel.setText("  " + me.prestigePoints());
        myPPLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #7a4a00;");

        myTribeBox.getChildren().clear();

        Map<String, List<String>> byType = groupCardsByType(me.tribeCardID());
        int buildings     = me.buildingID() != null ? me.buildingID().size() : 0;
        int builderDisc   = byType.getOrDefault("BUILDER", List.of()).stream()
                .mapToInt(CardCatalog::getBuilderDiscount).sum();
        int shamanStars   = byType.getOrDefault("SHAMAN", List.of()).stream()
                .mapToInt(CardCatalog::getShamanSymbols).sum();
        long distinctInvs = byType.getOrDefault("INVENTOR", List.of()).stream()
                .map(CardCatalog::getInventionType).filter(t -> !t.isEmpty()).distinct().count();

        // Section 1: all 6 character types, always shown (×0 if none)
        FlowPane typesPane = new FlowPane(10, 6);
        typesPane.setPadding(new Insets(0, 0, 4, 0));
        for (String type : CHAR_TYPES) {
            int count = byType.getOrDefault(type, List.of()).size();
            typesPane.getChildren().add(makeIconStat(typeIconPath(type), "×" + count));
        }
        myTribeBox.getChildren().add(typesPane);

        // Section 2: inventions, shaman stars, building discount — always shown
        Separator sep = new Separator();
        sep.setPadding(new Insets(4, 0, 4, 0));
        HBox derivedRow = new HBox(12);
        derivedRow.setAlignment(Pos.CENTER_LEFT);
        derivedRow.getChildren().add(makeIconStat("/GUI-resources/stats/inventions.png", String.valueOf(distinctInvs)));
        derivedRow.getChildren().add(makeIconStat("/GUI-resources/stats/shamanStars.png", String.valueOf(shamanStars)));
        derivedRow.getChildren().add(makeIconStat("/GUI-resources/stats/buildingSale.png", "-" + builderDisc));
        myTribeBox.getChildren().addAll(sep, derivedRow);

        // Per-type rows: icon + label on left, stacked card images on right
        for (String type : CHAR_TYPES) {
            List<String> cards = byType.getOrDefault(type, List.of());
            if (cards.isEmpty()) continue;

            HBox typeRow = new HBox(10);
            typeRow.setAlignment(Pos.TOP_LEFT);
            typeRow.setPadding(new Insets(2, 0, 2, 0));

            VBox labelCol = new VBox(2);
            labelCol.setAlignment(Pos.TOP_LEFT);
            labelCol.setMinWidth(68);
            labelCol.setMaxWidth(68);
            Image typeImg = loadImage(typeIconPath(type));
            if (typeImg != null) {
                ImageView iv = new ImageView(typeImg);
                iv.setFitWidth(20); iv.setFitHeight(20); iv.setPreserveRatio(true);
                labelCol.getChildren().add(iv);
            } else {
                Label iconLbl = new Label(typeEmoji(type));
                iconLbl.setStyle("-fx-font-size: 16px;");
                labelCol.getChildren().add(iconLbl);
            }
            Label nameLbl = new Label(capitalize(type) + " ×" + cards.size());
            nameLbl.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #3b1e00; -fx-wrap-text: true;");
            labelCol.getChildren().add(nameLbl);

            typeRow.getChildren().addAll(labelCol, createCardStack(cards, 46.0));
            myTribeBox.getChildren().add(typeRow);
        }

        // Buildings row
        if (buildings > 0) {
            Separator buildSep = new Separator();
            buildSep.setPadding(new Insets(4, 0, 4, 0));
            HBox buildRow = new HBox(10);
            buildRow.setAlignment(Pos.TOP_LEFT);
            buildRow.setPadding(new Insets(2, 0, 2, 0));
            VBox buildLbl = new VBox(2);
            buildLbl.setAlignment(Pos.TOP_LEFT);
            buildLbl.setMinWidth(68);
            buildLbl.setMaxWidth(68);
            Label buildIcon = new Label("🏛");
            buildIcon.setStyle("-fx-font-size: 16px;");
            Label buildName = new Label("Buildings ×" + buildings);
            buildName.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #3b1e00; -fx-wrap-text: true;");
            buildLbl.getChildren().addAll(buildIcon, buildName);
            buildRow.getChildren().addAll(buildLbl, createCardStack(me.buildingID(), 46.0));
            myTribeBox.getChildren().addAll(buildSep, buildRow);
        }
    }

    /**
     * Renders the list of other players with resource totals, tribe stat chips and a detail button.
     */
    private void renderOtherPlayers(ClientModel model) {
        playersBox.getChildren().clear();

        for (PlayerData player : model.getPlayers().values()) {
            if (player.nickname().equals(gui.getNickname())) continue;

            boolean isActive = player.nickname().equals(model.getCurrentPlayerNickname());
            Map<String, List<String>> byType = groupCardsByType(player.tribeCardID());
            int builderDisc   = byType.getOrDefault("BUILDER", List.of()).stream()
                    .mapToInt(CardCatalog::getBuilderDiscount).sum();
            int shamanStars   = byType.getOrDefault("SHAMAN", List.of()).stream()
                    .mapToInt(CardCatalog::getShamanSymbols).sum();
            long distinctInvs = byType.getOrDefault("INVENTOR", List.of()).stream()
                    .map(CardCatalog::getInventionType).filter(t -> !t.isEmpty()).distinct().count();

            VBox card = new VBox(5);
            card.setPadding(new Insets(8, 10, 8, 10));
            card.setStyle(
                "-fx-background-color: " + (isActive ? "rgba(255,230,140,0.92)" : "rgba(255,245,220,0.82)") + ";" +
                "-fx-background-radius: 10;" +
                "-fx-border-color: " + (isActive ? "#c8860a" : "#8b5c2a") + ";" +
                "-fx-border-radius: 10;" +
                "-fx-border-width: " + (isActive ? "2" : "1") + ";");

            // Name row: totem + name + active badge
            HBox nameRow = new HBox(7);
            nameRow.setAlignment(Pos.CENTER_LEFT);
            ImageView totem = new ImageView(loadTotemImage(player.totemColor()));
            totem.setFitHeight(20);
            totem.setPreserveRatio(true);
            Label nameLbl = new Label(player.nickname());
            nameLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + (isActive ? "#4a2a00" : "#3b1e00") + ";");
            nameRow.getChildren().addAll(totem, nameLbl);
            if (isActive) {
                Label activeBadge = new Label("◀");
                activeBadge.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #c8860a;");
                nameRow.getChildren().add(activeBadge);
            }

            // Resource row
            HBox resRow = new HBox(14);
            resRow.setAlignment(Pos.CENTER_LEFT);
            Label foodLbl = new Label("  " + player.food());
            foodLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #5c3a21;");
            setLabelIcon(foodLbl, "/GUI-resources/stats/food.png", 16);
            Label ppLbl = new Label("  " + player.prestigePoints());
            ppLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #5c3a21;");
            setLabelIcon(ppLbl, "/GUI-resources/stats/points.png", 16);
            resRow.getChildren().addAll(foodLbl, ppLbl);

            // Character type counts (all 6, always shown)
            FlowPane typesPane = new FlowPane(8, 4);
            typesPane.setPadding(new Insets(2, 0, 0, 0));
            for (String type : CHAR_TYPES) {
                int count = byType.getOrDefault(type, List.of()).size();
                typesPane.getChildren().add(makeIconStat(typeIconPath(type), "×" + count));
            }

            // Derived stats (always shown)
            HBox derivedRow = new HBox(10);
            derivedRow.setAlignment(Pos.CENTER_LEFT);
            derivedRow.getChildren().add(makeIconStat("/GUI-resources/stats/inventions.png", String.valueOf(distinctInvs)));
            derivedRow.getChildren().add(makeIconStat("/GUI-resources/stats/shamanStars.png", String.valueOf(shamanStars)));
            derivedRow.getChildren().add(makeIconStat("/GUI-resources/stats/buildingSale.png", "-" + builderDisc));

            card.getChildren().addAll(nameRow, resRow, typesPane, derivedRow);

            // Entire card is clickable; glow on hover to signal interactivity
            card.setCursor(javafx.scene.Cursor.HAND);
            String glowColor = isActive ? "#c8860a" : "#8b5c2a";
            card.setOnMouseEntered(e -> card.setEffect(new DropShadow(16, Color.web(glowColor))));
            card.setOnMouseExited(e -> card.setEffect(null));
            card.setOnMouseClicked(e -> showPlayerPopup(player, card));

            playersBox.getChildren().add(card);
        }
    }

    /**
     * Shows a centered overlay with a player's full tribe (stacked card images, 2-column layout).
     * Clicking the dark backdrop or the ✕ button closes it.
     */
    private void showPlayerPopup(PlayerData player, Node anchor) {
        if (playerOverlay != null) {
            scalablePane.getChildren().remove(playerOverlay);
            playerOverlay = null;
        }
        if (scalablePane == null) return;

        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.42);");
        overlay.prefWidthProperty().bind(scalablePane.widthProperty());
        overlay.prefHeightProperty().bind(scalablePane.heightProperty());
        overlay.setOpacity(0);
        playerOverlay = overlay;

        // ── Content card ──────────────────────────────────────────────────────
        VBox content = new VBox(10);
        content.setPadding(new Insets(18, 22, 18, 22));
        content.setMaxWidth(500);
        content.setMaxHeight(580);
        content.setStyle("""
                -fx-background-color: rgba(245,235,210,0.99);
                -fx-background-radius: 16;
                -fx-border-color: #5c3a21;
                -fx-border-width: 3;
                -fx-border-radius: 16;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.55), 28, 0, 0, 5);
                """);

        // Header
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        ImageView totem = new ImageView(loadTotemImage(player.totemColor()));
        totem.setFitHeight(36);
        totem.setPreserveRatio(true);
        Label title = new Label(player.nickname() + "'s Tribe");
        title.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #3b1e00;");
        Region hSpacer = new Region();
        HBox.setHgrow(hSpacer, Priority.ALWAYS);
        Button closeBtn = new Button("✕");
        closeBtn.setStyle("""
                -fx-background-color: rgba(122,74,26,0.15);
                -fx-text-fill: #5c3a00; -fx-font-weight: bold; -fx-font-size: 14px;
                -fx-cursor: hand; -fx-background-radius: 20; -fx-padding: 2 8;
                -fx-border-color: rgba(122,74,26,0.30); -fx-border-radius: 20; -fx-border-width: 1;
                """);
        header.getChildren().addAll(totem, title, hSpacer, closeBtn);
        content.getChildren().add(header);

        // Resources
        HBox resRow = new HBox(18);
        Label resFood = new Label("  " + player.food());
        resFood.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #5c3a00;");
        setLabelIcon(resFood, "/GUI-resources/stats/food.png", 18);
        Label resPP = new Label("  " + player.prestigePoints());
        resPP.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #7a4a00;");
        setLabelIcon(resPP, "/GUI-resources/stats/points.png", 18);
        resRow.getChildren().addAll(resFood, resPP);
        content.getChildren().add(resRow);

        Map<String, List<String>> byType = groupCardsByType(player.tribeCardID());
        int buildings     = player.buildingID() != null ? player.buildingID().size() : 0;
        int builderDisc   = byType.getOrDefault("BUILDER", List.of()).stream()
                .mapToInt(CardCatalog::getBuilderDiscount).sum();
        int shamanStars   = byType.getOrDefault("SHAMAN", List.of()).stream()
                .mapToInt(CardCatalog::getShamanSymbols).sum();
        long distinctInvs = byType.getOrDefault("INVENTOR", List.of()).stream()
                .map(CardCatalog::getInventionType).filter(t -> !t.isEmpty()).distinct().count();

        // Character type counts (all 6, always shown)
        FlowPane typesPane = new FlowPane(10, 6);
        typesPane.setPadding(new Insets(0, 0, 2, 0));
        for (String type : CHAR_TYPES) {
            int count = byType.getOrDefault(type, List.of()).size();
            typesPane.getChildren().add(makeIconStat(typeIconPath(type), "×" + count));
        }

        // Derived stats (always shown)
        HBox derivedRow = new HBox(12);
        derivedRow.setAlignment(Pos.CENTER_LEFT);
        derivedRow.getChildren().add(makeIconStat("/GUI-resources/stats/inventions.png", String.valueOf(distinctInvs)));
        derivedRow.getChildren().add(makeIconStat("/GUI-resources/stats/shamanStars.png", String.valueOf(shamanStars)));
        derivedRow.getChildren().add(makeIconStat("/GUI-resources/stats/buildingSale.png", "-" + builderDisc));
        content.getChildren().addAll(typesPane, derivedRow, new Separator());

        // 2-column grid of type sections, each with stacked card images
        GridPane typeGrid = new GridPane();
        typeGrid.setHgap(10);
        typeGrid.setVgap(10);
        ColumnConstraints tc1 = new ColumnConstraints();
        tc1.setPercentWidth(50);
        ColumnConstraints tc2 = new ColumnConstraints();
        tc2.setPercentWidth(50);
        typeGrid.getColumnConstraints().addAll(tc1, tc2);

        int col = 0, tRow = 0;
        for (String type : CHAR_TYPES) {
            List<String> cards = byType.getOrDefault(type, List.of());
            if (cards.isEmpty()) continue;
            typeGrid.add(makeTypeSection(type, cards, 58.0), col, tRow);
            col++;
            if (col == 2) { col = 0; tRow++; }
        }
        if (buildings > 0) {
            typeGrid.add(makeTypeSection("BUILDING", player.buildingID(), 58.0), col, tRow);
        }

        ScrollPane tribeScroll = new ScrollPane(typeGrid);
        tribeScroll.setFitToWidth(true);
        tribeScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        tribeScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        tribeScroll.getStyleClass().add("game-transparent-scroll");
        VBox.setVgrow(tribeScroll, Priority.ALWAYS);
        content.getChildren().add(tribeScroll);

        overlay.getChildren().add(content);
        scalablePane.getChildren().add(overlay);

        // Animate in
        FadeTransition overlayFade = new FadeTransition(Duration.millis(220), overlay);
        overlayFade.setToValue(1);
        overlayFade.play();
        content.setOpacity(0);
        content.setScaleX(0.80);
        content.setScaleY(0.80);
        new ParallelTransition(makeFade(content, 0, 1, 260), makeScale(content, 0.80, 1.0, 260)).play();

        Runnable doClose = () -> {
            FadeTransition ft = new FadeTransition(Duration.millis(180), overlay);
            ft.setToValue(0);
            ft.setOnFinished(e -> {
                scalablePane.getChildren().remove(overlay);
                if (playerOverlay == overlay) playerOverlay = null;
            });
            ft.play();
        };
        closeBtn.setOnAction(e -> doClose.run());
        overlay.setOnMouseClicked(e -> { if (e.getTarget() == overlay) doClose.run(); });
    }

    /** Creates a styled section box (used in the 2-column popup grid) for one card type. */
    private VBox makeTypeSection(String type, List<String> cards, double cardWidth) {
        VBox section = new VBox(6);
        section.setPadding(new Insets(8));
        section.setStyle("-fx-background-color: rgba(0,0,0,0.04); -fx-background-radius: 8;");
        HBox typeHeader = new HBox(5);
        typeHeader.setAlignment(Pos.CENTER_LEFT);
        String label = type.equals("BUILDING") ? "Buildings" : capitalize(type);
        String iconPath = typeIconPath(type);
        if (!iconPath.isEmpty()) {
            Image typeImg = loadImage(iconPath);
            if (typeImg != null) {
                ImageView iv = new ImageView(typeImg);
                iv.setFitWidth(16); iv.setFitHeight(16); iv.setPreserveRatio(true);
                typeHeader.getChildren().add(iv);
            }
        } else {
            Label icon = new Label("🏛");
            icon.setStyle("-fx-font-size: 14px;");
            typeHeader.getChildren().add(icon);
        }
        Label typeName = new Label(label + " ×" + cards.size());
        typeName.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #3b1e00;");
        typeHeader.getChildren().add(typeName);
        section.getChildren().addAll(typeHeader, createCardStack(cards, cardWidth));
        return section;
    }

    // ── Action area ───────────────────────────────────────────────────────────

    /**
     * Updates the action panel based on the current phase and whether it is the local player's turn.
     */
    private void renderActionArea(ClientModel model) {
        hideError();

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
                OfferSlotData mySlot = getMySlot(model);
                int upSel = mySlot != null ? mySlot.upSel() : 0;
                int downSel = mySlot != null ? mySlot.downSel() : 0;
                long availUpper = countAffordable(model, model.getUpperRowCardIDs());
                long availLower = countAffordable(model, model.getLowerRowCardIDs());
                int effectiveUp = (int) Math.min(upSel, availUpper);
                int effectiveDn = (int) Math.min(downSel, availLower);
                messageLabel.setText("Pick " + upSel + " from upper row, " + downSel + " from lower row.");
                if (effectiveUp == 0 && effectiveDn == 0 && currentPickPopup == null) {
                    Platform.runLater(() -> showPickConfirmPopup(List.of(), List.of()));
                }
            }
            case "ExtraCardPhase" -> {
                messageLabel.setText("Extra card: click any card from the rows.");
            }
            default -> {
                messageLabel.setText("Automatic phase. Waiting for the server...");
                clearSelections();
            }
        }
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
        // Pane is intentionally larger than the image so zoom never reaches the pane edge —
        // no clip needed, no overflow into adjacent tiles, no jitter.
        cardPane.setPrefSize(112, 162);

        ImageView imageView = new ImageView(loadImage(getCardImagePath(cardID)));
        imageView.setFitWidth(82);
        imageView.setPreserveRatio(true);
        cardPane.getChildren().add(imageView);

        if (imageView.getImage() == null || imageView.getImage().isError()) {
            Label fallback = new Label(cardID);
            fallback.setStyle("-fx-font-weight: bold; -fx-text-fill: #3b1e00; -fx-font-size: 10;");
            cardPane.getChildren().add(fallback);
        }

        Tooltip.install(cardPane, new Tooltip(cardID + "\n" + CardCatalog.getDescription(cardID)));

        ScaleTransition[] scaleAnim = {null};

        cardPane.setOnMouseEntered(e -> {
            if (scaleAnim[0] != null) scaleAnim[0].stop();
            ScaleTransition st = new ScaleTransition(Duration.millis(150), imageView);
            st.setToX(1.10);
            st.setToY(1.10);
            st.setInterpolator(Interpolator.EASE_OUT);
            scaleAnim[0] = st;
            st.play();
        });

        cardPane.setOnMouseExited(e -> {
            if (scaleAnim[0] != null) scaleAnim[0].stop();
            ScaleTransition st = new ScaleTransition(Duration.millis(150), imageView);
            st.setToX(1.0);
            st.setToY(1.0);
            st.setInterpolator(Interpolator.EASE_OUT);
            scaleAnim[0] = st;
            st.play();
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
     * Event cards (EV*) are never pickable. Row limits from the player's slot are enforced.
     * Opens a confirmation popup automatically when the selection is exactly complete.
     */
    private void handleCardClick(String cardID, boolean upperRow, StackPane cardPane) {
        if (pendingConfirm) return; // waiting for server — ignore clicks

        // Any card interaction dismisses the open popup so the user can modify selection
        if (currentPickPopup != null) { currentPickPopup.hide(); currentPickPopup = null; }

        ClientModel model = gui.getClientModel();
        if (model == null || !isMyTurn(model)) return;
        if (cardID != null && cardID.startsWith("EV")) return;

        String phase = model.getCurrentPhaseName();

        if ("OfferResolutionPhase".equals(phase)) {
            OfferSlotData mySlot = getMySlot(model);
            int upSel = mySlot != null ? mySlot.upSel() : 0;
            int downSel = mySlot != null ? mySlot.downSel() : 0;

            if (upperRow && upSel == 0) return;
            if (!upperRow && downSel == 0) return;
            // Block adding beyond the limit; deselecting is always allowed
            if (upperRow && !selectedUpperCards.contains(cardID) && selectedUpperCards.size() >= upSel) return;
            if (!upperRow && !selectedLowerCards.contains(cardID) && selectedLowerCards.size() >= downSel) return;

            toggleCardSelection(cardID, upperRow, cardPane);

            long availUpper = countAffordable(model, model.getUpperRowCardIDs());
            long availLower = countAffordable(model, model.getLowerRowCardIDs());
            int effectiveUp = (int) Math.min(upSel, availUpper);
            int effectiveDn = (int) Math.min(downSel, availLower);
            if (selectedUpperCards.size() == effectiveUp && selectedLowerCards.size() == effectiveDn) {
                showPickConfirmPopup(new ArrayList<>(selectedUpperCards), new ArrayList<>(selectedLowerCards));
            }
        } else if ("ExtraCardPhase".equals(phase)) {
            clearCardSelectionStyles();
            clearSelections();
            selectedExtraCard = cardID;
            animateSelect(cardPane);
            showPickConfirmPopup(List.of(cardID), List.of());
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

    // ── Pick confirmation popup ───────────────────────────────────────────────

    /**
     * Sends the confirmed card selection to the server.
     * The fly-to-tribe animation is deferred until render() confirms the server accepted
     * the pick (cards are gone from the board).  If the server rejects (e.g. not enough
     * food for a building), showError() will reopen the popup so the player can change
     * their selection — no misleading animation plays.
     */
    private void confirmPickAction(List<String> upper, List<String> lower, ClientModel model) {
        if (currentPickPopup != null) { currentPickPopup.hide(); currentPickPopup = null; }

        // ── Capture card positions and images NOW, while nodes are still in the scene ──
        List<String> all = new ArrayList<>(upper);
        all.addAll(lower);
        pendingFlyBounds.clear();
        pendingFlyImages.clear();
        for (String cardID : all) {
            StackPane node = cardNodeMap.get(cardID);
            if (node != null && node.getScene() != null) {
                pendingFlyBounds.put(cardID, node.localToScene(node.getBoundsInLocal()));
                for (Node child : node.getChildren()) {
                    if (child instanceof ImageView iv && iv.getImage() != null) {
                        pendingFlyImages.put(cardID, iv.getImage());
                        break;
                    }
                }
            }
        }
        pendingConfirmUpper.clear();
        pendingConfirmUpper.addAll(upper);
        pendingConfirmLower.clear();
        pendingConfirmLower.addAll(lower);
        pendingConfirm = true;
        // Selections and selection styles remain visible while we wait for the server.

        switch (model.getCurrentPhaseName()) {
            case "OfferResolutionPhase" -> {
                List<String> ordered = new ArrayList<>(upper);
                ordered.addAll(lower);
                gui.getVirtualServer().takeCards(gui.getNickname(), upper, lower, ordered);
            }
            case "ExtraCardPhase" -> {
                String extra = upper.isEmpty() ? lower.get(0) : upper.get(0);
                gui.getVirtualServer().takeExtraCard(gui.getNickname(), extra);
            }
        }
    }

    /**
     * Opens a summary popup showing the cards about to be picked, with Confirm and Back buttons.
     * Animate-in with a fade + scale. The Back button clears selection and closes the popup.
     */
    private void showPickConfirmPopup(List<String> upper, List<String> lower) {
        if (currentPickPopup != null) { currentPickPopup.hide(); currentPickPopup = null; }
        ClientModel model = gui.getClientModel();

        VBox content = new VBox(14);
        content.setPadding(new Insets(20, 24, 20, 24));
        content.setMaxWidth(520);
        content.setStyle("""
                -fx-background-color: rgba(245, 235, 210, 0.98);
                -fx-background-radius: 14;
                -fx-border-color: #7a4a1a;
                -fx-border-width: 3;
                -fx-border-radius: 14;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.60), 28, 0, 0, 6);
                """);

        Label title = new Label("Confirm Selection");
        title.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #2d1800;");
        content.getChildren().addAll(title, new Separator());

        List<String> all = new ArrayList<>(upper);
        all.addAll(lower);

        if (all.isEmpty()) {
            Label none = new Label("No cards to pick — food reward only.");
            none.setStyle("-fx-font-size: 13; -fx-text-fill: #5c3a21; -fx-wrap-text: true;");
            content.getChildren().add(none);
        } else {
            HBox cards = new HBox(16);
            cards.setAlignment(Pos.CENTER);
            cards.setPadding(new Insets(4, 0, 4, 0));
            for (String cardID : all) {
                VBox cardBox = new VBox(6);
                cardBox.setAlignment(Pos.CENTER);

                StackPane mini = new StackPane();
                mini.setPrefSize(68, 100);
                ImageView img = new ImageView(loadImage(getCardImagePath(cardID)));
                img.setFitWidth(64);
                img.setPreserveRatio(true);
                mini.getChildren().add(img);
                if (img.getImage() == null || img.getImage().isError()) {
                    Label fb = new Label(cardID);
                    fb.setStyle("-fx-font-size: 9; -fx-text-fill: #3b1e00;");
                    mini.getChildren().add(fb);
                }

                Label desc = new Label(CardCatalog.getDescription(cardID));
                desc.setStyle("-fx-font-size: 10; -fx-text-fill: #3b1e00; -fx-wrap-text: true;");
                desc.setMaxWidth(80);
                desc.setAlignment(Pos.CENTER);

                cardBox.getChildren().addAll(mini, desc);
                cards.getChildren().add(cardBox);
            }
            content.getChildren().add(cards);
        }

        content.getChildren().add(new Separator());

        HBox buttons = new HBox(16);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        Button backBtn = new Button("← Back");
        backBtn.setStyle("""
                -fx-background-color: linear-gradient(to bottom, #888880, #555550);
                -fx-text-fill: #f0f0e8; -fx-border-color: #333330;
                -fx-border-radius: 8; -fx-background-radius: 8; -fx-border-width: 1;
                -fx-font-size: 13px; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8 20;
                """);

        Button confirmBtn = new Button("Confirm ✓");
        confirmBtn.setStyle("""
                -fx-background-color: linear-gradient(to bottom, #4a8a3a, #2d5c22);
                -fx-text-fill: #f0f8e8; -fx-border-color: #1e3c14;
                -fx-border-radius: 8; -fx-background-radius: 8; -fx-border-width: 1;
                -fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8 20;
                """);

        buttons.getChildren().addAll(backBtn, confirmBtn);
        content.getChildren().add(buttons);

        Popup popup = new Popup();
        popup.getContent().add(content);
        popup.setAutoHide(false);
        currentPickPopup = popup;

        javafx.stage.Window window = scalablePane.getScene().getWindow();
        popup.show(window,
                window.getX() + window.getWidth()  / 2 - 250,
                window.getY() + window.getHeight() / 2 - 170);

        content.setOpacity(0);
        content.setScaleX(0.82);
        content.setScaleY(0.82);
        new ParallelTransition(makeFade(content, 0, 1, 200), makeScale(content, 0.82, 1.0, 200)).play();

        final List<String> finalUpper = List.copyOf(upper);
        final List<String> finalLower = List.copyOf(lower);

        backBtn.setOnAction(e -> {
            popup.hide();
            currentPickPopup = null;
            clearSelections();
            clearCardSelectionStyles();
        });
        confirmBtn.setOnAction(e -> confirmPickAction(finalUpper, finalLower, model));
    }

    /** Returns how many cards in the given row the local player can currently afford,
     *  accounting for builder discounts from cards already selected this turn. */
    private long countAffordable(ClientModel model, List<String> rowCardIDs) {
        String me = gui.getNickname();
        PlayerData myData = me != null ? model.getPlayers().get(me) : null;
        if (myData == null) return 0;
        int food = myData.food();
        int discount = myData.tribeCardID().stream().mapToInt(CardCatalog::getBuilderDiscount).sum()
                + selectedUpperCards.stream().mapToInt(CardCatalog::getBuilderDiscount).sum();
        return rowCardIDs.stream()
                .filter(id -> !id.startsWith("EV"))
                .filter(id -> Math.max(0, CardCatalog.getCost(id) - discount) <= food)
                .count();
    }

    /** Returns the offer slot currently occupied by the local player, or null if none. */
    private OfferSlotData getMySlot(ClientModel model) {
        String me = gui.getNickname();
        if (me == null) return null;
        return model.getOfferSlots().stream()
                .filter(s -> me.equals(s.occupantNickname()))
                .findFirst().orElse(null);
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

    /**
     * Animates card images flying from pre-captured scene positions to the tribe panel.
     * This is called after the server has confirmed the pick, using positions that were
     * stored at confirm time (the original card nodes may no longer exist in the scene).
     *
     * @param images    card image keyed by card ID
     * @param bounds    scene-coordinate bounds of each card at confirm time
     * @param orderedIds cards in the order they should fly (staggered by 80 ms each)
     */
    private void animateFlyToTribe(Map<String, Image> images, Map<String, Bounds> bounds,
                                    List<String> orderedIds) {
        if (scalablePane == null || rightPanel == null || rightPanel.getScene() == null) return;
        if (orderedIds.isEmpty()) return;

        Bounds tgtScene = rightPanel.localToScene(rightPanel.getBoundsInLocal());
        Bounds tgtLocal = scalablePane.sceneToLocal(tgtScene);
        double cx = scalablePane.getWidth() / 2;
        double cy = scalablePane.getHeight() / 2;
        double targetX = tgtLocal.getCenterX() - cx;
        double targetY = tgtLocal.getMinY() + tgtLocal.getHeight() * 0.25 - cy;

        int idx = 0;
        for (String cardID : orderedIds) {
            Image  cardImg  = images.get(cardID);
            Bounds srcScene = bounds.get(cardID);
            if (cardImg == null || srcScene == null) { idx++; continue; }

            Bounds srcLocal = scalablePane.sceneToLocal(srcScene);

            ImageView flying = new ImageView(cardImg);
            flying.setFitWidth(96);
            flying.setPreserveRatio(true);
            flying.setMouseTransparent(true);
            flying.setTranslateX(srcLocal.getCenterX() - cx);
            flying.setTranslateY(srcLocal.getCenterY() - cy);
            scalablePane.getChildren().add(flying);

            TranslateTransition move = new TranslateTransition(Duration.millis(430), flying);
            move.setByX(targetX - (srcLocal.getCenterX() - cx));
            move.setByY(targetY - (srcLocal.getCenterY() - cy));
            move.setInterpolator(Interpolator.EASE_BOTH);
            ScaleTransition shrink = new ScaleTransition(Duration.millis(430), flying);
            shrink.setToX(0.25);
            shrink.setToY(0.25);
            FadeTransition fade = new FadeTransition(Duration.millis(430), flying);
            fade.setToValue(0);

            final ImageView f = flying;
            Animation anim = new ParallelTransition(move, shrink, fade);
            anim.setOnFinished(e -> scalablePane.getChildren().remove(f));

            if (idx == 0) {
                anim.play();
            } else {
                SequentialTransition delayed = new SequentialTransition(
                        new PauseTransition(Duration.millis(idx * 80L)), anim);
                delayed.setOnFinished(e -> scalablePane.getChildren().remove(f));
                anim.setOnFinished(null);
                delayed.play();
            }
            idx++;
        }
    }

    private void animateTotemFly(Bounds srcBoundsInScene, ImageView targetTotem) {
        if (scalablePane == null || scalablePane.getScene() == null) return;
        if (targetTotem == null || targetTotem.getScene() == null) return;

        Bounds srcLocal = scalablePane.sceneToLocal(srcBoundsInScene);
        Bounds tgtLocal = scalablePane.sceneToLocal(
                targetTotem.localToScene(targetTotem.getBoundsInLocal()));

        ImageView flying = new ImageView(targetTotem.getImage());
        flying.setFitHeight(targetTotem.getFitHeight());
        flying.setPreserveRatio(true);
        flying.setMouseTransparent(true);

        // Place flying totem at source center (StackPane centers children; use translate offset)
        double cx = scalablePane.getWidth() / 2;
        double cy = scalablePane.getHeight() / 2;
        flying.setTranslateX(srcLocal.getCenterX() - cx);
        flying.setTranslateY(srcLocal.getCenterY() - cy);

        scalablePane.getChildren().add(flying);

        TranslateTransition move = new TranslateTransition(Duration.millis(450), flying);
        move.setByX(tgtLocal.getCenterX() - srcLocal.getCenterX());
        move.setByY(tgtLocal.getCenterY() - srcLocal.getCenterY());
        move.setInterpolator(Interpolator.EASE_BOTH);
        move.setOnFinished(e -> {
            scalablePane.getChildren().remove(flying);
            targetTotem.setOpacity(1);
        });
        move.play();
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
        errorLabel.setText("⚠  " + message);
        errorLabel.setVisible(true);

        // Server rejected the pending pick — cancel the deferred animation and reopen the
        // popup so the player can change their selection (e.g. pick a cheaper card).
        if (pendingConfirm) {
            pendingConfirm = false;
            pendingFlyBounds.clear();
            pendingFlyImages.clear();
            List<String> upper = new ArrayList<>(pendingConfirmUpper);
            List<String> lower = new ArrayList<>(pendingConfirmLower);
            pendingConfirmUpper.clear();
            pendingConfirmLower.clear();
            Platform.runLater(() -> showPickConfirmPopup(upper, lower));
        }
    }

    private void hideError() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
    }

    /**
     * Enqueues an event resolution. The animation and popup play sequentially —
     * the next event only starts after the user closes the current popup.
     */
    public void showEventResolved(String eventCardID, String eventType,
                          Map<String, Integer> ppDelta,
                          Map<String, Integer> foodDelta) {
        eventQueue.add(new PendingEvent(eventCardID, eventType,
                new HashMap<>(ppDelta), new HashMap<>(foodDelta)));
        if (!eventAnimating) drainEventQueue();
    }

    /**
     * Called by {@link GUI} when the model has entered EndGame phase.
     * If the event queue is already empty the final-scoring overlay is shown immediately;
     * otherwise it is deferred until the last popup is dismissed by the user.
     */
    public void scheduleEndGame() {
        endGamePending = true;
        if (!eventAnimating && eventQueue.isEmpty()) {
            showFinalScoringOverlay();
        }
    }

    /** Pulls the next pending event from the queue and plays its card-fly animation. */
    private void drainEventQueue() {
        if (eventQueue.isEmpty()) {
            eventAnimating = false;
            if (endGamePending) {
                endGamePending = false;
                showFinalScoringOverlay();
            }
            return;
        }
        eventAnimating = true;
        PendingEvent ev = eventQueue.poll();
        Image cardImg = loadImage(getCardImagePath(ev.cardID()));

        if (scalablePane == null || scalablePane.getScene() == null) {
            showEventResolvedOverlay(ev, cardImg);
            return;
        }

        double cx = scalablePane.getWidth() / 2;
        double cy = scalablePane.getHeight() / 2;

        ImageView flyingCard = new ImageView(cardImg);
        flyingCard.setFitWidth(82);
        flyingCard.setPreserveRatio(true);
        flyingCard.setMouseTransparent(true);

        StackPane sourcePane = cardNodeMap.get(ev.cardID());
        if (sourcePane != null && sourcePane.getScene() != null) {
            Bounds src = scalablePane.sceneToLocal(sourcePane.localToScene(sourcePane.getBoundsInLocal()));
            flyingCard.setTranslateX(src.getCenterX() - cx);
            flyingCard.setTranslateY(src.getCenterY() - cy);
        } else {
            flyingCard.setTranslateX(0);
            flyingCard.setTranslateY(cy * 0.6);
        }

        scalablePane.getChildren().add(flyingCard);

        double targetScale = 200.0 / 82.0;

        TranslateTransition move = new TranslateTransition(Duration.millis(580), flyingCard);
        move.setToX(0);
        move.setToY(-20);
        move.setInterpolator(Interpolator.EASE_BOTH);

        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(580), flyingCard);
        scaleUp.setToX(targetScale);
        scaleUp.setToY(targetScale);
        scaleUp.setInterpolator(Interpolator.EASE_BOTH);

        ParallelTransition flyIn = new ParallelTransition(move, scaleUp);
        flyIn.setOnFinished(e -> {
            PauseTransition pause = new PauseTransition(Duration.millis(160));
            pause.setOnFinished(pe -> {
                scalablePane.getChildren().remove(flyingCard);
                showEventResolvedOverlay(ev, cardImg);
            });
            pause.play();
        });
        flyIn.play();
    }

    private void showEventResolvedOverlay(PendingEvent ev, Image cardImg) {
        showEventResolvedOverlay(ev.cardID(), ev.type(), ev.ppDelta(), ev.foodDelta(), cardImg);
    }

    private void showEventResolvedOverlay(String eventCardID, String eventType,
                                           Map<String, Integer> ppDelta,
                                           Map<String, Integer> foodDelta,
                                           Image cardImg) {
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.55);");
        overlay.prefWidthProperty().bind(scalablePane.widthProperty());
        overlay.prefHeightProperty().bind(scalablePane.heightProperty());
        overlay.setOpacity(0);

        VBox content = new VBox(8);
        content.setPadding(new Insets(14, 18, 14, 18));
        content.setMaxWidth(360);
        content.setMinWidth(260);
        content.setStyle("""
                -fx-background-color: rgba(245,235,210,0.99);
                -fx-background-radius: 14;
                -fx-border-color: #7a4a1a;
                -fx-border-width: 3;
                -fx-border-radius: 14;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.65), 28, 0, 0, 5);
                """);

        // ── Header: card thumbnail + type label + close X ─────────────────────
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        if (cardImg != null) {
            ImageView thumb = new ImageView(cardImg);
            thumb.setFitHeight(48);
            thumb.setPreserveRatio(true);
            thumb.setEffect(new DropShadow(6, 1, 2, Color.color(0, 0, 0, 0.4)));
            header.getChildren().add(thumb);
        }

        VBox titleBox = new VBox(1);
        HBox.setHgrow(titleBox, Priority.ALWAYS);
        Label resolvedTag = new Label("EVENT RESOLVED");
        resolvedTag.setStyle("-fx-font-size: 9px; -fx-font-weight: bold; -fx-text-fill: #7a4a1a;");
        Label typeLbl = new Label(formatEventType(eventType));
        typeLbl.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2d1800;");
        Label descLbl = new Label(CardCatalog.getDescription(eventCardID));
        descLbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #7a5030;");
        descLbl.setWrapText(true);
        titleBox.getChildren().addAll(resolvedTag, typeLbl, descLbl);

        Button closeX = new Button("✕");
        closeX.setStyle("""
                -fx-background-color: rgba(122,74,26,0.15);
                -fx-text-fill: #5c3a00; -fx-font-weight: bold; -fx-font-size: 13px;
                -fx-cursor: hand; -fx-background-radius: 18; -fx-padding: 2 7;
                -fx-border-color: rgba(122,74,26,0.30); -fx-border-radius: 18; -fx-border-width: 1;
                """);
        header.getChildren().addAll(titleBox, closeX);
        content.getChildren().addAll(header, new Separator());

        String myNick = gui.getNickname();
        int myPP   = ppDelta.getOrDefault(myNick, 0);
        int myFood = foodDelta.getOrDefault(myNick, 0);

        // ── Local player — large highlighted block ────────────────────────────
        VBox mySection = new VBox(5);
        mySection.setPadding(new Insets(8, 12, 8, 12));
        mySection.setStyle("""
                -fx-background-color: rgba(160,100,20,0.12);
                -fx-background-radius: 10;
                -fx-border-color: rgba(160,100,20,0.35);
                -fx-border-width: 1.5;
                -fx-border-radius: 10;
                """);
        Label youLbl = new Label("YOU  —  " + safeText(myNick));
        youLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #5c3a00;");
        HBox myDeltas = new HBox(24);
        myDeltas.setAlignment(Pos.CENTER_LEFT);
        Label myFoodLbl = new Label("🍖  " + formatDelta(myFood));
        myFoodLbl.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: " + deltaColor(myFood) + ";");
        Label myPPLbl = new Label("⭐  " + formatDelta(myPP));
        myPPLbl.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: " + deltaColor(myPP) + ";");
        myDeltas.getChildren().addAll(myFoodLbl, myPPLbl);
        mySection.getChildren().addAll(youLbl, myDeltas);
        content.getChildren().add(mySection);

        // ── Other players — compact rows ──────────────────────────────────────
        List<String> others = ppDelta.keySet().stream()
                .filter(n -> !n.equals(myNick)).sorted().toList();
        if (!others.isEmpty()) {
            Label othersTitle = new Label("OTHER PLAYERS");
            othersTitle.setStyle("-fx-font-size: 9px; -fx-font-weight: bold; -fx-text-fill: #7a4a1a;");
            content.getChildren().add(othersTitle);

            for (String nick : others) {
                int pp   = ppDelta.getOrDefault(nick, 0);
                int food = foodDelta.getOrDefault(nick, 0);

                HBox row = new HBox(10);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(2, 6, 2, 6));

                Label nameLbl = new Label(nick);
                nameLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #3b1e00;");
                HBox.setHgrow(nameLbl, Priority.ALWAYS);

                Label foodLbl = new Label("🍖 " + formatDelta(food));
                foodLbl.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + deltaColor(food) + ";");
                Label ppLbl = new Label("⭐ " + formatDelta(pp));
                ppLbl.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + deltaColor(pp) + ";");

                row.getChildren().addAll(nameLbl, foodLbl, ppLbl);
                content.getChildren().add(row);
            }
        }

        // ── Continue button ───────────────────────────────────────────────────
        Separator sep = new Separator();
        sep.setPadding(new Insets(2, 0, 0, 0));
        Button continueBtn = new Button("Continue  →");
        continueBtn.setStyle("""
                -fx-background-color: linear-gradient(to bottom, #7a4a1a, #4a2a08);
                -fx-text-fill: #fde8b0; -fx-border-color: #2d1800;
                -fx-border-radius: 8; -fx-background-radius: 8; -fx-border-width: 1;
                -fx-font-size: 13px; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 7 24;
                """);
        HBox btnRow = new HBox();
        btnRow.setAlignment(Pos.CENTER);
        btnRow.getChildren().add(continueBtn);
        content.getChildren().addAll(sep, btnRow);

        overlay.getChildren().add(content);
        scalablePane.getChildren().add(overlay);

        FadeTransition overlayFade = new FadeTransition(Duration.millis(220), overlay);
        overlayFade.setToValue(1);
        overlayFade.play();
        content.setOpacity(0);
        content.setScaleX(0.86);
        content.setScaleY(0.86);
        new ParallelTransition(makeFade(content, 0, 1, 260), makeScale(content, 0.86, 1.0, 260)).play();

        Runnable doClose = () -> {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(180), overlay);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(fe -> {
                scalablePane.getChildren().remove(overlay);
                drainEventQueue();
            });
            fadeOut.play();
        };
        closeX.setOnAction(e -> doClose.run());
        continueBtn.setOnAction(e -> doClose.run());
    }

    /**
     * Shows a full-screen overlay with the final PP breakdown (base + end-game bonus → total)
     * after all event popups have been dismissed.  The "View Rankings" button navigates to
     * the EndGame screen.
     */
    private void showFinalScoringOverlay() {
        if (scalablePane == null) { gui.showEndGameScreen(); return; }

        ClientModel model = gui.getClientModel();
        List<String>         ranking = model.getRanking()      != null ? model.getRanking()      : List.of();
        Map<String, Integer> finalPP = model.getFinalPP()      != null ? model.getFinalPP()      : Map.of();
        Map<String, Integer> bonus   = model.getEndGameBonus() != null ? model.getEndGameBonus() : Map.of();
        String myNick = gui.getNickname();

        // ── Dark backdrop ─────────────────────────────────────────────────────
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.68);");
        overlay.prefWidthProperty().bind(scalablePane.widthProperty());
        overlay.prefHeightProperty().bind(scalablePane.heightProperty());
        overlay.setOpacity(0);

        // ── Content panel ─────────────────────────────────────────────────────
        VBox content = new VBox(10);
        content.setPadding(new Insets(24, 30, 24, 30));
        content.setMaxWidth(520);
        content.setStyle("""
                -fx-background-color: rgba(245,235,210,0.99);
                -fx-background-radius: 16;
                -fx-border-color: #7a4a1a;
                -fx-border-width: 3;
                -fx-border-radius: 16;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.70), 32, 0, 0, 6);
                """);

        // Header
        Label title = new Label("⭐  FINAL TALLY  ⭐");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-font-family: Georgia;" +
                       " -fx-text-fill: #2d1800;");
        title.setAlignment(Pos.CENTER);
        title.setMaxWidth(Double.MAX_VALUE);
        Label sub = new Label("End-game building bonuses have been applied.");
        sub.setStyle("-fx-font-size: 11px; -fx-font-style: italic; -fx-text-fill: #7a5030;");
        sub.setAlignment(Pos.CENTER);
        sub.setMaxWidth(Double.MAX_VALUE);
        content.getChildren().addAll(title, sub, new Separator());

        // Column header
        HBox hdr = new HBox(10);
        hdr.setAlignment(Pos.CENTER_LEFT);
        hdr.setPadding(new Insets(0, 6, 0, 6));
        String hs = "-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #7a4a1a;";
        Label hName  = new Label("PLAYER");   hName.setStyle(hs);  HBox.setHgrow(hName, Priority.ALWAYS);
        Label hBase  = new Label("BASE");     hBase.setStyle(hs);
        Label hPlus  = new Label("+");        hPlus.setStyle(hs);
        Label hBonus = new Label("BONUS");    hBonus.setStyle(hs);
        Label hEq    = new Label("=");        hEq.setStyle(hs);
        Label hTotal = new Label("TOTAL");    hTotal.setStyle(hs);
        hdr.getChildren().addAll(hName, hBase, hPlus, hBonus, hEq, hTotal);
        content.getChildren().add(hdr);
        content.getChildren().add(new Separator());

        // One row per player (ranking order = sorted by total descending)
        for (int i = 0; i < ranking.size(); i++) {
            String nick   = ranking.get(i);
            int total     = finalPP.getOrDefault(nick, 0);
            int bon       = bonus.getOrDefault(nick, 0);
            int base      = total - bon;
            boolean isMe  = nick.equals(myNick);

            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(5, 8, 5, 8));
            if (isMe) row.setStyle(
                "-fx-background-color: rgba(160,100,20,0.12); " +
                "-fx-background-radius: 8;");

            String medal = switch (i) {
                case 0 -> "👑 "; case 1 -> "🥈 "; case 2 -> "🥉 ";
                default -> "#" + (i + 1) + " ";
            };
            Label nameLbl  = new Label(medal + nick);
            nameLbl.setStyle("-fx-font-size: " + (isMe ? 14 : 13) + "px;" +
                             " -fx-font-weight: " + (isMe ? "bold" : "normal") + ";" +
                             " -fx-text-fill: #3b1e00;");
            HBox.setHgrow(nameLbl, Priority.ALWAYS);

            Label baseLbl  = new Label(String.valueOf(base));
            baseLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #5c3a21;");
            Label plusLbl  = new Label("+");
            plusLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #888;");
            Label bonLbl   = new Label(String.valueOf(bon));
            bonLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #c87828;");
            Label eqLbl    = new Label("=");
            eqLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #888;");
            Label totalLbl = new Label(total + " ⭐");
            totalLbl.setStyle("-fx-font-size: " + (isMe ? 16 : 13) + "px;" +
                              " -fx-font-weight: bold; -fx-text-fill: #2d5a1a;");

            row.getChildren().addAll(nameLbl, baseLbl, plusLbl, bonLbl, eqLbl, totalLbl);
            content.getChildren().add(row);
        }

        content.getChildren().add(new Separator());

        // Proceed button
        Button proceedBtn = new Button("View Rankings  →");
        proceedBtn.setStyle("""
                -fx-background-color: linear-gradient(to bottom, #7a4a1a, #4a2a08);
                -fx-text-fill: #fde8b0; -fx-font-weight: bold; -fx-font-size: 14px;
                -fx-border-color: #2d1800; -fx-border-radius: 8; -fx-background-radius: 8;
                -fx-border-width: 1; -fx-cursor: hand; -fx-padding: 9 28;
                -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 6, 0, 1, 2);
                """);
        HBox btnRow = new HBox();
        btnRow.setAlignment(Pos.CENTER_RIGHT);
        btnRow.getChildren().add(proceedBtn);
        content.getChildren().add(btnRow);

        overlay.getChildren().add(content);
        scalablePane.getChildren().add(overlay);

        // Animate in
        makeFade(overlay, 0, 1, 300).play();
        content.setOpacity(0); content.setScaleX(0.85); content.setScaleY(0.85);
        new ParallelTransition(makeFade(content, 0, 1, 380), makeScale(content, 0.85, 1.0, 380)).play();

        proceedBtn.setOnAction(e -> {
            FadeTransition ft = new FadeTransition(Duration.millis(220), overlay);
            ft.setToValue(0);
            ft.setOnFinished(ev -> {
                scalablePane.getChildren().remove(overlay);
                gui.showEndGameScreen();
            });
            ft.play();
        });
    }

    private String formatEventType(String type) {
        if (type == null) return "Event";
        return switch (type) {
            case "HUNT",            "HuntEventCard"           -> "Hunt";
            case "SUSTENANCE",      "SustenanceEventCard"     -> "Sustenance";
            case "SHAMANIC_RITUAL", "ShamanicRitualEventCard" -> "Shamanic Ritual";
            case "CAVE_PAINTINGS",  "CavePaintingsEventCard"  -> "Cave Paintings";
            default -> type.replace("EventCard", "").replaceAll("([A-Z])", " $1").trim();
        };
    }

    private String deltaColor(int delta) {
        if (delta > 0) return "#2a7a2a";
        if (delta < 0) return "#a80000";
        return "#5c5c5c";
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
            case "BUILDER"  -> "⚒";
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

    /**
     * Builds a StackPane where each card image is offset 12 px below the previous one,
     * creating a vertically fanned "deck" effect. The last card in the list sits on top.
     */
    private StackPane createCardStack(List<String> cardIDs, double cardWidth) {
        StackPane container = new StackPane();
        if (cardIDs == null || cardIDs.isEmpty()) return container;
        int count = cardIDs.size();
        double offset = 12.0;
        double cardHeight = cardWidth * 1.42;
        container.setPrefWidth(cardWidth + 2);
        container.setMinHeight(cardHeight + offset * (count - 1));
        container.setMaxWidth(cardWidth + 2);
        for (int i = 0; i < count; i++) {
            ImageView img = new ImageView(loadImage(getCardImagePath(cardIDs.get(i))));
            img.setFitWidth(cardWidth);
            img.setPreserveRatio(true);
            img.setEffect(new DropShadow(5, 1, 2, Color.color(0, 0, 0, 0.45)));
            StackPane.setAlignment(img, Pos.TOP_CENTER);
            StackPane.setMargin(img, new Insets(i * offset, 0, 0, 0));
            Tooltip.install(img, new Tooltip(cardIDs.get(i) + "\n" + CardCatalog.getDescription(cardIDs.get(i))));
            container.getChildren().add(img);
        }
        return container;
    }

    /** Returns the classpath path of the stats icon for the given character type. */
    private String typeIconPath(String type) {
        return switch (type) {
            case "HUNTER"   -> "/GUI-resources/stats/hunter.png";
            case "SHAMAN"   -> "/GUI-resources/stats/shaman.png";
            case "BUILDER"  -> "/GUI-resources/stats/builder.png";
            case "INVENTOR" -> "/GUI-resources/stats/inventor.png";
            case "ARTIST"   -> "/GUI-resources/stats/artist.png";
            case "GATHERER" -> "/GUI-resources/stats/collector.png";
            default         -> "";
        };
    }

    /** Creates a compact icon + number widget for the stats row. */
    private HBox makeIconStat(String iconPath, String text) {
        HBox box = new HBox(4);
        box.setAlignment(Pos.CENTER_LEFT);
        Image img = loadImage(iconPath);
        if (img != null) {
            ImageView iv = new ImageView(img);
            iv.setFitWidth(18); iv.setFitHeight(18); iv.setPreserveRatio(true);
            box.getChildren().add(iv);
        }
        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #3b1e00;");
        box.getChildren().add(lbl);
        return box;
    }

    /** Sets a loaded image as the graphic of a Label, silently skipped if the image is missing. */
    private void setLabelIcon(Label label, String iconPath, double size) {
        Image img = loadImage(iconPath);
        if (img == null) return;
        ImageView iv = new ImageView(img);
        iv.setFitWidth(size); iv.setFitHeight(size); iv.setPreserveRatio(true);
        label.setGraphic(iv);
    }

    /** Creates a compact chip label for tribe stat summaries. */
    private Label makeStatChip(String text) {
        Label l = new Label(text);
        l.setStyle("""
                -fx-background-color: rgba(122,74,26,0.15);
                -fx-background-radius: 10;
                -fx-border-color: rgba(122,74,26,0.35);
                -fx-border-radius: 10;
                -fx-border-width: 1;
                -fx-font-size: 11px;
                -fx-font-weight: bold;
                -fx-text-fill: #5c3a00;
                -fx-padding: 2 7;
                """);
        return l;
    }

    /**
     * Returns the number of complete sets (one of each of the 6 character types)
     * in the given type→cards map. Returns 0 if any type is missing.
     */
    private int computeFullSets(Map<String, List<String>> byType) {
        int min = Integer.MAX_VALUE;
        for (String type : CHAR_TYPES) {
            int count = byType.getOrDefault(type, List.of()).size();
            if (count == 0) return 0;
            min = Math.min(min, count);
        }
        return min == Integer.MAX_VALUE ? 0 : min;
    }

    /** Capitalizes the first letter and lowercases the rest (e.g. "HUNTER" → "Hunter"). */
    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.charAt(0) + s.substring(1).toLowerCase();
    }
}
