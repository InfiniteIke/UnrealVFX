package com.unrealvfx.schematic;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Parser per la deserializzazione dell'header dei file Schematic VFX (.json).
 * 100% Server-Safe: non contiene alcun import o riferimento a classi client, OpenGL o LWJGL.
 */
public class VFXSchematicParser {

    private static final JsonParser PARSER = new JsonParser();

    /**
     * Parsing leggero dell'header dello schematic.
     * Sicuro al 100% per l'esecuzione su Server dedicato.
     */
    public static VFXSchematic parseHeader(String jsonSource) {
        try {
            JsonObject root = PARSER.parse(jsonSource).getAsJsonObject();
            String id = getString(root, "id", "unnamed_vfx");
            int duration = getInt(root, "duration", 60);
            boolean loop = getBoolean(root, "loop", false);
            double trackingRadius = getFloat(root, "trackingRadius", 96.0f);

            return new VFXSchematic(id, duration, loop, trackingRadius, jsonSource);
        } catch (Exception e) {
            throw new RuntimeException("Errore durante il parsing dell'header dello schematic VFX", e);
        }
    }

    private static String getString(JsonObject obj, String name, String def) {
        return (obj.has(name) && !obj.get(name).isJsonNull()) ? obj.get(name).getAsString() : def;
    }

    private static int getInt(JsonObject obj, String name, int def) {
        return (obj.has(name) && !obj.get(name).isJsonNull()) ? obj.get(name).getAsInt() : def;
    }

    private static float getFloat(JsonObject obj, String name, float def) {
        return (obj.has(name) && !obj.get(name).isJsonNull()) ? obj.get(name).getAsFloat() : def;
    }

    private static boolean getBoolean(JsonObject obj, String name, boolean def) {
        return (obj.has(name) && !obj.get(name).isJsonNull()) ? obj.get(name).getAsBoolean() : def;
    }
}
