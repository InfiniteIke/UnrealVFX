#version 120

varying vec2 v_texCoord;

uniform sampler2D u_depthTexture;
uniform vec2 u_screenResolution;
uniform float u_near;
uniform float u_far;
uniform float u_fadeDistance;

uniform float u_time;
uniform float u_progress;
uniform vec4 u_color;
uniform vec3 u_hotCoreColor;

float linearizeDepth(float z, float n, float f) {
    return (n * f) / (f - z * (f - n));
}

void main() {
    vec2 uv = v_texCoord * 2.0 - 1.0;
    float dist = length(uv);

    // Spessore dell'arco anulare (anello dal raggio 0.45 a 1.0)
    if (dist < 0.45 || dist > 1.0) {
        discard;
    }

    // Angolo polare normalizzato in [0, 1]
    float angle = (atan(uv.y, uv.x) + 3.14159265) / 6.2831853;

    // Sweep dinamico dell'arco durante la durata dell'effetto
    float arcSweep = fract(u_progress + 0.25);
    float deltaAngle = fract(angle - arcSweep + 1.0);

    // Ampiezza angolare dell'arco (circa 150 gradi = 0.42 del cerchio)
    float arcSpan = 0.42;
    if (deltaAngle > arcSpan) {
        discard;
    }

    // Posizione normalizzata lungo il fendente: 0 = coda sfumata, 1 = lama d'attacco
    float trailFactor = 1.0 - (deltaAngle / arcSpan);

    // Profilo radiale smussato ai bordi interno ed esterno
    float radialWidth = 1.0 - abs(dist - 0.725) / 0.275;
    radialWidth = smoothstep(0.0, 0.4, radialWidth);

    // Bordo d'attacco affilato e incandescente (hot edge)
    float hotEdge = pow(trailFactor, 4.0);
    vec3 hotColor = (length(u_hotCoreColor) > 0.01) ? u_hotCoreColor : vec3(1.0, 1.0, 1.0);
    vec3 finalRgb = mix(u_color.rgb, hotColor, hotEdge);

    float alpha = trailFactor * radialWidth * clamp(u_color.a, 0.0, 1.0);

    // Soft Depth Fading contro il terreno voxel di Minecraft
    if (u_fadeDistance > 0.001 && u_screenResolution.x > 0.0) {
        vec2 screenUV = gl_FragCoord.xy / u_screenResolution;
        float rawSceneZ = texture2D(u_depthTexture, screenUV).r;
        float sceneZ = linearizeDepth(rawSceneZ, u_near, u_far);
        float fragZ  = linearizeDepth(gl_FragCoord.z, u_near, u_far);

        float deltaZ = sceneZ - fragZ;
        float depthFade = clamp(deltaZ / u_fadeDistance, 0.0, 1.0);
        alpha *= smoothstep(0.0, 1.0, depthFade);
    }

    if (alpha < 0.01) {
        discard;
    }

    gl_FragColor = vec4(finalRgb * (1.2 + hotEdge * 1.5), alpha);
}
