package it.polimi.ingsw.network.server;

import com.google.gson.*;
import java.lang.reflect.Type;

/**
 * Adapter personalizzato per GSON.
 * Permette di serializzare e deserializzare correttamente Interfacce e Classi Astratte
 * salvando il nome effettivo della classe concreta nel JSON, senza causare loop ricorsivi.
 */
public class InterfaceAdapter<T> implements JsonSerializer<T>, JsonDeserializer<T> {

    private static final String CLASSNAME = "CLASSNAME";
    private static final String DATA = "DATA";
    private static final Gson innerGson = new Gson();

    @Override
    public T deserialize(JsonElement jsonElement, Type type,
                         JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        JsonPrimitive prim = (JsonPrimitive) jsonObject.get(CLASSNAME);
        String className = prim.getAsString();
        Class<?> klass;
        try {
            klass = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new JsonParseException(e.getMessage());
        }
        return (T) innerGson.fromJson(jsonObject.get(DATA), klass);
    }

    @Override
    public JsonElement serialize(T jsonElement, Type type, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(CLASSNAME, jsonElement.getClass().getName());
        jsonObject.add(DATA, innerGson.toJsonTree(jsonElement));
        return jsonObject;
    }
}