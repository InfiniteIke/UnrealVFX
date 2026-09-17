package com.unrealvfx.server;

import com.unrealvfx.network.PacketVFXVirtualEntity;
import com.unrealvfx.network.VFXNetwork;
import com.unrealvfx.schematic.VFXSchematic;
import com.unrealvfx.schematic.VFXSchematicRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Gestore principale (Server-Side) delle Entità Virtuali VFX.
 * Supervisiona il ciclo di vita, il movimento dead-reckoned e il tracking spaziale dei giocatori.
 */
public class ServerVFXManager {
    private static final ServerVFXManager INSTANCE = new ServerVFXManager();

    private final Map<Integer, ServerVFXInstance> instances = new ConcurrentHashMap<>();
    private final AtomicInteger idCounter = new AtomicInteger(100000);

    public static ServerVFXManager getInstance() {
        return INSTANCE;
    }

    private ServerVFXManager() {}

    public ServerVFXInstance spawn(World world, double x, double y, double z, String effectId, float scale) {
        int dimId = (world != null && world.provider != null) ? world.provider.dimensionId : 0;
        return spawn(dimId, x, y, z, effectId, scale);
    }

    public ServerVFXInstance spawn(int dimensionId, double x, double y, double z, String effectId, float scale) {
        int virtualId = idCounter.incrementAndGet();
        ServerVFXInstance inst = new ServerVFXInstance(virtualId, effectId, dimensionId, x, y, z);
        inst.setScale(scale);

        // Se lo schematic è registrato, applica le impostazioni di default dello schematic
        VFXSchematic schematic = VFXSchematicRegistry.getInstance().get(effectId);
        if (schematic != null) {
            inst.setDuration(schematic.getDuration());
            inst.setLoop(schematic.isLoop());
            inst.setTrackingRadius(schematic.getTrackingRadius());
        }

        instances.put(virtualId, inst);
        return inst;
    }

    public ServerVFXInstance attach(Entity entity, String effectId, float scale) {
        if (entity == null || entity.worldObj == null) return null;
        int dimId = (entity.worldObj.provider != null) ? entity.worldObj.provider.dimensionId : 0;
        ServerVFXInstance inst = spawn(dimId, entity.posX, entity.posY, entity.posZ, effectId, scale);
        inst.attachTo(entity);
        return inst;
    }

    public ServerVFXInstance getInstance(int virtualId) {
        return instances.get(virtualId);
    }

    public Collection<ServerVFXInstance> getAllInstances() {
        return Collections.unmodifiableCollection(instances.values());
    }

    public boolean kill(int virtualId) {
        ServerVFXInstance inst = instances.get(virtualId);
        if (inst != null) {
            inst.kill();
            return true;
        }
        return false;
    }

    public void clearAll() {
        for (ServerVFXInstance inst : instances.values()) {
            inst.kill();
        }
    }

    public void onPlayerDisconnect(EntityPlayerMP player) {
        for (ServerVFXInstance inst : instances.values()) {
            inst.getTrackedPlayers().remove(player);
        }
    }

    public void tick() {
        if (instances.isEmpty()) return;

        MinecraftServer server = MinecraftServer.getServer();
        if (server == null) return;

        Iterator<Map.Entry<Integer, ServerVFXInstance>> it = instances.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, ServerVFXInstance> entry = it.next();
            ServerVFXInstance inst = entry.getValue();

            if (inst.isDead()) {
                despawnInstance(inst);
                it.remove();
                continue;
            }

            inst.tick();

            if (inst.isDead()) {
                despawnInstance(inst);
                it.remove();
                continue;
            }

            WorldServer world = server.worldServerForDimension(inst.getDimensionId());
            if (world == null) continue;

            double radius = inst.getTrackingRadius();
            double radiusSq = radius * radius;

            // 1. Aggiorna i player nel raggio di tracking
            Set<EntityPlayerMP> currentTracked = inst.getTrackedPlayers();
            List<?> playersInWorld = world.playerEntities;

            for (Object obj : playersInWorld) {
                if (!(obj instanceof EntityPlayerMP)) continue;
                EntityPlayerMP player = (EntityPlayerMP) obj;

                double dx = player.posX - inst.getPosX();
                double dy = player.posY - inst.getPosY();
                double dz = player.posZ - inst.getPosZ();
                double distSq = dx * dx + dy * dy + dz * dz;

                boolean inRange = distSq <= radiusSq;
                boolean isTracked = currentTracked.contains(player);

                if (inRange && !isTracked) {
                    currentTracked.add(player);
                    sendSpawnPacket(inst, player);
                } else if (!inRange && isTracked) {
                    currentTracked.remove(player);
                    sendDespawnPacket(inst.getVirtualId(), player);
                }
            }

            // Rimuovi player disconnessi o che hanno cambiato mondo
            currentTracked.removeIf(p -> p.isDead || p.worldObj != world || !playersInWorld.contains(p));

            // 2. Se l'effetto è stato modificato (dirty), invia delta update a tutti i player che lo stanno guardando
            int dirtyMask = inst.getDirtyMask();
            if (dirtyMask != 0 && !currentTracked.isEmpty()) {
                PacketVFXVirtualEntity updatePacket = createUpdatePacket(inst, dirtyMask);
                for (EntityPlayerMP player : currentTracked) {
                    VFXNetwork.CHANNEL.sendTo(updatePacket, player);
                }
                inst.clearDirty();
            }
        }
    }

    private void sendSpawnPacket(ServerVFXInstance inst, EntityPlayerMP player) {
        PacketVFXVirtualEntity packet = PacketVFXVirtualEntity.spawn(
            inst.getVirtualId(),
            inst.getEffectId(),
            inst.getPosX(), inst.getPosY(), inst.getPosZ(),
            (float) inst.getMotionX(), (float) inst.getMotionY(), (float) inst.getMotionZ(),
            inst.getYaw(), inst.getPitch(), inst.getRoll(),
            inst.getRotYawVel(), inst.getRotPitchVel(), inst.getRotRollVel(),
            inst.getScale(), inst.getColorOverride(),
            inst.getAge(), inst.getDuration(), inst.isLoop(),
            inst.getAttachedEntityId(),
            inst.getOffsetX(), inst.getOffsetY(), inst.getOffsetZ()
        );
        VFXNetwork.CHANNEL.sendTo(packet, player);
    }

    private void sendDespawnPacket(int virtualId, EntityPlayerMP player) {
        VFXNetwork.CHANNEL.sendTo(PacketVFXVirtualEntity.despawn(virtualId), player);
    }

    private void despawnInstance(ServerVFXInstance inst) {
        PacketVFXVirtualEntity despawnPacket = PacketVFXVirtualEntity.despawn(inst.getVirtualId());
        for (EntityPlayerMP player : inst.getTrackedPlayers()) {
            VFXNetwork.CHANNEL.sendTo(despawnPacket, player);
        }
        inst.getTrackedPlayers().clear();
    }

    private PacketVFXVirtualEntity createUpdatePacket(ServerVFXInstance inst, int mask) {
        PacketVFXVirtualEntity p = PacketVFXVirtualEntity.update(inst.getVirtualId(), mask);
        if ((mask & PacketVFXVirtualEntity.FLAG_POS) != 0) {
            p.setPositionData(inst.getPosX(), inst.getPosY(), inst.getPosZ());
        }
        if ((mask & PacketVFXVirtualEntity.FLAG_MOTION) != 0) {
            p.setMotionData((float) inst.getMotionX(), (float) inst.getMotionY(), (float) inst.getMotionZ());
        }
        if ((mask & PacketVFXVirtualEntity.FLAG_ROT) != 0) {
            p.setRotationData(inst.getYaw(), inst.getPitch(), inst.getRoll());
        }
        if ((mask & PacketVFXVirtualEntity.FLAG_ROT_VEL) != 0) {
            p.setRotationVelData(inst.getRotYawVel(), inst.getRotPitchVel(), inst.getRotRollVel());
        }
        if ((mask & PacketVFXVirtualEntity.FLAG_SCALE) != 0) {
            p.setScaleData(inst.getScale());
        }
        if ((mask & PacketVFXVirtualEntity.FLAG_COLOR) != 0) {
            p.setColorData(inst.getColorOverride());
        }
        if ((mask & PacketVFXVirtualEntity.FLAG_ATTACH) != 0) {
            p.setAttachData(inst.getAttachedEntityId(), inst.getOffsetX(), inst.getOffsetY(), inst.getOffsetZ());
        }
        return p;
    }
}
