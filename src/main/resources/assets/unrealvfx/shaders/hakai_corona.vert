#version 120

varying vec2 v_texCoord;
varying vec3 v_normal;
varying vec3 v_viewDir;

uniform float u_time;

void main() {
    v_texCoord = gl_MultiTexCoord0.xy;
    vec3 normal = normalize(gl_Normal);

    // Spostamento vertici ad alta frequenza per generare le fiamme/punte di plasma ruggenti
    float s1 = sin(gl_Vertex.x * 6.0 + u_time * 9.0);
    float s2 = cos(gl_Vertex.y * 7.0 - u_time * 11.0);
    float s3 = sin(gl_Vertex.z * 6.5 + u_time * 8.0);
    float turbulence = s1 * s2 * s3;

    float spike = pow(max(0.0, turbulence), 1.8) * 0.40;
    vec4 displaced = gl_Vertex + vec4(normal * spike, 0.0);

    v_normal = normalize(gl_NormalMatrix * normal);
    vec4 viewPos = gl_ModelViewMatrix * displaced;
    v_viewDir = normalize(-viewPos.xyz);

    gl_Position = gl_ModelViewProjectionMatrix * displaced;
}
