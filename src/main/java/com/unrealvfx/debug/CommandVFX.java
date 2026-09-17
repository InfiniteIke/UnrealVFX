package com.unrealvfx.debug;

import com.unrealvfx.api.UnrealVFXAPI;
import com.unrealvfx.network.PacketVFX;
import com.unrealvfx.network.VFXNetwork;
import com.unrealvfx.schematic.VFXSchematic;
import com.unrealvfx.schematic.VFXSchematicRegistry;
import com.unrealvfx.server.ServerVFXInstance;
import com.unrealvfx.server.ServerVFXManager;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Comando unificato per gestione in-game di schematic VFX, entità virtuali e filtri post-processing.
 *
 * Esempi:
 * /vfx spawn solar_flare 1.5
 * /vfx spawn cosmic_vortex 2.0 0.1 0.0 0.0
 * /vfx reload
 * /vfx instances
 * /vfx move <id> <x> <y> <z>
 * /vfx velocity <id> <vx> <vy> <vz>
 * /vfx rot <id> <yaw> <pitch> [roll]
 * /vfx scale <id> <scale>
 * /vfx kill <id>
 * /vfx clear
 */
public class CommandVFX extends CommandBase {

    @Override
    public String getCommandName() {
        return "vfx";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/vfx <spawn|attach|detach|clear|reload|instances|move|velocity|rot|rotvel|scale|kill|sketch|list>";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0; // Accessibile a tutti per test e scripting in dev
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length == 0) {
            throw new WrongUsageException(getCommandUsage(sender));
        }

        String sub = args[0].toLowerCase();

        // =========================================================================
        // 1. LIST
        // =========================================================================
        if (sub.equals("list")) {
            Collection<VFXSchematic> schematics = VFXSchematicRegistry.getInstance().getAll();
            StringBuilder sbSchem = new StringBuilder();
            for (VFXSchematic s : schematics) {
                if (sbSchem.length() > 0) sbSchem.append(", ");
                sbSchem.append(s.getId());
            }

            sender.addChatMessage(new ChatComponentText(
                EnumChatFormatting.GOLD + "[UnrealVFX] Schematic JSON caricati: " +
                EnumChatFormatting.GREEN + (sbSchem.length() > 0 ? sbSchem.toString() : "(nessuno)")
            ));

            sender.addChatMessage(new ChatComponentText(
                EnumChatFormatting.GOLD + "[UnrealVFX] Preset Coded disponibili: " +
                EnumChatFormatting.LIGHT_PURPLE + "cosmic:gargantua, cosmic:black_hole, " +
                EnumChatFormatting.YELLOW + "dbz:hakai_ball, dbz:hakai_explosion, dbz:sparking_aura_ssj, " +
                EnumChatFormatting.AQUA + "dbz:zanzoken_dash, dbz:sparking_explosion, dbz:energy_beam, dbz:energy_disc, dbz:energy_dome"
            ));
            return;
        }

        // =========================================================================
        // 2. RELOAD (Ricarica gli schematic da disco e li trasmette a tutti i player)
        // =========================================================================
        if (sub.equals("reload")) {
            int loaded = UnrealVFXAPI.reloadSchematics();
            sender.addChatMessage(new ChatComponentText(
                EnumChatFormatting.GREEN + "[UnrealVFX] Ricaricati con successo " +
                EnumChatFormatting.YELLOW + loaded +
                EnumChatFormatting.GREEN + " schematics VFX da config/unrealvfx/effects/ e sincronizzati ai client!"
            ));
            return;
        }

        // =========================================================================
        // 3. INSTANCES (Lista tutte le Virtual Entities VFX attive nel server)
        // =========================================================================
        if (sub.equals("instances")) {
            Collection<ServerVFXInstance> list = UnrealVFXAPI.getAllInstances();
            sender.addChatMessage(new ChatComponentText(
                EnumChatFormatting.GOLD + "[UnrealVFX] Istanze Virtuali attive nel server (" + list.size() + "):"
            ));
            for (ServerVFXInstance inst : list) {
                sender.addChatMessage(new ChatComponentText(
                    EnumChatFormatting.AQUA + " #" + inst.getVirtualId() + " " +
                    EnumChatFormatting.WHITE + "['" + inst.getEffectId() + "'] " +
                    EnumChatFormatting.GRAY + "pos: (" + String.format("%.1f", inst.getPosX()) + ", " + String.format("%.1f", inst.getPosY()) + ", " + String.format("%.1f", inst.getPosZ()) + ") " +
                    EnumChatFormatting.YELLOW + "scala: " + inst.getScale() + " " +
                    EnumChatFormatting.LIGHT_PURPLE + "player tracciati: " + inst.getTrackedPlayers().size()
                ));
            }
            return;
        }

        // =========================================================================
        // 4. SPAWN (Spawna una Virtual Entity con tracking spaziale autonomo)
        // =========================================================================
        if (sub.equals("spawn")) {
            if (args.length < 2) {
                throw new WrongUsageException("/vfx spawn <effect_id> [scale] [vx vy vz] OR /vfx spawn <effect_id> <x> <y> <z> [scale] [vx vy vz]");
            }
            String effectId = args[1];
            double x, y, z;
            float scale = 1.0f;
            double vx = 0.0, vy = 0.0, vz = 0.0;

            boolean hasExplicitCoords = args.length >= 5 && isCoordinate(args[2]) && isCoordinate(args[3]) && isCoordinate(args[4]);

            if (hasExplicitCoords) {
                double baseX = (sender instanceof EntityPlayerMP) ? ((EntityPlayerMP) sender).posX : sender.getPlayerCoordinates().posX;
                double baseY = (sender instanceof EntityPlayerMP) ? ((EntityPlayerMP) sender).posY : sender.getPlayerCoordinates().posY;
                double baseZ = (sender instanceof EntityPlayerMP) ? ((EntityPlayerMP) sender).posZ : sender.getPlayerCoordinates().posZ;

                try {
                    x = parseCoord(baseX, args[2]);
                    y = parseCoord(baseY, args[3]);
                    z = parseCoord(baseZ, args[4]);
                } catch (NumberFormatException e) {
                    throw new WrongUsageException("Coordinate non valide: " + args[2] + " " + args[3] + " " + args[4]);
                }

                if (args.length >= 6) {
                    try { scale = Float.parseFloat(args[5]); } catch (NumberFormatException ignored) {}
                }
                if (args.length >= 9) {
                    try {
                        vx = Double.parseDouble(args[6]);
                        vy = Double.parseDouble(args[7]);
                        vz = Double.parseDouble(args[8]);
                    } catch (NumberFormatException ignored) {}
                }
            } else {
                if (args.length >= 3) {
                    try { scale = Float.parseFloat(args[2]); } catch (NumberFormatException ignored) {}
                }
                if (args.length >= 6) {
                    try {
                        vx = Double.parseDouble(args[3]);
                        vy = Double.parseDouble(args[4]);
                        vz = Double.parseDouble(args[5]);
                    } catch (NumberFormatException ignored) {}
                }

                if (sender instanceof EntityPlayerMP) {
                    EntityPlayerMP player = (EntityPlayerMP) sender;
                    net.minecraft.util.Vec3 look = player.getLookVec();
                    double spawnDist = Math.max(8.0, 3.5 * scale);
                    x = player.posX + look.xCoord * spawnDist;
                    y = player.posY + player.getEyeHeight() + look.yCoord * spawnDist;
                    z = player.posZ + look.zCoord * spawnDist;
                } else {
                    x = sender.getPlayerCoordinates().posX;
                    y = sender.getPlayerCoordinates().posY;
                    z = sender.getPlayerCoordinates().posZ;
                }
            }

            if (!VFXSchematicRegistry.getInstance().has(effectId)) {
                sender.addChatMessage(new ChatComponentText(
                    EnumChatFormatting.RED + "[UnrealVFX] Errore: Schematic VFX '" + effectId + "' non trovato! Esegui '/vfx reload' o verifica che il file .json sia in 'config/unrealvfx/effects/'."
                ));
                return;
            }

            net.minecraft.world.World world = (sender instanceof EntityPlayerMP) ? ((EntityPlayerMP) sender).worldObj : sender.getEntityWorld();
            ServerVFXInstance inst = UnrealVFXAPI.spawn(world, x, y, z, effectId, scale);
            if (vx != 0.0 || vy != 0.0 || vz != 0.0) {
                inst.setMotion(vx, vy, vz);
            }

            sender.addChatMessage(new ChatComponentText(
                EnumChatFormatting.AQUA + "[UnrealVFX] Spawnata Virtual Entity " +
                EnumChatFormatting.YELLOW + "#" + inst.getVirtualId() +
                EnumChatFormatting.AQUA + " ('" + effectId + "', pos: " + String.format("%.1f", x) + ", " + String.format("%.1f", y) + ", " + String.format("%.1f", z) + ", scala: " + scale + ")"
            ));
            return;
        }

        // =========================================================================
        // 5. ATTACH
        // =========================================================================
        if (sub.equals("attach")) {
            if (args.length < 2) {
                throw new WrongUsageException("/vfx attach <effect_id> [scale]");
            }
            if (!(sender instanceof EntityPlayerMP)) {
                sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Questo comando puo' essere eseguito solo da un giocatore."));
                return;
            }

            EntityPlayerMP player = (EntityPlayerMP) sender;
            String effectId = args[1];
            float scale = 1.0f;
            if (args.length >= 3) {
                try {
                    scale = Float.parseFloat(args[2]);
                } catch (NumberFormatException ignored) {}
            }

            if (!VFXSchematicRegistry.getInstance().has(effectId)) {
                sender.addChatMessage(new ChatComponentText(
                    EnumChatFormatting.RED + "[UnrealVFX] Errore: Schematic VFX '" + effectId + "' non trovato! Esegui '/vfx reload' o verifica che il file .json sia in 'config/unrealvfx/effects/'."
                ));
                return;
            }

            ServerVFXInstance inst = UnrealVFXAPI.attach(player, effectId, scale);
            sender.addChatMessage(new ChatComponentText(
                EnumChatFormatting.GOLD + "[UnrealVFX] Effetto '" + effectId + "' agganciato (Virtual ID: #" + inst.getVirtualId() + ")"
            ));
            return;
        }

        // =========================================================================
        // 6. DETACH
        // =========================================================================
        if (sub.equals("detach")) {
            if (sender instanceof EntityPlayerMP) {
                EntityPlayerMP player = (EntityPlayerMP) sender;
                for (ServerVFXInstance inst : UnrealVFXAPI.getAllInstances()) {
                    if (inst.getAttachedEntity() == player) {
                        inst.kill();
                    }
                }
                VFXNetwork.CHANNEL.sendToAll(PacketVFX.detach(player.getEntityId()));
                sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "[UnrealVFX] Effetti rimossi dal giocatore."));
            }
            return;
        }

        // =========================================================================
        // 7. KILL
        // =========================================================================
        if (sub.equals("kill")) {
            if (args.length < 2) {
                throw new WrongUsageException("/vfx kill <virtual_id>");
            }
            try {
                int id = Integer.parseInt(args[1]);
                boolean ok = UnrealVFXAPI.kill(id);
                if (ok) {
                    sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "[UnrealVFX] Istanza #" + id + " terminata."));
                } else {
                    sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "[UnrealVFX] Nessuna istanza attiva con ID #" + id));
                }
            } catch (NumberFormatException e) {
                sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "ID istanza non valido."));
            }
            return;
        }

        // =========================================================================
        // 8. CLEAR
        // =========================================================================
        if (sub.equals("clear")) {
            UnrealVFXAPI.clearAll();
            VFXNetwork.CHANNEL.sendToAll(PacketVFX.clearAll());
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "[UnrealVFX] Tutti gli effetti virtuali e visivi sono stati rimossi."));
            return;
        }

        // =========================================================================
        // 9. MOVE / POSITION
        // =========================================================================
        if (sub.equals("move")) {
            if (args.length < 5) {
                throw new WrongUsageException("/vfx move <virtual_id> <x> <y> <z>");
            }
            try {
                int id = Integer.parseInt(args[1]);
                double x = Double.parseDouble(args[2]);
                double y = Double.parseDouble(args[3]);
                double z = Double.parseDouble(args[4]);

                ServerVFXInstance inst = UnrealVFXAPI.getInstance(id);
                if (inst != null) {
                    inst.setPosition(x, y, z);
                    sender.addChatMessage(new ChatComponentText(EnumChatFormatting.AQUA + "[UnrealVFX] Posizione di #" + id + " aggiornata."));
                } else {
                    sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Istanza #" + id + " non trovata."));
                }
            } catch (NumberFormatException e) {
                sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Coordinate o ID non validi."));
            }
            return;
        }

        // =========================================================================
        // 10. VELOCITY / MOTION
        // =========================================================================
        if (sub.equals("velocity")) {
            if (args.length < 5) {
                throw new WrongUsageException("/vfx velocity <virtual_id> <vx> <vy> <vz>");
            }
            try {
                int id = Integer.parseInt(args[1]);
                double vx = Double.parseDouble(args[2]);
                double vy = Double.parseDouble(args[3]);
                double vz = Double.parseDouble(args[4]);

                ServerVFXInstance inst = UnrealVFXAPI.getInstance(id);
                if (inst != null) {
                    inst.setVelocity(vx, vy, vz);
                    sender.addChatMessage(new ChatComponentText(EnumChatFormatting.AQUA + "[UnrealVFX] Velocità di #" + id + " aggiornata."));
                } else {
                    sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Istanza #" + id + " non trovata."));
                }
            } catch (NumberFormatException e) {
                sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Valori non validi."));
            }
            return;
        }

        // =========================================================================
        // 11. ROTATION
        // =========================================================================
        if (sub.equals("rot")) {
            if (args.length < 4) {
                throw new WrongUsageException("/vfx rot <virtual_id> <yaw> <pitch> [roll]");
            }
            try {
                int id = Integer.parseInt(args[1]);
                float yaw = Float.parseFloat(args[2]);
                float pitch = Float.parseFloat(args[3]);
                float roll = args.length >= 5 ? Float.parseFloat(args[4]) : 0.0f;

                ServerVFXInstance inst = UnrealVFXAPI.getInstance(id);
                if (inst != null) {
                    inst.setRotation(yaw, pitch, roll);
                    sender.addChatMessage(new ChatComponentText(EnumChatFormatting.AQUA + "[UnrealVFX] Rotazione di #" + id + " aggiornata."));
                } else {
                    sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Istanza #" + id + " non trovata."));
                }
            } catch (NumberFormatException e) {
                sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Angoli non validi."));
            }
            return;
        }

        // =========================================================================
        // 12. ROTATION VELOCITY
        // =========================================================================
        if (sub.equals("rotvel")) {
            if (args.length < 4) {
                throw new WrongUsageException("/vfx rotvel <virtual_id> <dyaw> <dpitch> [droll]");
            }
            try {
                int id = Integer.parseInt(args[1]);
                float dyaw = Float.parseFloat(args[2]);
                float dpitch = Float.parseFloat(args[3]);
                float droll = args.length >= 5 ? Float.parseFloat(args[4]) : 0.0f;

                ServerVFXInstance inst = UnrealVFXAPI.getInstance(id);
                if (inst != null) {
                    inst.setRotationVelocity(dyaw, dpitch, droll);
                    sender.addChatMessage(new ChatComponentText(EnumChatFormatting.AQUA + "[UnrealVFX] Velocità di rotazione di #" + id + " aggiornata."));
                } else {
                    sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Istanza #" + id + " non trovata."));
                }
            } catch (NumberFormatException e) {
                sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Valori non validi."));
            }
            return;
        }

        // =========================================================================
        // 13. SCALE
        // =========================================================================
        if (sub.equals("scale")) {
            if (args.length < 3) {
                throw new WrongUsageException("/vfx scale <virtual_id> <scale>");
            }
            try {
                int id = Integer.parseInt(args[1]);
                float scale = Float.parseFloat(args[2]);

                ServerVFXInstance inst = UnrealVFXAPI.getInstance(id);
                if (inst != null) {
                    inst.setScale(scale);
                    sender.addChatMessage(new ChatComponentText(EnumChatFormatting.AQUA + "[UnrealVFX] Scala di #" + id + " impostata a " + scale));
                } else {
                    sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Istanza #" + id + " non trovata."));
                }
            } catch (NumberFormatException e) {
                sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Scala o ID non validi."));
            }
            return;
        }

        // =========================================================================
        // 14. SKETCH
        // =========================================================================
        if (sub.equals("sketch")) {
            int mode = -1; // toggle default
            if (args.length >= 2) {
                String arg1 = args[1].toLowerCase();
                if (arg1.equals("on") || arg1.equals("true") || arg1.equals("enable")) {
                    mode = 1;
                } else if (arg1.equals("off") || arg1.equals("false") || arg1.equals("disable")) {
                    mode = 0;
                } else if (arg1.equals("toggle")) {
                    mode = -1;
                } else {
                    try {
                        mode = Integer.parseInt(arg1);
                    } catch (NumberFormatException ignored) {}
                }
            }

            VFXNetwork.CHANNEL.sendToAll(PacketVFX.sketch(mode));
            String status = (mode == 1) ? "ATTIVATO (Continuo)" :
                            (mode == 0) ? "DISATTIVATO" :
                            (mode == -1) ? "TOGGLE (Stato invertito)" :
                            "BURST (" + mode + " ticks / " + String.format("%.1f", mode / 20.0f) + "s)";
            sender.addChatMessage(new ChatComponentText(
                EnumChatFormatting.DARK_PURPLE + "[UnrealVFX] " +
                EnumChatFormatting.WHITE + "Filtro Anime Sakuga Sketch: " +
                EnumChatFormatting.YELLOW + status
            ));
            return;
        }

        throw new WrongUsageException(getCommandUsage(sender));
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args,
                "spawn", "attach", "detach", "clear", "reload", "instances",
                "move", "velocity", "rot", "rotvel", "scale", "kill", "sketch", "list"
            );
        } else if (args.length == 2 && args[0].equalsIgnoreCase("sketch")) {
            return getListOfStringsMatchingLastWord(args, "on", "off", "toggle", "40", "80", "120");
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("spawn") || args[0].equalsIgnoreCase("attach"))) {
            List<String> options = new ArrayList<>();
            for (VFXSchematic s : VFXSchematicRegistry.getInstance().getAll()) {
                options.add(s.getId());
            }
            options.add("solar_flare");
            options.add("cosmic_vortex");
            options.add("divine_shield");
            options.add("energy_slash");
            options.add("cosmic:gargantua");
            options.add("cosmic:black_hole");
            options.add("dbz:hakai_ball");
            options.add("dbz:hakai_explosion");
            options.add("dbz:sparking_aura_ssj");
            options.add("dbz:sparking_explosion");
            options.add("dbz:zanzoken_dash");
            options.add("dbz:energy_beam");
            options.add("dbz:energy_disc");
            options.add("dbz:energy_dome");
            return getListOfStringsMatchingLastWord(args, options.toArray(new String[0]));
        }
        return null;
    }

    private static boolean isCoordinate(String s) {
        if (s == null || s.isEmpty()) return false;
        if (s.startsWith("~")) return true;
        try {
            Double.parseDouble(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static double parseCoord(double base, String arg) {
        if (arg.startsWith("~")) {
            if (arg.length() == 1) return base;
            return base + Double.parseDouble(arg.substring(1));
        }
        return Double.parseDouble(arg);
    }
}
