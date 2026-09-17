package com.unrealvfx.client.vfx.component;

import com.unrealvfx.client.render.MeshCache;
import com.unrealvfx.client.render.RenderState;
import com.unrealvfx.client.render.ShaderProgram;
import com.unrealvfx.client.vfx.Easing;
import com.unrealvfx.client.vfx.VFXComponent;
import com.unrealvfx.client.vfx.VFXInstance;
import com.unrealvfx.client.vfx.VFXManager;
import org.lwjgl.opengl.GL11;

/**
 * Componente per Ground Decal proiettato sul terreno (Crateri e Crepe di energia fusa).
 * Rimane stampato sul suolo senza distruggere i blocchi e sfuma gradualmente.
 */
public class GroundDecalComponent implements VFXComponent {

    private float startRadius = 0.5f;
    private float endRadius = 6.0f;
    private Easing radiusEasing = Easing.OUT_EXPO;

    private float startAlpha = 1.0f;
    private float endAlpha = 0.0f;
    private Easing alphaEasing = Easing.IN_QUAD;

    private float[] color = new float[]{1.0f, 0.2f, 0.85f}; // Magenta Hakai di default
    private double offsetY = 0.025; // Sopra il blocco per evitare Z-fighting

    public GroundDecalComponent(float startRadius, float endRadius) {
        this.startRadius = startRadius;
        this.endRadius = endRadius;
    }

    @Override
    public void tick(VFXInstance instance) {
    }

    @Override
    public void render(VFXInstance instance, float partialTicks, float progress) {
        float currentRadius = radiusEasing.interpolate(startRadius, endRadius, progress) * instance.getScaleModifier();
        float currentAlpha = alphaEasing.interpolate(startAlpha, endAlpha, progress);

        float r = color[0], g = color[1], b = color[2];
        int override = instance.getColorOverride();
        if (override != -1) {
            r = ((override >> 16) & 0xFF) / 255.0f;
            g = ((override >> 8) & 0xFF) / 255.0f;
            b = (override & 0xFF) / 255.0f;
        }

        RenderState.begin(RenderState.BlendMode.NORMAL, false, false);

        ShaderProgram shader = VFXManager.getInstance().getShader("decal_cracks");
        if (shader != null) {
            shader.use();
            shader.setFloat("u_progress", progress);
            shader.setVec3("u_color", r, g, b);
            shader.setFloat("u_alpha", currentAlpha);
        }

        GL11.glPushMatrix();
        GL11.glTranslated(0.0, offsetY, 0.0);
        // Ruota il quad orizzontale sul piano del pavimento
        GL11.glRotatef(90.0f, 1.0f, 0.0f, 0.0f);
        GL11.glScalef(currentRadius * 2.0f, currentRadius * 2.0f, 1.0f);

        MeshCache.renderQuad();

        GL11.glPopMatrix();

        if (shader != null) {
            ShaderProgram.release();
        }

        RenderState.end();
    }

    public GroundDecalComponent setColor(float r, float g, float b) {
        this.color = new float[]{r, g, b};
        return this;
    }

    public GroundDecalComponent setRadius(Easing easing, float start, float end) {
        this.radiusEasing = easing;
        this.startRadius = start;
        this.endRadius = end;
        return this;
    }

    public GroundDecalComponent setAlpha(Easing easing, float start, float end) {
        this.alphaEasing = easing;
        this.startAlpha = start;
        this.endAlpha = end;
        return this;
    }
}
