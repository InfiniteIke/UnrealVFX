#version 120

varying vec3 v_normal;
varying vec3 v_camDir;

void main() {
    v_normal = normalize(gl_NormalMatrix * gl_Normal);
    vec4 viewPos = gl_ModelViewMatrix * gl_Vertex;
    v_camDir = normalize(-viewPos.xyz);
    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
}
