package com.unrealvfx.client.render;

import org.lwjgl.opengl.GL11;

/**
 * Gestore sicuro dello stato OpenGL.
 * Isola completamente il rendering dei VFX dal pipeline di Minecraft,
 * evitando glitch grafici con blocchi, entità, shader di OptiFine o altre mod.
 */
public class RenderState {

    public enum BlendMode {
        NORMAL(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA),
        ADDITIVE(GL11.GL_SRC_ALPHA, GL11.GL_ONE),
        MULTIPLY(GL11.GL_DST_COLOR, GL11.GL_ZERO),
        SCREEN(GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_COLOR),
        PREMULTIPLIED_ALPHA(GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);

        public final int src;
        public final int dst;

        BlendMode(int src, int dst) {
            this.src = src;
            this.dst = dst;
        }
    }

    /**
     * Inizia una fase di rendering VFX isolando lo stato OpenGL.
     */
    public static void begin(BlendMode blendMode, boolean depthMask, boolean cullFace) {
        // Salva lo stato OpenGL corrente
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glPushMatrix();

        // Disabilita l'illuminazione vanilla per rendere l'effetto auto-illuminante (fullbright / ki emission)
        GL11.glDisable(GL11.GL_LIGHTING);

        // Abilita la trasparenza con il blend mode richiesto
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(blendMode.src, blendMode.dst);

        // Depth test sempre attivo, ma depth mask disattivabile per evitare artefatti di sorting
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(depthMask);

        // Culling delle facce posteriori (per auras spesso vogliamo vedere anche l'interno: double-sided)
        if (cullFace) {
            GL11.glEnable(GL11.GL_CULL_FACE);
        } else {
            GL11.glDisable(GL11.GL_CULL_FACE);
        }

        // Abilita texture 2D
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        // Resetta il colore a bianco opaco iniziale
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
    }

    /**
     * Ripristina integralmente lo stato OpenGL precedente.
     */
    public static void end() {
        GL11.glPopMatrix();
        GL11.glPopAttrib();
    }
}
