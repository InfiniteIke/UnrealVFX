package com.unrealvfx.client.render;

import org.lwjgl.opengl.GL11;

/**
 * Cache di geometrie 3D procedurali precompilate (Display Lists GPU).
 * Fornisce mesh riutilizzabili ad alte prestazioni:
 * - Sfera ad alta definizione (64x48) per core e sfere energetiche
 * - Thin Ring (anello di accrescimento a nastro con UV ripetute)
 * - Billboard Quad (quad 2x2 per flare rotanti e corone 2D/3D)
 * - Aura Capsule (guscio a 48 segmenti per auras volumetriche a 360 gradi)
 * - Dome (emisfero per barriere di forza e cupole protettive stile CNPC+)
 * - Disc (disco piatto circolare per Destructo Disc / Kienzan)
 * - Beam Tube (cilindro 3D lungo l'asse Z per raggi energetici continui)
 * - Anello orizzontale (onde d'urto)
 * - Roccia Low-Poly (detriti anti-gravita)
 * - Needle Quad (scintille ad ago)
 */
public class MeshCache {
    private static int sphereList = -1;
    private static int auraCylinderList = -1;
    private static int auraCapsuleList = -1;
    private static int starFlameList = -1;
    private static int ringList = -1;
    private static int thinRingList = -1;
    private static int domeList = -1;
    private static int discList = -1;
    private static int beamTubeList = -1;
    private static int lowPolyRockList = -1;
    private static int needleQuadList = -1;
    private static int quadList = -1;
    private static int billboardQuadList = -1;
    private static int accretionDiskList = -1;
    private static int cubeList = -1;
    private static int cylinderList = -1;
    private static int coneList = -1;
    private static int planeList = -1;

    public static void init() {
        sphereList = buildSphere(64, 48);
        auraCylinderList = buildAuraCylinder(48, 1.2f, 0.8f, 2.2f);
        auraCapsuleList = buildAuraCapsule(48, 1.3f, 0.7f, 2.4f);
        starFlameList = buildStarFlame(6, 1.3f, 2.5f);
        ringList = buildRing(48, 0.2f, 1.0f, 1.0f);
        thinRingList = buildRing(64, 0.90f, 1.12f, 4.0f);
        accretionDiskList = buildAccretionDisk(72, 0.32f, 1.0f, 0.055f);
        domeList = buildDome(48, 24);
        discList = buildDisc(48);
        beamTubeList = buildBeamTube(32, 1.0f, 1.0f);
        lowPolyRockList = buildLowPolyRock();
        needleQuadList = buildNeedleQuad();
        quadList = buildQuad();
        billboardQuadList = buildBillboardQuad();
        cubeList = buildCube();
        cylinderList = buildCylinder(32);
        coneList = buildCone(32);
        planeList = buildPlane();
    }

    public static void renderSphere() {
        if (sphereList == -1) init();
        GL11.glCallList(sphereList);
    }

    public static void renderAuraCylinder() {
        if (auraCylinderList == -1) init();
        GL11.glCallList(auraCylinderList);
    }

    public static void renderAuraCapsule() {
        if (auraCapsuleList == -1) init();
        GL11.glCallList(auraCapsuleList);
    }

    public static void renderStarFlame() {
        if (starFlameList == -1) init();
        GL11.glCallList(starFlameList);
    }

    public static void renderRing() {
        if (ringList == -1) init();
        GL11.glCallList(ringList);
    }

    public static void renderThinRing() {
        if (thinRingList == -1) init();
        GL11.glCallList(thinRingList);
    }

    public static void renderDome() {
        if (domeList == -1) init();
        GL11.glCallList(domeList);
    }

    public static void renderDisc() {
        if (discList == -1) init();
        GL11.glCallList(discList);
    }

    public static void renderBeamTube() {
        if (beamTubeList == -1) init();
        GL11.glCallList(beamTubeList);
    }

    public static void renderLowPolyRock() {
        if (lowPolyRockList == -1) init();
        GL11.glCallList(lowPolyRockList);
    }

    public static void renderNeedleQuad() {
        if (needleQuadList == -1) init();
        GL11.glCallList(needleQuadList);
    }

    public static void renderQuad() {
        if (quadList == -1) init();
        GL11.glCallList(quadList);
    }

    public static void renderBillboardQuad() {
        if (billboardQuadList == -1) init();
        GL11.glCallList(billboardQuadList);
    }

    public static void renderAccretionDisk() {
        if (accretionDiskList == -1) init();
        GL11.glCallList(accretionDiskList);
    }

    public static void renderCube() {
        if (cubeList == -1) init();
        GL11.glCallList(cubeList);
    }

    public static void renderCylinder() {
        if (cylinderList == -1) init();
        GL11.glCallList(cylinderList);
    }

    public static void renderCone() {
        if (coneList == -1) init();
        GL11.glCallList(coneList);
    }

    public static void renderPlane() {
        if (planeList == -1) init();
        GL11.glCallList(planeList);
    }

    private static int buildDome(int slices, int stacks) {
        int list = GL11.glGenLists(1);
        GL11.glNewList(list, GL11.GL_COMPILE);
        GL11.glBegin(GL11.GL_TRIANGLES);

        for (int i = 0; i < stacks; i++) {
            float lat0 = (float) Math.PI * 0.5f * ((float) i / stacks);
            float y0 = (float) Math.sin(lat0);
            float r0 = (float) Math.cos(lat0);

            float lat1 = (float) Math.PI * 0.5f * ((float) (i + 1) / stacks);
            float y1 = (float) Math.sin(lat1);
            float r1 = (float) Math.cos(lat1);

            float v0 = (float) i / stacks;
            float v1 = (float) (i + 1) / stacks;

            for (int j = 0; j < slices; j++) {
                float lng0 = 2.0f * (float) Math.PI * (float) j / slices;
                float x0 = (float) Math.cos(lng0);
                float z0 = (float) Math.sin(lng0);

                float lng1 = 2.0f * (float) Math.PI * (float) (j + 1) / slices;
                float x1 = (float) Math.cos(lng1);
                float z1 = (float) Math.sin(lng1);

                float u0 = (float) j / slices;
                float u1 = (float) (j + 1) / slices;

                GL11.glTexCoord2f(u0, v0); GL11.glNormal3f(x0 * r0, y0, z0 * r0); GL11.glVertex3f(x0 * r0, y0, z0 * r0);
                GL11.glTexCoord2f(u1, v0); GL11.glNormal3f(x1 * r0, y0, z1 * r0); GL11.glVertex3f(x1 * r0, y0, z1 * r0);
                GL11.glTexCoord2f(u1, v1); GL11.glNormal3f(x1 * r1, y1, z1 * r1); GL11.glVertex3f(x1 * r1, y1, z1 * r1);

                GL11.glTexCoord2f(u0, v0); GL11.glNormal3f(x0 * r0, y0, z0 * r0); GL11.glVertex3f(x0 * r0, y0, z0 * r0);
                GL11.glTexCoord2f(u1, v1); GL11.glNormal3f(x1 * r1, y1, z1 * r1); GL11.glVertex3f(x1 * r1, y1, z1 * r1);
                GL11.glTexCoord2f(u0, v1); GL11.glNormal3f(x0 * r1, y1, z0 * r1); GL11.glVertex3f(x0 * r1, y1, z0 * r1);
            }
        }

        GL11.glEnd();
        GL11.glEndList();
        return list;
    }

    private static int buildDisc(int segments) {
        int list = GL11.glGenLists(1);
        GL11.glNewList(list, GL11.GL_COMPILE);
        GL11.glBegin(GL11.GL_TRIANGLES);

        for (int i = 0; i < segments; i++) {
            float a0 = 2.0f * (float) Math.PI * (float) i / segments;
            float a1 = 2.0f * (float) Math.PI * (float) (i + 1) / segments;

            float x0 = (float) Math.cos(a0);
            float z0 = (float) Math.sin(a0);
            float x1 = (float) Math.cos(a1);
            float z1 = (float) Math.sin(a1);

            float u0 = x0 * 0.5f + 0.5f;
            float v0 = z0 * 0.5f + 0.5f;
            float u1 = x1 * 0.5f + 0.5f;
            float v1 = z1 * 0.5f + 0.5f;

            GL11.glNormal3f(0, 1, 0);
            GL11.glTexCoord2f(0.5f, 0.5f); GL11.glVertex3f(0.0f, 0.0f, 0.0f);
            GL11.glTexCoord2f(u0, v0); GL11.glVertex3f(x0, 0.0f, z0);
            GL11.glTexCoord2f(u1, v1); GL11.glVertex3f(x1, 0.0f, z1);
        }

        GL11.glEnd();
        GL11.glEndList();
        return list;
    }

    private static int buildBeamTube(int segments, float radius, float length) {
        int list = GL11.glGenLists(1);
        GL11.glNewList(list, GL11.GL_COMPILE);
        GL11.glBegin(GL11.GL_TRIANGLES);

        for (int i = 0; i < segments; i++) {
            float a0 = 2.0f * (float) Math.PI * (float) i / segments;
            float a1 = 2.0f * (float) Math.PI * (float) (i + 1) / segments;

            float cos0 = (float) Math.cos(a0);
            float sin0 = (float) Math.sin(a0);
            float cos1 = (float) Math.cos(a1);
            float sin1 = (float) Math.sin(a1);

            float u0 = (float) i / segments;
            float u1 = (float) (i + 1) / segments;

            // Allineato lungo l'asse Z: da z=0 a z=length
            GL11.glTexCoord2f(u0, 0.0f); GL11.glNormal3f(cos0, sin0, 0); GL11.glVertex3f(cos0 * radius, sin0 * radius, 0.0f);
            GL11.glTexCoord2f(u1, 0.0f); GL11.glNormal3f(cos1, sin1, 0); GL11.glVertex3f(cos1 * radius, sin1 * radius, 0.0f);
            GL11.glTexCoord2f(u1, 1.0f); GL11.glNormal3f(cos1, sin1, 0); GL11.glVertex3f(cos1 * radius, sin1 * radius, length);

            GL11.glTexCoord2f(u0, 0.0f); GL11.glNormal3f(cos0, sin0, 0); GL11.glVertex3f(cos0 * radius, sin0 * radius, 0.0f);
            GL11.glTexCoord2f(u1, 1.0f); GL11.glNormal3f(cos1, sin1, 0); GL11.glVertex3f(cos1 * radius, sin1 * radius, length);
            GL11.glTexCoord2f(u0, 1.0f); GL11.glNormal3f(cos0, sin0, 0); GL11.glVertex3f(cos0 * radius, sin0 * radius, length);
        }

        GL11.glEnd();
        GL11.glEndList();
        return list;
    }

    private static int buildBillboardQuad() {
        int list = GL11.glGenLists(1);
        GL11.glNewList(list, GL11.GL_COMPILE);
        GL11.glBegin(GL11.GL_QUADS);

        GL11.glTexCoord2f(0.0f, 1.0f); GL11.glNormal3f(0, 0, 1); GL11.glVertex3f(-1.0f, -1.0f, 0.0f);
        GL11.glTexCoord2f(1.0f, 1.0f); GL11.glNormal3f(0, 0, 1); GL11.glVertex3f(1.0f, -1.0f, 0.0f);
        GL11.glTexCoord2f(1.0f, 0.0f); GL11.glNormal3f(0, 0, 1); GL11.glVertex3f(1.0f, 1.0f, 0.0f);
        GL11.glTexCoord2f(0.0f, 0.0f); GL11.glNormal3f(0, 0, 1); GL11.glVertex3f(-1.0f, 1.0f, 0.0f);

        GL11.glEnd();
        GL11.glEndList();
        return list;
    }

    private static int buildStarFlame(int blades, float width, float height) {
        int list = GL11.glGenLists(1);
        GL11.glNewList(list, GL11.GL_COMPILE);
        GL11.glBegin(GL11.GL_TRIANGLES);

        for (int b = 0; b < blades; b++) {
            float angle = (float) (Math.PI * b / blades);
            float cos = (float) Math.cos(angle);
            float sin = (float) Math.sin(angle);

            int segments = 6;
            for (int s = 0; s < segments; s++) {
                float y0 = height * ((float) s / segments);
                float y1 = height * ((float) (s + 1) / segments);

                float taper0 = (float) Math.sin((s / (float) segments) * Math.PI) * 0.4f + 0.6f;
                float taper1 = (float) Math.sin(((s + 1) / (float) segments) * Math.PI) * 0.4f + 0.6f;

                float w0 = (width * 0.5f) * taper0;
                float w1 = (width * 0.5f) * taper1;

                float v0 = (float) s / segments;
                float v1 = (float) (s + 1) / segments;

                float x0_left = -cos * w0, z0_left = -sin * w0;
                float x0_right = cos * w0, z0_right = sin * w0;
                float x1_left = -cos * w1, z1_left = -sin * w1;
                float x1_right = cos * w1, z1_right = sin * w1;

                float nx = -sin, nz = cos;

                GL11.glTexCoord2f(0.0f, v0); GL11.glNormal3f(nx, 0, nz); GL11.glVertex3f(x0_left, y0, z0_left);
                GL11.glTexCoord2f(1.0f, v0); GL11.glNormal3f(nx, 0, nz); GL11.glVertex3f(x0_right, y0, z0_right);
                GL11.glTexCoord2f(1.0f, v1); GL11.glNormal3f(nx, 0, nz); GL11.glVertex3f(x1_right, y1, z1_right);

                GL11.glTexCoord2f(0.0f, v0); GL11.glNormal3f(nx, 0, nz); GL11.glVertex3f(x0_left, y0, z0_left);
                GL11.glTexCoord2f(1.0f, v1); GL11.glNormal3f(nx, 0, nz); GL11.glVertex3f(x1_right, y1, z1_right);
                GL11.glTexCoord2f(0.0f, v1); GL11.glNormal3f(nx, 0, nz); GL11.glVertex3f(x1_left, y1, z1_left);
            }
        }

        GL11.glEnd();
        GL11.glEndList();
        return list;
    }

    private static int buildLowPolyRock() {
        int list = GL11.glGenLists(1);
        GL11.glNewList(list, GL11.GL_COMPILE);
        GL11.glBegin(GL11.GL_TRIANGLES);

        float[][] v = new float[][]{
            {0.0f, 0.45f, 0.0f},
            {-0.3f, 0.05f, 0.25f},
            {0.35f, -0.05f, 0.2f},
            {0.25f, 0.1f, -0.3f},
            {-0.32f, -0.08f, -0.26f},
            {0.05f, -0.42f, 0.0f}
        };

        int[][] faces = new int[][]{
            {0, 1, 2}, {0, 2, 3}, {0, 3, 4}, {0, 4, 1},
            {5, 2, 1}, {5, 3, 2}, {5, 4, 3}, {5, 1, 4}
        };

        for (int[] f : faces) {
            float[] v0 = v[f[0]];
            float[] v1 = v[f[1]];
            float[] v2 = v[f[2]];

            float ax = v1[0] - v0[0], ay = v1[1] - v0[1], az = v1[2] - v0[2];
            float bx = v2[0] - v0[0], by = v2[1] - v0[1], bz = v2[2] - v0[2];
            float nx = ay * bz - az * by;
            float ny = az * bx - ax * bz;
            float nz = ax * by - ay * bx;
            float len = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
            if (len > 0.0001f) { nx /= len; ny /= len; nz /= len; }

            GL11.glNormal3f(nx, ny, nz);
            GL11.glTexCoord2f(0.0f, 0.0f); GL11.glVertex3f(v0[0], v0[1], v0[2]);
            GL11.glTexCoord2f(1.0f, 0.0f); GL11.glVertex3f(v1[0], v1[1], v1[2]);
            GL11.glTexCoord2f(0.5f, 1.0f); GL11.glVertex3f(v2[0], v2[1], v2[2]);
        }

        GL11.glEnd();
        GL11.glEndList();
        return list;
    }

    private static int buildNeedleQuad() {
        int list = GL11.glGenLists(1);
        GL11.glNewList(list, GL11.GL_COMPILE);
        GL11.glBegin(GL11.GL_QUADS);

        GL11.glTexCoord2f(0.0f, 0.0f); GL11.glVertex3f(-0.1f, -0.5f, 0.0f);
        GL11.glTexCoord2f(1.0f, 0.0f); GL11.glVertex3f(0.1f, -0.5f, 0.0f);
        GL11.glTexCoord2f(1.0f, 1.0f); GL11.glVertex3f(0.02f, 0.5f, 0.0f);
        GL11.glTexCoord2f(0.0f, 1.0f); GL11.glVertex3f(-0.02f, 0.5f, 0.0f);

        GL11.glEnd();
        GL11.glEndList();
        return list;
    }

    private static int buildSphere(int slices, int stacks) {
        int list = GL11.glGenLists(1);
        GL11.glNewList(list, GL11.GL_COMPILE);
        GL11.glBegin(GL11.GL_TRIANGLES);

        for (int i = 0; i < stacks; i++) {
            float lat0 = (float) Math.PI * (-0.5f + (float) i / stacks);
            float z0 = (float) Math.sin(lat0);
            float zr0 = (float) Math.cos(lat0);

            float lat1 = (float) Math.PI * (-0.5f + (float) (i + 1) / stacks);
            float z1 = (float) Math.sin(lat1);
            float zr1 = (float) Math.cos(lat1);

            float v0 = (float) i / stacks;
            float v1 = (float) (i + 1) / stacks;

            for (int j = 0; j < slices; j++) {
                float lng0 = 2.0f * (float) Math.PI * (float) j / slices;
                float x0 = (float) Math.cos(lng0);
                float y0 = (float) Math.sin(lng0);

                float lng1 = 2.0f * (float) Math.PI * (float) (j + 1) / slices;
                float x1 = (float) Math.cos(lng1);
                float y1 = (float) Math.sin(lng1);

                float u0 = (float) j / slices;
                float u1 = (float) (j + 1) / slices;

                // Triangle 1 (CCW):
                GL11.glTexCoord2f(u0, v0); GL11.glNormal3f(x0 * zr0, z0, y0 * zr0); GL11.glVertex3f(x0 * zr0, z0, y0 * zr0);
                GL11.glTexCoord2f(u1, v1); GL11.glNormal3f(x1 * zr1, z1, y1 * zr1); GL11.glVertex3f(x1 * zr1, z1, y1 * zr1);
                GL11.glTexCoord2f(u1, v0); GL11.glNormal3f(x1 * zr0, z0, y1 * zr0); GL11.glVertex3f(x1 * zr0, z0, y1 * zr0);

                // Triangle 2 (CCW):
                GL11.glTexCoord2f(u0, v0); GL11.glNormal3f(x0 * zr0, z0, y0 * zr0); GL11.glVertex3f(x0 * zr0, z0, y0 * zr0);
                GL11.glTexCoord2f(u0, v1); GL11.glNormal3f(x0 * zr1, z1, y0 * zr1); GL11.glVertex3f(x0 * zr1, z1, y0 * zr1);
                GL11.glTexCoord2f(u1, v1); GL11.glNormal3f(x1 * zr1, z1, y1 * zr1); GL11.glVertex3f(x1 * zr1, z1, y1 * zr1);
            }
        }

        GL11.glEnd();
        GL11.glEndList();
        return list;
    }

    private static int buildAuraCylinder(int segments, float bottomRadius, float topRadius, float height) {
        int list = GL11.glGenLists(1);
        GL11.glNewList(list, GL11.GL_COMPILE);
        GL11.glBegin(GL11.GL_TRIANGLES);

        for (int i = 0; i < segments; i++) {
            float angle0 = 2.0f * (float) Math.PI * (float) i / segments;
            float angle1 = 2.0f * (float) Math.PI * (float) (i + 1) / segments;

            float x0b = (float) Math.cos(angle0) * bottomRadius;
            float z0b = (float) Math.sin(angle0) * bottomRadius;
            float x1b = (float) Math.cos(angle1) * bottomRadius;
            float z1b = (float) Math.sin(angle1) * bottomRadius;

            float x0t = (float) Math.cos(angle0) * topRadius;
            float z0t = (float) Math.sin(angle0) * topRadius;
            float x1t = (float) Math.cos(angle1) * topRadius;
            float z1t = (float) Math.sin(angle1) * topRadius;

            float u0 = (float) i / segments;
            float u1 = (float) (i + 1) / segments;

            GL11.glTexCoord2f(u0, 0.0f); GL11.glNormal3f(x0b, 0, z0b); GL11.glVertex3f(x0b, 0.0f, z0b);
            GL11.glTexCoord2f(u1, 0.0f); GL11.glNormal3f(x1b, 0, z1b); GL11.glVertex3f(x1b, 0.0f, z1b);
            GL11.glTexCoord2f(u1, 1.0f); GL11.glNormal3f(x1t, 0, z1t); GL11.glVertex3f(x1t, height, z1t);

            GL11.glTexCoord2f(u0, 0.0f); GL11.glNormal3f(x0b, 0, z0b); GL11.glVertex3f(x0b, 0.0f, z0b);
            GL11.glTexCoord2f(u1, 1.0f); GL11.glNormal3f(x1t, 0, z1t); GL11.glVertex3f(x1t, height, z1t);
            GL11.glTexCoord2f(u0, 1.0f); GL11.glNormal3f(x0t, 0, z0t); GL11.glVertex3f(x0t, height, z0t);
        }

        GL11.glEnd();
        GL11.glEndList();
        return list;
    }

    private static int buildAuraCapsule(int segments, float radius, float topTaper, float height) {
        int list = GL11.glGenLists(1);
        GL11.glNewList(list, GL11.GL_COMPILE);
        GL11.glBegin(GL11.GL_TRIANGLES);

        int heightRings = 16;
        for (int r = 0; r < heightRings; r++) {
            float y0 = height * ((float) r / heightRings);
            float y1 = height * ((float) (r + 1) / heightRings);

            float normH0 = (float) r / heightRings;
            float normH1 = (float) (r + 1) / heightRings;

            float r0 = radius * (1.0f - normH0 * (1.0f - topTaper)) * (float) Math.sin(Math.min(1.0f, normH0 * 2.2f) * Math.PI * 0.5f);
            float r1 = radius * (1.0f - normH1 * (1.0f - topTaper)) * (float) Math.sin(Math.min(1.0f, normH1 * 2.2f) * Math.PI * 0.5f);

            for (int i = 0; i < segments; i++) {
                float a0 = 2.0f * (float) Math.PI * (float) i / segments;
                float a1 = 2.0f * (float) Math.PI * (float) (i + 1) / segments;

                float cos0 = (float) Math.cos(a0), sin0 = (float) Math.sin(a0);
                float cos1 = (float) Math.cos(a1), sin1 = (float) Math.sin(a1);

                float u0 = (float) i / segments;
                float u1 = (float) (i + 1) / segments;

                GL11.glTexCoord2f(u0, normH0); GL11.glNormal3f(cos0, 0, sin0); GL11.glVertex3f(cos0 * r0, y0, sin0 * r0);
                GL11.glTexCoord2f(u1, normH0); GL11.glNormal3f(cos1, 0, sin1); GL11.glVertex3f(cos1 * r0, y0, sin1 * r0);
                GL11.glTexCoord2f(u1, normH1); GL11.glNormal3f(cos1, 0, sin1); GL11.glVertex3f(cos1 * r1, y1, sin1 * r1);

                GL11.glTexCoord2f(u0, normH0); GL11.glNormal3f(cos0, 0, sin0); GL11.glVertex3f(cos0 * r0, y0, sin0 * r0);
                GL11.glTexCoord2f(u1, normH1); GL11.glNormal3f(cos1, 0, sin1); GL11.glVertex3f(cos1 * r1, y1, sin1 * r1);
                GL11.glTexCoord2f(u0, normH1); GL11.glNormal3f(cos0, 0, sin0); GL11.glVertex3f(cos0 * r1, y1, sin0 * r1);
            }
        }

        GL11.glEnd();
        GL11.glEndList();
        return list;
    }

    private static int buildRing(int segments, float innerRadius, float outerRadius, float uRepeat) {
        int list = GL11.glGenLists(1);
        GL11.glNewList(list, GL11.GL_COMPILE);
        GL11.glBegin(GL11.GL_TRIANGLES);

        for (int i = 0; i < segments; i++) {
            float angle0 = 2.0f * (float) Math.PI * (float) i / segments;
            float angle1 = 2.0f * (float) Math.PI * (float) (i + 1) / segments;

            float cos0 = (float) Math.cos(angle0);
            float sin0 = (float) Math.sin(angle0);
            float cos1 = (float) Math.cos(angle1);
            float sin1 = (float) Math.sin(angle1);

            float u0 = ((float) i / segments) * uRepeat;
            float u1 = ((float) (i + 1) / segments) * uRepeat;

            GL11.glTexCoord2f(u0, 0.0f); GL11.glNormal3f(0, 1, 0); GL11.glVertex3f(cos0 * innerRadius, 0.0f, sin0 * innerRadius);
            GL11.glTexCoord2f(u0, 1.0f); GL11.glNormal3f(0, 1, 0); GL11.glVertex3f(cos0 * outerRadius, 0.0f, sin0 * outerRadius);
            GL11.glTexCoord2f(u1, 1.0f); GL11.glNormal3f(0, 1, 0); GL11.glVertex3f(cos1 * outerRadius, 0.0f, sin1 * outerRadius);

            GL11.glTexCoord2f(u0, 0.0f); GL11.glNormal3f(0, 1, 0); GL11.glVertex3f(cos0 * innerRadius, 0.0f, sin0 * innerRadius);
            GL11.glTexCoord2f(u1, 1.0f); GL11.glNormal3f(0, 1, 0); GL11.glVertex3f(cos1 * outerRadius, 0.0f, sin1 * outerRadius);
            GL11.glTexCoord2f(u1, 0.0f); GL11.glNormal3f(0, 1, 0); GL11.glVertex3f(cos1 * innerRadius, 0.0f, sin1 * innerRadius);
        }

        GL11.glEnd();
        GL11.glEndList();
        return list;
    }

    private static int buildQuad() {
        int list = GL11.glGenLists(1);
        GL11.glNewList(list, GL11.GL_COMPILE);
        GL11.glBegin(GL11.GL_QUADS);

        GL11.glTexCoord2f(0.0f, 0.0f); GL11.glNormal3f(0, 0, 1); GL11.glVertex3f(-0.5f, -0.5f, 0.0f);
        GL11.glTexCoord2f(1.0f, 0.0f); GL11.glNormal3f(0, 0, 1); GL11.glVertex3f(0.5f, -0.5f, 0.0f);
        GL11.glTexCoord2f(1.0f, 1.0f); GL11.glNormal3f(0, 0, 1); GL11.glVertex3f(0.5f, 0.5f, 0.0f);
        GL11.glTexCoord2f(0.0f, 1.0f); GL11.glNormal3f(0, 0, 1); GL11.glVertex3f(-0.5f, 0.5f, 0.0f);

        GL11.glEnd();
        GL11.glEndList();
        return list;
    }

    /**
     * Disco d'accrescimento 3D con profilo lenticolare:
     * - 8 fasce radiali con altezza a campana lungo Y
     * - Piu spesso al ~35% dal bordo interno, sottile ai bordi
     * - Normali analitiche calcolate sulla derivata della pendenza per shading volumetrico perfetto
     * - Superficie superiore (+Y) e inferiore (-Y) per volume reale
     */
    private static int buildAccretionDisk(int segments, float innerRadius, float outerRadius, float thickness) {
        int list = GL11.glGenLists(1);
        GL11.glNewList(list, GL11.GL_COMPILE);
        GL11.glBegin(GL11.GL_TRIANGLES);

        int radialSteps = 8;
        float dr = outerRadius - innerRadius;

        for (int r = 0; r < radialSteps; r++) {
            float t0 = (float) r / radialSteps;
            float t1 = (float) (r + 1) / radialSteps;

            float rad0 = innerRadius + dr * t0;
            float rad1 = innerRadius + dr * t1;

            // Profilo lenticolare
            float h0 = thickness * (float) (Math.sin(Math.PI * t0) * (1.0 - t0 * 0.25));
            float h1 = thickness * (float) (Math.sin(Math.PI * t1) * (1.0 - t1 * 0.25));

            // Derivata analitica dh/dr per le normali
            float dhdt0 = thickness * (float) (Math.PI * Math.cos(Math.PI * t0) * (1.0 - t0 * 0.25) - 0.25 * Math.sin(Math.PI * t0));
            float dhdt1 = thickness * (float) (Math.PI * Math.cos(Math.PI * t1) * (1.0 - t1 * 0.25) - 0.25 * Math.sin(Math.PI * t1));
            float slope0 = dhdt0 / dr;
            float slope1 = dhdt1 / dr;

            // Lunghezza del vettore normale per normalizzazione
            float invLen0 = 1.0f / (float) Math.sqrt(slope0 * slope0 + 1.0f);
            float invLen1 = 1.0f / (float) Math.sqrt(slope1 * slope1 + 1.0f);

            for (int i = 0; i < segments; i++) {
                float a0 = 2.0f * (float) Math.PI * (float) i / segments;
                float a1 = 2.0f * (float) Math.PI * (float) (i + 1) / segments;

                float cos0 = (float) Math.cos(a0);
                float sin0 = (float) Math.sin(a0);
                float cos1 = (float) Math.cos(a1);
                float sin1 = (float) Math.sin(a1);

                float u0 = (float) i / segments;
                float u1 = (float) (i + 1) / segments;

                // Normali superiori
                float nx00 = -slope0 * cos0 * invLen0; float ny00 = invLen0; float nz00 = -slope0 * sin0 * invLen0;
                float nx10 = -slope1 * cos0 * invLen1; float ny10 = invLen1; float nz10 = -slope1 * sin0 * invLen1;
                float nx11 = -slope1 * cos1 * invLen1; float ny11 = invLen1; float nz11 = -slope1 * sin1 * invLen1;
                float nx01 = -slope0 * cos1 * invLen0; float ny01 = invLen0; float nz01 = -slope0 * sin1 * invLen0;

                // SUPERFICIE SUPERIORE (+Y)
                GL11.glNormal3f(nx00, ny00, nz00); GL11.glTexCoord2f(u0, t0); GL11.glVertex3f(cos0 * rad0, h0, sin0 * rad0);
                GL11.glNormal3f(nx10, ny10, nz10); GL11.glTexCoord2f(u0, t1); GL11.glVertex3f(cos0 * rad1, h1, sin0 * rad1);
                GL11.glNormal3f(nx11, ny11, nz11); GL11.glTexCoord2f(u1, t1); GL11.glVertex3f(cos1 * rad1, h1, sin1 * rad1);

                GL11.glNormal3f(nx00, ny00, nz00); GL11.glTexCoord2f(u0, t0); GL11.glVertex3f(cos0 * rad0, h0, sin0 * rad0);
                GL11.glNormal3f(nx11, ny11, nz11); GL11.glTexCoord2f(u1, t1); GL11.glVertex3f(cos1 * rad1, h1, sin1 * rad1);
                GL11.glNormal3f(nx01, ny01, nz01); GL11.glTexCoord2f(u1, t0); GL11.glVertex3f(cos1 * rad0, h0, sin1 * rad0);

                // SUPERFICIE INFERIORE (-Y)
                GL11.glNormal3f(nx00, -ny00, nz00); GL11.glTexCoord2f(u0, t0); GL11.glVertex3f(cos0 * rad0, -h0, sin0 * rad0);
                GL11.glNormal3f(nx11, -ny11, nz11); GL11.glTexCoord2f(u1, t1); GL11.glVertex3f(cos1 * rad1, -h1, sin1 * rad1);
                GL11.glNormal3f(nx10, -ny10, nz10); GL11.glTexCoord2f(u0, t1); GL11.glVertex3f(cos0 * rad1, -h1, sin0 * rad1);

                GL11.glNormal3f(nx00, -ny00, nz00); GL11.glTexCoord2f(u0, t0); GL11.glVertex3f(cos0 * rad0, -h0, sin0 * rad0);
                GL11.glNormal3f(nx01, -ny01, nz01); GL11.glTexCoord2f(u1, t0); GL11.glVertex3f(cos1 * rad0, -h0, sin1 * rad0);
                GL11.glNormal3f(nx11, -ny11, nz11); GL11.glTexCoord2f(u1, t1); GL11.glVertex3f(cos1 * rad1, -h1, sin1 * rad1);
            }
        }

        GL11.glEnd();
        GL11.glEndList();
        return list;
    }

    private static int buildCube() {
        int list = GL11.glGenLists(1);
        GL11.glNewList(list, GL11.GL_COMPILE);
        GL11.glBegin(GL11.GL_QUADS);

        // Front face (+Z)
        GL11.glNormal3f(0.0f, 0.0f, 1.0f);
        GL11.glTexCoord2f(0.0f, 0.0f); GL11.glVertex3f(-0.5f, -0.5f, 0.5f);
        GL11.glTexCoord2f(1.0f, 0.0f); GL11.glVertex3f(0.5f, -0.5f, 0.5f);
        GL11.glTexCoord2f(1.0f, 1.0f); GL11.glVertex3f(0.5f, 0.5f, 0.5f);
        GL11.glTexCoord2f(0.0f, 1.0f); GL11.glVertex3f(-0.5f, 0.5f, 0.5f);

        // Back face (-Z)
        GL11.glNormal3f(0.0f, 0.0f, -1.0f);
        GL11.glTexCoord2f(1.0f, 0.0f); GL11.glVertex3f(-0.5f, -0.5f, -0.5f);
        GL11.glTexCoord2f(1.0f, 1.0f); GL11.glVertex3f(-0.5f, 0.5f, -0.5f);
        GL11.glTexCoord2f(0.0f, 1.0f); GL11.glVertex3f(0.5f, 0.5f, -0.5f);
        GL11.glTexCoord2f(0.0f, 0.0f); GL11.glVertex3f(0.5f, -0.5f, -0.5f);

        // Top face (+Y)
        GL11.glNormal3f(0.0f, 1.0f, 0.0f);
        GL11.glTexCoord2f(0.0f, 1.0f); GL11.glVertex3f(-0.5f, 0.5f, -0.5f);
        GL11.glTexCoord2f(0.0f, 0.0f); GL11.glVertex3f(-0.5f, 0.5f, 0.5f);
        GL11.glTexCoord2f(1.0f, 0.0f); GL11.glVertex3f(0.5f, 0.5f, 0.5f);
        GL11.glTexCoord2f(1.0f, 1.0f); GL11.glVertex3f(0.5f, 0.5f, -0.5f);

        // Bottom face (-Y)
        GL11.glNormal3f(0.0f, -1.0f, 0.0f);
        GL11.glTexCoord2f(1.0f, 1.0f); GL11.glVertex3f(-0.5f, -0.5f, -0.5f);
        GL11.glTexCoord2f(0.0f, 1.0f); GL11.glVertex3f(0.5f, -0.5f, -0.5f);
        GL11.glTexCoord2f(0.0f, 0.0f); GL11.glVertex3f(0.5f, -0.5f, 0.5f);
        GL11.glTexCoord2f(1.0f, 0.0f); GL11.glVertex3f(-0.5f, -0.5f, 0.5f);

        // Right face (+X)
        GL11.glNormal3f(1.0f, 0.0f, 0.0f);
        GL11.glTexCoord2f(0.0f, 0.0f); GL11.glVertex3f(0.5f, -0.5f, -0.5f);
        GL11.glTexCoord2f(1.0f, 0.0f); GL11.glVertex3f(0.5f, 0.5f, -0.5f);
        GL11.glTexCoord2f(1.0f, 1.0f); GL11.glVertex3f(0.5f, 0.5f, 0.5f);
        GL11.glTexCoord2f(0.0f, 1.0f); GL11.glVertex3f(0.5f, -0.5f, 0.5f);

        // Left face (-X)
        GL11.glNormal3f(-1.0f, 0.0f, 0.0f);
        GL11.glTexCoord2f(0.0f, 0.0f); GL11.glVertex3f(-0.5f, -0.5f, -0.5f);
        GL11.glTexCoord2f(1.0f, 0.0f); GL11.glVertex3f(-0.5f, -0.5f, 0.5f);
        GL11.glTexCoord2f(1.0f, 1.0f); GL11.glVertex3f(-0.5f, 0.5f, 0.5f);
        GL11.glTexCoord2f(0.0f, 1.0f); GL11.glVertex3f(-0.5f, 0.5f, -0.5f);

        GL11.glEnd();
        GL11.glEndList();
        return list;
    }

    private static int buildCylinder(int slices) {
        int list = GL11.glGenLists(1);
        GL11.glNewList(list, GL11.GL_COMPILE);
        GL11.glBegin(GL11.GL_TRIANGLES);

        float halfH = 0.5f;
        float radius = 0.5f;

        for (int i = 0; i < slices; i++) {
            float a0 = (float) (2.0 * Math.PI * i / slices);
            float a1 = (float) (2.0 * Math.PI * (i + 1) / slices);

            float c0 = (float) Math.cos(a0);
            float s0 = (float) Math.sin(a0);
            float c1 = (float) Math.cos(a1);
            float s1 = (float) Math.sin(a1);

            float u0 = (float) i / slices;
            float u1 = (float) (i + 1) / slices;

            // Side quad (as 2 triangles, CCW)
            GL11.glNormal3f(c0, 0.0f, s0); GL11.glTexCoord2f(u0, 0.0f); GL11.glVertex3f(c0 * radius, -halfH, s0 * radius);
            GL11.glNormal3f(c1, 0.0f, s1); GL11.glTexCoord2f(u1, 1.0f); GL11.glVertex3f(c1 * radius,  halfH, s1 * radius);
            GL11.glNormal3f(c1, 0.0f, s1); GL11.glTexCoord2f(u1, 0.0f); GL11.glVertex3f(c1 * radius, -halfH, s1 * radius);

            GL11.glNormal3f(c0, 0.0f, s0); GL11.glTexCoord2f(u0, 0.0f); GL11.glVertex3f(c0 * radius, -halfH, s0 * radius);
            GL11.glNormal3f(c0, 0.0f, s0); GL11.glTexCoord2f(u0, 1.0f); GL11.glVertex3f(c0 * radius,  halfH, s0 * radius);
            GL11.glNormal3f(c1, 0.0f, s1); GL11.glTexCoord2f(u1, 1.0f); GL11.glVertex3f(c1 * radius,  halfH, s1 * radius);

            // Top cap
            GL11.glNormal3f(0.0f, 1.0f, 0.0f);
            GL11.glTexCoord2f(0.5f, 0.5f); GL11.glVertex3f(0.0f, halfH, 0.0f);
            GL11.glTexCoord2f(0.5f + c0 * 0.5f, 0.5f + s0 * 0.5f); GL11.glVertex3f(c0 * radius, halfH, s0 * radius);
            GL11.glTexCoord2f(0.5f + c1 * 0.5f, 0.5f + s1 * 0.5f); GL11.glVertex3f(c1 * radius, halfH, s1 * radius);

            // Bottom cap
            GL11.glNormal3f(0.0f, -1.0f, 0.0f);
            GL11.glTexCoord2f(0.5f, 0.5f); GL11.glVertex3f(0.0f, -halfH, 0.0f);
            GL11.glTexCoord2f(0.5f + c1 * 0.5f, 0.5f + s1 * 0.5f); GL11.glVertex3f(c1 * radius, -halfH, s1 * radius);
            GL11.glTexCoord2f(0.5f + c0 * 0.5f, 0.5f + s0 * 0.5f); GL11.glVertex3f(c0 * radius, -halfH, s0 * radius);
        }

        GL11.glEnd();
        GL11.glEndList();
        return list;
    }

    private static int buildCone(int slices) {
        int list = GL11.glGenLists(1);
        GL11.glNewList(list, GL11.GL_COMPILE);
        GL11.glBegin(GL11.GL_TRIANGLES);

        float radius = 0.5f;
        float h = 1.0f;

        for (int i = 0; i < slices; i++) {
            float a0 = (float) (2.0 * Math.PI * i / slices);
            float a1 = (float) (2.0 * Math.PI * (i + 1) / slices);

            float c0 = (float) Math.cos(a0);
            float s0 = (float) Math.sin(a0);
            float c1 = (float) Math.cos(a1);
            float s1 = (float) Math.sin(a1);

            // Cone side triangle
            float midAngle = (a0 + a1) * 0.5f;
            float nx = (float) Math.cos(midAngle);
            float nz = (float) Math.sin(midAngle);
            float ny = radius / h;
            float invLen = 1.0f / (float) Math.sqrt(nx * nx + ny * ny + nz * nz);

            GL11.glNormal3f(nx * invLen, ny * invLen, nz * invLen);
            GL11.glTexCoord2f(0.5f, 1.0f); GL11.glVertex3f(0.0f, h * 0.5f, 0.0f);
            GL11.glTexCoord2f((float) i / slices, 0.0f); GL11.glVertex3f(c0 * radius, -h * 0.5f, s0 * radius);
            GL11.glTexCoord2f((float) (i + 1) / slices, 0.0f); GL11.glVertex3f(c1 * radius, -h * 0.5f, s1 * radius);

            // Base cap
            GL11.glNormal3f(0.0f, -1.0f, 0.0f);
            GL11.glTexCoord2f(0.5f, 0.5f); GL11.glVertex3f(0.0f, -h * 0.5f, 0.0f);
            GL11.glTexCoord2f(0.5f + c1 * 0.5f, 0.5f + s1 * 0.5f); GL11.glVertex3f(c1 * radius, -h * 0.5f, s1 * radius);
            GL11.glTexCoord2f(0.5f + c0 * 0.5f, 0.5f + s0 * 0.5f); GL11.glVertex3f(c0 * radius, -h * 0.5f, s0 * radius);
        }

        GL11.glEnd();
        GL11.glEndList();
        return list;
    }

    private static int buildPlane() {
        int list = GL11.glGenLists(1);
        GL11.glNewList(list, GL11.GL_COMPILE);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glNormal3f(0.0f, 1.0f, 0.0f);
        GL11.glTexCoord2f(0.0f, 0.0f); GL11.glVertex3f(-0.5f, 0.0f, -0.5f);
        GL11.glTexCoord2f(1.0f, 0.0f); GL11.glVertex3f(0.5f, 0.0f, -0.5f);
        GL11.glTexCoord2f(1.0f, 1.0f); GL11.glVertex3f(0.5f, 0.0f, 0.5f);
        GL11.glTexCoord2f(0.0f, 1.0f); GL11.glVertex3f(-0.5f, 0.0f, 0.5f);
        GL11.glEnd();
        GL11.glEndList();
        return list;
    }
}

