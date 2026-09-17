#version 120

varying vec2 v_texCoord;
varying vec3 v_normal;
varying vec3 v_viewDir;
varying vec3 v_worldPos;

uniform sampler2D u_depthTexture;
uniform vec2 u_screenResolution;
uniform float u_near;
uniform float u_far;
uniform float u_fadeDistance;

uniform float u_time;
uniform float u_progress;
uniform vec4 u_color;

float linearizeDepth(float z, float n, float f) {
    return (n * f) / (f - z * (f - n));
}

vec3 hash3(vec3 p) {
    p = vec3(dot(p, vec3(127.1, 311.7, 74.7)),
             dot(p, vec3(269.5, 183.3, 246.1)),
             dot(p, vec3(113.5, 271.9, 124.6)));
    return fract(sin(p) * 43758.5453123);
}

float noise3D(vec3 p) {
    vec3 i = floor(p);
    vec3 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);

    float n000 = dot(hash3(i + vec3(0.0, 0.0, 0.0)) * 2.0 - 1.0, f - vec3(0.0, 0.0, 0.0));
    float n100 = dot(hash3(i + vec3(1.0, 0.0, 0.0)) * 2.0 - 1.0, f - vec3(1.0, 0.0, 0.0));
    float n010 = dot(hash3(i + vec3(0.0, 1.0, 0.0)) * 2.0 - 1.0, f - vec3(0.0, 1.0, 0.0));
    float n110 = dot(hash3(i + vec3(1.0, 1.0, 0.0)) * 2.0 - 1.0, f - vec3(1.0, 1.0, 0.0));
    float n001 = dot(hash3(i + vec3(0.0, 0.0, 1.0)) * 2.0 - 1.0, f - vec3(0.0, 0.0, 1.0));
    float n101 = dot(hash3(i + vec3(1.0, 0.0, 1.0)) * 2.0 - 1.0, f - vec3(1.0, 0.0, 1.0));
    float n011 = dot(hash3(i + vec3(0.0, 1.0, 1.0)) * 2.0 - 1.0, f - vec3(0.0, 1.0, 1.0));
    float n111 = dot(hash3(i + vec3(1.0, 1.0, 1.0)) * 2.0 - 1.0, f - vec3(1.0, 1.0, 1.0));

    float nx00 = mix(n000, n100, f.x);
    float nx10 = mix(n010, n110, f.x);
    float nx01 = mix(n001, n101, f.x);
    float nx11 = mix(n011, n111, f.x);

    float nxy0 = mix(nx00, nx10, f.y);
    float nxy1 = mix(nx01, nx11, f.y);

    return mix(nxy0, nxy1, f.z) * 0.5 + 0.5;
}

void main() {
    vec3 normal = normalize(v_normal);
    vec3 viewDir = normalize(v_viewDir);

    float ndv = clamp(dot(normal, viewDir), 0.0, 1.0);
    // Fresnel Rim ad alto contrasto (bordo dell'orizzonte degli eventi)
    float rim = pow(1.0 - ndv, 2.5);

    // 1. Raymarching volumetrico interno (assorbimento gravitazionale di Beer-Lambert)
    vec3 rayDir = -viewDir;
    vec3 pStart = v_worldPos;
    float transmittance = 1.0;
    vec3 interiorGlow = vec3(0.0);

    for (int i = 0; i < 6; i++) {
        vec3 pStep = pStart + rayDir * (float(i) * 0.18);
        float r = length(pStep);
        float density = 1.0 / (r * r + 0.12);
        transmittance *= exp(-density * 0.22);
        interiorGlow += vec3(0.65, 0.0, 0.85) * (1.0 - transmittance) * 0.15;
    }

    // 2. Vene cosmiche di distruzione che serpeggiano sul guscio
    vec3 p = v_worldPos * 3.5 + vec3(u_time * 0.6, -u_time * 0.8, u_time * 0.5);
    float n1 = noise3D(p);
    float n2 = noise3D(p * 2.2 - vec3(u_time * 1.2));
    float veins = smoothstep(0.56, 0.66, n1 * 0.6 + n2 * 0.4);

    // 3. Palette Hakai pura (Sparking! ZERO)
    vec3 voidBlack = vec3(0.005, 0.001, 0.015);
    vec3 veinColor = vec3(0.85, 0.0, 0.95);
    vec3 neonMagenta = vec3(1.0, 0.04, 0.82);
    vec3 hotRim = vec3(1.0, 0.92, 0.98);

    vec3 finalColor = mix(voidBlack, interiorGlow, 0.4);
    finalColor = mix(finalColor, veinColor, veins * 0.88);

    // Cel-shading sul bordo dell'orizzonte degli eventi
    if (rim > 0.68) {
        finalColor = mix(finalColor, hotRim, (rim - 0.68) / 0.32);
    } else if (rim > 0.32) {
        finalColor = mix(finalColor, neonMagenta, (rim - 0.32) / 0.36);
    } else if (rim > 0.12) {
        finalColor = mix(finalColor, veinColor, (rim - 0.12) / 0.20);
    }

    float alpha = clamp(0.98 * u_color.a, 0.0, 1.0);

    // 4. Soft Depth Fading
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

    if (alpha < 0.015) {
        discard;
    }

    gl_FragColor = vec4(finalColor, alpha);
}
