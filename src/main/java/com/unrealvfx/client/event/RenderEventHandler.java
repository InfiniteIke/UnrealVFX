package com.unrealvfx.client.event;

import com.unrealvfx.client.camera.CameraManager;
import com.unrealvfx.client.render.AnimeSketchManager;
import com.unrealvfx.client.render.DepthCaptureHelper;
import com.unrealvfx.client.render.GhostTrailRenderer;
import com.unrealvfx.client.render.ImpactFrameManager;
import com.unrealvfx.client.render.SpeedLinesRenderer;
import com.unrealvfx.client.vfx.VFXManager;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;

/**
 * Event Handler Forge per l'aggancio del motore VFX, Camera, Impact Frames, Speed Lines e Ghost Trails al loop di Minecraft.
 */
public class RenderEventHandler {

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (Minecraft.getMinecraft().theWorld == null) return;
        DepthCaptureHelper.capture();
        VFXManager.getInstance().render(event.partialTicks);
        GhostTrailRenderer.getInstance().render(event.partialTicks);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            if (Minecraft.getMinecraft().theWorld != null && !Minecraft.getMinecraft().isGamePaused()) {
                VFXManager.getInstance().tick();
                CameraManager.getInstance().tick();
                ImpactFrameManager.getInstance().tick();
                SpeedLinesRenderer.getInstance().tick();
                GhostTrailRenderer.getInstance().tick();
                AnimeSketchManager.getInstance().tick();
            }
        }
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (Minecraft.getMinecraft().theWorld == null) return;

        if (event.phase == TickEvent.Phase.START) {
            CameraManager.getInstance().onRenderTickStart();
        } else if (event.phase == TickEvent.Phase.END) {
            CameraManager.getInstance().onRenderTickEnd();

            // Render post-processing di Speed Lines, Anime Sketch Filter e Impact Frames
            SpeedLinesRenderer.getInstance().render();
            AnimeSketchManager.getInstance().render();
            ImpactFrameManager.getInstance().render();
        }
    }

    @SubscribeEvent
    public void onFOVUpdate(FOVUpdateEvent event) {
        float fovOffset = CameraManager.getInstance().getFovOffset();
        if (fovOffset > 0.01f) {
            event.newfov += fovOffset * 0.015f;
        }
    }

    @SubscribeEvent
    public void onWorldUnload(net.minecraftforge.event.world.WorldEvent.Unload event) {
        if (Minecraft.getMinecraft().theWorld != null && event.world == Minecraft.getMinecraft().theWorld) {
            VFXManager.getInstance().clearAll();
            GhostTrailRenderer.getInstance().clear();
        }
    }
}
