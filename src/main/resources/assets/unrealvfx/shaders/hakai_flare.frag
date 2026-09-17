#version 120

varying vec2 v_texCoord;

uniform float u_time;
uniform float u_progress;
uniform vec4 u_color;
uniform sampler2D u_texture;

void main() {
    vec4 tex = texture2D(u_texture, v_texCoord);

    if (tex.a < 0.05) {
        discard;
    }

    // Pulsazione energetica della corona anime
    float pulse = sin(u_time * 9.0) * 0.15 + 0.95;

    // Colore ultra-saturo con emissione luminosa
    vec3 color = tex.rgb * u_color.rgb * pulse * 1.4;
    float alpha = clamp(tex.a * u_color.a, 0.0, 1.0);

    gl_FragColor = vec4(color, alpha);
}
