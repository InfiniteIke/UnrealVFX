#version 120

varying vec2 v_texCoord;
varying vec3 v_camDir;

void main() {
    v_texCoord = gl_MultiTexCoord0.xy;

    vec4 viewPos = gl_ModelViewMatrix * gl_Vertex;
    v_camDir = normalize(-viewPos.xyz);

    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
}
