package com.unrealvfx.client.render;

import org.lwjgl.opengl.GL11;
import java.util.ArrayList;
import java.util.List;

/**
 * Generatore e renderizzatore di mesh 3D volumetriche per raggi curvi ed estrusi (Beam / Laser).
 * Utilizza il Bishop Frame (Parallel Transport Frame / Rotation-Minimizing Frame - RMF)
 * con l'algoritmo Double Reflection (Wang et al., 2008) per eliminare del tutto
 * singolarita geometriche, torsioni incontrollate (twist artifacts) e gimbal lock.
 */
public class CurvedBeamMesh {

    public static class Vector3 {
        public float x, y, z;

        public Vector3(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public Vector3 sub(Vector3 o) {
            return new Vector3(x - o.x, y - o.y, z - o.z);
        }

        public Vector3 add(Vector3 o) {
            return new Vector3(x + o.x, y + o.y, z + o.z);
        }

        public Vector3 mul(float s) {
            return new Vector3(x * s, y * s, z * s);
        }

        public float dot(Vector3 o) {
            return x * o.x + y * o.y + z * o.z;
        }

        public Vector3 cross(Vector3 o) {
            return new Vector3(y * o.z - z * o.y, z * o.x - x * o.z, x * o.y - y * o.x);
        }

        public float len() {
            return (float) Math.sqrt(x * x + y * y + z * z);
        }

        public Vector3 norm() {
            float l = len();
            return l > 1e-6f ? new Vector3(x / l, y / l, z / l) : new Vector3(0, 1, 0);
        }
    }

    /**
     * Campiona una spline Catmull-Rom centripeta (alpha = 0.5) tra una lista di punti di controllo.
     */
    public static List<Vector3> interpolateSpline(List<Vector3> controlPoints, int samplesPerSegment) {
        List<Vector3> sampled = new ArrayList<Vector3>();
        if (controlPoints.size() < 2) return sampled;
        if (controlPoints.size() == 2) {
            for (int i = 0; i <= samplesPerSegment; i++) {
                float t = (float) i / samplesPerSegment;
                sampled.add(controlPoints.get(0).mul(1.0f - t).add(controlPoints.get(1).mul(t)));
            }
            return sampled;
        }

        int count = controlPoints.size();
        for (int i = 0; i < count - 1; i++) {
            Vector3 p0 = controlPoints.get(Math.max(0, i - 1));
            Vector3 p1 = controlPoints.get(i);
            Vector3 p2 = controlPoints.get(i + 1);
            Vector3 p3 = controlPoints.get(Math.min(count - 1, i + 2));

            for (int s = 0; s < samplesPerSegment; s++) {
                float t = (float) s / (float) samplesPerSegment;
                sampled.add(evaluateCatmullRom(p0, p1, p2, p3, t));
            }
        }
        sampled.add(controlPoints.get(count - 1));
        return sampled;
    }

    private static Vector3 evaluateCatmullRom(Vector3 p0, Vector3 p1, Vector3 p2, Vector3 p3, float t) {
        float t2 = t * t;
        float t3 = t2 * t;
        float f1 = -0.5f * t3 + t2 - 0.5f * t;
        float f2 =  1.5f * t3 - 2.5f * t2 + 1.0f;
        float f3 = -1.5f * t3 + 2.0f * t2 + 0.5f * t;
        float f4 =  0.5f * t3 - 0.5f * t2;
        return p0.mul(f1).add(p1.mul(f2)).add(p2.mul(f3)).add(p3.mul(f4));
    }

    /**
     * Renderizza un raggio continuo a tubo cilindrico a rotazione minima (Bishop Frame / RMF).
     * @param path Punti 3D lungo il tragitto del raggio
     * @param radius Raggio della sezione trasversale
     * @param radialSegments Segmenti angolari del cilindro (es. 16 o 24)
     * @param tileLength Passo in unita metriche di ripetizione della coordinata V
     */
    public static void renderTube(List<Vector3> path, float radius, int radialSegments, float tileLength) {
        int n = path.size();
        if (n < 2) return;

        Vector3[] T = new Vector3[n];
        Vector3[] R = new Vector3[n];
        Vector3[] S = new Vector3[n];
        float[] vCoords = new float[n];

        // 1. Calcolo Tangenti e Lunghezze d'Arco V
        vCoords[0] = 0.0f;
        for (int i = 0; i < n; i++) {
            if (i < n - 1) {
                T[i] = path.get(i + 1).sub(path.get(i)).norm();
                if (i > 0) {
                    vCoords[i] = vCoords[i - 1] + path.get(i).sub(path.get(i - 1)).len() / tileLength;
                }
            } else {
                T[i] = T[i - 1];
                vCoords[i] = vCoords[i - 1] + path.get(i).sub(path.get(i - 1)).len() / tileLength;
            }
        }

        // 2. Base Iniziale non singolare
        Vector3 initUp = Math.abs(T[0].y) < 0.9f ? new Vector3(0, 1, 0) : new Vector3(1, 0, 0);
        R[0] = T[0].cross(initUp).norm();
        S[0] = T[0].cross(R[0]).norm();

        // 3. Trasporto Parallelo (Double Reflection RMF - Wang et al. 2008)
        for (int i = 0; i < n - 1; i++) {
            Vector3 v1 = path.get(i + 1).sub(path.get(i));
            float c1 = v1.dot(v1);
            if (c1 < 1e-7f) {
                R[i + 1] = R[i];
                S[i + 1] = S[i];
                continue;
            }

            Vector3 rL = R[i].sub(v1.mul((2.0f / c1) * v1.dot(R[i])));
            Vector3 tL = T[i].sub(v1.mul((2.0f / c1) * v1.dot(T[i])));

            Vector3 v2 = T[i + 1].sub(tL);
            float c2 = v2.dot(v2);
            if (c2 < 1e-7f) {
                R[i + 1] = rL;
            } else {
                R[i + 1] = rL.sub(v2.mul((2.0f / c2) * v2.dot(rL)));
            }
            R[i + 1] = R[i + 1].norm();
            S[i + 1] = T[i + 1].cross(R[i + 1]).norm();
        }

        // 4. Rendering con vertici condivisi (GL_TRIANGLES)
        GL11.glBegin(GL11.GL_TRIANGLES);
        for (int i = 0; i < n - 1; i++) {
            Vector3 pA = path.get(i);
            Vector3 pB = path.get(i + 1);
            float vA = vCoords[i];
            float vB = vCoords[i + 1];

            for (int k = 0; k < radialSegments; k++) {
                float a0 = (float) (2.0 * Math.PI * k / radialSegments);
                float a1 = (float) (2.0 * Math.PI * (k + 1) / radialSegments);
                float u0 = (float) k / radialSegments;
                float u1 = (float) (k + 1) / radialSegments;

                Vector3 nA0 = R[i].mul((float) Math.cos(a0)).add(S[i].mul((float) Math.sin(a0)));
                Vector3 nA1 = R[i].mul((float) Math.cos(a1)).add(S[i].mul((float) Math.sin(a1)));
                Vector3 vA0 = pA.add(nA0.mul(radius));
                Vector3 vA1 = pA.add(nA1.mul(radius));

                Vector3 nB0 = R[i + 1].mul((float) Math.cos(a0)).add(S[i + 1].mul((float) Math.sin(a0)));
                Vector3 nB1 = R[i + 1].mul((float) Math.cos(a1)).add(S[i + 1].mul((float) Math.sin(a1)));
                Vector3 vB0 = pB.add(nB0.mul(radius));
                Vector3 vB1 = pB.add(nB1.mul(radius));

                // Triangolo 1
                GL11.glNormal3f(nA0.x, nA0.y, nA0.z);
                GL11.glTexCoord2f(u0, vA);
                GL11.glVertex3f(vA0.x, vA0.y, vA0.z);

                GL11.glNormal3f(nB0.x, nB0.y, nB0.z);
                GL11.glTexCoord2f(u0, vB);
                GL11.glVertex3f(vB0.x, vB0.y, vB0.z);

                GL11.glNormal3f(nA1.x, nA1.y, nA1.z);
                GL11.glTexCoord2f(u1, vA);
                GL11.glVertex3f(vA1.x, vA1.y, vA1.z);

                // Triangolo 2
                GL11.glNormal3f(nA1.x, nA1.y, nA1.z);
                GL11.glTexCoord2f(u1, vA);
                GL11.glVertex3f(vA1.x, vA1.y, vA1.z);

                GL11.glNormal3f(nB0.x, nB0.y, nB0.z);
                GL11.glTexCoord2f(u0, vB);
                GL11.glVertex3f(vB0.x, vB0.y, vB0.z);

                GL11.glNormal3f(nB1.x, nB1.y, nB1.z);
                GL11.glTexCoord2f(u1, vB);
                GL11.glVertex3f(vB1.x, vB1.y, vB1.z);
            }
        }
        GL11.glEnd();
    }

    /**
     * Renderizza un raggio rettilineo continuo lungo l'asse Z positivo con micro-turbolenza opzionale.
     */
    public static void renderStraightBeam(float radius, float length, int radialSegments, float tileLength, float swayAmount, float time) {
        int lengthSegments = Math.max(4, (int) (length / 1.5f));
        List<Vector3> path = new ArrayList<Vector3>(lengthSegments + 1);

        for (int i = 0; i <= lengthSegments; i++) {
            float frac = (float) i / lengthSegments;
            float z = frac * length;

            float x = 0.0f;
            float y = 0.0f;
            if (swayAmount > 0.001f && i > 0 && i < lengthSegments) {
                float wave = z * 0.4f - time * 6.0f;
                x = (float) Math.sin(wave) * swayAmount * (float) Math.sin(frac * Math.PI);
                y = (float) Math.cos(wave * 1.3f) * swayAmount * (float) Math.sin(frac * Math.PI);
            }

            path.add(new Vector3(x, y, z));
        }

        renderTube(path, radius, radialSegments, tileLength);
    }
}
