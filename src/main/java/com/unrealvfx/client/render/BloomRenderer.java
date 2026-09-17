package com.unrealvfx.client.render;

import com.unrealvfx.client.vfx.VFXManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.GL11;

/**
 * Pipeline di Post-Processing Bloom multi-stadio progressivo (MIP Pyramid Blur / Gaussian Glow).
 * Diffonde la luce dei VFX sullo schermo con un alone morbido, accecante e saturo (HDR Glow),
 * replicando il comportamento e la resa di UnrealBloomPass (Three.js) nativamente in Minecraft 1.7.10.
 */
public class BloomRenderer {
    private static Framebuffer bloomFboA;
    private static Framebuffer bloomFboB;
    private static int lastWidth = -1;
    private static int lastHeight = -1;

    private static void checkFbos(int width, int height) {
        if (bloomFboA == null || bloomFboB == null || width != lastWidth || height != lastHeight) {
            if (bloomFboA != null) {
                bloomFboA.deleteFramebuffer();
            }
            if (bloomFboB != null) {
                bloomFboB.deleteFramebuffer();
            }

            // Risoluzione nativa a schermo intero con depth buffer per supportare soft particles e depth-aware shaders
            bloomFboA = new Framebuffer(width, height, true);
            bloomFboB = new Framebuffer(width, height, true);

            bloomFboA.setFramebufferFilter(GL11.GL_LINEAR);
            bloomFboB.setFramebufferFilter(GL11.GL_LINEAR);

            lastWidth = width;
            lastHeight = height;
        }
    }

    private static boolean capturing = false;

    public static boolean isCapturing() {
        return capturing;
    }

    public static void setCapturing(boolean val) {
        capturing = val;
    }

    /**
     * Ripristino di emergenza per evitare che lo stato rimanga bloccato in capturing
     * o che il framebuffer di Minecraft rimanga non associato.
     */
    public static void forceReset() {
        capturing = false;
        try {
            ShaderProgram.release();
        } catch (Throwable ignored) {}
        try {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc != null && mc.getFramebuffer() != null) {
                mc.getFramebuffer().bindFramebuffer(true);
            }
        } catch (Throwable ignored) {}
    }

    /**
     * Inizia la cattura della passata emissiva per il bloom.
     */
    public static void beginCapture() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.getFramebuffer() == null || !OpenGlHelper.isFramebufferEnabled() || mc.displayWidth <= 0 || mc.displayHeight <= 0) return;

        checkFbos(mc.displayWidth, mc.displayHeight);
        if (bloomFboA == null || bloomFboB == null) return;

        capturing = true;

        // Pulisce l'FBO senza chiamare framebufferClear() che causerebbe unbindFramebuffer() e FBO 0 unbound
        bloomFboA.bindFramebuffer(true);
        GL11.glColorMask(true, true, true, true);
        GL11.glDepthMask(true);
        GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GL11.glClearDepth(1.0D);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
    }

    /**
     * Termina la cattura, esegue i passaggi progressivi di sfocatura (Blur) e compone il bloom sullo schermo.
     */
    public static void endAndComposite() {
        if (!capturing) return;
        capturing = false;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.getFramebuffer() == null || !OpenGlHelper.isFramebufferEnabled() || mc.displayWidth <= 0 || mc.displayHeight <= 0) {
            forceReset();
            return;
        }

        int width = mc.displayWidth;
        int height = mc.displayHeight;

        if (bloomFboA == null || bloomFboB == null) {
            forceReset();
            return;
        }

        ShaderProgram blurShader = VFXManager.getInstance().getShader("bloom_blur");
        if (blurShader == null) {
            forceReset();
            return;
        }

        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glPushMatrix();
        try {
            OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
            GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glDepthMask(false);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_FOG);
            GL11.glDisable(GL11.GL_ALPHA_TEST);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glColorMask(true, true, true, true);

            // =========================================================================
            // Sfocatura Gaussiana Progressiva Multi-Pass (step 1.5, 4.0, 8.5)
            // Simula la piramide MIP di Three.js UnrealBloomPass
            // =========================================================================
            GL11.glDisable(GL11.GL_BLEND);

            blurShader.use();
            blurShader.setInt("u_texture", 0);

            float[] blurSteps = new float[]{ 1.5f, 4.0f, 8.5f };
            for (float step : blurSteps) {
                // Passaggio Orizzontale (FBO A -> FBO B)
                bloomFboB.bindFramebuffer(true);
                GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
                GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

                blurShader.setVec2("u_blurDir", step / (float) width, 0.0f);
                bloomFboA.bindFramebufferTexture();
                renderScreenQuad(width, height);
                bloomFboA.unbindFramebufferTexture();

                // Passaggio Verticale (FBO B -> FBO A)
                bloomFboA.bindFramebuffer(true);
                GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
                GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

                blurShader.setVec2("u_blurDir", 0.0f, step / (float) height);
                bloomFboB.bindFramebufferTexture();
                renderScreenQuad(width, height);
                bloomFboB.unbindFramebufferTexture();
            }

            ShaderProgram.release();

            // =========================================================================
            // Composizione Additiva sullo Schermo di Minecraft (mc.getFramebuffer())
            // Blending additivo (GL_ONE, GL_ONE) e intensità totale 1.8x (passata doppia 1.0 + 0.8)
            // per replicare l'intensità 1.8 di Three.js UnrealBloomPass
            // =========================================================================
            mc.getFramebuffer().bindFramebuffer(true);

            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);

            bloomFboA.bindFramebufferTexture();

            // Prima passata: intensità 1.0
            GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
            renderScreenQuad(width, height);

            // Seconda passata: intensità 0.8 (totale combinato = 1.8x)
            GL11.glColor4f(0.8f, 0.8f, 0.8f, 1.0f);
            renderScreenQuad(width, height);

            bloomFboA.unbindFramebufferTexture();
        } finally {
            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            GL11.glPopMatrix();
            GL11.glPopAttrib();
            if (mc.getFramebuffer() != null) {
                mc.getFramebuffer().bindFramebuffer(true);
            }
        }
    }

    private static void renderScreenQuad(int width, int height) {
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        GL11.glOrtho(0.0D, width, height, 0.0D, 1000.0D, 3000.0D);

        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        GL11.glTranslatef(0.0F, 0.0F, -2000.0F);

        GL11.glBegin(GL11.GL_QUADS);
        GL11.glTexCoord2f(0.0F, 0.0F); GL11.glVertex3f(0.0F, height, 0.0F);
        GL11.glTexCoord2f(1.0F, 0.0F); GL11.glVertex3f(width, height, 0.0F);
        GL11.glTexCoord2f(1.0F, 1.0F); GL11.glVertex3f(width, 0.0F, 0.0F);
        GL11.glTexCoord2f(0.0F, 1.0F); GL11.glVertex3f(0.0F, 0.0F, 0.0F);
        GL11.glEnd();

        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPopMatrix();
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPopMatrix();
    }
}
