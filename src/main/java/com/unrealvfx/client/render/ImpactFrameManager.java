package com.unrealvfx.client.render;

import com.unrealvfx.client.vfx.VFXManager;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

/**
 * Gestore degli Impact Frames: fotogrammi ad altissimo contrasto (Manga B&W, Negativo o Hakai Purple)
 * che si attivano per 2-3 tick durante i colpi e le esplosioni cataclismatiche stile DBS: Broly / Sparking! ZERO.
 */
public class ImpactFrameManager {
    private static final ImpactFrameManager INSTANCE = new ImpactFrameManager();

    public enum Type {
        MANGA_BW(0),
        NEGATIVE_INVERT(1),
        HAKAI_PURPLE(2);

        public final int modeId;
        Type(int id) { this.modeId = id; }
    }

    private Type currentType = Type.MANGA_BW;
    private int ticksRemaining = 0;
    private static int captureTexId = -1;
    private static int lastW = -1, lastH = -1;

    public static ImpactFrameManager getInstance() {
        return INSTANCE;
    }

    private ImpactFrameManager() {}

    public void trigger(Type type, int durationTicks) {
        this.currentType = type;
        this.ticksRemaining = Math.max(1, durationTicks);
    }

    public void tick() {
        if (ticksRemaining > 0) {
            ticksRemaining--;
        }
    }

    public boolean isActive() {
        return ticksRemaining > 0;
    }

    public Type getCurrentType() {
        return currentType;
    }

    public int getTicksRemaining() {
        return ticksRemaining;
    }

    public void render() {
        if (!isActive()) return;

        Minecraft mc = Minecraft.getMinecraft();
        int width = mc.displayWidth;
        int height = mc.displayHeight;
        if (width <= 0 || height <= 0) return;

        if (captureTexId == -1 || width != lastW || height != lastH) {
            if (captureTexId == -1) captureTexId = GL11.glGenTextures();
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, captureTexId);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, width, height, 0, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, (java.nio.ByteBuffer) null);
            lastW = width;
            lastH = height;
        }

        // Cattura il frame attuale
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, captureTexId);
        GL11.glCopyTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, 0, 0, width, height);

        ShaderProgram shader = VFXManager.getInstance().getShader("impact_frame");
        if (shader == null) return;

        RenderState.begin(RenderState.BlendMode.NORMAL, false, false);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, captureTexId);

        shader.use();
        shader.setInt("u_texture", 0);
        shader.setInt("u_mode", currentType.modeId);
        shader.setVec2("u_resolution", width, height);

        renderScreenQuad(width, height);

        ShaderProgram.release();
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
