#version 120

uniform sampler2D u_depthTexture;
uniform vec2 u_screenResolution;
uniform vec3 u_lightScreenPos;
uniform vec3 u_color;
uniform vec3 u_coreColor;
uniform float u_flareIntensity;
uniform float u_time;

varying vec2 v_texCoord;

void main() {
    // Controllo occlusione ottica: se il centro della sorgente e coperto da un blocco
    float occ = 1.0;
    if (u_lightScreenPos.x >= 0.0 && u_lightScreenPos.x <= 1.0 && u_lightScreenPos.y >= 0.0 && u_lightScreenPos.y <= 1.0) {
        float centerZ = texture2D(u_depthTexture, u_lightScreenPos.xy).r;
        if (centerZ < u_lightScreenPos.z - 0.0035 && centerZ > 0.001) {
            occ = 0.05; // Forte oscuramento se dietro un blocco
        }
    }

    vec2 p = v_texCoord * 2.0 - 1.0;
    float r = length(p);
    if (r > 1.0) {
        discard;
    }

    // 1. Corona ottica centrale
    float corona = pow(clamp(1.0 - r, 0.0, 1.0), 3.0);

    // 2. Raggi di diffrazione a stella (Starburst spikes)
    float angle = atan(p.y, p.x) + u_time * 0.12;
    float spikes = pow(max(0.0, cos(angle * 4.0)), 16.0) * pow(clamp(1.0 - r, 0.0, 1.0), 1.5);

    // 3. Striscia luminosa orizzontale anamorfica (cinematic anamorphic flare)
    float streak = exp(-abs(p.y) * 28.0) * exp(-abs(p.x) * 1.8) * 1.6;

    float flare = (corona * 0.55 + spikes * 0.65 + streak * 0.9) * occ * clamp(u_flareIntensity, 0.0, 1.5);
    flare = clamp(flare, 0.0, 0.6);
    if (flare <= 0.005) {
        discard;
    }

    vec3 col = mix(u_color, u_coreColor, clamp(pow(clamp(1.0 - r * 1.8, 0.0, 1.0), 2.0), 0.0, 1.0));
    gl_FragColor = vec4(col * flare, flare);
}
