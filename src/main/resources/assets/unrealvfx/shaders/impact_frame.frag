#version 120

varying vec2 v_texCoord;

uniform sampler2D u_texture;
uniform int u_mode; // 0 = MANGA_BW, 1 = NEGATIVE_INVERT, 2 = HAKAI_PURPLE
uniform vec2 u_resolution;

void main() {
    vec4 color = texture2D(u_texture, v_texCoord);
    float luma = dot(color.rgb, vec3(0.299, 0.587, 0.114));

    if (u_mode == 0) {
        // MANGA B&W: Contrasto estremo a inchiostro e bianco puro
        float ink = step(0.48, luma);
        gl_FragColor = vec4(vec3(ink), 1.0);
    } else if (u_mode == 1) {
        // NEGATIVE INVERT: Inversione dei canali con boost di saturazione
        vec3 inv = 1.0 - color.rgb;
        gl_FragColor = vec4(inv * 1.25, 1.0);
    } else {
        // HAKAI PURPLE: Vuoto nero abissale con luci al neon magenta/viola
        vec3 darkVoid = vec3(0.04, 0.0, 0.08);
        vec3 hakaiNeon = vec3(1.0, 0.08, 0.85);
        float t = smoothstep(0.35, 0.85, luma);
        vec3 finalColor = mix(darkVoid, hakaiNeon, t);
        if (luma > 0.90) {
            finalColor = vec3(1.0, 0.9, 1.0); // Nucleo bianco-rosato
        }
        gl_FragColor = vec4(finalColor, 1.0);
    }
}
