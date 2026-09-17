#version 120

varying vec2 v_uv;

uniform float u_viewElevation; // Angolo verticale di elevazione del giocatore (in radianti)
uniform float u_viewAzimuth;   // Angolo orizzontale di rotazione del giocatore nel mondo
uniform float u_camDistance;   // Distanza della telecamera in blocchi
uniform float u_meshScale;     // Raggio dell'effetto

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

void main() {
    // Angolo di inclinazione dell'osservatore rispetto al piano d'accrescimento (in radianti)
    // Con lieve inclinazione naturale (~6.5 gradi = 0.11 rad)
    float theta = clamp(u_viewElevation + 0.11, -1.42, 1.42);
    float cosT = cos(theta);
    float sinT = sin(theta);

    // Sistema di coordinate della camera rivolta al centro del buco nero
    vec3 forward = vec3(0.0, -sinT, cosT);
    vec3 right   = vec3(1.0, 0.0, 0.0);
    vec3 up      = vec3(0.0, cosT, sinT);

    float camDist = 9.2;
    vec3 rayOrigin = vec3(0.0, camDist * sinT, -camDist * cosT);
    vec3 rayDir = normalize(forward * 2.2 + right * (v_uv.x * 1.55) + up * (v_uv.y * 1.55));

    // Parametri Fisici di Gargantua (Schwarzschild Metric)
    float rHorizon = 1.0;     // Orizzonte degli Eventi
    float rISCO    = 1.55;    // Innermost Stable Circular Orbit
    float rOuter   = 5.2;     // Raggio esterno del disco d'accrescimento

    vec3 pos = rayOrigin;
    vec3 dir = rayDir;
    float dt = 0.28;

    vec3 accumulatedColor = vec3(0.0);
    float transmittance = 1.0;
    bool hitHorizon = false;

    // Raymarching Geodesico Relativistico Front-to-Back
    for (int step = 0; step < 36; step++) {
        float r = length(pos);

        // 1. Cattura nell'Orizzonte degli Eventi: il raggio si ferma IMMEDIATAMENTE
        // Nessuna luce dietro la singolarità potrà MAI oltrepassarla
        if (r < rHorizon) {
            hitHorizon = true;
            transmittance = 0.0;
            break;
        }

        if (r > 13.0 && dot(pos, dir) > 0.0) {
            break;
        }

        // 2. Curvatura gravitazionale verso la singolarità: delta_v = -(1.48 * r_s / r^4) * p * dt
        vec3 accel = - (1.48 * rHorizon / (r * r * r * r)) * pos;
        dir = normalize(dir + accel * dt);
        vec3 nextPos = pos + dir * dt;

        // 3. Intersezione con il piano equatoriale del disco d'accrescimento (y = 0)
        if (pos.y * nextPos.y <= 0.0) {
            float s = -pos.y / (nextPos.y - pos.y);
            vec3 hitPos = mix(pos, nextPos, s);
            float rDisk = length(hitPos.xz);

            if (rDisk >= rISCO && rDisk <= rOuter) {
                float normR = (rDisk - rISCO) / (rOuter - rISCO);

                // Angolo di rotazione polare sul disco, sincronizzato con la visuale orizzontale
                float phi = atan(hitPos.z, hitPos.x);
                float omega = 2.6 / (rDisk * sqrt(rDisk));
                float flowAngle = phi - u_viewAzimuth - omega * u_time * 3.5;

                // Striature e turbolenza del plasma morbida e armoniosamente mischiata
                float plasmaNoise = fbm(vec2(rDisk * 5.0, flowAngle * 3.0)) * 0.55 +
                                    noise(vec2(rDisk * 10.0 - u_time * 1.5, flowAngle * 6.0)) * 0.45;

                // Beaming Doppler Relativistico
                vec3 vOrbit = normalize(vec3(-hitPos.z, 0.0, hitPos.x));
                float vSpeed = sqrt(1.0 / rDisk) * 0.62;
                float cosPsi = dot(vOrbit, -dir);

                float gamma = 1.0 / sqrt(max(0.05, 1.0 - vSpeed * vSpeed));
                float dopplerFactor = 1.0 / (gamma * (1.0 - vSpeed * cosPsi));

                // Temperatura effettiva continua (sfumata senza scalini o bande fisse)
                float baseTemp = pow(1.0 - normR, 1.25) * (1.0 - sqrt(rISCO / rDisk) * 0.90);
                float effTemp = baseTemp * pow(dopplerFactor, 1.75);

                // Spettro termico continuo a 5 stop:
                // Rosso cupo -> Ambra solare -> Oro brillante -> Bianco incandescente -> Ciano caldo
                vec3 colDarkRed   = vec3(0.55, 0.08, 0.01);
                vec3 colWarmAmber = vec3(0.92, 0.38, 0.05);
                vec3 colGold      = vec3(1.00, 0.74, 0.20);
                vec3 colWhite     = vec3(1.00, 0.96, 0.88);
                vec3 colHotCyan   = vec3(0.82, 0.94, 1.00);

                float t = clamp(effTemp, 0.0, 1.5);
                vec3 diskColor = colDarkRed;
                diskColor = mix(diskColor, colWarmAmber, smoothstep(0.04, 0.28, t));
                diskColor = mix(diskColor, colGold, smoothstep(0.28, 0.62, t));
                diskColor = mix(diskColor, colWhite, smoothstep(0.62, 0.98, t));
                diskColor = mix(diskColor, colHotCyan, smoothstep(0.98, 1.40, t));

                // Integrazione morbida e naturale del plasma
                diskColor *= (0.75 + plasmaNoise * 0.35);

                // Densità con bordi sfumati dolcemente sia all'ISCO che all'esterno
                float diskDensity = smoothstep(rISCO, rISCO + 0.30, rDisk) *
                                    (1.0 - smoothstep(rOuter - 0.85, rOuter, rDisk));
                float diskAlpha = clamp(diskDensity * (0.65 + baseTemp * 0.35), 0.0, 1.0);

                accumulatedColor += diskColor * diskAlpha * transmittance;
                transmittance *= (1.0 - diskAlpha * 0.88);
            }
        }

        // 4. Anello Fotonico soffuso a r ~ 1.52 r_s
        float photonDist = abs(r - 1.52 * rHorizon);
        if (photonDist < 0.14) {
            float photonGlow = exp(-photonDist * 28.0) * 0.38;
            accumulatedColor += vec3(1.0, 0.96, 0.88) * photonGlow * transmittance;
        }

        pos = nextPos;
    }

    // Se il raggio è caduto nell'Orizzonte: NERO ASSOLUTO E OPACITÀ 100%
    if (hitHorizon) {
        accumulatedColor = vec3(0.0);
        transmittance = 0.0;
    }

    float finalAlpha = clamp(1.0 - transmittance, 0.0, 1.0);
    if (hitHorizon) {
        finalAlpha = 1.0;
    }

    // Scarta pixel completamente trasparenti per liberare il fill-rate
    if (finalAlpha < 0.012) {
        discard;
    }

    // Soft Depth Fading vicino ai blocchi del terreno
    if (u_fadeDistance > 0.001 && u_screenResolution.x > 0.0) {
        vec2 screenUV = gl_FragCoord.xy / u_screenResolution;
        float rawSceneZ = texture2D(u_depthTexture, screenUV).r;
        float sceneZ = linearizeDepth(rawSceneZ, u_near, u_far);
        float fragZ  = linearizeDepth(gl_FragCoord.z, u_near, u_far);

        float deltaZ = sceneZ - fragZ;
        float depthFade = clamp(deltaZ / u_fadeDistance, 0.0, 1.0);
        depthFade = smoothstep(0.0, 1.0, depthFade);
        finalAlpha *= depthFade;
        accumulatedColor *= depthFade;
    }

    gl_FragColor = vec4(accumulatedColor * u_color.rgb * u_color.a, finalAlpha * u_color.a);
}
