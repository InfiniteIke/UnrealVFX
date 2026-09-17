#version 120

uniform sampler2D u_screenTexture;
uniform vec2 u_screenSize;
uniform float u_time;
uniform float u_strength;
uniform float u_falloffPower;
uniform int u_shape; // 0 = SPHERE, 1 = CUBE, 2 = CYLINDER
uniform float u_noiseSpeed;
uniform float u_noiseScale;
uniform vec3 u_scale;
uniform vec3 u_tintColor;
uniform float u_tintAlpha;

varying vec3 v_localPos;
varying vec3 v_normal;
varying vec3 v_objNormal;
varying vec3 v_viewDir;
varying vec2 v_screenUV;
varying vec2 v_centerUV;

float hash21(vec2 p) {
    p = fract(p * vec2(123.34, 456.21));
    p += dot(p, p + 45.32);
    return fract(p.x * p.y);
}

float noise2D(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    vec2 u = f * f * (3.0 - 2.0 * f);

    float a = hash21(i);
    float b = hash21(i + vec2(1.0, 0.0));
    float c = hash21(i + vec2(0.0, 1.0));
    float d = hash21(i + vec2(1.0, 1.0));

    return mix(mix(a, b, u.x), mix(c, d, u.x), u.y);
}

float fbm2D(vec2 p) {
    float v = 0.0;
    float a = 0.5;
    mat2 rot = mat2(0.8, 0.6, -0.6, 0.8);
    for (int i = 0; i < 3; i++) {
        v += a * noise2D(p);
        p = rot * p * 2.0;
        a *= 0.5;
    }
    return v;
}

void main() {
    vec3 norm = normalize(v_normal);
    vec3 view = normalize(v_viewDir);
    vec3 objN = abs(v_objNormal);

    float r = 0.0;

    if (u_shape == 0) {
        // SPHERE: Raggio euclideo proiettato
        float ndv = clamp(abs(dot(norm, view)), 0.0, 1.0);
        r = sqrt(clamp(1.0 - ndv * ndv, 0.0, 1.0));
    } else if (u_shape == 1) {
        // CUBE: Distanza di Chebyshev sul piano delle facce orientata in spazio oggetto
        vec3 p = abs(v_localPos * 2.0);
        if (objN.x >= objN.y && objN.x >= objN.z) {
            r = max(p.y, p.z);
        } else if (objN.y >= objN.x && objN.y >= objN.z) {
            r = max(p.x, p.z);
        } else {
            r = max(p.x, p.y);
        }
    } else {
        // CYLINDER: Raggio radiale sul piano XZ con limiti verticali in Y
        vec3 p = v_localPos * 2.0;
        if (objN.y > 0.5) {
            // Base o cima del cilindro
            r = length(p.xz);
        } else {
            // Superficie laterale del cilindro
            float ndv = clamp(abs(dot(norm, view)), 0.0, 1.0);
            float rRadial = sqrt(clamp(1.0 - ndv * ndv, 0.0, 1.0));
            float rY = abs(p.y);
            r = max(rRadial, rY);
        }
    }

    r = clamp(r, 0.0, 1.0);

    // Esponente di decadimento dal centro alla periferia: Falloff(r) = max(0.0, 1.0 - (r/R)^p)
    float pVal = (u_falloffPower > 0.01) ? u_falloffPower : 2.0;
    float falloff = max(0.0, 1.0 - pow(r, pVal));

    vec2 screenUV = gl_FragCoord.xy / u_screenSize;

    float nScale = (u_noiseScale > 0.01) ? u_noiseScale : 4.0;
    float nSpeed = (u_noiseSpeed > 0.01) ? u_noiseSpeed : 2.5;

    // Scorrimento verso l'alto tipico della convezione dell'aria calda
    vec2 waveUV = screenUV * nScale * 3.0;
    waveUV.y -= u_time * nSpeed;

    // Doppia ottava di rumore FBM per micro-ondulazioni turbolente
    float wave1 = fbm2D(waveUV);
    float wave2 = fbm2D(waveUV * 1.6 + vec2(4.3, 7.9) - vec2(0.0, u_time * nSpeed * 0.4));
    vec2 disp = vec2(wave1 - 0.5, wave2 - 0.5) * 2.0;

    // u_strength e' in "unita' naturali": 1.0 ≈ 20px max offset a 1920px larghezza.
    // Il fattore 0.015 normalizza l'intervallo ±1.0 di disp a offset UV ragionevoli.
    float amp = max(u_strength, 0.0) * 0.015;
    vec2 distortion = disp * amp * falloff;

    // Rifrazione con dispersione e aberrazione cromatica sottile (clamped to screen boundaries)
    vec2 uvR = clamp(screenUV + distortion * 1.15, 0.001, 0.999);
    vec2 uvG = clamp(screenUV + distortion * 1.00, 0.001, 0.999);
    vec2 uvB = clamp(screenUV + distortion * 0.85, 0.001, 0.999);
    float rCol = texture2D(u_screenTexture, uvR).r;
    float gCol = texture2D(u_screenTexture, uvG).g;
    float bCol = texture2D(u_screenTexture, uvB).b;
    vec3 refracted = vec3(rCol, gCol, bCol);

    // Tonalita di colore opzionale (0.0 = aria completamente trasparente, pura distorsione ottica)
    vec3 tint = u_tintColor;
    float alpha = clamp(u_tintAlpha, 0.0, 1.0);
    vec3 resultColor = mix(refracted, tint, alpha * falloff);

    gl_FragColor = vec4(resultColor, 1.0);
}
