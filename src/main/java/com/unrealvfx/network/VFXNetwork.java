package com.unrealvfx.network;

import com.unrealvfx.client.vfx.VFXManager;
import com.unrealvfx.client.vfx.VFXRegistry;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;

public class VFXNetwork {
    public static final SimpleNetworkWrapper CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel("unrealvfx");

    public static void init() {
        CHANNEL.registerMessage(Handler.class, PacketVFX.class, 0, Side.CLIENT);
        CHANNEL.registerMessage(PacketVFXVirtualEntity.Handler.class, PacketVFXVirtualEntity.class, 1, Side.CLIENT);
        CHANNEL.registerMessage(PacketVFXSchematicSync.Handler.class, PacketVFXSchematicSync.class, 2, Side.CLIENT);
    }

    public static class Handler implements IMessageHandler<PacketVFX, IMessage> {
        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(PacketVFX message, MessageContext ctx) {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.theWorld == null) return null;

            switch (message.getAction()) {
                case PacketVFX.ACTION_SPAWN:
                    VFXRegistry.spawn(
                        message.getEffectId(),
                        mc.theWorld,
                        message.getX(),
                        message.getY(),
                        message.getZ(),
                        message.getScale()
                    );
                    break;

                case PacketVFX.ACTION_ATTACH:
                    Entity target = mc.theWorld.getEntityByID(message.getEntityId());
                    if (target != null) {
                        VFXRegistry.attach(message.getEffectId(), target, message.getScale());
                    }
                    break;

                case PacketVFX.ACTION_DETACH:
                    Entity detachTarget = mc.theWorld.getEntityByID(message.getEntityId());
                    if (detachTarget != null) {
                        VFXManager.getInstance().removeAttachedTo(detachTarget);
                    }
                    break;

                case PacketVFX.ACTION_CLEAR:
                    VFXManager.getInstance().clearAll();
                    break;

                case PacketVFX.ACTION_SKETCH:
                    int mode = (int) message.getScale();
                    if (mode == -1) {
                        com.unrealvfx.client.render.AnimeSketchManager.getInstance().toggle();
                    } else if (mode == 0) {
                        com.unrealvfx.client.render.AnimeSketchManager.getInstance().setActive(false);
                    } else if (mode == 1) {
                        com.unrealvfx.client.render.AnimeSketchManager.getInstance().setActive(true);
                    } else {
                        com.unrealvfx.client.render.AnimeSketchManager.getInstance().triggerBurst(mode);
                    }
                    break;
            }
            return null;
        }
    }
}
