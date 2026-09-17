#version 120

varying vec2 v_texCoord;

uniform float u_progress;
uniform vec3 u_color;
uniform float u_alpha;

vec2 hash2(vec2 p) {
    p = vec2(dot(p, vec2(127.1, 311.7)), dot(p, vec2(269.5, 183.3)));
    return fract(sin(p) * 43758.5453);
}

// Calcolo distanza dai bordi delle celle Voronoi per generare crepe realistiche
float voronoiBorder(vec2 p) {
    vec2 n = floor(p);
    vec2 f = fract(p);

    vec2 mg, mr;
    float md = 8.0;
    for (int j = -1; j <= 1; j++) {
        for (int i = -1; i <= 1; i++) {
            vec2 g = vec2(float(i), float(j));
            vec2 o = hash2(n + g);
            vec2 r = g + o - f;
            float d = dot(r, r);
            if (d < md) {
                md = d;
                mr = r;
                mg = g;
            }
        }
    }

    md = 8.0;
    for (int j = -2; j <= 2; j++) {
        for (int i = -2; i <= 2; i++) {
            vec2 g = mg + vec2(float(i), float(j));
            vec2 o = hash2(n + g);
            vec2 r = g + o - f;
            if (dot(mr - r, mr - r) > 0.00001) {
                md = min(md, dot(0.5 * (mr + r), normalize(r - mr)));
            }
        }
    }
    return md;
}

void main() {
    vec2 uv = (v_texCoord - 0.5) * 2.0;
    float dist = length(uv);
    if (dist > 1.0) discard;

    // Dissolvenza radiale ai bordi del cerchio d'impatto
    float radialFade = 1.0 - smoothstep(0.7, 1.0, dist);

    // Genera la rete di crepe
    float cracks = voronoiBorder(uv * 7.5);
    // Linea della crepa sottile e affilata
    float crackMask = 1.0 - smoothstep(0.015, 0.065, cracks);

    // Terra bruciata/carbonizzata tra le crepe
    vec3 charcoal = vec3(0.08, 0.05, 0.08);

    // Energia fusa che risplende all'interno delle crepe
    vec3 energyGlow = mix(u_color, vec3(1.0, 1.0, 1.0), 0.5);

    vec3 finalColor = mix(charcoal, energyGlow, crackMask);
    float finalAlpha = (crackMask * 0.95 + 0.55 * (1.0 - dist)) * radialFade * u_alpha;

    gl_FragColor = vec4(finalColor, clamp(finalAlpha, 0.0, 1.0));
}
