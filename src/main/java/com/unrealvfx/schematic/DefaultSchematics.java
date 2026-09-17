package com.unrealvfx.schematic;

/**
 * Contiene i modelli JSON predefiniti di alta qualità generati automaticamente
 * nella cartella config/unrealvfx/effects/.
 */
public class DefaultSchematics {

    public static final String SOLAR_FLARE = "{\n" +
        "  \"id\": \"solar_flare\",\n" +
        "  \"duration\": 50,\n" +
        "  \"loop\": false,\n" +
        "  \"trackingRadius\": 128.0,\n" +
        "  \"components\": [\n" +
        "    {\n" +
        "      \"type\": \"camera_shake\",\n" +
        "      \"trauma\": 0.75,\n" +
        "      \"fovKick\": 12.0,\n" +
        "      \"continuous\": false\n" +
        "    },\n" +
        "    {\n" +
        "      \"type\": \"distortion\",\n" +
        "      \"startRadius\": 0.5,\n" +
        "      \"endRadius\": 14.0,\n" +
        "      \"strength\": 0.07\n" +
        "    },\n" +
        "    {\n" +
        "      \"type\": \"shockwave\",\n" +
        "      \"radius\": { \"start\": 0.5, \"end\": 12.0, \"easing\": \"OUT_QUAD\" },\n" +
        "      \"alpha\": { \"start\": 1.0, \"end\": 0.0, \"easing\": \"IN_QUAD\" },\n" +
        "      \"color\": [1.0, 0.75, 0.2],\n" +
        "      \"offsetY\": 0.05\n" +
        "    },\n" +
        "    {\n" +
        "      \"type\": \"mesh\",\n" +
        "      \"meshType\": \"SPHERE\",\n" +
        "      \"shader\": \"energy_core\",\n" +
        "      \"scale\": { \"start\": 0.4, \"end\": 7.0, \"easing\": \"OUT_EXPO\" },\n" +
        "      \"alpha\": { \"start\": 1.0, \"end\": 0.0, \"easing\": \"IN_QUAD\" },\n" +
        "      \"color\": {\n" +
        "        \"start\": [1.0, 0.95, 0.4],\n" +
        "        \"end\": [1.0, 0.25, 0.05],\n" +
        "        \"easing\": \"IN_QUAD\"\n" +
        "      },\n" +
        "      \"rotationSpeed\": [3.0, 2.0, 0.0],\n" +
        "      \"blendMode\": \"ADDITIVE\"\n" +
        "    },\n" +
        "    {\n" +
        "      \"type\": \"mesh\",\n" +
        "      \"meshType\": \"RING\",\n" +
        "      \"shader\": \"energy_disc\",\n" +
        "      \"scale\": { \"start\": 0.8, \"end\": 9.5, \"easing\": \"OUT_EXPO\" },\n" +
        "      \"alpha\": { \"start\": 1.0, \"end\": 0.0, \"easing\": \"IN_QUAD\" },\n" +
        "      \"color\": [1.0, 0.65, 0.15],\n" +
        "      \"initialRotation\": [0.0, 35.0, 15.0],\n" +
        "      \"rotationSpeed\": [8.0, 0.0, 0.0],\n" +
        "      \"blendMode\": \"ADDITIVE\"\n" +
        "    },\n" +
        "    {\n" +
        "      \"type\": \"lightning\",\n" +
        "      \"arcCount\": 10,\n" +
        "      \"radius\": 5.5,\n" +
        "      \"height\": 4.0,\n" +
        "      \"color\": [1.0, 0.85, 0.25],\n" +
        "      \"width\": 0.10,\n" +
        "      \"spherical\": true\n" +
        "    }\n" +
        "  ]\n" +
        "}";

    public static final String COSMIC_VORTEX = "{\n" +
        "  \"id\": \"cosmic_vortex\",\n" +
        "  \"duration\": 120,\n" +
        "  \"loop\": true,\n" +
        "  \"trackingRadius\": 160.0,\n" +
        "  \"components\": [\n" +
        "    {\n" +
        "      \"type\": \"camera_shake\",\n" +
        "      \"trauma\": 0.05,\n" +
        "      \"fovKick\": 0.0,\n" +
        "      \"continuous\": true\n" +
        "    },\n" +
        "    {\n" +
        "      \"type\": \"gravitational_pull\",\n" +
        "      \"radius\": 12.0,\n" +
        "      \"pullStrength\": 0.4,\n" +
        "      \"swirlStrength\": 0.25,\n" +
        "      \"damageAtHorizon\": true\n" +
        "    },\n" +
        "    {\n" +
        "      \"type\": \"distortion\",\n" +
        "      \"startRadius\": 2.0,\n" +
        "      \"endRadius\": 4.0,\n" +
        "      \"strength\": 0.05\n" +
        "    },\n" +
        "    {\n" +
        "      \"type\": \"mesh\",\n" +
        "      \"meshType\": \"SPHERE\",\n" +
        "      \"shader\": \"hakai_core\",\n" +
        "      \"scale\": 1.6,\n" +
        "      \"color\": [0.05, 0.0, 0.1],\n" +
        "      \"rotationSpeed\": [0.5, 0.3, 0.2],\n" +
        "      \"blendMode\": \"NORMAL\",\n" +
        "      \"depthMask\": true\n" +
        "    },\n" +
        "    {\n" +
        "      \"type\": \"mesh\",\n" +
        "      \"meshType\": \"DISC\",\n" +
        "      \"shader\": \"accretion_disk\",\n" +
        "      \"scale\": 4.5,\n" +
        "      \"color\": [0.75, 0.2, 1.0],\n" +
        "      \"initialRotation\": [0.0, 18.0, 0.0],\n" +
        "      \"rotationSpeed\": [4.0, 0.0, 0.0],\n" +
        "      \"blendMode\": \"ADDITIVE\"\n" +
        "    },\n" +
        "    {\n" +
        "      \"type\": \"particle\",\n" +
        "      \"particleType\": \"REALITY_CUBE\",\n" +
        "      \"spawnRate\": 6,\n" +
        "      \"emissionRadius\": 5.0,\n" +
        "      \"baseSpeed\": 0.8,\n" +
        "      \"color\": [0.85, 0.3, 0.95],\n" +
        "      \"suction\": true,\n" +
        "      \"swirl\": true\n" +
        "    }\n" +
        "  ]\n" +
        "}";

    public static final String DIVINE_SHIELD = "{\n" +
        "  \"id\": \"divine_shield\",\n" +
        "  \"duration\": 80,\n" +
        "  \"loop\": true,\n" +
        "  \"trackingRadius\": 96.0,\n" +
        "  \"components\": [\n" +
        "    {\n" +
        "      \"type\": \"mesh\",\n" +
        "      \"meshType\": \"DOME\",\n" +
        "      \"shader\": \"energy_barrier\",\n" +
        "      \"scale\": { \"start\": 0.5, \"end\": 3.0, \"easing\": \"OUT_QUAD\" },\n" +
        "      \"alpha\": { \"start\": 0.8, \"end\": 0.9, \"easing\": \"LINEAR\" },\n" +
        "      \"color\": [0.3, 0.95, 0.6],\n" +
        "      \"blendMode\": \"ADDITIVE\"\n" +
        "    },\n" +
        "    {\n" +
        "      \"type\": \"mesh\",\n" +
        "      \"meshType\": \"THIN_RING\",\n" +
        "      \"shader\": \"energy_disc\",\n" +
        "      \"scale\": 3.5,\n" +
        "      \"color\": [1.0, 0.9, 0.3],\n" +
        "      \"initialRotation\": [0.0, 25.0, 15.0],\n" +
        "      \"rotationSpeed\": [5.0, 0.0, 0.0],\n" +
        "      \"blendMode\": \"ADDITIVE\"\n" +
        "    },\n" +
        "    {\n" +
        "      \"type\": \"floating_debris\",\n" +
        "      \"count\": 5,\n" +
        "      \"radius\": 2.2\n" +
        "    }\n" +
        "  ]\n" +
        "}";

    public static final String ENERGY_SLASH = "{\n" +
        "  \"id\": \"energy_slash\",\n" +
        "  \"duration\": 30,\n" +
        "  \"loop\": false,\n" +
        "  \"trackingRadius\": 96.0,\n" +
        "  \"components\": [\n" +
        "    {\n" +
        "      \"type\": \"distortion\",\n" +
        "      \"startRadius\": 0.5,\n" +
        "      \"endRadius\": 3.0,\n" +
        "      \"strength\": 0.06\n" +
        "    },\n" +
        "    {\n" +
        "      \"type\": \"mesh\",\n" +
        "      \"meshType\": \"DISC\",\n" +
        "      \"shader\": \"energy_disc\",\n" +
        "      \"scale\": 2.2,\n" +
        "      \"color\": [0.15, 0.85, 1.0],\n" +
        "      \"initialRotation\": [90.0, 0.0, 0.0],\n" +
        "      \"rotationSpeed\": [0.0, 20.0, 0.0],\n" +
        "      \"blendMode\": \"ADDITIVE\"\n" +
        "    },\n" +
        "    {\n" +
        "      \"type\": \"particle\",\n" +
        "      \"particleType\": \"NEEDLE\",\n" +
        "      \"spawnRate\": 6,\n" +
        "      \"emissionRadius\": 0.8,\n" +
        "      \"baseSpeed\": 0.5,\n" +
        "      \"stretchFactor\": 3.5,\n" +
        "      \"color\": [0.4, 0.9, 1.0],\n" +
        "      \"suction\": false,\n" +
        "      \"swirl\": false\n" +
        "    }\n" +
        "  ]\n" +
        "}";

    public static final String CUBE_LASER = "{\n" +
        "  \"id\": \"cube_laser\",\n" +
        "  \"duration\": 70,\n" +
        "  \"loop\": false,\n" +
        "  \"trackingRadius\": 140.0,\n" +
        "  \"components\": [\n" +
        "    {\n" +
        "      \"type\": \"camera_shake\",\n" +
        "      \"trauma\": 0.35,\n" +
        "      \"fovKick\": 4.0,\n" +
        "      \"continuous\": false\n" +
        "    },\n" +
        "    {\n" +
        "      \"type\": \"distortion\",\n" +
        "      \"startRadius\": 0.4,\n" +
        "      \"endRadius\": 3.5,\n" +
        "      \"strength\": 0.08\n" +
        "    },\n" +
        "    {\n" +
        "      \"type\": \"shockwave\",\n" +
        "      \"radius\": { \"start\": 0.2, \"end\": 4.5, \"easing\": \"OUT_QUAD\" },\n" +
        "      \"alpha\": { \"start\": 1.0, \"end\": 0.0, \"easing\": \"IN_QUAD\" },\n" +
        "      \"color\": [0.1, 0.85, 1.0],\n" +
        "      \"offsetY\": 0.0\n" +
        "    },\n" +
        "    {\n" +
        "      \"type\": \"mesh\",\n" +
        "      \"meshType\": \"CUBE\",\n" +
        "      \"shader\": \"universal_energy\",\n" +
        "      \"scale\": { \"start\": [0.05, 0.05, 0.2], \"end\": [0.4, 0.4, 1.6], \"easing\": \"OUT_EXPO\" },\n" +
        "      \"alpha\": { \"start\": 1.0, \"end\": 0.0, \"easing\": \"IN_QUAD\" },\n" +
        "      \"color\": [0.1, 0.8, 1.0],\n" +
        "      \"hotCoreColor\": [1.0, 1.0, 1.0],\n" +
        "      \"rimColor\": [0.0, 0.4, 1.0],\n" +
        "      \"rimPower\": 2.5,\n" +
        "      \"noiseSpeed\": 1.8,\n" +
        "      \"noiseScale\": 2.0,\n" +
        "      \"bloomMultiplier\": 2.2,\n" +
        "      \"blendMode\": \"ADDITIVE\",\n" +
        "      \"array\": {\n" +
        "        \"count\": 20,\n" +
        "        \"stepPos\": [0.0, 0.0, 1.4],\n" +
        "        \"stepRot\": [0.0, 0.0, 6.0],\n" +
        "        \"stepScale\": [0.0, 0.0, 0.0],\n" +
        "        \"phaseDelay\": 0.6\n" +
        "      }\n" +
        "    },\n" +
        "    {\n" +
        "      \"type\": \"mesh\",\n" +
        "      \"meshType\": \"CUBE\",\n" +
        "      \"shader\": \"universal_energy\",\n" +
        "      \"scale\": [0.16, 0.16, 0.16],\n" +
        "      \"alpha\": { \"start\": 1.0, \"end\": 0.0, \"easing\": \"IN_QUAD\" },\n" +
        "      \"color\": [0.35, 0.95, 1.0],\n" +
        "      \"hotCoreColor\": [1.0, 1.0, 1.0],\n" +
        "      \"rimColor\": [0.1, 0.5, 1.0],\n" +
        "      \"rimPower\": 2.0,\n" +
        "      \"bloomMultiplier\": 1.8,\n" +
        "      \"pivot\": [0.85, 0.0, 0.0],\n" +
        "      \"rotationSpeed\": [0.0, 0.0, 12.0],\n" +
        "      \"blendMode\": \"ADDITIVE\",\n" +
        "      \"array\": {\n" +
        "        \"count\": 24,\n" +
        "        \"stepPos\": [0.0, 0.0, 1.15],\n" +
        "        \"stepRot\": [0.0, 0.0, 25.0],\n" +
        "        \"stepScale\": [0.0, 0.0, 0.0],\n" +
        "        \"phaseDelay\": 0.5\n" +
        "      }\n" +
        "    }\n" +
        "  ]\n" +
        "}";

    public static final String GIGANTIC_METEOR = "{\n" +
        "  \"id\": \"gigantic_meteor\",\n" +
        "  \"duration\": 120,\n" +
        "  \"loop\": true,\n" +
        "  \"trackingRadius\": 256.0,\n" +
        "  \"components\": [\n" +
        "    {\n" +
        "      \"type\": \"camera_shake\",\n" +
        "      \"trauma\": 0.20,\n" +
        "      \"continuous\": true\n" +
        "    },\n" +
        "    {\n" +
        "      \"type\": \"mesh\",\n" +
        "      \"meshType\": \"AURA_CAPSULE\",\n" +
        "      \"shader\": \"aura_cel\",\n" +
        "      \"scale\": [1.4, 2.3, 1.4],\n" +
        "      \"offset\": [0.0, 1.0, 0.0],\n" +
        "      \"color\": [0.88, 0.18, 1.0],\n" +
        "      \"hotCoreColor\": [1.0, 0.95, 0.8],\n" +
        "      \"rimColor\": [0.45, 0.08, 0.65],\n" +
        "      \"distortionStrength\": 0.14,\n" +
        "      \"celBands\": 3.0,\n" +
        "      \"bloomMultiplier\": 1.6,\n" +
        "      \"blendMode\": \"ADDITIVE\"\n" +
        "    },\n" +
        "    {\n" +
        "      \"type\": \"mesh\",\n" +
        "      \"meshType\": \"SPHERE\",\n" +
        "      \"shader\": \"celestial_void\",\n" +
        "      \"scale\": 18.0,\n" +
        "      \"offset\": [0.0, 24.0, -16.0],\n" +
        "      \"color\": [0.01, 0.002, 0.025],\n" +
        "      \"hotCoreColor\": [0.005, 0.001, 0.015],\n" +
        "      \"rimColor\": [0.70, 0.22, 1.0],\n" +
        "      \"rimPower\": 2.8,\n" +
        "      \"noiseSpeed\": 0.8,\n" +
        "      \"noiseScale\": 3.5,\n" +
        "      \"filamentStrength\": 1.1,\n" +
        "      \"bloomMultiplier\": 2.2,\n" +
        "      \"blendMode\": \"NORMAL\",\n" +
        "      \"depthMask\": true\n" +
        "    },\n" +
        "    {\n" +
        "      \"type\": \"mesh\",\n" +
        "      \"meshType\": \"SPHERE\",\n" +
        "      \"shader\": \"universal_energy\",\n" +
        "      \"scale\": 18.6,\n" +
        "      \"offset\": [0.0, 24.0, -16.0],\n" +
        "      \"color\": [0.45, 0.15, 0.95],\n" +
        "      \"hotCoreColor\": [0.0, 0.0, 0.0],\n" +
        "      \"rimColor\": [0.25, 0.80, 1.0],\n" +
        "      \"rimPower\": 3.8,\n" +
        "      \"bloomMultiplier\": 2.5,\n" +
        "      \"invertedCore\": true,\n" +
        "      \"blendMode\": \"ADDITIVE\"\n" +
        "    },\n" +
        "    {\n" +
        "      \"type\": \"mesh\",\n" +
        "      \"meshType\": \"RING\",\n" +
        "      \"shader\": \"energy_disc\",\n" +
        "      \"scale\": 25.0,\n" +
        "      \"offset\": [0.0, 24.0, -16.0],\n" +
        "      \"initialRotation\": [75.0, 15.0, 0.0],\n" +
"      \"rotationSpeed\": [0.0, 2.0, 0.0],\n" +
        "      \"color\": [0.65, 0.25, 1.0],\n" +
        "      \"blendMode\": \"ADDITIVE\"\n" +
        "    },\n" +
        "    {\n" +
        "      \"type\": \"mesh\",\n" +
        "      \"meshType\": \"SPHERE\",\n" +
        "      \"shader\": \"universal_energy\",\n" +
        "      \"scale\": [0.40, 0.40, 0.40],\n" +
        "      \"offset\": [0.0, 2.2, 0.0],\n" +
        "      \"color\": [0.90, 0.95, 1.0],\n" +
        "      \"hotCoreColor\": [1.0, 1.0, 1.0],\n" +
        "      \"rimColor\": [0.4, 0.7, 1.0],\n" +
        "      \"bloomMultiplier\": 2.5,\n" +
        "      \"blendMode\": \"ADDITIVE\",\n" +
        "      \"array\": {\n" +
        "        \"count\": 28,\n" +
        "        \"stepPos\": [0.0, 1.15, -0.85],\n" +
        "        \"stepRot\": [0.0, 0.0, 0.0],\n" +
        "        \"phaseDelay\": 0.35\n" +
        "      }\n" +
        "    },\n" +
        "    {\n" +
        "      \"type\": \"lightning\",\n" +
        "      \"mode\": \"SURFACE_SPHERE\",\n" +
        "      \"arcCount\": 18,\n" +
        "      \"radius\": 18.2,\n" +
        "      \"offset\": [0.0, 24.0, -16.0],\n" +
        "      \"color\": [0.75, 0.30, 1.0],\n" +
        "      \"coreColor\": [0.55, 0.90, 1.0],\n" +
        "      \"width\": 0.14,\n" +
        "      \"roughness\": 0.30,\n" +
        "      \"branchForks\": 2,\n" +
        "      \"forkChance\": 0.40,\n" +
        "      \"glowMultiplier\": 2.2,\n" +
        "      \"spherical\": true\n" +
        "    }\n" +
        "  ]\n" +
        "}";

    public static final String BEAM_CANNON = "{\n" +
        "  \"id\": \"beam_cannon\",\n" +
        "  \"duration\": 80,\n" +
        "  \"loop\": true,\n" +
        "  \"trackingRadius\": 160.0,\n" +
        "  \"components\": [\n" +
        "    {\n" +
        "      \"type\": \"camera_shake\",\n" +
        "      \"trauma\": 0.45,\n" +
        "      \"fovKick\": 6.0,\n" +
        "      \"continuous\": false\n" +
        "    },\n" +
        "    {\n" +
        "      \"type\": \"impact_frame\",\n" +
        "      \"triggerTick\": 14,\n" +
        "      \"durationTicks\": 2,\n" +
        "      \"impactType\": \"MANGA_BW\"\n" +
        "    },\n" +
        "    {\n" +
        "      \"type\": \"shockwave\",\n" +
        "      \"radius\": { \"start\": 0.4, \"end\": 8.0, \"easing\": \"OUT_QUAD\" },\n" +
        "      \"alpha\": { \"start\": 1.0, \"end\": 0.0, \"easing\": \"IN_QUAD\" },\n" +
        "      \"color\": [0.2, 0.8, 1.0],\n" +
        "      \"offsetY\": 0.0\n" +
        "    },\n" +
        "    {\n" +
        "      \"type\": \"beam\",\n" +
        "      \"length\": 28.0,\n" +
        "      \"width\": 0.70,\n" +
        "      \"chargeTicks\": 14,\n" +
        "      \"color\": [0.15, 0.85, 1.0],\n" +
        "      \"hasHeadOrb\": true,\n" +
        "      \"hasTailOrb\": true,\n" +
        "      \"hasHelicalRibbons\": true,\n" +
        "      \"ribbonRadius\": 0.95,\n" +
        "      \"ribbonSpeed\": 8.0\n" +
        "    }\n" +
        "  ]\n" +
        "}";

    public static final String REALITY_CUBE_VORTEX = "{\n" +
        "  \"id\": \"reality_cube_vortex\",\n" +
        "  \"duration\": 80,\n" +
        "  \"loop\": true,\n" +
        "  \"trackingRadius\": 160.0,\n" +
        "  \"components\": [\n" +
        "    {\n" +
        "      \"type\": \"camera_shake\",\n" +
        "      \"trauma\": 0.35,\n" +
        "      \"fovKick\": 4.0,\n" +
        "      \"continuous\": false\n" +
        "    },\n" +
        "    {\n" +
        "      \"type\": \"gravitational_pull\",\n" +
        "      \"radius\": 14.0,\n" +
        "      \"pullStrength\": 0.6,\n" +
        "      \"swirlStrength\": 0.35\n" +
        "    },\n" +
        "    {\n" +
        "      \"type\": \"mesh\",\n" +
        "      \"meshType\": \"SPHERE\",\n" +
        "      \"shader\": \"celestial_void\",\n" +
        "      \"scale\": 4.0,\n" +
        "      \"offset\": [0.0, 4.0, 0.0],\n" +
        "      \"color\": [0.01, 0.002, 0.03],\n" +
        "      \"hotCoreColor\": [0.0, 0.0, 0.0],\n" +
        "      \"rimColor\": [0.95, 0.1, 0.8],\n" +
        "      \"rimPower\": 3.2,\n" +
        "      \"bloomMultiplier\": 2.5,\n" +
        "      \"invertedCore\": true,\n" +
        "      \"steppedFps\": 12.0\n" +
        "    },\n" +
        "    {\n" +
        "      \"type\": \"floating_debris\",\n" +
        "      \"debrisType\": \"REALITY_CUBE\",\n" +
        "      \"count\": 32,\n" +
        "      \"radius\": 7.0,\n" +
        "      \"color\": [0.9, 0.2, 1.0],\n" +
        "      \"anticipationLevitation\": true,\n" +
        "      \"centripetalSuction\": true,\n" +
        "      \"explosiveDispersal\": true\n" +
        "    },\n" +
        "    {\n" +
        "      \"type\": \"impact_frame\",\n" +
        "      \"triggerTick\": 65,\n" +
        "      \"durationTicks\": 2,\n" +
        "      \"impactType\": \"HAKAI_PURPLE\"\n" +
        "    }\n" +
        "  ]\n" +
        "}";

    public static final String AURA_TEST_PURPLE = "{\n" +
        "  \"id\": \"aura_test_purple\",\n" +
        "  \"duration\": 120,\n" +
        "  \"loop\": true,\n" +
        "  \"trackingRadius\": 256.0,\n" +
        "  \"components\": [\n" +
        "    {\n" +
        "      \"type\": \"camera_shake\",\n" +
        "      \"trauma\": 0.16,\n" +
        "      \"continuous\": true\n" +
        "    },\n" +
        "    {\n" +
        "      \"type\": \"shockwave\",\n" +
        "      \"offsetY\": 0.05,\n" +
        "      \"radius\": { \"start\": 0.6, \"end\": 2.4, \"easing\": \"OUT_QUAD\" },\n" +
        "      \"alpha\": { \"start\": 0.9, \"end\": 0.0, \"easing\": \"IN_QUAD\" },\n" +
        "      \"color\": [0.75, 0.15, 1.0]\n" +
        "    },\n" +
        "    {\n" +
        "      \"type\": \"mesh\",\n" +
        "      \"meshType\": \"AURA_CAPSULE\",\n" +
        "      \"shader\": \"aura_cel\",\n" +
        "      \"scale\": [1.5, 2.5, 1.5],\n" +
        "      \"offset\": [0.0, 1.0, 0.0],\n" +
        "      \"color\": [0.88, 0.18, 1.0],\n" +
        "      \"hotCoreColor\": [1.0, 0.95, 0.8],\n" +
        "      \"rimColor\": [0.45, 0.08, 0.65],\n" +
        "      \"distortionStrength\": 0.18,\n" +
        "      \"celBands\": 3.0,\n" +
        "      \"noiseSpeed\": 2.2,\n" +
        "      \"bloomMultiplier\": 1.6,\n" +
        "      \"blendMode\": \"ADDITIVE\",\n" +
        "      \"rotationSpeed\": [2.5, 0.0, 0.0]\n" +
        "    },\n" +
        "    {\n" +
        "      \"type\": \"particle\",\n" +
        "      \"particleType\": \"KI_EMBER\",\n" +
        "      \"spawnRate\": 8,\n" +
        "      \"emissionRadius\": 1.4,\n" +
        "      \"baseSpeed\": 0.25,\n" +
        "      \"color\": [0.95, 0.45, 1.0],\n" +
        "      \"suction\": false,\n" +
        "      \"swirl\": true\n" +
        "    },\n" +
        "    {\n" +
        "      \"type\": \"distortion\",\n" +
        "      \"startRadius\": 0.6,\n" +
        "      \"endRadius\": 3.2,\n" +
        "      \"strength\": 0.04\n" +
        "    }\n" +
        "  ]\n" +
        "}";

    public static final String SUPERNOVA_HEAT_HAZE = "{\n" +
        "  \"id\": \"supernova_heat_haze\",\n" +
        "  \"duration\": 80,\n" +
        "  \"loop\": true,\n" +
        "  \"trackingRadius\": 160.0,\n" +
        "  \"components\": [\n" +
        "    {\n" +
        "      \"type\": \"camera_shake\",\n" +
        "      \"trauma\": 0.18,\n" +
        "      \"fovKick\": 2.0,\n" +
        "      \"continuous\": true\n" +
        "    },\n" +
        "    {\n" +
        "      \"type\": \"heat_distortion\",\n" +
        "      \"shape\": \"SPHERE\",\n" +
        "      \"strength\": 0.08,\n" +
        "      \"centerFalloff\": 2.0,\n" +
        "      \"noiseSpeed\": 2.8,\n" +
        "      \"noiseFrequency\": 4.5,\n" +
        "      \"scale\": [5.0, 5.0, 5.0],\n" +
        "      \"offset\": [0.0, 2.5, 0.0],\n" +
        "      \"tintColor\": [1.0, 0.65, 0.25],\n" +
        "      \"tintAlpha\": 0.0\n" +
        "    },\n" +
        "    {\n" +
        "      \"type\": \"shockwave\",\n" +
        "      \"radius\": { \"start\": 0.5, \"end\": 8.0, \"easing\": \"OUT_QUAD\" },\n" +
        "      \"alpha\": { \"start\": 1.0, \"end\": 0.0, \"easing\": \"IN_QUAD\" },\n" +
        "      \"color\": [1.0, 0.6, 0.15],\n" +
        "      \"offsetY\": 0.05\n" +
        "    },\n" +
        "    {\n" +
        "      \"type\": \"mesh\",\n" +
        "      \"meshType\": \"SPHERE\",\n" +
        "      \"shader\": \"energy_core\",\n" +
        "      \"scale\": 2.2,\n" +
        "      \"offset\": [0.0, 2.5, 0.0],\n" +
        "      \"color\": [1.0, 0.75, 0.2],\n" +
        "      \"hotCoreColor\": [1.0, 1.0, 0.9],\n" +
        "      \"rimColor\": [1.0, 0.35, 0.05],\n" +
        "      \"rimPower\": 2.2,\n" +
        "      \"bloomMultiplier\": 2.4,\n" +
        "      \"blendMode\": \"ADDITIVE\"\n" +
        "    },\n" +
        "    {\n" +
        "      \"type\": \"particle\",\n" +
        "      \"particleType\": \"KI_EMBER\",\n" +
        "      \"spawnRate\": 10,\n" +
        "      \"emissionRadius\": 2.8,\n" +
        "      \"baseSpeed\": 0.45,\n" +
        "      \"color\": [1.0, 0.7, 0.2],\n" +
        "      \"suction\": false,\n" +
        "      \"swirl\": true,\n" +
        "      \"offset\": [0.0, 2.5, 0.0]\n" +
        "    }\n" +
        "  ]\n" +
        "}";

    public static final String HEAT_MIRAGE_ZONE = "{\n" +
        "  \"id\": \"heat_mirage_zone\",\n" +
        "  \"duration\": 100,\n" +
        "  \"loop\": true,\n" +
        "  \"trackingRadius\": 96.0,\n" +
        "  \"components\": [\n" +
        "    {\n" +
        "      \"type\": \"heat_distortion\",\n" +
        "      \"shape\": \"CYLINDER\",\n" +
        "      \"strength\": 0.08,\n" +
        "      \"centerFalloff\": 1.8,\n" +
        "      \"noiseSpeed\": 2.5,\n" +
        "      \"noiseFrequency\": 4.5,\n" +
        "      \"scale\": [4.0, 3.5, 4.0],\n" +
        "      \"offset\": [0.0, 1.75, 0.0],\n" +
        "      \"tintColor\": [1.0, 0.65, 0.25],\n" +
        "      \"tintAlpha\": 0.0\n" +
        "    }\n" +
        "  ]\n" +
        "}";

    public static final String DIVINE_SANCTUARY_LIGHT = "{\n" +
        "  \"id\": \"divine_sanctuary_light\",\n" +
        "  \"duration\": 120,\n" +
        "  \"loop\": true,\n" +
        "  \"trackingRadius\": 128.0,\n" +
        "  \"components\": [\n" +
        "    {\n" +
        "      \"type\": \"camera_shake\",\n" +
        "      \"trauma\": 0.05,\n" +
        "      \"fovKick\": 1.0,\n" +
        "      \"continuous\": true\n" +
        "    },\n" +
        "    {\n" +
        "      \"type\": \"shockwave\",\n" +
        "      \"radius\": { \"start\": 0.5, \"end\": 7.5, \"easing\": \"OUT_QUAD\" },\n" +
        "      \"alpha\": { \"start\": 0.8, \"end\": 0.0, \"easing\": \"IN_QUAD\" },\n" +
        "      \"color\": [1.0, 0.85, 0.35],\n" +
        "      \"offsetY\": 0.05\n" +
        "    },\n" +
        "    {\n" +
        "      \"type\": \"light\",\n" +
        "      \"shape\": \"SPOT\",\n" +
        "      \"offset\": [0.0, 7.0, 0.0],\n" +
        "      \"radius\": 8.0,\n" +
        "      \"spotAngle\": 42.0,\n" +
        "      \"penumbra\": 0.65,\n" +
        "      \"color\": [1.0, 0.85, 0.35],\n" +
        "      \"coreColor\": [1.0, 1.0, 1.0],\n" +
        "      \"intensity\": 1.2,\n" +
        "      \"falloffPower\": 1.8,\n" +
        "      \"volumetricNoise\": true,\n" +
        "      \"noiseSpeed\": 1.2,\n" +
        "      \"dustDensity\": 0.6,\n" +
        "      \"pulsing\": true,\n" +
        "      \"pulseSpeed\": 2.5,\n" +
        "      \"pulseIntensity\": 0.25,\n" +
        "      \"bloomMultiplier\": 1.4,\n" +
        "      \"illuminateWorld\": true,\n" +
        "      \"blockLightLevel\": 15,\n" +
        "      \"lightTerrain\": true,\n" +
        "      \"terrainIntensity\": 0.45,\n" +
        "      \"terrainSpecular\": 0.35,\n" +
        "      \"godRays\": true,\n" +
        "      \"godRayDensity\": 0.65,\n" +
        "      \"godRayDecay\": 0.93,\n" +
        "      \"godRayWeight\": 0.025,\n" +
        "      \"lensFlare\": true,\n" +
        "      \"flareSize\": 0.75,\n" +
        "      \"flareIntensity\": 0.35\n" +
        "    },\n" +
        "    {\n" +
        "      \"type\": \"light\",\n" +
        "      \"shape\": \"POINT\",\n" +
        "      \"offset\": [0.0, 7.0, 0.0],\n" +
        "      \"radius\": 3.5,\n" +
        "      \"intensity\": 0.5,\n" +
        "      \"color\": [1.0, 0.70, 0.20],\n" +
        "      \"coreColor\": [1.0, 1.0, 0.95],\n" +
        "      \"bloomMultiplier\": 1.2,\n" +
        "      \"pulsing\": true,\n" +
        "      \"pulseSpeed\": 2.5,\n" +
        "      \"pulseIntensity\": 0.2,\n" +
        "      \"lightTerrain\": false,\n" +
        "      \"godRays\": false,\n" +
        "      \"lensFlare\": false\n" +
        "    },\n" +
        "    {\n" +
        "      \"type\": \"light\",\n" +
        "      \"shape\": \"AREA_DISC\",\n" +
        "      \"offset\": [0.0, 0.05, 0.0],\n" +
        "      \"radius\": 4.5,\n" +
        "      \"color\": [1.0, 0.90, 0.50],\n" +
        "      \"coreColor\": [1.0, 1.0, 1.0],\n" +
        "      \"intensity\": 0.6,\n" +
        "      \"bloomMultiplier\": 1.2,\n" +
        "      \"falloffPower\": 2.0,\n" +
        "      \"lightTerrain\": false,\n" +
        "      \"godRays\": false,\n" +
        "      \"lensFlare\": false\n" +
        "    },\n" +
        "    {\n" +
        "      \"type\": \"particle\",\n" +
        "      \"particleType\": \"KI_EMBER\",\n" +
        "      \"spawnRate\": 8,\n" +
        "      \"emissionRadius\": 2.5,\n" +
        "      \"baseSpeed\": 0.35,\n" +
        "      \"color\": [1.0, 0.88, 0.45],\n" +
        "      \"suction\": false,\n" +
        "      \"swirl\": true,\n" +
        "      \"offset\": [0.0, 2.0, 0.0]\n" +
        "    }\n" +
        "  ]\n" +
        "}";

    public static final String CELESTIAL_JUDGMENT_PILLAR = "{\n" +
        "  \"id\": \"celestial_judgment_pillar\",\n" +
        "  \"duration\": 90,\n" +
        "  \"loop\": true,\n" +
        "  \"trackingRadius\": 160.0,\n" +
        "  \"components\": [\n" +
        "    {\n" +
        "      \"type\": \"camera_shake\",\n" +
        "      \"trauma\": 0.22,\n" +
        "      \"fovKick\": 2.0,\n" +
        "      \"continuous\": true\n" +
        "    },\n" +
        "    {\n" +
        "      \"type\": \"shockwave\",\n" +
        "      \"radius\": { \"start\": 0.5, \"end\": 6.5, \"easing\": \"OUT_QUAD\" },\n" +
        "      \"alpha\": { \"start\": 0.85, \"end\": 0.0, \"easing\": \"IN_QUAD\" },\n" +
        "      \"color\": [0.2, 0.9, 1.0],\n" +
        "      \"offsetY\": 0.05\n" +
        "    },\n" +
        "    {\n" +
        "      \"type\": \"light\",\n" +
        "      \"shape\": \"COLUMN\",\n" +
        "      \"shader\": \"celestial_beam\",\n" +
        "      \"offset\": [0.0, 0.0, 0.0],\n" +
        "      \"radius\": 3.5,\n" +
        "      \"intensity\": 1.8,\n" +
        "      \"color\": [0.15, 0.85, 1.0],\n" +
        "      \"secondaryColor\": [0.95, 0.35, 1.0],\n" +
        "      \"coreColor\": [1.0, 1.0, 1.0],\n" +
        "      \"coreWidth\": 0.42,\n" +
        "      \"beamSpeed\": 1.4,\n" +
        "      \"patternDensity\": 4.0,\n" +
        "      \"fringeStrength\": 0.85,\n" +
        "      \"bloomMultiplier\": 2.2,\n" +
        "      \"illuminateWorld\": true,\n" +
        "      \"blockLightLevel\": 15,\n" +
        "      \"lightTerrain\": true,\n" +
        "      \"terrainIntensity\": 0.5,\n" +
        "      \"terrainSpecular\": 0.4\n" +
        "    },\n" +
        "    {\n" +
        "      \"type\": \"particle\",\n" +
        "      \"particleType\": \"KI_EMBER\",\n" +
        "      \"spawnRate\": 12,\n" +
        "      \"emissionRadius\": 2.8,\n" +
        "      \"baseSpeed\": 0.4,\n" +
        "      \"color\": [0.2, 0.9, 1.0],\n" +
        "      \"suction\": false,\n" +
        "      \"swirl\": true,\n" +
        "      \"offset\": [0.0, 2.0, 0.0]\n" +
        "    }\n" +
        "  ]\n" +
        "}";

    public static final String CYBER_MATRIX_BEACON = "{\n" +
        "  \"id\": \"cyber_matrix_beacon\",\n" +
        "  \"duration\": 100,\n" +
        "  \"loop\": true,\n" +
        "  \"trackingRadius\": 140.0,\n" +
        "  \"components\": [\n" +
        "    {\n" +
        "      \"type\": \"light\",\n" +
        "      \"shape\": \"SPOT\",\n" +
        "      \"shader\": \"cyber_grid_shaft\",\n" +
        "      \"offset\": [0.0, 8.0, 0.0],\n" +
        "      \"radius\": 9.0,\n" +
        "      \"spotAngle\": 48.0,\n" +
        "      \"penumbra\": 0.5,\n" +
        "      \"intensity\": 1.6,\n" +
        "      \"color\": [0.0, 0.95, 1.0],\n" +
        "      \"secondaryColor\": [0.85, 0.1, 1.0],\n" +
        "      \"coreColor\": [0.9, 1.0, 1.0],\n" +
        "      \"coreWidth\": 0.32,\n" +
        "      \"beamSpeed\": 1.5,\n" +
        "      \"patternDensity\": 3.8,\n" +
        "      \"fringeStrength\": 1.2,\n" +
        "      \"bloomMultiplier\": 2.0,\n" +
        "      \"illuminateWorld\": true,\n" +
        "      \"blockLightLevel\": 15,\n" +
        "      \"lightTerrain\": true,\n" +
        "      \"terrainIntensity\": 0.55,\n" +
        "      \"godRays\": true,\n" +
        "      \"godRayDensity\": 0.7,\n" +
        "      \"godRayDecay\": 0.92,\n" +
        "      \"godRayWeight\": 0.025,\n" +
        "      \"lensFlare\": true,\n" +
        "      \"flareSize\": 0.7,\n" +
        "      \"flareIntensity\": 0.35\n" +
        "    },\n" +
        "    {\n" +
        "      \"type\": \"light\",\n" +
        "      \"shape\": \"AREA_DISC\",\n" +
        "      \"offset\": [0.0, 0.05, 0.0],\n" +
        "      \"radius\": 5.0,\n" +
        "      \"intensity\": 0.7,\n" +
        "      \"color\": [0.0, 0.8, 1.0],\n" +
        "      \"coreColor\": [1.0, 1.0, 1.0],\n" +
        "      \"bloomMultiplier\": 1.3\n" +
        "    },\n" +
        "    {\n" +
        "      \"type\": \"particle\",\n" +
        "      \"particleType\": \"KI_EMBER\",\n" +
        "      \"spawnRate\": 6,\n" +
        "      \"emissionRadius\": 2.2,\n" +
        "      \"baseSpeed\": 0.25,\n" +
        "      \"color\": [0.0, 0.95, 1.0],\n" +
        "      \"suction\": false,\n" +
        "      \"swirl\": true\n" +
        "    }\n" +
        "  ]\n" +
        "}";

    public static final String ABYSSAL_ECLIPSE_WELL = "{\n" +
        "  \"id\": \"abyssal_eclipse_well\",\n" +
        "  \"duration\": 120,\n" +
        "  \"loop\": true,\n" +
        "  \"trackingRadius\": 160.0,\n" +
        "  \"components\": [\n" +
        "    {\n" +
        "      \"type\": \"camera_shake\",\n" +
        "      \"trauma\": 0.14,\n" +
        "      \"fovKick\": 1.5,\n" +
        "      \"continuous\": true\n" +
        "    },\n" +
        "    {\n" +
        "      \"type\": \"light\",\n" +
        "      \"shape\": \"COLUMN\",\n" +
        "      \"shader\": \"abyssal_void_light\",\n" +
        "      \"offset\": [0.0, 0.0, 0.0],\n" +
        "      \"radius\": 4.5,\n" +
        "      \"intensity\": 1.5,\n" +
        "      \"color\": [0.6, 0.05, 0.95],\n" +
        "      \"secondaryColor\": [0.95, 0.1, 0.3],\n" +
        "      \"coreColor\": [0.01, 0.0, 0.02],\n" +
        "      \"coreWidth\": 0.48,\n" +
        "      \"beamSpeed\": 0.9,\n" +
        "      \"patternDensity\": 2.8,\n" +
        "      \"fringeStrength\": 1.0,\n" +
        "      \"bloomMultiplier\": 2.4,\n" +
        "      \"illuminateWorld\": true,\n" +
        "      \"blockLightLevel\": 8\n" +
        "    },\n" +
        "    {\n" +
        "      \"type\": \"distortion\",\n" +
        "      \"startRadius\": 0.5,\n" +
        "      \"endRadius\": 5.0,\n" +
        "      \"strength\": 0.06\n" +
        "    },\n" +
        "    {\n" +
        "      \"type\": \"particle\",\n" +
        "      \"particleType\": \"KI_EMBER\",\n" +
        "      \"spawnRate\": 10,\n" +
        "      \"emissionRadius\": 3.0,\n" +
        "      \"baseSpeed\": -0.3,\n" +
        "      \"color\": [0.7, 0.1, 0.9],\n" +
        "      \"suction\": true,\n" +
        "      \"swirl\": true\n" +
        "    }\n" +
        "  ]\n" +
        "}";

    public static final String VALKYRIE_AURORA_SHRINE = "{\n" +
        "  \"id\": \"valkyrie_aurora_shrine\",\n" +
        "  \"duration\": 140,\n" +
        "  \"loop\": true,\n" +
        "  \"trackingRadius\": 160.0,\n" +
        "  \"components\": [\n" +
        "    {\n" +
        "      \"type\": \"light\",\n" +
        "      \"shape\": \"SPOT\",\n" +
        "      \"shader\": \"aurora_borealis\",\n" +
        "      \"offset\": [0.0, 8.5, 0.0],\n" +
        "      \"radius\": 10.0,\n" +
        "      \"spotAngle\": 55.0,\n" +
        "      \"penumbra\": 0.75,\n" +
        "      \"intensity\": 1.4,\n" +
        "      \"color\": [0.1, 0.98, 0.6],\n" +
        "      \"secondaryColor\": [0.9, 0.25, 0.85],\n" +
        "      \"coreColor\": [0.95, 1.0, 0.95],\n" +
        "      \"coreWidth\": 0.25,\n" +
        "      \"beamSpeed\": 0.8,\n" +
        "      \"patternDensity\": 2.2,\n" +
        "      \"fringeStrength\": 0.65,\n" +
        "      \"bloomMultiplier\": 1.8,\n" +
        "      \"illuminateWorld\": true,\n" +
        "      \"blockLightLevel\": 14\n" +
        "    },\n" +
        "    {\n" +
        "      \"type\": \"light\",\n" +
        "      \"shape\": \"AREA_DISC\",\n" +
        "      \"offset\": [0.0, 0.05, 0.0],\n" +
        "      \"radius\": 6.0,\n" +
        "      \"intensity\": 0.6,\n" +
        "      \"color\": [0.15, 0.95, 0.7],\n" +
        "      \"coreColor\": [1.0, 1.0, 1.0],\n" +
        "      \"bloomMultiplier\": 1.2\n" +
        "    },\n" +
        "    {\n" +
        "      \"type\": \"particle\",\n" +
        "      \"particleType\": \"KI_EMBER\",\n" +
        "      \"spawnRate\": 8,\n" +
        "      \"emissionRadius\": 2.5,\n" +
        "      \"baseSpeed\": 0.28,\n" +
        "      \"color\": [0.3, 1.0, 0.8],\n" +
        "      \"suction\": false,\n" +
        "      \"swirl\": true\n" +
        "    }\n" +
        "  ]\n" +
        "}";

    public static final String LIGHT_SACRED_CATHEDRAL = "{\n" +
        "  \"id\": \"light_sacred_cathedral\",\n" +
        "  \"duration\": 120,\n" +
        "  \"loop\": true,\n" +
        "  \"trackingRadius\": 160.0,\n" +
        "  \"components\": [\n" +
        "    {\n" +
        "      \"type\": \"light\",\n" +
        "      \"shape\": \"SPOT\",\n" +
        "      \"shader\": \"volumetric_sacred\",\n" +
        "      \"offset\": [0.0, 9.0, 0.0],\n" +
        "      \"radius\": 12.0,\n" +
        "      \"spotAngle\": 48.0,\n" +
        "      \"penumbra\": 0.65,\n" +
        "      \"intensity\": 1.4,\n" +
        "      \"color\": [1.0, 0.88, 0.45],\n" +
        "      \"secondaryColor\": [1.0, 0.65, 0.2],\n" +
        "      \"coreColor\": [1.0, 1.0, 0.95],\n" +
        "      \"coreWidth\": 0.35,\n" +
        "      \"beamSpeed\": 1.0,\n" +
        "      \"patternDensity\": 3.0,\n" +
        "      \"fringeStrength\": 0.8,\n" +
        "      \"bloomMultiplier\": 2.0,\n" +
        "      \"godRays\": true,\n" +
        "      \"godRayDensity\": 1.2,\n" +
        "      \"godRayDecay\": 0.94,\n" +
        "      \"godRayWeight\": 0.75,\n" +
        "      \"volumetricNoise\": true,\n" +
        "      \"noiseSpeed\": 1.2,\n" +
        "      \"noiseScale\": 3.5,\n" +
        "      \"dustDensity\": 0.55,\n" +
        "      \"flicker\": true,\n" +
        "      \"flickerSpeed\": 3.0,\n" +
        "      \"flickerIntensity\": 0.1,\n" +
        "      \"lightTerrain\": true,\n" +
        "      \"terrainIntensity\": 1.2,\n" +
        "      \"terrainSpecular\": 0.8,\n" +
        "      \"illuminateWorld\": true,\n" +
        "      \"blockLightLevel\": 15\n" +
        "    },\n" +
        "    {\n" +
        "      \"type\": \"light\",\n" +
        "      \"shape\": \"AREA_DISC\",\n" +
        "      \"offset\": [0.0, 0.05, 0.0],\n" +
        "      \"radius\": 7.0,\n" +
        "      \"intensity\": 0.7,\n" +
        "      \"color\": [1.0, 0.85, 0.5],\n" +
        "      \"coreColor\": [1.0, 1.0, 0.95],\n" +
        "      \"bloomMultiplier\": 1.3\n" +
        "    },\n" +
        "    {\n" +
        "      \"type\": \"light\",\n" +
        "      \"shape\": \"POINT\",\n" +
        "      \"offset\": [0.0, 8.8, 0.0],\n" +
        "      \"radius\": 2.5,\n" +
        "      \"intensity\": 1.8,\n" +
        "      \"color\": [1.0, 0.95, 0.75],\n" +
        "      \"coreColor\": [1.0, 1.0, 1.0],\n" +
        "      \"lensFlare\": true,\n" +
        "      \"flareSize\": 1.2,\n" +
        "      \"flareIntensity\": 0.9,\n" +
        "      \"bloomMultiplier\": 2.5\n" +
        "    }\n" +
        "  ]\n" +
        "}";

    public static final String LIGHT_CELESTIAL_PILLAR = "{\n" +
        "  \"id\": \"light_celestial_pillar\",\n" +
        "  \"duration\": 120,\n" +
        "  \"loop\": true,\n" +
        "  \"trackingRadius\": 160.0,\n" +
        "  \"components\": [\n" +
        "    {\n" +
        "      \"type\": \"light\",\n" +
        "      \"shape\": \"COLUMN\",\n" +
        "      \"shader\": \"celestial_beam\",\n" +
        "      \"offset\": [0.0, 5.0, 0.0],\n" +
        "      \"radius\": 3.5,\n" +
        "      \"intensity\": 1.3,\n" +
        "      \"color\": [0.15, 0.65, 1.0],\n" +
        "      \"secondaryColor\": [0.0, 1.0, 0.85],\n" +
        "      \"coreColor\": [1.0, 1.0, 1.0],\n" +
        "      \"coreWidth\": 0.35,\n" +
        "      \"beamSpeed\": 1.4,\n" +
        "      \"patternDensity\": 3.2,\n" +
        "      \"fringeStrength\": 1.1,\n" +
        "      \"bloomMultiplier\": 1.8,\n" +
        "      \"lightTerrain\": true,\n" +
        "      \"terrainIntensity\": 1.2,\n" +
        "      \"terrainSpecular\": 1.0,\n" +
        "      \"illuminateWorld\": true,\n" +
        "      \"blockLightLevel\": 15\n" +
        "    },\n" +
        "    {\n" +
        "      \"type\": \"light\",\n" +
        "      \"shape\": \"AREA_DISC\",\n" +
        "      \"shader\": \"celestial_beam\",\n" +
        "      \"offset\": [0.0, 0.05, 0.0],\n" +
        "      \"radius\": 5.0,\n" +
        "      \"intensity\": 0.8,\n" +
        "      \"color\": [0.15, 0.65, 1.0],\n" +
        "      \"secondaryColor\": [0.0, 1.0, 0.85],\n" +
        "      \"coreColor\": [1.0, 1.0, 1.0],\n" +
        "      \"coreWidth\": 0.35,\n" +
        "      \"patternDensity\": 3.0,\n" +
        "      \"bloomMultiplier\": 1.4\n" +
        "    }\n" +
        "  ]\n" +
        "}";

    public static final String LIGHT_CYBER_HOLOBEACON = "{\n" +
        "  \"id\": \"light_cyber_holobeacon\",\n" +
        "  \"duration\": 120,\n" +
        "  \"loop\": true,\n" +
        "  \"trackingRadius\": 160.0,\n" +
        "  \"components\": [\n" +
        "    {\n" +
        "      \"type\": \"light\",\n" +
        "      \"shape\": \"SPOT\",\n" +
        "      \"shader\": \"cyber_grid_shaft\",\n" +
        "      \"offset\": [0.0, 8.0, 0.0],\n" +
        "      \"radius\": 10.0,\n" +
        "      \"spotAngle\": 50.0,\n" +
        "      \"penumbra\": 0.4,\n" +
        "      \"intensity\": 1.5,\n" +
        "      \"color\": [0.0, 0.95, 1.0],\n" +
        "      \"secondaryColor\": [1.0, 0.1, 0.6],\n" +
        "      \"coreColor\": [0.85, 1.0, 1.0],\n" +
        "      \"coreWidth\": 0.3,\n" +
        "      \"beamSpeed\": 1.6,\n" +
        "      \"patternDensity\": 4.0,\n" +
        "      \"fringeStrength\": 1.5,\n" +
        "      \"bloomMultiplier\": 2.0,\n" +
        "      \"pulsing\": true,\n" +
        "      \"pulseSpeed\": 5.0,\n" +
        "      \"pulseIntensity\": 0.15,\n" +
        "      \"lightTerrain\": true,\n" +
        "      \"terrainIntensity\": 1.3,\n" +
        "      \"terrainSpecular\": 1.6,\n" +
        "      \"illuminateWorld\": true,\n" +
        "      \"blockLightLevel\": 14\n" +
        "    },\n" +
        "    {\n" +
        "      \"type\": \"light\",\n" +
        "      \"shape\": \"AREA_DISC\",\n" +
        "      \"shader\": \"cyber_grid_shaft\",\n" +
        "      \"offset\": [0.0, 0.05, 0.0],\n" +
        "      \"radius\": 4.5,\n" +
        "      \"intensity\": 0.85,\n" +
        "      \"color\": [0.0, 0.95, 1.0],\n" +
        "      \"secondaryColor\": [1.0, 0.05, 0.5],\n" +
        "      \"coreColor\": [0.9, 1.0, 1.0],\n" +
        "      \"patternDensity\": 3.5,\n" +
        "      \"bloomMultiplier\": 1.4\n" +
        "    }\n" +
        "  ]\n" +
        "}";

    public static final String LIGHT_ABYSSAL_SINGULARITY = "{\n" +
        "  \"id\": \"light_abyssal_singularity\",\n" +
        "  \"duration\": 120,\n" +
        "  \"loop\": true,\n" +
        "  \"trackingRadius\": 160.0,\n" +
        "  \"components\": [\n" +
        "    {\n" +
        "      \"type\": \"light\",\n" +
        "      \"shape\": \"POINT\",\n" +
        "      \"shader\": \"abyssal_void_light\",\n" +
        "      \"offset\": [0.0, 3.5, 0.0],\n" +
        "      \"radius\": 3.5,\n" +
        "      \"intensity\": 1.2,\n" +
        "      \"color\": [0.55, 0.05, 0.9],\n" +
        "      \"secondaryColor\": [0.95, 0.05, 0.25],\n" +
        "      \"coreColor\": [0.01, 0.0, 0.02],\n" +
        "      \"coreWidth\": 0.48,\n" +
        "      \"beamSpeed\": 0.8,\n" +
        "      \"patternDensity\": 2.6,\n" +
        "      \"fringeStrength\": 1.2,\n" +
        "      \"bloomMultiplier\": 1.5,\n" +
        "      \"illuminateWorld\": true,\n" +
        "      \"blockLightLevel\": 6\n" +
        "    },\n" +
        "    {\n" +
        "      \"type\": \"light\",\n" +
        "      \"shape\": \"COLUMN\",\n" +
        "      \"shader\": \"abyssal_void_light\",\n" +
        "      \"offset\": [0.0, 3.5, 0.0],\n" +
        "      \"radius\": 2.0,\n" +
        "      \"intensity\": 1.0,\n" +
        "      \"color\": [0.45, 0.0, 0.8],\n" +
        "      \"secondaryColor\": [0.85, 0.0, 0.3],\n" +
        "      \"coreColor\": [0.0, 0.0, 0.0],\n" +
        "      \"coreWidth\": 0.45,\n" +
        "      \"beamSpeed\": 0.7,\n" +
        "      \"patternDensity\": 2.4,\n" +
        "      \"bloomMultiplier\": 1.4\n" +
        "    },\n" +
        "    {\n" +
        "      \"type\": \"light\",\n" +
        "      \"shape\": \"AREA_DISC\",\n" +
        "      \"shader\": \"abyssal_void_light\",\n" +
        "      \"offset\": [0.0, 0.05, 0.0],\n" +
        "      \"radius\": 4.5,\n" +
        "      \"intensity\": 0.75,\n" +
        "      \"color\": [0.45, 0.0, 0.75],\n" +
        "      \"secondaryColor\": [0.9, 0.05, 0.2],\n" +
        "      \"coreColor\": [0.0, 0.0, 0.0],\n" +
        "      \"coreWidth\": 0.4,\n" +
        "      \"bloomMultiplier\": 1.3\n" +
        "    }\n" +
        "  ]\n" +
        "}";

    public static final String LIGHT_VALKYRIE_AURORA = "{\n" +
        "  \"id\": \"light_valkyrie_aurora\",\n" +
        "  \"duration\": 140,\n" +
        "  \"loop\": true,\n" +
        "  \"trackingRadius\": 160.0,\n" +
        "  \"components\": [\n" +
        "    {\n" +
        "      \"type\": \"light\",\n" +
        "      \"shape\": \"SPOT\",\n" +
        "      \"shader\": \"aurora_borealis\",\n" +
        "      \"offset\": [0.0, 8.5, 0.0],\n" +
        "      \"radius\": 10.0,\n" +
        "      \"spotAngle\": 55.0,\n" +
        "      \"penumbra\": 0.75,\n" +
        "      \"intensity\": 1.4,\n" +
        "      \"color\": [0.10, 0.98, 0.60],\n" +
        "      \"secondaryColor\": [0.90, 0.25, 0.85],\n" +
        "      \"coreColor\": [0.95, 1.0, 0.95],\n" +
        "      \"coreWidth\": 0.25,\n" +
        "      \"beamSpeed\": 0.8,\n" +
        "      \"patternDensity\": 2.2,\n" +
        "      \"fringeStrength\": 0.65,\n" +
        "      \"bloomMultiplier\": 1.8,\n" +
        "      \"lightTerrain\": true,\n" +
        "      \"terrainIntensity\": 1.1,\n" +
        "      \"terrainSpecular\": 0.8,\n" +
        "      \"illuminateWorld\": true,\n" +
        "      \"blockLightLevel\": 14\n" +
        "    },\n" +
        "    {\n" +
        "      \"type\": \"light\",\n" +
        "      \"shape\": \"COLUMN\",\n" +
        "      \"shader\": \"aurora_borealis\",\n" +
        "      \"offset\": [0.0, 4.5, 0.0],\n" +
        "      \"radius\": 2.2,\n" +
        "      \"intensity\": 0.85,\n" +
        "      \"color\": [0.15, 0.95, 0.70],\n" +
        "      \"secondaryColor\": [0.95, 0.20, 0.85],\n" +
        "      \"coreColor\": [0.95, 1.0, 0.95],\n" +
        "      \"coreWidth\": 0.20,\n" +
        "      \"beamSpeed\": 0.9,\n" +
        "      \"patternDensity\": 2.4,\n" +
        "      \"bloomMultiplier\": 1.5\n" +
        "    },\n" +
        "    {\n" +
        "      \"type\": \"light\",\n" +
        "      \"shape\": \"AREA_DISC\",\n" +
        "      \"shader\": \"aurora_borealis\",\n" +
        "      \"offset\": [0.0, 0.05, 0.0],\n" +
        "      \"radius\": 5.5,\n" +
        "      \"intensity\": 0.70,\n" +
        "      \"color\": [0.10, 0.95, 0.65],\n" +
        "      \"secondaryColor\": [0.85, 0.20, 0.80],\n" +
        "      \"bloomMultiplier\": 1.3\n" +
        "    }\n" +
        "  ]\n" +
        "}";
}
