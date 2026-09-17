package com.unrealvfx.client.vfx.component;

import com.unrealvfx.client.render.BloomRenderer;
import com.unrealvfx.client.render.MeshCache;
import com.unrealvfx.client.render.RenderState;
import com.unrealvfx.client.render.ShaderProgram;
import com.unrealvfx.client.vfx.Easing;
import com.unrealvfx.client.vfx.VFXComponent;
import com.unrealvfx.client.vfx.VFXInstance;
import com.unrealvfx.client.vfx.VFXManager;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

/**
 * Componente per distorsione ottica e rifrazione dello sfondo (Heat Haze / Shockwave Refraction).
 * Cattura il frame del mondo e devia i raggi visivi dietro all'effetto per creare
 * l'illusione ottica di un'onda d'urto o di calore estremo.
 */
public class DistortionComponent implements VFXComponent {

    private static int screenTextureId = -1;
    private static int lastWidth = -1;
    private static int lastHeight = -1;

    private float startRadius = 0.5f;
    private float endRadius = 9.0f;
    private Easing radiusEasing = Easing.OUT_QUAD;
    private float distortionStrength = 0.045f;
    private double offsetY = 0.1;

    public DistortionComponent(float startRadius, float endRadius, float strength) {
        this.startRadius = startRadius;
        this.endRadius = endRadius;
        this.distortionStrength = strength;
    }

    @Override
    public void tick(VFXInstance instance) {
    }

    @Override
    public void render(VFXInstance instance, float partialTicks, float progress) {
        if (BloomRenderer.isCapturing()) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null) return;
        int width = mc.displayWidth;
        int height = mc.displayHeight;

        if (width <= 0 || height <= 0) return;

        // Inizializza o rialloca la texture di cattura schermo se la finestra cambia risoluzione
        if (screenTextureId == -1 || width != lastWidth || height != lastHeight) {
            if (screenTextureId == -1) {
                screenTextureId = GL11.glGenTextures();
            }
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, screenTextureId);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, width, height, 0, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, (java.nio.ByteBuffer) null);
            lastWidth = width;
            lastHeight = height;
        }

        // Cattura il buffer visivo del mondo
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, screenTextureId);
        GL11.glCopyTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, 0, 0, width, height);

        float currentRadius = radiusEasing.interpolate(startRadius, endRadius, progress) * instance.getScaleModifier();

        RenderState.begin(RenderState.BlendMode.NORMAL, false, false);

        ShaderProgram shader = VFXManager.getInstance().getShader("distortion");
        if (shader != null) {
            shader.use();
            shader.setInt("u_screenTexture", 0);
            shader.setVec2("u_screenSize", width, height);
            shader.setFloat("u_strength", distortionStrength);
            shader.setFloat("u_progress", progress);
        }

        GL11.glPushMatrix();
        GL11.glTranslated(0.0, offsetY, 0.0);
        GL11.glScalef(currentRadius, 1.0f, currentRadius);

        MeshCache.renderRing();

        GL11.glPopMatrix();

        if (shader != null) {
            ShaderProgram.release();
        }

        RenderState.end();
    }

    public float getStartRadius() {
        return startRadius;
    }

    public float getEndRadius() {
        return endRadius;
    }

    public float getStrength() {
        return distortionStrength;
    }

    public double getOffsetY() {
        return offsetY;
    }
}
