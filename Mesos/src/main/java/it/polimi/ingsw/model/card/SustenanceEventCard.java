package it.polimi.ingsw.model.card;
import it.polimi.ingsw.model.card.building.BuildingCard;
import it.polimi.ingsw.model.game.Era;
import it.polimi.ingsw.model.player.Tribe;
import it.polimi.ingsw.model.player.Player;
import java.util.List;

/**
 * Represents the Sustenance event.
 * During this event, each player must spend food to sustain
 * the Characters in their tribe.
 * The total cost is equal to the number of Characters owned,
 * reduced by discounts provided by Gatherers and Building cards.
 * If a player cannot pay the full cost, they lose prestige points
 * for each unpaid unit.
 *
 * @author Andrea Markvukaj
 */
public class SustenanceEventCard extends EventCard {

    private final int prestigePenalty;

    public SustenanceEventCard(Era era, String id, boolean isFinal, int prestigePenalty) {
        super(era, id, isFinal);
        this.prestigePenalty = prestigePenalty;
    }

    /**
     * Resolves the Sustenance event for all players.
     * Each player must pay food equal to the number of Characters in their tribe.
     * The cost is reduced by discounts from Gatherers and Building cards.
     * If the player cannot fully pay, they lose prestige points
     * for each unit of food they are unable to provide.
     *
     * @param players the list of players affected by the event
     */
    @Override
    public void resolveEvent(List<Player> players) {

        for (Player p : players) {

            Tribe tribe = p.getTribe();

            int totalCharacters = 0;
            for (List<CharacterCard> list : tribe.getMembers().values()) {
                totalCharacters += list.size();
            }

            int cost = totalCharacters;
            int discount = tribe.getCollectorDiscount();

            for (BuildingCard b : tribe.getBuildings()) {
                discount = discount + b.getSustenanceDiscount(p);
            }

            cost = Math.max(0, cost - discount);

            int availableFood = p.getFood();
            int foodToPay = Math.min(cost, availableFood);

            p.spendFood(foodToPay);

            int unpaid = cost - foodToPay;

            if (unpaid > 0) {
                p.losePP(unpaid * prestigePenalty);
            }
        }
    }

    /**
     * Adds this event to the sustenance events list during event resolution.
     * Sustenance events are resolved after all other events,
     * therefore they are collected in a separate list.
     *
     * @param normal the list of standard events (unused here)
     * @param sustenance the list of sustenance events
     */
    @Override
    public void addToList(List<EventCard> normal, List<EventCard> sustenance) {
        sustenance.add(this);
    }

}
