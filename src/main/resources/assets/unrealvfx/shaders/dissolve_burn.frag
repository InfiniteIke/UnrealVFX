#version 120

varying vec2 v_texCoord;
varying vec3 v_normal;
varying vec3 v_localPos;
varying vec3 v_viewDir;

uniform sampler2D u_depthTexture;
uniform vec2 u_screenResolution;
uniform float u_near;
uniform float u_far;
uniform float u_fadeDistance;

uniform float u_time;
uniform float u_progress;
uniform vec4 u_color;
uniform vec3 u_hotCoreColor;

uniform float u_noiseScale;
uniform float u_noiseSpeed;
uniform float u_bloomMultiplier;
uniform float u_invertedCore;
uniform float u_filamentStrength;

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
    float scale = (u_noiseScale > 0.01) ? u_noiseScale : 3.5;
    float speed = (u_noiseSpeed > 0.0) ? u_noiseSpeed : 0.8;

    // Coordinate miste UV e posizionali 3D per supportare qualsiasi primitiva
    vec2 uvCoord = v_texCoord * scale + vec2(u_time * speed * 0.12, u_time * speed * 0.07);
    vec2 posCoord = (v_localPos.xy + v_localPos.yz * 0.5) * (scale * 0.4);
    float n = fbm2D(uvCoord + posCoord);

    // Mappatura morbida del rumore tra 0.0 e 1.0
    n = clamp(n * 1.25 - 0.1, 0.0, 1.0);

    // Soglia di erosione basata sul progresso dell'effetto (0.0 -> 1.0)
    float cut = clamp(u_progress, 0.0, 1.0);
    if (u_invertedCore > 0.5) {
        // Modalita Materializzazione / Manifestazione inversa (da scintille a mesh solida)
        cut = 1.0 - cut;
    }

    // Se il valore del rumore e sotto la soglia di taglio, il frammento e dissolto
    if (n < cut) {
        discard;
    }

    // Spessore del bordo di combustione incandescente
    float burnWidth = (u_filamentStrength > 0.001) ? u_filamentStrength : 0.085;
    float edgeDist = n - cut;

    vec3 baseRgb = u_color.rgb;
    vec3 finalRgb;
    float finalAlpha = u_color.a;

    // Shading direzionale morbido
    vec3 norm = normalize(v_normal);
    vec3 view = normalize(v_viewDir);
    float NdotV = max(0.0, dot(norm, view));
    float diffuse = 0.55 + 0.45 * NdotV;

    if (edgeDist < burnWidth) {
        // Bordo di combustione in fiamme incandescenti (Hot Burn Edge)
        float burnFactor = 1.0 - (edgeDist / burnWidth);
        float hotCoreFactor = pow(burnFactor, 3.2);

        vec3 burnColor = (length(u_hotCoreColor) > 0.01) ? u_hotCoreColor : vec3(1.0, 0.45, 0.05);
        vec3 whiteHot = vec3(1.0, 1.0, 1.0);

        vec3 flameColor = mix(burnColor, whiteHot, hotCoreFactor);
        float bloomBoost = (u_bloomMultiplier > 0.0) ? u_bloomMultiplier : 2.5;

        finalRgb = mix(baseRgb * diffuse, flameColor * (1.2 + bloomBoost * hotCoreFactor), burnFactor);
        finalAlpha = mix(u_color.a, 1.0, burnFactor);
    } else {
        // Superficie solida della mesh: leggero bordo carbonizzato vicino alla linea di fiamma
        float charFactor = smoothstep(burnWidth, burnWidth * 2.2, edgeDist);
        finalRgb = mix(baseRgb * 0.25, baseRgb * diffuse, charFactor);
    }

    // Soft Depth Fading contro il terreno voxel di Minecraft
    if (u_fadeDistance > 0.001 && u_screenResolution.x > 0.0) {
        vec2 screenUV = gl_FragCoord.xy / u_screenResolution;
        float rawSceneZ = texture2D(u_depthTexture, screenUV).r;
        float sceneZ = linearizeDepth(rawSceneZ, u_near, u_far);
        float fragZ  = linearizeDepth(gl_FragCoord.z, u_near, u_far);

        float deltaZ = sceneZ - fragZ;
        float depthFade = clamp(deltaZ / u_fadeDistance, 0.0, 1.0);
        finalAlpha *= smoothstep(0.0, 1.0, depthFade);
    }

    if (finalAlpha < 0.01) {
        discard;
    }

    gl_FragColor = vec4(finalRgb, finalAlpha);
}
