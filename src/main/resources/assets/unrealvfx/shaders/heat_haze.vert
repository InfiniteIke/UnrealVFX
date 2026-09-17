#version 120

varying vec3 v_localPos;
varying vec3 v_normal;
varying vec3 v_objNormal;
varying vec3 v_viewDir;
varying vec2 v_screenUV;
varying vec2 v_centerUV;

void main() {
    v_localPos = gl_Vertex.xyz;
    v_objNormal = gl_Normal;
    v_normal = normalize(gl_NormalMatrix * gl_Normal);

    vec4 eyePos = gl_ModelViewMatrix * gl_Vertex;
    v_viewDir = normalize(-eyePos.xyz);

    vec4 clipPos = gl_ModelViewProjectionMatrix * gl_Vertex;
    v_screenUV = (clipPos.xy / clipPos.w) * 0.5 + 0.5;

    vec4 centerClip = gl_ModelViewProjectionMatrix * vec4(0.0, 0.0, 0.0, 1.0);
    v_centerUV = (centerClip.xy / centerClip.w) * 0.5 + 0.5;

    gl_Position = clipPos;
}
