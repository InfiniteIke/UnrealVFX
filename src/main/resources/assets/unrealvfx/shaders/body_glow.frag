#version 120

varying vec2 v_texCoord;
varying vec3 v_normal;
varying vec3 v_viewDir;

uniform float u_time;
uniform vec4 u_color;

void main() {
    vec3 normal = normalize(v_normal);
    vec3 viewDir = normalize(v_viewDir);

    float ndv = clamp(dot(normal, viewDir), 0.0, 1.0);
    // Bordo incandescente invertito (Inverted Fresnel per contorno anime)
    float rim = 1.0 - ndv;
    rim = pow(rim, 2.2);

    // Micro-pulsazione elettrica superficiale ad alta frequenza (anime shimmer)
    float flicker = sin(u_time * 18.0) * 0.08 + 0.92;

    // Cel-shading netto della sagoma corporea
    float rimBand = smoothstep(0.35, 0.40, rim);

    // Palette: interno dorato/luminoso, bordo esterno bianco caldo accecante
    vec3 innerColor = u_color.rgb;
    vec3 edgeColor = vec3(1.0, 1.0, 0.9);

    vec3 finalColor = mix(innerColor, edgeColor, rimBand) * flicker * 1.5;
    float alpha = clamp((rim * 1.6 + 0.35) * u_color.a, 0.0, 1.0);

    gl_FragColor = vec4(finalColor, alpha);
}
