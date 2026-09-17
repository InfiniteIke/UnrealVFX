package com.unrealvfx.client.vfx.component;

import com.unrealvfx.client.render.CurvedBeamMesh;
import com.unrealvfx.client.render.DepthCaptureHelper;
import com.unrealvfx.client.render.MeshCache;
import com.unrealvfx.client.render.RenderState;
import com.unrealvfx.client.render.ShaderProgram;
import com.unrealvfx.client.vfx.Easing;
import com.unrealvfx.client.vfx.VFXComponent;
import com.unrealvfx.client.vfx.VFXInstance;
import com.unrealvfx.client.vfx.VFXManager;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

import java.util.Random;

/**
 * Componente per il rendering di raggi energetici continui (Beam / Laser) in puro stile anime.
 * Ispirato all'architettura a 3 fasi di CustomNPC+ (Charging Windup, Tail Anchor, Beam Trail, Head Impact)
 * combinato con shader GLSL a scorrimento supersonico del plasma e nucleo bianco cel-shaded.
 */
public class BeamComponent implements VFXComponent {

    private float length = 20.0f;
    private float width = 0.55f;
    private int chargeTicks = 12; // Durata della fase di carica prima del rilascio
    private float[] color = new float[]{0.2f, 0.65f, 1.0f}; // Azzurro/Ciano default

    private boolean hasHeadOrb = true;
    private boolean hasTailOrb = true;
    private boolean hasLightning = true;
    private boolean hasHelicalRibbons = true;
    private float ribbonRadius = 0.85f;
    private float ribbonSpeed = 8.0f;
    private float[] ribbonColor = null;

    private final Random rand = new Random();

    public BeamComponent(float length, float width) {
        this.length = length;
        this.width = width;
    }

    @Override
    public void tick(VFXInstance instance) {
    }

    @Override
    public void render(VFXInstance instance, float partialTicks, float progress) {
        Entity viewEntity = Minecraft.getMinecraft().renderViewEntity;
        if (viewEntity == null) return;

        int age = instance.getAge();
        float exactAge = age + partialTicks;

        // Se agganciato a un'entita (es. giocatore), calcola direzione di mira
        Entity attached = instance.getAttachedEntity();
        float yaw = 0.0f;
        float pitch = 0.0f;

        if (attached != null) {
            yaw = attached.rotationYaw;
            pitch = attached.rotationPitch;
        }

        // FASE 1: CARICA / WINDUP (Sfera che cresce tra le mani con scariche elettriche)
        if (chargeTicks > 0 && exactAge < chargeTicks) {
            float chargeProgress = exactAge / (float) chargeTicks;
            renderCharging(instance, chargeProgress, partialTicks);
            return;
        }

        // FASE 2: RAGGIO ATTIVO E RILASCIATO
        float beamProgress = chargeTicks > 0 ? ((exactAge - chargeTicks) / (float) (instance.getBlueprint().getDuration() - chargeTicks)) : progress;
        float currentWidth = width * instance.getScaleModifier();

        // Leggera espansione iniziale all'uscita (burst)
        if (chargeTicks > 0 && exactAge < chargeTicks + 4) {
            currentWidth *= 1.35f;
        }

        GL11.glPushMatrix();

        // Orienta il raggio lungo la direzione di mira
        GL11.glRotatef(-yaw, 0.0f, 1.0f, 0.0f);
        GL11.glRotatef(pitch, 1.0f, 0.0f, 0.0f);

        float[] effColor = getEffectiveColor(instance);

        // 1. CORPO DEL RAGGIO A SCORRIMENTO UV CON BISHOP FRAME / RMF
        ShaderProgram beamShader = VFXManager.getInstance().getShader("energy_beam");
        if (beamShader != null) {
            beamShader.use();
            beamShader.setFloat("u_time", exactAge * 0.05f);
            beamShader.setFloat("u_progress", beamProgress);
            beamShader.setVec4("u_color", effColor[0], effColor[1], effColor[2], 1.0f);
            beamShader.setFloat("u_spikeAmount", 0.18f);
            DepthCaptureHelper.setupShaderDepthUniforms(beamShader, 1.2f);
        }

        RenderState.begin(RenderState.BlendMode.ADDITIVE, false, false);

        // Corpo esterno di plasma con micro-turbolenza organica RMF
        CurvedBeamMesh.renderStraightBeam(currentWidth, length, 16, 2.5f, 0.08f, exactAge * 0.05f);

        // 2. STRATO INTERNO BIANCO PIU STRETTO (HOT CORE ESTREMO)
        if (beamShader != null) {
            beamShader.setVec4("u_color", 1.0f, 1.0f, 1.0f, 0.95f);
            beamShader.setFloat("u_spikeAmount", 0.06f);
        }
        CurvedBeamMesh.renderStraightBeam(currentWidth * 0.42f, length, 16, 2.5f, 0.03f, exactAge * 0.05f);

        if (beamShader != null) {
            ShaderProgram.release();
        }

        RenderState.end();

        // 3. ANCORA ALLA BASE (TAIL ORB) SULLE MANI DEL LANCIARE
        if (hasTailOrb) {
            renderAnchorOrb(currentWidth * 1.5f, exactAge, effColor);
        }

        // 4. TESTA D'IMPATTO (HEAD ORB) AL TERMINE DEL RAGGIO
        if (hasHeadOrb) {
            GL11.glPushMatrix();
            GL11.glTranslated(0.0, 0.0, length);
            renderImpactHead(currentWidth * 1.8f, exactAge, effColor);
            GL11.glPopMatrix();
        }

        // 5. FULMINI CHE SERPEGGIANO LUNGO IL RAGGIO
        if (hasLightning) {
            renderBeamLightning(length, currentWidth, exactAge);
        }

        // 6. NASTRI A DOPPIA ELICA (DOUBLE HELICAL SPIRAL RIBBONS) ATTORNO AL MANTELLO
        if (hasHelicalRibbons) {
            renderHelicalRibbons(length, currentWidth, exactAge, effColor);
        }

        GL11.glPopMatrix();
    }

    private void renderCharging(VFXInstance instance, float chargeProgress, float partialTicks) {
        float scale = (width * 1.8f) * chargeProgress * instance.getScaleModifier();
        float[] effColor = getEffectiveColor(instance);

        ShaderProgram orbShader = VFXManager.getInstance().getShader("anime_energy_orb");
        if (orbShader != null) {
            orbShader.use();
            orbShader.setFloat("u_time", (instance.getAge() + partialTicks) * 0.05f);
            orbShader.setFloat("u_progress", chargeProgress);
            orbShader.setVec4("u_color", effColor[0], effColor[1], effColor[2], 1.0f);
            DepthCaptureHelper.setupShaderDepthUniforms(orbShader, 1.0f);
        }

        RenderState.begin(RenderState.BlendMode.ADDITIVE, false, false);

        GL11.glPushMatrix();
        GL11.glScalef(scale, scale, scale);
        MeshCache.renderSphere();
        GL11.glPopMatrix();

        // Hot white core
        GL11.glPushMatrix();
        GL11.glScalef(scale * 0.55f, scale * 0.55f, scale * 0.55f);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        MeshCache.renderSphere();
        GL11.glPopMatrix();

        if (orbShader != null) {
            ShaderProgram.release();
        }

        RenderState.end();
    }

    private float[] getEffectiveColor(VFXInstance instance) {
        int override = instance.getColorOverride();
        if (override != -1) {
            float r = ((override >> 16) & 0xFF) / 255.0f;
            float g = ((override >> 8) & 0xFF) / 255.0f;
            float b = (override & 0xFF) / 255.0f;
            return new float[]{r, g, b};
        }
        return color;
    }

    private void renderAnchorOrb(float orbRadius, float exactAge, float[] effColor) {
        ShaderProgram orbShader = VFXManager.getInstance().getShader("anime_energy_orb");
        if (orbShader != null) {
            orbShader.use();
            orbShader.setFloat("u_time", exactAge * 0.05f);
            orbShader.setFloat("u_progress", 0.0f);
            orbShader.setVec4("u_color", effColor[0], effColor[1], effColor[2], 1.0f);
            DepthCaptureHelper.setupShaderDepthUniforms(orbShader, 1.0f);
        }

        RenderState.begin(RenderState.BlendMode.ADDITIVE, false, false);

        GL11.glPushMatrix();
        GL11.glScalef(orbRadius, orbRadius, orbRadius);
        MeshCache.renderSphere();
        GL11.glPopMatrix();

        if (orbShader != null) {
            ShaderProgram.release();
        }

        RenderState.end();
    }

    private void renderImpactHead(float headRadius, float exactAge, float[] effColor) {
        ShaderProgram orbShader = VFXManager.getInstance().getShader("anime_energy_orb");
        if (orbShader != null) {
            orbShader.use();
            orbShader.setFloat("u_time", exactAge * 0.05f);
            orbShader.setFloat("u_progress", 1.0f);
            orbShader.setVec4("u_color", effColor[0], effColor[1], effColor[2], 1.0f);
            DepthCaptureHelper.setupShaderDepthUniforms(orbShader, 1.2f);
        }

        RenderState.begin(RenderState.BlendMode.ADDITIVE, false, false);

        GL11.glPushMatrix();
        GL11.glScalef(headRadius, headRadius, headRadius);
        MeshCache.renderSphere();
        GL11.glPopMatrix();

        if (orbShader != null) {
            ShaderProgram.release();
        }

        RenderState.end();
    }

    private void renderBeamLightning(float beamLen, float beamRadius, float exactAge) {
        RenderState.begin(RenderState.BlendMode.ADDITIVE, false, false);
        ShaderProgram.release();

        GL11.glColor4f(1.0f, 1.0f, 1.0f, 0.9f);
        GL11.glLineWidth(2.5f);
        GL11.glBegin(GL11.GL_LINES);

        int arcs = 4;
        for (int a = 0; a < arcs; a++) {
            float startZ = rand.nextFloat() * (beamLen * 0.8f);
            float endZ = startZ + 3.0f + rand.nextFloat() * 4.0f;
            float angle = rand.nextFloat() * (float) Math.PI * 2.0f;

            float x1 = (float) Math.cos(angle) * beamRadius * 1.15f;
            float y1 = (float) Math.sin(angle) * beamRadius * 1.15f;

            float x2 = (float) Math.cos(angle + 0.8f) * beamRadius * 1.15f;
            float y2 = (float) Math.sin(angle + 0.8f) * beamRadius * 1.15f;

            float mx = (x1 + x2) * 0.5f + (rand.nextFloat() - 0.5f) * 0.35f;
            float my = (y1 + y2) * 0.5f + (rand.nextFloat() - 0.5f) * 0.35f;
            float mz = (startZ + endZ) * 0.5f;

            GL11.glVertex3f(x1, y1, startZ);
            GL11.glVertex3f(mx, my, mz);
            GL11.glVertex3f(mx, my, mz);
            GL11.glVertex3f(x2, y2, endZ);
        }

        GL11.glEnd();
        RenderState.end();
    }

    public BeamComponent setColor(float r, float g, float b) {
        this.color = new float[]{r, g, b};
        return this;
    }

    public BeamComponent setChargeTicks(int ticks) {
        this.chargeTicks = ticks;
        return this;
    }

    public BeamComponent setHasHeadOrb(boolean hasHeadOrb) {
        this.hasHeadOrb = hasHeadOrb;
        return this;
    }

    public BeamComponent setHasTailOrb(boolean hasTailOrb) {
        this.hasTailOrb = hasTailOrb;
        return this;
    }

    public BeamComponent setHasLightning(boolean hasLightning) {
        this.hasLightning = hasLightning;
        return this;
    }

    private void renderHelicalRibbons(float beamLength, float beamWidth, float exactAge, float[] effColor) {
        float[] useColor = (ribbonColor != null) ? ribbonColor : effColor;
        RenderState.begin(RenderState.BlendMode.ADDITIVE, false, false);
        GL11.glLineWidth(3.0f);
        ShaderProgram shader = VFXManager.getInstance().getShader("universal_energy");
        if (shader != null) {
            shader.use();
            shader.setFloat("u_time", exactAge * 0.05f);
            shader.setVec4("u_color", useColor[0], useColor[1], useColor[2], 0.85f);
            shader.setVec3("u_hotCoreColor", 1.0f, 1.0f, 1.0f);
            shader.setFloat("u_bloomMultiplier", 2.0f);
        } else {
            GL11.glColor4f(useColor[0], useColor[1], useColor[2], 0.85f);
        }

        float radius = (beamWidth * 1.15f) + ribbonRadius * 0.4f;
        int segments = Math.max(16, (int) (beamLength * 1.8f));
        float stepZ = beamLength / (float) segments;
        float pitchRate = 0.55f;
        float rot = exactAge * ribbonSpeed * 0.1f;

        // Elica 1 (fase 0)
        GL11.glBegin(GL11.GL_LINE_STRIP);
        for (int i = 0; i <= segments; i++) {
            float z = i * stepZ;
            float angle = z * pitchRate + rot;
            float cosA = (float) Math.cos(angle);
            float sinA = (float) Math.sin(angle);
            float x = cosA * radius;
            float y = sinA * radius;
            GL11.glNormal3f(cosA, sinA, 0.0f);
            GL11.glTexCoord2f(z / beamLength, 0.5f);
            GL11.glVertex3f(x, y, z);
        }
        GL11.glEnd();

        // Elica 2 (fase sfasata di 180 gradi - PI)
        GL11.glBegin(GL11.GL_LINE_STRIP);
        for (int i = 0; i <= segments; i++) {
            float z = i * stepZ;
            float angle = z * pitchRate + rot + (float) Math.PI;
            float cosA = (float) Math.cos(angle);
            float sinA = (float) Math.sin(angle);
            float x = cosA * radius;
            float y = sinA * radius;
            GL11.glNormal3f(cosA, sinA, 0.0f);
            GL11.glTexCoord2f(z / beamLength, 0.5f);
            GL11.glVertex3f(x, y, z);
        }
        GL11.glEnd();

        if (shader != null) {
            ShaderProgram.release();
        }
        RenderState.end();
    }

    public boolean hasHelicalRibbons() {
        return hasHelicalRibbons;
    }

    public BeamComponent setHasHelicalRibbons(boolean hasHelicalRibbons) {
        this.hasHelicalRibbons = hasHelicalRibbons;
        return this;
    }

    public float getRibbonRadius() {
        return ribbonRadius;
    }

    public BeamComponent setRibbonRadius(float ribbonRadius) {
        this.ribbonRadius = ribbonRadius;
        return this;
    }

    public float getRibbonSpeed() {
        return ribbonSpeed;
    }

    public BeamComponent setRibbonSpeed(float ribbonSpeed) {
        this.ribbonSpeed = ribbonSpeed;
        return this;
    }

    public float[] getRibbonColor() {
        return ribbonColor;
    }

    public BeamComponent setRibbonColor(float r, float g, float b) {
        this.ribbonColor = new float[]{r, g, b};
        return this;
    }
}
