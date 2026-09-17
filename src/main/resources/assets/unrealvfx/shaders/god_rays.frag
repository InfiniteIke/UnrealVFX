#version 120

uniform sampler2D u_depthTexture;
uniform vec2 u_screenResolution;
uniform vec3 u_lightScreenPos; // x, y in [0, 1] screen UV, z in [0, 1] depth
uniform vec3 u_lightColor;
uniform vec3 u_coreColor;
uniform float u_intensity;
uniform float u_density;
uniform float u_decay;
uniform float u_weight;

const int NUM_SAMPLES = 28;

void main() {
    vec2 screenUV = gl_FragCoord.xy / u_screenResolution;
    
    // Vettore dal pixel corrente verso la posizione a schermo della sorgente luminosa
    vec2 deltaUV = (u_lightScreenPos.xy - screenUV) * (1.0 / float(NUM_SAMPLES)) * clamp(u_density, 0.1, 2.0);
    vec2 curUV = screenUV;
    
    float illuminationDecay = 1.0;
    float totalScattering = 0.0;
    
    for (int i = 0; i < NUM_SAMPLES; i++) {
        curUV += deltaUV;
        if (curUV.x >= 0.0 && curUV.x <= 1.0 && curUV.y >= 0.0 && curUV.y <= 1.0) {
            float sampleDepth = texture2D(u_depthTexture, curUV).r;
            // Se la profondita della scena e dietro o attorno alla luce, il raggio non e bloccato
            if (sampleDepth >= u_lightScreenPos.z - 0.003) {
                totalScattering += illuminationDecay * u_weight;
            }
        }
        illuminationDecay *= clamp(u_decay, 0.7, 0.99);
    }
    
    // Limita l'accumulo totale di scattering per evitare il whiteout dello schermo
    totalScattering = clamp(totalScattering, 0.0, 0.35);

    // Scattering di fase in avanti di Henyey-Greenstein (simulazione fisica Mie scattering di aerosol atmosferici)
    const float g = 0.55;
    const float g2 = 0.3025; // g * g
    vec3 rayDir = normalize(vec3((screenUV - u_lightScreenPos.xy) * 1.6, 1.0));
    float cosTheta = clamp(rayDir.z, 0.0, 1.0);
    float denom = 1.0 + g2 - 2.0 * g * cosTheta;
    float hgPhase = (1.0 - g2) / pow(max(denom, 0.05), 1.5);
    float phaseFactor = clamp(hgPhase * 0.18, 0.05, 1.8);

    // Distanza radiale dalla sorgente a schermo per vignettatura naturale e morbida
    float distToCenter = length(screenUV - u_lightScreenPos.xy);
    float radialFalloff = pow(clamp(1.0 - distToCenter * 1.0, 0.0, 1.0), 1.3);

    float finalRayFactor = totalScattering * phaseFactor * radialFalloff * clamp(u_intensity, 0.0, 2.0);
    if (finalRayFactor <= 0.002) {
        discard;
    }

    vec3 rayColor = mix(u_lightColor, u_coreColor, 0.25) * finalRayFactor;
    gl_FragColor = vec4(rayColor, finalRayFactor);
}
