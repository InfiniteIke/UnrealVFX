package com.unrealvfx.client.render;

import com.unrealvfx.client.vfx.VFXManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import java.nio.ByteBuffer;

/**
 * Gestore del filtro Anime Sakuga Pencil Sketch (Inverted Manga / Progressive Motion Drawing).
 * Utilizza un accumulatore temporale Ping-Pong FBO per simulare il disegno progressivo a matita:
 * quando il giocatore gira la visuale o un mob si muove, i nuovi tratti vengono attivamente
 * tracciati a mano fotogramma per fotogramma con graffiature di grafite e scia di bozzetto.
 */
public class AnimeSketchManager {

    private static final AnimeSketchManager INSTANCE = new AnimeSketchManager();

    private boolean continuousActive = false;
    private int burstTicksRemaining = 0;
    private float currentIntensity = 0.0f;
    private long totalTicks = 0;
    private boolean needsReset = true;

    private static Framebuffer historyFboA;
    private static Framebuffer historyFboB;
    private static int captureTexId = -1;
    private static int lastW = -1;
    private static int lastH = -1;

    public static AnimeSketchManager getInstance() {
        return INSTANCE;
    }

    private AnimeSketchManager() {}

    public void setActive(boolean active) {
        if (active && !this.continuousActive) {
            this.needsReset = true;
        }
        this.continuousActive = active;
        if (!active) {
            this.burstTicksRemaining = 0;
        }
    }

    public void toggle() {
        setActive(!this.continuousActive);
    }

    public boolean isActive() {
        return continuousActive || burstTicksRemaining > 0;
    }

    public void triggerBurst(int ticks) {
        this.needsReset = true;
        this.burstTicksRemaining = Math.max(1, ticks);
    }

    public void tick() {
        totalTicks++;

        if (burstTicksRemaining > 0) {
            burstTicksRemaining--;
        }

        boolean shouldBeActive = isActive();
        float target = shouldBeActive ? 1.0f : 0.0f;
        float speed = shouldBeActive ? 0.40f : 0.22f;
        currentIntensity += (target - currentIntensity) * speed;
        if (Math.abs(currentIntensity - target) < 0.01f) {
            currentIntensity = target;
        }
    }

    private void checkFbos(int width, int height) {
        if (historyFboA == null || historyFboB == null || width != lastW || height != lastH) {
            if (historyFboA != null) historyFboA.deleteFramebuffer();
            if (historyFboB != null) historyFboB.deleteFramebuffer();

            historyFboA = new Framebuffer(width, height, false);
            historyFboB = new Framebuffer(width, height, false);

            historyFboA.setFramebufferFilter(GL11.GL_LINEAR);
            historyFboB.setFramebufferFilter(GL11.GL_LINEAR);

            historyFboA.bindFramebuffer(true);
            historyFboA.framebufferClear();

            historyFboB.bindFramebuffer(true);
            historyFboB.framebufferClear();

            this.needsReset = true;
        }
    }

    public void render() {
        if (currentIntensity <= 0.005f) return;

        Minecraft mc = Minecraft.getMinecraft();
        int width = mc.displayWidth;
        int height = mc.displayHeight;
        if (width <= 0 || height <= 0) return;

        checkFbos(width, height);

        // Cattura del frame corrente di Minecraft
        if (captureTexId == -1 || width != lastW || height != lastH) {
            if (captureTexId == -1) {
                captureTexId = GL11.glGenTextures();
            }
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, captureTexId);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, width, height, 0, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
            lastW = width;
            lastH = height;
        }

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, captureTexId);
        GL11.glCopyTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, 0, 0, width, height);

        // =========================================================================
        // PASSAGGIO 1: Accumulo Temporale e Tracciamento Progressivo (Ping-Pong FBO)
        // =========================================================================
        historyFboB.bindFramebuffer(true);

        ShaderProgram shader = VFXManager.getInstance().getShader("anime_sketch");
        if (shader == null) {
            mc.getFramebuffer().bindFramebuffer(true);
            return;
        }

        RenderState.begin(RenderState.BlendMode.NORMAL, false, false);

        // Texture 0: Frame attuale del mondo
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, captureTexId);

        // Texture 1: Depth buffer di Minecraft
        DepthCaptureHelper.bindToUnit(1);

        // Texture 2: Storico dei tratti disegnati nel frame precedente
        GL13.glActiveTexture(GL13.GL_TEXTURE2);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, historyFboA.framebufferTexture);

        float continuousTime = totalTicks * 0.05f;
        float steppedTime = (float) Math.floor(continuousTime * 12.0) / 12.0f;

        shader.use();
        shader.setInt("u_texture", 0);
        shader.setInt("u_depthTexture", 1);
        shader.setInt("u_historyTexture", 2);
        shader.setVec2("u_resolution", width, height);
        shader.setFloat("u_time", continuousTime);
        shader.setFloat("u_steppedTime", steppedTime);
        shader.setFloat("u_drawSpeed", 0.38f);  // Velocita di tracciamento (~0.2s per completare un nuovo tratto)
        shader.setFloat("u_fadeSpeed", 0.68f);  // Velocita di dissolvenza scia precedente
        shader.setFloat("u_reset", needsReset ? 1.0f : 0.0f);
        shader.setFloat("u_intensity", currentIntensity);
        shader.setFloat("u_near", 0.05f);
        shader.setFloat("u_far", (float) (mc.gameSettings.renderDistanceChunks * 16));

        renderScreenQuad(width, height);

        ShaderProgram.release();
        DepthCaptureHelper.unbind(1);
        GL13.glActiveTexture(GL13.GL_TEXTURE2);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        RenderState.end();

        // Scambio dei buffer temporali
        Framebuffer temp = historyFboA;
        historyFboA = historyFboB;
        historyFboB = temp;
        this.needsReset = false;

        // =========================================================================
        // PASSAGGIO 2: Composizione Finale sullo Schermo Principale di Minecraft
        // =========================================================================
        mc.getFramebuffer().bindFramebuffer(true);

        RenderState.begin(RenderState.BlendMode.NORMAL, false, false);
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, historyFboA.framebufferTexture);

        renderScreenQuad(width, height);

        RenderState.end();
    }

    private void renderScreenQuad(int width, int height) {
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
