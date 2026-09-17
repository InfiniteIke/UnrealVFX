#version 120

varying vec3 v_normal;
varying vec3 v_camDir;

uniform float u_time;
uniform vec4 u_color;

void main() {
    // Angolo tra la normale della superficie e la direzione dell'osservatore
    float cosTheta = clamp(abs(dot(v_normal, v_camDir)), 0.0, 1.0);

    // FRESNEL RIM: l'alone fosforescente bianco appare sempre sul bordo/silhouette della sfera.
    // Poiche la sfera e 3D, il bordo si sposta naturalmente in base a dove guardi:
    // se guardi dal lato, l'alone e un cerchio sull'asse XY.
    // Se guardi dall'alto, l'alone circonda il polo.
    float rim = pow(1.0 - cosTheta, 4.5);

    // Fosforescenza bianca intensa con leggero pulsare
    float pulse = 0.90 + sin(u_time * 2.5) * 0.10;
    vec3 rimColor = vec3(1.0, 0.98, 0.94) * rim * 1.65 * pulse;

    // Corpo nero puro al centro (opacita totale 100%)
    gl_FragColor = vec4(rimColor, 1.0);
}
