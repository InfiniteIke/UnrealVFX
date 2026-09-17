#version 120

varying vec2 v_texCoord;

uniform sampler2D u_texture;
uniform sampler2D u_depthTexture;
uniform sampler2D u_historyTexture;
uniform vec2 u_resolution;
uniform float u_time;
uniform float u_steppedTime;
uniform float u_drawSpeed;
uniform float u_fadeSpeed;
uniform float u_reset;
uniform float u_intensity;
uniform float u_near;
uniform float u_far;

float linearizeDepth(float z) {
    float n = (u_near > 0.001) ? u_near : 0.05;
    float f = (u_far > 1.0) ? u_far : 256.0;
    return (n * f) / (f - z * (f - n));
}

float hash(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453123);
}

float getLuma(vec3 c) {
    return dot(c, vec3(0.299, 0.587, 0.114));
}

void main() {
    vec2 px = 1.0 / max(vec2(1.0), u_resolution);

    // 1. ANIME SAKUGA LINE BOIL (Micro-vibrazione a 12 FPS)
    float frameSeed = fract(sin(u_steppedTime * 127.83) * 43758.5453);
    vec2 jitter = vec2(sin(frameSeed * 6.283), cos(frameSeed * 6.283)) * px * 1.35;
    vec2 uv = v_texCoord + jitter;

    // Colore e luminanza originale al centro
    vec4 baseTex = texture2D(u_texture, v_texCoord);
    float lumaC = getLuma(texture2D(u_texture, uv).rgb);

    // Profondita linearizzata
    float rawDepthC = texture2D(u_depthTexture, uv).r;
    float depthC = linearizeDepth(rawDepthC);

    // 2. EDGE DETECTION SOBEL
    float lumaTL = getLuma(texture2D(u_texture, uv + vec2(-px.x,  px.y)).rgb);
    float lumaT  = getLuma(texture2D(u_texture, uv + vec2(  0.0,  px.y)).rgb);
    float lumaTR = getLuma(texture2D(u_texture, uv + vec2( px.x,  px.y)).rgb);
    float lumaL  = getLuma(texture2D(u_texture, uv + vec2(-px.x,  0.0 )).rgb);
    float lumaR  = getLuma(texture2D(u_texture, uv + vec2( px.x,  0.0 )).rgb);
    float lumaBL = getLuma(texture2D(u_texture, uv + vec2(-px.x, -px.y)).rgb);
    float lumaB  = getLuma(texture2D(u_texture, uv + vec2(  0.0, -px.y)).rgb);
    float lumaBR = getLuma(texture2D(u_texture, uv + vec2( px.x, -px.y)).rgb);

    float gx = (lumaTR + 2.0 * lumaR + lumaBR) - (lumaTL + 2.0 * lumaL + lumaBL);
    float gy = (lumaTL + 2.0 * lumaT + lumaTR) - (lumaBL + 2.0 * lumaB + lumaBR);
    float edgeLuma = sqrt(gx * gx + gy * gy);

    // 3. DEPTH DISCONTINUITY
    float depthL = linearizeDepth(texture2D(u_depthTexture, uv + vec2(-px.x * 1.5, 0.0)).r);
    float depthR = linearizeDepth(texture2D(u_depthTexture, uv + vec2( px.x * 1.5, 0.0)).r);
    float depthT = linearizeDepth(texture2D(u_depthTexture, uv + vec2(0.0,  px.y * 1.5)).r);
    float depthB = linearizeDepth(texture2D(u_depthTexture, uv + vec2(0.0, -px.y * 1.5)).r);

    float depthDiff = abs(depthC - depthL) + abs(depthC - depthR) +
                      abs(depthC - depthT) + abs(depthC - depthB);
    float edgeDepth = smoothstep(0.06, 0.40, depthDiff / max(0.5, depthC * 0.15));

    // Bordo di luminanza pulito (solo forti contrasti, esclude micro-variazioni interne delle texture dei blocchi)
    float cleanEdgeLuma = smoothstep(0.22, 0.55, edgeLuma);
    // Bordo geometrico di profondita (contorno esatto dei cubi, colline ed entita)
    float rawEdge = max(cleanEdgeLuma * 2.2, edgeDepth * 3.2);

    // 4. TRATTO A MATITA BIANCO PULITO SUI SOLI BORDI
    vec2 grainCoord = gl_FragCoord.xy * 0.75 + jitter * 800.0;
    float pencilGrain = hash(grainCoord) * 0.30 +
                        hash(grainCoord * 2.13 + vec2(frameSeed * 10.0)) * 0.20;

    // Solo i bordi vengono tracciati, tutto il resto rimane nero assoluto (zero riempimenti)
    float strokeThreshold = 0.32 - pencilGrain * 0.10;
    float targetLine = smoothstep(strokeThreshold, strokeThreshold + 0.28, rawEdge);

    // 6. ACCUMULO TEMPORALE E DISEGNO PROGRESSIVO IN MOVIMENTO
    // Legge il valore del tratto dal frame storico precedente
    vec4 prevPixel = texture2D(u_historyTexture, v_texCoord);
    float prevLine = prevPixel.r;

    if (u_reset > 0.5) {
        prevLine = targetLine;
    }

    float currentLine;
    if (targetLine > prevLine) {
        // NUOVO TRATTO IN ARRIVO (Camera girata o mob mosso):
        // La linea non scatta all'istante, ma viene disegnata progressivamente
        // con graffiature veloci di grafite sulla carta
        float scratch = hash(gl_FragCoord.xy * 1.8 + vec2(u_time * 50.0, frameSeed * 25.0));
        float leadScratch = 0.70 + scratch * 0.60;
        currentLine = prevLine + (targetLine - prevLine) * clamp(u_drawSpeed * leadScratch, 0.05, 1.0);
    } else {
        // POSA PRECEDENTE ABBANDONATA DAL MOVIMENTO:
        // Dissolvenza morbida come un alone a carboncino (scia di bozzetto)
        currentLine = max(targetLine, prevLine * u_fadeSpeed);
    }

    currentLine = clamp(currentLine, 0.0, 1.0);

    // 7. COMPOSIZIONE FINALE INVERTITA:
    vec3 backColor = vec3(0.008, 0.008, 0.010);
    vec3 pencilColor = vec3(0.98, 0.98, 0.95);

    vec3 sketchFinal = mix(backColor, pencilColor, currentLine);
    vec3 result = mix(baseTex.rgb, sketchFinal, clamp(u_intensity, 0.0, 1.0));

    // Scrive il bianco/nero del disegno su RGB e alpha 1.0
    gl_FragColor = vec4(result, 1.0);
}
