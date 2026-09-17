package com.unrealvfx.client.vfx.component;

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
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

/**
 * Componente per il rendering di mesh 3D animate (Sfere energetiche, gusci d'aura, anelli, billboard)
 * con supporto per shader GLSL personalizzati, curve di scala/alpha, rotazioni e billboard alla telecamera.
 */
public class MeshComponent implements VFXComponent {

    public enum MeshType {
        SPHERE,
        AURA_CYLINDER,
        AURA_CAPSULE,
        STAR_FLAME,
        LOW_POLY_ROCK,
        RING,
        THIN_RING,
        DOME,
        DISC,
        BEAM_TUBE,
        QUAD,
        BILLBOARD_QUAD,
        ACCRETION_DISK,
        CUBE,
        BOX,
        CYLINDER,
        CONE,
        PLANE
    }

    private MeshType meshType = MeshType.SPHERE;
    private String shaderName;
    private ResourceLocation texture;

    // Scala 3D Non-Uniforme
    private float startScaleX = 1.0f, startScaleY = 1.0f, startScaleZ = 1.0f;
    private float endScaleX = 1.0f, endScaleY = 1.0f, endScaleZ = 1.0f;
    private Easing scaleEasing = Easing.LINEAR;

    // Pivot Offset (perno di rotazione e scalatura locale)
    private float pivotX = 0.0f, pivotY = 0.0f, pivotZ = 0.0f;

    // Modificatore Cloner / Array (stile MoGraph Cinema 4D)
    private int arrayCount = 1;
    private float arrayStepPosX = 0.0f, arrayStepPosY = 0.0f, arrayStepPosZ = 0.0f;
    private float arrayStepRotPitch = 0.0f, arrayStepRotYaw = 0.0f, arrayStepRotRoll = 0.0f;
    private float arrayStepScaleX = 0.0f, arrayStepScaleY = 0.0f, arrayStepScaleZ = 0.0f;
    private float arrayPhaseDelay = 0.0f;

    // Parametri Shader Multi-Layer Universali
    private float[] hotCoreColor = null;
    private float[] rimColor = null;
    private float rimPower = 3.0f;
    private float noiseSpeed = 1.0f;
    private float noiseScale = 2.0f;
    private float bloomMultiplier = 1.5f;
    private boolean invertedCore = false;
    private float filamentStrength = 0.85f;
    private float distortionStrength = 0.12f;
    private float celBands = 3.0f;
    private float steppedFps = 0.0f;

    private float startAlpha = 1.0f;
    private float endAlpha = 1.0f;
    private Easing alphaEasing = Easing.LINEAR;

    private float[] startColor = new float[]{1.0f, 1.0f, 1.0f};
    private float[] endColor = new float[]{1.0f, 1.0f, 1.0f};
    private Easing colorEasing = Easing.LINEAR;

    private float rotationSpeedYaw = 0.0f;
    private float rotationSpeedPitch = 0.0f;
    private float rotationSpeedRoll = 0.0f;

    private float initialYaw = 0.0f;
    private float initialPitch = 0.0f;
    private float initialRoll = 0.0f;

    private double offsetX = 0.0;
    private double offsetY = 0.0;
    private double offsetZ = 0.0;

    private boolean billboard = false;

    private RenderState.BlendMode blendMode = RenderState.BlendMode.ADDITIVE;
    private boolean depthMask = false;
    private boolean cullFace = false;

    public MeshComponent(MeshType meshType) {
        this.meshType = meshType;
        if (meshType == MeshType.BILLBOARD_QUAD) {
            this.billboard = true;
        }
    }

    @Override
    public void tick(VFXInstance instance) {
    }

    @Override
    public void render(VFXInstance instance, float partialTicks, float progress) {
        float renderProgress = progress;
        float timeTicks = instance.getAge() + partialTicks;

        // Quantized Stepped Sakuga Timing ("On 2s" / 12-16 FPS vs 60 FPS smooth)
        if (steppedFps > 0.0f) {
            float stepTicks = 20.0f / steppedFps;
            timeTicks = (float) (Math.floor(timeTicks / stepTicks) * stepTicks);
            if (instance.getMaxAge() > 0) {
                float effectiveTicks = instance.isLooping() ? (timeTicks % (float) instance.getMaxAge()) : timeTicks;
                renderProgress = Math.max(0.0f, Math.min(1.0f, effectiveTicks / (float) instance.getMaxAge()));
            }
        }

        float currentAlpha = alphaEasing.interpolate(startAlpha, endAlpha, renderProgress);

        float r = colorEasing.interpolate(startColor[0], endColor[0], renderProgress);
        float g = colorEasing.interpolate(startColor[1], endColor[1], renderProgress);
        float b = colorEasing.interpolate(startColor[2], endColor[2], renderProgress);

        // Se c'e un override di colore sull'istanza
        int override = instance.getColorOverride();
        if (override != -1) {
            r = ((override >> 16) & 0xFF) / 255.0f;
            g = ((override >> 8) & 0xFF) / 255.0f;
            b = (override & 0xFF) / 255.0f;
        }

        RenderState.begin(blendMode, depthMask, cullFace);

        if (texture != null) {
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        }

        ShaderProgram shader = null;
        if (shaderName != null) {
            String effectiveShader = shaderName;
            if ("default".equalsIgnoreCase(effectiveShader) || "none".equalsIgnoreCase(effectiveShader)) {
                if (hotCoreColor != null || rimColor != null) {
                    effectiveShader = "celestial_void";
                } else {
                    effectiveShader = null;
                }
            }
            if (effectiveShader != null) {
                shader = VFXManager.getInstance().getShader(effectiveShader);
            }
            if (shader != null) {
                shader.use();
                shader.setFloat("u_time", timeTicks * 0.05f);
                shader.setFloat("u_progress", renderProgress);
                shader.setVec4("u_color", r, g, b, currentAlpha);
                shader.setInt("u_texture", 0);

                if (hotCoreColor != null) {
                    shader.setVec3("u_hotCoreColor", hotCoreColor[0], hotCoreColor[1], hotCoreColor[2]);
                }
                if (rimColor != null) {
                    shader.setVec3("u_rimColor", rimColor[0], rimColor[1], rimColor[2]);
                }
                shader.setFloat("u_rimPower", rimPower);
                shader.setFloat("u_noiseSpeed", noiseSpeed);
                shader.setFloat("u_noiseScale", noiseScale);
                shader.setFloat("u_bloomMultiplier", bloomMultiplier);
                shader.setFloat("u_invertedCore", invertedCore ? 1.0f : 0.0f);
                shader.setFloat("u_filamentStrength", filamentStrength);
                shader.setFloat("u_distortionStrength", distortionStrength);
                shader.setFloat("u_celBands", celBands);
                shader.setFloat("u_steppedFps", steppedFps);

                DepthCaptureHelper.setupShaderDepthUniforms(shader, 0.8f);
                DepthCaptureHelper.bindToUnit(1);

                Entity viewEntity = Minecraft.getMinecraft().renderViewEntity;
                if (viewEntity != null) {
                    double camX = viewEntity.lastTickPosX + (viewEntity.posX - viewEntity.lastTickPosX) * partialTicks;
                    double camY = viewEntity.lastTickPosY + (viewEntity.posY - viewEntity.lastTickPosY) * partialTicks + viewEntity.getEyeHeight();
                    double camZ = viewEntity.lastTickPosZ + (viewEntity.posZ - viewEntity.lastTickPosZ) * partialTicks;

                    double objX = instance.getCurrentX() + offsetX;
                    double objY = instance.getCurrentY() + offsetY;
                    double objZ = instance.getCurrentZ() + offsetZ;

                    double dx = camX - objX;
                    double dy = camY - objY;
                    double dz = camZ - objZ;
                    double distXZ = Math.sqrt(dx * dx + dz * dz);
                    double dist3D = Math.sqrt(distXZ * distXZ + dy * dy);

                    float theta = (float) Math.atan2(dy, Math.max(0.001, distXZ));
                    float phi = (float) Math.atan2(dz, dx);

                    shader.setFloat("u_viewElevation", theta);
                    shader.setFloat("u_viewAzimuth", phi);
                    shader.setFloat("u_camDistance", (float) dist3D);
                    shader.setFloat("u_meshScale", Math.max(0.001f, startScaleZ * instance.getScaleModifier()));
                }
            }
        }

        // Iterazione repliche Array / Cloner Modifier
        int count = Math.max(1, arrayCount);
        for (int i = 0; i < count; i++) {
            float instProgress = renderProgress;
            if (arrayPhaseDelay > 0.0f && instance.getMaxAge() > 0) {
                float delayFraction = (i * arrayPhaseDelay) / (float) instance.getMaxAge();
                instProgress = renderProgress - delayFraction;
                if (instance.isLooping()) {
                    while (instProgress < 0.0f) instProgress += 1.0f;
                } else {
                    instProgress = Math.max(0.0f, Math.min(1.0f, instProgress));
                }
            }

            float scX = scaleEasing.interpolate(startScaleX, endScaleX, instProgress) * instance.getScaleModifier() + (i * arrayStepScaleX);
            float scY = scaleEasing.interpolate(startScaleY, endScaleY, instProgress) * instance.getScaleModifier() + (i * arrayStepScaleY);
            float scZ = scaleEasing.interpolate(startScaleZ, endScaleZ, instProgress) * instance.getScaleModifier() + (i * arrayStepScaleZ);
            float instAlpha = alphaEasing.interpolate(startAlpha, endAlpha, instProgress);

            GL11.glPushMatrix();
            GL11.glTranslated(offsetX + (i * arrayStepPosX), offsetY + (i * arrayStepPosY), offsetZ + (i * arrayStepPosZ));

            if (billboard) {
                Entity viewEntity = Minecraft.getMinecraft().renderViewEntity;
                if (viewEntity != null) {
                    GL11.glRotatef(-viewEntity.rotationYaw, 0.0f, 1.0f, 0.0f);
                    GL11.glRotatef(viewEntity.rotationPitch, 1.0f, 0.0f, 0.0f);
                }
                GL11.glRotatef(initialRoll + rotationSpeedRoll * timeTicks + (i * arrayStepRotRoll), 0.0f, 0.0f, 1.0f);
            } else {
                GL11.glRotatef(initialYaw + rotationSpeedYaw * timeTicks + (i * arrayStepRotYaw), 0.0f, 1.0f, 0.0f);
                GL11.glRotatef(initialPitch + rotationSpeedPitch * timeTicks + (i * arrayStepRotPitch), 1.0f, 0.0f, 0.0f);
                GL11.glRotatef(initialRoll + rotationSpeedRoll * timeTicks + (i * arrayStepRotRoll), 0.0f, 0.0f, 1.0f);
            }

            if (pivotX != 0.0f || pivotY != 0.0f || pivotZ != 0.0f) {
                GL11.glTranslatef(pivotX, pivotY, pivotZ);
            }

            GL11.glScalef(Math.max(0.0001f, scX), Math.max(0.0001f, scY), Math.max(0.0001f, scZ));

            if (pivotX != 0.0f || pivotY != 0.0f || pivotZ != 0.0f) {
                GL11.glTranslatef(-pivotX, -pivotY, -pivotZ);
            }

            GL11.glColor4f(r, g, b, instAlpha);

            switch (meshType) {
                case CUBE:
                case BOX:
                    MeshCache.renderCube();
                    break;
                case CYLINDER:
                    MeshCache.renderCylinder();
                    break;
                case CONE:
                    MeshCache.renderCone();
                    break;
                case PLANE:
                    MeshCache.renderPlane();
                    break;
                case SPHERE:
                    MeshCache.renderSphere();
                    break;
                case AURA_CYLINDER:
                    MeshCache.renderAuraCylinder();
                    break;
                case AURA_CAPSULE:
                    MeshCache.renderAuraCapsule();
                    break;
                case STAR_FLAME:
                    MeshCache.renderStarFlame();
                    break;
                case LOW_POLY_ROCK:
                    MeshCache.renderLowPolyRock();
                    break;
                case RING:
                    MeshCache.renderRing();
                    break;
                case THIN_RING:
                    MeshCache.renderThinRing();
                    break;
                case DOME:
                    MeshCache.renderDome();
                    break;
                case DISC:
                    MeshCache.renderDisc();
                    break;
                case BEAM_TUBE:
                    MeshCache.renderBeamTube();
                    break;
                case QUAD:
                    MeshCache.renderQuad();
                    break;
                case BILLBOARD_QUAD:
                    MeshCache.renderBillboardQuad();
                    break;
                case ACCRETION_DISK:
                    MeshCache.renderAccretionDisk();
                    break;
            }

            GL11.glPopMatrix();
        }

        if (shader != null) {
            DepthCaptureHelper.unbind(1);
            ShaderProgram.release();
        }

        RenderState.end();
    }

    public MeshComponent setBillboard(boolean billboard) {
        this.billboard = billboard;
        return this;
    }

    public MeshComponent setShader(String shaderName) {
        this.shaderName = shaderName;
        return this;
    }

    public MeshComponent setTexture(ResourceLocation texture) {
        this.texture = texture;
        return this;
    }

    public MeshComponent setScale(Easing easing, float start, float end) {
        this.scaleEasing = easing;
        this.startScaleX = start; this.startScaleY = start; this.startScaleZ = start;
        this.endScaleX = end; this.endScaleY = end; this.endScaleZ = end;
        return this;
    }

    public MeshComponent setScale3D(Easing easing, float sx1, float sy1, float sz1, float sx2, float sy2, float sz2) {
        this.scaleEasing = easing;
        this.startScaleX = sx1; this.startScaleY = sy1; this.startScaleZ = sz1;
        this.endScaleX = sx2; this.endScaleY = sy2; this.endScaleZ = sz2;
        return this;
    }

    public MeshComponent setPivot(float x, float y, float z) {
        this.pivotX = x;
        this.pivotY = y;
        this.pivotZ = z;
        return this;
    }

    public MeshComponent setArrayModifier(int count, float stepX, float stepY, float stepZ,
                                          float stepPitch, float stepYaw, float stepRoll,
                                          float stepScaleX, float stepScaleY, float stepScaleZ,
                                          float phaseDelay) {
        this.arrayCount = Math.max(1, count);
        this.arrayStepPosX = stepX; this.arrayStepPosY = stepY; this.arrayStepPosZ = stepZ;
        this.arrayStepRotPitch = stepPitch; this.arrayStepRotYaw = stepYaw; this.arrayStepRotRoll = stepRoll;
        this.arrayStepScaleX = stepScaleX; this.arrayStepScaleY = stepScaleY; this.arrayStepScaleZ = stepScaleZ;
        this.arrayPhaseDelay = phaseDelay;
        return this;
    }

    public MeshComponent setUniversalShaderProps(float[] hotCore, float[] rim, float rimPower,
                                                 float noiseSpeed, float noiseScale, float bloomMult) {
        this.hotCoreColor = hotCore;
        this.rimColor = rim;
        this.rimPower = rimPower;
        this.noiseSpeed = noiseSpeed;
        this.noiseScale = noiseScale;
        this.bloomMultiplier = bloomMult;
        return this;
    }

    public MeshComponent setUniversalShaderProps(float[] hotCore, float[] rim, float rimPower,
                                                 float noiseSpeed, float noiseScale, float bloomMult,
                                                 boolean invertedCore, float filamentStrength) {
        this.hotCoreColor = hotCore;
        this.rimColor = rim;
        this.rimPower = rimPower;
        this.noiseSpeed = noiseSpeed;
        this.noiseScale = noiseScale;
        this.bloomMultiplier = bloomMult;
        this.invertedCore = invertedCore;
        this.filamentStrength = filamentStrength;
        return this;
    }

    public MeshComponent setInvertedCore(boolean invertedCore) {
        this.invertedCore = invertedCore;
        return this;
    }

    public boolean isInvertedCore() {
        return invertedCore;
    }

    public MeshComponent setFilamentStrength(float filamentStrength) {
        this.filamentStrength = filamentStrength;
        return this;
    }

    public float getFilamentStrength() {
        return filamentStrength;
    }

    public MeshComponent setDistortionStrength(float distortionStrength) {
        this.distortionStrength = distortionStrength;
        return this;
    }

    public float getDistortionStrength() {
        return distortionStrength;
    }

    public MeshComponent setCelBands(float celBands) {
        this.celBands = celBands;
        return this;
    }

    public float getCelBands() {
        return celBands;
    }

    public MeshComponent setAlpha(Easing easing, float start, float end) {
        this.alphaEasing = easing;
        this.startAlpha = start;
        this.endAlpha = end;
        return this;
    }

    public MeshComponent setColor(float r, float g, float b) {
        return setColor(Easing.LINEAR, r, g, b, r, g, b);
    }

    public MeshComponent setColor(Easing easing, float r1, float g1, float b1, float r2, float g2, float b2) {
        this.colorEasing = easing;
        this.startColor = new float[]{r1, g1, b1};
        this.endColor = new float[]{r2, g2, b2};
        return this;
    }

    public MeshComponent setRotationSpeed(float yaw, float pitch, float roll) {
        this.rotationSpeedYaw = yaw;
        this.rotationSpeedPitch = pitch;
        this.rotationSpeedRoll = roll;
        return this;
    }

    public MeshComponent setInitialRotation(float yaw, float pitch, float roll) {
        this.initialYaw = yaw;
        this.initialPitch = pitch;
        this.initialRoll = roll;
        return this;
    }

    public MeshComponent setOffset(double x, double y, double z) {
        this.offsetX = x;
        this.offsetY = y;
        this.offsetZ = z;
        return this;
    }

    public MeshComponent setBlendMode(RenderState.BlendMode mode) {
        this.blendMode = mode;
        return this;
    }

    public MeshComponent setCullFace(boolean cullFace) {
        this.cullFace = cullFace;
        return this;
    }

    public MeshComponent setDepthMask(boolean depthMask) {
        this.depthMask = depthMask;
        return this;
    }

    public int getArrayCount() { return arrayCount; }
    public float getStartScaleX() { return startScaleX; }
    public float getStartScaleY() { return startScaleY; }
    public float getStartScaleZ() { return startScaleZ; }
    public float getEndScaleX() { return endScaleX; }
    public float getEndScaleY() { return endScaleY; }
    public float getEndScaleZ() { return endScaleZ; }

    public MeshType getMeshType() { return meshType; }
    public String getShader() { return shaderName; }
    public double getOffsetX() { return offsetX; }
    public double getOffsetY() { return offsetY; }
    public double getOffsetZ() { return offsetZ; }

    public float getSteppedFps() { return steppedFps; }
    public MeshComponent setSteppedFps(float steppedFps) {
        this.steppedFps = steppedFps;
        return this;
    }
}
