package com.unrealvfx.server;

import com.unrealvfx.network.PacketVFXSchematicSync;
import com.unrealvfx.network.VFXNetwork;
import com.unrealvfx.schematic.VFXSchematic;
import com.unrealvfx.schematic.VFXSchematicRegistry;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 * Event Handler Forge/FML lato Server per il loop di tick e la gestione degli ingressi/uscite dei giocatori.
 */
public class ServerVFXEventHandler {

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            ServerVFXManager.getInstance().tick();
        }
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) event.player;
            // Sincronizza tutti gli schematic registrati al giocatore appena connesso
            for (VFXSchematic schematic : VFXSchematicRegistry.getInstance().getAll()) {
                VFXNetwork.CHANNEL.sendTo(
                    new PacketVFXSchematicSync(schematic.getId(), schematic.getJsonSource()),
                    player
                );
            }
        }
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            ServerVFXManager.getInstance().onPlayerDisconnect((EntityPlayerMP) event.player);
        }
    }

    @SubscribeEvent
    public void onPlayerDimChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            ServerVFXManager.getInstance().onPlayerDisconnect((EntityPlayerMP) event.player);
        }
    }
}
