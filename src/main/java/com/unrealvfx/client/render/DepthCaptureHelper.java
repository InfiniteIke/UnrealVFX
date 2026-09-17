package com.unrealvfx.client.render;

import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;

/**
 * Gestore per l'estrazione e il binding del Depth Buffer del mondo in Minecraft 1.7.10.
 * Consente l'implementazione di Soft Particles (Depth Fading) eliminando le intersezioni
 * poligonali nette e plasticose con blocchi e terreno.
 */
public class DepthCaptureHelper {
    private static int depthTexId = -1;
    private static int allocatedW = -1;
    private static int allocatedH = -1;
    private static boolean enabled = true;

    public static void prepareDepthTexture(int w, int h) {
        if (depthTexId == -1 || allocatedW != w || allocatedH != h) {
            if (depthTexId != -1) {
                GL11.glDeleteTextures(depthTexId);
            }

            depthTexId = GL11.glGenTextures();
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, depthTexId);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

            // Alloca storage depth buffer a 24 bit
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL14.GL_DEPTH_COMPONENT24, 
                              w, h, 0, GL11.GL_DEPTH_COMPONENT, GL11.GL_UNSIGNED_INT, (java.nio.ByteBuffer) null);
            allocatedW = w;
            allocatedH = h;
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        }
    }

    /**
     * Cattura istantaneamente il depth buffer attuale dell'FBO di Minecraft.
     * Da chiamare all'inizio della passata di render VFX in RenderWorldLastEvent.
     */
    public static void capture() {
        if (!enabled) return;
        try {
            Minecraft mc = Minecraft.getMinecraft();
            int w = mc.displayWidth;
            int h = mc.displayHeight;
            if (w <= 0 || h <= 0) return;

            prepareDepthTexture(w, h);

            GL11.glBindTexture(GL11.GL_TEXTURE_2D, depthTexId);
            GL11.glCopyTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, 0, 0, w, h);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        } catch (Throwable t) {
            enabled = false;
        }
    }

    /**
     * Associa la texture di profondita alla Texture Unit specificata (es. 1).
     */
    public static void bindToUnit(int textureUnit) {
        if (!enabled || depthTexId == -1) return;
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + textureUnit);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, depthTexId);
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
    }

    /**
     * Rilascia la texture unit.
     */
    public static void unbind(int textureUnit) {
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + textureUnit);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
    }

    /**
     * Configura tutte le uniform di linearizzazione della profondita per lo shader attivo.
     */
    public static void setupShaderDepthUniforms(ShaderProgram shader, float fadeDistance) {
        if (shader == null) return;
        Minecraft mc = Minecraft.getMinecraft();
        shader.setInt("u_depthTexture", 1);
        shader.setVec2("u_screenResolution", (float) mc.displayWidth, (float) mc.displayHeight);
        float near = 0.05f;
        float far = Math.max(16.0f, (float) mc.gameSettings.renderDistanceChunks * 16.0f);
        shader.setFloat("u_near", near);
        shader.setFloat("u_far", far);
        shader.setFloat("u_fadeDistance", enabled ? fadeDistance : 0.0f);
    }

    public static boolean isEnabled() {
        return enabled;
    }
}
