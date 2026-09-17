package com.unrealvfx.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

public class PacketVFX implements IMessage {
    public static final int ACTION_SPAWN = 0;
    public static final int ACTION_ATTACH = 1;
    public static final int ACTION_DETACH = 2;
    public static final int ACTION_CLEAR = 3;
    public static final int ACTION_SKETCH = 4;

    private int action;
    private String effectId;
    private double x, y, z;
    private float scale;
    private int entityId;

    public PacketVFX() {}

    public PacketVFX(int action, String effectId, double x, double y, double z, float scale, int entityId) {
        this.action = action;
        this.effectId = effectId != null ? effectId : "";
        this.x = x;
        this.y = y;
        this.z = z;
        this.scale = scale;
        this.entityId = entityId;
    }

    public static PacketVFX spawn(String effectId, double x, double y, double z, float scale) {
        return new PacketVFX(ACTION_SPAWN, effectId, x, y, z, scale, -1);
    }

    public static PacketVFX attach(String effectId, int entityId, float scale) {
        return new PacketVFX(ACTION_ATTACH, effectId, 0, 0, 0, scale, entityId);
    }

    public static PacketVFX detach(int entityId) {
        return new PacketVFX(ACTION_DETACH, "", 0, 0, 0, 1.0f, entityId);
    }

    public static PacketVFX clearAll() {
        return new PacketVFX(ACTION_CLEAR, "", 0, 0, 0, 1.0f, -1);
    }

    public static PacketVFX sketch(int modeOrDuration) {
        return new PacketVFX(ACTION_SKETCH, "", 0, 0, 0, (float) modeOrDuration, -1);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.action = buf.readInt();
        this.effectId = ByteBufUtils.readUTF8String(buf);
        this.x = buf.readDouble();
        this.y = buf.readDouble();
        this.z = buf.readDouble();
        this.scale = buf.readFloat();
        this.entityId = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(action);
        ByteBufUtils.writeUTF8String(buf, effectId);
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
        buf.writeFloat(scale);
        buf.writeInt(entityId);
    }

    public int getAction() { return action; }
    public String getEffectId() { return effectId; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public float getScale() { return scale; }
    public int getEntityId() { return entityId; }
}
