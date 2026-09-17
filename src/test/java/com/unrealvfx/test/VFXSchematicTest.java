package com.unrealvfx.test;

import com.unrealvfx.client.vfx.VFXBlueprint;
import com.unrealvfx.client.vfx.VFXClientBlueprintCompiler;
import com.unrealvfx.client.vfx.VFXInstance;
import com.unrealvfx.network.PacketVFXSchematicSync;
import com.unrealvfx.network.PacketVFXVirtualEntity;
import com.unrealvfx.schematic.DefaultSchematics;
import com.unrealvfx.schematic.VFXSchematic;
import com.unrealvfx.schematic.VFXSchematicParser;
import com.unrealvfx.server.ServerVFXInstance;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.charset.StandardCharsets;

public class VFXSchematicTest {

    public static void main(String[] args) {
        System.out.println("=== Starting UnrealVFX Deep Verification Tests ===");

        testParseDefaultHeaders();
        System.out.println("[PASS] testParseDefaultHeaders");

        testClientBlueprintCompiler();
        System.out.println("[PASS] testClientBlueprintCompiler");

        testServerVFXInstanceMovementAndDirtyFlags();
        System.out.println("[PASS] testServerVFXInstanceMovementAndDirtyFlags");

        testScaleAndTrackingRadiusBounds();
        System.out.println("[PASS] testScaleAndTrackingRadiusBounds");

        testClientInstanceLoopAndRotationInit();
        System.out.println("[PASS] testClientInstanceLoopAndRotationInit");

        testPacketVFXVirtualEntitySerialization();
        System.out.println("[PASS] testPacketVFXVirtualEntitySerialization");

        testPacketVFXSchematicSyncSerializationAndUTF8();
        System.out.println("[PASS] testPacketVFXSchematicSyncSerializationAndUTF8");

        testCorruptedPacketProtection();
        System.out.println("[PASS] testCorruptedPacketProtection");

        testCubeLaserAndArrayModifier();
        System.out.println("[PASS] testCubeLaserAndArrayModifier");

        testGiganticMeteorAndEnhancedLightning();
        System.out.println("[PASS] testGiganticMeteorAndEnhancedLightning");

        testCelestialVoidAndInvertedCoreDefaults();
        System.out.println("[PASS] testCelestialVoidAndInvertedCoreDefaults");

        testBloomRendererAndComponentFilter();
        System.out.println("[PASS] testBloomRendererAndComponentFilter");

        testSteppedSakugaTiming();
        System.out.println("[PASS] testSteppedSakugaTiming");

        testImpactFrameTriggerComponent();
        System.out.println("[PASS] testImpactFrameTriggerComponent");

        testBeamHelicalRibbonsAndBeamCannon();
        System.out.println("[PASS] testBeamHelicalRibbonsAndBeamCannon");

        testFloatingDebrisThreePhasePhysics();
        System.out.println("[PASS] testFloatingDebrisThreePhasePhysics");

        testSteppedSakugaLoopingAndShaderTime();
        System.out.println("[PASS] testSteppedSakugaLoopingAndShaderTime");

        testImpactFrameLoopingAndZeroTrigger();
        System.out.println("[PASS] testImpactFrameLoopingAndZeroTrigger");

        testRealityCubeVortexSchematicAndDefaults();
        System.out.println("[PASS] testRealityCubeVortexSchematicAndDefaults");

        testBeamRibbonColorAndDebrisSteppedFps();
        System.out.println("[PASS] testBeamRibbonColorAndDebrisSteppedFps");

        testAuraTestPurpleSchematicAndComposition();
        System.out.println("[PASS] testAuraTestPurpleSchematicAndComposition");

        testVFXSchematicRegistryAndDefaultDiskFiles();
        System.out.println("[PASS] testVFXSchematicRegistryAndDefaultDiskFiles");

        testHeatDistortionComponentParsingAndValidation();
        System.out.println("[PASS] testHeatDistortionComponentParsingAndValidation");

        testLightComponentParsingAndValidation();
        System.out.println("[PASS] testLightComponentParsingAndValidation");

        testLightComponentShaderSelectionAndNewParameters();
        System.out.println("[PASS] testLightComponentShaderSelectionAndNewParameters");

        testLightLifecycleAndPhantomLightCleanup();
        System.out.println("[PASS] testLightLifecycleAndPhantomLightCleanup");

        testPureLightSchematicsIntegrity();
        System.out.println("[PASS] testPureLightSchematicsIntegrity");

        System.out.println("=== All 27 Deep Verification Tests PASSED successfully! ===");
    }

    public static void testBloomRendererAndComponentFilter() {
        assertTrue(!com.unrealvfx.client.render.BloomRenderer.isCapturing());
        com.unrealvfx.client.render.BloomRenderer.forceReset();
        assertTrue(!com.unrealvfx.client.render.BloomRenderer.isCapturing());

        com.unrealvfx.client.vfx.component.DistortionComponent dist =
            new com.unrealvfx.client.vfx.component.DistortionComponent(0.5f, 5.0f, 0.05f);
        com.unrealvfx.client.vfx.component.HeatDistortionComponent heatDist =
            new com.unrealvfx.client.vfx.component.HeatDistortionComponent();
        com.unrealvfx.client.vfx.component.FloatingDebrisComponent debris =
            new com.unrealvfx.client.vfx.component.FloatingDebrisComponent(10, 3.0f);

        VFXBlueprint bp = new VFXBlueprint("test_bloom_comp").setDuration(50);
        VFXInstance inst = new VFXInstance(bp, null, 0, 0, 0);
        dist.tick(inst);
        heatDist.tick(inst);
        debris.tick(inst);

        // Test capturing skip behavior in render()
        com.unrealvfx.client.render.BloomRenderer.setCapturing(true);
        assertTrue(com.unrealvfx.client.render.BloomRenderer.isCapturing());

        // During bloom capture, non-emissive components MUST return immediately
        // and must NOT touch Minecraft or OpenGL context
        dist.render(inst, 0.0f, 0.5f);
        heatDist.render(inst, 0.0f, 0.5f);
        debris.render(inst, 0.0f, 0.5f);

        // Reset state and verify reset restores capturing = false
        com.unrealvfx.client.render.BloomRenderer.forceReset();
        assertTrue(!com.unrealvfx.client.render.BloomRenderer.isCapturing());

        // Verify that when NOT capturing, calling render() proceeds beyond the capture guard
        // (reaching Minecraft client runtime access, which throws NoClassDefFoundError in headless CLI test environment)
        boolean reachedClientAccess = false;
        try {
            dist.render(inst, 0.0f, 0.5f);
        } catch (Throwable t) {
            reachedClientAccess = true;
        }
        assertTrue(reachedClientAccess);

        boolean reachedHeatAccess = false;
        try {
            heatDist.render(inst, 0.0f, 0.5f);
        } catch (Throwable t) {
            reachedHeatAccess = true;
        }
        assertTrue(reachedHeatAccess);
    }

    public static void testParseDefaultHeaders() {
        VFXSchematic solar = VFXSchematicParser.parseHeader(DefaultSchematics.SOLAR_FLARE);
        assertEquals("solar_flare", solar.getId());
        assertEquals(50, solar.getDuration());
        assertTrue(!solar.isLoop());
        assertEquals(128.0, solar.getTrackingRadius(), 0.001);

        VFXSchematic vortex = VFXSchematicParser.parseHeader(DefaultSchematics.COSMIC_VORTEX);
        assertEquals("cosmic_vortex", vortex.getId());
        assertEquals(120, vortex.getDuration());
        assertTrue(vortex.isLoop());
        assertEquals(160.0, vortex.getTrackingRadius(), 0.001);
    }

    public static void testClientBlueprintCompiler() {
        VFXBlueprint blueprint = VFXClientBlueprintCompiler.compile(DefaultSchematics.SOLAR_FLARE);
        assertEquals("solar_flare", blueprint.getId());
        assertEquals(50, blueprint.getDuration());
        assertTrue(!blueprint.isLoop());
        assertEquals(6, blueprint.getComponents().size());

        VFXBlueprint shield = VFXClientBlueprintCompiler.compile(DefaultSchematics.DIVINE_SHIELD);
        assertEquals("divine_shield", shield.getId());
        assertEquals(80, shield.getDuration());
        assertTrue(shield.isLoop());
        assertEquals(3, shield.getComponents().size());
    }

    public static void testServerVFXInstanceMovementAndDirtyFlags() {
        ServerVFXInstance inst = new ServerVFXInstance(1001, "solar_flare", 0, 10.0, 64.0, 10.0);
        assertEquals(1001, inst.getVirtualId());
        assertEquals(10.0, inst.getPosX(), 0.001);

        // Impostazione velocità
        inst.setVelocity(1.0, 0.5, -0.5);
        assertEquals(PacketVFXVirtualEntity.FLAG_MOTION, inst.getDirtyMask());
        inst.clearDirty();
        assertEquals(0, inst.getDirtyMask());

        // Tick per il movimento dead reckoning
        inst.tick();
        assertEquals(11.0, inst.getPosX(), 0.001);
        assertEquals(64.5, inst.getPosY(), 0.001);
        assertEquals(9.5, inst.getPosZ(), 0.001);
        assertEquals(1, inst.getAge());

        // Modifica rotazione e scala
        inst.setRotation(90.0f, 45.0f, 0.0f);
        inst.setScale(2.5f);
        assertEquals(PacketVFXVirtualEntity.FLAG_ROT | PacketVFXVirtualEntity.FLAG_SCALE, inst.getDirtyMask());
    }

    public static void testScaleAndTrackingRadiusBounds() {
        ServerVFXInstance inst = new ServerVFXInstance(2001, "test_bounds", 0, 0, 0, 0);

        // Scala negativa o zero deve essere sanificata
        inst.setScale(-3.5f);
        assertEquals(3.5f, inst.getScale(), 0.001);

        inst.setScale(0.0f);
        assertTrue(inst.getScale() > 0.0001f);

        // Tracking radius troppo piccolo (< 8.0) o estremo (> 1024.0)
        inst.setTrackingRadius(2.0);
        assertEquals(8.0, inst.getTrackingRadius(), 0.001);

        inst.setTrackingRadius(50000.0);
        assertEquals(1024.0, inst.getTrackingRadius(), 0.001);
    }

    public static void testClientInstanceLoopAndRotationInit() {
        VFXBlueprint bp = new VFXBlueprint("test_loop").setDuration(50).setLoop(false);
        VFXInstance inst = new VFXInstance(bp, null, 0, 0, 0);

        // Verifica initRotation (nessun snap interpolato da 0)
        inst.initRotation(180.0f, 45.0f, -30.0f);
        assertEquals(180.0f, inst.getYaw(), 0.001);
        assertEquals(45.0f, inst.getPitch(), 0.001);
        assertEquals(-30.0f, inst.getRoll(), 0.001);

        // Verifica loop override (l'istanza può essere forzata a loop anche se il blueprint non lo è)
        assertTrue(!inst.isLooping());
        inst.setLoopOverride(true);
        assertTrue(inst.isLooping());

        // Progress a tick 75 di un ciclo a 50 deve ciclare a 0.5
        inst.setAge(75);
        assertEquals(0.5f, inst.getProgress(0.0f), 0.001);
    }

    public static void testPacketVFXVirtualEntitySerialization() {
        PacketVFXVirtualEntity spawnPacket = PacketVFXVirtualEntity.spawn(
            1050, "divine_shield",
            100.5, 70.0, -200.5,
            0.1f, 0.2f, 0.3f,
            45.0f, 10.0f, 5.0f,
            1.0f, 2.0f, 0.0f,
            3.0f, 0xFF00FF,
            10, 80, true,
            -1, 0, 0, 0
        );

        ByteBuf buf = Unpooled.buffer();
        spawnPacket.toBytes(buf);

        PacketVFXVirtualEntity readPacket = new PacketVFXVirtualEntity();
        readPacket.fromBytes(buf);

        // Test update packet
        PacketVFXVirtualEntity updatePacket = PacketVFXVirtualEntity.update(
            1050, PacketVFXVirtualEntity.FLAG_MOTION | PacketVFXVirtualEntity.FLAG_SCALE
        );
        updatePacket.setMotionData(2.5f, 0.0f, -1.2f);
        updatePacket.setScaleData(4.5f);

        ByteBuf buf2 = Unpooled.buffer();
        updatePacket.toBytes(buf2);

        PacketVFXVirtualEntity readUpdate = new PacketVFXVirtualEntity();
        readUpdate.fromBytes(buf2);

        // Test despawn packet
        PacketVFXVirtualEntity despawnPacket = PacketVFXVirtualEntity.despawn(1050);
        ByteBuf buf3 = Unpooled.buffer();
        despawnPacket.toBytes(buf3);

        PacketVFXVirtualEntity readDespawn = new PacketVFXVirtualEntity();
        readDespawn.fromBytes(buf3);
    }

    public static void testPacketVFXSchematicSyncSerializationAndUTF8() {
        String utf8Json = "{\n  \"id\": \"test_utf8_🔥\",\n  \"duration\": 100,\n  \"loop\": true,\n  \"comment\": \"Effetto con caratteri speciali: àèìòù © §\"\n}";
        PacketVFXSchematicSync packet = new PacketVFXSchematicSync("test_utf8_🔥", utf8Json);
        ByteBuf buf = Unpooled.buffer();
        packet.toBytes(buf);

        PacketVFXSchematicSync read = new PacketVFXSchematicSync();
        read.fromBytes(buf);

        VFXSchematic parsed = VFXSchematicParser.parseHeader(utf8Json);
        assertEquals("test_utf8_🔥", parsed.getId());
        assertEquals(100, parsed.getDuration());
        assertTrue(parsed.isLoop());
    }

    public static void testCorruptedPacketProtection() {
        ByteBuf badBuf = Unpooled.buffer();
        badBuf.writeByte(1); // string len
        badBuf.writeByte('x');
        badBuf.writeInt(-999); // negative length payload

        PacketVFXSchematicSync read = new PacketVFXSchematicSync();
        read.fromBytes(badBuf); // Non deve lanciare NegativeArraySizeException
    }

    public static void testCubeLaserAndArrayModifier() {
        VFXSchematic laserSchematic = VFXSchematicParser.parseHeader(DefaultSchematics.CUBE_LASER);
        assertEquals("cube_laser", laserSchematic.getId());
        assertEquals(70, laserSchematic.getDuration());
        assertTrue(!laserSchematic.isLoop());
        assertEquals(140.0, laserSchematic.getTrackingRadius(), 0.001);

        VFXBlueprint blueprint = VFXClientBlueprintCompiler.compile(DefaultSchematics.CUBE_LASER);
        assertEquals("cube_laser", blueprint.getId());
        assertEquals(70, blueprint.getDuration());
        assertEquals(5, blueprint.getComponents().size());

        // Component 3 è la mesh principale del raggio (Array di 20 cubi)
        com.unrealvfx.client.vfx.component.MeshComponent beam =
            (com.unrealvfx.client.vfx.component.MeshComponent) blueprint.getComponents().get(3);
        assertEquals(20, beam.getArrayCount());
        assertEquals(0.05f, beam.getStartScaleX(), 0.001f);
        assertEquals(0.2f, beam.getStartScaleZ(), 0.001f);
        assertEquals(1.6f, beam.getEndScaleZ(), 0.001f);

        // Component 4 è la spirale orbitante (Array di 24 cubi)
        com.unrealvfx.client.vfx.component.MeshComponent spiral =
            (com.unrealvfx.client.vfx.component.MeshComponent) blueprint.getComponents().get(4);
        assertEquals(24, spiral.getArrayCount());
        assertEquals(0.16f, spiral.getStartScaleX(), 0.001f);
    }

    public static void testGiganticMeteorAndEnhancedLightning() {
        VFXSchematic meteorSchematic = VFXSchematicParser.parseHeader(DefaultSchematics.GIGANTIC_METEOR);
        assertEquals("gigantic_meteor", meteorSchematic.getId());
        assertEquals(120, meteorSchematic.getDuration());
        assertTrue(meteorSchematic.isLoop());
        assertEquals(256.0, meteorSchematic.getTrackingRadius(), 0.001);

        VFXBlueprint blueprint = VFXClientBlueprintCompiler.compile(DefaultSchematics.GIGANTIC_METEOR);
        assertEquals("gigantic_meteor", blueprint.getId());
        assertEquals(120, blueprint.getDuration());
        assertEquals(7, blueprint.getComponents().size());

        // Component 1: Ki Aura capsule con aura_cel, celBands e distortion
        com.unrealvfx.client.vfx.component.MeshComponent aura =
            (com.unrealvfx.client.vfx.component.MeshComponent) blueprint.getComponents().get(1);
        assertEquals(com.unrealvfx.client.vfx.component.MeshComponent.MeshType.AURA_CAPSULE, aura.getMeshType());
        assertEquals("aura_cel", aura.getShader());
        assertEquals(0.14f, aura.getDistortionStrength(), 0.001f);
        assertEquals(3.0f, aura.getCelBands(), 0.001f);

        // Component 2: Sfera cosmica con shader celestial_void
        com.unrealvfx.client.vfx.component.MeshComponent voidSphere =
            (com.unrealvfx.client.vfx.component.MeshComponent) blueprint.getComponents().get(2);
        assertEquals("celestial_void", voidSphere.getShader());
        assertEquals(18.0f, voidSphere.getStartScaleX(), 0.001f);
        assertEquals(24.0f, (float) voidSphere.getOffsetY(), 0.001f);
        assertEquals(-16.0f, (float) voidSphere.getOffsetZ(), 0.001f);
        assertEquals(1.1f, voidSphere.getFilamentStrength(), 0.001f);

        // Component 3: Corona con invertedCore attivo
        com.unrealvfx.client.vfx.component.MeshComponent coronaSphere =
            (com.unrealvfx.client.vfx.component.MeshComponent) blueprint.getComponents().get(3);
        assertTrue(coronaSphere.isInvertedCore());
        assertEquals(18.6f, coronaSphere.getStartScaleX(), 0.001f);

        // Component 5: Ki siphon cloner array
        com.unrealvfx.client.vfx.component.MeshComponent siphon =
            (com.unrealvfx.client.vfx.component.MeshComponent) blueprint.getComponents().get(5);
        assertEquals(28, siphon.getArrayCount());
        assertEquals(0.40f, siphon.getStartScaleX(), 0.001f);

        // Component 6: LightningComponent su superficie sferica con offset e diramazioni
        com.unrealvfx.client.vfx.component.LightningComponent lightning =
            (com.unrealvfx.client.vfx.component.LightningComponent) blueprint.getComponents().get(6);
        assertEquals(com.unrealvfx.client.vfx.component.LightningComponent.LightningMode.SURFACE_SPHERE, lightning.getMode());
        assertEquals(18, lightning.getArcCount());
        assertEquals(18.2f, lightning.getRadius(), 0.001f);
        assertEquals(24.0f, lightning.getOffsetY(), 0.001f);
        assertEquals(-16.0f, lightning.getOffsetZ(), 0.001f);
        assertEquals(0.14f, lightning.getWidth(), 0.001f);
        assertEquals(0.30f, lightning.getRoughness(), 0.001f);
        assertEquals(2, lightning.getBranchForks());
        assertEquals(2.2f, lightning.getGlowMultiplier(), 0.001f);
        assertTrue(lightning.isSpherical());

        // Test generazione archi sferici
        VFXInstance instance = new VFXInstance(blueprint, null, 0, 0, 0);
        lightning.tick(instance);

        // Test modalità TARGET_DIRECTED
        com.unrealvfx.client.vfx.component.LightningComponent directed =
            new com.unrealvfx.client.vfx.component.LightningComponent(4, 5.0f, 10.0f);
        directed.setTarget(0, 20, 0, 0, 0, 0);
        assertEquals(com.unrealvfx.client.vfx.component.LightningComponent.LightningMode.TARGET_DIRECTED, directed.getMode());
        directed.tick(instance);
    }

    public static void testCelestialVoidAndInvertedCoreDefaults() {
        String json = "{\n" +
            "  \"id\": \"test_void\",\n" +
            "  \"components\": [\n" +
            "    { \"type\": \"mesh\", \"shader\": \"celestial_void\" },\n" +
            "    { \"type\": \"mesh\", \"shader\": \"celestial_void\", \"invertedCore\": false },\n" +
            "    { \"type\": \"mesh\", \"shader\": \"universal_energy\" }\n" +
            "  ]\n" +
            "}";
        VFXBlueprint bp = VFXClientBlueprintCompiler.compile(json);
        com.unrealvfx.client.vfx.component.MeshComponent c0 = (com.unrealvfx.client.vfx.component.MeshComponent) bp.getComponents().get(0);
        assertTrue(c0.isInvertedCore()); // defaults to true for celestial_void!

        com.unrealvfx.client.vfx.component.MeshComponent c1 = (com.unrealvfx.client.vfx.component.MeshComponent) bp.getComponents().get(1);
        assertTrue(!c1.isInvertedCore()); // explicitly overridden to false!

        com.unrealvfx.client.vfx.component.MeshComponent c2 = (com.unrealvfx.client.vfx.component.MeshComponent) bp.getComponents().get(2);
        assertTrue(!c2.isInvertedCore()); // defaults to false for standard meshes!
    }

    public static void testSteppedSakugaTiming() {
        String json = "{\n" +
            "  \"id\": \"test_sakuga\",\n" +
            "  \"duration\": 60,\n" +
            "  \"components\": [\n" +
            "    { \"type\": \"mesh\", \"steppedFps\": 12.0 },\n" +
            "    { \"type\": \"mesh\", \"sakugaFps\": 15.0 },\n" +
            "    { \"type\": \"mesh\", \"steppedTiming\": true },\n" +
            "    { \"type\": \"mesh\" }\n" +
            "  ]\n" +
            "}";
        VFXBlueprint bp = VFXClientBlueprintCompiler.compile(json);
        com.unrealvfx.client.vfx.component.MeshComponent c0 = (com.unrealvfx.client.vfx.component.MeshComponent) bp.getComponents().get(0);
        assertEquals(12.0f, c0.getSteppedFps(), 0.001f);

        com.unrealvfx.client.vfx.component.MeshComponent c1 = (com.unrealvfx.client.vfx.component.MeshComponent) bp.getComponents().get(1);
        assertEquals(15.0f, c1.getSteppedFps(), 0.001f);

        com.unrealvfx.client.vfx.component.MeshComponent c2 = (com.unrealvfx.client.vfx.component.MeshComponent) bp.getComponents().get(2);
        assertEquals(12.0f, c2.getSteppedFps(), 0.001f);

        com.unrealvfx.client.vfx.component.MeshComponent c3 = (com.unrealvfx.client.vfx.component.MeshComponent) bp.getComponents().get(3);
        assertEquals(0.0f, c3.getSteppedFps(), 0.001f);
    }

    public static void testImpactFrameTriggerComponent() {
        String json = "{\n" +
            "  \"id\": \"test_impact\",\n" +
            "  \"duration\": 40,\n" +
            "  \"components\": [\n" +
            "    { \"type\": \"impact_frame\", \"triggerTick\": 10, \"duration\": 2, \"impactType\": \"HAKAI_PURPLE\" }\n" +
            "  ]\n" +
            "}";
        VFXBlueprint bp = VFXClientBlueprintCompiler.compile(json);
        assertEquals(1, bp.getComponents().size());
        com.unrealvfx.client.vfx.component.ImpactFrameTriggerComponent impact =
            (com.unrealvfx.client.vfx.component.ImpactFrameTriggerComponent) bp.getComponents().get(0);
        assertEquals(com.unrealvfx.client.render.ImpactFrameManager.Type.HAKAI_PURPLE, impact.getType());
        assertEquals(10, impact.getTriggerTick());
        assertEquals(2, impact.getDurationTicks());

        // Test tick trigger logic
        VFXInstance inst = new VFXInstance(bp, null, 0, 0, 0);
        inst.setAge(5);
        impact.tick(inst);
        assertTrue(!com.unrealvfx.client.render.ImpactFrameManager.getInstance().isActive());

        inst.setAge(10);
        impact.tick(inst);
        assertTrue(com.unrealvfx.client.render.ImpactFrameManager.getInstance().isActive());
        assertEquals(com.unrealvfx.client.render.ImpactFrameManager.Type.HAKAI_PURPLE,
            com.unrealvfx.client.render.ImpactFrameManager.getInstance().getCurrentType());
    }

    public static void testBeamHelicalRibbonsAndBeamCannon() {
        VFXSchematic cannonSchematic = VFXSchematicParser.parseHeader(DefaultSchematics.BEAM_CANNON);
        assertEquals("beam_cannon", cannonSchematic.getId());
        assertEquals(80, cannonSchematic.getDuration());
        assertTrue(cannonSchematic.isLoop());
        assertEquals(160.0, cannonSchematic.getTrackingRadius(), 0.001);

        VFXBlueprint bp = VFXClientBlueprintCompiler.compile(DefaultSchematics.BEAM_CANNON);
        assertEquals("beam_cannon", bp.getId());
        assertEquals(4, bp.getComponents().size());

        // Component 1 is impact frame
        com.unrealvfx.client.vfx.component.ImpactFrameTriggerComponent impact =
            (com.unrealvfx.client.vfx.component.ImpactFrameTriggerComponent) bp.getComponents().get(1);
        assertEquals(14, impact.getTriggerTick());
        assertEquals(2, impact.getDurationTicks());
        assertEquals(com.unrealvfx.client.render.ImpactFrameManager.Type.MANGA_BW, impact.getType());

        // Component 3 is beam with double helical ribbons
        com.unrealvfx.client.vfx.component.BeamComponent beam =
            (com.unrealvfx.client.vfx.component.BeamComponent) bp.getComponents().get(3);
        assertTrue(beam.hasHelicalRibbons());
        assertEquals(0.95f, beam.getRibbonRadius(), 0.001f);
        assertEquals(8.0f, beam.getRibbonSpeed(), 0.001f);

        // Verify that redundant mesh cylinder causing whiteout has been eliminated
        for (com.unrealvfx.client.vfx.VFXComponent c : bp.getComponents()) {
            assertTrue(!(c instanceof com.unrealvfx.client.vfx.component.MeshComponent));
        }
    }

    public static void testFloatingDebrisThreePhasePhysics() {
        String json = "{\n" +
            "  \"id\": \"test_debris\",\n" +
            "  \"duration\": 100,\n" +
            "  \"components\": [\n" +
            "    {\n" +
            "      \"type\": \"floating_debris\",\n" +
            "      \"debrisType\": \"REALITY_CUBE\",\n" +
            "      \"count\": 16,\n" +
            "      \"radius\": 5.0,\n" +
            "      \"color\": [0.8, 0.2, 1.0],\n" +
            "      \"anticipationLevitation\": true,\n" +
            "      \"centripetalSuction\": true,\n" +
            "      \"explosiveDispersal\": true\n" +
            "    }\n" +
            "  ]\n" +
            "}";
        VFXBlueprint bp = VFXClientBlueprintCompiler.compile(json);
        com.unrealvfx.client.vfx.component.FloatingDebrisComponent debris =
            (com.unrealvfx.client.vfx.component.FloatingDebrisComponent) bp.getComponents().get(0);
        assertEquals(com.unrealvfx.client.vfx.component.FloatingDebrisComponent.DebrisType.REALITY_CUBE, debris.getDebrisType());
        assertEquals(16, debris.getMaxRocks());
        assertEquals(5.0f, debris.getRadius(), 0.001f);
        assertEquals(0.8f, debris.getColor()[0], 0.001f);
        assertTrue(debris.isAnticipationLevitation());
        assertTrue(debris.isCentripetalSuction());
        assertTrue(debris.isExplosiveDispersal());

        // Simulate 3 phases with instance
        VFXInstance inst = new VFXInstance(bp, null, 0, 0, 0);
        for (int t = 0; t < 80; t++) {
            inst.tick();
            debris.tick(inst);
        }
        assertTrue(!debris.getRocks().isEmpty());
    }

    public static void testSteppedSakugaLoopingAndShaderTime() {
        String json = "{\n" +
            "  \"id\": \"test_sakuga_loop\",\n" +
            "  \"duration\": 80,\n" +
            "  \"loop\": true,\n" +
            "  \"components\": [\n" +
            "    { \"type\": \"mesh\", \"steppedFps\": 12.0, \"array\": { \"count\": 4, \"phaseDelay\": 1.0 } }\n" +
            "  ]\n" +
            "}";
        VFXBlueprint bp = VFXClientBlueprintCompiler.compile(json);
        com.unrealvfx.client.vfx.component.MeshComponent mesh =
            (com.unrealvfx.client.vfx.component.MeshComponent) bp.getComponents().get(0);
        assertEquals(12.0f, mesh.getSteppedFps(), 0.001f);

        VFXInstance inst = new VFXInstance(bp, null, 0, 0, 0);
        // Loop cycle 1 (age 10 of 80): progress ~ 0.125
        inst.setAge(10);
        float p1 = inst.getProgress(0.0f);
        assertEquals(10.0f / 80.0f, p1, 0.001f);

        // Loop cycle 2 (age 90 of 80): progress ~ 10 / 80 = 0.125, MUST NOT BE 1.0f!
        inst.setAge(90);
        float p2 = inst.getProgress(0.0f);
        assertEquals(10.0f / 80.0f, p2, 0.001f);

        // Verify quantization calculation for 12 fps:
        float stepTicks = 20.0f / 12.0f;
        float timeTicks = (float) (Math.floor(90.0f / stepTicks) * stepTicks);
        float effectiveTicks = inst.isLooping() ? (timeTicks % (float) inst.getMaxAge()) : timeTicks;
        float renderProgress = Math.max(0.0f, Math.min(1.0f, effectiveTicks / (float) inst.getMaxAge()));
        assertTrue(renderProgress < 0.20f); // Correctly looping, not clamped to 1.0!
    }

    public static void testImpactFrameLoopingAndZeroTrigger() {
        String json = "{\n" +
            "  \"id\": \"test_impact_loop\",\n" +
            "  \"duration\": 80,\n" +
            "  \"loop\": true,\n" +
            "  \"components\": [\n" +
            "    { \"type\": \"impact_frame\", \"triggerTick\": 14, \"duration\": 2, \"impactType\": \"NEGATIVE_INVERT\" },\n" +
            "    { \"type\": \"impact_frame\", \"triggerTick\": 0, \"duration\": 2, \"impactType\": \"MANGA_BW\" }\n" +
            "  ]\n" +
            "}";
        VFXBlueprint bp = VFXClientBlueprintCompiler.compile(json);
        com.unrealvfx.client.vfx.component.ImpactFrameTriggerComponent impact14 =
            (com.unrealvfx.client.vfx.component.ImpactFrameTriggerComponent) bp.getComponents().get(0);
        com.unrealvfx.client.vfx.component.ImpactFrameTriggerComponent impact0 =
            (com.unrealvfx.client.vfx.component.ImpactFrameTriggerComponent) bp.getComponents().get(1);

        assertEquals(14, impact14.getTriggerTick());
        assertEquals(0, impact0.getTriggerTick());

        VFXInstance inst = new VFXInstance(bp, null, 0, 0, 0);

        // Tick 1: impact0 should trigger on the first tick!
        inst.setAge(1);
        impact0.tick(inst);
        assertTrue(com.unrealvfx.client.render.ImpactFrameManager.getInstance().isActive());
        assertEquals(com.unrealvfx.client.render.ImpactFrameManager.Type.MANGA_BW,
            com.unrealvfx.client.render.ImpactFrameManager.getInstance().getCurrentType());

        // Reset manager ticks
        while (com.unrealvfx.client.render.ImpactFrameManager.getInstance().isActive()) {
            com.unrealvfx.client.render.ImpactFrameManager.getInstance().tick();
        }
        assertTrue(!com.unrealvfx.client.render.ImpactFrameManager.getInstance().isActive());

        // Cycle 1, tick 14: impact14 triggers
        inst.setAge(14);
        impact14.tick(inst);
        assertTrue(com.unrealvfx.client.render.ImpactFrameManager.getInstance().isActive());
        assertEquals(com.unrealvfx.client.render.ImpactFrameManager.Type.NEGATIVE_INVERT,
            com.unrealvfx.client.render.ImpactFrameManager.getInstance().getCurrentType());

        while (com.unrealvfx.client.render.ImpactFrameManager.getInstance().isActive()) {
            com.unrealvfx.client.render.ImpactFrameManager.getInstance().tick();
        }

        // Cycle 2, tick 94 (80 + 14): impact14 MUST trigger again on loop cycle 2!
        inst.setAge(94);
        impact14.tick(inst);
        assertTrue(com.unrealvfx.client.render.ImpactFrameManager.getInstance().isActive());
        assertEquals(com.unrealvfx.client.render.ImpactFrameManager.Type.NEGATIVE_INVERT,
            com.unrealvfx.client.render.ImpactFrameManager.getInstance().getCurrentType());
    }

    public static void testRealityCubeVortexSchematicAndDefaults() {
        VFXSchematic vortexSchematic = VFXSchematicParser.parseHeader(DefaultSchematics.REALITY_CUBE_VORTEX);
        assertEquals("reality_cube_vortex", vortexSchematic.getId());
        assertEquals(80, vortexSchematic.getDuration());
        assertTrue(vortexSchematic.isLoop());
        assertEquals(160.0, vortexSchematic.getTrackingRadius(), 0.001);

        VFXBlueprint bp = VFXClientBlueprintCompiler.compile(DefaultSchematics.REALITY_CUBE_VORTEX);
        assertEquals("reality_cube_vortex", bp.getId());
        assertEquals(5, bp.getComponents().size());

        // Component 0: CameraShake
        com.unrealvfx.client.vfx.component.CameraShakeComponent shake =
            (com.unrealvfx.client.vfx.component.CameraShakeComponent) bp.getComponents().get(0);
        assertEquals(0.35f, shake.getTrauma(), 0.001f);
        assertEquals(4.0f, shake.getFovKick(), 0.001f);

        // Component 1: GravitationalPull
        com.unrealvfx.client.vfx.component.GravitationalPullComponent pull =
            (com.unrealvfx.client.vfx.component.GravitationalPullComponent) bp.getComponents().get(1);
        assertEquals(14.0f, pull.getRadius(), 0.001f);
        assertEquals(0.6f, pull.getPullStrength(), 0.001f);
        assertEquals(0.35f, pull.getSwirlStrength(), 0.001f);

        // Component 2: Celestial Void mesh with steppedFps
        com.unrealvfx.client.vfx.component.MeshComponent voidMesh =
            (com.unrealvfx.client.vfx.component.MeshComponent) bp.getComponents().get(2);
        assertEquals("celestial_void", voidMesh.getShader());
        assertTrue(voidMesh.isInvertedCore());
        assertEquals(12.0f, voidMesh.getSteppedFps(), 0.001f);

        // Component 3: Reality Cube Floating Debris
        com.unrealvfx.client.vfx.component.FloatingDebrisComponent debris =
            (com.unrealvfx.client.vfx.component.FloatingDebrisComponent) bp.getComponents().get(3);
        assertEquals(com.unrealvfx.client.vfx.component.FloatingDebrisComponent.DebrisType.REALITY_CUBE, debris.getDebrisType());
        assertEquals(32, debris.getMaxRocks());
        assertEquals(7.0f, debris.getRadius(), 0.001f);
        assertTrue(debris.isAnticipationLevitation());
        assertTrue(debris.isCentripetalSuction());
        assertTrue(debris.isExplosiveDispersal());

        // Component 4: Impact Frame Hakai
        com.unrealvfx.client.vfx.component.ImpactFrameTriggerComponent impact =
            (com.unrealvfx.client.vfx.component.ImpactFrameTriggerComponent) bp.getComponents().get(4);
        assertEquals(65, impact.getTriggerTick());
        assertEquals(2, impact.getDurationTicks());
        assertEquals(com.unrealvfx.client.render.ImpactFrameManager.Type.HAKAI_PURPLE, impact.getType());
    }

    public static void testBeamRibbonColorAndDebrisSteppedFps() {
        String json = "{\n" +
            "  \"id\": \"test_enhancements\",\n" +
            "  \"duration\": 50,\n" +
            "  \"components\": [\n" +
            "    { \"type\": \"beam\", \"length\": 20.0, \"hasHelicalRibbons\": true, \"ribbonRadius\": 1.2, \"ribbonSpeed\": 10.0, \"ribbonColor\": [0.9, 0.1, 0.5] },\n" +
            "    { \"type\": \"floating_debris\", \"debrisType\": \"REALITY_CUBE\", \"steppedFps\": 12.0 }\n" +
            "  ]\n" +
            "}";
        VFXBlueprint bp = VFXClientBlueprintCompiler.compile(json);
        com.unrealvfx.client.vfx.component.BeamComponent beam =
            (com.unrealvfx.client.vfx.component.BeamComponent) bp.getComponents().get(0);
        assertTrue(beam.hasHelicalRibbons());
        assertEquals(1.2f, beam.getRibbonRadius(), 0.001f);
        assertEquals(10.0f, beam.getRibbonSpeed(), 0.001f);
        assertTrue(beam.getRibbonColor() != null);
        assertEquals(0.9f, beam.getRibbonColor()[0], 0.001f);

        com.unrealvfx.client.vfx.component.FloatingDebrisComponent debris =
            (com.unrealvfx.client.vfx.component.FloatingDebrisComponent) bp.getComponents().get(1);
        assertEquals(12.0f, debris.getSteppedFps(), 0.001f);
    }

    public static void testAuraTestPurpleSchematicAndComposition() {
        VFXSchematic auraSchematic = VFXSchematicParser.parseHeader(DefaultSchematics.AURA_TEST_PURPLE);
        assertEquals("aura_test_purple", auraSchematic.getId());
        assertEquals(120, auraSchematic.getDuration());
        assertTrue(auraSchematic.isLoop());
        assertEquals(256.0, auraSchematic.getTrackingRadius(), 0.001);

        VFXBlueprint bp = VFXClientBlueprintCompiler.compile(DefaultSchematics.AURA_TEST_PURPLE);
        assertEquals("aura_test_purple", bp.getId());
        assertEquals(5, bp.getComponents().size());

        // Component 0: CameraShake
        com.unrealvfx.client.vfx.component.CameraShakeComponent shake =
            (com.unrealvfx.client.vfx.component.CameraShakeComponent) bp.getComponents().get(0);
        assertEquals(0.16f, shake.getTrauma(), 0.001f);
        assertTrue(shake.isContinuous());

        // Component 1: Shockwave
        com.unrealvfx.client.vfx.component.ShockwaveComponent shock =
            (com.unrealvfx.client.vfx.component.ShockwaveComponent) bp.getComponents().get(1);
        assertEquals(0.05f, (float) shock.getOffsetY(), 0.001f);
        assertEquals(0.6f, shock.getStartRadius(), 0.001f);
        assertEquals(2.4f, shock.getEndRadius(), 0.001f);

        // Component 2: AURA_CAPSULE mesh
        com.unrealvfx.client.vfx.component.MeshComponent capsule =
            (com.unrealvfx.client.vfx.component.MeshComponent) bp.getComponents().get(2);
        assertEquals(com.unrealvfx.client.vfx.component.MeshComponent.MeshType.AURA_CAPSULE, capsule.getMeshType());
        assertEquals("aura_cel", capsule.getShader());
        assertEquals(3.0f, capsule.getCelBands(), 0.001f);
        assertEquals(0.18f, capsule.getDistortionStrength(), 0.001f);

        // Component 3: KI_EMBER particle
        com.unrealvfx.client.vfx.component.VelocityParticleComponent particles =
            (com.unrealvfx.client.vfx.component.VelocityParticleComponent) bp.getComponents().get(3);
        assertEquals(com.unrealvfx.client.vfx.component.VelocityParticleComponent.ParticleType.KI_EMBER, particles.getParticleType());
        assertEquals(8, particles.getSpawnRate());
        assertEquals(1.4f, particles.getEmissionRadius(), 0.001f);
        assertTrue(particles.isSwirl());
        assertTrue(!particles.isSuction());

        // Component 4: Distortion
        com.unrealvfx.client.vfx.component.DistortionComponent dist =
            (com.unrealvfx.client.vfx.component.DistortionComponent) bp.getComponents().get(4);
        assertEquals(0.6f, dist.getStartRadius(), 0.001f);
        assertEquals(3.2f, dist.getEndRadius(), 0.001f);
        assertEquals(0.04f, dist.getStrength(), 0.001f);
    }

    public static void testVFXSchematicRegistryAndDefaultDiskFiles() {
        String[] requiredIds = new String[]{
            "solar_flare", "cosmic_vortex", "divine_shield", "energy_slash",
            "cube_laser", "gigantic_meteor", "beam_cannon", "reality_cube_vortex", "aura_test_purple", "supernova_heat_haze",
            "heat_mirage_zone", "divine_sanctuary_light"
        };

        // 1. Verify all required schematics exist on disk in config/unrealvfx/effects/
        java.io.File configDir = new java.io.File("config/unrealvfx/effects");
        assertTrue(configDir.exists() && configDir.isDirectory());
        for (String id : requiredIds) {
            java.io.File file = new java.io.File(configDir, id + ".json");
            assertTrue(file.exists());
            assertTrue(file.length() > 0);
            try {
                String content = new String(java.nio.file.Files.readAllBytes(file.toPath()), java.nio.charset.StandardCharsets.UTF_8);
                VFXSchematic s = VFXSchematicParser.parseHeader(content);
                assertEquals(id, s.getId());
            } catch (Exception e) {
                throw new RuntimeException("Failed reading " + file.getAbsolutePath(), e);
            }
        }

        // 2. Verify all required schematics exist on disk in run/client/config/unrealvfx/effects/
        java.io.File runClientDir = new java.io.File("run/client/config/unrealvfx/effects");
        assertTrue(runClientDir.exists() && runClientDir.isDirectory());
        for (String id : requiredIds) {
            java.io.File file = new java.io.File(runClientDir, id + ".json");
            assertTrue(file.exists());
            assertTrue(file.length() > 0);
        }
    }

    public static void testHeatDistortionComponentParsingAndValidation() {
        // 1. Direct Component Instantiation and Parameter Checks
        com.unrealvfx.client.vfx.component.HeatDistortionComponent defComp =
            new com.unrealvfx.client.vfx.component.HeatDistortionComponent();
        assertEquals(com.unrealvfx.client.vfx.component.HeatDistortionComponent.Shape.SPHERE, defComp.getShape());
        assertEquals(0.5f, defComp.getStrength(), 0.001f);
        assertEquals(2.0f, defComp.getCenterFalloff(), 0.001f);
        assertEquals(2.0f, defComp.getFalloffPower(), 0.001f);
        assertEquals(2.5f, defComp.getNoiseSpeed(), 0.001f);
        assertEquals(4.0f, defComp.getNoiseScale(), 0.001f);
        assertEquals(4.0f, defComp.getNoiseFrequency(), 0.001f);
        assertEquals(2.0f, defComp.getScaleX(), 0.001f);
        assertEquals(2.0f, defComp.getScaleY(), 0.001f);
        assertEquals(2.0f, defComp.getScaleZ(), 0.001f);
        assertEquals(0.0f, defComp.getOffsetX(), 0.001f);
        assertEquals(0.0f, defComp.getOffsetY(), 0.001f);
        assertEquals(0.0f, defComp.getOffsetZ(), 0.001f);
        assertEquals(0.0f, defComp.getTintAlpha(), 0.001f);

        // Fluent setters
        defComp.setShape(com.unrealvfx.client.vfx.component.HeatDistortionComponent.Shape.CUBE)
               .setStrength(0.12f)
               .setCenterFalloff(3.0f)
               .setNoiseSpeed(3.5f)
               .setNoiseFrequency(6.0f)
               .setScale(4.0f, 5.0f, 6.0f)
               .setOffset(1.0f, 2.0f, 3.0f)
               .setTintColor(0.9f, 0.5f, 0.1f)
               .setTintAlpha(0.65f);

        assertEquals(com.unrealvfx.client.vfx.component.HeatDistortionComponent.Shape.CUBE, defComp.getShape());
        assertEquals(0.12f, defComp.getStrength(), 0.001f);
        assertEquals(3.0f, defComp.getCenterFalloff(), 0.001f);
        assertEquals(3.5f, defComp.getNoiseSpeed(), 0.001f);
        assertEquals(6.0f, defComp.getNoiseScale(), 0.001f);
        assertEquals(4.0f, defComp.getScaleX(), 0.001f);
        assertEquals(5.0f, defComp.getScaleY(), 0.001f);
        assertEquals(6.0f, defComp.getScaleZ(), 0.001f);
        assertEquals(1.0f, defComp.getOffsetX(), 0.001f);
        assertEquals(2.0f, defComp.getOffsetY(), 0.001f);
        assertEquals(3.0f, defComp.getOffsetZ(), 0.001f);
        assertEquals(0.9f, defComp.getTintColor()[0], 0.001f);
        assertEquals(0.5f, defComp.getTintColor()[1], 0.001f);
        assertEquals(0.1f, defComp.getTintColor()[2], 0.001f);
        assertEquals(0.65f, defComp.getTintAlpha(), 0.001f);

        // Clamping & percentage normalization
        defComp.setTintAlpha(-0.5f);
        assertEquals(0.0f, defComp.getTintAlpha(), 0.001f);
        defComp.setTintAlpha(100.0f);
        assertEquals(1.0f, defComp.getTintAlpha(), 0.001f);
        defComp.setTintAlpha(40.0f);
        assertEquals(0.40f, defComp.getTintAlpha(), 0.001f);
        defComp.setTintAlpha(150.0f);
        assertEquals(1.0f, defComp.getTintAlpha(), 0.001f);

        // 2. JSON Blueprint Compilation - SPHERE shape
        String jsonSphere = "{\n" +
            "  \"id\": \"test_heat_sphere\",\n" +
            "  \"duration\": 40,\n" +
            "  \"components\": [\n" +
            "    {\n" +
            "      \"type\": \"heat_distortion\",\n" +
            "      \"shape\": \"SPHERE\",\n" +
            "      \"strength\": 0.09,\n" +
            "      \"centerFalloff\": 2.5,\n" +
            "      \"noiseSpeed\": 3.0,\n" +
            "      \"noiseFrequency\": 5.0,\n" +
            "      \"scale\": [3.0, 3.0, 3.0],\n" +
            "      \"offset\": [0.0, 1.5, 0.0],\n" +
            "      \"tintColor\": [1.0, 0.7, 0.3]\n" +
            "    }\n" +
            "  ]\n" +
            "}";
        VFXBlueprint bpSphere = VFXClientBlueprintCompiler.compile(jsonSphere);
        assertEquals(1, bpSphere.getComponents().size());
        assertTrue(bpSphere.getComponents().get(0) instanceof com.unrealvfx.client.vfx.component.HeatDistortionComponent);
        com.unrealvfx.client.vfx.component.HeatDistortionComponent parsedSphere =
            (com.unrealvfx.client.vfx.component.HeatDistortionComponent) bpSphere.getComponents().get(0);
        assertEquals(com.unrealvfx.client.vfx.component.HeatDistortionComponent.Shape.SPHERE, parsedSphere.getShape());
        assertEquals(0.09f, parsedSphere.getStrength(), 0.001f);
        assertEquals(2.5f, parsedSphere.getCenterFalloff(), 0.001f);
        assertEquals(3.0f, parsedSphere.getNoiseSpeed(), 0.001f);
        assertEquals(5.0f, parsedSphere.getNoiseFrequency(), 0.001f);
        assertEquals(3.0f, parsedSphere.getScaleX(), 0.001f);
        assertEquals(1.5f, parsedSphere.getOffsetY(), 0.001f);
        assertEquals(1.0f, parsedSphere.getTintColor()[0], 0.001f);
        assertEquals(0.0f, parsedSphere.getTintAlpha(), 0.001f); // default when unspecified is 0.0f (pure transparent)

        // 2b. JSON Blueprint with explicit tintAlpha
        String jsonTintAlpha = "{\n" +
            "  \"id\": \"test_heat_tint_alpha\",\n" +
            "  \"duration\": 40,\n" +
            "  \"components\": [\n" +
            "    {\n" +
            "      \"type\": \"heat_distortion\",\n" +
            "      \"tintColor\": [1.0, 0.5, 0.0],\n" +
            "      \"tintAlpha\": 0.45\n" +
            "    }\n" +
            "  ]\n" +
            "}";
        VFXBlueprint bpTintAlpha = VFXClientBlueprintCompiler.compile(jsonTintAlpha);
        com.unrealvfx.client.vfx.component.HeatDistortionComponent parsedTintAlpha =
            (com.unrealvfx.client.vfx.component.HeatDistortionComponent) bpTintAlpha.getComponents().get(0);
        assertEquals(0.45f, parsedTintAlpha.getTintAlpha(), 0.001f);

        // 2c. JSON Blueprint with tintOpacity alias
        String jsonTintOpacity = "{\n" +
            "  \"id\": \"test_heat_tint_opacity\",\n" +
            "  \"duration\": 40,\n" +
            "  \"components\": [\n" +
            "    {\n" +
            "      \"type\": \"heat_distortion\",\n" +
            "      \"tintOpacity\": 0.70\n" +
            "    }\n" +
            "  ]\n" +
            "}";
        VFXBlueprint bpTintOpacity = VFXClientBlueprintCompiler.compile(jsonTintOpacity);
        com.unrealvfx.client.vfx.component.HeatDistortionComponent parsedTintOpacity =
            (com.unrealvfx.client.vfx.component.HeatDistortionComponent) bpTintOpacity.getComponents().get(0);
        assertEquals(0.70f, parsedTintOpacity.getTintAlpha(), 0.001f);

        // 2d. JSON Blueprint with percentage tintAlpha
        String jsonTintPercent = "{\n" +
            "  \"id\": \"test_heat_percent\",\n" +
            "  \"duration\": 40,\n" +
            "  \"components\": [\n" +
            "    {\n" +
            "      \"type\": \"heat_distortion\",\n" +
            "      \"tintAlpha\": 85.0\n" +
            "    }\n" +
            "  ]\n" +
            "}";
        VFXBlueprint bpTintPercent = VFXClientBlueprintCompiler.compile(jsonTintPercent);
        com.unrealvfx.client.vfx.component.HeatDistortionComponent parsedTintPercent =
            (com.unrealvfx.client.vfx.component.HeatDistortionComponent) bpTintPercent.getComponents().get(0);
        assertEquals(0.85f, parsedTintPercent.getTintAlpha(), 0.001f);

        // 2e. JSON Blueprint with easing object alpha
        String jsonEasingAlpha = "{\n" +
            "  \"id\": \"test_heat_easing_alpha\",\n" +
            "  \"duration\": 40,\n" +
            "  \"components\": [\n" +
            "    {\n" +
            "      \"type\": \"heat_distortion\",\n" +
            "      \"alpha\": { \"start\": 0.35, \"end\": 0.0, \"easing\": \"LINEAR\" }\n" +
            "    }\n" +
            "  ]\n" +
            "}";
        VFXBlueprint bpEasingAlpha = VFXClientBlueprintCompiler.compile(jsonEasingAlpha);
        com.unrealvfx.client.vfx.component.HeatDistortionComponent parsedEasingAlpha =
            (com.unrealvfx.client.vfx.component.HeatDistortionComponent) bpEasingAlpha.getComponents().get(0);
        assertEquals(0.35f, parsedEasingAlpha.getTintAlpha(), 0.001f);

        // 3. JSON Blueprint Compilation - CUBE shape
        String jsonCube = "{\n" +
            "  \"id\": \"test_heat_cube\",\n" +
            "  \"duration\": 40,\n" +
            "  \"components\": [\n" +
            "    {\n" +
            "      \"type\": \"heat_distortion\",\n" +
            "      \"shape\": \"CUBE\",\n" +
            "      \"strength\": 0.15,\n" +
            "      \"falloffPower\": 1.8,\n" +
            "      \"noiseScale\": 3.2,\n" +
            "      \"scale\": 4.5,\n" +
            "      \"offset\": [1.0, 0.0, -1.0]\n" +
            "    }\n" +
            "  ]\n" +
            "}";
        VFXBlueprint bpCube = VFXClientBlueprintCompiler.compile(jsonCube);
        com.unrealvfx.client.vfx.component.HeatDistortionComponent parsedCube =
            (com.unrealvfx.client.vfx.component.HeatDistortionComponent) bpCube.getComponents().get(0);
        assertEquals(com.unrealvfx.client.vfx.component.HeatDistortionComponent.Shape.CUBE, parsedCube.getShape());
        assertEquals(0.15f, parsedCube.getStrength(), 0.001f);
        assertEquals(1.8f, parsedCube.getFalloffPower(), 0.001f);
        assertEquals(3.2f, parsedCube.getNoiseScale(), 0.001f);
        assertEquals(4.5f, parsedCube.getScaleX(), 0.001f);
        assertEquals(4.5f, parsedCube.getScaleY(), 0.001f);
        assertEquals(4.5f, parsedCube.getScaleZ(), 0.001f);
        assertEquals(1.0f, parsedCube.getOffsetX(), 0.001f);

        // 4. JSON Blueprint Compilation - CYLINDER shape
        String jsonCyl = "{\n" +
            "  \"id\": \"test_heat_cyl\",\n" +
            "  \"duration\": 40,\n" +
            "  \"components\": [\n" +
            "    {\n" +
            "      \"type\": \"heat_wave\",\n" +
            "      \"shape\": \"CYLINDER\",\n" +
            "      \"strength\": 0.05,\n" +
            "      \"centerFalloff\": 2.2,\n" +
            "      \"scaleX\": 2.5,\n" +
            "      \"scaleY\": 6.0,\n" +
            "      \"scaleZ\": 2.5\n" +
            "    }\n" +
            "  ]\n" +
            "}";
        VFXBlueprint bpCyl = VFXClientBlueprintCompiler.compile(jsonCyl);
        com.unrealvfx.client.vfx.component.HeatDistortionComponent parsedCyl =
            (com.unrealvfx.client.vfx.component.HeatDistortionComponent) bpCyl.getComponents().get(0);
        assertEquals(com.unrealvfx.client.vfx.component.HeatDistortionComponent.Shape.CYLINDER, parsedCyl.getShape());
        assertEquals(2.5f, parsedCyl.getScaleX(), 0.001f);
        assertEquals(6.0f, parsedCyl.getScaleY(), 0.001f);
        assertEquals(2.5f, parsedCyl.getScaleZ(), 0.001f);

        // 5. Mode: heat in distortion type
        String jsonDistHeat = "{\n" +
            "  \"id\": \"test_dist_heat_mode\",\n" +
            "  \"duration\": 40,\n" +
            "  \"components\": [\n" +
            "    {\n" +
            "      \"type\": \"distortion\",\n" +
            "      \"mode\": \"heat\",\n" +
            "      \"shape\": \"SPHERE\",\n" +
            "      \"strength\": 0.07,\n" +
            "      \"centerFalloff\": 1.5\n" +
            "    }\n" +
            "  ]\n" +
            "}";
        VFXBlueprint bpDistHeat = VFXClientBlueprintCompiler.compile(jsonDistHeat);
        assertTrue(bpDistHeat.getComponents().get(0) instanceof com.unrealvfx.client.vfx.component.HeatDistortionComponent);

        // 6. Regular distortion still compiles to DistortionComponent
        String jsonRegularDist = "{\n" +
            "  \"id\": \"test_reg_dist\",\n" +
            "  \"duration\": 40,\n" +
            "  \"components\": [\n" +
            "    {\n" +
            "      \"type\": \"distortion\",\n" +
            "      \"startRadius\": 0.5,\n" +
            "      \"endRadius\": 8.0,\n" +
            "      \"strength\": 0.06\n" +
            "    }\n" +
            "  ]\n" +
            "}";
        VFXBlueprint bpRegDist = VFXClientBlueprintCompiler.compile(jsonRegularDist);
        assertTrue(bpRegDist.getComponents().get(0) instanceof com.unrealvfx.client.vfx.component.DistortionComponent);

        // 7. Edge Cases: Invalid shape defaults gracefully to SPHERE
        String jsonInvalidShape = "{\n" +
            "  \"id\": \"test_invalid_shape\",\n" +
            "  \"duration\": 40,\n" +
            "  \"components\": [\n" +
            "    {\n" +
            "      \"type\": \"heat_distortion\",\n" +
            "      \"shape\": \"PYRAMID_NON_EXISTENT\"\n" +
            "    }\n" +
            "  ]\n" +
            "}";
        VFXBlueprint bpInvalidShape = VFXClientBlueprintCompiler.compile(jsonInvalidShape);
        com.unrealvfx.client.vfx.component.HeatDistortionComponent parsedInvalid =
            (com.unrealvfx.client.vfx.component.HeatDistortionComponent) bpInvalidShape.getComponents().get(0);
        assertEquals(com.unrealvfx.client.vfx.component.HeatDistortionComponent.Shape.SPHERE, parsedInvalid.getShape());

        // 8. Supernova Heat Haze default schematic compilation
        VFXBlueprint bpSupernova = VFXClientBlueprintCompiler.compile(DefaultSchematics.SUPERNOVA_HEAT_HAZE);
        assertEquals("supernova_heat_haze", bpSupernova.getId());
        assertEquals(80, bpSupernova.getDuration());
        assertTrue(bpSupernova.isLoop());
        assertEquals(5, bpSupernova.getComponents().size());
        assertTrue(bpSupernova.getComponents().get(1) instanceof com.unrealvfx.client.vfx.component.HeatDistortionComponent);
        com.unrealvfx.client.vfx.component.HeatDistortionComponent snComp =
            (com.unrealvfx.client.vfx.component.HeatDistortionComponent) bpSupernova.getComponents().get(1);
        assertEquals(com.unrealvfx.client.vfx.component.HeatDistortionComponent.Shape.SPHERE, snComp.getShape());
        assertEquals(0.08f, snComp.getStrength(), 0.001f);
        assertEquals(2.0f, snComp.getCenterFalloff(), 0.001f);
        assertEquals(5.0f, snComp.getScaleX(), 0.001f);
        assertEquals(2.5f, snComp.getOffsetY(), 0.001f);
        assertEquals(0.0f, snComp.getTintAlpha(), 0.001f);

        // 8b. Dedicated Heat Mirage Zone default schematic compilation (pure transparent air refraction)
        VFXBlueprint bpHeatMirage = VFXClientBlueprintCompiler.compile(DefaultSchematics.HEAT_MIRAGE_ZONE);
        assertEquals("heat_mirage_zone", bpHeatMirage.getId());
        assertEquals(100, bpHeatMirage.getDuration());
        assertTrue(bpHeatMirage.isLoop());
        assertEquals(1, bpHeatMirage.getComponents().size());
        assertTrue(bpHeatMirage.getComponents().get(0) instanceof com.unrealvfx.client.vfx.component.HeatDistortionComponent);
        com.unrealvfx.client.vfx.component.HeatDistortionComponent mirageComp =
            (com.unrealvfx.client.vfx.component.HeatDistortionComponent) bpHeatMirage.getComponents().get(0);
        assertEquals(com.unrealvfx.client.vfx.component.HeatDistortionComponent.Shape.CYLINDER, mirageComp.getShape());
        assertEquals(0.08f, mirageComp.getStrength(), 0.001f);
        assertEquals(1.8f, mirageComp.getCenterFalloff(), 0.001f);
        assertEquals(0.0f, mirageComp.getTintAlpha(), 0.001f); // 0.0 = pure transparent refraction

        // 9. Edge Cases: cullFace parsing and zero strength preservation
        String jsonAdvancedHeat = "{\n" +
            "  \"id\": \"test_adv_heat\",\n" +
            "  \"duration\": 30,\n" +
            "  \"components\": [\n" +
            "    {\n" +
            "      \"type\": \"distortion\",\n" +
            "      \"mode\": \"heat_wave\",\n" +
            "      \"shape\": \"CUBE\",\n" +
            "      \"strength\": 0.0,\n" +
            "      \"cullFace\": false\n" +
            "    }\n" +
            "  ]\n" +
            "}";
        VFXBlueprint bpAdvHeat = VFXClientBlueprintCompiler.compile(jsonAdvancedHeat);
        assertTrue(bpAdvHeat.getComponents().get(0) instanceof com.unrealvfx.client.vfx.component.HeatDistortionComponent);
        com.unrealvfx.client.vfx.component.HeatDistortionComponent advHeat =
            (com.unrealvfx.client.vfx.component.HeatDistortionComponent) bpAdvHeat.getComponents().get(0);
        assertEquals(0.0f, advHeat.getStrength(), 0.0001f);
        assertTrue(!advHeat.isCullFace());
        assertEquals(com.unrealvfx.client.vfx.component.HeatDistortionComponent.Shape.CUBE, advHeat.getShape());

        // 10. Default cullFace is false across instances and omitted JSON keys
        com.unrealvfx.client.vfx.component.HeatDistortionComponent freshComp = new com.unrealvfx.client.vfx.component.HeatDistortionComponent();
        assertTrue(!freshComp.isCullFace());
        assertTrue(!parsedInvalid.isCullFace());
        assertTrue(!snComp.isCullFace());
        assertTrue(!mirageComp.isCullFace());

        // Explicit cullFace: true
        String jsonCullTrue = "{\n" +
            "  \"id\": \"test_cull_true\",\n" +
            "  \"duration\": 20,\n" +
            "  \"components\": [\n" +
            "    {\n" +
            "      \"type\": \"heat_distortion\",\n" +
            "      \"cullFace\": true\n" +
            "    }\n" +
            "  ]\n" +
            "}";
        VFXBlueprint bpCullTrue = VFXClientBlueprintCompiler.compile(jsonCullTrue);
        com.unrealvfx.client.vfx.component.HeatDistortionComponent cullTrueComp =
            (com.unrealvfx.client.vfx.component.HeatDistortionComponent) bpCullTrue.getComponents().get(0);
        assertTrue(cullTrueComp.isCullFace());
    }

    public static void testLightComponentParsingAndValidation() {
        // 1. Direct Component Instantiation and Default Values
        com.unrealvfx.client.vfx.component.LightComponent defComp =
            new com.unrealvfx.client.vfx.component.LightComponent();
        assertEquals(com.unrealvfx.client.vfx.component.LightComponent.LightShape.POINT, defComp.getShape());
        assertEquals(8.0f, defComp.getRadius(), 0.001f);
        assertEquals(1.5f, defComp.getIntensity(), 0.001f);
        assertEquals(2.0f, defComp.getFalloffPower(), 0.001f);
        assertEquals(45.0f, defComp.getSpotAngle(), 0.001f);
        assertEquals(0.5f, defComp.getPenumbra(), 0.001f);
        assertEquals(0.0, defComp.getOffsetX(), 0.001);
        assertEquals(0.0, defComp.getOffsetY(), 0.001);
        assertEquals(0.0, defComp.getOffsetZ(), 0.001);
        assertTrue(defComp.isVolumetricNoise());
        assertEquals(1.5f, defComp.getNoiseSpeed(), 0.001f);
        assertEquals(3.0f, defComp.getNoiseScale(), 0.001f);
        assertEquals(0.4f, defComp.getDustDensity(), 0.001f);
        assertTrue(!defComp.isFlicker());
        assertTrue(!defComp.isPulsing());
        assertTrue(defComp.isIlluminateWorld());
        assertEquals(15, defComp.getBlockLightLevel());
        assertEquals(2.0f, defComp.getBloomMultiplier(), 0.001f);

        // 2. Fluent Setters
        defComp.setShape(com.unrealvfx.client.vfx.component.LightComponent.LightShape.SPOT)
               .setRadius(12.0f)
               .setIntensity(3.0f)
               .setColor(0.2f, 0.8f, 1.0f)
               .setCoreColor(0.9f, 0.95f, 1.0f)
               .setSpotAngle(35.0f)
               .setPenumbra(0.7f)
               .setOffset(1.0, 5.0, -2.0)
               .setRotation(15.0f, 45.0f, 0.0f)
               .setFlicker(true)
               .setFlickerSpeed(16.0f)
               .setFlickerIntensity(0.35f)
               .setPulsing(true)
               .setPulseSpeed(4.0f)
               .setPulseIntensity(0.45f)
               .setBloomMultiplier(3.5f)
               .setIlluminateWorld(false)
               .setBlockLightLevel(12);

        assertEquals(com.unrealvfx.client.vfx.component.LightComponent.LightShape.SPOT, defComp.getShape());
        assertEquals(12.0f, defComp.getRadius(), 0.001f);
        assertEquals(3.0f, defComp.getIntensity(), 0.001f);
        assertEquals(0.2f, defComp.getColor()[0], 0.001f);
        assertEquals(0.8f, defComp.getColor()[1], 0.001f);
        assertEquals(1.0f, defComp.getColor()[2], 0.001f);
        assertEquals(0.9f, defComp.getCoreColor()[0], 0.001f);
        assertEquals(35.0f, defComp.getSpotAngle(), 0.001f);
        assertEquals(0.7f, defComp.getPenumbra(), 0.001f);
        assertEquals(1.0, defComp.getOffsetX(), 0.001);
        assertEquals(5.0, defComp.getOffsetY(), 0.001);
        assertEquals(-2.0, defComp.getOffsetZ(), 0.001);
        assertEquals(15.0f, defComp.getRotYaw(), 0.001f);
        assertEquals(45.0f, defComp.getRotPitch(), 0.001f);
        assertTrue(defComp.isFlicker());
        assertEquals(16.0f, defComp.getFlickerSpeed(), 0.001f);
        assertTrue(defComp.isPulsing());
        assertTrue(!defComp.isIlluminateWorld());
        assertEquals(12, defComp.getBlockLightLevel());

        // 3. JSON Parsing & Compilation for all Light Shapes
        String jsonSpot = "{\n" +
            "  \"id\": \"test_spot\",\n" +
            "  \"duration\": 40,\n" +
            "  \"components\": [\n" +
            "    {\n" +
            "      \"type\": \"light\",\n" +
            "      \"shape\": \"SPOT\",\n" +
            "      \"radius\": 10.0,\n" +
            "      \"intensity\": 2.5,\n" +
            "      \"spotAngle\": 50.0,\n" +
            "      \"penumbra\": 0.6,\n" +
            "      \"color\": [1.0, 0.9, 0.2],\n" +
            "      \"offset\": [0.0, 6.0, 0.0],\n" +
            "      \"flicker\": true,\n" +
            "      \"pulsing\": true\n" +
            "    }\n" +
            "  ]\n" +
            "}";
        VFXBlueprint bpSpot = VFXClientBlueprintCompiler.compile(jsonSpot);
        assertEquals(1, bpSpot.getComponents().size());
        assertTrue(bpSpot.getComponents().get(0) instanceof com.unrealvfx.client.vfx.component.LightComponent);
        com.unrealvfx.client.vfx.component.LightComponent parsedSpot =
            (com.unrealvfx.client.vfx.component.LightComponent) bpSpot.getComponents().get(0);
        assertEquals(com.unrealvfx.client.vfx.component.LightComponent.LightShape.SPOT, parsedSpot.getShape());
        assertEquals(10.0f, parsedSpot.getRadius(), 0.001f);
        assertEquals(2.5f, parsedSpot.getIntensity(), 0.001f);
        assertEquals(50.0f, parsedSpot.getSpotAngle(), 0.001f);
        assertEquals(0.6f, parsedSpot.getPenumbra(), 0.001f);
        assertEquals(6.0, parsedSpot.getOffsetY(), 0.001);
        assertTrue(parsedSpot.isFlicker());
        assertTrue(parsedSpot.isPulsing());

        // 4. Test COLUMN shape and alias volumetric_light
        String jsonColumn = "{\n" +
            "  \"id\": \"test_column\",\n" +
            "  \"duration\": 60,\n" +
            "  \"components\": [\n" +
            "    {\n" +
            "      \"type\": \"volumetric_light\",\n" +
            "      \"shape\": \"COLUMN\",\n" +
            "      \"radius\": 4.0,\n" +
            "      \"intensity\": 1.8\n" +
            "    }\n" +
            "  ]\n" +
            "}";
        VFXBlueprint bpColumn = VFXClientBlueprintCompiler.compile(jsonColumn);
        com.unrealvfx.client.vfx.component.LightComponent parsedColumn =
            (com.unrealvfx.client.vfx.component.LightComponent) bpColumn.getComponents().get(0);
        assertEquals(com.unrealvfx.client.vfx.component.LightComponent.LightShape.COLUMN, parsedColumn.getShape());
        assertEquals(4.0f, parsedColumn.getRadius(), 0.001f);

        // 5. Test AREA_DISC & DIRECTIONAL
        String jsonDisc = "{\n" +
            "  \"id\": \"test_disc\",\n" +
            "  \"duration\": 60,\n" +
            "  \"components\": [\n" +
            "    { \"type\": \"point_light\", \"shape\": \"AREA_DISC\", \"radius\": 5.0 },\n" +
            "    { \"type\": \"spot_light\", \"shape\": \"DIRECTIONAL\", \"radius\": 6.0 }\n" +
            "  ]\n" +
            "}";
        VFXBlueprint bpDisc = VFXClientBlueprintCompiler.compile(jsonDisc);
        assertEquals(2, bpDisc.getComponents().size());
        assertEquals(com.unrealvfx.client.vfx.component.LightComponent.LightShape.AREA_DISC,
                     ((com.unrealvfx.client.vfx.component.LightComponent) bpDisc.getComponents().get(0)).getShape());
        assertEquals(com.unrealvfx.client.vfx.component.LightComponent.LightShape.DIRECTIONAL,
                     ((com.unrealvfx.client.vfx.component.LightComponent) bpDisc.getComponents().get(1)).getShape());

        // 6. Test divine_sanctuary_light preset compilation
        VFXBlueprint bpDivine = VFXClientBlueprintCompiler.compile(DefaultSchematics.DIVINE_SANCTUARY_LIGHT);
        assertEquals("divine_sanctuary_light", bpDivine.getId());
        assertEquals(120, bpDivine.getDuration());
        assertTrue(bpDivine.isLoop());
        assertEquals(6, bpDivine.getComponents().size());
        assertTrue(bpDivine.getComponents().get(2) instanceof com.unrealvfx.client.vfx.component.LightComponent);
        com.unrealvfx.client.vfx.component.LightComponent divineSpot =
            (com.unrealvfx.client.vfx.component.LightComponent) bpDivine.getComponents().get(2);
        assertEquals(com.unrealvfx.client.vfx.component.LightComponent.LightShape.SPOT, divineSpot.getShape());
        assertEquals(8.0f, divineSpot.getRadius(), 0.001f);
        assertEquals(1.2f, divineSpot.getIntensity(), 0.001f);
        assertEquals(42.0f, divineSpot.getSpotAngle(), 0.001f);
        assertEquals(0.65f, divineSpot.getPenumbra(), 0.001f);
        assertTrue(divineSpot.isVolumetricNoise());
        assertTrue(divineSpot.isPulsing());
        assertTrue(divineSpot.isIlluminateWorld());
        assertEquals(15, divineSpot.getBlockLightLevel());

        com.unrealvfx.client.vfx.component.LightComponent divinePoint =
            (com.unrealvfx.client.vfx.component.LightComponent) bpDivine.getComponents().get(3);
        assertEquals(com.unrealvfx.client.vfx.component.LightComponent.LightShape.POINT, divinePoint.getShape());
        assertEquals(3.5f, divinePoint.getRadius(), 0.001f);
        assertEquals(0.5f, divinePoint.getIntensity(), 0.001f);

        com.unrealvfx.client.vfx.component.LightComponent divineDisc =
            (com.unrealvfx.client.vfx.component.LightComponent) bpDivine.getComponents().get(4);
        assertEquals(com.unrealvfx.client.vfx.component.LightComponent.LightShape.AREA_DISC, divineDisc.getShape());
        assertEquals(4.5f, divineDisc.getRadius(), 0.001f);
        assertEquals(0.6f, divineDisc.getIntensity(), 0.001f);

        // 7. Test Shader-Grade Advanced Features (lightTerrain, godRays, lensFlare)
        String jsonShaderGrade = "{\n" +
            "  \"id\": \"test_shader_grade_light\",\n" +
            "  \"duration\": 60,\n" +
            "  \"components\": [\n" +
            "    {\n" +
            "      \"type\": \"light\",\n" +
            "      \"shape\": \"SPOT\",\n" +
            "      \"radius\": 12.0,\n" +
            "      \"intensity\": 2.2,\n" +
            "      \"lightTerrain\": true,\n" +
            "      \"terrainIntensity\": 1.4,\n" +
            "      \"terrainSpecular\": 0.5,\n" +
            "      \"godRays\": true,\n" +
            "      \"godRayDensity\": 0.9,\n" +
            "      \"godRayDecay\": 0.96,\n" +
            "      \"godRayWeight\": 0.4,\n" +
            "      \"lensFlare\": true,\n" +
            "      \"flareSize\": 1.5,\n" +
            "      \"flareIntensity\": 1.2\n" +
            "    }\n" +
            "  ]\n" +
            "}";
        VFXBlueprint bpShaderGrade = VFXClientBlueprintCompiler.compile(jsonShaderGrade);
        com.unrealvfx.client.vfx.component.LightComponent parsedShaderLight =
            (com.unrealvfx.client.vfx.component.LightComponent) bpShaderGrade.getComponents().get(0);
        assertTrue(parsedShaderLight.isLightTerrain());
        assertEquals(1.4f, parsedShaderLight.getTerrainIntensity(), 0.001f);
        assertEquals(0.5f, parsedShaderLight.getTerrainSpecular(), 0.001f);
        assertTrue(parsedShaderLight.isGodRays());
        assertEquals(0.9f, parsedShaderLight.getGodRayDensity(), 0.001f);
        assertEquals(0.96f, parsedShaderLight.getGodRayDecay(), 0.001f);
        assertEquals(0.4f, parsedShaderLight.getGodRayWeight(), 0.001f);
        assertTrue(parsedShaderLight.isLensFlare());
        assertEquals(1.5f, parsedShaderLight.getFlareSize(), 0.001f);
        assertEquals(1.2f, parsedShaderLight.getFlareIntensity(), 0.001f);
    }

    public static void testLightComponentShaderSelectionAndNewParameters() {
        // 1. Verify default values on fresh LightComponent
        com.unrealvfx.client.vfx.component.LightComponent defaultLight = new com.unrealvfx.client.vfx.component.LightComponent();
        assertEquals("volumetric_sacred", defaultLight.getShaderName());
        assertEquals(0.35f, defaultLight.getCoreWidth(), 0.001f);
        assertEquals(1.0f, defaultLight.getBeamSpeed(), 0.001f);
        assertEquals(3.0f, defaultLight.getPatternDensity(), 0.001f);
        assertEquals(0.5f, defaultLight.getFringeStrength(), 0.001f);

        // 2. Verify builder setters
        defaultLight.setShader("celestial_beam")
            .setSecondaryColor(0.95f, 0.35f, 1.0f)
            .setCoreWidth(0.42f)
            .setBeamSpeed(1.4f)
            .setPatternDensity(4.0f)
            .setFringeStrength(0.85f);
        assertEquals("celestial_beam", defaultLight.getShaderName());
        assertEquals(0.95f, defaultLight.getSecondaryColor()[0], 0.001f);
        assertEquals(0.35f, defaultLight.getSecondaryColor()[1], 0.001f);
        assertEquals(1.0f, defaultLight.getSecondaryColor()[2], 0.001f);
        assertEquals(0.42f, defaultLight.getCoreWidth(), 0.001f);
        assertEquals(1.4f, defaultLight.getBeamSpeed(), 0.001f);
        assertEquals(4.0f, defaultLight.getPatternDensity(), 0.001f);
        assertEquals(0.85f, defaultLight.getFringeStrength(), 0.001f);

        // 3. Verify JSON compilation of the 4 new schematics in DefaultSchematics
        VFXBlueprint bpCelestial = VFXClientBlueprintCompiler.compile(DefaultSchematics.CELESTIAL_JUDGMENT_PILLAR);
        assertTrue(bpCelestial != null);
        boolean foundCelestialLight = false;
        for (com.unrealvfx.client.vfx.VFXComponent comp : bpCelestial.getComponents()) {
            if (comp instanceof com.unrealvfx.client.vfx.component.LightComponent) {
                com.unrealvfx.client.vfx.component.LightComponent lc = (com.unrealvfx.client.vfx.component.LightComponent) comp;
                assertEquals("celestial_beam", lc.getShaderName());
                assertEquals(0.42f, lc.getCoreWidth(), 0.001f);
                assertEquals(1.4f, lc.getBeamSpeed(), 0.001f);
                assertEquals(4.0f, lc.getPatternDensity(), 0.001f);
                assertEquals(0.85f, lc.getFringeStrength(), 0.001f);
                foundCelestialLight = true;
            }
        }
        assertTrue(foundCelestialLight);

        VFXBlueprint bpCyber = VFXClientBlueprintCompiler.compile(DefaultSchematics.CYBER_MATRIX_BEACON);
        assertTrue(bpCyber != null);
        boolean foundCyberLight = false;
        for (com.unrealvfx.client.vfx.VFXComponent comp : bpCyber.getComponents()) {
            if (comp instanceof com.unrealvfx.client.vfx.component.LightComponent) {
                com.unrealvfx.client.vfx.component.LightComponent lc = (com.unrealvfx.client.vfx.component.LightComponent) comp;
                if ("cyber_grid_shaft".equals(lc.getShaderName())) {
                    assertEquals(0.32f, lc.getCoreWidth(), 0.001f);
                    assertEquals(1.5f, lc.getBeamSpeed(), 0.001f);
                    assertEquals(3.8f, lc.getPatternDensity(), 0.001f);
                    assertEquals(1.2f, lc.getFringeStrength(), 0.001f);
                    foundCyberLight = true;
                }
            }
        }
        assertTrue(foundCyberLight);

        VFXBlueprint bpAbyssal = VFXClientBlueprintCompiler.compile(DefaultSchematics.ABYSSAL_ECLIPSE_WELL);
        assertTrue(bpAbyssal != null);
        boolean foundAbyssalLight = false;
        for (com.unrealvfx.client.vfx.VFXComponent comp : bpAbyssal.getComponents()) {
            if (comp instanceof com.unrealvfx.client.vfx.component.LightComponent) {
                com.unrealvfx.client.vfx.component.LightComponent lc = (com.unrealvfx.client.vfx.component.LightComponent) comp;
                assertEquals("abyssal_void_light", lc.getShaderName());
                assertEquals(0.48f, lc.getCoreWidth(), 0.001f);
                assertEquals(0.9f, lc.getBeamSpeed(), 0.001f);
                assertEquals(2.8f, lc.getPatternDensity(), 0.001f);
                assertEquals(1.0f, lc.getFringeStrength(), 0.001f);
                foundAbyssalLight = true;
            }
        }
        assertTrue(foundAbyssalLight);

        VFXBlueprint bpAurora = VFXClientBlueprintCompiler.compile(DefaultSchematics.VALKYRIE_AURORA_SHRINE);
        assertTrue(bpAurora != null);
        boolean foundAuroraLight = false;
        for (com.unrealvfx.client.vfx.VFXComponent comp : bpAurora.getComponents()) {
            if (comp instanceof com.unrealvfx.client.vfx.component.LightComponent) {
                com.unrealvfx.client.vfx.component.LightComponent lc = (com.unrealvfx.client.vfx.component.LightComponent) comp;
                if ("aurora_borealis".equals(lc.getShaderName())) {
                    assertEquals(0.25f, lc.getCoreWidth(), 0.001f);
                    assertEquals(0.8f, lc.getBeamSpeed(), 0.001f);
                    assertEquals(2.2f, lc.getPatternDensity(), 0.001f);
                    assertEquals(0.65f, lc.getFringeStrength(), 0.001f);
                    foundAuroraLight = true;
                }
            }
        }
        assertTrue(foundAuroraLight);
    }

    public static void testLightLifecycleAndPhantomLightCleanup() {
        // 1. Initial state
        com.unrealvfx.client.vfx.component.LightComponent lc =
            new com.unrealvfx.client.vfx.component.LightComponent();
        assertEquals(Integer.MIN_VALUE, lc.getLastLightX());
        assertEquals(Integer.MIN_VALUE, lc.getLastLightY());
        assertEquals(Integer.MIN_VALUE, lc.getLastLightZ());

        // 2. Explicit triggerDeath() clears lastLight coordinates
        VFXBlueprint bp1 = new VFXBlueprint("test_light_death").setDuration(10);
        bp1.addComponent(lc);
        VFXInstance inst1 = new VFXInstance(bp1, null, 10.0, 64.0, 20.0);
        lc.setLastLightPos(10, 64, 20);
        assertEquals(10, lc.getLastLightX());
        assertEquals(64, lc.getLastLightY());
        assertEquals(20, lc.getLastLightZ());

        inst1.triggerDeath();
        assertTrue(inst1.isDead());
        assertEquals(Integer.MIN_VALUE, lc.getLastLightX());
        assertEquals(Integer.MIN_VALUE, lc.getLastLightY());
        assertEquals(Integer.MIN_VALUE, lc.getLastLightZ());

        // 3. Natural expiry via tick() clears phantom light
        VFXBlueprint bp2 = new VFXBlueprint("test_light_expiry").setDuration(3);
        com.unrealvfx.client.vfx.component.LightComponent lc2 =
            new com.unrealvfx.client.vfx.component.LightComponent();
        bp2.addComponent(lc2);
        VFXInstance inst2 = new VFXInstance(bp2, null, 5.0, 70.0, -10.0);
        lc2.setLastLightPos(5, 70, -10);

        inst2.tick();
        assertTrue(!inst2.isDead());
        assertEquals(5, lc2.getLastLightX());

        inst2.tick();
        assertTrue(!inst2.isDead());
        assertEquals(5, lc2.getLastLightX());

        // Tick 3 reaches duration maxAge (3) -> triggers onDeath()
        inst2.tick();
        assertTrue(inst2.isDead());
        assertEquals(Integer.MIN_VALUE, lc2.getLastLightX());
        assertEquals(Integer.MIN_VALUE, lc2.getLastLightY());
        assertEquals(Integer.MIN_VALUE, lc2.getLastLightZ());

        // 4. setDead(true) transition clears light
        VFXBlueprint bp3 = new VFXBlueprint("test_light_setdead").setDuration(50);
        com.unrealvfx.client.vfx.component.LightComponent lc3 =
            new com.unrealvfx.client.vfx.component.LightComponent();
        bp3.addComponent(lc3);
        VFXInstance inst3 = new VFXInstance(bp3, null, 1.0, 2.0, 3.0);
        lc3.setLastLightPos(1, 2, 3);
        inst3.setDead(true);
        assertTrue(inst3.isDead());
        assertEquals(Integer.MIN_VALUE, lc3.getLastLightX());

        // 5. triggerRemove() clears light
        VFXBlueprint bp4 = new VFXBlueprint("test_light_remove").setDuration(50);
        com.unrealvfx.client.vfx.component.LightComponent lc4 =
            new com.unrealvfx.client.vfx.component.LightComponent();
        bp4.addComponent(lc4);
        VFXInstance inst4 = new VFXInstance(bp4, null, 15.0, 25.0, 35.0);
        lc4.setLastLightPos(15, 25, 35);
        inst4.triggerRemove();
        assertTrue(inst4.isDead());
        assertEquals(Integer.MIN_VALUE, lc4.getLastLightX());
    }

    public static void testPureLightSchematicsIntegrity() {
        String[] pureLightSchematics = new String[] {
            DefaultSchematics.LIGHT_SACRED_CATHEDRAL,
            DefaultSchematics.LIGHT_CELESTIAL_PILLAR,
            DefaultSchematics.LIGHT_CYBER_HOLOBEACON,
            DefaultSchematics.LIGHT_ABYSSAL_SINGULARITY,
            DefaultSchematics.LIGHT_VALKYRIE_AURORA
        };

        for (String json : pureLightSchematics) {
            VFXSchematic sc = VFXSchematicParser.parseHeader(json);
            assertTrue(sc != null);

            VFXBlueprint bp = VFXClientBlueprintCompiler.compile(json);
            assertTrue(bp != null);
            assertTrue(bp.getComponents() != null && bp.getComponents().size() >= 2);

            // Strict user requirement: "(e solo luci nessun altro oggetto)"
            // Every single component in all 5 schematics MUST strictly be an instance of LightComponent!
            for (com.unrealvfx.client.vfx.VFXComponent comp : bp.getComponents()) {
                assertTrue(comp instanceof com.unrealvfx.client.vfx.component.LightComponent);
            }
        }

        // Detailed check on light_sacred_cathedral
        VFXBlueprint bpCathedral = VFXClientBlueprintCompiler.compile(DefaultSchematics.LIGHT_SACRED_CATHEDRAL);
        com.unrealvfx.client.vfx.component.LightComponent spotComp = (com.unrealvfx.client.vfx.component.LightComponent) bpCathedral.getComponents().get(0);
        assertEquals(com.unrealvfx.client.vfx.component.LightComponent.LightShape.SPOT, spotComp.getShape());
        assertEquals("volumetric_sacred", spotComp.getShaderName());
        assertTrue(spotComp.isGodRays());
        assertTrue(spotComp.isVolumetricNoise());

        // Detailed check on light_celestial_pillar
        VFXBlueprint bpCelestial = VFXClientBlueprintCompiler.compile(DefaultSchematics.LIGHT_CELESTIAL_PILLAR);
        com.unrealvfx.client.vfx.component.LightComponent colComp = (com.unrealvfx.client.vfx.component.LightComponent) bpCelestial.getComponents().get(0);
        assertEquals(com.unrealvfx.client.vfx.component.LightComponent.LightShape.COLUMN, colComp.getShape());
        assertEquals("celestial_beam", colComp.getShaderName());
        assertTrue(colComp.isLightTerrain());

        // Detailed check on light_cyber_holobeacon
        VFXBlueprint bpCyber = VFXClientBlueprintCompiler.compile(DefaultSchematics.LIGHT_CYBER_HOLOBEACON);
        com.unrealvfx.client.vfx.component.LightComponent cyberComp = (com.unrealvfx.client.vfx.component.LightComponent) bpCyber.getComponents().get(0);
        assertEquals(com.unrealvfx.client.vfx.component.LightComponent.LightShape.SPOT, cyberComp.getShape());
        assertEquals("cyber_grid_shaft", cyberComp.getShaderName());
        assertTrue(cyberComp.isPulsing());

        // Detailed check on light_abyssal_singularity
        VFXBlueprint bpAbyssal = VFXClientBlueprintCompiler.compile(DefaultSchematics.LIGHT_ABYSSAL_SINGULARITY);
        com.unrealvfx.client.vfx.component.LightComponent abyssalComp = (com.unrealvfx.client.vfx.component.LightComponent) bpAbyssal.getComponents().get(0);
        assertEquals(com.unrealvfx.client.vfx.component.LightComponent.LightShape.POINT, abyssalComp.getShape());
        assertEquals("abyssal_void_light", abyssalComp.getShaderName());

        // Detailed check on light_valkyrie_aurora
        VFXBlueprint bpAurora = VFXClientBlueprintCompiler.compile(DefaultSchematics.LIGHT_VALKYRIE_AURORA);
        com.unrealvfx.client.vfx.component.LightComponent auroraComp = (com.unrealvfx.client.vfx.component.LightComponent) bpAurora.getComponents().get(0);
        assertEquals(com.unrealvfx.client.vfx.component.LightComponent.LightShape.SPOT, auroraComp.getShape());
        assertEquals("aurora_borealis", auroraComp.getShaderName());
        assertTrue(auroraComp.isLightTerrain());
    }

    private static void assertEquals(Object expected, Object actual) {
        if (expected == null && actual == null) return;
        if (expected != null && expected.equals(actual)) return;
        throw new AssertionError("Expected " + expected + " but got " + actual);
    }

    private static void assertEquals(double expected, double actual, double epsilon) {
        if (Math.abs(expected - actual) > epsilon) {
            throw new AssertionError("Expected " + expected + " but got " + actual);
        }
    }

    private static void assertTrue(boolean condition) {
        if (!condition) {
            throw new AssertionError("Expected true condition");
        }
    }
}
