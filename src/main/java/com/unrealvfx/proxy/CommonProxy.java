package com.unrealvfx.proxy;

import com.unrealvfx.debug.CommandVFX;
import com.unrealvfx.debug.ItemVFXWand;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.registry.GameRegistry;

public class CommonProxy {
    public static ItemVFXWand vfxWand;

    public void preInit(FMLPreInitializationEvent event) {
        com.unrealvfx.network.VFXNetwork.init();
        com.unrealvfx.schematic.VFXSchematicRegistry.getInstance().init(event.getModConfigurationDirectory());
        cpw.mods.fml.common.FMLCommonHandler.instance().bus().register(new com.unrealvfx.server.ServerVFXEventHandler());
        vfxWand = new ItemVFXWand();
        GameRegistry.registerItem(vfxWand, "vfx_wand");
    }

    public void init(FMLInitializationEvent event) {
    }

    public void postInit(FMLPostInitializationEvent event) {
    }

    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandVFX());
    }

    public void registerClientSchematic(String id, String json) {
        // No-op on dedicated server: client blueprints are only compiled and cached on client side
    }
}
