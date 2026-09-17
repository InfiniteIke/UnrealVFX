package com.unrealvfx.client.vfx.component;

import com.unrealvfx.client.render.ImpactFrameManager;
import com.unrealvfx.client.vfx.VFXComponent;
import com.unrealvfx.client.vfx.VFXInstance;

/**
 * Componente per innescare un Impact Frame durante la detonazione di un attacco
 * o al rilascio di fasci di energia (flash binario B&W / Invertito / Hakai).
 */
public class ImpactFrameTriggerComponent implements VFXComponent {
    private ImpactFrameManager.Type type = ImpactFrameManager.Type.MANGA_BW;
    private int durationTicks = 2;
    private int triggerTick = 1;

    public ImpactFrameTriggerComponent(ImpactFrameManager.Type type, int durationTicks) {
        this(type, durationTicks, 1);
    }

    public ImpactFrameTriggerComponent(ImpactFrameManager.Type type, int durationTicks, int triggerTick) {
        this.type = (type != null) ? type : ImpactFrameManager.Type.MANGA_BW;
        this.durationTicks = Math.max(1, durationTicks);
        this.triggerTick = Math.max(0, triggerTick);
    }

    public ImpactFrameManager.Type getType() {
        return type;
    }

    public ImpactFrameTriggerComponent setType(ImpactFrameManager.Type type) {
        this.type = (type != null) ? type : ImpactFrameManager.Type.MANGA_BW;
        return this;
    }

    public int getDurationTicks() {
        return durationTicks;
    }

    public ImpactFrameTriggerComponent setDurationTicks(int durationTicks) {
        this.durationTicks = Math.max(1, durationTicks);
        return this;
    }

    public int getTriggerTick() {
        return triggerTick;
    }

    public ImpactFrameTriggerComponent setTriggerTick(int triggerTick) {
        this.triggerTick = Math.max(0, triggerTick);
        return this;
    }

    @Override
    public void tick(VFXInstance instance) {
        // Scatta esattamente al trigger tick configurato (gestendo anche i cicli di loop)
        int age = instance.getAge();
        int dur = instance.getMaxAge();
        int effectiveAge = (instance.isLooping() && dur > 0) ? ((age - 1) % dur) + 1 : age;
        int effectiveTrigger = Math.max(1, triggerTick);
        if (effectiveAge == effectiveTrigger) {
            ImpactFrameManager.getInstance().trigger(type, durationTicks);
        }
    }

    @Override
    public void render(VFXInstance instance, float partialTicks, float progress) {
    }
}
