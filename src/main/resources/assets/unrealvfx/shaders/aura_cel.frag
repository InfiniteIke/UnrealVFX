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

// Customizable Cel-Shading & Ki Flame Controls
uniform vec3 u_hotCoreColor;     // Inner core flame color (bright white/yellow/cyan)
uniform vec3 u_rimColor;         // Outer dark/shadow edge color
uniform float u_distortionStrength;// Flame domain warp distortion strength
uniform float u_noiseSpeed;      // Rising flame velocity
uniform float u_celBands;        // Cel-shading step count (e.g. 3.0 or 4.0)
uniform float u_bloomMultiplier; // Bloom boost

float linearizeDepth(float z, float n, float f) {
    return (n * f) / (f - z * (f - n));
}

float hash(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453123);
}

float noise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    float a = hash(i);
    float b = hash(i + vec2(1.0, 0.0));
    float c = hash(i + vec2(0.0, 1.0));
    float d = hash(i + vec2(1.0, 1.0));
    return mix(mix(a, b, f.x), mix(c, d, f.x), f.y);
}

float fbm(vec2 p) {
    float v = 0.0;
    v += noise(p) * 0.5000; p *= 2.02;
    v += noise(p) * 0.2500; p *= 2.03;
    v += noise(p) * 0.1250; p *= 2.01;
    v += noise(p) * 0.0625;
    return v;
}

void main() {
    vec2 uv = v_texCoord;

    float speed = (u_noiseSpeed > 0.01) ? u_noiseSpeed : 1.8;
    float distStr = (u_distortionStrength > 0.001) ? u_distortionStrength : 0.65;

    // Quantized Stepped Sakuga Timing ("On 2s" / hand-drawn anime cadence)
    float t = (u_steppedFps > 0.0) ? floor(u_time * u_steppedFps + 0.0001) / u_steppedFps : u_time;

    // 1. Stylized Anime Ki Flame Flow (Domain Warping in rapid upward ascent)
    vec2 flow = vec2(0.0, -t * speed * 3.2);
    vec2 p = vec2(uv.x * 7.0, uv.y * 3.5) + flow;

    // Two stages of domain warping for anime flame tongues
    vec2 q = vec2(fbm(p), fbm(p + vec2(5.2, 1.3)));
    vec2 r = vec2(fbm(p + 3.0 * q * distStr + vec2(1.7, 9.2) - vec2(0.0, t * speed * 1.5)),
                  fbm(p + 3.0 * q * distStr + vec2(8.3, 2.8) + vec2(t * speed * 0.8, 0.0)));
    float flamePattern = fbm(p + 3.8 * r * distStr);

    // 2. High-contrast Fresnel Rim
    vec3 norm = normalize(v_normal);
    vec3 view = normalize(v_viewDir);
    float ndv = clamp(abs(dot(norm, view)), 0.0, 1.0);
    float rim = pow(1.0 - ndv, 1.8);

    // 3. Vertical taper (fades smoothly at the base, flares dramatically toward the top)
    float vertFade = sin(clamp(uv.y, 0.0, 1.0) * 3.14159265);
    vertFade = pow(vertFade, 0.55);

    // 4. Combined Ki intensity
    float intensity = (flamePattern * 0.72 + rim * 0.48) * vertFade;

    // Outer silhouette cutoff (sharp anime cut)
    float cutoff = 0.22;
    if (intensity < cutoff) {
        discard;
    }

    // 5. Customizable Cel-Shading Stepped Bands (Arc System Works style)
    float normalizedIntensity = clamp((intensity - cutoff) / (1.0 - cutoff), 0.0, 1.0);

    // Bands: Shadow -> Mid/Base -> Highlight -> Hot Core
    float bands = (u_celBands > 1.5) ? u_celBands : 3.0;
    float stepped = floor(normalizedIntensity * bands) / bands;

    vec3 baseCol = u_color.rgb;
    vec3 shadowCol = (length(u_rimColor) > 0.01) ? u_rimColor : (baseCol * vec3(0.40, 0.10, 0.55));
    vec3 coreCol = (length(u_hotCoreColor) > 0.01) ? u_hotCoreColor : vec3(1.0, 1.0, 0.95);

    vec3 finalColor;
    if (stepped > 0.65) {
        finalColor = mix(baseCol, coreCol, (stepped - 0.65) / 0.35);
    } else if (stepped > 0.30) {
        finalColor = mix(shadowCol, baseCol, (stepped - 0.30) / 0.35);
    } else {
        finalColor = shadowCol;
    }

    // Searing hot rim on cel band transitions
    float bandEdge = fract(normalizedIntensity * bands);
    float edgeGlow = smoothstep(0.85, 1.0, bandEdge) * 0.45;
    finalColor += coreCol * edgeGlow;

    // Bloom booster
    float bloom = (u_bloomMultiplier > 0.1) ? u_bloomMultiplier : 1.6;
    finalColor *= bloom;

    float alpha = clamp(smoothstep(cutoff, cutoff + 0.05, intensity) * u_color.a, 0.0, 1.0);

    // 6. Soft Depth Fading against terrain/player
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

    gl_FragColor = vec4(finalColor, alpha);
}
