package it.polimi.ingsw.model.card;
import it.polimi.ingsw.model.game.Era;
import it.polimi.ingsw.model.player.Tribe;
import it.polimi.ingsw.model.player.Player;
import java.util.List;

public class SustenanceEventCard extends EventCard {

    private final int prestigePenalty;

    public SustenanceEventCard(Era era, String id, boolean isFinal, int prestigePenalty) {
        super(era, id, isFinal);
        this.prestigePenalty = prestigePenalty;
    }

    @Override
    public void resolveEvent(List<Player> players) {

        for (Player p : players) {

            Tribe tribe = p.getTribe();

            //conta tutti i personaggi
            int totalCharacters = 0;
            for (List<CharacterCard> list : tribe.getMembers().values()) {
                totalCharacters += list.size();
            }

            //costo base
            int cost = totalCharacters;

            //sconto dei raccoglitori
            int discount = tribe.getCollectorDiscount();
            cost = Math.max(0, cost - discount);

            //paga il possibile
            int availableFood = p.getFood();
            int foodToPay = Math.min(cost, availableFood);
            p.spendFood(foodToPay);

            //calcola penalita per personaggi non sfamati
            int unpaid = cost - foodToPay;
            if (unpaid > 0) {
                p.losePP(unpaid * prestigePenalty);
            }
        }
    }

}
