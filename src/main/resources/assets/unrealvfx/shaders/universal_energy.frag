#version 120

varying vec2 v_texCoord;
varying vec3 v_normal;
varying vec3 v_viewDir;
varying vec3 v_localPos;

uniform float u_time;
uniform float u_progress;
uniform vec4 u_color;
uniform float u_steppedFps;

uniform vec3 u_hotCoreColor;
uniform vec3 u_rimColor;
uniform float u_rimPower;
uniform float u_noiseSpeed;
uniform float u_noiseScale;
uniform float u_bloomMultiplier;
uniform float u_invertedCore;

// Generatore di rumore procedurale GLSL 1.20 puro ad alte prestazioni
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
    for (int i = 0; i < 3; i++) {
        v += a * noise2D(p);
        p = rot * p * 2.0;
        a *= 0.5;
    }
    return v;
}

void main() {
    vec3 normal = normalize(v_normal);
    vec3 viewDir = normalize(v_viewDir);

    // Fresnel / Rim perimetrale
    float ndv = clamp(dot(normal, viewDir), 0.0, 1.0);
    float rim = pow(1.0 - ndv, max(0.1, u_rimPower));

    // Hot-Core centrale incandescente
    float core = pow(ndv, 2.0);

    // Quantized Stepped Sakuga Timing ("On 2s" / hand-drawn anime cadence)
    float t = (u_steppedFps > 0.0) ? floor(u_time * u_steppedFps + 0.0001) / u_steppedFps : u_time;

    // Coordinate UV animate per turbolenza energetica (laser o plasma scorrevole)
    vec2 uv = v_texCoord * max(0.1, u_noiseScale);
    uv.y -= t * u_noiseSpeed * 2.5;

    float energyNoise = fbm2D(uv);

    // Shading volumetrico delle facce geometriche del cubo
    float faceShade = clamp(abs(normal.y) * 0.2 + abs(normal.x) * 0.15 + abs(normal.z) * 0.25 + 0.5, 0.0, 1.0);

    // Colori effettivi con fallback
    vec3 baseColor = u_color.rgb;
    vec3 coreColor = (length(u_hotCoreColor) > 0.01) ? u_hotCoreColor : vec3(1.0, 1.0, 1.0);
    vec3 rimCol = (length(u_rimColor) > 0.01) ? u_rimColor : (baseColor * 1.3);

    // Miscelazione cromatica multi-strato: Inverted Core (Void Mode) vs Standard Hot Core
    vec3 mixedColor;
    float alpha;
    if (u_invertedCore > 0.5) {
        vec3 voidCore = (length(u_hotCoreColor) > 0.001) ? u_hotCoreColor : vec3(0.01, 0.0, 0.02);
        mixedColor = mix(voidCore, baseColor * (0.7 + 0.3 * energyNoise), rim * 0.9);
        mixedColor = mix(mixedColor, rimCol, pow(rim, 1.8) * 1.5);
        alpha = clamp((rim * 1.5 + (1.0 - core) * 0.6 + energyNoise * 0.3) * u_color.a, 0.0, 1.0);
    } else {
        mixedColor = mix(baseColor * (0.8 + 0.4 * energyNoise), rimCol, rim * 0.85);
        mixedColor = mix(mixedColor, coreColor, core * (0.6 + 0.4 * energyNoise));
        alpha = clamp((rim * 1.2 + core * 0.8 + energyNoise * 0.4) * u_color.a, 0.0, 1.0);
    }

    // Applicazione ombreggiatura facce e moltiplicatore bloom
    float bloom = max(1.0, u_bloomMultiplier);
    vec3 finalColor = mixedColor * faceShade * bloom;

    gl_FragColor = vec4(finalColor, alpha);
}
