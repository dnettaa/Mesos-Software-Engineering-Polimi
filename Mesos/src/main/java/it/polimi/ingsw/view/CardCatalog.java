package it.polimi.ingsw.view;

import com.google.gson.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class responsible for loading and managing human-readable descriptions
 * for all the cards in the game (Characters, Events, and Buildings).
 * <p>
 * It reads card data from internal JSON resources upon initialization and caches
 * the formatted descriptions in memory for fast retrieval by the UI.
 * </p>
 */
public class CardCatalog {

    /** Cache storing the mapping between card IDs and their formatted descriptions. */
    private static final Map<String, String> descriptions = new HashMap<>();

    /** Maps card ID → raw type string as found in the JSON (e.g. "HUNTER", "BUILDER"). */
    private static final Map<String, String> types = new HashMap<>();

    /** Maps builder card ID → food discount value (0 for non-builder cards). */
    private static final Map<String, Integer> builderDiscounts = new HashMap<>();

    // Static block to preload all card definitions as soon as the class is loaded into memory.
    static {
        loadJson("/JSON/characters.json");
        loadJson("/JSON/events.json");
        loadJson("/JSON/buildings.json");
    }

    /**
     * Parses a JSON file from the specified classpath resource path and extracts
     * card IDs and their corresponding descriptions.
     *
     * @param path The absolute path to the JSON resource file (e.g., "/JSON/events.json").
     */
    private static void loadJson(String path) {
        try (InputStream is = CardCatalog.class.getResourceAsStream(path)) {
            if (is == null) return;
            JsonArray arr = JsonParser.parseReader(new InputStreamReader(is)).getAsJsonArray();
            for (JsonElement el : arr) {
                JsonObject obj = el.getAsJsonObject();
                String id = obj.get("id").getAsString();
                String desc = buildDescription(obj);
                descriptions.put(id, desc);
                if(obj.has("type")){
                    types.put(id, obj.get("type").getAsString());
                }
                if (obj.has("builderDiscount")) {
                    builderDiscounts.put(id, obj.get("builderDiscount").getAsInt());
                }
            }
        } catch (Exception e) {
            System.err.println("[CardCatalog] Failed to load " + path + ": " + e.getMessage());
        }
    }

    /**
     * Inspects the JSON object representing a card and constructs a concise,
     * human-readable string summarizing its effects, costs, and rewards.
     *
     * @param obj The {@link JsonObject} containing the raw card data.
     * @return A formatted string describing the card's mechanics.
     */
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

    /**
     * Retrieves the formatted description for a specific card ID.
     *
     * @param cardID The unique identifier of the card (e.g., "CH15", "EV02").
     * @return The formatted description string. If the ID is not found, returns the ID itself as a fallback.
     */
    public static String getDescription(String cardID) {
        return descriptions.getOrDefault(cardID, cardID);
    }

    /**
     * Returns the raw type string for a card ID as stored in the JSON
     * (e.g. "HUNTER", "BUILDER", "SHAMAN", "HUNT", "SUSTENANCE").
     * Returns {@code "UNKNOWN"} if the card ID is not found.
     *
     * @param cardID the card identifier
     * @return the type string
     */
    public static String getType(String cardID) {
        return types.getOrDefault(cardID, "UNKNOWN");
    }

    /**
     * Returns the food discount granted by a builder card when purchasing a building.
     * Returns 0 for non-builder cards or unknown IDs.
     *
     * @param cardID the card identifier
     * @return food discount value (≥ 0)
     */
    public static int getBuilderDiscount(String cardID) {
        return builderDiscounts.getOrDefault(cardID, 0);
    }
}