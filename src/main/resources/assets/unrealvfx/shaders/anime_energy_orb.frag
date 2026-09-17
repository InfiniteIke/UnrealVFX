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

void main() {
    vec3 normal = normalize(v_normal);
    vec3 viewDir = normalize(v_viewDir);

    float ndv = clamp(dot(normal, viewDir), 0.0, 1.0);
    float rim = pow(1.0 - ndv, 2.0);

    // Dinamica superficiale della sfera energetica
    vec2 uv = v_texCoord;
    float plasma1 = noise(vec2(uv.x * 10.0 + u_time * 3.0, uv.y * 8.0 - u_time * 4.0));
    float plasma2 = noise(vec2(uv.x * 20.0 - u_time * 6.0, uv.y * 16.0 + u_time * 3.0));
    float plasma = plasma1 * 0.7 + plasma2 * 0.3;

    // Palette Anime Cel-Shading a 3 fasce (Arc System Works)
    vec3 coreWhite = vec3(1.0, 1.0, 1.0);
    vec3 bodyColor = u_color.rgb;
    vec3 rimDark   = u_color.rgb * vec3(0.35, 0.10, 0.50);

    float intensity = ndv * 0.75 + plasma * 0.30;

    float eps = 0.015;
    float tCore = smoothstep(0.72 - eps, 0.72 + eps, intensity);
    float tMid  = smoothstep(0.30 - eps, 0.30 + eps, intensity);

    vec3 finalColor = mix(rimDark, bodyColor, tMid);
    finalColor = mix(finalColor, coreWhite, tCore);

    // Rim highlight esterno
    finalColor += u_color.rgb * rim * 0.45;

    float alpha = clamp((0.88 + rim * 0.35) * u_color.a, 0.0, 1.0);

    // Soft Depth Fading: elimina le compenetrazioni dure coi blocchi
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
