#version 120

varying vec2 v_texCoord;

uniform sampler2D u_depthTexture;
uniform vec2 u_screenResolution;
uniform float u_near;
uniform float u_far;
uniform float u_fadeDistance;

uniform float u_progress;
uniform vec4 u_color;

float linearizeDepth(float z, float n, float f) {
    return (n * f) / (f - z * (f - n));
}

void main() {
    float ringCoord = v_texCoord.y;

    // Profilo a campana affilato per la cresta dell'onda d'urto
    float ringIntensity = sin(clamp(ringCoord, 0.0, 1.0) * 3.14159);
    ringIntensity = pow(ringIntensity, 2.0);

    vec3 ringColor = mix(u_color.rgb, vec3(1.0, 1.0, 1.0), ringIntensity * 0.7);
    float alpha = ringIntensity * u_color.a;

    // Soft Depth Fading: si fonde col terreno senza scalini
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

    gl_FragColor = vec4(ringColor, alpha);
}
