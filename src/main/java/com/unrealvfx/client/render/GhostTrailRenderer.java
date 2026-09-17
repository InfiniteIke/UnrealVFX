package com.unrealvfx.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import com.unrealvfx.client.vfx.VFXManager;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Gestore delle Immagini Residue (After-Images / Zanzoken).
 * Quando il giocatore si muove ad alta velocita o durante gli scatti ki,
 * lascia dietro di se copie spettrali semitrasparenti del modello che sfumano gradualmente.
 */
public class GhostTrailRenderer {
    private static final GhostTrailRenderer INSTANCE = new GhostTrailRenderer();

    public static class GhostSnapshot {
        public double x, y, z;
        public float yawOffset;
        public float yawHead;
        public float pitch;
        public float limbSwing;
        public float limbSwingAmount;
        public int age;
        public int maxAge;
        public float[] color;

        public GhostSnapshot(double x, double y, double z, float yawOffset, float yawHead, float pitch, float limbSwing, float limbSwingAmount, int maxAge, float[] color) {
            this.x = x; this.y = y; this.z = z;
            this.yawOffset = yawOffset;
            this.yawHead = yawHead;
            this.pitch = pitch;
            this.limbSwing = limbSwing;
            this.limbSwingAmount = limbSwingAmount;
            this.age = 0;
            this.maxAge = maxAge;
            this.color = color;
        }
    }

    private final List<GhostSnapshot> ghosts = new ArrayList<>();
    private int spawnCooldown = 0;
    private boolean forceZanzoken = false;

    public static GhostTrailRenderer getInstance() {
        return INSTANCE;
    }

    private GhostTrailRenderer() {}

    public void setForceZanzoken(boolean force) {
        this.forceZanzoken = force;
    }

    public void tick() {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        if (player != null && player.worldObj != null) {
            double speed = Math.sqrt(player.motionX * player.motionX + player.motionZ * player.motionZ);

            // Genera snapshot se il player sprinta, vola veloce o se è forzato dall'effetto
            if (forceZanzoken || (player.isSprinting() && speed > 0.15)) {
                spawnCooldown--;
                if (spawnCooldown <= 0) {
                    spawnCooldown = 2; // Uno snapshot ogni 2 tick

                    float[] color = new float[]{1.0f, 0.9f, 0.3f}; // Default gold
                    if (forceZanzoken) {
                        color = new float[]{0.85f, 0.1f, 0.85f}; // Magenta Zanzoken
                    }

                    ghosts.add(new GhostSnapshot(
                        player.posX, player.posY, player.posZ,
                        player.renderYawOffset,
                        player.rotationYawHead,
                        player.rotationPitch,
                        player.limbSwing,
                        player.limbSwingAmount,
                        12, // Dura 12 tick (circa 0.6s)
                        color
                    ));
                }
            }
        }

        Iterator<GhostSnapshot> it = ghosts.iterator();
        while (it.hasNext()) {
            GhostSnapshot g = it.next();
            g.age++;
            if (g.age >= g.maxAge) {
                it.remove();
            }
        }
    }

    public void render(float partialTicks) {
        if (ghosts.isEmpty()) return;

        Minecraft mc = Minecraft.getMinecraft();
        Entity viewEntity = mc.renderViewEntity;
        if (viewEntity == null) return;

        RenderPlayer renderPlayer = (RenderPlayer) RenderManager.instance.entityRenderMap.get(EntityPlayer.class);
        if (renderPlayer == null) return;

        double camX = viewEntity.lastTickPosX + (viewEntity.posX - viewEntity.lastTickPosX) * partialTicks;
        double camY = viewEntity.lastTickPosY + (viewEntity.posY - viewEntity.lastTickPosY) * partialTicks;
        double camZ = viewEntity.lastTickPosZ + (viewEntity.posZ - viewEntity.lastTickPosZ) * partialTicks;

        RenderState.begin(RenderState.BlendMode.ADDITIVE, false, false);
        ShaderProgram shader = VFXManager.getInstance().getShader("body_glow");
        if (shader != null) {
            shader.use();
        }

        ModelBiped model = renderPlayer.modelBipedMain;

        for (GhostSnapshot g : ghosts) {
            float progress = (g.age + partialTicks) / (float) g.maxAge;
            float alpha = (1.0f - progress) * 0.75f;

            if (shader != null) {
                shader.setFloat("u_time", (g.age + partialTicks) * 0.1f);
                shader.setVec4("u_color", g.color[0], g.color[1], g.color[2], alpha);
            }

            GL11.glPushMatrix();
            GL11.glTranslated(g.x - camX, g.y - camY + 1.62, g.z - camZ); // Offset altezza corretta

            GL11.glRotatef(180.0f - g.yawOffset, 0.0f, 1.0f, 0.0f);
            GL11.glScalef(-1.0f, -1.0f, 1.0f); // Inversione standard del modello player Minecraft
            GL11.glTranslatef(0.0f, -1.5f, 0.0f);

            GL11.glColor4f(g.color[0], g.color[1], g.color[2], alpha);

            model.render(
                mc.thePlayer,
                g.limbSwing,
                g.limbSwingAmount,
                g.age + partialTicks,
                g.yawHead - g.yawOffset,
                g.pitch,
                0.0625F
            );

            GL11.glPopMatrix();
        }

        if (shader != null) {
            ShaderProgram.release();
        }
        RenderState.end();
    }

    public synchronized void clear() {
        ghosts.clear();
        spawnCooldown = 0;
        forceZanzoken = false;
    }
}
