package com.unrealvfx.client.vfx.component;

import com.unrealvfx.client.render.GhostTrailRenderer;
import com.unrealvfx.client.vfx.VFXComponent;
import com.unrealvfx.client.vfx.VFXInstance;

/**
 * Componente per forzare l'effetto Zanzoken (After-Images) durante uno scatto o carica di ki.
 */
public class ZanzokenComponent implements VFXComponent {

    @Override
    public void tick(VFXInstance instance) {
        if (!instance.isDead()) {
            GhostTrailRenderer.getInstance().setForceZanzoken(true);
        } else {
            GhostTrailRenderer.getInstance().setForceZanzoken(false);
        }
    }

    @Override
    public void render(VFXInstance instance, float partialTicks, float progress) {
    }
}
