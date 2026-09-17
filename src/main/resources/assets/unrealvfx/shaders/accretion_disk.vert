#version 120

varying vec2 v_texCoord;
varying vec3 v_camDir;
varying vec3 v_normal;

uniform float u_meshScale;

void main() {
    v_texCoord = gl_MultiTexCoord0.xy;

    // Posizione del vertice e del centro in coordinate View Space (Camera a (0,0,0), guarda lungo -Z)
    vec4 viewPos = gl_ModelViewMatrix * gl_Vertex;
    vec3 centerView = (gl_ModelViewMatrix * vec4(0.0, 0.0, 0.0, 1.0)).xyz;
    vec3 relPos = viewPos.xyz - centerView;

    // DEFLESSIONE GRAVITAZIONALE DEI VERTICI (Effetto Kip Thorne - Interstellar):
    // Il raggio dell'orizzonte Rs scala dinamicamente con la grandezza impostata per il buco nero
    float Rs = 2.15;
    if (u_meshScale > 0.001) {
        Rs = u_meshScale * 0.2933;
    }
    float behind = smoothstep(0.0, Rs * 0.85, -relPos.z);

    if (behind > 0.001) {
        vec2 dirXY = relPos.xy;
        // Se la componente verticale e minima (disco visto quasi di taglio),
        // la deflessione relativistica devia la luce verso l'alto e verso il basso (archi di Thorne)
        if (abs(dirXY.y) < 0.30 * Rs) {
            float signY = 1.0;
            if (gl_Vertex.y < 0.0) {
                signY = -1.0;
            }
            dirXY.y = signY * max(abs(dirXY.y), 0.35 * Rs);
        }
        float dXY = length(dirXY);
        if (dXY > 0.01) {
            dirXY /= dXY;
            // Formula di deflessione relativistica ottica
            float bend = (Rs * Rs) / max(dXY, Rs * 0.75) * 0.75 * behind;
            bend = min(bend, Rs * 1.5);
            viewPos.xy += dirXY * bend;
        }
    }

    v_camDir = normalize(-viewPos.xyz);
    v_normal = normalize(gl_NormalMatrix * gl_Normal);

    gl_Position = gl_ProjectionMatrix * viewPos;
}
