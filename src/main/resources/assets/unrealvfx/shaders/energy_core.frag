#version 120

varying vec2 v_texCoord;
varying vec3 v_normal;
varying vec3 v_viewDir;

uniform float u_time;
uniform float u_progress;
uniform vec4 u_color;

void main() {
    vec3 normal = normalize(v_normal);
    vec3 viewDir = normalize(v_viewDir);

    // Fresnel glow verso l'esterno
    float ndv = clamp(dot(normal, viewDir), 0.0, 1.0);
    float rim = 1.0 - ndv;

    // Nucleo caldissimo (bianco incandescente) al centro, colore saturo sul bordo
    float core = pow(ndv, 2.5);
    vec3 baseColor = u_color.rgb;
    vec3 finalColor = mix(baseColor, vec3(1.0, 1.0, 1.0), core * 0.9);

    float alpha = clamp((rim * 1.5 + core) * u_color.a, 0.0, 1.0);

    gl_FragColor = vec4(finalColor, alpha);
}
