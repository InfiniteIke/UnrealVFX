#version 120

varying vec2 v_texCoord;
varying vec3 v_normal;
varying vec3 v_viewDir;

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

void main() {
    vec3 normal = normalize(v_normal);
    vec3 viewDir = normalize(v_viewDir);

    float ndv = clamp(dot(normal, viewDir), 0.0, 1.0);
    float rim = pow(1.0 - ndv, 1.5);

    // Dinamica delle fiamme di plasma
    vec2 uv = v_texCoord;
    float plasma = sin(uv.x * 18.0 + u_time * 8.0) * cos(uv.y * 14.0 - u_time * 7.0);
    plasma = plasma * 0.5 + 0.5;

    float intensity = rim * 0.70 + plasma * 0.55;

    // Cel-shading netto a 3 fasce (Anime Toei / Sparking! ZERO)
    if (intensity < 0.28) {
        discard;
    }

    vec3 darkPurple = vec3(0.55, 0.0, 0.85);
    vec3 neonMagenta = vec3(1.0, 0.05, 0.85);
    vec3 whiteHot = vec3(1.0, 0.92, 1.0);

    vec3 color;
    if (intensity > 0.82) {
        color = whiteHot;
    } else if (intensity > 0.48) {
        color = neonMagenta;
    } else {
        color = darkPurple;
    }

    float alpha = clamp((intensity - 0.28) / 0.72 * u_color.a, 0.0, 1.0);

    // Soft Depth Fading
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

    gl_FragColor = vec4(color, alpha);
}
