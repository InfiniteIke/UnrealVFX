package com.unrealvfx.schematic;

import com.unrealvfx.UnrealVFX;
import com.unrealvfx.network.PacketVFXSchematicSync;
import com.unrealvfx.network.VFXNetwork;
import cpw.mods.fml.common.FMLCommonHandler;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registro centrale per la gestione degli schematic VFX su server e client.
 * Gestisce il caricamento da disco (cartella config/unrealvfx/effects/),
 * l'esportazione dei preset predefiniti e l'hot-reloading senza riavvio.
 *
 * 100% Server-Safe: la registrazione grafica client avviene esclusivamente tramite SidedProxy.
 */
public class VFXSchematicRegistry {
    private static final VFXSchematicRegistry INSTANCE = new VFXSchematicRegistry();

    private final Map<String, VFXSchematic> schematics = new ConcurrentHashMap<>();
    private File effectsDirectory;

    public static VFXSchematicRegistry getInstance() {
        return INSTANCE;
    }

    private VFXSchematicRegistry() {}

    public void init(File configBaseDir) {
        this.effectsDirectory = new File(configBaseDir, "unrealvfx/effects");
        if (!effectsDirectory.exists()) {
            effectsDirectory.mkdirs();
        }

        // Assicura che tutti gli schematic predefiniti esistano sul disco (senza sovrascrivere file modificati)
        writeDefaultSchematics();

        loadAllFromDisk();
    }

    public int reload() {
        schematics.clear();
        int loaded = loadAllFromDisk();

        // Se siamo su server, sincronizza tutti gli schematics a tutti i giocatori connessi
        if (FMLCommonHandler.instance().getEffectiveSide().isServer()) {
            for (VFXSchematic schematic : schematics.values()) {
                VFXNetwork.CHANNEL.sendToAll(new PacketVFXSchematicSync(schematic.getId(), schematic.getJsonSource()));
            }
        }

        return loaded;
    }

    public int loadAllFromDisk() {
        int count = 0;
        Set<String> loadedFileKeys = new HashSet<>();

        // 1. Directory primaria di Forge (es. run/client/config/unrealvfx/effects)
        if (effectsDirectory != null && effectsDirectory.exists()) {
            count += loadFromDirectory(effectsDirectory, loadedFileKeys);
        }

        // 2. Directory di progetto radice IntelliJ (config/unrealvfx/effects)
        File rootWorkspaceDir = new File("config/unrealvfx/effects");
        if (rootWorkspaceDir.exists() && (effectsDirectory == null || !rootWorkspaceDir.getAbsolutePath().equals(effectsDirectory.getAbsolutePath()))) {
            count += loadFromDirectory(rootWorkspaceDir, loadedFileKeys);
        }

        // 3. Fallback relativo superiore (../../config/unrealvfx/effects)
        File parentWorkspaceDir = new File("../../config/unrealvfx/effects");
        if (parentWorkspaceDir.exists() && (effectsDirectory == null || !parentWorkspaceDir.getAbsolutePath().equals(effectsDirectory.getAbsolutePath()))) {
            count += loadFromDirectory(parentWorkspaceDir, loadedFileKeys);
        }

        UnrealVFX.LOGGER.info("Caricati con successo " + count + " schematics VFX in totale.");
        return count;
    }

    private int loadFromDirectory(File dir, Set<String> loadedFileKeys) {
        File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".json"));
        if (files == null) return 0;
        int count = 0;

        for (File file : files) {
            String fileName = file.getName().toLowerCase();
            if (loadedFileKeys.contains(fileName)) continue; // Non caricare duplicati se già caricati dalla directory primaria

            try (InputStreamReader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
                StringBuilder sb = new StringBuilder();
                char[] buf = new char[2048];
                int read;
                while ((read = reader.read(buf)) != -1) {
                    sb.append(buf, 0, read);
                }

                String json = sb.toString();
                VFXSchematic schematic = VFXSchematicParser.parseHeader(json);
                schematics.put(schematic.getId(), schematic);
                loadedFileKeys.add(fileName);
                count++;

                // Delega al proxy (su client compila il blueprint GL; su server dedicato è un no-op)
                UnrealVFX.proxy.registerClientSchematic(schematic.getId(), json);
            } catch (Exception e) {
                UnrealVFX.LOGGER.error("Impossibile caricare lo schematic VFX dal file: " + file.getAbsolutePath(), e);
            }
        }
        return count;
    }

    public void register(VFXSchematic schematic) {
        if (schematic != null) {
            schematics.put(schematic.getId(), schematic);
        }
    }

    public VFXSchematic get(String id) {
        return schematics.get(id);
    }

    public boolean has(String id) {
        return schematics.containsKey(id);
    }

    public Collection<VFXSchematic> getAll() {
        return schematics.values();
    }

    public File getEffectsDirectory() {
        return effectsDirectory;
    }

    private void writeDefaultSchematics() {
        writeSchematicFile("solar_flare.json", DefaultSchematics.SOLAR_FLARE);
        writeSchematicFile("cosmic_vortex.json", DefaultSchematics.COSMIC_VORTEX);
        writeSchematicFile("divine_shield.json", DefaultSchematics.DIVINE_SHIELD);
        writeSchematicFile("energy_slash.json", DefaultSchematics.ENERGY_SLASH);
        writeSchematicFile("cube_laser.json", DefaultSchematics.CUBE_LASER);
        writeSchematicFile("gigantic_meteor.json", DefaultSchematics.GIGANTIC_METEOR);
        writeSchematicFile("beam_cannon.json", DefaultSchematics.BEAM_CANNON);
        writeSchematicFile("reality_cube_vortex.json", DefaultSchematics.REALITY_CUBE_VORTEX);
        writeSchematicFile("aura_test_purple.json", DefaultSchematics.AURA_TEST_PURPLE);
        writeSchematicFile("supernova_heat_haze.json", DefaultSchematics.SUPERNOVA_HEAT_HAZE);
        writeSchematicFile("heat_mirage_zone.json", DefaultSchematics.HEAT_MIRAGE_ZONE);
        writeSchematicFile("divine_sanctuary_light.json", DefaultSchematics.DIVINE_SANCTUARY_LIGHT);
        writeSchematicFile("celestial_judgment_pillar.json", DefaultSchematics.CELESTIAL_JUDGMENT_PILLAR);
        writeSchematicFile("cyber_matrix_beacon.json", DefaultSchematics.CYBER_MATRIX_BEACON);
        writeSchematicFile("abyssal_eclipse_well.json", DefaultSchematics.ABYSSAL_ECLIPSE_WELL);
        writeSchematicFile("valkyrie_aurora_shrine.json", DefaultSchematics.VALKYRIE_AURORA_SHRINE);
        writeSchematicFile("light_sacred_cathedral.json", DefaultSchematics.LIGHT_SACRED_CATHEDRAL);
        writeSchematicFile("light_celestial_pillar.json", DefaultSchematics.LIGHT_CELESTIAL_PILLAR);
        writeSchematicFile("light_cyber_holobeacon.json", DefaultSchematics.LIGHT_CYBER_HOLOBEACON);
        writeSchematicFile("light_abyssal_singularity.json", DefaultSchematics.LIGHT_ABYSSAL_SINGULARITY);
        writeSchematicFile("light_valkyrie_aurora.json", DefaultSchematics.LIGHT_VALKYRIE_AURORA);
    }

    private void writeSchematicFile(String fileName, String content) {
        if (effectsDirectory == null) return;
        File target = new File(effectsDirectory, fileName);
        if (target.exists()) return;
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(target), StandardCharsets.UTF_8)) {
            writer.write(content);
        } catch (IOException e) {
            UnrealVFX.LOGGER.error("Impossibile scrivere il preset schematic predefinito: " + fileName, e);
        }
    }
}
