#version 120

varying vec2 v_texCoord;
varying vec3 v_normal;
varying vec3 v_viewDir;

uniform float u_time;
uniform float u_progress;
uniform vec4 u_color;

float hash(vec2 p) {
    p = fract(p * vec2(123.34, 456.21));
    p += dot(p, p + 45.32);
    return fract(p.x * p.y);
}

float noise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);

    float a = hash(i);
    float b = hash(i + vec2(1.0, 0.0));
    float c = hash(i + vec2(0.0, 1.0));
    float d = hash(i + vec2(1.0, 1.0));

    return mix(mix(a, b, f.x), mix(c, d, f.x), f.y);
}

void main() {
    vec2 uv = v_texCoord;
    float scroll1 = u_time * 2.8;
    float scroll2 = u_time * 4.2;

    float n1 = noise(vec2(uv.x * 6.0, uv.y * 3.0 - scroll1));
    float n2 = noise(vec2(uv.x * 12.0 + 3.4, uv.y * 5.0 - scroll2));
    float flamePattern = mix(n1, n2, 0.5);

    // Fresnel / Rim Lighting (bordo luminescente stile anime)
    float rim = 1.0 - abs(dot(normalize(v_normal), normalize(v_viewDir)));
    rim = pow(rim, 1.5);

    // Gradiente verticale
    float vertFade = sin(clamp(uv.y, 0.0, 1.0) * 3.14159);

    float flameIntensity = smoothstep(0.15, 0.75, flamePattern + rim * 0.5) * vertFade;

    vec3 flameColor = mix(u_color.rgb, vec3(1.0, 1.0, 0.85), flameIntensity * 0.6);
    float finalAlpha = clamp(flameIntensity * u_color.a * (0.4 + rim * 0.6), 0.0, 1.0);

    gl_FragColor = vec4(flameColor, finalAlpha);
}
