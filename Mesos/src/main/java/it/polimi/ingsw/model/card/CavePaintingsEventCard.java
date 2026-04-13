package it.polimi.ingsw.model.card;
import it.polimi.ingsw.model.card.building.BuildingCard;
import it.polimi.ingsw.model.game.Era;
import it.polimi.ingsw.model.player.Player;
import java.util.List;

public class CavePaintingsEventCard extends EventCard {

    private final int requiredArtists;
    private final int rewardPerArtist;
    private final int prestigePenalty;

    public CavePaintingsEventCard(Era era, String id, boolean isFinal, int requiredArtists, int prestigePenalty, int rewardPerArtist) {

        super(era, id, isFinal);
        this.requiredArtists = requiredArtists;
        this.prestigePenalty = prestigePenalty;
        this.rewardPerArtist = rewardPerArtist;
    }

    @Override
    public void resolveEvent(List<Player> players) {

        for (Player p : players) {

            int artists = p.getTribe().countByType(CharacterType.ARTIST);
            // bonus food dai building, se ho la building card ho 1 cibo bonus per ogni artista (ma va verificato se ho il building o meno)
            int bonusFood = 0;
            for (BuildingCard b : p.getTribe().getBuildings()) {
                bonusFood += artists * b.getPaintingsBonusFood();
            }
            // logica evento
            if (artists < requiredArtists) {
                p.losePP(prestigePenalty);
            } else {
                p.addPP(artists * rewardPerArtist);
            }
            // applico il foodbonus se > 0
            if (bonusFood > 0) {
                p.addFood(bonusFood);
            }
        }
    }
}
