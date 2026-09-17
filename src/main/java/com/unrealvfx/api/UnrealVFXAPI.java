package com.unrealvfx.api;

import com.unrealvfx.schematic.VFXSchematicRegistry;
import com.unrealvfx.server.ServerVFXInstance;
import com.unrealvfx.server.ServerVFXManager;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

import java.util.Collection;

/**
 * API Pubblica principale di UnrealVFX per l'integrazione con Scripting (CustomNPCs, Nashorn/Rhino, Groovy, Java modding).
 *
 * Esempio in script CustomNPCs (JavaScript):
 * <pre>
 *   var VFX = Java.type("com.unrealvfx.api.UnrealVFXAPI");
 *   var effect = VFX.spawn(npc.world, npc.x, npc.y + 1.0, npc.z, "solar_flare", 2.0);
 *   effect.setVelocity(0.5, 0.1, 0.0);
 *   effect.setRotation(npc.rotation, 0, 0);
 * </pre>
 */
public class UnrealVFXAPI {

    /**
     * Spawna un effetto VFX virtuale tracciato dal server alle coordinate specificate.
     *
     * @param world Mondo di Minecraft
     * @param x Coordinata X
     * @param y Coordinata Y
     * @param z Coordinata Z
     * @param effectId ID dello schematic (es. "solar_flare") o preset ("dbz:hakai_ball")
     * @param scale Moltiplicatore di scala (1.0f normale)
     * @return L'istanza dell'effetto virtuale per successive manipolazioni
     */
    public static ServerVFXInstance spawn(World world, double x, double y, double z, String effectId, float scale) {
        return ServerVFXManager.getInstance().spawn(world, x, y, z, effectId, scale);
    }

    public static ServerVFXInstance spawn(int dimensionId, double x, double y, double z, String effectId, float scale) {
        return ServerVFXManager.getInstance().spawn(dimensionId, x, y, z, effectId, scale);
    }

    /**
     * Aggancia un effetto grafico a un'entità (giocatore, NPC, mob o proiettile).
     * L'effetto ne seguirà automaticamente le coordinate.
     *
     * @param entity Entità bersaglio
     * @param effectId ID dello schematic o preset
     * @param scale Moltiplicatore di scala
     * @return L'istanza dell'effetto virtuale
     */
    public static ServerVFXInstance attach(Entity entity, String effectId, float scale) {
        return ServerVFXManager.getInstance().attach(entity, effectId, scale);
    }

    /**
     * Ottiene un'istanza virtuale attiva tramite il suo ID numerico.
     */
    public static ServerVFXInstance getInstance(int virtualId) {
        return ServerVFXManager.getInstance().getInstance(virtualId);
    }

    /**
     * Restituisce tutte le istanze virtuali correntemente attive nel server.
     */
    public static Collection<ServerVFXInstance> getAllInstances() {
        return ServerVFXManager.getInstance().getAllInstances();
    }

    /**
     * Termina e rimuove forzatamente un effetto virtuale specificando il suo ID.
     */
    public static boolean kill(int virtualId) {
        return ServerVFXManager.getInstance().kill(virtualId);
    }

    /**
     * Rimuove istantaneamente tutti gli effetti virtuali attivi nel server.
     */
    public static void clearAll() {
        ServerVFXManager.getInstance().clearAll();
    }

    /**
     * Ricarica gli schematic dalla cartella config/unrealvfx/effects/ e li trasmette a tutti i giocatori.
     * @return Numero di schematic caricati
     */
    public static int reloadSchematics() {
        return VFXSchematicRegistry.getInstance().reload();
    }
}
