package com.unrealvfx.network;

import com.unrealvfx.UnrealVFX;
import com.unrealvfx.schematic.VFXSchematic;
import com.unrealvfx.schematic.VFXSchematicParser;
import com.unrealvfx.schematic.VFXSchematicRegistry;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;

/**
 * Pacchetto di rete per sincronizzare le definizioni schematic VFX dal server ai client.
 * Permette ai giocatori di renderizzare effetti definiti solo sul server senza bisogno
 * di scaricare manualmente alcun file di configurazione.
 */
public class PacketVFXSchematicSync implements IMessage {
    private String schematicId;
    private String jsonSource;

    public PacketVFXSchematicSync() {}

    public PacketVFXSchematicSync(String schematicId, String jsonSource) {
        this.schematicId = schematicId;
        this.jsonSource = jsonSource;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.schematicId = ByteBufUtils.readUTF8String(buf);
        int len = buf.readInt();
        if (len < 0 || len > buf.readableBytes() || len > 10_000_000) {
            this.jsonSource = "{}";
            return;
        }
        byte[] bytes = new byte[len];
        buf.readBytes(bytes);
        this.jsonSource = new String(bytes, StandardCharsets.UTF_8);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, schematicId != null ? schematicId : "");
        byte[] bytes = (jsonSource != null ? jsonSource : "").getBytes(StandardCharsets.UTF_8);
        buf.writeInt(bytes.length);
        buf.writeBytes(bytes);
    }

    public static class Handler implements IMessageHandler<PacketVFXSchematicSync, IMessage> {
        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(PacketVFXSchematicSync message, MessageContext ctx) {
            try {
                VFXSchematic schematic = VFXSchematicParser.parseHeader(message.jsonSource);
                VFXSchematicRegistry.getInstance().register(schematic);

                // Compilazione e registrazione grafica client tramite SidedProxy
                UnrealVFX.proxy.registerClientSchematic(message.schematicId, message.jsonSource);

                UnrealVFX.LOGGER.info("[UnrealVFX] Sincronizzato schematic '" + message.schematicId + "' dal server.");
            } catch (Throwable t) {
                UnrealVFX.LOGGER.error("[UnrealVFX] Errore nella sincronizzazione dello schematic: " + message.schematicId, t);
            }
            return null;
        }
    }
}
