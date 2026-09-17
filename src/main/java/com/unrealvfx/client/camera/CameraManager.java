package com.unrealvfx.client.camera;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

/**
 * Gestore dell'impatto visivo della telecamera:
 * - Trauma-Based Camera Shake (senza bloccare il movimento del mouse!)
 * - FOV Kick (dilatazione temporanea del campo visivo nelle detonazioni)
 */
public class CameraManager {
    private static final CameraManager INSTANCE = new CameraManager();

    private float trauma = 0.0f;
    private float fovKick = 0.0f;
    private float noiseSeed = 0.0f;

    public static CameraManager getInstance() {
        return INSTANCE;
    }

    private CameraManager() {}

    public void addTrauma(float amount) {
        this.trauma = Math.min(1.0f, this.trauma + amount);
    }

    public void addFovKick(float amount) {
        this.fovKick = Math.min(30.0f, this.fovKick + amount);
    }

    public void tick() {
        if (trauma > 0.0f) {
            trauma = Math.max(0.0f, trauma - 0.04f);
        }

        if (fovKick > 0.0f) {
            fovKick = Math.max(0.0f, fovKick * 0.82f);
        }

        noiseSeed += 1.6f;

        // Applica un micro-impulso naturale additivo durante il tick senza bloccare il mouse
        float shake = trauma * trauma;
        if (shake > 0.01f) {
            EntityPlayer player = Minecraft.getMinecraft().thePlayer;
            if (player != null) {
                float nudgeYaw = (float) (Math.sin(noiseSeed * 1.8) * 0.45f * shake);
                float nudgePitch = (float) (Math.cos(noiseSeed * 2.2) * 0.35f * shake);
                player.rotationYaw += nudgeYaw;
                player.rotationPitch += nudgePitch;
            }
        }
    }

    public void onRenderTickStart() {
        // Nessun lock forzato sulle coordinate del mouse
    }

    public void onRenderTickEnd() {
        // Nessun reset che sovrascrive i movimenti del giocatore
    }

    public float getFovOffset() {
        return fovKick;
    }

    public float getTrauma() {
        return trauma;
    }
}
