package it.polimi.ingsw.view;

import com.google.gson.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class CardCatalog {

    private static final Map<String, String> descriptions = new HashMap<>();

    static {
        loadJson("/JSON/characters.json");
        loadJson("/JSON/events.json");
        loadJson("/JSON/buildings.json");
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

        // ── Characters ──────────────────────────────────────────
        if (obj.has("hunterFoodBonus")) {
            boolean bonus = obj.get("hunterFoodBonus").getAsBoolean();
            return bonus ? "Hunter " + era + " +🍖" : "Hunter " + era;
        }
        if (obj.has("builderDiscount")) {
            int disc = obj.get("builderDiscount").getAsInt();
            int pp   = obj.get("builderPrestige").getAsInt();
            return "Builder " + era + " -" + disc + "🍖+" + pp + "⭐";
        }
        if (obj.has("shamanSymbols")) {
            int sym = obj.get("shamanSymbols").getAsInt();
            return "Shaman " + era + " x" + sym + "🔮";
        }
        if (obj.has("inventionType")) {
            String inv = obj.get("inventionType").getAsString().replace("TYPE_", "");
            return "Inventor " + era + " #" + inv;
        }
        if (type.equals("GATHERER")) return "Gatherer " + era;
        if (type.equals("ARTIST"))   return "Artist " + era;

        // ── Events ──────────────────────────────────────────────
        if (type.equals("HUNT")) {
            int pp = obj.get("prestigeReward").getAsInt();
            return "Hunt +" + pp + "⭐/hunter";
        }
        if (type.equals("SUSTENANCE")) {
            int pen = obj.get("prestigePenalty").getAsInt();
            return "Sustenance -" + pen + "⭐";
        }
        if (type.equals("SHAMANIC_RITUAL")) {
            int rew = obj.get("majorityReward").getAsInt();
            int pen = obj.get("minorityPenalty").getAsInt();
            return "Ritual +" + rew + "/-" + pen + "⭐";
        }
        if (type.equals("CAVE_PAINTINGS")) {
            int req = obj.get("requiredArtists").getAsInt();
            int rew = obj.get("rewardPerArtist").getAsInt();
            return "Cave " + req + "art +" + rew + "⭐";
        }

        // ── Buildings ───────────────────────────────────────────
        if (obj.has("cost")) {
            int cost = obj.get("cost").getAsInt();
            int pp   = obj.get("prestigePoints").getAsInt();
            String shortType = switch (type) {
                case "TRIGGER_SET"        -> "TrigSet";
                case "TRIGGER_INVENTOR"   -> "TrigInv";
                case "SUSTENANCE_DISCOUNT"-> "SustDisc";
                case "SHAMAN_NO_PENALTY"  -> "ShamNoPen";
                case "SHAMAN_DOUBLE_REWARD"-> "ShamDbl";
                case "SHAMAN_BONUS_ICONS" -> "ShamIcon";
                case "HUNT_BONUS"         -> "HuntBonus";
                case "PAINTINGS_FOOD"     -> "PaintFood";
                case "TURN_ORDER_BONUS"   -> "TurnBonus";
                case "END_DOUBLE_BUILDER" -> "DblBuilder";
                case "END_SIX_SET"        -> "SixSet";
                case "END_PER_TYPE"       -> {
                    String t = obj.has("targetType") ? obj.get("targetType").getAsString() : "";
                    int ppp  = obj.has("ppPerCard")  ? obj.get("ppPerCard").getAsInt() : 0;
                    yield "End/" + t.charAt(0) + t.substring(1,3).toLowerCase() + " +" + ppp + "⭐";
                }
                case "EXTRA_PICK"         -> "ExtraPick";
                case "END_BONUS"          -> {
                    int bonus = obj.has("prestigeBonus") ? obj.get("prestigeBonus").getAsInt() : 0;
                    yield "EndBonus +" + bonus + "⭐";
                }
                default -> type.substring(0, Math.min(8, type.length()));
            };
            return shortType + " $" + cost + " +" + pp + "⭐";
        }

        return type + " " + era;
    }

    public static String getDescription(String cardID) {
        return descriptions.getOrDefault(cardID, cardID);
    }
}