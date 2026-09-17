package com.unrealvfx.client.vfx.component;

import com.unrealvfx.client.render.SpeedLinesRenderer;
import com.unrealvfx.client.vfx.VFXComponent;
import com.unrealvfx.client.vfx.VFXInstance;

/**
 * Componente per attivare linee cinetiche manga a tutto schermo durante un'istanza VFX.
 */
public class SpeedLinesTriggerComponent implements VFXComponent {
    private final float intensity;
    private final float[] color;

    public SpeedLinesTriggerComponent(float intensity, float r, float g, float b) {
        this.intensity = intensity;
        this.color = new float[]{r, g, b};
    }

    @Override
    public void tick(VFXInstance instance) {
        if (!instance.isDead()) {
            SpeedLinesRenderer.getInstance().setIntensity(intensity);
            SpeedLinesRenderer.getInstance().setColor(color[0], color[1], color[2]);
        }
    }

    @Override
    public void render(VFXInstance instance, float partialTicks, float progress) {
    }
}
