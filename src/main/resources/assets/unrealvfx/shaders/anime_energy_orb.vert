#version 120

varying vec2 v_texCoord;
varying vec3 v_normal;
varying vec3 v_viewDir;

uniform float u_time;

void main() {
    v_texCoord = gl_MultiTexCoord0.xy;
    vec3 normal = normalize(gl_Normal);

    // Micro-pulsazione sferica organica
    float pulse = sin(u_time * 7.0) * 0.02;
    vec4 displaced = gl_Vertex + vec4(normal * pulse, 0.0);

    v_normal = normalize(gl_NormalMatrix * normal);
    vec4 viewPos = gl_ModelViewMatrix * displaced;
    v_viewDir = normalize(-viewPos.xyz);

    gl_Position = gl_ModelViewProjectionMatrix * displaced;
}
