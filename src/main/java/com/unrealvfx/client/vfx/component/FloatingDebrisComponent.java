package com.unrealvfx.client.vfx.component;

import com.unrealvfx.client.render.BloomRenderer;
import com.unrealvfx.client.render.MeshCache;
import com.unrealvfx.client.render.RenderState;
import com.unrealvfx.client.render.ShaderProgram;
import com.unrealvfx.client.vfx.Easing;
import com.unrealvfx.client.vfx.VFXComponent;
import com.unrealvfx.client.vfx.VFXInstance;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Componente per frammenti di roccia e reality cubes fluttuanti in anti-gravita (Floating Debris).
 * Simula il ciclo completo a 3 fasi anime:
 * 1) Anticipation Levitation: sollevamento dal suolo con micro-tremolio sismico pre-esplosione
 * 2) Centripetal Vortex Suction: attrazione centripeta vorticosa e accelerata verso la singolarità
 * 3) Explosive Dispersal: espulsione radiale ipersonica con frammentazione e rotazione caotica
 */
public class FloatingDebrisComponent implements VFXComponent {

    public enum DebrisType {
        LOW_POLY_ROCK,
        REALITY_CUBE
    }

    public static class RockChunk {
        public float x, y, z;
        public float vy;
        public float angle;
        public float dist;
        public float rotX, rotY, rotZ;
        public float rotSpeedX, rotSpeedY, rotSpeedZ;
        public float scale;
        public int age;
        public int maxAge;
        public boolean dispersed;
        public float blastVx, blastVy, blastVz;

        public RockChunk(float x, float y, float z, float scale, int maxAge, float angle, float dist) {
            this.x = x; this.y = y; this.z = z;
            this.angle = angle;
            this.dist = dist;
            this.scale = scale;
            this.age = 0;
            this.maxAge = maxAge;
            this.vy = 0.022f;
            this.dispersed = false;
            Random r = new Random();
            this.rotX = r.nextFloat() * 360.0f;
            this.rotY = r.nextFloat() * 360.0f;
            this.rotZ = r.nextFloat() * 360.0f;
            this.rotSpeedX = (r.nextFloat() - 0.5f) * 6.0f;
            this.rotSpeedY = (r.nextFloat() - 0.5f) * 6.0f;
            this.rotSpeedZ = (r.nextFloat() - 0.5f) * 6.0f;
        }
    }

    private final List<RockChunk> rocks = new ArrayList<RockChunk>();
    private final Random rand = new Random();

    private final int maxRocks;
    private final float radius;

    private DebrisType debrisType = DebrisType.LOW_POLY_ROCK;
    private float[] color = new float[]{0.55f, 0.50f, 0.45f};
    private boolean anticipationLevitation = true;
    private boolean centripetalSuction = true;
    private boolean explosiveDispersal = true;
    private float steppedFps = 0.0f;

    public FloatingDebrisComponent(int maxRocks, float radius) {
        this.maxRocks = maxRocks;
        this.radius = radius;
    }

    @Override
    public void tick(VFXInstance instance) {
        // Se mancano rocce e l'istanza è viva, ne spawna di nuove dal terreno
        if (rocks.size() < maxRocks && rand.nextFloat() < 0.35f) {
            float angle = rand.nextFloat() * (float) Math.PI * 2.0f;
            float dist = (0.7f + rand.nextFloat() * 0.7f) * radius * instance.getScaleModifier();
            float rx = (float) Math.cos(angle) * dist;
            float rz = (float) Math.sin(angle) * dist;

            float rockScale = 0.25f + rand.nextFloat() * 0.22f;
            int maxAge = 40 + rand.nextInt(35); // Durata 2-4 secondi

            rocks.add(new RockChunk(rx, 0.05f, rz, rockScale, maxAge, angle, dist));
        }

        Iterator<RockChunk> it = rocks.iterator();
        while (it.hasNext()) {
            RockChunk rock = it.next();
            rock.age++;
            if (rock.age >= rock.maxAge) {
                it.remove();
                continue;
            }

            float life = rock.age / (float) rock.maxAge;

            if (anticipationLevitation && life < 0.35f) {
                // FASE 1: Anticipation Levitation (sollevamento con micro-tremolio sismico)
                rock.y += rock.vy;
                rock.vy = 0.022f * (1.0f - life / 0.35f);
                float tremor = (rand.nextFloat() - 0.5f) * 0.025f;
                rock.x += tremor;
                rock.z += tremor;

                rock.rotX += rock.rotSpeedX;
                rock.rotY += rock.rotSpeedY;
                rock.rotZ += rock.rotSpeedZ;
            } else if (centripetalSuction && life < 0.70f) {
                // FASE 2: Centripetal Vortex Suction (risucchio centripeto accelerato a spirale)
                rock.dist -= 0.05f * radius;
                if (rock.dist < 0.25f) rock.dist = 0.25f;
                rock.angle += 0.12f + (0.08f / Math.max(0.3f, rock.dist));
                rock.x = (float) Math.cos(rock.angle) * rock.dist;
                rock.z = (float) Math.sin(rock.angle) * rock.dist;
                rock.y += 0.012f * (float) Math.sin(rock.age * 0.25f);

                rock.rotX += rock.rotSpeedX * 2.0f;
                rock.rotY += rock.rotSpeedY * 2.0f;
                rock.rotZ += rock.rotSpeedZ * 2.0f;
            } else if (explosiveDispersal) {
                // FASE 3: Explosive Dispersal (dispersione esplosiva ipersonica radiale)
                if (!rock.dispersed) {
                    rock.dispersed = true;
                    float blastSpeed = 0.35f + rand.nextFloat() * 0.25f;
                    rock.blastVx = (float) Math.cos(rock.angle) * blastSpeed + (rand.nextFloat() - 0.5f) * 0.12f;
                    rock.blastVy = 0.18f + rand.nextFloat() * 0.20f;
                    rock.blastVz = (float) Math.sin(rock.angle) * blastSpeed + (rand.nextFloat() - 0.5f) * 0.12f;
                }
                rock.x += rock.blastVx;
                rock.y += rock.blastVy;
                rock.z += rock.blastVz;
                rock.blastVy -= 0.012f; // Gravita

                rock.rotX += rock.rotSpeedX * 4.5f;
                rock.rotY += rock.rotSpeedY * 4.5f;
                rock.rotZ += rock.rotSpeedZ * 4.5f;
            } else {
                // Movimento verticale standard
                rock.y += rock.vy;
                rock.vy = 0.025f * (float) Math.sin((1.0f - life) * Math.PI);
                rock.rotX += rock.rotSpeedX;
                rock.rotY += rock.rotSpeedY;
                rock.rotZ += rock.rotSpeedZ;
            }
        }
    }

    @Override
    public void render(VFXInstance instance, float partialTicks, float progress) {
        if (BloomRenderer.isCapturing()) return;
        if (rocks.isEmpty()) return;

        // Le rocce/cubi usano normal blending solido per sembrare vera materia
        RenderState.begin(RenderState.BlendMode.NORMAL, true, true);
        ShaderProgram.release();

        for (RockChunk rock : rocks) {
            float ageTicks = rock.age + partialTicks;
            if (steppedFps > 0.0f) {
                float stepTicks = 20.0f / steppedFps;
                ageTicks = (float) (Math.floor(ageTicks / stepTicks) * stepTicks);
            }
            float life = ageTicks / (float) rock.maxAge;

            // Scala: nasce dal suolo (0->1) e si sgretola alla fine (1->0)
            float s;
            if (life < 0.2f) {
                s = Easing.OUT_QUAD.interpolate(0.0f, rock.scale, life / 0.2f);
            } else if (life > 0.8f) {
                s = Easing.IN_QUAD.interpolate(rock.scale, 0.0f, (life - 0.8f) / 0.2f);
            } else {
                s = rock.scale;
            }

            s *= instance.getScaleModifier();

            GL11.glPushMatrix();
            GL11.glTranslated(rock.x, rock.y, rock.z);

            GL11.glRotatef(rock.rotX, 1.0f, 0.0f, 0.0f);
            GL11.glRotatef(rock.rotY, 0.0f, 1.0f, 0.0f);
            GL11.glRotatef(rock.rotZ, 0.0f, 0.0f, 1.0f);

            GL11.glScalef(s, s, s);

            GL11.glColor4f(color[0], color[1], color[2], 1.0f);
            if (debrisType == DebrisType.REALITY_CUBE) {
                MeshCache.renderCube();
            } else {
                MeshCache.renderLowPolyRock();
            }

            GL11.glPopMatrix();
        }

        RenderState.end();
    }

    public DebrisType getDebrisType() {
        return debrisType;
    }

    public FloatingDebrisComponent setDebrisType(DebrisType debrisType) {
        this.debrisType = debrisType;
        return this;
    }

    public float[] getColor() {
        return color;
    }

    public FloatingDebrisComponent setColor(float r, float g, float b) {
        this.color = new float[]{r, g, b};
        return this;
    }

    public boolean isAnticipationLevitation() {
        return anticipationLevitation;
    }

    public FloatingDebrisComponent setAnticipationLevitation(boolean anticipationLevitation) {
        this.anticipationLevitation = anticipationLevitation;
        return this;
    }

    public boolean isCentripetalSuction() {
        return centripetalSuction;
    }

    public FloatingDebrisComponent setCentripetalSuction(boolean centripetalSuction) {
        this.centripetalSuction = centripetalSuction;
        return this;
    }

    public boolean isExplosiveDispersal() {
        return explosiveDispersal;
    }

    public FloatingDebrisComponent setExplosiveDispersal(boolean explosiveDispersal) {
        this.explosiveDispersal = explosiveDispersal;
        return this;
    }

    public int getMaxRocks() {
        return maxRocks;
    }

    public float getRadius() {
        return radius;
    }

    public List<RockChunk> getRocks() {
        return rocks;
    }

    public float getSteppedFps() {
        return steppedFps;
    }

    public FloatingDebrisComponent setSteppedFps(float steppedFps) {
        this.steppedFps = steppedFps;
        return this;
    }
}
