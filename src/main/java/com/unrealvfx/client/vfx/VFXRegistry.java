package com.unrealvfx.client.vfx;

import com.unrealvfx.client.render.ImpactFrameManager;
import com.unrealvfx.client.render.RenderState;
import com.unrealvfx.client.vfx.component.*;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Registro centrale dei blueprint VFX.
 * Configura i preset visivi ispirati alla struttura di CustomNPC+ e all'estetica di Sparking! ZERO:
 * - dbz:energy_beam (Raggio continuo con carica windup, corpo a flusso supersonico, testa, ancora e scariche)
 * - dbz:energy_disc (Disco energetico rotante Kienzan / Destructo Disc)
 * - dbz:energy_dome (Cupola di difesa a celle esagonali cel-shaded)
 * - dbz:hakai_ball (Sfera della Distruzione snella e rifinita: nucleo nero cosmico, corona 3D, doppi anelli)
 * - dbz:hakai_explosion (Detonazione cataclismica con Impact Frame e distorsione ottica)
 * - dbz:sparking_aura_ssj (Aura volumetrica 360 pura senza sovrapposizioni sgraziate sul corpo)
 * - dbz:zanzoken_dash (Scatto con immagini residue)
 */
public class VFXRegistry {
    private static final Map<String, VFXBlueprint> registry = new HashMap<>();

    public static void init() {
        registerDefaultPresets();
    }

    public static void register(String id, VFXBlueprint blueprint) {
        blueprint.setId(id);
        registry.put(id, blueprint);
    }

    public static VFXBlueprint get(String id) {
        return registry.get(id);
    }

    public static boolean has(String id) {
        return registry.containsKey(id);
    }

    public static Set<String> getRegisteredIds() {
        return registry.keySet();
    }

    public static Collection<VFXBlueprint> getAll() {
        return registry.values();
    }

    public static VFXInstance spawn(String id, World world, double x, double y, double z, float scale) {
        VFXBlueprint bp = get(id);
        if (bp != null) {
            VFXInstance inst = new VFXInstance(bp, world, x, y, z);
            inst.setScaleModifier(scale);
            VFXManager.getInstance().addInstance(inst);
            return inst;
        }
        return null;
    }

    public static VFXInstance attach(String id, Entity entity, float scale) {
        VFXBlueprint bp = get(id);
        if (bp != null) {
            VFXInstance inst = new VFXInstance(bp, entity);
            inst.setScaleModifier(scale);
            VFXManager.getInstance().addInstance(inst);
            return inst;
        }
        return null;
    }

    private static void registerDefaultPresets() {
        // =========================================================================
        // 1. RAGGIO CONTINUO ENERGETICO (Beam / Kamehameha / Laser - Ispirato a CNPC+)
        // =========================================================================
        register("dbz:energy_beam", new VFXBlueprint()
            .setDuration(45)
            .setLoop(false)
            .addComponent(CameraShakeComponent.continuous(0.08f))
            .addComponent(new BeamComponent(24.0f, 0.65f)
                .setColor(0.18f, 0.68f, 1.0f) // Azzurro/Ciano classico
                .setChargeTicks(12)
                .setHasHeadOrb(true)
                .setHasTailOrb(true)
                .setHasLightning(true))
        );

        // =========================================================================
        // 2. DISCO DA TAGLIO (Kienzan / Destructo Disc / Slicer - Ispirato a CNPC+)
        // =========================================================================
        register("dbz:energy_disc", new VFXBlueprint()
            .setDuration(50)
            .setLoop(true)
            .addComponent(new MeshComponent(MeshComponent.MeshType.DISC)
                .setShader("energy_disc")
                .setColor(1.0f, 0.90f, 0.15f)
                .setScale(Easing.LINEAR, 1.35f, 1.35f)
                .setRotationSpeed(0.0f, 16.0f, 0.0f)
                .setBlendMode(RenderState.BlendMode.ADDITIVE))
        );

        // =========================================================================
        // 3. BARRIERA A CUPOLA (Ki Shield / Android 17 Barrier - Ispirato a CNPC+)
        // =========================================================================
        register("dbz:energy_dome", new VFXBlueprint()
            .setDuration(60)
            .setLoop(true)
            .addComponent(new MeshComponent(MeshComponent.MeshType.DOME)
                .setShader("energy_barrier")
                .setColor(0.20f, 0.85f, 0.45f) // Verde smeraldo brillante
                .setScale(Easing.OUT_QUAD, 0.3f, 3.2f)
                .setBlendMode(RenderState.BlendMode.ADDITIVE))
        );

        // =========================================================================
        // 4. HAKAI BALL (Sfera della Distruzione - Pulita, Snella e di Altissima Qualita)
        // =========================================================================
        register("dbz:hakai_ball", new VFXBlueprint()
            .setDuration(60)
            .setLoop(true)
            // Vibrazione viscerale e sismica
            .addComponent(CameraShakeComponent.continuous(0.10f))

            // Nucleo: Nero ossidiana abissale puro con vene cosmiche e bordo Fresnel ultra-netto
            .addComponent(new MeshComponent(MeshComponent.MeshType.SPHERE)
                .setShader("hakai_core")
                .setColor(1.0f, 0.04f, 0.82f)
                .setScale(Easing.LINEAR, 1.25f, 1.25f)
                .setRotationSpeed(0.8f, 0.5f, 0.3f)
                .setBlendMode(RenderState.BlendMode.NORMAL)
                .setDepthMask(true))

            // Corona 3D di plasma ruggente con displacement a punte
            .addComponent(new MeshComponent(MeshComponent.MeshType.SPHERE)
                .setShader("hakai_corona")
                .setColor(1.0f, 0.05f, 0.88f)
                .setScale(Easing.IN_OUT_QUAD, 1.30f, 1.40f)
                .setRotationSpeed(3.0f, 2.0f, 1.2f)
                .setBlendMode(RenderState.BlendMode.ADDITIVE))

            // Anello d'accrescimento orbitale 1 (Inclinato a 28 gradi, rotazione oraria)
            .addComponent(new MeshComponent(MeshComponent.MeshType.THIN_RING)
                .setShader("hakai_ring")
                .setColor(1.0f, 0.2f, 0.95f)
                .setScale(Easing.LINEAR, 1.85f, 1.85f)
                .setInitialRotation(0.0f, 28.0f, 18.0f)
                .setRotationSpeed(6.5f, 0.0f, 0.0f)
                .setBlendMode(RenderState.BlendMode.ADDITIVE))

            // Anello d'accrescimento orbitale 2 (Inclinato a -38 gradi, contro-rotazione veloce)
            .addComponent(new MeshComponent(MeshComponent.MeshType.THIN_RING)
                .setShader("hakai_ring")
                .setColor(0.9f, 0.1f, 0.9f)
                .setScale(Easing.LINEAR, 2.20f, 2.20f)
                .setInitialRotation(0.0f, -38.0f, -22.0f)
                .setRotationSpeed(-8.0f, 0.0f, 0.0f)
                .setBlendMode(RenderState.BlendMode.ADDITIVE))

            // Scariche elettriche superficiali che avvolgono la sfera
            .addComponent(new LightningComponent(10, 1.40f, 1.40f)
                .setSpherical(true)
                .setColor(0.98f, 0.12f, 0.92f)
                .setWidth(0.085f))

            // Mote cubici di realta risucchiati verso il nucleo
            .addComponent(new VelocityParticleComponent(5, 1.5f)
                .setParticleType(VelocityParticleComponent.ParticleType.REALITY_CUBE)
                .setSuction(true)
                .setSwirl(true)
                .setColor(0.95f, 0.1f, 0.9f))

            // Distorsione ottica gravitazionale (curvatura della luce dietro la sfera)
            .addComponent(new DistortionComponent(0.8f, 3.0f, 0.065f))
        );

        // =========================================================================
        // 5. HAKAI EXPLOSION (Detonazione Cataclismica - Pulita e Cinematografica)
        // =========================================================================
        register("dbz:hakai_explosion", new VFXBlueprint()
            .setDuration(50)
            .setLoop(false)
            // Impact Frame Sakuga (Flash Hakai Purple a 4 fotogrammi)
            .addComponent(new ImpactFrameTriggerComponent(ImpactFrameManager.Type.HAKAI_PURPLE, 4))

            // Trauma Shake massimo + FOV kick
            .addComponent(CameraShakeComponent.oneShot(1.0f, 28.0f))

            // Onda di distorsione ottica a 24 blocchi
            .addComponent(new DistortionComponent(0.5f, 24.0f, 0.085f))

            // Shockwave ad anello radente
            .addComponent(new ShockwaveComponent()
                .setRadius(Easing.OUT_EXPO, 0.5f, 20.0f)
                .setAlpha(Easing.IN_QUAD, 1.0f, 0.0f)
                .setColor(1.0f, 0.3f, 0.95f))

            // Sfera di vuoto assoluto gigante in espansione
            .addComponent(new MeshComponent(MeshComponent.MeshType.SPHERE)
                .setShader("hakai_core")
                .setColor(Easing.IN_QUAD, 1.0f, 0.05f, 0.85f, 0.35f, 0.0f, 0.55f)
                .setScale(Easing.OUT_EXPO, 0.5f, 16.0f)
                .setAlpha(Easing.IN_QUAD, 1.0f, 0.0f)
                .setBlendMode(RenderState.BlendMode.ADDITIVE))

            // Fulmini ramificati a cataclisma
            .addComponent(new LightningComponent(16, 12.0f, 8.0f)
                .setColor(0.98f, 0.15f, 0.95f)
                .setWidth(0.14f))
        );

        // =========================================================================
        // 6. AURA SPARKING! ZERO (Aura volumetrica 360 pura senza overlay sul player)
        // =========================================================================
        register("dbz:sparking_aura_ssj", new VFXBlueprint()
            .setDuration(30)
            .setLoop(true)
            .addComponent(CameraShakeComponent.continuous(0.12f))

            // Guscio a capsula arrotondata volumetrica 360 uniforme
            .addComponent(new MeshComponent(MeshComponent.MeshType.AURA_CAPSULE)
                .setShader("aura_cel")
                .setColor(1.0f, 0.88f, 0.15f)
                .setScale(Easing.IN_OUT_QUAD, 1.05f, 1.18f)
                .setAlpha(Easing.LINEAR, 0.95f, 0.95f)
                .setRotationSpeed(3.5f, 0.0f, 0.0f)
                .setBlendMode(RenderState.BlendMode.ADDITIVE))

            // Strato interno incandescente in contro-rotazione
            .addComponent(new MeshComponent(MeshComponent.MeshType.AURA_CAPSULE)
                .setShader("aura_cel")
                .setColor(1.0f, 1.0f, 0.65f)
                .setScale(Easing.IN_OUT_QUAD, 0.85f, 0.96f)
                .setAlpha(Easing.LINEAR, 0.90f, 0.90f)
                .setRotationSpeed(-5.0f, 0.0f, 0.0f)
                .setBlendMode(RenderState.BlendMode.ADDITIVE))

            // Scariche elettriche ramificate (Azzurro/Ciano SSJ2)
            .addComponent(new LightningComponent(6, 1.25f, 2.1f)
                .setColor(0.35f, 0.88f, 1.0f)
                .setWidth(0.08f))

            // Detriti di roccia anti-gravita
            .addComponent(new FloatingDebrisComponent(6, 1.6f))
        );

        // =========================================================================
        // 7. ZANZOKEN DASH (Scatto ad alta velocita con Immagini Residue)
        // =========================================================================
        register("dbz:zanzoken_dash", new VFXBlueprint()
            .setDuration(20)
            .setLoop(false)
            .addComponent(new ZanzokenComponent())
            .addComponent(new SpeedLinesTriggerComponent(0.85f, 1.0f, 1.0f, 1.0f))
            .addComponent(new VelocityParticleComponent(6, 0.8f)
                .setColor(1.0f, 1.0f, 1.0f))
        );

        // =========================================================================
        // 8. ESPLOSIONE COSMICA BASE
        // =========================================================================
        register("dbz:sparking_explosion", new VFXBlueprint()
            .setDuration(45)
            .setLoop(false)
            .addComponent(CameraShakeComponent.oneShot(0.85f, 18.0f))
            .addComponent(new DistortionComponent(0.5f, 16.0f, 0.065f))
            .addComponent(new ShockwaveComponent()
                .setRadius(Easing.OUT_QUAD, 0.5f, 14.0f)
                .setAlpha(Easing.IN_QUAD, 1.0f, 0.0f)
                .setColor(1.0f, 0.95f, 0.65f))
            .addComponent(new MeshComponent(MeshComponent.MeshType.SPHERE)
                .setShader("energy_core")
                .setColor(Easing.IN_QUAD, 1.0f, 0.95f, 0.3f, 1.0f, 0.18f, 0.04f)
                .setScale(Easing.OUT_EXPO, 0.2f, 9.5f)
                .setAlpha(Easing.IN_QUAD, 1.0f, 0.0f)
                .setRotationSpeed(2.5f, 2.0f, 0.0f)
                .setBlendMode(RenderState.BlendMode.ADDITIVE))
            .addComponent(new MeshComponent(MeshComponent.MeshType.SPHERE)
                .setShader("energy_core")
                .setColor(1.0f, 1.0f, 1.0f)
                .setScale(Easing.OUT_EXPO, 0.5f, 5.5f)
                .setAlpha(Easing.IN_EXPO, 1.0f, 0.0f)
                .setBlendMode(RenderState.BlendMode.ADDITIVE))
            .addComponent(new LightningComponent(12, 6.5f, 4.5f)
                .setColor(1.0f, 0.85f, 0.2f)
                .setWidth(0.12f))
        );

        // =========================================================================
        // 9. INTERSTELLAR GARGANTUA (Realistic Black Hole)
        //
        // =========================================================================
        // 9. INTERSTELLAR GARGANTUA (Realistic 3D Black Hole)
        //
        // Sequenza ottico-geometrica reale:
        //   1. Lensing Gravitazionale 3D: deflessione ottica dello spaziotempo (blocchi, cielo, mondo)
        //   2. Disco d'Accrescimento 3D: profilo lenticolare con deflessione di Kip Thorne nel vertex shader
        //   3. Sfera Nera 3D: orizzonte degli eventi opaco con alone bianco fosforescente Fresnel
        //   4. Particelle relativistiche: materia che spiraleggia nella singolarita
        // =========================================================================
        VFXBlueprint gargantua = new VFXBlueprint()
            .setDuration(120)
            .setLoop(true)
            .addComponent(CameraShakeComponent.continuous(0.04f))

            // Fisica gravitazionale reale: attira mob, animali e drop items
            .addComponent(new GravitationalPullComponent(16.0f, 0.55f)
                .setSwirlStrength(0.35f)
                .setDamageAtHorizon(true))

            // 1. DISTORSIONE GRAVITAZIONALE DELLO SPAZIOTEMPO (3D Einstein Lensing)
            //    - Deforma la luce del mondo di Minecraft circostante (anello di Einstein)
            //    - Discard automatico all'orizzonte: lascia visibile la sfera 3D al centro
            //    - Integrazione 3D con depth buffer e culling naturale del frustum
            .addComponent(new GravitationalLensingComponent(8.5f, 0.45f))

            // 2. DISCO D'ACCRESCIMENTO 3D (FERMO nello spazio del mondo, NON ruota)
            //    - Geometria lenticolare 3D a spessore volumetrico con normali analitiche
            //    - Deflessione di Kip Thorne nel vertex shader: la parte posteriore si incurva
            //      verticalmente sopra e sotto la sfera, creando gli archi di luce di Interstellar
            //    - Gradiente termico continuo a 5 stop + Doppler beaming relativistico
            .addComponent(new MeshComponent(MeshComponent.MeshType.ACCRETION_DISK)
                .setShader("accretion_disk")
                .setScale(Easing.LINEAR, 7.5f, 7.5f)
                .setInitialRotation(0.0f, 12.0f, 0.0f)
                .setBlendMode(RenderState.BlendMode.ADDITIVE))

            // 3. SFERA NERA 3D DELL'ORIZZONTE DEGLI EVENTI
            //    - Corpo nero assoluto opaco (opacita 100%, depthMask = true)
            //    - Alone bianco fosforescente Fresnel che ruota fluidamente con la prospettiva
            //    - Occlude fisicamente il centro del disco creando il contrasto nero perfetto
            .addComponent(new MeshComponent(MeshComponent.MeshType.SPHERE)
                .setShader("event_horizon")
                .setScale(Easing.LINEAR, 2.2f, 2.2f)
                .setBlendMode(RenderState.BlendMode.NORMAL)
                .setCullFace(false)
                .setDepthMask(true))

            // 4. PULVISCOLO STELLARE (materia che spiraleggia nella singolarita)
            .addComponent(new VelocityParticleComponent(6, 14.0f)
                .setParticleType(VelocityParticleComponent.ParticleType.NEEDLE)
                .setBaseSpeed(1.2f)
                .setStretchFactor(4.2f)
                .setSuction(true)
                .setSwirl(true)
                .setColor(1.0f, 0.82f, 0.32f));

        register("cosmic:gargantua", gargantua);
        register("cosmic:black_hole", gargantua);
    }
}
