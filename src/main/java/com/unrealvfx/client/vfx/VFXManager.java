package com.unrealvfx.client.vfx;

import com.unrealvfx.client.render.BloomRenderer;
import com.unrealvfx.client.render.DepthCaptureHelper;
import com.unrealvfx.client.render.ShaderProgram;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

import java.util.*;

/**
 * Gestore principale (Client-Side) del ciclo di vita e rendering di tutti i VFX attivi.
 */
public class VFXManager {
    private static final VFXManager INSTANCE = new VFXManager();

    private final List<VFXInstance> activeInstances = new ArrayList<>();
    private final List<VFXInstance> toAdd = new ArrayList<>();
    private final Map<Integer, VFXInstance> virtualInstances = new HashMap<>();
    private final Map<String, ShaderProgram> shaderCache = new HashMap<>();

    public static VFXManager getInstance() {
        return INSTANCE;
    }

    private VFXManager() {}

    public void init() {
        VFXRegistry.init();
    }

    public synchronized void addInstance(VFXInstance instance) {
        toAdd.add(instance);
    }

    public synchronized void registerVirtualInstance(int virtualId, VFXInstance instance) {
        instance.setVirtualId(virtualId);
        VFXInstance old = virtualInstances.put(virtualId, instance);
        if (old != null && old != instance) {
            old.setDead(true);
        }
        toAdd.add(instance);
    }

    public synchronized VFXInstance getVirtualInstance(int virtualId) {
        return virtualInstances.get(virtualId);
    }

    public synchronized void removeVirtualInstance(int virtualId) {
        VFXInstance inst = virtualInstances.remove(virtualId);
        if (inst != null) {
            inst.setDead(true);
        }
    }

    public synchronized void tick() {
        if (!toAdd.isEmpty()) {
            activeInstances.addAll(toAdd);
            toAdd.clear();
        }

        Iterator<VFXInstance> it = activeInstances.iterator();
        while (it.hasNext()) {
            VFXInstance instance = it.next();
            instance.tick();
            if (instance.isDead()) {
                if (instance.getVirtualId() != -1) {
                    virtualInstances.remove(instance.getVirtualId());
                }
                it.remove();
            }
        }
    }

    public synchronized void render(float partialTicks) {
        if (activeInstances.isEmpty()) return;

        // Cattura il Depth Buffer di Minecraft prima del rendering dei VFX traslucidi (Soft Particles)
        DepthCaptureHelper.capture();
        DepthCaptureHelper.bindToUnit(1);

        // 1. Render standard nel mondo di gioco
        for (VFXInstance instance : activeInstances) {
            instance.render(partialTicks);
        }

        // 2. Render pass di Post-Processing Bloom per creare l'alone luminoso accecante (HDR Glow)
        try {
            BloomRenderer.beginCapture();
            if (BloomRenderer.isCapturing()) {
                for (VFXInstance instance : activeInstances) {
                    instance.render(partialTicks);
                }
                BloomRenderer.endAndComposite();
            }
        } catch (Throwable t) {
            BloomRenderer.forceReset();
        }

        DepthCaptureHelper.unbind(1);
    }

    public synchronized void clearAll() {
        activeInstances.clear();
        toAdd.clear();
        virtualInstances.clear();
    }

    public synchronized void removeAttachedTo(Entity entity) {
        for (VFXInstance inst : activeInstances) {
            if (inst.getAttachedEntity() == entity) {
                inst.setDead(true);
            }
        }
    }

    public synchronized boolean hasEffectAttached(Entity entity, String blueprintId) {
        for (VFXInstance inst : activeInstances) {
            if (inst.getAttachedEntity() == entity && inst.getBlueprint().getId().equals(blueprintId) && !inst.isDead()) {
                return true;
            }
        }
        return false;
    }

    public synchronized VFXInstance getAttachedInstance(Entity entity) {
        for (VFXInstance inst : activeInstances) {
            if (inst.getAttachedEntity() == entity && !inst.isDead()) {
                return inst;
            }
        }
        return null;
    }

    public ShaderProgram getShader(String name) {
        return getShader(name, name);
    }

    public ShaderProgram getShader(String vertName, String fragName) {
        String key = vertName + ":" + fragName;
        if (shaderCache.containsKey(key)) {
            return shaderCache.get(key);
        }

        try {
            ResourceLocation vert = new ResourceLocation("unrealvfx", "shaders/" + vertName + ".vert");
            ResourceLocation frag = new ResourceLocation("unrealvfx", "shaders/" + fragName + ".frag");
            ShaderProgram program = new ShaderProgram(vert, frag);
            shaderCache.put(key, program);
            return program;
        } catch (Exception e) {
            return null;
        }
    }
}
