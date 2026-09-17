#version 120

varying vec2 v_texCoord;

uniform float u_time;
uniform float u_intensity;
uniform vec3 u_color;
uniform vec2 u_resolution;

float hash(float n) {
    return fract(sin(n) * 43758.5453123);
}

void main() {
    vec2 uv = v_texCoord - 0.5;
    uv.x *= (u_resolution.x / u_resolution.y);

    float radius = length(uv);
    float angle = atan(uv.y, uv.x);

    // Divisione in settori angolari densi
    float sector = floor(angle * 55.0);
    float h1 = hash(sector + floor(u_time * 18.0) * 0.17);

    // Comparsa di linee cinetiche animate a raffica
    float lineActive = step(0.65, h1);

    // Dissolvenza verso il centro dello schermo per lasciare visibile il personaggio/bersaglio
    float radialFade = smoothstep(0.28, 0.72, radius);

    float alpha = lineActive * radialFade * u_intensity;

    gl_FragColor = vec4(u_color, alpha);
}
