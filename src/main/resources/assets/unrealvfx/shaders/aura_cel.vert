#version 120

varying vec2 v_texCoord;
varying vec3 v_normal;
varying vec3 v_viewDir;
varying vec3 v_localPos;

uniform float u_time;
uniform float u_distortionStrength;
uniform float u_steppedFps;

void main() {
    v_texCoord = gl_MultiTexCoord0.xy;
    vec3 norm = normalize(gl_NormalMatrix * gl_Normal);
    v_normal = norm;

    vec4 pos = gl_Vertex;

    // Anime Ki Flame dynamic vertex displacement / fluttering
    float dist = (u_distortionStrength > 0.001) ? u_distortionStrength : 0.12;
    float waveY = pos.y;
    // Quantized Stepped Sakuga Timing ("On 2s" / hand-drawn anime cadence)
    float t = (u_steppedFps > 0.0) ? floor(u_time * u_steppedFps + 0.0001) / u_steppedFps : u_time;
    // Harmonic wave flaring outward and upward
    float wave1 = sin(waveY * 3.2 - t * 6.5 + pos.x * 2.2) * cos(pos.z * 2.2 + t * 5.0);
    float wave2 = sin(waveY * 6.5 - t * 11.0 + pos.z * 3.0) * 0.45;
    float wave3 = sin(pos.x * 4.0 + pos.z * 4.0 - t * 8.0) * 0.25;

    // Upward flare expansion: ki flames expand wider as they rise
    float flareFactor = clamp(waveY * 0.35 + 0.65, 0.2, 2.0);

    vec2 hNorm = (length(pos.xz) > 0.001) ? normalize(pos.xz) : vec2(1.0, 0.0);
    pos.xz += hNorm * (wave1 + wave2 + wave3) * dist * flareFactor;

    v_localPos = pos.xyz;
    vec4 viewPos = gl_ModelViewMatrix * pos;
    v_viewDir = normalize(-viewPos.xyz);

    gl_Position = gl_ModelViewProjectionMatrix * pos;
}
