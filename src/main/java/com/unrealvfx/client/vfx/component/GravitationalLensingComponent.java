package com.unrealvfx.client.vfx.component;

import com.unrealvfx.client.render.BloomRenderer;
import com.unrealvfx.client.render.MeshCache;
import com.unrealvfx.client.render.RenderState;
import com.unrealvfx.client.render.ShaderProgram;
import com.unrealvfx.client.vfx.VFXComponent;
import com.unrealvfx.client.vfx.VFXInstance;
import com.unrealvfx.client.vfx.VFXManager;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import java.nio.ByteBuffer;

/**
 * Componente di Lensing Gravitazionale 3D (Einstein Spacetime Curvature).
 * Ancorato nello spazio 3D del mondo di Minecraft, cattura lo sfondo e curva la luce
 * dei blocchi, del cielo e dell'ambiente circostante attorno alla singolarita,
 * preservando la vera sfera 3D dell'orizzonte degli eventi e il depth testing naturale.
 */
public class GravitationalLensingComponent implements VFXComponent {

    private static int screenTextureId = -1;
    private static int lastWidth = -1;
    private static int lastHeight = -1;

    private float worldScale = 8.5f;
    private float distortionStrength = 0.45f;
    private double offsetY = 0.0;

    public GravitationalLensingComponent(float worldScale, float distortionStrength) {
        this.worldScale = worldScale;
        this.distortionStrength = distortionStrength;
    }

    public GravitationalLensingComponent(float worldScale) {
        this(worldScale, 0.45f);
    }

    @Override
    public void tick(VFXInstance instance) {
    }

    @Override
    public void render(VFXInstance instance, float partialTicks, float progress) {
        // Evita cattura duplicata durante il pass di Bloom per massimizzare le performance
        if (BloomRenderer.isCapturing()) return;

        Minecraft mc = Minecraft.getMinecraft();
        int width = mc.displayWidth;
        int height = mc.displayHeight;

        if (width <= 0 || height <= 0) return;

        Entity viewEntity = mc.renderViewEntity;
        if (viewEntity == null) return;

        // Cattura una tantum del framebuffer del mondo per la deflessione ottica
        if (screenTextureId == -1 || width != lastWidth || height != lastHeight) {
            if (screenTextureId == -1) {
                screenTextureId = GL11.glGenTextures();
            }
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, screenTextureId);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, width, height, 0, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
            lastWidth = width;
            lastHeight = height;
        }

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, screenTextureId);
        GL11.glCopyTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, 0, 0, width, height);

        // Rendering isolato in spazio 3D: depth test attivo per occlusione corretta dai blocchi in primo piano
        RenderState.begin(RenderState.BlendMode.NORMAL, false, false);

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, screenTextureId);

        ShaderProgram shader = VFXManager.getInstance().getShader("gravitational_lens");
        if (shader != null) {
            shader.use();
            shader.setInt("u_screenTexture", 0);
            shader.setVec2("u_screenSize", width, height);
            shader.setFloat("u_strength", distortionStrength);
            shader.setFloat("u_time", (instance.getAge() + partialTicks) * 0.05f);
        }

        GL11.glPushMatrix();
        GL11.glTranslated(0.0, offsetY, 0.0);

        // Allineamento camera-facing nello spazio 3D (il frustum di Minecraft gestisce culling naturale)
        GL11.glRotatef(-viewEntity.rotationYaw, 0.0f, 1.0f, 0.0f);
        GL11.glRotatef(viewEntity.rotationPitch, 1.0f, 0.0f, 0.0f);

        // Dimensione del campo gravitazionale
        float quadScale = worldScale * instance.getScaleModifier() * 2.0f;
        GL11.glScalef(quadScale, quadScale, quadScale);

        MeshCache.renderQuad();

        GL11.glPopMatrix();

        if (shader != null) {
            ShaderProgram.release();
        }

        RenderState.end();
    }

    public GravitationalLensingComponent setWorldScale(float worldScale) {
        this.worldScale = worldScale;
        return this;
    }

    public GravitationalLensingComponent setDistortionStrength(float distortionStrength) {
        this.distortionStrength = distortionStrength;
        return this;
    }

    public GravitationalLensingComponent setOffsetY(double offsetY) {
        this.offsetY = offsetY;
        return this;
    }
}
