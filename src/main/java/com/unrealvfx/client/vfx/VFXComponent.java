package com.unrealvfx.client.vfx;

/**
 * Interfaccia base per ogni modulo visivo che compone un VFX
 * (es. guscio 3D, emettitore particelle, anello shockwave, raggio).
 */
public interface VFXComponent {

    /**
     * Chiamato ogni tick logico (20 volte al secondo).
     */
    void tick(VFXInstance instance);

    /**
     * Chiamato durante la fase di rendering (ogni frame).
     * @param instance L'istanza dell'effetto attiva
     * @param partialTicks Tempo parziale per interpolazione fluida tra i tick
     * @param progress Avanzamento temporale normalizzato da 0.0f (inizio) a 1.0f (fine)
     */
    void render(VFXInstance instance, float partialTicks, float progress);

    /**
     * Chiamato quando l'effetto giunge al termine del suo ciclo vitale naturale.
     */
    default void onDeath(VFXInstance instance) {}

    /**
     * Chiamato quando l'effetto viene forzatamente rimosso dal mondo.
     */
    default void onRemove(VFXInstance instance) {}
}
