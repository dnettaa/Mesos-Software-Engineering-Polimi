package it.polimi.ingsw.view;

import com.google.gson.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class CardCatalog {

    private static final Map<String, String> descriptions = new HashMap<>();

    static {
        loadJson("/characters.json");
        loadJson("/events.json");
        loadJson("/buildings.json");
    }

    private static void loadJson(String path) {
        try (InputStream is = CardCatalog.class.getResourceAsStream(path)) {
            if (is == null) return;
            JsonArray arr = JsonParser.parseReader(new InputStreamReader(is)).getAsJsonArray();
            for (JsonElement el : arr) {
                JsonObject obj = el.getAsJsonObject();
                String id = obj.get("id").getAsString();
                String desc = buildDescription(obj);
                descriptions.put(id, desc);
            }
        } catch (Exception e) {
            System.err.println("[CardCatalog] Errore caricamento " + path + ": " + e.getMessage());
        }
    }

    private static String buildDescription(JsonObject obj) {
        String type = obj.has("type") ? obj.get("type").getAsString() : "?";
        String era = obj.has("era") ? obj.get("era").getAsString().replace("Era", "E") : "?";

        // Characters
        if (obj.has("hunterFoodBonus")) {
            boolean bonus = obj.get("hunterFoodBonus").getAsBoolean();
            return String.format("HUNTER %s%s", era, bonus ? " +food" : "");
        }
        if (obj.has("builderDiscount")) {
            int disc = obj.get("builderDiscount").getAsInt();
            int pp = obj.get("builderPrestige").getAsInt();
            return String.format("BUILDER %s -%dfood +%dPP", era, disc, pp);
        }
        if (obj.has("shamanSymbols")) {
            int sym = obj.get("shamanSymbols").getAsInt();
            return String.format("SHAMAN %s x%d", era, sym);
        }
        if (obj.has("inventionType")) {
            String inv = obj.get("inventionType").getAsString().replace("TYPE_", "T");
            return String.format("INVENTOR %s %s", era, inv);
        }
        if (type.equals("GATHERER")) return "GATHERER " + era;
        if (type.equals("ARTIST")) return "ARTIST " + era;

        // Events
        if (type.equals("HUNT")) {
            int pp = obj.get("prestigeReward").getAsInt();
            return String.format("HUNT %s +%dPP/hunter", era, pp);
        }
        if (type.equals("SUSTENANCE")) {
            int pen = obj.get("prestigePenalty").getAsInt();
            return String.format("SUST %s -%dPP/missing", era, pen);
        }
        if (type.equals("SHAMANIC_RITUAL")) {
            int rew = obj.get("majorityReward").getAsInt();
            int pen = obj.get("minorityPenalty").getAsInt();
            return String.format("SHAMAN_R %s +%d/-%d", era, rew, pen);
        }
        if (type.equals("CAVE_PAINTINGS")) {
            int req = obj.get("requiredArtists").getAsInt();
            int rew = obj.get("rewardPerArtist").getAsInt();
            return String.format("CAVE %s req%d +%d/art", era, req, rew);
        }

        // Buildings
        if (obj.has("cost")) {
            int cost = obj.get("cost").getAsInt();
            int pp = obj.get("prestigePoints").getAsInt();
            return String.format("BLD %s %s cost%d +%dPP", era, type.substring(0, Math.min(6, type.length())), cost, pp);
        }

        return type + " " + era;
    }

    public static String getDescription(String cardID) {
        return descriptions.getOrDefault(cardID, cardID);
    }
}