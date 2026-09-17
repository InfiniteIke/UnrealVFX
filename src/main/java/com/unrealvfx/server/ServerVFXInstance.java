package com.unrealvfx.server;

import com.unrealvfx.network.PacketVFXVirtualEntity;
import com.unrealvfx.network.VFXNetwork;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Istanza logica di un effetto grafico nel mondo gestita esclusivamente sul Server.
 * Funziona come una "Virtual Entity":
 * - Ha coordinate, moto, rotazione, scala e ciclo di vita.
 * - Non ha il carico CPU di collisioni, intelligenza artificiale o sincronizzazioni continue vanilla.
 * - Traccia autonomamente i player nel raggio di visibilità e invia aggiornamenti solo su modifica.
 */
public class ServerVFXInstance {
    private final int virtualId;
    private final String effectId;
    private int dimensionId;

    private double posX, posY, posZ;
    private double prevPosX, prevPosY, prevPosZ;
    private double motionX = 0.0, motionY = 0.0, motionZ = 0.0;

    private float yaw = 0.0f, pitch = 0.0f, roll = 0.0f;
    private float rotYawVel = 0.0f, rotPitchVel = 0.0f, rotRollVel = 0.0f;

    private float scale = 1.0f;
    private int colorOverride = -1;

    private int age = 0;
    private int duration = 60;
    private boolean loop = false;
    private double trackingRadius = 96.0;

    private Entity attachedEntity;
    private int attachedEntityId = -1;
    private double offsetX = 0.0, offsetY = 0.0, offsetZ = 0.0;

    private boolean dead = false;
    private int dirtyMask = 0;

    private final Set<EntityPlayerMP> trackedPlayers = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public ServerVFXInstance(int virtualId, String effectId, int dimensionId, double x, double y, double z) {
        this.virtualId = virtualId;
        this.effectId = effectId != null ? effectId : "";
        this.dimensionId = dimensionId;
        this.posX = x; this.posY = y; this.posZ = z;
        this.prevPosX = x; this.prevPosY = y; this.prevPosZ = z;
    }

    public void tick() {
        if (dead) return;

        if (attachedEntity != null) {
            if (attachedEntity.isDead) {
                dead = true;
                return;
            }
            if (attachedEntity.worldObj != null && attachedEntity.worldObj.provider != null) {
                int entityDim = attachedEntity.worldObj.provider.dimensionId;
                if (entityDim != this.dimensionId) {
                    this.dimensionId = entityDim;
                    PacketVFXVirtualEntity despawnPacket = PacketVFXVirtualEntity.despawn(this.virtualId);
                    for (EntityPlayerMP p : trackedPlayers) {
                        VFXNetwork.CHANNEL.sendTo(despawnPacket, p);
                    }
                    trackedPlayers.clear();
                }
            }
            prevPosX = posX; prevPosY = posY; prevPosZ = posZ;
            posX = attachedEntity.posX + offsetX;
            posY = attachedEntity.posY + offsetY;
            posZ = attachedEntity.posZ + offsetZ;
        } else {
            prevPosX = posX; prevPosY = posY; prevPosZ = posZ;
            posX += motionX;
            posY += motionY;
            posZ += motionZ;

            yaw += rotYawVel;
            pitch += rotPitchVel;
            roll += rotRollVel;
        }

        age++;

        if (!loop && age >= duration) {
            dead = true;
        }
    }

    // =========================================================================
    // API DI MANIPOLAZIONE DINAMICA (Sfruttabile da Script, CustomNPCs e Java)
    // =========================================================================

    public ServerVFXInstance setPosition(double x, double y, double z) {
        this.posX = x;
        this.posY = y;
        this.posZ = z;
        this.dirtyMask |= PacketVFXVirtualEntity.FLAG_POS;
        return this;
    }

    public ServerVFXInstance setMotion(double vx, double vy, double vz) {
        this.motionX = vx;
        this.motionY = vy;
        this.motionZ = vz;
        this.dirtyMask |= PacketVFXVirtualEntity.FLAG_MOTION;
        return this;
    }

    public ServerVFXInstance setVelocity(double vx, double vy, double vz) {
        return setMotion(vx, vy, vz);
    }

    public ServerVFXInstance setRotation(float yaw, float pitch, float roll) {
        this.yaw = yaw;
        this.pitch = pitch;
        this.roll = roll;
        this.dirtyMask |= PacketVFXVirtualEntity.FLAG_ROT;
        return this;
    }

    public ServerVFXInstance setRotationVelocity(float dyaw, float dpitch, float droll) {
        this.rotYawVel = dyaw;
        this.rotPitchVel = dpitch;
        this.rotRollVel = droll;
        this.dirtyMask |= PacketVFXVirtualEntity.FLAG_ROT_VEL;
        return this;
    }

    public ServerVFXInstance setScale(float scale) {
        this.scale = Math.max(0.001f, Math.abs(scale));
        this.dirtyMask |= PacketVFXVirtualEntity.FLAG_SCALE;
        return this;
    }

    public ServerVFXInstance setColor(int rgb) {
        this.colorOverride = rgb;
        this.dirtyMask |= PacketVFXVirtualEntity.FLAG_COLOR;
        return this;
    }

    public ServerVFXInstance setColor(float r, float g, float b) {
        int ir = (int) (Math.max(0f, Math.min(1f, r)) * 255);
        int ig = (int) (Math.max(0f, Math.min(1f, g)) * 255);
        int ib = (int) (Math.max(0f, Math.min(1f, b)) * 255);
        return setColor((ir << 16) | (ig << 8) | ib);
    }

    public ServerVFXInstance attachTo(Entity entity) {
        this.attachedEntity = entity;
        this.attachedEntityId = entity != null ? entity.getEntityId() : -1;
        if (entity != null) {
            this.offsetY = entity.height * 0.55;
        }
        this.dirtyMask |= PacketVFXVirtualEntity.FLAG_ATTACH;
        return this;
    }

    public ServerVFXInstance detach() {
        this.attachedEntity = null;
        this.attachedEntityId = -1;
        this.dirtyMask |= PacketVFXVirtualEntity.FLAG_ATTACH;
        return this;
    }

    public ServerVFXInstance setOffset(double ox, double oy, double oz) {
        this.offsetX = ox;
        this.offsetY = oy;
        this.offsetZ = oz;
        this.dirtyMask |= PacketVFXVirtualEntity.FLAG_ATTACH;
        return this;
    }

    public ServerVFXInstance setDuration(int duration) {
        this.duration = Math.max(1, duration);
        return this;
    }

    public ServerVFXInstance setLoop(boolean loop) {
        this.loop = loop;
        return this;
    }

    public ServerVFXInstance setTrackingRadius(double radius) {
        this.trackingRadius = Math.max(8.0, Math.min(1024.0, radius));
        return this;
    }

    public void kill() {
        this.dead = true;
    }

    public void despawn() {
        kill();
    }

    // =========================================================================
    // GETTER E GESTIONE DELLO STATO INTERNO
    // =========================================================================

    public int getVirtualId() { return virtualId; }
    public String getEffectId() { return effectId; }
    public int getDimensionId() { return dimensionId; }
    public double getPosX() { return posX; }
    public double getPosY() { return posY; }
    public double getPosZ() { return posZ; }
    public double getMotionX() { return motionX; }
    public double getMotionY() { return motionY; }
    public double getMotionZ() { return motionZ; }
    public float getYaw() { return yaw; }
    public float getPitch() { return pitch; }
    public float getRoll() { return roll; }
    public float getRotYawVel() { return rotYawVel; }
    public float getRotPitchVel() { return rotPitchVel; }
    public float getRotRollVel() { return rotRollVel; }
    public float getScale() { return scale; }
    public int getColorOverride() { return colorOverride; }
    public int getAge() { return age; }
    public int getDuration() { return duration; }
    public boolean isLoop() { return loop; }
    public double getTrackingRadius() { return trackingRadius; }
    public Entity getAttachedEntity() { return attachedEntity; }
    public int getAttachedEntityId() { return attachedEntityId; }
    public double getOffsetX() { return offsetX; }
    public double getOffsetY() { return offsetY; }
    public double getOffsetZ() { return offsetZ; }
    public boolean isDead() { return dead; }

    public int getDirtyMask() { return dirtyMask; }
    public void clearDirty() { this.dirtyMask = 0; }

    public Set<EntityPlayerMP> getTrackedPlayers() {
        return trackedPlayers;
    }
}
