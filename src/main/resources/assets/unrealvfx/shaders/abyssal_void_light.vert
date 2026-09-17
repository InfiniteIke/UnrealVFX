#version 120

varying vec3 v_localPos;
varying vec3 v_normal;
varying vec3 v_objNormal;
varying vec3 v_viewDir;
varying vec2 v_texCoord;

void main() {
    v_localPos = gl_Vertex.xyz;
    v_objNormal = gl_Normal;
    v_normal = normalize(gl_NormalMatrix * gl_Normal);
    v_texCoord = gl_MultiTexCoord0.xy;

    vec4 eyePos = gl_ModelViewMatrix * gl_Vertex;
    v_viewDir = normalize(-eyePos.xyz);

    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
}
