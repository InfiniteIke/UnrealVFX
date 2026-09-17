package com.unrealvfx.proxy;

import com.unrealvfx.client.event.RenderEventHandler;
import com.unrealvfx.client.render.MeshCache;
import com.unrealvfx.client.vfx.VFXManager;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.common.MinecraftForge;

public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);

        // Inizializza il gestore VFX e la cache mesh
        VFXManager.getInstance().init();
        MeshCache.init();
        com.unrealvfx.schematic.VFXSchematicRegistry.getInstance().loadAllFromDisk();

        // Registra gli eventi di rendering e tick sul client
        RenderEventHandler renderHandler = new RenderEventHandler();
        MinecraftForge.EVENT_BUS.register(renderHandler);
        FMLCommonHandler.instance().bus().register(renderHandler);
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        super.postInit(event);
    }

    @Override
    public void registerClientSchematic(String id, String json) {
        try {
            com.unrealvfx.client.vfx.VFXBlueprint bp = com.unrealvfx.client.vfx.VFXClientBlueprintCompiler.compile(json);
            com.unrealvfx.client.vfx.VFXRegistry.register(id, bp);
        } catch (Throwable t) {
            com.unrealvfx.UnrealVFX.LOGGER.error("Errore nella compilazione/registrazione del blueprint client per: " + id, t);
        }
    }
}
