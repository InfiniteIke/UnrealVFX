package com.unrealvfx.network;

import com.unrealvfx.client.vfx.VFXBlueprint;
import com.unrealvfx.client.vfx.VFXInstance;
import com.unrealvfx.client.vfx.VFXManager;
import com.unrealvfx.client.vfx.VFXRegistry;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;

/**
 * Pacchetto leggero ad altissima efficienza per la sincronizzazione delle Entità Virtuali VFX.
 * Trasmette solo parametri matematici all'ingresso nel raggio di tracking e aggiornamenti delta.
 * Nessun traffico dati costante durante il movimento (Dead Reckoning lato client).
 */
public class PacketVFXVirtualEntity implements IMessage {
    public static final int ACTION_SPAWN = 0;
    public static final int ACTION_UPDATE = 1;
    public static final int ACTION_DESPAWN = 2;

    public static final int FLAG_POS = 1 << 0;
    public static final int FLAG_MOTION = 1 << 1;
    public static final int FLAG_ROT = 1 << 2;
    public static final int FLAG_ROT_VEL = 1 << 3;
    public static final int FLAG_SCALE = 1 << 4;
    public static final int FLAG_COLOR = 1 << 5;
    public static final int FLAG_ATTACH = 1 << 6;

    private int action;
    private int virtualId;

    // Solo per SPAWN
    private String effectId = "";
    private int age;
    private int duration;
    private boolean loop;

    // Condivisi / Update mascherati
    private int updateMask = 0;
    private double posX, posY, posZ;
    private float motionX, motionY, motionZ;
    private float yaw, pitch, roll;
    private float rotYawVel, rotPitchVel, rotRollVel;
    private float scale = 1.0f;
    private int colorOverride = -1;
    private int attachedEntityId = -1;
    private double offsetX, offsetY, offsetZ;

    public PacketVFXVirtualEntity() {}

    public static PacketVFXVirtualEntity spawn(
        int virtualId, String effectId,
        double x, double y, double z,
        float mx, float my, float mz,
        float yaw, float pitch, float roll,
        float dyaw, float dpitch, float droll,
        float scale, int colorOverride,
        int age, int duration, boolean loop,
        int attachedEntityId, double ox, double oy, double oz
    ) {
        PacketVFXVirtualEntity p = new PacketVFXVirtualEntity();
        p.action = ACTION_SPAWN;
        p.virtualId = virtualId;
        p.effectId = effectId != null ? effectId : "";
        p.posX = x; p.posY = y; p.posZ = z;
        p.motionX = mx; p.motionY = my; p.motionZ = mz;
        p.yaw = yaw; p.pitch = pitch; p.roll = roll;
        p.rotYawVel = dyaw; p.rotPitchVel = dpitch; p.rotRollVel = droll;
        p.scale = scale;
        p.colorOverride = colorOverride;
        p.age = age;
        p.duration = duration;
        p.loop = loop;
        p.attachedEntityId = attachedEntityId;
        p.offsetX = ox; p.offsetY = oy; p.offsetZ = oz;
        return p;
    }

    public static PacketVFXVirtualEntity update(int virtualId, int mask) {
        PacketVFXVirtualEntity p = new PacketVFXVirtualEntity();
        p.action = ACTION_UPDATE;
        p.virtualId = virtualId;
        p.updateMask = mask;
        return p;
    }

    public static PacketVFXVirtualEntity despawn(int virtualId) {
        PacketVFXVirtualEntity p = new PacketVFXVirtualEntity();
        p.action = ACTION_DESPAWN;
        p.virtualId = virtualId;
        return p;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.action = buf.readByte();
        this.virtualId = buf.readInt();

        if (action == ACTION_SPAWN) {
            this.effectId = ByteBufUtils.readUTF8String(buf);
            this.posX = buf.readDouble();
            this.posY = buf.readDouble();
            this.posZ = buf.readDouble();
            this.motionX = buf.readFloat();
            this.motionY = buf.readFloat();
            this.motionZ = buf.readFloat();
            this.yaw = buf.readFloat();
            this.pitch = buf.readFloat();
            this.roll = buf.readFloat();
            this.rotYawVel = buf.readFloat();
            this.rotPitchVel = buf.readFloat();
            this.rotRollVel = buf.readFloat();
            this.scale = buf.readFloat();
            this.colorOverride = buf.readInt();
            this.age = buf.readInt();
            this.duration = buf.readInt();
            this.loop = buf.readBoolean();
            this.attachedEntityId = buf.readInt();
            this.offsetX = buf.readDouble();
            this.offsetY = buf.readDouble();
            this.offsetZ = buf.readDouble();
        } else if (action == ACTION_UPDATE) {
            this.updateMask = buf.readInt();
            if ((updateMask & FLAG_POS) != 0) {
                this.posX = buf.readDouble();
                this.posY = buf.readDouble();
                this.posZ = buf.readDouble();
            }
            if ((updateMask & FLAG_MOTION) != 0) {
                this.motionX = buf.readFloat();
                this.motionY = buf.readFloat();
                this.motionZ = buf.readFloat();
            }
            if ((updateMask & FLAG_ROT) != 0) {
                this.yaw = buf.readFloat();
                this.pitch = buf.readFloat();
                this.roll = buf.readFloat();
            }
            if ((updateMask & FLAG_ROT_VEL) != 0) {
                this.rotYawVel = buf.readFloat();
                this.rotPitchVel = buf.readFloat();
                this.rotRollVel = buf.readFloat();
            }
            if ((updateMask & FLAG_SCALE) != 0) {
                this.scale = buf.readFloat();
            }
            if ((updateMask & FLAG_COLOR) != 0) {
                this.colorOverride = buf.readInt();
            }
            if ((updateMask & FLAG_ATTACH) != 0) {
                this.attachedEntityId = buf.readInt();
                this.offsetX = buf.readDouble();
                this.offsetY = buf.readDouble();
                this.offsetZ = buf.readDouble();
            }
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(action);
        buf.writeInt(virtualId);

        if (action == ACTION_SPAWN) {
            ByteBufUtils.writeUTF8String(buf, effectId);
            buf.writeDouble(posX);
            buf.writeDouble(posY);
            buf.writeDouble(posZ);
            buf.writeFloat(motionX);
            buf.writeFloat(motionY);
            buf.writeFloat(motionZ);
            buf.writeFloat(yaw);
            buf.writeFloat(pitch);
            buf.writeFloat(roll);
            buf.writeFloat(rotYawVel);
            buf.writeFloat(rotPitchVel);
            buf.writeFloat(rotRollVel);
            buf.writeFloat(scale);
            buf.writeInt(colorOverride);
            buf.writeInt(age);
            buf.writeInt(duration);
            buf.writeBoolean(loop);
            buf.writeInt(attachedEntityId);
            buf.writeDouble(offsetX);
            buf.writeDouble(offsetY);
            buf.writeDouble(offsetZ);
        } else if (action == ACTION_UPDATE) {
            buf.writeInt(updateMask);
            if ((updateMask & FLAG_POS) != 0) {
                buf.writeDouble(posX);
                buf.writeDouble(posY);
                buf.writeDouble(posZ);
            }
            if ((updateMask & FLAG_MOTION) != 0) {
                buf.writeFloat(motionX);
                buf.writeFloat(motionY);
                buf.writeFloat(motionZ);
            }
            if ((updateMask & FLAG_ROT) != 0) {
                buf.writeFloat(yaw);
                buf.writeFloat(pitch);
                buf.writeFloat(roll);
            }
            if ((updateMask & FLAG_ROT_VEL) != 0) {
                buf.writeFloat(rotYawVel);
                buf.writeFloat(rotPitchVel);
                buf.writeFloat(rotRollVel);
            }
            if ((updateMask & FLAG_SCALE) != 0) {
                buf.writeFloat(scale);
            }
            if ((updateMask & FLAG_COLOR) != 0) {
                buf.writeInt(colorOverride);
            }
            if ((updateMask & FLAG_ATTACH) != 0) {
                buf.writeInt(attachedEntityId);
                buf.writeDouble(offsetX);
                buf.writeDouble(offsetY);
                buf.writeDouble(offsetZ);
            }
        }
    }

    public void setPositionData(double x, double y, double z) {
        this.posX = x; this.posY = y; this.posZ = z;
    }

    public void setMotionData(float mx, float my, float mz) {
        this.motionX = mx; this.motionY = my; this.motionZ = mz;
    }

    public void setRotationData(float y, float p, float r) {
        this.yaw = y; this.pitch = p; this.roll = r;
    }

    public void setRotationVelData(float dy, float dp, float dr) {
        this.rotYawVel = dy; this.rotPitchVel = dp; this.rotRollVel = dr;
    }

    public void setScaleData(float s) {
        this.scale = s;
    }

    public void setColorData(int c) {
        this.colorOverride = c;
    }

    public void setAttachData(int entId, double ox, double oy, double oz) {
        this.attachedEntityId = entId;
        this.offsetX = ox; this.offsetY = oy; this.offsetZ = oz;
    }

    public static class Handler implements IMessageHandler<PacketVFXVirtualEntity, IMessage> {
        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(PacketVFXVirtualEntity message, MessageContext ctx) {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.theWorld == null) return null;

            switch (message.action) {
                case ACTION_SPAWN: {
                    VFXBlueprint bp = VFXRegistry.get(message.effectId);
                    if (bp == null) return null;

                    VFXInstance inst;
                    if (message.attachedEntityId != -1) {
                        Entity target = mc.theWorld.getEntityByID(message.attachedEntityId);
                        if (target != null) {
                            inst = new VFXInstance(bp, target);
                        } else {
                            inst = new VFXInstance(bp, mc.theWorld, message.posX, message.posY, message.posZ);
                        }
                        inst.setAttachedEntityId(message.attachedEntityId);
                        inst.setOffset(message.offsetX, message.offsetY, message.offsetZ);
                    } else {
                        inst = new VFXInstance(bp, mc.theWorld, message.posX, message.posY, message.posZ);
                    }

                    inst.setVirtualId(message.virtualId);
                    inst.setMotion(message.motionX, message.motionY, message.motionZ);
                    inst.initRotation(message.yaw, message.pitch, message.roll);
                    inst.setRotationVelocity(message.rotYawVel, message.rotPitchVel, message.rotRollVel);
                    inst.setScaleModifier(message.scale);
                    inst.setColorOverride(message.colorOverride);
                    inst.setAge(message.age);
                    inst.setDurationOverride(message.duration);
                    inst.setLoopOverride(message.loop);

                    VFXManager.getInstance().registerVirtualInstance(message.virtualId, inst);
                    break;
                }

                case ACTION_UPDATE: {
                    VFXInstance inst = VFXManager.getInstance().getVirtualInstance(message.virtualId);
                    if (inst != null) {
                        if ((message.updateMask & FLAG_POS) != 0) {
                            inst.setPosition(message.posX, message.posY, message.posZ);
                        }
                        if ((message.updateMask & FLAG_MOTION) != 0) {
                            inst.setMotion(message.motionX, message.motionY, message.motionZ);
                        }
                        if ((message.updateMask & FLAG_ROT) != 0) {
                            inst.setRotation(message.yaw, message.pitch, message.roll);
                        }
                        if ((message.updateMask & FLAG_ROT_VEL) != 0) {
                            inst.setRotationVelocity(message.rotYawVel, message.rotPitchVel, message.rotRollVel);
                        }
                        if ((message.updateMask & FLAG_SCALE) != 0) {
                            inst.setScaleModifier(message.scale);
                        }
                        if ((message.updateMask & FLAG_COLOR) != 0) {
                            inst.setColorOverride(message.colorOverride);
                        }
                        if ((message.updateMask & FLAG_ATTACH) != 0) {
                            inst.setAttachedEntityId(message.attachedEntityId);
                            inst.setOffset(message.offsetX, message.offsetY, message.offsetZ);
                            if (message.attachedEntityId != -1) {
                                Entity target = mc.theWorld.getEntityByID(message.attachedEntityId);
                                if (target != null) {
                                    inst.attachTo(target);
                                    inst.setOffset(message.offsetX, message.offsetY, message.offsetZ);
                                }
                            } else {
                                inst.detach();
                            }
                        }
                    }
                    break;
                }

                case ACTION_DESPAWN: {
                    VFXManager.getInstance().removeVirtualInstance(message.virtualId);
                    break;
                }
            }
            return null;
        }
    }
}
