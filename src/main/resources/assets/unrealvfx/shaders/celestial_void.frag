#version 120

varying vec2 v_texCoord;
varying vec3 v_normal;
varying vec3 v_viewDir;
varying vec3 v_localPos;

uniform sampler2D u_depthTexture;
uniform vec2 u_screenResolution;
uniform float u_near;
uniform float u_far;
uniform float u_fadeDistance;

uniform float u_time;
uniform float u_progress;
uniform vec4 u_color;
uniform float u_steppedFps;

// Inverted Core / Celestial Void Controls
uniform vec3 u_hotCoreColor;     // Void / core color (abyssal black/dark violet)
uniform vec3 u_rimColor;         // Radiant plasma corona silhouette color
uniform float u_rimPower;        // Silhouette falloff exponent
uniform float u_noiseSpeed;      // Surface plasma filament speed
uniform float u_noiseScale;      // Surface plasma filament frequency
uniform float u_bloomMultiplier; // Atmospheric bloom expansion
uniform float u_filamentStrength;// Surface plasma electric filament intensity
uniform float u_invertedCore;    // Void core (1.0) vs Standard glowing core (0.0)

float linearizeDepth(float z, float n, float f) {
    return (n * f) / (f - z * (f - n));
}

float hash21(vec2 p) {
    p = fract(p * vec2(123.34, 456.21));
    p += dot(p, p + 45.32);
    return fract(p.x * p.y);
}

float noise2D(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    vec2 u = f * f * (3.0 - 2.0 * f);

    float a = hash21(i);
    float b = hash21(i + vec2(1.0, 0.0));
    float c = hash21(i + vec2(0.0, 1.0));
    float d = hash21(i + vec2(1.0, 1.0));

    return mix(mix(a, b, u.x), mix(c, d, u.x), u.y);
}

float fbm2D(vec2 p) {
    float v = 0.0;
    float a = 0.5;
    mat2 rot = mat2(0.8, 0.6, -0.6, 0.8);
    for (int i = 0; i < 4; i++) {
        v += a * noise2D(p);
        p = rot * p * 2.02;
        a *= 0.5;
    }
    return v;
}

void main() {
    vec3 normal = normalize(v_normal);
    vec3 viewDir = normalize(v_viewDir);

    float ndv = clamp(dot(normal, viewDir), 0.0, 1.0);

    // Corona falloff power
    float pwr = (u_rimPower > 0.1) ? u_rimPower : 2.8;
    float rim = pow(1.0 - ndv, pwr);

    // Quantized Stepped Sakuga Timing ("On 2s" / hand-drawn anime cadence)
    float t = (u_steppedFps > 0.0) ? floor(u_time * u_steppedFps + 0.0001) / u_steppedFps : u_time;

    // Dynamic surface plasma filaments
    float nScale = (u_noiseScale > 0.1) ? u_noiseScale : 3.0;
    float nSpeed = (u_noiseSpeed > 0.001) ? u_noiseSpeed : 0.8;
    vec2 uv = v_texCoord * nScale;

    // Dual-axis swirling flows simulating plasma convection
    vec2 flow1 = vec2(t * nSpeed * 0.7, -t * nSpeed * 1.1);
    vec2 flow2 = vec2(-t * nSpeed * 0.9, t * nSpeed * 0.5);

    float n1 = fbm2D(uv + flow1);
    float n2 = fbm2D(uv * 1.8 + flow2 + vec2(n1 * 1.5, n1 * 1.2));
    float plasma = (n1 * 0.6 + n2 * 0.4);

    // High-frequency electric filaments / veins crawling over the sphere surface
    float filamentNoise = abs(plasma - 0.5) * 2.0;
    float filaments = 1.0 - smoothstep(0.0, 0.28, filamentNoise);
    filaments = pow(filaments, 2.0);

    // Inverted core colors: Abyssal cosmic void at center, radiant plasma corona at silhouette
    vec3 voidColor = (length(u_hotCoreColor) > 0.01) ? u_hotCoreColor : vec3(0.01, 0.002, 0.025);
    vec3 coronaColor = (length(u_rimColor) > 0.01) ? u_rimColor : (u_color.rgb * 1.4);

    // Secondary electric fringe color (electric cyan / light violet blend)
    vec3 fringeColor = mix(coronaColor, vec3(0.3, 0.85, 1.0), 0.4);

    // 1. Core calculation (Inverted Cosmic Void by default or when u_invertedCore > 0.5)
    vec3 col;
    if (u_invertedCore > 0.5 || u_invertedCore < -0.5) {
        // Abyssal Void Core (absorbs light toward center)
        col = mix(voidColor, voidColor * 0.15, pow(ndv, 1.8));
    } else {
        // Radiant Star Core
        vec3 starCore = (length(u_hotCoreColor) > 0.01) ? u_hotCoreColor : vec3(1.0, 1.0, 1.0);
        col = mix(u_color.rgb, starCore, pow(ndv, 2.0));
    }

    // 2. Surface Plasma Filaments
    float filStrength = (u_filamentStrength > 0.01) ? u_filamentStrength : 0.85;
    col += coronaColor * filaments * filStrength * (0.35 + 0.65 * rim);

    // 3. Smooth, soft electric plasma corona silhouette
    float bloom = (u_bloomMultiplier > 0.1) ? u_bloomMultiplier : 2.0;
    vec3 rimGlow = mix(coronaColor, fringeColor, pow(rim, 2.2));
    col += rimGlow * (rim * 1.8 + pow(rim, 4.0) * 2.5 * bloom);

    float alpha = clamp((0.95 + rim * 0.4) * u_color.a, 0.0, 1.0);

    // Soft Depth Fading against world geometry
    if (u_fadeDistance > 0.001 && u_screenResolution.x > 0.0) {
        vec2 screenUV = gl_FragCoord.xy / u_screenResolution;
        float rawSceneZ = texture2D(u_depthTexture, screenUV).r;
        float sceneZ = linearizeDepth(rawSceneZ, u_near, u_far);
        float fragZ  = linearizeDepth(gl_FragCoord.z, u_near, u_far);

        float deltaZ = sceneZ - fragZ;
        float depthFade = clamp(deltaZ / u_fadeDistance, 0.0, 1.0);
        depthFade = smoothstep(0.0, 1.0, depthFade);
        alpha *= depthFade;
    }

    if (alpha < 0.01) discard;

    gl_FragColor = vec4(col, alpha);
}
