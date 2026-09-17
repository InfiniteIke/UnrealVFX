#version 120

uniform sampler2D u_screenTexture;
uniform vec2 u_screenSize;
uniform float u_strength;

varying vec2 v_quadUV;
varying vec2 v_screenUV;
varying vec2 v_centerUV;

void main() {
    // Coordinate sul quad centrate tra -0.5 e 0.5
    vec2 pQuad = v_quadUV - vec2(0.5);
    float distQuad = length(pQuad);

    // Se fuori dal cerchio d'influenza gravitazionale (distQuad > 0.5), scarta
    if (distQuad > 0.5) {
        discard;
    }

    // ORIZZONTE DEGLI EVENTI:
    // La sfera 3D sottostante (raggio ~2.2 contro quad di scala 17.0 -> raggio ~0.13)
    // Se siamo all'interno dell'orizzonte degli eventi, SCARTA!
    // In questo modo la vera sfera 3D sottostante con il suo alone bianco fosforescente Fresnel
    // e visibile al 100% senza essere coperta da alcun disco piatto 2D.
    float rHorizon = 0.13;
    if (distQuad < rHorizon) {
        discard;
    }

    // Calcolo della deflessione relativistica dello sfondo di Minecraft
    vec2 screenUV = gl_FragCoord.xy / u_screenSize;
    vec2 delta = screenUV - v_centerUV;

    // Correzione aspect ratio per evitare deformazioni ellittiche su schermi widescreen
    float aspect = u_screenSize.x / max(1.0, u_screenSize.y);
    vec2 deltaAspect = delta;
    deltaAspect.x *= aspect;
    float distAspect = length(deltaAspect);

    if (distAspect < 0.0001) {
        discard;
    }

    // Direzione di deflessione della luce (la singolarita attrae i fotoni,
    // quindi l'immagine dello sfondo viene espansa verso l'esterno)
    vec2 dir = normalize(deltaAspect);
    dir.x /= aspect;

    // Decadimento morbido della gravita verso il bordo del quad per una fusione invisibile
    float fade = 1.0 - smoothstep(0.30, 0.495, distQuad);

    // Formula di deflessione relativistica di Einstein: ~ K / dist
    float normDist = (distQuad - rHorizon) / (0.50 - rHorizon);
    float bend = (u_strength * 0.045) / max(0.015, normDist) * fade;
    bend = min(bend, 0.12);

    vec2 uvShift = dir * bend;

    // Dispersione cromatica gravitazionale (aberrazione ottica relativistica)
    float colR = texture2D(u_screenTexture, screenUV - uvShift * 1.035).r;
    float colG = texture2D(u_screenTexture, screenUV - uvShift * 1.000).g;
    float colB = texture2D(u_screenTexture, screenUV - uvShift * 0.965).b;
    vec3 color = vec3(colR, colG, colB);

    // Anello fotonico secondario sottile di Einstein
    float photonDist = abs(distQuad - 0.17);
    float photonRing = exp(-photonDist * 40.0) * 0.40 * fade;
    color += vec3(1.0, 0.98, 0.92) * photonRing;

    gl_FragColor = vec4(color, fade);
}
