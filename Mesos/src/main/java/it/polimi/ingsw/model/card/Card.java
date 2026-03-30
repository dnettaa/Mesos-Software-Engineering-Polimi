package it.polimi.ingsw.model.card;

public abstract class Card {

    protected Era era;
    protected String id;

    public Card(Era era, String id) {
        this.era = era;
        this.id = id;
    }

    public Era getEra() {
        return era;
    }

    public String getId() {
        return id;
    }
}
