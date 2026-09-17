package com.unrealvfx.client.vfx.component;

import com.unrealvfx.client.render.DepthCaptureHelper;
import com.unrealvfx.client.render.MeshCache;
import com.unrealvfx.client.render.RenderState;
import com.unrealvfx.client.render.ShaderProgram;
import com.unrealvfx.client.vfx.Easing;
import com.unrealvfx.client.vfx.VFXComponent;
import com.unrealvfx.client.vfx.VFXInstance;
import com.unrealvfx.client.vfx.VFXManager;
import org.lwjgl.opengl.GL11;

/**
 * Componente per onde d'urto ad anello (Shockwaves) che si espandono rapidamente a terra o in aria.
 */
public class ShockwaveComponent implements VFXComponent {

    private float startRadius = 0.5f;
    private float endRadius = 6.0f;
    private Easing radiusEasing = Easing.OUT_QUAD;

    private float startAlpha = 1.0f;
    private float endAlpha = 0.0f;
    private Easing alphaEasing = Easing.IN_QUAD;

    private float[] color = new float[]{1.0f, 0.9f, 0.5f};
    private double offsetY = 0.05; // Leggermente sopra il blocco per evitare Z-fighting col terreno

    public ShockwaveComponent() {}

    @Override
    public void tick(VFXInstance instance) {
    }

    @Override
    public void render(VFXInstance instance, float partialTicks, float progress) {
        float currentRadius = radiusEasing.interpolate(startRadius, endRadius, progress) * instance.getScaleModifier();
        float currentAlpha = alphaEasing.interpolate(startAlpha, endAlpha, progress);

        float r = color[0];
        float g = color[1];
        float b = color[2];

        int override = instance.getColorOverride();
        if (override != -1) {
            r = ((override >> 16) & 0xFF) / 255.0f;
            g = ((override >> 8) & 0xFF) / 255.0f;
            b = (override & 0xFF) / 255.0f;
        }

        RenderState.begin(RenderState.BlendMode.ADDITIVE, false, false);

        ShaderProgram shader = VFXManager.getInstance().getShader("shockwave");
        if (shader != null) {
            shader.use();
            shader.setFloat("u_progress", progress);
            shader.setVec4("u_color", r, g, b, currentAlpha);
            DepthCaptureHelper.setupShaderDepthUniforms(shader, 0.5f);
        }

        GL11.glPushMatrix();
        GL11.glTranslated(0.0, offsetY, 0.0);
        GL11.glScalef(currentRadius, 1.0f, currentRadius);

        GL11.glColor4f(r, g, b, currentAlpha);
        MeshCache.renderRing();

        GL11.glPopMatrix();

        if (shader != null) {
            ShaderProgram.release();
        }

        RenderState.end();
    }

    public ShockwaveComponent setRadius(Easing easing, float start, float end) {
        this.radiusEasing = easing;
        this.startRadius = start;
        this.endRadius = end;
        return this;
    }

    public ShockwaveComponent setAlpha(Easing easing, float start, float end) {
        this.alphaEasing = easing;
        this.startAlpha = start;
        this.endAlpha = end;
        return this;
    }

    public ShockwaveComponent setColor(float r, float g, float b) {
        this.color = new float[]{r, g, b};
        return this;
    }

    public ShockwaveComponent setOffsetY(double offsetY) {
        this.offsetY = offsetY;
        return this;
    }

    public double getOffsetY() {
        return offsetY;
    }

    public float getStartRadius() {
        return startRadius;
    }

    public float getEndRadius() {
        return endRadius;
    }

    public float[] getColor() {
        return color;
    }
}
