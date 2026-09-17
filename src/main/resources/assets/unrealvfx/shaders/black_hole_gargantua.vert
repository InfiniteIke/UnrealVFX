#version 120

varying vec2 v_uv;

void main() {
    v_uv = gl_Vertex.xy;
    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
}
