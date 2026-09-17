#version 120

varying vec2 v_texCoord;
varying vec3 v_camDir;

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

void main() {
    float radialNorm = clamp(v_texCoord.y, 0.0, 1.0);
    float angle = v_texCoord.x * 6.2831853;

    // Turbolenza del plasma lungo l'arco di lensing
    float plasma = noise(vec2(radialNorm * 8.0, angle * 4.0 - u_time * 1.2)) * 0.5 + 0.5;

    // Luminosità concentrata nell'arco superiore e inferiore
    float temp = pow(1.0 - radialNorm, 1.35) * (0.85 + plasma * 0.35);

    // Spettro termico continuo:
    // Rosso cupo -> Ambra solare -> Oro brillante -> Bianco incandescente
    vec3 colDarkRed   = vec3(0.55, 0.08, 0.01);
    vec3 colWarmAmber = vec3(0.95, 0.40, 0.05);
    vec3 colGold      = vec3(1.00, 0.76, 0.20);
    vec3 colWhite     = vec3(1.00, 0.96, 0.88);

    vec3 haloColor = colDarkRed;
    haloColor = mix(haloColor, colWarmAmber, smoothstep(0.05, 0.30, temp));
    haloColor = mix(haloColor, colGold, smoothstep(0.30, 0.65, temp));
    haloColor = mix(haloColor, colWhite, smoothstep(0.65, 1.00, temp));

    // Dissolvenza morbida
    float innerFade = smoothstep(0.0, 0.16, radialNorm);
    float outerFade = 1.0 - smoothstep(0.70, 1.0, radialNorm);
    float alpha = innerFade * outerFade * 0.82;

    if (alpha < 0.015) {
        discard;
    }

    gl_FragColor = vec4(haloColor * u_color.rgb * u_color.a, alpha * u_color.a);
}
