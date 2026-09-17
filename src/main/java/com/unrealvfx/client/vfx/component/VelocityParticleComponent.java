package com.unrealvfx.client.vfx.component;

import com.unrealvfx.client.render.MeshCache;
import com.unrealvfx.client.render.RenderState;
import com.unrealvfx.client.render.ShaderProgram;
import com.unrealvfx.client.vfx.VFXComponent;
import com.unrealvfx.client.vfx.VFXInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Emettitore di particelle ad alte prestazioni:
 * - Scintille ad ago allungate sul vettore velocita (Velocity-Stretched Billboards)
 * - Mote di cancellazione della realta a cubi/frammenti
 * - Modalita risucchio gravitazionale (Suction Vortex) verso il nucleo
 */
public class VelocityParticleComponent implements VFXComponent {

    public enum ParticleType {
        NEEDLE,
        ENERGY_SPARK,
        REALITY_CUBE,
        KI_EMBER
    }

    public static class Particle {
        public float x, y, z;
        public float vx, vy, vz;
        public int age;
        public int maxAge;
        public float size;
        public float r, g, b;

        public Particle(float x, float y, float z, float vx, float vy, float vz, int maxAge, float size, float r, float g, float b) {
            this.x = x; this.y = y; this.z = z;
            this.vx = vx; this.vy = vy; this.vz = vz;
            this.age = 0;
            this.maxAge = maxAge;
            this.size = size;
            this.r = r; this.g = g; this.b = b;
        }
    }

    private final List<Particle> particles = new ArrayList<>();
    private final Random rand = new Random();

    private ParticleType particleType = ParticleType.NEEDLE;
    private int spawnRate = 4;
    private float emissionRadius = 0.9f;
    private float baseSpeed = 0.35f;
    private float stretchFactor = 3.2f;
    private float[] color = new float[]{1.0f, 0.95f, 0.3f};
    private boolean swirl = true;
    private boolean suction = false;

    public VelocityParticleComponent(int spawnRate, float emissionRadius) {
        this.spawnRate = spawnRate;
        this.emissionRadius = emissionRadius;
    }

    @Override
    public void tick(VFXInstance instance) {
        for (int i = 0; i < spawnRate; i++) {
            float pr = color[0], pg = color[1], pb = color[2];
            int override = instance.getColorOverride();
            if (override != -1) {
                pr = ((override >> 16) & 0xFF) / 255.0f;
                pg = ((override >> 8) & 0xFF) / 255.0f;
                pb = (override & 0xFF) / 255.0f;
            }

            if (suction) {
                // Genera in guscio sferico ed equatoriale ampio attorno alla singolarità
                float u = rand.nextFloat() * 1.6f - 0.8f;
                u = u * u * u; // Concentrazione naturale verso il piano equatoriale del disco
                float theta = rand.nextFloat() * (float) Math.PI * 2.0f;
                float r = (float) Math.sqrt(Math.max(0.0f, 1.0f - u * u));
                float dist = (0.75f + rand.nextFloat() * 1.25f) * emissionRadius * instance.getScaleModifier();

                float px = r * (float) Math.cos(theta) * dist;
                float py = u * dist;
                float pz = r * (float) Math.sin(theta) * dist;

                // Vettore iniziale verso l'origine ad alta velocità
                float invDist = 1.0f / Math.max(0.1f, dist);
                float inwardSpeed = baseSpeed * (1.1f + rand.nextFloat() * 0.7f);
                float vx = -px * invDist * inwardSpeed;
                float vy = -py * invDist * inwardSpeed;
                float vz = -pz * invDist * inwardSpeed;

                if (swirl) {
                    float swirlVel = 0.40f * (0.8f + rand.nextFloat() * 0.4f);
                    vx += -pz * invDist * swirlVel * (float) Math.sqrt(dist);
                    vz += px * invDist * swirlVel * (float) Math.sqrt(dist);
                }

                int life = 24 + rand.nextInt(18);
                float size = 0.12f + rand.nextFloat() * 0.12f;
                particles.add(new Particle(px, py, pz, vx, vy, vz, life, size, pr, pg, pb));
            } else {
                // Eiezione classica verso l'alto
                float angle = rand.nextFloat() * (float) Math.PI * 2.0f;
                float dist = (0.2f + rand.nextFloat() * 0.8f) * emissionRadius * instance.getScaleModifier();

                float px = (float) Math.cos(angle) * dist;
                float py = 0.1f + rand.nextFloat() * 0.3f;
                float pz = (float) Math.sin(angle) * dist;

                float upward = baseSpeed * (0.8f + rand.nextFloat() * 0.6f);
                float vx = (rand.nextFloat() - 0.5f) * 0.08f;
                float vz = (rand.nextFloat() - 0.5f) * 0.08f;

                if (swirl) {
                    vx += -pz * 0.15f;
                    vz += px * 0.15f;
                }

                int life = 14 + rand.nextInt(12);
                float size = 0.12f + rand.nextFloat() * 0.08f;
                particles.add(new Particle(px, py, pz, vx, upward, vz, life, size, pr, pg, pb));
            }
        }

        Iterator<Particle> it = particles.iterator();
        while (it.hasNext()) {
            Particle p = it.next();
            p.age++;
            if (p.age >= p.maxAge) {
                it.remove();
                continue;
            }

            p.x += p.vx;
            p.y += p.vy;
            p.z += p.vz;

            if (suction) {
                // Accelerazione gravitazionale relativistica: a = G / dist^1.35
                float currentDistSq = p.x * p.x + p.y * p.y + p.z * p.z;
                if (currentDistSq < 0.25f) {
                    // Inghiottito dall'orizzonte degli eventi della singolarità
                    it.remove();
                    continue;
                }
                float currentDist = (float) Math.sqrt(currentDistSq);
                float invDist = 1.0f / currentDist;

                // Spinta gravitazionale intensa che cresce a velocità vertiginosa vicino all'orizzonte
                float accel = Math.min(1.2f, 0.48f / (float) Math.pow(Math.max(0.5f, currentDist), 1.25f));
                p.vx += -p.x * invDist * accel;
                p.vy += -p.y * invDist * accel;
                p.vz += -p.z * invDist * accel;

                if (swirl) {
                    // Accelerazione orbitale kepleriana: aumento vertiginoso della rotazione a spirale
                    float swirlAccel = Math.min(0.75f, 0.35f / (float) Math.sqrt(Math.max(0.4f, currentDist)));
                    p.vx += -p.z * invDist * swirlAccel;
                    p.vz += p.x * invDist * swirlAccel;
                }
            } else {
                p.vy *= 1.02f;
            }
        }
    }

    @Override
    public void render(VFXInstance instance, float partialTicks, float progress) {
        if (particles.isEmpty()) return;

        Entity viewEntity = Minecraft.getMinecraft().renderViewEntity;
        if (viewEntity == null) return;

        RenderState.begin(RenderState.BlendMode.ADDITIVE, false, false);
        ShaderProgram.release();
        GL11.glDisable(GL11.GL_TEXTURE_2D);

        for (Particle p : particles) {
            float interpX = p.x + p.vx * partialTicks;
            float interpY = p.y + p.vy * partialTicks;
            float interpZ = p.z + p.vz * partialTicks;

            float lifeRatio = (p.age + partialTicks) / (float) p.maxAge;
            float alpha = (1.0f - lifeRatio) * (1.0f - lifeRatio);

            GL11.glPushMatrix();
            GL11.glTranslated(interpX, interpY, interpZ);

            if (particleType == ParticleType.REALITY_CUBE) {
                // Billboard camera-facing cube
                GL11.glRotatef(-viewEntity.rotationYaw, 0.0f, 1.0f, 0.0f);
                GL11.glRotatef(viewEntity.rotationPitch, 1.0f, 0.0f, 0.0f);
                GL11.glScalef(p.size * 0.8f, p.size * 0.8f, p.size * 0.8f);
                GL11.glColor4f(p.r, p.g, p.b, alpha);
                MeshCache.renderBillboardQuad();
            } else if (particleType == ParticleType.KI_EMBER) {
                // Billboard camera-facing rising ki ember
                GL11.glRotatef(-viewEntity.rotationYaw, 0.0f, 1.0f, 0.0f);
                GL11.glRotatef(viewEntity.rotationPitch, 1.0f, 0.0f, 0.0f);
                GL11.glScalef(p.size * 0.9f, p.size * 1.4f, p.size * 0.9f);
                GL11.glColor4f(p.r, p.g, p.b, alpha);
                MeshCache.renderBillboardQuad();
            } else {
                // Stretched needle quad
                float speed = (float) Math.sqrt(p.vx * p.vx + p.vy * p.vy + p.vz * p.vz);
                float lengthScale = p.size * (1.0f + speed * stretchFactor);
                float widthScale = p.size * 0.7f;

                float pitch = (float) Math.toDegrees(Math.asin(-p.vy / (speed + 0.0001f)));
                float yaw = (float) Math.toDegrees(Math.atan2(p.vx, p.vz));

                GL11.glRotatef(yaw, 0.0f, 1.0f, 0.0f);
                GL11.glRotatef(pitch, 1.0f, 0.0f, 0.0f);
                GL11.glScalef(widthScale, lengthScale, widthScale);

                GL11.glColor4f(p.r, p.g, p.b, alpha);
                MeshCache.renderNeedleQuad();
            }

            GL11.glPopMatrix();
        }

        RenderState.end();
    }

    public VelocityParticleComponent setColor(float r, float g, float b) {
        this.color = new float[]{r, g, b};
        return this;
    }

    public VelocityParticleComponent setSpawnRate(int rate) {
        this.spawnRate = rate;
        return this;
    }

    public VelocityParticleComponent setSwirl(boolean swirl) {
        this.swirl = swirl;
        return this;
    }

    public VelocityParticleComponent setSuction(boolean suction) {
        this.suction = suction;
        return this;
    }

    public VelocityParticleComponent setParticleType(ParticleType type) {
        this.particleType = type;
        return this;
    }

    public VelocityParticleComponent setBaseSpeed(float baseSpeed) {
        this.baseSpeed = baseSpeed;
        return this;
    }

    public VelocityParticleComponent setStretchFactor(float stretchFactor) {
        this.stretchFactor = stretchFactor;
        return this;
    }

    public ParticleType getParticleType() {
        return particleType;
    }

    public int getSpawnRate() {
        return spawnRate;
    }

    public float getEmissionRadius() {
        return emissionRadius;
    }

    public float getBaseSpeed() {
        return baseSpeed;
    }

    public boolean isSwirl() {
        return swirl;
    }

    public boolean isSuction() {
        return suction;
    }
}
