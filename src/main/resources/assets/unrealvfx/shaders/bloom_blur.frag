#version 120

varying vec2 v_texCoord;

uniform sampler2D u_texture;
uniform vec2 u_blurDir;

void main() {
    vec4 sum = vec4(0.0);

    // Convoluzione Gaussiana separabile a 9 punti per glow ad alta fedelta
    sum += texture2D(u_texture, v_texCoord - u_blurDir * 4.0) * 0.051;
    sum += texture2D(u_texture, v_texCoord - u_blurDir * 3.0) * 0.0918;
    sum += texture2D(u_texture, v_texCoord - u_blurDir * 2.0) * 0.12245;
    sum += texture2D(u_texture, v_texCoord - u_blurDir * 1.0) * 0.1531;
    sum += texture2D(u_texture, v_texCoord) * 0.1633;
    sum += texture2D(u_texture, v_texCoord + u_blurDir * 1.0) * 0.1531;
    sum += texture2D(u_texture, v_texCoord + u_blurDir * 2.0) * 0.12245;
    sum += texture2D(u_texture, v_texCoord + u_blurDir * 3.0) * 0.0918;
    sum += texture2D(u_texture, v_texCoord + u_blurDir * 4.0) * 0.051;

    gl_FragColor = sum;
}
