package com.unrealvfx.client.vfx;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.unrealvfx.client.render.ImpactFrameManager;
import com.unrealvfx.client.render.RenderState;
import com.unrealvfx.client.vfx.component.*;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.util.ResourceLocation;

/**
 * Compilatore Blueprint VFX Client-Side dedicato.
 * Converte le definizioni JSON in strutture dati e componenti di rendering OpenGL/GLSL.
 * Risiede nel pacchetto client per garantire la totale separazione dai server dedicati headless.
 */
@SideOnly(Side.CLIENT)
public class VFXClientBlueprintCompiler {

    private static final JsonParser PARSER = new JsonParser();

    public static VFXBlueprint compile(String jsonSource) {
        try {
            JsonObject root = PARSER.parse(jsonSource).getAsJsonObject();
            String id = getString(root, "id", "unnamed_vfx");
            int duration = getInt(root, "duration", 60);
            boolean loop = getBoolean(root, "loop", false);

            VFXBlueprint blueprint = new VFXBlueprint(id)
                .setDuration(duration)
                .setLoop(loop);

            if (root.has("components") && root.get("components").isJsonArray()) {
                JsonArray componentsArray = root.getAsJsonArray("components");
                for (int i = 0; i < componentsArray.size(); i++) {
                    JsonElement elem = componentsArray.get(i);
                    if (!elem.isJsonObject()) continue;
                    JsonObject compObj = elem.getAsJsonObject();
                    String type = getString(compObj, "type", "").toLowerCase();

                    switch (type) {
                        case "mesh":
                            blueprint.addComponent(parseMeshComponent(compObj));
                            break;
                        case "beam":
                            blueprint.addComponent(parseBeamComponent(compObj));
                            break;
                        case "shockwave":
                            blueprint.addComponent(parseShockwaveComponent(compObj));
                            break;
                        case "distortion":
                            if (isHeatDistortion(compObj)) {
                                blueprint.addComponent(parseHeatDistortionComponent(compObj));
                            } else {
                                blueprint.addComponent(parseDistortionComponent(compObj));
                            }
                            break;
                        case "heat_distortion":
                        case "heat_wave":
                            blueprint.addComponent(parseHeatDistortionComponent(compObj));
                            break;
                        case "lightning":
                            blueprint.addComponent(parseLightningComponent(compObj));
                            break;
                        case "particle":
                        case "velocity_particle":
                            blueprint.addComponent(parseParticleComponent(compObj));
                            break;
                        case "camera_shake":
                            blueprint.addComponent(parseCameraShakeComponent(compObj));
                            break;
                        case "impact_frame":
                            blueprint.addComponent(parseImpactFrameComponent(compObj));
                            break;
                        case "gravitational_pull":
                            blueprint.addComponent(parseGravitationalPullComponent(compObj));
                            break;
                        case "gravitational_lensing":
                            blueprint.addComponent(parseGravitationalLensingComponent(compObj));
                            break;
                        case "zanzoken":
                            blueprint.addComponent(new ZanzokenComponent());
                            break;
                        case "speed_lines":
                            blueprint.addComponent(parseSpeedLinesComponent(compObj));
                            break;
                        case "floating_debris":
                            blueprint.addComponent(parseFloatingDebrisComponent(compObj));
                            break;
                        case "decal":
                        case "ground_decal":
                            blueprint.addComponent(parseGroundDecalComponent(compObj));
                            break;
                        case "light":
                        case "volumetric_light":
                        case "point_light":
                        case "spot_light":
                            blueprint.addComponent(parseLightComponent(compObj));
                            break;
                    }
                }
            }

            return blueprint;
        } catch (Exception e) {
            throw new RuntimeException("Errore durante la compilazione del blueprint client VFX", e);
        }
    }

    private static MeshComponent parseMeshComponent(JsonObject obj) {
        String meshTypeStr = getString(obj, "meshType", "SPHERE").toUpperCase();
        MeshComponent.MeshType meshType;
        try {
            meshType = MeshComponent.MeshType.valueOf(meshTypeStr);
        } catch (Exception e) {
            meshType = MeshComponent.MeshType.SPHERE;
        }

        MeshComponent comp = new MeshComponent(meshType);

        if (obj.has("shader")) {
            comp.setShader(obj.get("shader").getAsString());
        }
        if (obj.has("texture")) {
            comp.setTexture(new ResourceLocation(obj.get("texture").getAsString()));
        }

        // Scala (Uniforme o Vettoriale 3D)
        if (obj.has("scale")) {
            JsonElement scaleElem = obj.get("scale");
            if (scaleElem.isJsonObject()) {
                JsonObject sObj = scaleElem.getAsJsonObject();
                Easing easing = parseEasing(getString(sObj, "easing", "LINEAR"), Easing.LINEAR);
                if (sObj.has("start") && sObj.get("start").isJsonArray()) {
                    float[] s1 = parseVec3(sObj.get("start"), new float[]{1f, 1f, 1f});
                    float[] s2 = parseVec3(sObj.get("end"), s1);
                    comp.setScale3D(easing, s1[0], s1[1], s1[2], s2[0], s2[1], s2[2]);
                } else {
                    float start = getFloat(sObj, "start", 1.0f);
                    float end = getFloat(sObj, "end", 1.0f);
                    comp.setScale(easing, start, end);
                }
            } else if (scaleElem.isJsonArray()) {
                float[] s = parseVec3(scaleElem, new float[]{1f, 1f, 1f});
                comp.setScale3D(Easing.LINEAR, s[0], s[1], s[2], s[0], s[1], s[2]);
            } else if (scaleElem.isJsonPrimitive()) {
                float val = scaleElem.getAsFloat();
                comp.setScale(Easing.LINEAR, val, val);
            }
        }

        // Pivot Offset
        if (obj.has("pivot")) {
            float[] piv = parseVec3(obj.get("pivot"), new float[]{0f, 0f, 0f});
            comp.setPivot(piv[0], piv[1], piv[2]);
        }

        // Modificatore Cloner / Array (stile MoGraph Cinema 4D)
        if (obj.has("array") && obj.get("array").isJsonObject()) {
            JsonObject arrObj = obj.getAsJsonObject("array");
            int count = getInt(arrObj, "count", 1);
            float[] stepPos = parseVec3(arrObj.get("stepPos"), new float[]{0f, 0f, 0f});
            float[] stepRot = parseVec3(arrObj.get("stepRot"), new float[]{0f, 0f, 0f});
            float[] stepScale = parseVec3(arrObj.get("stepScale"), new float[]{0f, 0f, 0f});
            float phaseDelay = getFloat(arrObj, "phaseDelay", 0.0f);
            comp.setArrayModifier(count, stepPos[0], stepPos[1], stepPos[2],
                                  stepRot[0], stepRot[1], stepRot[2],
                                  stepScale[0], stepScale[1], stepScale[2],
                                  phaseDelay);
        }

        // Proprietà Shader Multi-Layer Universale & Celestial Void / Aura
        float[] hotCore = obj.has("hotCoreColor") ? parseVec3(obj.get("hotCoreColor"), new float[]{1f, 1f, 1f}) : null;
        if (hotCore == null && obj.has("voidColor")) {
            hotCore = parseVec3(obj.get("voidColor"), new float[]{0.01f, 0.0f, 0.02f});
        }
        float[] rimCol = obj.has("rimColor") ? parseVec3(obj.get("rimColor"), null) : null;
        if (rimCol == null && obj.has("coronaColor")) {
            rimCol = parseVec3(obj.get("coronaColor"), null);
        }
        float rimPow = getFloat(obj, "rimPower", getFloat(obj, "falloffPower", 3.0f));
        float nSpeed = getFloat(obj, "noiseSpeed", 1.0f);
        float nScale = getFloat(obj, "noiseScale", 2.0f);
        float bMult = getFloat(obj, "bloomMultiplier", 1.5f);
        boolean isCelestialVoid = "celestial_void".equalsIgnoreCase(getString(obj, "shader", ""));
        boolean invertedCore = getBoolean(obj, "invertedCore", getBoolean(obj, "voidMode", isCelestialVoid));
        float filamentStrength = getFloat(obj, "filamentStrength", getFloat(obj, "surfaceFilaments", 0.85f));
        float distortionStrength = getFloat(obj, "distortionStrength", 0.12f);
        float celBands = getFloat(obj, "celBands", 3.0f);

        comp.setUniversalShaderProps(hotCore, rimCol, rimPow, nSpeed, nScale, bMult, invertedCore, filamentStrength);
        comp.setDistortionStrength(distortionStrength);
        comp.setCelBands(celBands);

        // Opacita Alpha
        if (obj.has("alpha")) {
            JsonElement alphaElem = obj.get("alpha");
            if (alphaElem.isJsonObject()) {
                JsonObject aObj = alphaElem.getAsJsonObject();
                float start = getFloat(aObj, "start", 1.0f);
                float end = getFloat(aObj, "end", 1.0f);
                Easing easing = parseEasing(getString(aObj, "easing", "LINEAR"), Easing.LINEAR);
                comp.setAlpha(easing, start, end);
            } else if (alphaElem.isJsonPrimitive()) {
                float val = alphaElem.getAsFloat();
                comp.setAlpha(Easing.LINEAR, val, val);
            }
        }

        // Colore
        if (obj.has("color")) {
            JsonElement colElem = obj.get("color");
            if (colElem.isJsonObject()) {
                JsonObject cObj = colElem.getAsJsonObject();
                float[] start = parseVec3(cObj.get("start"), new float[]{1f, 1f, 1f});
                float[] end = parseVec3(cObj.get("end"), start);
                Easing easing = parseEasing(getString(cObj, "easing", "LINEAR"), Easing.LINEAR);
                comp.setColor(easing, start[0], start[1], start[2], end[0], end[1], end[2]);
            } else if (colElem.isJsonArray()) {
                float[] col = parseVec3(colElem, new float[]{1f, 1f, 1f});
                comp.setColor(col[0], col[1], col[2]);
            }
        }

        // Rotazione
        if (obj.has("rotationSpeed")) {
            float[] spd = parseVec3(obj.get("rotationSpeed"), new float[]{0f, 0f, 0f});
            comp.setRotationSpeed(spd[0], spd[1], spd[2]);
        }
        if (obj.has("initialRotation")) {
            float[] init = parseVec3(obj.get("initialRotation"), new float[]{0f, 0f, 0f});
            comp.setInitialRotation(init[0], init[1], init[2]);
        }

        // Offset
        if (obj.has("offset")) {
            float[] off = parseVec3(obj.get("offset"), new float[]{0f, 0f, 0f});
            comp.setOffset(off[0], off[1], off[2]);
        }

        // Opzioni di rendering
        if (obj.has("blendMode")) {
            String bStr = obj.get("blendMode").getAsString().toUpperCase();
            try {
                comp.setBlendMode(RenderState.BlendMode.valueOf(bStr));
            } catch (Exception ignored) {}
        }
        if (obj.has("depthMask")) {
            comp.setDepthMask(obj.get("depthMask").getAsBoolean());
        }
        if (obj.has("cullFace")) {
            comp.setCullFace(obj.get("cullFace").getAsBoolean());
        }
        if (obj.has("billboard")) {
            comp.setBillboard(obj.get("billboard").getAsBoolean());
        }
        if (obj.has("steppedFps")) {
            comp.setSteppedFps(getFloat(obj, "steppedFps", 0.0f));
        } else if (obj.has("sakugaFps")) {
            comp.setSteppedFps(getFloat(obj, "sakugaFps", 0.0f));
        } else if (obj.has("steppedTiming") && obj.get("steppedTiming").getAsBoolean()) {
            comp.setSteppedFps(12.0f);
        }

        return comp;
    }

    private static BeamComponent parseBeamComponent(JsonObject obj) {
        float length = getFloat(obj, "length", 24.0f);
        float width = getFloat(obj, "width", 0.65f);
        BeamComponent comp = new BeamComponent(length, width);

        if (obj.has("chargeTicks")) {
            comp.setChargeTicks(getInt(obj, "chargeTicks", 12));
        }
        if (obj.has("color")) {
            float[] col = parseVec3(obj.get("color"), new float[]{0.2f, 0.65f, 1.0f});
            comp.setColor(col[0], col[1], col[2]);
        }
        if (obj.has("hasHeadOrb")) {
            comp.setHasHeadOrb(obj.get("hasHeadOrb").getAsBoolean());
        }
        if (obj.has("hasTailOrb")) {
            comp.setHasTailOrb(obj.get("hasTailOrb").getAsBoolean());
        }
        if (obj.has("hasLightning")) {
            comp.setHasLightning(obj.get("hasLightning").getAsBoolean());
        }
        if (obj.has("hasHelicalRibbons")) {
            comp.setHasHelicalRibbons(obj.get("hasHelicalRibbons").getAsBoolean());
        }
        if (obj.has("ribbonRadius")) {
            comp.setRibbonRadius(getFloat(obj, "ribbonRadius", 0.85f));
        }
        if (obj.has("ribbonSpeed")) {
            comp.setRibbonSpeed(getFloat(obj, "ribbonSpeed", 8.0f));
        }
        if (obj.has("ribbonColor")) {
            float[] rcol = parseVec3(obj.get("ribbonColor"), new float[]{0.3f, 0.8f, 1.0f});
            comp.setRibbonColor(rcol[0], rcol[1], rcol[2]);
        }
        return comp;
    }

    private static ShockwaveComponent parseShockwaveComponent(JsonObject obj) {
        ShockwaveComponent comp = new ShockwaveComponent();

        if (obj.has("radius")) {
            JsonElement radElem = obj.get("radius");
            if (radElem.isJsonObject()) {
                JsonObject rObj = radElem.getAsJsonObject();
                float start = getFloat(rObj, "start", 0.5f);
                float end = getFloat(rObj, "end", 6.0f);
                Easing easing = parseEasing(getString(rObj, "easing", "OUT_QUAD"), Easing.OUT_QUAD);
                comp.setRadius(easing, start, end);
            } else if (radElem.isJsonPrimitive()) {
                float val = radElem.getAsFloat();
                comp.setRadius(Easing.OUT_QUAD, 0.5f, val);
            }
        }

        if (obj.has("alpha")) {
            JsonElement aElem = obj.get("alpha");
            if (aElem.isJsonObject()) {
                JsonObject aObj = aElem.getAsJsonObject();
                float start = getFloat(aObj, "start", 1.0f);
                float end = getFloat(aObj, "end", 0.0f);
                Easing easing = parseEasing(getString(aObj, "easing", "IN_QUAD"), Easing.IN_QUAD);
                comp.setAlpha(easing, start, end);
            }
        }

        if (obj.has("color")) {
            float[] col = parseVec3(obj.get("color"), new float[]{1.0f, 0.9f, 0.5f});
            comp.setColor(col[0], col[1], col[2]);
        }
        if (obj.has("offsetY")) {
            comp.setOffsetY(getFloat(obj, "offsetY", 0.05f));
        }

        return comp;
    }

    private static DistortionComponent parseDistortionComponent(JsonObject obj) {
        float startRadius = getFloat(obj, "startRadius", 0.5f);
        float endRadius = getFloat(obj, "endRadius", 16.0f);
        float strength = getFloat(obj, "strength", 0.065f);
        return new DistortionComponent(startRadius, endRadius, strength);
    }

    private static boolean isHeatDistortion(JsonObject obj) {
        String mode = getString(obj, "mode", "");
        return obj.has("shape") ||
               obj.has("centerFalloff") ||
               obj.has("falloffPower") ||
               "heat".equalsIgnoreCase(mode) ||
               "heat_wave".equalsIgnoreCase(mode) ||
               "heat_distortion".equalsIgnoreCase(mode);
    }

    private static HeatDistortionComponent parseHeatDistortionComponent(JsonObject obj) {
        String shapeStr = getString(obj, "shape", "SPHERE").toUpperCase();
        HeatDistortionComponent.Shape shape;
        try {
            shape = HeatDistortionComponent.Shape.valueOf(shapeStr);
        } catch (Exception e) {
            shape = HeatDistortionComponent.Shape.SPHERE;
        }

        float strength = getFloat(obj, "strength", 0.06f);
        float falloffPower = getFloat(obj, "centerFalloff", getFloat(obj, "falloffPower", 2.0f));

        HeatDistortionComponent comp = new HeatDistortionComponent(shape, strength, falloffPower);

        comp.setCullFace(getBoolean(obj, "cullFace", false));

        if (obj.has("noiseSpeed")) {
            comp.setNoiseSpeed(getFloat(obj, "noiseSpeed", 2.5f));
        }
        if (obj.has("noiseFrequency")) {
            comp.setNoiseScale(getFloat(obj, "noiseFrequency", 4.0f));
        } else if (obj.has("noiseScale")) {
            comp.setNoiseScale(getFloat(obj, "noiseScale", 4.0f));
        }

        if (obj.has("scale")) {
            JsonElement sElem = obj.get("scale");
            if (sElem.isJsonArray()) {
                float[] sc = parseVec3(sElem, new float[]{2.0f, 2.0f, 2.0f});
                comp.setScale(sc[0], sc[1], sc[2]);
            } else if (sElem.isJsonPrimitive()) {
                float s = sElem.getAsFloat();
                comp.setScale(s, s, s);
            }
        } else {
            float sx = getFloat(obj, "scaleX", 2.0f);
            float sy = getFloat(obj, "scaleY", 2.0f);
            float sz = getFloat(obj, "scaleZ", 2.0f);
            comp.setScale(sx, sy, sz);
        }

        if (obj.has("offset")) {
            float[] off = parseVec3(obj.get("offset"), new float[]{0.0f, 0.0f, 0.0f});
            comp.setOffset(off[0], off[1], off[2]);
        } else {
            float ox = getFloat(obj, "offsetX", 0.0f);
            float oy = getFloat(obj, "offsetY", 0.0f);
            float oz = getFloat(obj, "offsetZ", 0.0f);
            comp.setOffset(ox, oy, oz);
        }

        if (obj.has("tintColor")) {
            float[] tint = parseVec3(obj.get("tintColor"), new float[]{1.0f, 0.65f, 0.25f});
            comp.setTintColor(tint[0], tint[1], tint[2]);
        } else if (obj.has("color")) {
            float[] col = parseVec3(obj.get("color"), new float[]{1.0f, 0.65f, 0.25f});
            comp.setTintColor(col[0], col[1], col[2]);
        }

        if (obj.has("tintAlpha")) {
            comp.setTintAlpha(getFloat(obj, "tintAlpha", 0.0f));
        } else if (obj.has("tintOpacity")) {
            comp.setTintAlpha(getFloat(obj, "tintOpacity", 0.0f));
        } else if (obj.has("alpha")) {
            comp.setTintAlpha(getFloat(obj, "alpha", 0.0f));
        }

        return comp;
    }

    private static LightningComponent parseLightningComponent(JsonObject obj) {
        int count = getInt(obj, "arcCount", getInt(obj, "density", 8));
        float radius = getFloat(obj, "radius", 4.0f);
        float height = getFloat(obj, "height", 3.0f);

        LightningComponent comp = new LightningComponent(count, radius, height);

        if (obj.has("mode")) {
            String mStr = obj.get("mode").getAsString().toUpperCase();
            try {
                comp.setMode(LightningComponent.LightningMode.valueOf(mStr));
            } catch (Exception ignored) {}
        }
        if (obj.has("spherical")) {
            comp.setSpherical(obj.get("spherical").getAsBoolean());
        }

        if (obj.has("color")) {
            float[] col = parseVec3(obj.get("color"), new float[]{0.75f, 0.35f, 1.0f});
            comp.setColor(col[0], col[1], col[2]);
        }
        if (obj.has("coreColor")) {
            float[] cCol = parseVec3(obj.get("coreColor"), new float[]{1.0f, 1.0f, 1.0f});
            comp.setCoreColor(cCol[0], cCol[1], cCol[2]);
        }
        if (obj.has("width")) {
            comp.setWidth(getFloat(obj, "width", 0.10f));
        }
        if (obj.has("roughness")) {
            comp.setRoughness(getFloat(obj, "roughness", 0.35f));
        }
        if (obj.has("branchForks")) {
            comp.setBranchForks(getInt(obj, "branchForks", 1));
        }
        if (obj.has("forkChance")) {
            comp.setForkChance(getFloat(obj, "forkChance", 0.35f));
        }
        if (obj.has("glowMultiplier") || obj.has("glow")) {
            comp.setGlowMultiplier(getFloat(obj, "glowMultiplier", getFloat(obj, "glow", 2.0f)));
        }
        if (obj.has("offset")) {
            float[] off = parseVec3(obj.get("offset"), new float[]{0f, 0f, 0f});
            comp.setOffset(off[0], off[1], off[2]);
        }
        if (obj.has("targetStart") && obj.has("targetEnd")) {
            float[] s = parseVec3(obj.get("targetStart"), new float[]{0f, 0f, 0f});
            float[] e = parseVec3(obj.get("targetEnd"), new float[]{0f, -5f, 0f});
            comp.setTarget(s[0], s[1], s[2], e[0], e[1], e[2]);
        }
        if (obj.has("subdivisions")) {
            comp.setSubdivisions(getInt(obj, "subdivisions", 4));
        }

        return comp;
    }

    private static VelocityParticleComponent parseParticleComponent(JsonObject obj) {
        int count = getInt(obj, "spawnRate", 4);
        float radius = getFloat(obj, "emissionRadius", 1.5f);

        VelocityParticleComponent comp = new VelocityParticleComponent(count, radius);

        if (obj.has("particleType")) {
            try {
                comp.setParticleType(VelocityParticleComponent.ParticleType.valueOf(obj.get("particleType").getAsString().toUpperCase()));
            } catch (Exception ignored) {}
        }
        if (obj.has("baseSpeed")) {
            comp.setBaseSpeed(getFloat(obj, "baseSpeed", 0.35f));
        }
        if (obj.has("stretchFactor")) {
            comp.setStretchFactor(getFloat(obj, "stretchFactor", 3.2f));
        }
        if (obj.has("color")) {
            float[] col = parseVec3(obj.get("color"), new float[]{1.0f, 0.95f, 0.3f});
            comp.setColor(col[0], col[1], col[2]);
        }
        if (obj.has("suction")) {
            comp.setSuction(obj.get("suction").getAsBoolean());
        }
        if (obj.has("swirl")) {
            comp.setSwirl(obj.get("swirl").getAsBoolean());
        }
        return comp;
    }

    private static CameraShakeComponent parseCameraShakeComponent(JsonObject obj) {
        float trauma = getFloat(obj, "trauma", 0.5f);
        float fovKick = getFloat(obj, "fovKick", 0.0f);
        boolean continuous = getBoolean(obj, "continuous", false);
        return new CameraShakeComponent(trauma, fovKick, continuous);
    }

    private static ImpactFrameTriggerComponent parseImpactFrameComponent(JsonObject obj) {
        String typeStr = getString(obj, "impactType", getString(obj, "flashType", getString(obj, "mode", "MANGA_BW"))).toUpperCase();
        ImpactFrameManager.Type type;
        try {
            type = ImpactFrameManager.Type.valueOf(typeStr);
        } catch (Exception e) {
            type = ImpactFrameManager.Type.MANGA_BW;
        }
        int duration = getInt(obj, "duration", getInt(obj, "durationTicks", 2));
        int triggerTick = getInt(obj, "triggerTick", getInt(obj, "triggerFrame", 1));
        return new ImpactFrameTriggerComponent(type, duration, triggerTick);
    }

    private static GravitationalPullComponent parseGravitationalPullComponent(JsonObject obj) {
        float radius = getFloat(obj, "radius", 16.0f);
        float strength = getFloat(obj, "pullStrength", 0.55f);
        GravitationalPullComponent comp = new GravitationalPullComponent(radius, strength);

        if (obj.has("swirlStrength")) {
            comp.setSwirlStrength(getFloat(obj, "swirlStrength", 0.35f));
        }
        if (obj.has("damageAtHorizon")) {
            comp.setDamageAtHorizon(obj.get("damageAtHorizon").getAsBoolean());
        }
        return comp;
    }

    private static GravitationalLensingComponent parseGravitationalLensingComponent(JsonObject obj) {
        float radius = getFloat(obj, "radius", 8.5f);
        float strength = getFloat(obj, "strength", 0.45f);
        return new GravitationalLensingComponent(radius, strength);
    }

    private static SpeedLinesTriggerComponent parseSpeedLinesComponent(JsonObject obj) {
        float speed = getFloat(obj, "speed", 0.85f);
        float[] col = parseVec3(obj.get("color"), new float[]{1.0f, 1.0f, 1.0f});
        return new SpeedLinesTriggerComponent(speed, col[0], col[1], col[2]);
    }

    private static FloatingDebrisComponent parseFloatingDebrisComponent(JsonObject obj) {
        int count = getInt(obj, "count", getInt(obj, "maxRocks", 6));
        float radius = getFloat(obj, "radius", 1.6f);
        FloatingDebrisComponent comp = new FloatingDebrisComponent(count, radius);

        if (obj.has("debrisType")) {
            try {
                comp.setDebrisType(FloatingDebrisComponent.DebrisType.valueOf(obj.get("debrisType").getAsString().toUpperCase()));
            } catch (Exception ignored) {}
        }
        if (obj.has("color")) {
            float[] c = parseVec3(obj.get("color"), new float[]{0.55f, 0.50f, 0.45f});
            comp.setColor(c[0], c[1], c[2]);
        }
        if (obj.has("anticipationLevitation")) {
            comp.setAnticipationLevitation(obj.get("anticipationLevitation").getAsBoolean());
        }
        if (obj.has("centripetalSuction")) {
            comp.setCentripetalSuction(obj.get("centripetalSuction").getAsBoolean());
        }
        if (obj.has("explosiveDispersal")) {
            comp.setExplosiveDispersal(obj.get("explosiveDispersal").getAsBoolean());
        }
        if (obj.has("steppedFps")) {
            comp.setSteppedFps(getFloat(obj, "steppedFps", 0.0f));
        } else if (obj.has("sakugaFps")) {
            comp.setSteppedFps(getFloat(obj, "sakugaFps", 0.0f));
        } else if (obj.has("steppedTiming") && obj.get("steppedTiming").getAsBoolean()) {
            comp.setSteppedFps(12.0f);
        }
        return comp;
    }

    private static GroundDecalComponent parseGroundDecalComponent(JsonObject obj) {
        float startRadius = getFloat(obj, "startRadius", 0.5f);
        float endRadius = getFloat(obj, "endRadius", 6.0f);
        return new GroundDecalComponent(startRadius, endRadius);
    }

    private static String getString(JsonObject obj, String name, String def) {
        return (obj.has(name) && !obj.get(name).isJsonNull()) ? obj.get(name).getAsString() : def;
    }

    private static int getInt(JsonObject obj, String name, int def) {
        return (obj.has(name) && !obj.get(name).isJsonNull()) ? obj.get(name).getAsInt() : def;
    }

    private static float getFloat(JsonObject obj, String name, float def) {
        if (obj == null || !obj.has(name) || obj.get(name).isJsonNull()) return def;
        JsonElement el = obj.get(name);
        if (el.isJsonPrimitive()) {
            try {
                return el.getAsFloat();
            } catch (Exception e) {
                return def;
            }
        } else if (el.isJsonObject()) {
            JsonObject o = el.getAsJsonObject();
            if (o.has("start") && o.get("start").isJsonPrimitive()) {
                try {
                    return o.get("start").getAsFloat();
                } catch (Exception ignored) {}
            }
        }
        return def;
    }

    private static boolean getBoolean(JsonObject obj, String name, boolean def) {
        return (obj.has(name) && !obj.get(name).isJsonNull()) ? obj.get(name).getAsBoolean() : def;
    }

    private static Easing parseEasing(String name, Easing def) {
        if (name == null) return def;
        try {
            return Easing.valueOf(name.toUpperCase());
        } catch (Exception e) {
            return def;
        }
    }

    private static float[] parseVec3(JsonElement elem, float[] def) {
        if (elem != null && elem.isJsonArray()) {
            JsonArray arr = elem.getAsJsonArray();
            float[] result = new float[3];
            result[0] = arr.size() > 0 ? arr.get(0).getAsFloat() : def[0];
            result[1] = arr.size() > 1 ? arr.get(1).getAsFloat() : def[1];
            result[2] = arr.size() > 2 ? arr.get(2).getAsFloat() : def[2];
            return result;
        }
        return def;
    }

    private static LightComponent parseLightComponent(JsonObject obj) {
        String shapeStr = getString(obj, "shape", "POINT").toUpperCase();
        LightComponent.LightShape shape;
        try {
            shape = LightComponent.LightShape.valueOf(shapeStr);
        } catch (Exception e) {
            shape = LightComponent.LightShape.POINT;
        }

        float radius = getFloat(obj, "radius", getFloat(obj, "range", 8.0f));
        float intensity = getFloat(obj, "intensity", 1.5f);

        LightComponent comp = new LightComponent(shape, radius, intensity);

        if (obj.has("color")) {
            float[] c = parseVec3(obj.get("color"), new float[]{1.0f, 0.85f, 0.40f});
            comp.setColor(c[0], c[1], c[2]);
        }
        if (obj.has("coreColor")) {
            float[] cc = parseVec3(obj.get("coreColor"), new float[]{1.0f, 1.0f, 1.0f});
            comp.setCoreColor(cc[0], cc[1], cc[2]);
        }

        comp.setFalloffPower(getFloat(obj, "falloffPower", getFloat(obj, "falloff", 2.0f)));
        comp.setSpotAngle(getFloat(obj, "spotAngle", getFloat(obj, "angle", 45.0f)));
        comp.setPenumbra(getFloat(obj, "penumbra", 0.5f));

        if (obj.has("offset")) {
            float[] off = parseVec3(obj.get("offset"), new float[]{0.0f, 0.0f, 0.0f});
            comp.setOffset(off[0], off[1], off[2]);
        } else {
            comp.setOffset(getFloat(obj, "offsetX", 0.0f),
                           getFloat(obj, "offsetY", 0.0f),
                           getFloat(obj, "offsetZ", 0.0f));
        }

        if (obj.has("rotation")) {
            float[] rot = parseVec3(obj.get("rotation"), new float[]{0.0f, 0.0f, 0.0f});
            comp.setRotation(rot[0], rot[1], rot[2]);
        } else if (obj.has("initialRotation")) {
            float[] rot = parseVec3(obj.get("initialRotation"), new float[]{0.0f, 0.0f, 0.0f});
            comp.setRotation(rot[0], rot[1], rot[2]);
        }

        comp.setVolumetricNoise(getBoolean(obj, "volumetricNoise", true));
        comp.setNoiseSpeed(getFloat(obj, "noiseSpeed", 1.5f));
        comp.setNoiseScale(getFloat(obj, "noiseScale", getFloat(obj, "noiseFrequency", 3.0f)));
        comp.setDustDensity(getFloat(obj, "dustDensity", 0.4f));

        comp.setFlicker(getBoolean(obj, "flicker", false));
        comp.setFlickerSpeed(getFloat(obj, "flickerSpeed", 12.0f));
        comp.setFlickerIntensity(getFloat(obj, "flickerIntensity", 0.20f));

        comp.setPulsing(getBoolean(obj, "pulsing", false));
        comp.setPulseSpeed(getFloat(obj, "pulseSpeed", 3.0f));
        comp.setPulseIntensity(getFloat(obj, "pulseIntensity", 0.30f));

        comp.setBloomMultiplier(getFloat(obj, "bloomMultiplier", 2.0f));
        comp.setIlluminateWorld(getBoolean(obj, "illuminateWorld", true));
        comp.setBlockLightLevel(getInt(obj, "blockLightLevel", 15));

        // Shader-Grade Advanced Features
        comp.setLightTerrain(getBoolean(obj, "lightTerrain", true));
        comp.setTerrainIntensity(getFloat(obj, "terrainIntensity", 1.0f));
        comp.setTerrainSpecular(getFloat(obj, "terrainSpecular", 0.35f));

        comp.setGodRays(getBoolean(obj, "godRays", true));
        comp.setGodRayDensity(getFloat(obj, "godRayDensity", 0.8f));
        comp.setGodRayDecay(getFloat(obj, "godRayDecay", 0.95f));
        comp.setGodRayWeight(getFloat(obj, "godRayWeight", 0.35f));

        comp.setLensFlare(getBoolean(obj, "lensFlare", true));
        comp.setFlareSize(getFloat(obj, "flareSize", 1.2f));
        comp.setFlareIntensity(getFloat(obj, "flareIntensity", 1.0f));
        comp.setShaderName(getString(obj, "shader", getString(obj, "shaderName", "volumetric_sacred")));
        if (obj.has("secondaryColor")) {
            float[] sc = parseVec3(obj.get("secondaryColor"), new float[]{0.2f, 0.9f, 0.8f});
            comp.setSecondaryColor(sc[0], sc[1], sc[2]);
        } else if (obj.has("colorB")) {
            float[] sc = parseVec3(obj.get("colorB"), new float[]{0.2f, 0.9f, 0.8f});
            comp.setSecondaryColor(sc[0], sc[1], sc[2]);
        }

        comp.setCoreWidth(getFloat(obj, "coreWidth", 0.35f));
        comp.setBeamSpeed(getFloat(obj, "beamSpeed", getFloat(obj, "scrollSpeed", 1.0f)));
        comp.setPatternDensity(getFloat(obj, "patternDensity", 3.0f));
        comp.setFringeStrength(getFloat(obj, "fringeStrength", 0.5f));

        return comp;
    }
}
