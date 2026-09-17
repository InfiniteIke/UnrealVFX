package com.unrealvfx.schematic;

/**
 * Rappresenta la definizione immutabile di uno "schematic" VFX salvato in formato JSON.
 * Può essere caricato sia lato server (per logica, tracking e sincronizzazione di rete)
 * che lato client (per il parsing nei componenti grafici e il rendering GPU).
 */
public class VFXSchematic {
    private final String id;
    private final int duration;
    private final boolean loop;
    private final double trackingRadius;
    private final String jsonSource;

    public VFXSchematic(String id, int duration, boolean loop, double trackingRadius, String jsonSource) {
        this.id = id;
        this.duration = Math.max(1, duration);
        this.loop = loop;
        this.trackingRadius = Math.max(8.0, trackingRadius);
        this.jsonSource = jsonSource;
    }

    public String getId() {
        return id;
    }

    public int getDuration() {
        return duration;
    }

    public boolean isLoop() {
        return loop;
    }

    public double getTrackingRadius() {
        return trackingRadius;
    }

    public String getJsonSource() {
        return jsonSource;
    }
}
