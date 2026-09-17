package com.unrealvfx.client.vfx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Blueprint immutabile che descrive la ricetta di un effetto grafico (VFX).
 * Può essere composto liberamente da più componenti grafici (Mesh, Shockwave, ecc.).
 */
public class VFXBlueprint {
    private String id;
    private int duration = 40; // Durata predefinita di 2 secondi (40 tick)
    private boolean loop = false;
    private final List<VFXComponent> components = new ArrayList<>();

    public VFXBlueprint() {}

    public VFXBlueprint(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public VFXBlueprint setId(String id) {
        this.id = id;
        return this;
    }

    public int getDuration() {
        return duration;
    }

    public VFXBlueprint setDuration(int duration) {
        this.duration = Math.max(1, duration);
        return this;
    }

    public boolean isLoop() {
        return loop;
    }

    public VFXBlueprint setLoop(boolean loop) {
        this.loop = loop;
        return this;
    }

    public VFXBlueprint addComponent(VFXComponent component) {
        if (component != null) {
            this.components.add(component);
        }
        return this;
    }

    public List<VFXComponent> getComponents() {
        return Collections.unmodifiableList(components);
    }
}
