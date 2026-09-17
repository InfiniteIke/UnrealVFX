package com.unrealvfx;

import com.unrealvfx.proxy.CommonProxy;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = UnrealVFX.MODID, name = UnrealVFX.NAME, version = UnrealVFX.VERSION)
public class UnrealVFX {
    public static final String MODID = "unrealvfx";
    public static final String NAME = "UnrealVFX";
    public static final String VERSION = "1.0.0";

    @Mod.Instance(MODID)
    public static UnrealVFX instance;

    @SidedProxy(
        clientSide = "com.unrealvfx.proxy.ClientProxy",
        serverSide = "com.unrealvfx.proxy.CommonProxy"
    )
    public static CommonProxy proxy;

    public static final Logger LOGGER = LogManager.getLogger(MODID);

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        proxy.serverStarting(event);
    }
}
