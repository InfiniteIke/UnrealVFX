#version 120

varying vec2 v_texCoord;
varying vec3 v_normal;
varying vec3 v_viewDir;

uniform float u_time;
uniform float u_spikeAmount;

float animeSaw(float x, float power) {
    float s = abs(fract(x) - 0.5) * 2.0;
    return pow(s, power);
}

void main() {
    v_texCoord = gl_MultiTexCoord0.xy;
    vec3 normal = gl_Normal;
    vec4 localPos = gl_Vertex;

    if (u_spikeAmount > 0.001) {
        float angle = atan(normal.x, normal.y);
        float zPos = localPos.z;

        float wave = zPos * 1.5 - u_time * 12.0;
        float spike1 = animeSaw(angle * 1.5 + wave * 0.3, 2.5);
        float spike2 = animeSaw(angle * 3.0 - u_time * 18.0, 1.8) * 0.4;

        float displacement = (spike1 + spike2) * u_spikeAmount;
        localPos.xyz += normal * displacement;
    }

    v_normal = normalize(gl_NormalMatrix * normal);

    vec4 viewPos = gl_ModelViewMatrix * localPos;
    v_viewDir = normalize(-viewPos.xyz);

    gl_Position = gl_ModelViewProjectionMatrix * localPos;
}
