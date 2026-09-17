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
    v += noise(p) * 0.1250;
    return v;
}

float linearizeDepth(float z, float n, float f) {
    return (n * f) / (f - z * (f - n));
}

void main() {
    vec2 uv = v_texCoord;

    // 1. Plasma procedurale supersonico con Domain Warping di Quilez
    vec2 flow = vec2(0.0, -u_time * 18.0);
    vec2 p = vec2(uv.x * 5.0, uv.y * 2.2) + flow;
    vec2 q = vec2(fbm(p), fbm(p + vec2(5.2, 1.3)));
    vec2 r = vec2(fbm(p + 3.0 * q + vec2(1.7, 9.2)), fbm(p + 3.0 * q + vec2(8.3, 2.8)));
    float plasma = fbm(p + 3.8 * r);

    // 2. Profilo della normale rispetto alla visuale (Center-to-Rim)
    vec3 norm = normalize(v_normal);
    vec3 view = normalize(v_viewDir);
    float ndv = clamp(abs(dot(norm, view)), 0.0, 1.0);
    float centerProfile = pow(ndv, 0.70);

    // 3. Calcolo Intensita Complessiva Cel
    float intensity = centerProfile * 0.78 + plasma * 0.32;

    // 4. Cel-Shading Anime a 3 Bande (Arc System Works)
    vec3 coreWhite = vec3(1.0, 1.0, 1.0);
    vec3 midColor  = u_color.rgb;
    vec3 rimColor  = u_color.rgb * vec3(0.30, 0.05, 0.50);

    float eps = 0.015;
    float tCore = smoothstep(0.76 - eps, 0.76 + eps, intensity);
    float tMid  = smoothstep(0.34 - eps, 0.34 + eps, intensity);

    vec3 finalColor = mix(rimColor, midColor, tMid);
    finalColor = mix(finalColor, coreWhite, tCore);

    // 5. Alpha Base con taglio cel netto all'esterno
    float alpha = clamp(smoothstep(0.12, 0.28, intensity) * u_color.a, 0.0, 1.0);

    // 6. Soft Depth Fading (Previene intersezione dura contro i blocchi)
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
