#version 120

varying vec2 v_quadUV;
varying vec2 v_screenUV;
varying vec2 v_centerUV;

void main() {
    v_quadUV = gl_MultiTexCoord0.xy;

    // Posizione del vertice nello spazio clip (NDC)
    vec4 clipPos = gl_ModelViewProjectionMatrix * gl_Vertex;
    v_screenUV = (clipPos.xy / clipPos.w) * 0.5 + 0.5;

    // Centro della singolarita (0, 0, 0) proiettato in coordinate clip
    vec4 centerClip = gl_ModelViewProjectionMatrix * vec4(0.0, 0.0, 0.0, 1.0);
    v_centerUV = (centerClip.xy / centerClip.w) * 0.5 + 0.5;

    gl_Position = clipPos;
}
