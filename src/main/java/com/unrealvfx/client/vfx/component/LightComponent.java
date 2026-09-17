package com.unrealvfx.client.vfx.component;

import com.unrealvfx.client.render.BloomRenderer;
import com.unrealvfx.client.render.DepthCaptureHelper;
import com.unrealvfx.client.render.MeshCache;
import com.unrealvfx.client.render.RenderState;
import com.unrealvfx.client.render.ShaderProgram;
import com.unrealvfx.client.vfx.VFXComponent;
import com.unrealvfx.client.vfx.VFXInstance;
import com.unrealvfx.client.vfx.VFXManager;
import net.minecraft.client.Minecraft;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Componente per sorgenti di luce volumetriche, faretti, colonne celesti e illuminazione dinamica del mondo.
 * Supporta forme sferiche (POINT), coniche (SPOT), colonne cilindriche (COLUMN),
 * decadimento regolabile, penombra morbida, pulviscolo procedurale, flicker,
 * illuminazione fisica differita del terreno/blocchi, God Rays volumetrici e Lens Flare ottico.
 */
public class LightComponent implements VFXComponent {

    public enum LightShape {
        POINT,
        SPOT,
        COLUMN,
        DIRECTIONAL,
        AREA_DISC
    }

    private static final FloatBuffer matrixBuf = ByteBuffer.allocateDirect(16 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
    private static final FloatBuffer projBuf = ByteBuffer.allocateDirect(16 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();

    private LightShape shape = LightShape.POINT;
    private String shaderName = "volumetric_sacred";
    private float[] color = new float[]{1.0f, 0.85f, 0.40f};
    private float[] secondaryColor = new float[]{0.2f, 0.9f, 0.8f};
    private float[] coreColor = new float[]{1.0f, 1.0f, 1.0f};
    private float intensity = 1.5f;
    private float radius = 8.0f;
    private float falloffPower = 2.0f;
    private float coreWidth = 0.35f;
    private float beamSpeed = 1.0f;
    private float patternDensity = 3.0f;
    private float fringeStrength = 0.5f;

    // Parametri specifici per SPOT (faretto conico)
    private float spotAngle = 45.0f; // in gradi
    private float penumbra = 0.5f;   // 0.0 (netto) - 1.0 (ultra-sfumato)

    // Offset e orientamento 3D
    private double offsetX = 0.0;
    private double offsetY = 0.0;
    private double offsetZ = 0.0;
    private float rotYaw = 0.0f;
    private float rotPitch = 0.0f;
    private float rotRoll = 0.0f;

    // Pulviscolo e nebbia volumetrica
    private boolean volumetricNoise = true;
    private float noiseSpeed = 1.5f;
    private float noiseScale = 3.0f;
    private float dustDensity = 0.4f;

    // Dinamiche di animazione
    private boolean flicker = false;
    private float flickerSpeed = 12.0f;
    private float flickerIntensity = 0.20f;
    private boolean pulsing = false;
    private float pulseSpeed = 3.0f;
    private float pulseIntensity = 0.30f;

    // Post-processing e Shader Features avanzate
    private float bloomMultiplier = 2.0f;

    // 1. Illuminazione differita colorata del terreno/blocchi (Deferred Terrain Lighting)
    private boolean lightTerrain = false;
    private float terrainIntensity = 0.45f;
    private float terrainSpecular = 0.35f;

    // 2. Raggi di luce volumetrici (Crepuscular Rays / God Rays)
    private boolean godRays = false;
    private float godRayDensity = 0.65f;
    private float godRayDecay = 0.93f;
    private float godRayWeight = 0.025f;

    // 3. Lens Flare ottico e corona anamorfica
    private boolean lensFlare = false;
    private float flareSize = 0.8f;
    private float flareIntensity = 0.35f;

    // Illuminazione fisica dei blocchi Minecraft
    private boolean illuminateWorld = true;
    private int blockLightLevel = 15;
    private int lastLightX = Integer.MIN_VALUE;
    private int lastLightY = Integer.MIN_VALUE;
    private int lastLightZ = Integer.MIN_VALUE;

    public LightComponent() {}

    public LightComponent(LightShape shape, float radius, float intensity) {
        this.shape = shape != null ? shape : LightShape.POINT;
        this.radius = radius;
        this.intensity = intensity;
    }

    @Override
    public void onDeath(VFXInstance instance) {
        cleanupLight(instance);
    }

    @Override
    public void onRemove(VFXInstance instance) {
        cleanupLight(instance);
    }

    public void cleanupLight(VFXInstance instance) {
        if (lastLightX != Integer.MIN_VALUE && instance != null) {
            World world = instance.getWorld();
            if (world != null && world.blockExists(lastLightX, lastLightY, lastLightZ)) {
                try {
                    world.updateLightByType(EnumSkyBlock.Block, lastLightX, lastLightY, lastLightZ);
                } catch (Throwable ignored) {}
            }
            lastLightX = Integer.MIN_VALUE;
            lastLightY = Integer.MIN_VALUE;
            lastLightZ = Integer.MIN_VALUE;
        }
    }

    @Override
    public void tick(VFXInstance instance) {
        if (!illuminateWorld || instance == null) return;

        World world = instance.getWorld();
        if (world == null) return;

        try {
            if (instance.isDead()) {
                cleanupLight(instance);
                return;
            }

            int bx = (int) Math.floor(instance.getCurrentX() + offsetX);
            int by = (int) Math.floor(instance.getCurrentY() + offsetY);
            int bz = (int) Math.floor(instance.getCurrentZ() + offsetZ);

            if (!world.blockExists(bx, by, bz)) {
                return;
            }

            if (lastLightX != Integer.MIN_VALUE && (lastLightX != bx || lastLightY != by || lastLightZ != bz)) {
                if (world.blockExists(lastLightX, lastLightY, lastLightZ)) {
                    world.updateLightByType(EnumSkyBlock.Block, lastLightX, lastLightY, lastLightZ);
                }
            }

            int lightVal = Math.max(0, Math.min(15, blockLightLevel));
            world.setLightValue(EnumSkyBlock.Block, bx, by, bz, lightVal);
            lastLightX = bx;
            lastLightY = by;
            lastLightZ = bz;
        } catch (Throwable ignored) {
        }
    }

    @Override
    public void render(VFXInstance instance, float partialTicks, float progress) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null) return;
        int width = mc.displayWidth;
        int height = mc.displayHeight;
        if (width <= 0 || height <= 0) return;

        float exactAge = (instance != null ? instance.getAge() : 0.0f) + partialTicks;

        // Modulazione tremolio (flicker) e pulsazione
        float flickerVal = 1.0f;
        if (flicker) {
            float wave = (float) Math.sin(exactAge * flickerSpeed * 0.35f);
            float pseudoRandom = (float) Math.sin(exactAge * 97.13f) * 0.5f;
            flickerVal = 1.0f + (wave * 0.5f + pseudoRandom * 0.5f) * flickerIntensity;
        }

        float pulseVal = 1.0f;
        if (pulsing) {
            pulseVal = 1.0f + (float) Math.sin(exactAge * pulseSpeed * 0.25f) * pulseIntensity;
        }

        float effectiveIntensity = intensity * Math.max(0.0f, flickerVal * pulseVal);
        float mod = instance != null ? instance.getScaleModifier() : 1.0f;
        float currentRadius = radius * mod;

        // Calcolo posizione View-Space e Screen-Space della luce
        matrixBuf.rewind();
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, matrixBuf);
        float m0 = matrixBuf.get(0), m1 = matrixBuf.get(1), m2 = matrixBuf.get(2);
        float m4 = matrixBuf.get(4), m5 = matrixBuf.get(5), m6 = matrixBuf.get(6);
        float m8 = matrixBuf.get(8), m9 = matrixBuf.get(9), m10 = matrixBuf.get(10);
        float m12 = matrixBuf.get(12), m13 = matrixBuf.get(13), m14 = matrixBuf.get(14);

        float ox = (float) offsetX;
        float oy = (float) offsetY;
        float oz = (float) offsetZ;

        float lightViewX = m0 * ox + m4 * oy + m8 * oz + m12;
        float lightViewY = m1 * ox + m5 * oy + m9 * oz + m13;
        float lightViewZ = m2 * ox + m6 * oy + m10 * oz + m14;

        projBuf.rewind();
        GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, projBuf);
        float p0 = projBuf.get(0), p5 = projBuf.get(5), p10 = projBuf.get(10), p14 = projBuf.get(14);

        float clipX = p0 * lightViewX;
        float clipY = p5 * lightViewY;
        float clipZ = p10 * lightViewZ + p14;
        float clipW = -lightViewZ;

        float screenU = 0.5f, screenV = 0.5f, screenZ = 0.5f;
        boolean inFront = clipW > 0.1f;
        if (inFront) {
            screenU = (clipX / clipW) * 0.5f + 0.5f;
            screenV = (clipY / clipW) * 0.5f + 0.5f;
            screenZ = (clipZ / clipW) * 0.5f + 0.5f;
        }

        float tanHalfFov = p5 != 0.0f ? 1.0f / p5 : 1.0f;
        float aspectRatio = (p0 != 0.0f && p5 != 0.0f) ? p5 / p0 : (float) width / (float) height;

        // Calcolo vettore direzione spot in View-Space se forma SPOT
        float spotDirViewX = 0.0f, spotDirViewY = -1.0f, spotDirViewZ = 0.0f;
        if (shape == LightShape.SPOT) {
            spotDirViewX = -m4;
            spotDirViewY = -m5;
            spotDirViewZ = -m6;
            float len = (float) Math.sqrt(spotDirViewX * spotDirViewX + spotDirViewY * spotDirViewY + spotDirViewZ * spotDirViewZ);
            if (len > 0.0001f) {
                spotDirViewX /= len;
                spotDirViewY /= len;
                spotDirViewZ /= len;
            }
        }

        // Assicura binding texture depth unit 1 per tutti i passaggi
        DepthCaptureHelper.bindToUnit(1);

        // ========================================================
        // PASS 1: Volume geometrico visibile (Volumetric Mesh)
        // ========================================================
        String activeShaderName = (shaderName != null && !shaderName.trim().isEmpty()) ? shaderName.trim() : "volumetric_sacred";
        if ("default".equalsIgnoreCase(activeShaderName) || "volumetric".equalsIgnoreCase(activeShaderName)) {
            activeShaderName = "volumetric_sacred";
        }
        ShaderProgram shader = VFXManager.getInstance().getShader(activeShaderName);
        if (shader == null || !shader.isValid()) {
            shader = VFXManager.getInstance().getShader("volumetric_light");
        }
        if (shader == null || !shader.isValid()) {
            // Se lo shader non compila, interrompi per evitare di renderizzare geometria bianca non ombreggiata
            return;
        }

        RenderState.BlendMode blend = "abyssal_void_light".equalsIgnoreCase(activeShaderName) ? RenderState.BlendMode.NORMAL : RenderState.BlendMode.ADDITIVE;
        boolean cull = "abyssal_void_light".equalsIgnoreCase(activeShaderName) && shape == LightShape.POINT;
        RenderState.begin(blend, false, cull);
        shader.use();
        shader.setInt("u_shape", shape.ordinal());
        shader.setVec3("u_color", color[0], color[1], color[2]);
        shader.setVec3("u_secondaryColor", secondaryColor[0], secondaryColor[1], secondaryColor[2]);
        shader.setVec3("u_coreColor", coreColor[0], coreColor[1], coreColor[2]);
        shader.setFloat("u_intensity", effectiveIntensity);
        shader.setFloat("u_falloffPower", falloffPower);
        shader.setFloat("u_spotAngle", (float) Math.toRadians(spotAngle));
        shader.setFloat("u_penumbra", penumbra);
        shader.setFloat("u_time", exactAge * 0.05f);
        shader.setInt("u_hasNoise", volumetricNoise ? 1 : 0);
        shader.setFloat("u_noiseSpeed", noiseSpeed);
        shader.setFloat("u_noiseScale", noiseScale);
        shader.setFloat("u_dustDensity", dustDensity);
        shader.setFloat("u_flicker", flickerVal);
        shader.setFloat("u_pulse", pulseVal);

        shader.setFloat("u_coreWidth", coreWidth);
        shader.setFloat("u_beamSpeed", beamSpeed);
        shader.setFloat("u_patternDensity", patternDensity);
        shader.setFloat("u_fringeStrength", fringeStrength);

        float bloomFactor = BloomRenderer.isCapturing() ? bloomMultiplier * 1.6f : bloomMultiplier;
        shader.setFloat("u_bloomMultiplier", bloomFactor);
        DepthCaptureHelper.setupShaderDepthUniforms(shader, 1.2f);

        GL11.glPushMatrix();
        GL11.glTranslated(offsetX, offsetY, offsetZ);

        if (rotYaw != 0.0f) GL11.glRotatef(rotYaw, 0.0f, 1.0f, 0.0f);
        if (rotPitch != 0.0f) GL11.glRotatef(rotPitch, 1.0f, 0.0f, 0.0f);
        if (rotRoll != 0.0f) GL11.glRotatef(rotRoll, 0.0f, 0.0f, 1.0f);

        switch (shape) {
            case SPOT: {
                float baseR = currentRadius * (float) Math.tan(Math.toRadians(spotAngle * 0.5f));
                GL11.glScalef(baseR * 2.0f, currentRadius, baseR * 2.0f);
                GL11.glTranslatef(0.0f, -0.5f, 0.0f);
                MeshCache.renderCone();
                break;
            }
            case COLUMN: {
                GL11.glScalef(currentRadius, currentRadius * 3.5f, currentRadius);
                MeshCache.renderCylinder();
                break;
            }
            case DIRECTIONAL: {
                GL11.glScalef(currentRadius, currentRadius, currentRadius);
                MeshCache.renderBillboardQuad();
                break;
            }
            case AREA_DISC: {
                GL11.glScalef(currentRadius, 1.0f, currentRadius);
                MeshCache.renderDisc();
                break;
            }
            case POINT:
            default: {
                GL11.glScalef(currentRadius, currentRadius, currentRadius);
                MeshCache.renderSphere();
                break;
            }
        }
        GL11.glPopMatrix();

        ShaderProgram.release();
        RenderState.end();

        // ========================================================
        // PASS 2: Illuminazione Dinamica Differita Terreno/Blocchi
        // ========================================================
        if (lightTerrain && !BloomRenderer.isCapturing()) {
            ShaderProgram terrainShader = VFXManager.getInstance().getShader("world_light_inject");
            if (terrainShader != null && terrainShader.isValid()) {
                RenderState.begin(RenderState.BlendMode.ADDITIVE, false, false);
                terrainShader.use();
                terrainShader.setInt("u_depthTexture", 1);
                terrainShader.setVec2("u_screenResolution", (float) width, (float) height);
                terrainShader.setVec3("u_lightPos", lightViewX, lightViewY, lightViewZ);
                terrainShader.setVec3("u_lightColor", color[0], color[1], color[2]);
                terrainShader.setFloat("u_radius", currentRadius);
                terrainShader.setFloat("u_intensity", effectiveIntensity * terrainIntensity);
                terrainShader.setFloat("u_falloffPower", falloffPower);
                terrainShader.setFloat("u_specular", terrainSpecular);

                float near = 0.05f;
                float far = Math.max(16.0f, (float) mc.gameSettings.renderDistanceChunks * 16.0f);
                terrainShader.setFloat("u_near", near);
                terrainShader.setFloat("u_far", far);
                terrainShader.setFloat("u_tanHalfFov", tanHalfFov);
                terrainShader.setFloat("u_aspectRatio", aspectRatio);

                terrainShader.setInt("u_isSpot", shape == LightShape.SPOT ? 1 : 0);
                terrainShader.setVec3("u_spotDir", spotDirViewX, spotDirViewY, spotDirViewZ);
                terrainShader.setFloat("u_spotAngle", (float) Math.toRadians(spotAngle));
                terrainShader.setFloat("u_penumbra", penumbra);

                renderScreenQuad(width, height);
                ShaderProgram.release();
                RenderState.end();
            }
        }

        // ========================================================
        // PASS 3: Raggi di Luce Volumetrici (Crepuscular God Rays)
        // ========================================================
        if (godRays && inFront && !BloomRenderer.isCapturing()) {
            ShaderProgram godRayShader = VFXManager.getInstance().getShader("god_rays");
            if (godRayShader != null && godRayShader.isValid()) {
                RenderState.begin(RenderState.BlendMode.ADDITIVE, false, false);
                godRayShader.use();
                godRayShader.setInt("u_depthTexture", 1);
                godRayShader.setVec2("u_screenResolution", (float) width, (float) height);
                godRayShader.setVec3("u_lightScreenPos", screenU, screenV, screenZ);
                godRayShader.setVec3("u_lightColor", color[0], color[1], color[2]);
                godRayShader.setVec3("u_coreColor", coreColor[0], coreColor[1], coreColor[2]);
                godRayShader.setFloat("u_intensity", effectiveIntensity);
                godRayShader.setFloat("u_density", godRayDensity);
                godRayShader.setFloat("u_decay", godRayDecay);
                godRayShader.setFloat("u_weight", godRayWeight);

                renderScreenQuad(width, height);
                ShaderProgram.release();
                RenderState.end();
            }
        }

        // ========================================================
        // PASS 4: Lens Flare Ottico e Corona Anamorfica
        // ========================================================
        if (lensFlare && inFront && !BloomRenderer.isCapturing()) {
            ShaderProgram flareShader = VFXManager.getInstance().getShader("lens_flare");
            if (flareShader != null && flareShader.isValid()) {
                RenderState.begin(RenderState.BlendMode.ADDITIVE, false, false);
                flareShader.use();
                flareShader.setInt("u_depthTexture", 1);
                flareShader.setVec2("u_screenResolution", (float) width, (float) height);
                flareShader.setVec3("u_lightScreenPos", screenU, screenV, screenZ);
                flareShader.setVec3("u_color", color[0], color[1], color[2]);
                flareShader.setVec3("u_coreColor", coreColor[0], coreColor[1], coreColor[2]);
                flareShader.setFloat("u_flareIntensity", flareIntensity * (BloomRenderer.isCapturing() ? 1.5f : 1.0f));
                flareShader.setFloat("u_time", exactAge * 0.05f);

                GL11.glPushMatrix();
                GL11.glTranslated(offsetX, offsetY, offsetZ);

                // Billboard quad rivolto sempre verso la telecamera
                GL11.glRotatef(-mc.renderViewEntity.rotationYaw, 0.0F, 1.0F, 0.0F);
                GL11.glRotatef(mc.renderViewEntity.rotationPitch, 1.0F, 0.0F, 0.0F);

                float flareScale = flareSize * mod * (shape == LightShape.SPOT ? 2.5f : 2.0f);
                GL11.glScalef(flareScale, flareScale, flareScale);
                MeshCache.renderBillboardQuad();

                GL11.glPopMatrix();

                ShaderProgram.release();
                RenderState.end();
            }
        }
    }

    private void renderScreenQuad(int width, int height) {
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        GL11.glOrtho(0.0D, width, height, 0.0D, -100.0D, 100.0D);

        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();

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

    // Getters e Setters con pattern Fluent Builder
    public LightShape getShape() { return shape; }
    public LightComponent setShape(LightShape shape) {
        this.shape = shape != null ? shape : LightShape.POINT;
        return this;
    }

    public float[] getColor() { return color; }
    public LightComponent setColor(float r, float g, float b) {
        this.color = new float[]{r, g, b};
        return this;
    }

    public float[] getCoreColor() { return coreColor; }
    public LightComponent setCoreColor(float r, float g, float b) {
        this.coreColor = new float[]{r, g, b};
        return this;
    }

    public float getIntensity() { return intensity; }
    public LightComponent setIntensity(float intensity) {
        this.intensity = intensity;
        return this;
    }

    public float getRadius() { return radius; }
    public LightComponent setRadius(float radius) {
        this.radius = radius;
        return this;
    }

    public float getFalloffPower() { return falloffPower; }
    public LightComponent setFalloffPower(float falloffPower) {
        this.falloffPower = falloffPower;
        return this;
    }

    public float getSpotAngle() { return spotAngle; }
    public LightComponent setSpotAngle(float spotAngle) {
        this.spotAngle = spotAngle;
        return this;
    }

    public float getPenumbra() { return penumbra; }
    public LightComponent setPenumbra(float penumbra) {
        this.penumbra = penumbra;
        return this;
    }

    public double getOffsetX() { return offsetX; }
    public double getOffsetY() { return offsetY; }
    public double getOffsetZ() { return offsetZ; }
    public LightComponent setOffset(double x, double y, double z) {
        this.offsetX = x;
        this.offsetY = y;
        this.offsetZ = z;
        return this;
    }

    public float getRotYaw() { return rotYaw; }
    public float getRotPitch() { return rotPitch; }
    public float getRotRoll() { return rotRoll; }
    public LightComponent setRotation(float yaw, float pitch, float roll) {
        this.rotYaw = yaw;
        this.rotPitch = pitch;
        this.rotRoll = roll;
        return this;
    }

    public boolean isVolumetricNoise() { return volumetricNoise; }
    public LightComponent setVolumetricNoise(boolean volumetricNoise) {
        this.volumetricNoise = volumetricNoise;
        return this;
    }

    public float getNoiseSpeed() { return noiseSpeed; }
    public LightComponent setNoiseSpeed(float noiseSpeed) {
        this.noiseSpeed = noiseSpeed;
        return this;
    }

    public float getNoiseScale() { return noiseScale; }
    public LightComponent setNoiseScale(float noiseScale) {
        this.noiseScale = noiseScale;
        return this;
    }

    public float getDustDensity() { return dustDensity; }
    public LightComponent setDustDensity(float dustDensity) {
        this.dustDensity = dustDensity;
        return this;
    }

    public boolean isFlicker() { return flicker; }
    public LightComponent setFlicker(boolean flicker) {
        this.flicker = flicker;
        return this;
    }

    public float getFlickerSpeed() { return flickerSpeed; }
    public LightComponent setFlickerSpeed(float flickerSpeed) {
        this.flickerSpeed = flickerSpeed;
        return this;
    }

    public float getFlickerIntensity() { return flickerIntensity; }
    public LightComponent setFlickerIntensity(float flickerIntensity) {
        this.flickerIntensity = flickerIntensity;
        return this;
    }

    public boolean isPulsing() { return pulsing; }
    public LightComponent setPulsing(boolean pulsing) {
        this.pulsing = pulsing;
        return this;
    }

    public float getPulseSpeed() { return pulseSpeed; }
    public LightComponent setPulseSpeed(float pulseSpeed) {
        this.pulseSpeed = pulseSpeed;
        return this;
    }

    public float getPulseIntensity() { return pulseIntensity; }
    public LightComponent setPulseIntensity(float pulseIntensity) {
        this.pulseIntensity = pulseIntensity;
        return this;
    }

    public float getBloomMultiplier() { return bloomMultiplier; }
    public LightComponent setBloomMultiplier(float bloomMultiplier) {
        this.bloomMultiplier = bloomMultiplier;
        return this;
    }

    // Shader-grade features
    public boolean isLightTerrain() { return lightTerrain; }
    public LightComponent setLightTerrain(boolean lightTerrain) {
        this.lightTerrain = lightTerrain;
        return this;
    }

    public float getTerrainIntensity() { return terrainIntensity; }
    public LightComponent setTerrainIntensity(float terrainIntensity) {
        this.terrainIntensity = terrainIntensity;
        return this;
    }

    public float getTerrainSpecular() { return terrainSpecular; }
    public LightComponent setTerrainSpecular(float terrainSpecular) {
        this.terrainSpecular = terrainSpecular;
        return this;
    }

    public boolean isGodRays() { return godRays; }
    public LightComponent setGodRays(boolean godRays) {
        this.godRays = godRays;
        return this;
    }

    public float getGodRayDensity() { return godRayDensity; }
    public LightComponent setGodRayDensity(float godRayDensity) {
        this.godRayDensity = godRayDensity;
        return this;
    }

    public float getGodRayDecay() { return godRayDecay; }
    public LightComponent setGodRayDecay(float godRayDecay) {
        this.godRayDecay = godRayDecay;
        return this;
    }

    public float getGodRayWeight() { return godRayWeight; }
    public LightComponent setGodRayWeight(float godRayWeight) {
        this.godRayWeight = godRayWeight;
        return this;
    }

    public boolean isLensFlare() { return lensFlare; }
    public LightComponent setLensFlare(boolean lensFlare) {
        this.lensFlare = lensFlare;
        return this;
    }

    public float getFlareSize() { return flareSize; }
    public LightComponent setFlareSize(float flareSize) {
        this.flareSize = flareSize;
        return this;
    }

    public float getFlareIntensity() { return flareIntensity; }
    public LightComponent setFlareIntensity(float flareIntensity) {
        this.flareIntensity = flareIntensity;
        return this;
    }

    public boolean isIlluminateWorld() { return illuminateWorld; }
    public LightComponent setIlluminateWorld(boolean illuminateWorld) {
        this.illuminateWorld = illuminateWorld;
        return this;
    }

    public int getBlockLightLevel() { return blockLightLevel; }
    public LightComponent setBlockLightLevel(int blockLightLevel) {
        this.blockLightLevel = blockLightLevel;
        return this;
    }

    public String getShaderName() { return shaderName; }
    public LightComponent setShaderName(String shaderName) {
        this.shaderName = shaderName != null ? shaderName : "volumetric_sacred";
        return this;
    }
    public LightComponent setShader(String shader) { return setShaderName(shader); }
    public String getShader() { return getShaderName(); }

    public float[] getSecondaryColor() { return secondaryColor; }
    public LightComponent setSecondaryColor(float r, float g, float b) {
        this.secondaryColor = new float[]{r, g, b};
        return this;
    }

    public float getCoreWidth() { return coreWidth; }
    public LightComponent setCoreWidth(float coreWidth) {
        this.coreWidth = coreWidth;
        return this;
    }

    public float getBeamSpeed() { return beamSpeed; }
    public LightComponent setBeamSpeed(float beamSpeed) {
        this.beamSpeed = beamSpeed;
        return this;
    }

    public float getPatternDensity() { return patternDensity; }
    public LightComponent setPatternDensity(float patternDensity) {
        this.patternDensity = patternDensity;
        return this;
    }

    public float getFringeStrength() { return fringeStrength; }
    public LightComponent setFringeStrength(float fringeStrength) {
        this.fringeStrength = fringeStrength;
        return this;
    }

    public int getLastLightX() { return lastLightX; }
    public int getLastLightY() { return lastLightY; }
    public int getLastLightZ() { return lastLightZ; }
    public void setLastLightPos(int x, int y, int z) {
        this.lastLightX = x;
        this.lastLightY = y;
        this.lastLightZ = z;
    }
}
