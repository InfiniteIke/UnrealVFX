package com.unrealvfx.client.vfx.component;

import com.unrealvfx.client.vfx.VFXComponent;
import com.unrealvfx.client.vfx.VFXInstance;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

import java.util.List;

/**
 * Componente di fisica gravitazionale reale nel mondo di Minecraft.
 * Esercita una potente attrazione gravitazionale relativistica a spirale su mob,
 * animali, item e proiettili circostanti verso il centro del buco nero.
 * Le entità che raggiungono l'Orizzonte degli Eventi subiscono danno da vuoto cosmico
 * e gli item caduti vengono disintegrati.
 */
public class GravitationalPullComponent implements VFXComponent {

    private float radius = 16.0f;
    private float pullStrength = 0.65f;
    private float swirlStrength = 0.35f;
    private boolean pullItems = true;
    private boolean pullEntities = true;
    private boolean damageAtHorizon = true;

    public GravitationalPullComponent(float radius, float pullStrength) {
        this.radius = radius;
        this.pullStrength = pullStrength;
    }

    @Override
    public void tick(VFXInstance instance) {
        World world = instance.getWorld();
        if (world == null) return;

        double cx = instance.getCurrentX();
        double cy = instance.getCurrentY();
        double cz = instance.getCurrentZ();
        float r = radius * instance.getScaleModifier();

        AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(
            cx - r, cy - r, cz - r,
            cx + r, cy + r, cz + r
        );

        @SuppressWarnings("unchecked")
        List<Entity> list = world.getEntitiesWithinAABB(Entity.class, aabb);
        if (list == null || list.isEmpty()) return;

        Entity caster = instance.getAttachedEntity();

        for (Entity e : list) {
            // Non risucchiare il caster stesso se l'effetto è ancorato al giocatore
            if (e == caster) continue;

            boolean isItem = e instanceof EntityItem;
            if (isItem && !pullItems) continue;
            if (!isItem && !pullEntities) continue;

            double dx = cx - e.posX;
            double dy = cy - e.posY - (e.height * 0.5);
            double dz = cz - e.posZ;
            double distSq = dx * dx + dy * dy + dz * dz;

            if (distSq > r * r || distSq < 0.01) continue;
            double dist = Math.sqrt(distSq);

            // Legge di gravità relativistica ripida: accelerazione ~ 1 / dist^1.35
            double invDist = 1.0 / dist;
            double factor = Math.min(1.4, (pullStrength * 4.5) / Math.pow(Math.max(1.0, dist), 1.35));

            double vx = dx * invDist * factor;
            // Leggera spinta verso l'alto per sollevare mob da terra e risucchiarli a mezz'aria
            double vy = dy * invDist * factor * 0.9 + 0.045;
            double vz = dz * invDist * factor;

            // Vortice orbitale attorno all'asse equatoriale (Keplero / frame dragging)
            double swirlFactor = Math.min(0.7, (swirlStrength * 2.8) / Math.sqrt(Math.max(1.0, dist)));
            vx += -dz * invDist * swirlFactor;
            vz += dx * invDist * swirlFactor;

            e.motionX += vx * 0.4;
            e.motionY += vy * 0.4;
            e.motionZ += vz * 0.4;
            e.velocityChanged = true;

            // Se l'entità entra nell'Orizzonte degli Eventi (distanza < 1.6 blocchi)
            if (dist < 1.6 * instance.getScaleModifier() && damageAtHorizon) {
                if (e instanceof EntityLivingBase) {
                    ((EntityLivingBase) e).attackEntityFrom(DamageSource.outOfWorld, 4.0f);
                } else if (isItem) {
                    // Disintegrazione dell'oggetto caduto nella singolarità
                    if (dist < 1.0 * instance.getScaleModifier()) {
                        e.setDead();
                    }
                }
            }
        }
    }

    @Override
    public void render(VFXInstance instance, float partialTicks, float progress) {
        // Logica puramente fisica nei tick
    }

    public GravitationalPullComponent setSwirlStrength(float swirlStrength) {
        this.swirlStrength = swirlStrength;
        return this;
    }

    public GravitationalPullComponent setPullItems(boolean pullItems) {
        this.pullItems = pullItems;
        return this;
    }

    public GravitationalPullComponent setPullEntities(boolean pullEntities) {
        this.pullEntities = pullEntities;
        return this;
    }

    public GravitationalPullComponent setDamageAtHorizon(boolean damageAtHorizon) {
        this.damageAtHorizon = damageAtHorizon;
        return this;
    }

    public float getRadius() {
        return radius;
    }

    public float getPullStrength() {
        return pullStrength;
    }

    public float getSwirlStrength() {
        return swirlStrength;
    }

    public boolean isPullItems() {
        return pullItems;
    }

    public boolean isPullEntities() {
        return pullEntities;
    }

    public boolean isDamageAtHorizon() {
        return damageAtHorizon;
    }
}
