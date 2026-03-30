package it.polimi.ingsw.model.card.building;

import it.polimi.ingsw.model.card.Card;
import it.polimi.ingsw.model.card.CharacterCard;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.card.Era;

public abstract class BuildingCard extends Card{

    protected int cost;
    protected int prestigePoints;

    public BuildingCard(Era era, String id, int cost, int prestigePoints)
    {
        super(era, id);
        this.cost = cost;
        this.prestigePoints = prestigePoints;
    }

    //Metodi trigger impostati di default a vuoto
    public void onCharacterAdded(CharacterCard card, Player player){}
    public void onTurnOrderPlaced(int slotIndex, Player player) {}

    public boolean requiresExtraCardPhase() {
        return false;
    }

    public int calculateEndGameBonus(Player player) {
        return 0;
    }

    public int getSustenanceDiscount(Player player) {
        return 0;
    }

    public int getBonusShamanIcons() {
        return 0;
    }

    public boolean avoidsMinorityPenalty() {
        return false;
    }

    public boolean doublesWinnerReward() {
        return false;
    }

    public int getHunterBonusFood() {
        return 0;
    }

    public int getHuntBonusPP() {
        return 0;
    }

    public int getPaintingsBonusFood() {
        return 0;
    }
}
