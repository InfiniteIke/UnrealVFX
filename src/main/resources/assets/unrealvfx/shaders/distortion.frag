#version 120

varying vec2 v_texCoord;
varying vec3 v_normal;

uniform sampler2D u_screenTexture;
uniform vec2 u_screenSize;
uniform float u_strength;
uniform float u_progress;

void main() {
    vec2 screenUV = gl_FragCoord.xy / u_screenSize;

    // Profilo a campana della distorsione lungo l'anello
    float ringProfile = sin(clamp(v_texCoord.y, 0.0, 1.0) * 3.14159);
    ringProfile = pow(ringProfile, 2.0);

    // Vettore di spostamento rifrattivo
    vec2 distortion = v_normal.xz * u_strength * ringProfile * (1.0 - u_progress);

    // Aberrazione cromatica (sfasamento canali R, G, B per look anime)
    float r = texture2D(u_screenTexture, screenUV + distortion * 1.2).r;
    float g = texture2D(u_screenTexture, screenUV + distortion).g;
    float b = texture2D(u_screenTexture, screenUV + distortion * 0.8).b;

    // Bagliore sottile sulla cresta dell'onda d'urto
    vec3 resultColor = vec3(r, g, b) + vec3(0.25, 0.28, 0.35) * ringProfile;

    gl_FragColor = vec4(resultColor, 1.0);
}
