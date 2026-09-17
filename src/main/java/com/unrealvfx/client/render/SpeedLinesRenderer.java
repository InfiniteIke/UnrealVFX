package com.unrealvfx.client.render;

import com.unrealvfx.client.vfx.VFXManager;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

/**
 * Renderer per linee cinetiche radiali a schermo (Anime Speed Lines).
 * Crea la tipica accelerazione visiva manga durante la concentrazione del ki o attacchi supremi.
 */
public class SpeedLinesRenderer {
    private static final SpeedLinesRenderer INSTANCE = new SpeedLinesRenderer();

    private float intensity = 0.0f; // 0.0f a 1.0f
    private float[] color = new float[]{1.0f, 1.0f, 1.0f};

    public static SpeedLinesRenderer getInstance() {
        return INSTANCE;
    }

    private SpeedLinesRenderer() {}

    public void setIntensity(float intensity) {
        this.intensity = Math.max(0.0f, Math.min(1.0f, intensity));
    }

    public void setColor(float r, float g, float b) {
        this.color = new float[]{r, g, b};
    }

    public void tick() {
        // Decadimento naturale se non rinnovato
        if (intensity > 0.0f) {
            intensity = Math.max(0.0f, intensity - 0.04f);
        }
    }

    public void render() {
        if (intensity <= 0.01f) return;

        Minecraft mc = Minecraft.getMinecraft();
        int width = mc.displayWidth;
        int height = mc.displayHeight;
        if (width <= 0 || height <= 0) return;

        ShaderProgram shader = VFXManager.getInstance().getShader("speed_lines");
        if (shader == null) return;

        RenderState.begin(RenderState.BlendMode.ADDITIVE, false, false);

        shader.use();
        shader.setFloat("u_time", (mc.theWorld != null ? mc.theWorld.getTotalWorldTime() : 0) * 0.4f);
        shader.setFloat("u_intensity", intensity);
        shader.setVec3("u_color", color[0], color[1], color[2]);
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
