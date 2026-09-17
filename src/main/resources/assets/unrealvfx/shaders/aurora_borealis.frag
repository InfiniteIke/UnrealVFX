#version 120

uniform int u_shape; // 0 = POINT, 1 = SPOT, 2 = COLUMN, 3 = DIRECTIONAL, 4 = AREA_DISC
uniform vec3 u_color;
uniform vec3 u_secondaryColor;
uniform vec3 u_coreColor;
uniform float u_intensity;
uniform float u_falloffPower;
uniform float u_spotAngle;
uniform float u_penumbra;
uniform float u_time;
uniform int u_hasNoise;
uniform float u_noiseSpeed;
uniform float u_noiseScale;
uniform float u_dustDensity;
uniform float u_flicker;
uniform float u_pulse;
uniform float u_bloomMultiplier;

uniform float u_coreWidth;
uniform float u_beamSpeed;
uniform float u_patternDensity;
uniform float u_fringeStrength;

// Soft Depth Fade Uniforms
uniform sampler2D u_depthTexture;
uniform vec2 u_screenResolution;
uniform float u_near;
uniform float u_far;
uniform float u_fadeDistance;

varying vec3 v_localPos;
varying vec3 v_normal;
varying vec3 v_objNormal;
varying vec3 v_viewDir;
varying vec2 v_texCoord;

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
    for (int i = 0; i < 3; i++) {
        v += a * noise2D(p);
        p = rot * p * 2.0;
        a *= 0.5;
    }
    return v;
}

float hash3(vec3 p) {
    p = fract(p * vec3(443.897, 441.423, 437.195));
    p += dot(p, p + 45.32);
    return fract((p.x + p.y) * p.z);
}

float calculateBaseFalloff(vec3 norm, vec3 view, float pVal) {
    float falloff = 0.0;
    if (u_shape == 0) {
        float r = length(v_localPos * 2.0);
        float baseFalloff = pow(clamp(1.0 - r, 0.0, 1.0), pVal);
        float fresnel = clamp(abs(dot(norm, view)), 0.0, 1.0);
        falloff = baseFalloff * (0.4 + 0.6 * pow(fresnel, 0.8));
    } else if (u_shape == 1) {
        float t = clamp(0.5 - v_localPos.y, 0.0, 1.0);
        float expectedR = max(0.001, t * 0.5);
        float radRatio = length(v_localPos.xz) / expectedR;
        float pen = clamp(u_penumbra, 0.02, 0.98);
        float angleFalloff = smoothstep(1.0, 1.0 - pen, radRatio);
        float distFalloff = pow(clamp(1.0 - t, 0.0, 1.0), pVal);
        falloff = angleFalloff * distFalloff;
    } else if (u_shape == 2) {
        float radFalloff = (abs(norm.y) > 0.5)
            ? pow(clamp(1.0 - length(v_localPos.xz) * 2.0, 0.0, 1.0), pVal)
            : pow(clamp(abs(dot(norm, view)), 0.0, 1.0), max(pVal * 0.7, 0.2));
        float vertFalloff = clamp(1.0 - pow(abs(v_localPos.y * 2.0), 6.0), 0.0, 1.0);
        falloff = radFalloff * vertFalloff;
    } else if (u_shape == 3) {
        float dist = abs(v_localPos.x * 2.0);
        falloff = pow(clamp(1.0 - dist, 0.0, 1.0), pVal);
    } else {
        float rad = length(v_localPos.xz) * 2.0;
        falloff = pow(clamp(1.0 - rad, 0.0, 1.0), pVal);
    }
    return clamp(falloff, 0.0, 1.0);
}

float applySoftDepth(float alpha) {
    if (u_fadeDistance > 0.001 && u_screenResolution.x > 0.0) {
        vec2 screenUV = gl_FragCoord.xy / u_screenResolution;
        float rawSceneZ = texture2D(u_depthTexture, screenUV).r;
        if (rawSceneZ > 0.0001 && rawSceneZ < 0.9999) {
            float sceneZ = linearizeDepth(rawSceneZ, u_near, u_far);
            float fragZ  = linearizeDepth(gl_FragCoord.z, u_near, u_far);
            float deltaZ = sceneZ - fragZ;
            if (deltaZ > 0.0) {
                float depthFade = clamp(deltaZ / u_fadeDistance, 0.0, 1.0);
                alpha *= smoothstep(0.0, 1.0, depthFade);
            } else {
                alpha *= 0.1;
            }
        }
    }
    return alpha;
}

void main() {
    vec3 norm = normalize(v_normal);
    vec3 view = normalize(v_viewDir);
    float pVal = max(u_falloffPower, 0.1);
    float falloff = calculateBaseFalloff(norm, view, pVal);

    float speed = u_beamSpeed > 0.0 ? u_beamSpeed : 0.7;
    float density = u_patternDensity > 0.0 ? u_patternDensity : 2.0;

    // Harmonic undulating curtain waves
    float azimuth = atan(v_localPos.z, v_localPos.x);
    float wave1 = sin(v_localPos.y * 4.0 * density + azimuth * 3.0 + u_time * speed * 1.5) * 0.5 + 0.5;
    float wave2 = cos(v_localPos.y * 7.0 * density - azimuth * 2.0 - u_time * speed * 1.0) * 0.5 + 0.5;
    float curtainFold = wave1 * 0.6 + wave2 * 0.4;
    curtainFold = pow(curtainFold, 1.6);

    falloff *= (0.55 + 0.75 * curtainFold);

    // Height-based spectral color transition (Base color -> Apex secondary color)
    float heightNorm = clamp(v_localPos.y + 0.5, 0.0, 1.0);
    vec3 spectralColor = mix(u_color, u_secondaryColor, smoothstep(0.15, 0.85, heightNorm));

    // Gossamer Fresnel sheen
    float fresnel = pow(1.0 - clamp(abs(dot(norm, view)), 0.0, 1.0), 1.5) * u_fringeStrength;
    spectralColor = mix(spectralColor, u_coreColor, fresnel * 0.5);

    float animMod = max(0.0, u_flicker * u_pulse);
    float alpha = falloff * max(u_intensity, 0.0) * (0.6 + 0.4 * fresnel) * animMod;
    spectralColor *= (1.0 + max(u_bloomMultiplier - 1.0, 0.0) * 0.55);

    alpha = applySoftDepth(alpha);
    if (alpha < 0.003) discard;

    gl_FragColor = vec4(spectralColor, alpha);
}
