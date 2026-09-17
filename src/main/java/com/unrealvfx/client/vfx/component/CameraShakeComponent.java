package com.unrealvfx.client.vfx.component;

import com.unrealvfx.client.camera.CameraManager;
import com.unrealvfx.client.vfx.VFXComponent;
import com.unrealvfx.client.vfx.VFXInstance;

/**
 * Componente per innescare Camera Shake (scuotimento viscerale dello schermo)
 * e FOV Kick (dilatazione campo visivo).
 */
public class CameraShakeComponent implements VFXComponent {

    private final float traumaAmount;
    private final float fovKickAmount;
    private final boolean continuous; // True per aure (ronzio continuo), False per esplosioni (singolo shock all'avvio)

    public CameraShakeComponent(float traumaAmount, float fovKickAmount, boolean continuous) {
        this.traumaAmount = traumaAmount;
        this.fovKickAmount = fovKickAmount;
        this.continuous = continuous;
    }

    public static CameraShakeComponent oneShot(float trauma, float fovKick) {
        return new CameraShakeComponent(trauma, fovKick, false);
    }

    public static CameraShakeComponent continuous(float trauma) {
        return new CameraShakeComponent(trauma, 0.0f, true);
    }

    @Override
    public void tick(VFXInstance instance) {
        if (continuous) {
            // Mantiene una vibrazione costante durante tutta la durata dell'effetto
            if (CameraManager.getInstance().getTrauma() < traumaAmount) {
                CameraManager.getInstance().addTrauma(traumaAmount * 0.4f);
            }
        } else {
            // Singolo colpo iniziale nel primo tick di vita
            if (instance.getAge() == 1) {
                CameraManager.getInstance().addTrauma(traumaAmount);
                if (fovKickAmount > 0.0f) {
                    CameraManager.getInstance().addFovKick(fovKickAmount);
                }
            }
        }
    }

    @Override
    public void render(VFXInstance instance, float partialTicks, float progress) {
    }

    public float getTraumaAmount() {
        return traumaAmount;
    }

    public float getTrauma() {
        return traumaAmount;
    }

    public float getFovKickAmount() {
        return fovKickAmount;
    }

    public float getFovKick() {
        return fovKickAmount;
    }

    public boolean isContinuous() {
        return continuous;
    }
}
