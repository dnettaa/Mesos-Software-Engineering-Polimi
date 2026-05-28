package it.polimi.ingsw.view.GUI;

import it.polimi.ingsw.leaderboard.MatchResult;
import it.polimi.ingsw.model.game.DTO.PlayerData;
import it.polimi.ingsw.model.player.TotemColor;
import it.polimi.ingsw.view.ClientModel;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Controller for the end-game screen.
 * Builds a Mesopotamian-themed podium with animated reveals for the top 3 players,
 * compact cards for 4th place onward, and a personalized result message.
 */
public class GUIEndGameController {

    @FXML private HBox podiumBox;
    @FXML private HBox othersBox;
    @FXML private Label localResultLabel;
    @FXML private Label globalLeaderboardSubtitle;
    @FXML private HBox globalLeaderboardBox;
    @FXML private VBox topLeaderboardBox;
    @FXML private VBox positionLeaderboardBox;
    @FXML private ScrollPane endGameScrollPane;

    private GUI gui;

    // Podium column display order: 2nd (left), 1st (center), 3rd (right)
    private static final int[]    DISPLAY_RANK_IDX = {1, 0, 2};
    private static final int[]    BLOCK_HEIGHTS    = {160, 240, 105};
    private static final String[] BLOCK_GRADIENTS  = {
        "linear-gradient(to bottom, #b8bac2, #62606e)",  // silver
        "linear-gradient(to bottom, #d4a820, #7a520a)",  // gold
        "linear-gradient(to bottom, #c87828, #6a3810)"   // bronze / terracotta
    };
    private static final String[] MEDALS  = {"II", "I", "III"};
    private static final DateTimeFormatter LEADERBOARD_DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String[] CROWNS  = {"🥈", "👑", "🥉"};

    public void setGUI(GUI gui) { this.gui = gui; }

    @FXML
    public void initialize() { /* data arrives via render() */ }

    /**
     * Builds the podium from the current ClientModel and schedules staggered animations.
     * Safe to call multiple times.
     */
    public void render() {
        if (gui == null || gui.getClientModel() == null) return;

        render(
                gui.getClientModel(),
                gui.getNickname(),
                gui.getGlobalLeaderboard(),
                gui.getGlobalLeaderboardPosition()
        );
    }

    /**
     * Builds the end-game screen from explicit data.
     * Used by the real GUI and by TestClassGUI without requiring a running match.
     */
    public void render(ClientModel model,
                       String myNick,
                       List<MatchResult> globalLeaderboard,
                       int globalPosition) {
        if (model == null) return;

        List<String>         ranking = safeList(model.getRanking());
        Map<String, Integer> finalPP = safeMap(model.getFinalPP());
        Map<String, Integer> bonus   = safeMap(model.getEndGameBonus());

        podiumBox.getChildren().clear();
        othersBox.getChildren().clear();

        // ── Podium columns (positions 1–3) ──────────────────────────────────────
        List<Node> podiumColumns = new ArrayList<>();

        for (int col = 0; col < 3; col++) {
            int rankIdx = DISPLAY_RANK_IDX[col];
            if (rankIdx >= ranking.size()) continue;

            String nick  = ranking.get(rankIdx);
            boolean isMe = nick.equals(myNick);

            VBox column = buildPodiumColumn(
                    nick,
                    finalPP.getOrDefault(nick, 0),
                    bonus.getOrDefault(nick, 0),
                    BLOCK_HEIGHTS[col],
                    BLOCK_GRADIENTS[col],
                    MEDALS[col],
                    CROWNS[col],
                    isMe,
                    model);

            column.setOpacity(0);
            podiumColumns.add(column);
            podiumBox.getChildren().add(column);
        }

        // ── 4th+ players ────────────────────────────────────────────────────────
        for (int i = 3; i < ranking.size(); i++) {
            String nick  = ranking.get(i);
            boolean isMe = nick.equals(myNick);
            othersBox.getChildren().add(
                    buildMiniCard(nick, finalPP.getOrDefault(nick, 0), i + 1, isMe, model));
        }

        // ── Personal result message ──────────────────────────────────────────────
        int myPos = ranking.indexOf(myNick) + 1;
        if (myPos > 0) {
            String tag = switch (myPos) {
                case 1 -> "👑  You are the champion of Mesopotamia!";
                case 2 -> "🥈  A worthy challenger — well played!";
                case 3 -> "🥉  An honorable showing, ancient one.";
                default -> "You placed #" + myPos + " — your story is not yet written.";
            };
            localResultLabel.setText(tag + "   " + finalPP.getOrDefault(myNick, 0) + " ⭐ Prestige Points");
        } else {
            localResultLabel.setText("");
        }

        renderGlobalLeaderboard(safeLeaderboard(globalLeaderboard), globalPosition, myNick);

        // ── Staggered reveal: 3rd → 2nd → 1st ──────────────────────────────────
        // displayOrder columns: [0]=2nd place, [1]=1st place, [2]=3rd place
        // Reveal order for drama: 3rd (idx 2), 2nd (idx 0), 1st (idx 1)
        List<int[]> revealPlan = switch (podiumColumns.size()) {
            case 1  -> List.of(new int[]{0, 500});
            case 2  -> List.of(new int[]{0, 400}, new int[]{1, 950});
            default -> List.of(new int[]{2, 400}, new int[]{0, 950}, new int[]{1, 1500});
        };

        for (int[] plan : revealPlan) {
            int colIdx  = plan[0];
            int delayMs = plan[1];
            if (colIdx >= podiumColumns.size()) continue;
            Node col = podiumColumns.get(colIdx);

            PauseTransition pause = new PauseTransition(Duration.millis(delayMs));
            pause.setOnFinished(ev -> {
                col.setTranslateY(55);
                FadeTransition ft = new FadeTransition(Duration.millis(550), col);
                ft.setToValue(1);
                TranslateTransition tt = new TranslateTransition(Duration.millis(550), col);
                tt.setToY(0);
                tt.setInterpolator(Interpolator.EASE_OUT);
                new ParallelTransition(ft, tt).play();
            });
            pause.play();
        }

        Platform.runLater(() -> {
            if (endGameScrollPane != null) {
                endGameScrollPane.setVvalue(0);
            }
        });
    }

    private void renderGlobalLeaderboard(List<MatchResult> leaderboard, int globalPosition, String myNick) {
        if (globalLeaderboardBox == null || globalLeaderboardSubtitle == null
                || topLeaderboardBox == null || positionLeaderboardBox == null) {
            return;
        }

        topLeaderboardBox.getChildren().clear();
        positionLeaderboardBox.getChildren().clear();

        if (leaderboard.isEmpty()) {
            globalLeaderboardSubtitle.setText("Global leaderboard is loading...");
            Label empty = new Label("No global leaderboard data received yet.");
            empty.setStyle("-fx-font-size: 12px; -fx-text-fill: #bda875; -fx-font-family: Georgia;");
            topLeaderboardBox.getChildren().add(empty);
            return;
        }

        int playerCount = leaderboard.get(0).playerCount();
        int localIndex = findLocalLeaderboardIndex(leaderboard, globalPosition, myNick);
        String positionText = localIndex >= 0
                ? "Your global position: #" + (localIndex + 1)
                : "Your global position is not available";
        globalLeaderboardSubtitle.setText("Global leaderboard for " + playerCount + "-player games - " + positionText);

        int topLimit = Math.min(10, leaderboard.size());
        topLeaderboardBox.getChildren().add(leaderboardHeader());
        for (int i = 0; i < topLimit; i++) {
            topLeaderboardBox.getChildren().add(leaderboardRow(leaderboard, i, localIndex));
        }

        if (localIndex >= topLimit) {
            positionLeaderboardBox.getChildren().add(leaderboardHeader());

            int start = Math.max(topLimit, localIndex - 2);
            int end = Math.min(leaderboard.size() - 1, localIndex + 2);

            if (start > topLimit) {
                positionLeaderboardBox.getChildren().add(ellipsisLabel());
            }

            for (int i = start; i <= end; i++) {
                positionLeaderboardBox.getChildren().add(leaderboardRow(leaderboard, i, localIndex));
            }

            if (end < leaderboard.size() - 1) {
                positionLeaderboardBox.getChildren().add(ellipsisLabel());
            }
        } else {
            Label message = new Label(localIndex >= 0
                    ? "You are already in the TOP 10."
                    : "Position is not available.");
            message.setWrapText(true);
            message.setMaxWidth(Double.MAX_VALUE);
            message.setAlignment(Pos.CENTER);
            message.setStyle("-fx-font-size: 13px; -fx-font-family: Georgia; -fx-text-fill: #bda875; -fx-padding: 28 10;");
            positionLeaderboardBox.getChildren().add(message);
        }
    }

    private int findLocalLeaderboardIndex(List<MatchResult> leaderboard, int globalPosition, String myNick) {
        if (globalPosition > 0 && globalPosition <= leaderboard.size()) {
            return globalPosition - 1;
        }

        if (myNick == null) {
            return -1;
        }

        for (int i = 0; i < leaderboard.size(); i++) {
            if (myNick.equals(leaderboard.get(i).nickname())) {
                return i;
            }
        }
        return -1;
    }

    private HBox leaderboardHeader() {
        HBox header = leaderboardRow("#", "Player", "Score", "Date", false);
        header.setStyle("-fx-background-color: rgba(200,134,10,0.22); -fx-background-radius: 6; -fx-padding: 5 9;");
        return header;
    }

    private HBox leaderboardRow(List<MatchResult> leaderboard, int index, int localIndex) {
        MatchResult result = leaderboard.get(index);
        return leaderboardRow(
                String.valueOf(index + 1),
                result.nickname(),
                String.valueOf(result.finalScore()),
                result.timestamp() == null ? "-" : result.timestamp().format(LEADERBOARD_DATE_FORMAT),
                index == localIndex
        );
    }

    private Label ellipsisLabel() {
        Label label = new Label("...");
        label.setMaxWidth(Double.MAX_VALUE);
        label.setAlignment(Pos.CENTER);
        label.setStyle("-fx-font-size: 13px; -fx-font-family: Georgia; -fx-text-fill: #bda875;");
        return label;
    }

    private HBox leaderboardRow(String rank, String player, String score, String date, boolean highlight) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setMinHeight(28);
        row.setMaxWidth(Double.MAX_VALUE);
        row.setStyle(
                "-fx-background-color: " + (highlight ? "rgba(200,134,10,0.34)" : "rgba(22,10,4,0.42)") + "; " +
                "-fx-background-radius: 6; " +
                "-fx-border-color: " + (highlight ? "#c8860a" : "rgba(180,130,60,0.18)") + "; " +
                "-fx-border-radius: 6; -fx-border-width: 1; -fx-padding: 4 9;"
        );

        row.getChildren().addAll(
                leaderboardCell(rank, 42, Pos.CENTER, true),
                leaderboardCell(player, 190, Pos.CENTER_LEFT, true),
                leaderboardCell(score, 70, Pos.CENTER_RIGHT, true),
                leaderboardCell(date, 170, Pos.CENTER_RIGHT, false)
        );
        return row;
    }

    private Label leaderboardCell(String text, double width, Pos alignment, boolean bold) {
        Label label = new Label(text == null ? "" : text);
        label.setMinWidth(width);
        label.setPrefWidth(width);
        label.setMaxWidth(width);
        label.setAlignment(alignment);
        label.setWrapText(false);
        label.setStyle(
                "-fx-font-size: 12px; " +
                "-fx-font-family: Georgia; " +
                "-fx-font-weight: " + (bold ? "bold" : "normal") + "; " +
                "-fx-text-fill: #fde8b0;"
        );
        return label;
    }

    // ── Podium column builder ─────────────────────────────────────────────────

    private VBox buildPodiumColumn(String nick, int pp, int bonusPP,
                                    int blockHeight, String gradient,
                                    String medal, String crown,
                                    boolean isMe, ClientModel model) {
        VBox column = new VBox(0);
        column.setAlignment(Pos.BOTTOM_CENTER);
        column.setPrefWidth(210);
        column.setMaxWidth(210);

        // ── Player info above the stone block ──
        VBox info = new VBox(5);
        info.setAlignment(Pos.CENTER);
        info.setPadding(new Insets(0, 8, 12, 8));

        // Crown / medal emoji
        Label crownLbl = new Label(crown);
        crownLbl.setStyle("-fx-font-size: " + (crown.equals("👑") ? "34" : "26") + "px;");
        if (crown.equals("👑")) crownLbl.setEffect(new Glow(0.95));

        // Totem image
        ImageView totemImg = new ImageView(loadTotemImage(nick, model));
        totemImg.setFitHeight(56);
        totemImg.setPreserveRatio(true);
        totemImg.setEffect(new DropShadow(10, Color.color(0, 0, 0, 0.7)));

        // Name
        Label nameLbl = new Label(nick);
        nameLbl.setStyle(
            "-fx-font-size: " + (isMe ? "17" : "14") + "px; " +
            "-fx-font-weight: bold; -fx-font-family: Georgia; " +
            "-fx-text-fill: " + (isMe ? "#fde8b0" : "#e8d4a8") + "; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.90), 6, 0, 1, 2);");
        nameLbl.setMaxWidth(190);
        nameLbl.setAlignment(Pos.CENTER);
        nameLbl.setWrapText(true);

        // Score
        Label scoreLbl = new Label("⭐  " + pp + " PP");
        scoreLbl.setStyle(
            "-fx-font-size: " + (crown.equals("👑") ? "20" : "15") + "px; " +
            "-fx-font-weight: bold; -fx-font-family: Georgia; " +
            "-fx-text-fill: #fde8b0; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.85), 5, 0, 1, 1);");

        info.getChildren().addAll(crownLbl, totemImg, nameLbl, scoreLbl);

        // Bonus chip (shown only if non-zero)
        if (bonusPP > 0) {
            Label bonusLbl = new Label("+" + bonusPP + " end bonus");
            bonusLbl.setStyle(
                "-fx-font-size: 10px; -fx-text-fill: #c8a050; " +
                "-fx-background-color: rgba(0,0,0,0.40); " +
                "-fx-background-radius: 8; -fx-padding: 2 8;");
            info.getChildren().add(bonusLbl);
        }

        // ── Stone block ──
        StackPane block = new StackPane();
        block.setPrefWidth(210);
        block.setPrefHeight(blockHeight);
        block.setMinHeight(blockHeight);
        block.setMaxHeight(blockHeight);
        block.setStyle(
            "-fx-background-color: " + gradient + "; " +
            "-fx-background-radius: 6 6 0 0; " +
            "-fx-border-color: rgba(255,255,255,0.22) rgba(0,0,0,0.50) transparent rgba(0,0,0,0.50); " +
            "-fx-border-width: 3 2 0 2; " +
            "-fx-border-radius: 6 6 0 0;");

        // Roman numeral watermark inside the block
        Label rankLbl = new Label(medal);
        rankLbl.setStyle(
            "-fx-font-size: 50px; -fx-font-weight: bold; -fx-font-family: Georgia; " +
            "-fx-text-fill: rgba(255,255,255,0.18);");
        block.getChildren().add(rankLbl);

        // Horizontal light stripe at the top of the block (ziggurate notch)
        Region stripe = new Region();
        stripe.setPrefHeight(9);
        stripe.setMaxWidth(Double.MAX_VALUE);
        stripe.setStyle("-fx-background-color: rgba(255,255,255,0.18);");
        StackPane.setAlignment(stripe, Pos.TOP_CENTER);
        block.getChildren().add(stripe);

        // Glow on block
        block.setEffect(crown.equals("👑")
                ? new DropShadow(26, Color.web("#d4a820", 0.75))
                : new DropShadow(12, Color.color(0, 0, 0, 0.55)));

        // Amber halo on local player's column
        if (isMe) {
            column.setStyle(
                "-fx-effect: dropshadow(gaussian, rgba(255,215,60,0.55), 24, 0, 0, 0);");
        }

        column.getChildren().addAll(info, block);
        return column;
    }

    // ── Mini card for 4th+ ────────────────────────────────────────────────────

    private VBox buildMiniCard(String nick, int pp, int position, boolean isMe, ClientModel model) {
        VBox card = new VBox(4);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(8, 14, 8, 14));
        card.setStyle(
            "-fx-background-color: " + (isMe ? "rgba(200,134,10,0.28)" : "rgba(22,10,4,0.65)") + "; " +
            "-fx-background-radius: 10; " +
            "-fx-border-color: " + (isMe ? "#c8860a" : "rgba(180,130,60,0.40)") + "; " +
            "-fx-border-radius: 10; -fx-border-width: 1.5;");

        ImageView totem = new ImageView(loadTotemImage(nick, model));
        totem.setFitHeight(28);
        totem.setPreserveRatio(true);

        Label posLbl = new Label("#" + position);
        posLbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #a08040; -fx-font-weight: bold;");

        Label nameLbl = new Label(nick);
        nameLbl.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #e8d4a8; -fx-font-family: Georgia;");

        Label ppLbl = new Label("⭐ " + pp);
        ppLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #c8a060;");

        card.getChildren().addAll(totem, posLbl, nameLbl, ppLbl);
        return card;
    }

    // ── Image loader ──────────────────────────────────────────────────────────

    private Image loadTotemImage(String nick, ClientModel model) {
        if (model.getPlayers() == null) return null;
        PlayerData pd = model.getPlayers().get(nick);
        if (pd == null || pd.totemColor() == null) return null;
        TotemColor color = pd.totemColor();
        try {
            var stream = getClass().getResourceAsStream(
                    "/GUI-resources/totem/" + color.name().toLowerCase() + ".png");
            return stream == null ? null : new Image(stream);
        } catch (Exception e) {
            return null;
        }
    }

    // ── Exit ──────────────────────────────────────────────────────────────────

    @FXML
    public void onExit() {
        try {
            if (gui != null && gui.getVirtualServer() != null)
                gui.getVirtualServer().disconnect();
        } catch (Exception ignored) {}
        Platform.exit();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private List<String> safeList(List<String> list) {
        return list == null ? new ArrayList<>() : list;
    }

    private List<MatchResult> safeLeaderboard(List<MatchResult> list) {
        return list == null ? List.of() : list;
    }

    private Map<String, Integer> safeMap(Map<String, Integer> map) {
        return map == null ? Map.of() : map;
    }
}
