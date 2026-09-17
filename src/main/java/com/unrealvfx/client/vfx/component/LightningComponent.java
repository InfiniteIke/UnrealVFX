package com.unrealvfx.client.vfx.component;

import com.unrealvfx.client.render.RenderState;
import com.unrealvfx.client.render.ShaderProgram;
import com.unrealvfx.client.vfx.VFXComponent;
import com.unrealvfx.client.vfx.VFXInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Generatore procedurale di scariche elettriche a nastro 3D (Ribbon Lightning).
 * Supporta modalità multiple: SURFACE_SPHERE (aderente a superficie sferica con archi planetari/Hakai),
 * TARGET_DIRECTED (scarica mirata tra due punti), CYLINDER (aura ascendente) e CHAOTIC.
 * Risolve i bug di stitching dei triangoli (glBegin/glEnd per arco) e la proiezione esatta
 * verso la telecamera in spazio locale.
 */
public class LightningComponent implements VFXComponent {

    public enum LightningMode {
        SURFACE_SPHERE,
        TARGET_DIRECTED,
        CYLINDER,
        CHAOTIC
    }

    public static class ArcSegment {
        public final float x, y, z;
        public ArcSegment(float x, float y, float z) {
            this.x = x; this.y = y; this.z = z;
        }
    }

    public static class LightningArc {
        public final List<ArcSegment> points = new ArrayList<>();
        public final List<LightningArc> branches = new ArrayList<>();
        public int life;
        public int maxLife;

        public LightningArc(int maxLife) {
            this.maxLife = maxLife;
            this.life = maxLife;
        }
    }

    private LightningMode mode = LightningMode.CYLINDER;
    private final int arcCount;
    private final float radius;
    private final float height;
    private float[] color = new float[]{0.75f, 0.35f, 1.0f};
    private float[] coreColor = new float[]{1.0f, 1.0f, 1.0f};
    private float width = 0.10f;
    private int subdivisions = 4;
    private float roughness = 0.35f;
    private int branchForks = 1;
    private float forkChance = 0.35f;
    private float glowMultiplier = 2.0f;
    private float offsetX = 0.0f, offsetY = 0.0f, offsetZ = 0.0f;
    private float[] targetStart = new float[]{0.0f, 0.0f, 0.0f};
    private float[] targetEnd = new float[]{0.0f, -5.0f, 0.0f};

    private final List<LightningArc> arcs = new ArrayList<>();
    private final Random rand = new Random();

    public LightningComponent(int arcCount, float radius, float height) {
        this.arcCount = arcCount;
        this.radius = radius;
        this.height = height;
    }

    @Override
    public void tick(VFXInstance instance) {
        arcs.removeIf(arc -> {
            arc.life--;
            return arc.life <= 0;
        });

        while (arcs.size() < arcCount) {
            arcs.add(generateArc(instance));
        }
    }

    private LightningArc generateArc(VFXInstance instance) {
        int life = 2 + rand.nextInt(3);
        LightningArc arc = new LightningArc(life);

        float currentRadius = radius * instance.getScaleModifier();

        if (mode == LightningMode.SURFACE_SPHERE) {
            // Generazione sulla superficie della sfera
            float u1 = rand.nextFloat() * 2.0f - 1.0f;
            float th1 = rand.nextFloat() * (float) (Math.PI * 2.0);
            float r1 = (float) Math.sqrt(Math.max(0.0f, 1.0f - u1 * u1));

            float x1 = r1 * (float) Math.cos(th1) * currentRadius;
            float y1 = u1 * currentRadius;
            float z1 = r1 * (float) Math.sin(th1) * currentRadius;

            // Punto finale vicino (arco superficiale serpeggiante)
            float offsetAngle = (rand.nextFloat() - 0.5f) * 1.5f;
            float th2 = th1 + offsetAngle;
            float u2 = Math.max(-0.99f, Math.min(0.99f, u1 + (rand.nextFloat() - 0.5f) * 0.7f));
            float r2 = (float) Math.sqrt(Math.max(0.0f, 1.0f - u2 * u2));

            float x2 = r2 * (float) Math.cos(th2) * currentRadius;
            float y2 = u2 * currentRadius;
            float z2 = r2 * (float) Math.sin(th2) * currentRadius;

            List<ArcSegment> pts = new ArrayList<>();
            pts.add(new ArcSegment(x1, y1, z1));
            pts.add(new ArcSegment(x2, y2, z2));

            float stepRoughness = currentRadius * roughness * 0.25f;
            for (int step = 0; step < subdivisions; step++) {
                List<ArcSegment> nextPts = new ArrayList<>();
                for (int i = 0; i < pts.size() - 1; i++) {
                    ArcSegment a = pts.get(i);
                    ArcSegment b = pts.get(i + 1);

                    float mx = (a.x + b.x) * 0.5f + (rand.nextFloat() - 0.5f) * stepRoughness;
                    float my = (a.y + b.y) * 0.5f + (rand.nextFloat() - 0.5f) * stepRoughness;
                    float mz = (a.z + b.z) * 0.5f + (rand.nextFloat() - 0.5f) * stepRoughness;

                    // Vincolo geometrico: riproiezione diretta sulla superficie sferica
                    float mLen = (float) Math.sqrt(mx * mx + my * my + mz * mz);
                    if (mLen > 0.0001f) {
                        float radialJitter = (rand.nextFloat() - 0.5f) * stepRoughness * 0.25f;
                        float targetR = currentRadius + radialJitter;
                        mx = (mx / mLen) * targetR;
                        my = (my / mLen) * targetR;
                        mz = (mz / mLen) * targetR;
                    }

                    nextPts.add(a);
                    ArcSegment mid = new ArcSegment(mx, my, mz);
                    nextPts.add(mid);

                    // Diramazioni (branch forks) superficiali
                    if (branchForks > 0 && step == 1 && rand.nextFloat() < forkChance) {
                        LightningArc branch = createSurfaceBranch(mid, currentRadius, stepRoughness, life);
                        if (branch != null) arc.branches.add(branch);
                    }
                }
                nextPts.add(pts.get(pts.size() - 1));
                pts = nextPts;
                stepRoughness *= 0.55f;
            }
            arc.points.addAll(pts);

        } else if (mode == LightningMode.TARGET_DIRECTED) {
            // Scarica mirata da targetStart a targetEnd
            float x1 = targetStart[0];
            float y1 = targetStart[1];
            float z1 = targetStart[2];

            float x2 = targetEnd[0];
            float y2 = targetEnd[1];
            float z2 = targetEnd[2];

            List<ArcSegment> pts = new ArrayList<>();
            pts.add(new ArcSegment(x1, y1, z1));
            pts.add(new ArcSegment(x2, y2, z2));

            float dist = (float) Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1) + (z2 - z1) * (z2 - z1));
            float stepRoughness = Math.max(0.5f, dist * roughness * 0.35f);

            for (int step = 0; step < subdivisions; step++) {
                List<ArcSegment> nextPts = new ArrayList<>();
                for (int i = 0; i < pts.size() - 1; i++) {
                    ArcSegment a = pts.get(i);
                    ArcSegment b = pts.get(i + 1);

                    float mx = (a.x + b.x) * 0.5f + (rand.nextFloat() - 0.5f) * stepRoughness;
                    float my = (a.y + b.y) * 0.5f + (rand.nextFloat() - 0.5f) * stepRoughness;
                    float mz = (a.z + b.z) * 0.5f + (rand.nextFloat() - 0.5f) * stepRoughness;

                    nextPts.add(a);
                    ArcSegment mid = new ArcSegment(mx, my, mz);
                    nextPts.add(mid);

                    if (branchForks > 0 && step == 1 && rand.nextFloat() < forkChance) {
                        LightningArc branch = createDirectedBranch(mid, b, stepRoughness, life);
                        if (branch != null) arc.branches.add(branch);
                    }
                }
                nextPts.add(pts.get(pts.size() - 1));
                pts = nextPts;
                stepRoughness *= 0.52f;
            }
            arc.points.addAll(pts);

        } else {
            // Modalità CYLINDER o CHAOTIC (per aure ki o scariche libere)
            float startAngle = rand.nextFloat() * (float) (Math.PI * 2.0);
            float startR = (0.2f + rand.nextFloat() * currentRadius) * 0.8f;
            float x1 = (float) Math.cos(startAngle) * startR;
            float y1 = 0.2f + rand.nextFloat() * height;
            float z1 = (float) Math.sin(startAngle) * startR;

            float endAngle = startAngle + (rand.nextFloat() - 0.5f) * 2.2f;
            float endR = currentRadius * (0.8f + rand.nextFloat() * 0.4f);
            float x2 = (float) Math.cos(endAngle) * endR;
            float y2 = rand.nextFloat() < 0.5f ? -0.1f : (y1 + (rand.nextFloat() - 0.5f) * 1.2f);
            float z2 = (float) Math.sin(endAngle) * endR;

            List<ArcSegment> pts = new ArrayList<>();
            pts.add(new ArcSegment(x1, y1, z1));
            pts.add(new ArcSegment(x2, y2, z2));

            float stepRoughness = roughness * 0.45f;
            for (int step = 0; step < subdivisions; step++) {
                List<ArcSegment> nextPts = new ArrayList<>();
                for (int i = 0; i < pts.size() - 1; i++) {
                    ArcSegment a = pts.get(i);
                    ArcSegment b = pts.get(i + 1);

                    float mx = (a.x + b.x) * 0.5f + (rand.nextFloat() - 0.5f) * stepRoughness;
                    float my = (a.y + b.y) * 0.5f + (rand.nextFloat() - 0.5f) * stepRoughness;
                    float mz = (a.z + b.z) * 0.5f + (rand.nextFloat() - 0.5f) * stepRoughness;

                    nextPts.add(a);
                    nextPts.add(new ArcSegment(mx, my, mz));
                }
                nextPts.add(pts.get(pts.size() - 1));
                pts = nextPts;
                stepRoughness *= 0.52f;
            }
            arc.points.addAll(pts);
        }

        return arc;
    }

    private LightningArc createSurfaceBranch(ArcSegment origin, float currentRadius, float stepRoughness, int life) {
        LightningArc branch = new LightningArc(life);
        float bx = origin.x + (rand.nextFloat() - 0.5f) * stepRoughness * 1.5f;
        float by = origin.y + (rand.nextFloat() - 0.5f) * stepRoughness * 1.5f;
        float bz = origin.z + (rand.nextFloat() - 0.5f) * stepRoughness * 1.5f;
        float bLen = (float) Math.sqrt(bx * bx + by * by + bz * bz);
        if (bLen > 0.0001f) {
            bx = (bx / bLen) * currentRadius;
            by = (by / bLen) * currentRadius;
            bz = (bz / bLen) * currentRadius;
        }
        branch.points.add(origin);
        branch.points.add(new ArcSegment(bx, by, bz));
        return branch;
    }

    private LightningArc createDirectedBranch(ArcSegment origin, ArcSegment target, float stepRoughness, int life) {
        LightningArc branch = new LightningArc(life);
        float dirX = (target.x - origin.x) * 0.5f + (rand.nextFloat() - 0.5f) * stepRoughness;
        float dirY = (target.y - origin.y) * 0.5f + (rand.nextFloat() - 0.5f) * stepRoughness;
        float dirZ = (target.z - origin.z) * 0.5f + (rand.nextFloat() - 0.5f) * stepRoughness;

        branch.points.add(origin);
        branch.points.add(new ArcSegment(origin.x + dirX, origin.y + dirY, origin.z + dirZ));
        return branch;
    }

    @Override
    public void render(VFXInstance instance, float partialTicks, float progress) {
        if (arcs.isEmpty()) return;

        Entity viewEntity = Minecraft.getMinecraft().renderViewEntity;
        if (viewEntity == null) return;

        double camX = viewEntity.lastTickPosX + (viewEntity.posX - viewEntity.lastTickPosX) * partialTicks;
        double camY = viewEntity.lastTickPosY + (viewEntity.posY - viewEntity.lastTickPosY) * partialTicks + viewEntity.getEyeHeight();
        double camZ = viewEntity.lastTickPosZ + (viewEntity.posZ - viewEntity.lastTickPosZ) * partialTicks;

        // Vettore relativo dall'origine dell'istanza alla telecamera nel mondo
        double dx = camX - instance.getCurrentX();
        double dy = camY - instance.getCurrentY();
        double dz = camZ - instance.getCurrentZ();

        // Rotazione interpolata dell'istanza VFX
        float curYaw = instance.getInterpolatedYaw(partialTicks);
        float curPitch = instance.getInterpolatedPitch(partialTicks);
        float curRoll = instance.getInterpolatedRoll(partialTicks);

        // De-rotazione esatta per ottenere le coordinate della telecamera nello spazio locale dell'istanza
        float radYaw = (float) Math.toRadians(-curYaw);
        double cosY = Math.cos(radYaw), sinY = Math.sin(radYaw);
        double x1 = dx * cosY - dz * sinY;
        double y1 = dy;
        double z1 = dx * sinY + dz * cosY;

        float radPitch = (float) Math.toRadians(-curPitch);
        double cosP = Math.cos(radPitch), sinP = Math.sin(radPitch);
        double x2 = x1;
        double y2 = y1 * cosP - z1 * sinP;
        double z2 = y1 * sinP + z1 * cosP;

        float radRoll = (float) Math.toRadians(-curRoll);
        double cosR = Math.cos(radRoll), sinR = Math.sin(radRoll);
        float instCamX = (float) (x2 * cosR - y2 * sinR);
        float instCamY = (float) (x2 * sinR + y2 * cosR);
        float instCamZ = (float) z2;

        // Coordinate della telecamera nello spazio locale del componente (sottraendo l'offset locale)
        float localCamX = instCamX - offsetX;
        float localCamY = instCamY - offsetY;
        float localCamZ = instCamZ - offsetZ;

        RenderState.begin(RenderState.BlendMode.ADDITIVE, false, false);
        ShaderProgram.release();

        GL11.glPushMatrix();
        if (offsetX != 0.0f || offsetY != 0.0f || offsetZ != 0.0f) {
            GL11.glTranslatef(offsetX, offsetY, offsetZ);
        }

        float r = color[0], g = color[1], b = color[2];
        int override = instance.getColorOverride();
        if (override != -1) {
            r = ((override >> 16) & 0xFF) / 255.0f;
            g = ((override >> 8) & 0xFF) / 255.0f;
            b = (override & 0xFF) / 255.0f;
        }

        // Passaggio 1: Alone esterno colorato ad alto bloom (Ribbon largo con alpha morbido)
        renderArcList(arcs, width * glowMultiplier, r, g, b, 0.85f, localCamX, localCamY, localCamZ);

        // Passaggio 2: Nucleo interno bianco/incandescente ad alta emissione
        float cr = coreColor[0], cg = coreColor[1], cb = coreColor[2];
        renderArcList(arcs, width * 0.45f, cr, cg, cb, 1.0f, localCamX, localCamY, localCamZ);

        GL11.glPopMatrix();
        RenderState.end();
    }

    private void renderArcList(List<LightningArc> arcList, float stripWidth, float r, float g, float b, float a,
                               float localCamX, float localCamY, float localCamZ) {
        GL11.glColor4f(r, g, b, a);

        for (LightningArc arc : arcList) {
            renderSingleArc(arc, stripWidth, localCamX, localCamY, localCamZ);
            for (LightningArc branch : arc.branches) {
                renderSingleArc(branch, stripWidth * 0.65f, localCamX, localCamY, localCamZ);
            }
        }
    }

    private void renderSingleArc(LightningArc arc, float stripWidth,
                                 float localCamX, float localCamY, float localCamZ) {
        if (arc.points.size() < 2) return;

        // RISOLUZIONE BUG CRITICO: glBegin/glEnd rigorosamente PER SINGOLO ARCO
        // per eliminare qualsiasi poligono diagonale tra archi consecutivi a tutto schermo
        GL11.glBegin(GL11.GL_TRIANGLE_STRIP);

        for (int i = 0; i < arc.points.size() - 1; i++) {
            ArcSegment p1 = arc.points.get(i);
            ArcSegment p2 = arc.points.get(i + 1);

            float dx = p2.x - p1.x;
            float dy = p2.y - p1.y;
            float dz = p2.z - p1.z;

            float cx = localCamX - p1.x;
            float cy = localCamY - p1.y;
            float cz = localCamZ - p1.z;

            // Prodotto vettoriale (Direzione segmento x Direzione verso la telecamera)
            float nx = dy * cz - dz * cy;
            float ny = dz * cx - dx * cz;
            float nz = dx * cy - dy * cx;
            float len = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
            if (len < 0.0001f) {
                nx = dy * 1.0f;
                ny = -dx * 1.0f;
                nz = 0.0f;
                len = (float) Math.sqrt(nx * nx + ny * ny);
            }
            if (len > 0.0001f) {
                float halfW = stripWidth * 0.5f / len;
                nx *= halfW;
                ny *= halfW;
                nz *= halfW;
            }

            if (i == 0) {
                GL11.glVertex3f(p1.x - nx, p1.y - ny, p1.z - nz);
                GL11.glVertex3f(p1.x + nx, p1.y + ny, p1.z + nz);
            }
            GL11.glVertex3f(p2.x - nx, p2.y - ny, p2.z - nz);
            GL11.glVertex3f(p2.x + nx, p2.y + ny, p2.z + nz);
        }

        GL11.glEnd();
    }

    public LightningComponent setMode(LightningMode mode) {
        this.mode = mode;
        return this;
    }

    public LightningMode getMode() {
        return mode;
    }

    public LightningComponent setColor(float r, float g, float b) {
        this.color = new float[]{r, g, b};
        return this;
    }

    public float[] getColor() {
        return color;
    }

    public LightningComponent setCoreColor(float r, float g, float b) {
        this.coreColor = new float[]{r, g, b};
        return this;
    }

    public float[] getCoreColor() {
        return coreColor;
    }

    public LightningComponent setWidth(float width) {
        this.width = width;
        return this;
    }

    public float getWidth() {
        return width;
    }

    public LightningComponent setRoughness(float roughness) {
        this.roughness = roughness;
        return this;
    }

    public float getRoughness() {
        return roughness;
    }

    public LightningComponent setBranchForks(int branchForks) {
        this.branchForks = branchForks;
        return this;
    }

    public int getBranchForks() {
        return branchForks;
    }

    public LightningComponent setForkChance(float forkChance) {
        this.forkChance = forkChance;
        return this;
    }

    public float getForkChance() {
        return forkChance;
    }

    public LightningComponent setGlowMultiplier(float glowMultiplier) {
        this.glowMultiplier = glowMultiplier;
        return this;
    }

    public float getGlowMultiplier() {
        return glowMultiplier;
    }

    public LightningComponent setOffset(float x, float y, float z) {
        this.offsetX = x;
        this.offsetY = y;
        this.offsetZ = z;
        return this;
    }

    public float getOffsetX() { return offsetX; }
    public float getOffsetY() { return offsetY; }
    public float getOffsetZ() { return offsetZ; }

    public LightningComponent setTarget(float startX, float startY, float startZ, float endX, float endY, float endZ) {
        this.targetStart = new float[]{startX, startY, startZ};
        this.targetEnd = new float[]{endX, endY, endZ};
        this.mode = LightningMode.TARGET_DIRECTED;
        return this;
    }

    public float[] getTargetStart() { return targetStart; }
    public float[] getTargetEnd() { return targetEnd; }

    public LightningComponent setSubdivisions(int subdivisions) {
        this.subdivisions = subdivisions;
        return this;
    }

    public int getSubdivisions() {
        return subdivisions;
    }

    public LightningComponent setSpherical(boolean spherical) {
        this.mode = spherical ? LightningMode.SURFACE_SPHERE : LightningMode.CYLINDER;
        return this;
    }

    public boolean isSpherical() {
        return mode == LightningMode.SURFACE_SPHERE;
    }

    public int getArcCount() {
        return arcCount;
    }

    public float getRadius() {
        return radius;
    }

    public float getHeight() {
        return height;
    }
}
