#version 120

varying vec2 v_texCoord;
varying vec3 v_camDir;
varying vec3 v_normal;

uniform float u_time;
uniform float u_progress;
uniform vec4 u_color;

uniform sampler2D u_depthTexture;
uniform vec2 u_screenResolution;
uniform float u_near;
uniform float u_far;
uniform float u_fadeDistance;

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
    float radialNorm = clamp(v_texCoord.y, 0.0, 1.0);
    float r = mix(1.65, 5.4, radialNorm);

    // Velocita orbitale kepleriana differenziale: omega = K / (r * sqrt(r))
    float omega = 3.2 / (r * sqrt(r));
    float angle = v_texCoord.x * 6.2831853;
    float flowAngle = angle - omega * u_time * 3.2;

    // Turbolenza del plasma a piu ottave (fBm)
    float plasma = fbm(vec2(r * 4.8, flowAngle * 2.6)) * 0.55 +
                   noise(vec2(r * 9.5, flowAngle * 5.2 - u_time * 1.8)) * 0.45;

    // Relativistic Doppler Beaming
    // Il lato del disco che ruota verso l'osservatore viene incrementato in luminosita ed energia
    float cosOrbit = sin(angle);
    float vSpeed = sqrt(1.0 / r) * 0.60;
    float doppler = 1.0 / max(0.2, (1.0 - vSpeed * cosOrbit));

    // Temperatura effettiva continua
    float baseTemp = pow(1.0 - radialNorm, 1.25) * 1.35;
    float effTemp = baseTemp * pow(doppler, 1.70) * (0.80 + plasma * 0.40);

    // Spettro termico continuo a 5 stop:
    // Rosso cupo -> Ambra solare -> Oro brillante -> Bianco incandescente -> Ciano caldo
    vec3 colDarkRed   = vec3(0.58, 0.08, 0.01);
    vec3 colWarmAmber = vec3(0.95, 0.40, 0.05);
    vec3 colGold      = vec3(1.00, 0.76, 0.20);
    vec3 colWhite     = vec3(1.00, 0.96, 0.88);
    vec3 colHotCyan   = vec3(0.80, 0.94, 1.00);

    float t = clamp(effTemp, 0.0, 1.6);
    vec3 diskColor = colDarkRed;
    diskColor = mix(diskColor, colWarmAmber, smoothstep(0.04, 0.28, t));
    diskColor = mix(diskColor, colGold, smoothstep(0.28, 0.62, t));
    diskColor = mix(diskColor, colWhite, smoothstep(0.62, 0.98, t));
    diskColor = mix(diskColor, colHotCyan, smoothstep(0.98, 1.45, t));

    // Dissolvenza morbida ai bordi interni (ISCO) ed esterni del disco
    float innerFade = smoothstep(0.0, 0.14, radialNorm);
    float outerFade = 1.0 - smoothstep(0.72, 1.0, radialNorm);
    float finalAlpha = innerFade * outerFade * clamp(0.75 + baseTemp * 0.25, 0.0, 1.0);

    // Scarta frammenti trasparenti per ottimizzare il fill-rate
    if (finalAlpha < 0.015) {
        discard;
    }

    // Soft Depth Fading vicino ai blocchi del mondo
    if (u_fadeDistance > 0.001 && u_screenResolution.x > 0.0) {
        vec2 screenUV = gl_FragCoord.xy / u_screenResolution;
        float rawSceneZ = texture2D(u_depthTexture, screenUV).r;
        float sceneZ = linearizeDepth(rawSceneZ, u_near, u_far);
        float fragZ  = linearizeDepth(gl_FragCoord.z, u_near, u_far);

        float deltaZ = sceneZ - fragZ;
        float depthFade = clamp(deltaZ / u_fadeDistance, 0.0, 1.0);
        depthFade = smoothstep(0.0, 1.0, depthFade);
        finalAlpha *= depthFade;
        diskColor *= depthFade;
    }

    // Emissione edge-on: quando il disco e visto di taglio, i bordi emettono
    // un sottile bagliore (il gas caldo emette luce in tutte le direzioni).
    // Rende visibile lo spessore 3D del profilo lenticolare.
    float viewDot = abs(dot(v_normal, v_camDir));
    float edgeGlow = pow(1.0 - viewDot, 3.5) * 0.30;
    diskColor += diskColor * edgeGlow;

    gl_FragColor = vec4(diskColor * u_color.rgb * u_color.a, finalAlpha * u_color.a);
}
