#version 120

varying vec2 v_texCoord;
varying vec3 v_normal;
varying vec3 v_viewDir;
varying vec3 v_worldPos;

uniform float u_time;

void main() {
    v_texCoord = gl_MultiTexCoord0.xy;
    vec3 normal = normalize(gl_Normal);

    // Leggero respiro del vuoto cosmico: denso, pesante e minaccioso
    float breathe = sin(u_time * 5.0) * 0.02;
    vec4 displacedVertex = gl_Vertex + vec4(normal * breathe, 0.0);

    v_normal = normalize(gl_NormalMatrix * normal);
    vec4 viewPos = gl_ModelViewMatrix * displacedVertex;
    v_viewDir = normalize(-viewPos.xyz);
    v_worldPos = displacedVertex.xyz;

    gl_Position = gl_ModelViewProjectionMatrix * displacedVertex;
}
