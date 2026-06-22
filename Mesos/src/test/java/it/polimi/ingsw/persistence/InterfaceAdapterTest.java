package it.polimi.ingsw.persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests polymorphic JSON serialization and deserialization through
 * {@link InterfaceAdapter}.
 *
 * @author Diana
 */
class InterfaceAdapterTest {

    /**
     * Verifies that an object serialized through an interface stores the concrete
     * implementation class name in the generated JSON.
     * Setup: a Gson instance registers {@link InterfaceAdapter} for an interface type.
     * Action: serialize a concrete implementation through the interface reference.
     * Expected behavior: the JSON contains both the concrete class name and wrapped object data.
     * Edge case covered: polymorphic persistence must retain enough type information to restore the object.
     */
    @Test
    void serializeShouldStoreConcreteClassNameAndData() {
        Gson gson = createGson();
        TestPayload payload = new NumberPayload("food", 7);

        JsonObject json = gson.toJsonTree(payload, TestPayload.class).getAsJsonObject();

        assertEquals(NumberPayload.class.getName(), json.get("CLASSNAME").getAsString());
        assertTrue(json.has("DATA"));
        assertEquals("food", json.getAsJsonObject("DATA").get("name").getAsString());
        assertEquals(7, json.getAsJsonObject("DATA").get("amount").getAsInt());
    }

    /**
     * Verifies that serialized polymorphic JSON is restored to the original
     * concrete implementation.
     * Setup: a concrete payload is serialized through an interface reference.
     * Action: deserialize the JSON back through the same interface type.
     * Expected behavior: the resulting object is a {@link NumberPayload} with preserved field values.
     * Edge case covered: interface-typed fields in saved games must restore concrete runtime behavior.
     */
    @Test
    void deserializeShouldRestoreConcreteImplementation() {
        Gson gson = createGson();
        TestPayload original = new NumberPayload("prestige", 12);
        String json = gson.toJson(original, TestPayload.class);

        TestPayload restored = gson.fromJson(json, TestPayload.class);

        assertTrue(restored instanceof NumberPayload);
        NumberPayload restoredPayload = (NumberPayload) restored;
        assertEquals("prestige", restoredPayload.name);
        assertEquals(12, restoredPayload.amount);
    }

    /**
     * Verifies that an unknown stored class name produces a JSON parse failure.
     * Setup: a manually built JSON object contains a class name that is absent from the classpath.
     * Action: deserialize the object through {@link InterfaceAdapter}.
     * Expected behavior: deserialization throws {@link JsonParseException}.
     * Edge case covered: corrupted or incompatible save files should fail clearly during type restoration.
     */
    @Test
    void deserializeShouldThrowJsonParseExceptionWhenClassNameIsUnknown() {
        Gson gson = createGson();
        JsonObject json = new JsonObject();
        json.addProperty("CLASSNAME", "it.polimi.ingsw.persistence.UnknownPayload");
        json.add("DATA", new JsonObject());

        assertThrows(JsonParseException.class, () -> gson.fromJson(json, TestPayload.class));
    }

    private Gson createGson() {
        return new GsonBuilder()
                .registerTypeAdapter(TestPayload.class, new InterfaceAdapter<TestPayload>())
                .create();
    }

    /**
     * Test-only interface used to exercise polymorphic persistence.
     */
    private interface TestPayload {
    }

    /**
     * Test-only concrete payload with simple fields that Gson can serialize.
     */
    private static class NumberPayload implements TestPayload {

        private String name;
        private int amount;

        private NumberPayload() {
        }

        private NumberPayload(String name, int amount) {
            this.name = name;
            this.amount = amount;
        }
    }
}
