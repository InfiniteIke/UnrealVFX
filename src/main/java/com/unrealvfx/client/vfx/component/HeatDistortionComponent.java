package com.unrealvfx.client.vfx.component;

import com.unrealvfx.client.render.BloomRenderer;
import com.unrealvfx.client.render.MeshCache;
import com.unrealvfx.client.render.RenderState;
import com.unrealvfx.client.render.ShaderProgram;
import com.unrealvfx.client.vfx.VFXComponent;
import com.unrealvfx.client.vfx.VFXInstance;
import com.unrealvfx.client.vfx.VFXManager;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;

import java.nio.ByteBuffer;

/**
 * Componente per la simulazione volumetrica di onde di calore e rifrazione ottica (Heat Distortion).
 * Consente la configurazione della forma geometrica (SPHERE, CUBE, CYLINDER), della forza di distorsione,
 * del decadimento dal centro verso i bordi (Center Falloff con esponente personalizzabile),
 * della velocita/frequenza dello shimmer termico procedurale e delle dimensioni 3D.
 */
public class HeatDistortionComponent implements VFXComponent {

    public enum Shape {
        SPHERE,
        CUBE,
        CYLINDER
    }

    private static int screenTextureId = -1;
    private static int lastWidth = -1;
    private static int lastHeight = -1;

    private Shape shape = Shape.SPHERE;
    private float strength = 0.5f;
    private float falloffPower = 2.0f;
    private float noiseSpeed = 2.5f;
    private float noiseScale = 4.0f;
    private float scaleX = 2.0f;
    private float scaleY = 2.0f;
    private float scaleZ = 2.0f;
    private float offsetX = 0.0f;
    private float offsetY = 0.0f;
    private float offsetZ = 0.0f;
    private float[] tintColor = new float[]{1.0f, 0.65f, 0.25f};
    private float tintAlpha = 0.0f;
    private boolean cullFace = false;

    public HeatDistortionComponent() {
    }

    public HeatDistortionComponent(Shape shape, float strength, float falloffPower) {
        this.shape = shape != null ? shape : Shape.SPHERE;
        this.strength = strength;
        this.falloffPower = falloffPower;
    }

    public HeatDistortionComponent(Shape shape, float strength, float falloffPower, float scaleX, float scaleY, float scaleZ) {
        this(shape, strength, falloffPower);
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.scaleZ = scaleZ;
    }

    @Override
    public void tick(VFXInstance instance) {
    }

    @Override
    public void render(VFXInstance instance, float partialTicks, float progress) {
        // Evita cattura ricorsiva durante il pass di bloom per massimizzare le prestazioni
        if (BloomRenderer.isCapturing()) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null) return;
        int width = mc.displayWidth;
        int height = mc.displayHeight;

        if (width <= 0 || height <= 0) return;

        // Inizializza o aggiorna la texture di cattura del frame del mondo
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        if (screenTextureId == -1 || width != lastWidth || height != lastHeight) {
            if (screenTextureId == -1) {
                screenTextureId = GL11.glGenTextures();
            }
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, screenTextureId);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, width, height, 0, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
            lastWidth = width;
            lastHeight = height;
        }

        // Cattura il frame attuale dello schermo
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, screenTextureId);
        GL11.glCopyTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, 0, 0, width, height);

        RenderState.begin(RenderState.BlendMode.NORMAL, false, cullFace);

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, screenTextureId);

        ShaderProgram shader = VFXManager.getInstance().getShader("heat_haze");
        if (shader != null) {
            shader.use();
            shader.setInt("u_screenTexture", 0);
            shader.setVec2("u_screenSize", width, height);
            float currentAge = instance != null ? instance.getAge() : 0.0f;
            shader.setFloat("u_time", (currentAge + partialTicks) * 0.05f);
            shader.setFloat("u_strength", strength);
            shader.setFloat("u_falloffPower", falloffPower);
            shader.setInt("u_shape", shape.ordinal()); // 0=SPHERE, 1=CUBE, 2=CYLINDER
            shader.setFloat("u_noiseSpeed", noiseSpeed);
            shader.setFloat("u_noiseScale", noiseScale);
            shader.setVec3("u_scale", scaleX, scaleY, scaleZ);
            shader.setVec3("u_tintColor", tintColor[0], tintColor[1], tintColor[2]);
            shader.setFloat("u_tintAlpha", tintAlpha);
        }

        GL11.glPushMatrix();
        GL11.glTranslated(offsetX, offsetY, offsetZ);

        float mod = instance != null ? instance.getScaleModifier() : 1.0f;
        GL11.glScalef(scaleX * mod, scaleY * mod, scaleZ * mod);

        switch (shape) {
            case CUBE:
                MeshCache.renderCube();
                break;
            case CYLINDER:
                MeshCache.renderCylinder();
                break;
            case SPHERE:
            default:
                MeshCache.renderSphere();
                break;
        }

        GL11.glPopMatrix();

        if (shader != null) {
            ShaderProgram.release();
        }

        RenderState.end();
    }

    public Shape getShape() {
        return shape;
    }

    public HeatDistortionComponent setShape(Shape shape) {
        this.shape = shape != null ? shape : Shape.SPHERE;
        return this;
    }

    public float getStrength() {
        return strength;
    }

    public HeatDistortionComponent setStrength(float strength) {
        this.strength = strength;
        return this;
    }

    public float getCenterFalloff() {
        return falloffPower;
    }

    public HeatDistortionComponent setCenterFalloff(float centerFalloff) {
        this.falloffPower = centerFalloff;
        return this;
    }

    public float getFalloffPower() {
        return falloffPower;
    }

    public HeatDistortionComponent setFalloffPower(float falloffPower) {
        this.falloffPower = falloffPower;
        return this;
    }

    public float getNoiseSpeed() {
        return noiseSpeed;
    }

    public HeatDistortionComponent setNoiseSpeed(float noiseSpeed) {
        this.noiseSpeed = noiseSpeed;
        return this;
    }

    public float getNoiseScale() {
        return noiseScale;
    }

    public HeatDistortionComponent setNoiseScale(float noiseScale) {
        this.noiseScale = noiseScale;
        return this;
    }

    public float getNoiseFrequency() {
        return noiseScale;
    }

    public HeatDistortionComponent setNoiseFrequency(float noiseFrequency) {
        this.noiseScale = noiseFrequency;
        return this;
    }

    public float getScaleX() {
        return scaleX;
    }

    public HeatDistortionComponent setScaleX(float scaleX) {
        this.scaleX = scaleX;
        return this;
    }

    public float getScaleY() {
        return scaleY;
    }

    public HeatDistortionComponent setScaleY(float scaleY) {
        this.scaleY = scaleY;
        return this;
    }

    public float getScaleZ() {
        return scaleZ;
    }

    public HeatDistortionComponent setScaleZ(float scaleZ) {
        this.scaleZ = scaleZ;
        return this;
    }

    public HeatDistortionComponent setScale(float x, float y, float z) {
        this.scaleX = x;
        this.scaleY = y;
        this.scaleZ = z;
        return this;
    }

    public HeatDistortionComponent setScale(float uniform) {
        return setScale(uniform, uniform, uniform);
    }

    public float getOffsetX() {
        return offsetX;
    }

    public HeatDistortionComponent setOffsetX(float offsetX) {
        this.offsetX = offsetX;
        return this;
    }

    public float getOffsetY() {
        return offsetY;
    }

    public HeatDistortionComponent setOffsetY(float offsetY) {
        this.offsetY = offsetY;
        return this;
    }

    public float getOffsetZ() {
        return offsetZ;
    }

    public HeatDistortionComponent setOffsetZ(float offsetZ) {
        this.offsetZ = offsetZ;
        return this;
    }

    public HeatDistortionComponent setOffset(float x, float y, float z) {
        this.offsetX = x;
        this.offsetY = y;
        this.offsetZ = z;
        return this;
    }

    public float[] getTintColor() {
        return tintColor;
    }

    public HeatDistortionComponent setTintColor(float r, float g, float b) {
        this.tintColor = new float[]{r, g, b};
        return this;
    }

    public float getTintAlpha() {
        return tintAlpha;
    }

    public HeatDistortionComponent setTintAlpha(float tintAlpha) {
        if (tintAlpha > 1.0f && tintAlpha <= 100.0f) {
            this.tintAlpha = tintAlpha / 100.0f;
        } else {
            this.tintAlpha = Math.max(0.0f, Math.min(1.0f, tintAlpha));
        }
        return this;
    }

    public boolean isCullFace() {
        return cullFace;
    }

    public HeatDistortionComponent setCullFace(boolean cullFace) {
        this.cullFace = cullFace;
        return this;
    }
}
