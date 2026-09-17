#version 120
#extension GL_OES_standard_derivatives : enable

uniform sampler2D u_depthTexture;
uniform vec2 u_screenResolution;
uniform vec3 u_lightPos; // Posizione della sorgente luminosa in view space
uniform vec3 u_lightColor;
uniform float u_radius;
uniform float u_intensity;
uniform float u_falloffPower;
uniform float u_specular;
uniform float u_near;
uniform float u_far;
uniform float u_tanHalfFov;
uniform float u_aspectRatio;
uniform int u_isSpot;
uniform vec3 u_spotDir;
uniform float u_spotAngle;
uniform float u_penumbra;

float linearizeDepth(float z, float n, float f) {
    return (n * f) / (f - z * (f - n));
}

void main() {
    vec2 screenUV = gl_FragCoord.xy / u_screenResolution;
    float rawZ = texture2D(u_depthTexture, screenUV).r;

    // Se e cielo (depth = 1.0) o non inizializzato, non illuminare
    if (rawZ >= 0.9999 || rawZ < 0.0005) {
        discard;
    }

    float linearZ = linearizeDepth(rawZ, u_near, u_far);
    vec2 ndc = screenUV * 2.0 - 1.0;
    vec3 fragViewPos = vec3(ndc.x * u_tanHalfFov * u_aspectRatio * linearZ, ndc.y * u_tanHalfFov * linearZ, -linearZ);

    vec3 toLight = u_lightPos - fragViewPos;
    float dist = length(toLight);
    if (dist >= u_radius || dist <= 0.001) {
        discard;
    }

    vec3 L = toLight / dist;
    float atten = pow(clamp(1.0 - (dist / u_radius), 0.0, 1.0), max(u_falloffPower, 0.1));

    if (u_isSpot > 0) {
        float spotCos = dot(-L, normalize(u_spotDir));
        float minCos = cos(u_spotAngle * 0.5);
        float penCos = cos(u_spotAngle * 0.5 * (1.0 - clamp(u_penumbra, 0.01, 0.99)));
        float spotFactor = smoothstep(minCos, penCos, spotCos);
        atten *= spotFactor;
    }

    if (atten <= 0.001) {
        discard;
    }

    // Ricostruzione normale della superficie del blocco dai gradienti di profondita a schermo
    vec3 dX = dFdx(fragViewPos);
    vec3 dY = dFdy(fragViewPos);
    vec3 crossN = cross(dX, dY);
    vec3 N = (length(crossN) < 0.0001) ? vec3(0.0, 0.0, 1.0) : normalize(crossN);
    if (dot(N, -fragViewPos) < 0.0) {
        N = -N;
    }

    float NdotL = max(dot(N, L), 0.0);
    float diffuse = NdotL * 0.82 + 0.18;

    vec3 V = normalize(-fragViewPos);
    vec3 H = normalize(L + V);
    float NdotH = max(dot(N, H), 0.0);
    float spec = pow(NdotH, 16.0) * u_specular;

    vec3 colorContrib = u_lightColor * (diffuse + spec) * atten * u_intensity;
    colorContrib = clamp(colorContrib, 0.0, 0.75);
    if (length(colorContrib) <= 0.005) {
        discard;
    }
    gl_FragColor = vec4(colorContrib, 1.0);
}
