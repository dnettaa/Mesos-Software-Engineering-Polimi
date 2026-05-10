package it.polimi.ingsw.network.server;

import com.google.gson.*;
import java.lang.reflect.Type;

/**
 * A custom GSON adapter designed to handle the serialization and deserialization
 * of interfaces and abstract classes.
 * <p>
 * Standard GSON struggles with polymorphism because it loses the concrete class type
 * during serialization. This adapter solves the issue by embedding the fully qualified
 * class name into a {@code CLASSNAME} property within the JSON. During deserialization,
 * it reads this property, dynamically loads the correct class via reflection, and
 * reconstructs the specific object.
 * </p>
 *
 * @param <T> The type of the interface or abstract class being adapted.
 */
public class InterfaceAdapter<T> implements JsonSerializer<T>, JsonDeserializer<T> {

    /** The JSON key used to store the fully qualified name of the concrete class. */
    private static final String CLASSNAME = "CLASSNAME";

    /** The JSON key used to store the actual serialized fields of the object. */
    private static final String DATA = "DATA";

    /** * An isolated, standard GSON instance used strictly for parsing the inner data.
     * Bypassing the custom serialization context prevents infinite recursive loops
     * ({@code StackOverflowError}) when an object contains fields of its own super-type.
     */
    private static final Gson innerGson = new Gson();

    /**
     * Deserializes a JSON element back into an object of the correct concrete type.
     * * @param jsonElement The JSON data being deserialized.
     * @param type The type of the Object to deserialize to.
     * @param context The deserialization context.
     * @return The reconstructed object cast to the generic type {@code T}.
     * @throws JsonParseException If the stored class name cannot be found in the classpath.
     */
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

        @SuppressWarnings("unchecked")
        T result = (T) innerGson.fromJson(jsonObject.get(DATA), klass);
        return result;
    }

    /**
     * Serializes an object by wrapping its data alongside its concrete class name.
     * * @param jsonElement The object to serialize.
     * @param type The type of the source object.
     * @param context The serialization context.
     * @return A {@link JsonObject} containing the {@code CLASSNAME} and the nested {@code DATA}.
     */
    @Override
    public JsonElement serialize(T jsonElement, Type type, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(CLASSNAME, jsonElement.getClass().getName());
        jsonObject.add(DATA, innerGson.toJsonTree(jsonElement));
        return jsonObject;
    }
}