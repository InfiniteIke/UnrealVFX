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

float linearizeDepth(float z, float n, float f) {
    return (n * f) / (f - z * (f - n));
}

void main() {
    vec2 uv = v_texCoord * 2.0 - 1.0;
    float dist = length(uv);

    if (dist > 1.0) {
        discard;
    }

    float angle = atan(uv.y, uv.x);

    // Denti rotanti a lama di sega (plasma saw-teeth Kienzan)
    float teeth = sin(angle * 28.0 + u_time * 30.0);
    float edgeCutoff = 0.88 + teeth * 0.11;

    if (dist > edgeCutoff) {
        discard;
    }

    // Cerchi di energia concentrici cel-shaded
    float rings = sin(dist * 32.0 - u_time * 16.0) * 0.5 + 0.5;

    // Palette Kienzan / Slicer cel-shaded
    vec3 coreWhite = vec3(1.0, 1.0, 1.0);
    vec3 bodyColor = u_color.rgb;
    vec3 bladeEdge = vec3(1.0, 1.0, 0.7);

    vec3 color;
    if (dist < 0.22) {
        // Centro bianco nucleare
        color = coreWhite;
    } else if (dist > 0.72) {
        // Lama tagliente esterna
        color = mix(bodyColor, bladeEdge, (dist - 0.72) / 0.28);
    } else {
        color = mix(bodyColor, coreWhite, rings * 0.40);
    }

    float alpha = clamp(u_color.a, 0.0, 1.0);

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
