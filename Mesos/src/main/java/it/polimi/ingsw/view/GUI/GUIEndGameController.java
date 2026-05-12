package it.polimi.ingsw.view.GUI;

import it.polimi.ingsw.view.ClientModel;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * JavaFX controller for the final game screen.
 *
 * This screen is read-only: it does not change the model and it does not call
 * game actions on the server. It only presents the final ranking computed by
 * the authoritative server-side model.
 */
public class GUIEndGameController {

    @FXML private Label winnerLabel;
    @FXML private Label localResultLabel;

    @FXML private TableView<RankingRow> rankingTable;
    @FXML private TableColumn<RankingRow, Number> positionColumn;
    @FXML private TableColumn<RankingRow, String> playerColumn;
    @FXML private TableColumn<RankingRow, Number> basePointsColumn;
    @FXML private TableColumn<RankingRow, Number> bonusColumn;

    private GUI gui;

    /**
     * Connects this controller to the main GUI object.
     *
     * @param gui main GUI instance
     */
    public void setGUI(GUI gui) {
        this.gui = gui;
    }

    /**
     * Called automatically by JavaFX after the FXML has been loaded.
     * Here we configure the ranking table columns once.
     */
    @FXML
    public void initialize() {
        positionColumn.setCellValueFactory(row ->
                new SimpleIntegerProperty(row.getValue().position()));

        playerColumn.setCellValueFactory(row ->
                new SimpleStringProperty(row.getValue().playerNickname()));

        basePointsColumn.setCellValueFactory(row ->
                new SimpleIntegerProperty(row.getValue().finalPoints()));

        bonusColumn.setCellValueFactory(row ->
                new SimpleIntegerProperty(row.getValue().endGameBonus()));
    }

    /**
     * Redraws the final screen using the current ClientModel.
     * It is safe to call this method multiple times.
     */
    public void render() {
        if (gui == null || gui.getClientModel() == null) {
            return;
        }

        ClientModel model = gui.getClientModel();

        List<String> ranking = safeList(model.getRanking());
        Map<String, Integer> finalPP = safeMap(model.getFinalPP());
        Map<String, Integer> bonus = safeMap(model.getEndGameBonus());

        rankingTable.getItems().clear();

        for (int i = 0; i < ranking.size(); i++) {
            String player = ranking.get(i);

            rankingTable.getItems().add(new RankingRow(
                    i + 1,
                    player,
                    finalPP.getOrDefault(player, 0),
                    bonus.getOrDefault(player, 0)
            ));
        }

        renderWinner(ranking);
        renderLocalResult(ranking, finalPP);
    }

    /**
     * Shows the winner at the top of the screen.
     */
    private void renderWinner(List<String> ranking) {
        if (ranking.isEmpty()) {
            winnerLabel.setText("Winner: -");
            return;
        }

        winnerLabel.setText("Winner: " + ranking.getFirst());
    }

    /**
     * Shows a small personalized summary for the local player.
     */
    private void renderLocalResult(List<String> ranking, Map<String, Integer> finalPP) {
        String nickname = gui.getNickname();

        if (nickname == null || ranking.isEmpty()) {
            localResultLabel.setText("");
            return;
        }

        int position = ranking.indexOf(nickname) + 1;
        int points = finalPP.getOrDefault(nickname, 0);

        if (position <= 0) {
            localResultLabel.setText("Your final score: " + points + " PP");
            return;
        }

        localResultLabel.setText("You placed #" + position + " with " + points + " Prestige Points.");
    }

    /**
     * Exits the application.
     * We also try to notify the server, but the screen remains safe even if the server is already closed.
     */
    @FXML
    public void onExit() {
        try {
            if (gui != null && gui.getVirtualServer() != null) {
                gui.getVirtualServer().disconnect();
            }
        } catch (Exception ignored) {
            /*
             * The match is already over, so a failed disconnect message should not
             * block the client shutdown.
             */
        }

        Platform.exit();
    }

    /**
     * Converts a possibly-null list into a safe list.
     */
    private List<String> safeList(List<String> list) {
        return list == null ? new ArrayList<>() : list;
    }

    /**
     * Converts a possibly-null map into a safe map.
     */
    private Map<String, Integer> safeMap(Map<String, Integer> map) {
        return map == null ? Map.of() : map;
    }

    /**
     * Immutable row used by the JavaFX ranking table.
     *
     * @param position       ranking position
     * @param playerNickname player nickname
     * @param finalPoints    total final prestige points
     * @param endGameBonus   prestige points gained during final scoring
     */
    private record RankingRow(
            int position,
            String playerNickname,
            int finalPoints,
            int endGameBonus
    ) {
    }
}