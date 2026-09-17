package com.unrealvfx.client.vfx;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

/**
 * Istanza viva di un effetto nel mondo di gioco.
 * Gestisce posizione, interpolazione, ancoraggio a entità e ciclo di vita.
 */
public class VFXInstance {
    private final VFXBlueprint blueprint;
    private final World world;

    private double posX;
    private double posY;
    private double posZ;

    private double prevPosX;
    private double prevPosY;
    private double prevPosZ;

    private double motionX = 0.0;
    private double motionY = 0.0;
    private double motionZ = 0.0;

    private float yaw = 0.0f;
    private float pitch = 0.0f;
    private float roll = 0.0f;

    private float prevYaw = 0.0f;
    private float prevPitch = 0.0f;
    private float prevRoll = 0.0f;

    private float rotYawVel = 0.0f;
    private float rotPitchVel = 0.0f;
    private float rotRollVel = 0.0f;

    private Entity attachedEntity;
    private int attachedEntityId = -1;
    private double offsetX;
    private double offsetY;
    private double offsetZ;

    private int age = 0;
    private boolean dead = false;
    private float scaleModifier = 1.0f;
    private int colorOverride = -1; // -1 se si usano i colori definiti nel blueprint
    private int virtualId = -1; // ID virtuale server-side (-1 se locale/non gestito)
    private int durationOverride = -1;
    private Boolean loopOverride = null;

    public VFXInstance(VFXBlueprint blueprint, World world, double x, double y, double z) {
        this.blueprint = blueprint;
        this.world = world;
        this.posX = x;
        this.posY = y;
        this.posZ = z;
        this.prevPosX = x;
        this.prevPosY = y;
        this.prevPosZ = z;
    }

    public VFXInstance(VFXBlueprint blueprint, Entity entity) {
        this(blueprint, entity.worldObj, entity.posX, entity.posY, entity.posZ);
        this.attachedEntity = entity;
        this.attachedEntityId = entity.getEntityId();
        this.offsetY = entity.height * 0.55;
    }

    public void tick() {
        if (dead) return;

        // Se agganciato a un'entità morta o rimossa, termina l'effetto
        if (attachedEntity != null && (attachedEntity.isDead || attachedEntity.worldObj != world)) {
            triggerDeath();
            return;
        }

        // Risoluzione resiliente per entità arrivate in ritardo sulla rete
        if (attachedEntity == null && attachedEntityId != -1 && world != null) {
            Entity target = world.getEntityByID(attachedEntityId);
            if (target != null && !target.isDead) {
                this.attachedEntity = target;
            }
        }

        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;

        prevYaw = yaw;
        prevPitch = pitch;
        prevRoll = roll;

        if (attachedEntity == null) {
            posX += motionX;
            posY += motionY;
            posZ += motionZ;

            yaw += rotYawVel;
            pitch += rotPitchVel;
            roll += rotRollVel;
        }

        age++;

        int maxAge = durationOverride > 0 ? durationOverride : blueprint.getDuration();
        if (!isLooping() && age >= maxAge) {
            triggerDeath();
            return;
        }

        for (VFXComponent comp : blueprint.getComponents()) {
            comp.tick(this);
        }
    }

    public void render(float partialTicks) {
        if (dead) return;

        Entity viewEntity = Minecraft.getMinecraft().renderViewEntity;
        if (viewEntity == null) return;

        // Calcolo posizione della telecamera del giocatore per le coordinate relative nel mondo
        double camX = viewEntity.lastTickPosX + (viewEntity.posX - viewEntity.lastTickPosX) * partialTicks;
        double camY = viewEntity.lastTickPosY + (viewEntity.posY - viewEntity.lastTickPosY) * partialTicks;
        double camZ = viewEntity.lastTickPosZ + (viewEntity.posZ - viewEntity.lastTickPosZ) * partialTicks;

        // Calcolo coordinate dell'effetto (con interpolazione)
        double currentX, currentY, currentZ;
        if (attachedEntity != null) {
            currentX = attachedEntity.lastTickPosX + (attachedEntity.posX - attachedEntity.lastTickPosX) * partialTicks + offsetX;
            currentY = attachedEntity.lastTickPosY + (attachedEntity.posY - attachedEntity.lastTickPosY) * partialTicks + offsetY;
            currentZ = attachedEntity.lastTickPosZ + (attachedEntity.posZ - attachedEntity.lastTickPosZ) * partialTicks + offsetZ;
        } else {
            currentX = prevPosX + (posX - prevPosX) * partialTicks;
            currentY = prevPosY + (posY - prevPosY) * partialTicks;
            currentZ = prevPosZ + (posZ - prevPosZ) * partialTicks;
        }

        float curYaw = prevYaw + (yaw - prevYaw) * partialTicks;
        float curPitch = prevPitch + (pitch - prevPitch) * partialTicks;
        float curRoll = prevRoll + (roll - prevRoll) * partialTicks;

        float progress = getProgress(partialTicks);

        GL11.glPushMatrix();
        GL11.glTranslated(currentX - camX, currentY - camY, currentZ - camZ);

        if (curYaw != 0.0f) GL11.glRotatef(curYaw, 0.0f, 1.0f, 0.0f);
        if (curPitch != 0.0f) GL11.glRotatef(curPitch, 1.0f, 0.0f, 0.0f);
        if (curRoll != 0.0f) GL11.glRotatef(curRoll, 0.0f, 0.0f, 1.0f);

        for (VFXComponent comp : blueprint.getComponents()) {
            comp.render(this, partialTicks, progress);
        }

        GL11.glPopMatrix();
    }

    public float getProgress(float partialTicks) {
        float exactAge = age + partialTicks;
        int dur = durationOverride > 0 ? durationOverride : blueprint.getDuration();
        if (isLooping()) {
            return (exactAge % dur) / (float) dur;
        }
        return Math.min(1.0f, exactAge / (float) dur);
    }

    public boolean isLooping() {
        return loopOverride != null ? loopOverride : blueprint.isLoop();
    }

    public VFXInstance setLoopOverride(boolean loop) {
        this.loopOverride = loop;
        return this;
    }

    public Boolean getLoopOverride() {
        return loopOverride;
    }

    public VFXInstance initRotation(float yaw, float pitch, float roll) {
        this.yaw = this.prevYaw = yaw;
        this.pitch = this.prevPitch = pitch;
        this.roll = this.prevRoll = roll;
        return this;
    }

    public int getAttachedEntityId() {
        return attachedEntityId;
    }

    public VFXInstance setAttachedEntityId(int id) {
        this.attachedEntityId = id;
        return this;
    }

    public VFXBlueprint getBlueprint() {
        return blueprint;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public boolean isDead() {
        return dead;
    }

    public void setDead(boolean dead) {
        if (!this.dead && dead) {
            triggerDeath();
        } else {
            this.dead = dead;
        }
    }

    public void triggerDeath() {
        if (!this.dead) {
            this.dead = true;
            if (blueprint != null && blueprint.getComponents() != null) {
                for (VFXComponent comp : blueprint.getComponents()) {
                    try {
                        comp.onDeath(this);
                    } catch (Throwable ignored) {}
                }
            }
        }
    }

    public void triggerRemove() {
        triggerDeath();
        if (blueprint != null && blueprint.getComponents() != null) {
            for (VFXComponent comp : blueprint.getComponents()) {
                try {
                    comp.onRemove(this);
                } catch (Throwable ignored) {}
            }
        }
    }

    public float getScaleModifier() {
        return scaleModifier;
    }

    public VFXInstance setScaleModifier(float scaleModifier) {
        this.scaleModifier = scaleModifier;
        return this;
    }

    public int getColorOverride() {
        return colorOverride;
    }

    public VFXInstance setColorOverride(int colorOverride) {
        this.colorOverride = colorOverride;
        return this;
    }

    public int getVirtualId() {
        return virtualId;
    }

    public VFXInstance setVirtualId(int virtualId) {
        this.virtualId = virtualId;
        return this;
    }

    public int getDurationOverride() {
        return durationOverride;
    }

    public VFXInstance setDurationOverride(int duration) {
        this.durationOverride = duration;
        return this;
    }

    public VFXInstance setPosition(double x, double y, double z) {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        this.posX = x;
        this.posY = y;
        this.posZ = z;
        return this;
    }

    public VFXInstance setMotion(double mx, double my, double mz) {
        this.motionX = mx;
        this.motionY = my;
        this.motionZ = mz;
        return this;
    }

    public VFXInstance setRotation(float yaw, float pitch, float roll) {
        this.prevYaw = this.yaw;
        this.prevPitch = this.pitch;
        this.prevRoll = this.roll;
        this.yaw = yaw;
        this.pitch = pitch;
        this.roll = roll;
        return this;
    }

    public VFXInstance setRotationVelocity(float dyaw, float dpitch, float droll) {
        this.rotYawVel = dyaw;
        this.rotPitchVel = dpitch;
        this.rotRollVel = droll;
        return this;
    }

    public VFXInstance attachTo(Entity entity) {
        this.attachedEntity = entity;
        return this;
    }

    public VFXInstance detach() {
        this.attachedEntity = null;
        return this;
    }

    public VFXInstance setOffset(double x, double y, double z) {
        this.offsetX = x;
        this.offsetY = y;
        this.offsetZ = z;
        return this;
    }

    public Entity getAttachedEntity() {
        return attachedEntity;
    }

    public World getWorld() {
        return world;
    }

    public double getCurrentX() {
        if (attachedEntity != null) {
            return attachedEntity.posX + offsetX;
        }
        return posX;
    }

    public double getCurrentY() {
        if (attachedEntity != null) {
            return attachedEntity.posY + offsetY;
        }
        return posY;
    }

    public double getCurrentZ() {
        if (attachedEntity != null) {
            return attachedEntity.posZ + offsetZ;
        }
        return posZ;
    }

    public double getMotionX() { return motionX; }
    public double getMotionY() { return motionY; }
    public double getMotionZ() { return motionZ; }

    public float getYaw() { return yaw; }
    public float getPitch() { return pitch; }
    public float getRoll() { return roll; }

    public float getPrevYaw() { return prevYaw; }
    public float getPrevPitch() { return prevPitch; }
    public float getPrevRoll() { return prevRoll; }

    public float getInterpolatedYaw(float partialTicks) {
        return prevYaw + (yaw - prevYaw) * partialTicks;
    }

    public float getInterpolatedPitch(float partialTicks) {
        return prevPitch + (pitch - prevPitch) * partialTicks;
    }

    public float getInterpolatedRoll(float partialTicks) {
        return prevRoll + (roll - prevRoll) * partialTicks;
    }

    public double getOffsetX() { return offsetX; }
    public double getOffsetY() { return offsetY; }
    public double getOffsetZ() { return offsetZ; }

    public int getMaxAge() {
        return durationOverride > 0 ? durationOverride : blueprint.getDuration();
    }
}
