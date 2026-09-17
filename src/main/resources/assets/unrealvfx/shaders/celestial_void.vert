#version 120

varying vec2 v_texCoord;
varying vec3 v_normal;
varying vec3 v_viewDir;
varying vec3 v_localPos;

void main() {
    v_texCoord = gl_MultiTexCoord0.xy;
    v_normal = normalize(gl_NormalMatrix * gl_Normal);
    v_localPos = gl_Vertex.xyz;

    vec4 viewPos = gl_ModelViewMatrix * gl_Vertex;
    v_viewDir = normalize(-viewPos.xyz);

    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
}
