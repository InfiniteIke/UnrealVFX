#version 120

varying vec2 v_texCoord;

uniform sampler2D u_depthTexture;
uniform vec2 u_screenResolution;
uniform float u_near;
uniform float u_far;
uniform float u_fadeDistance;

uniform float u_time;
uniform float u_progress;
uniform vec4 u_color;

float linearizeDepth(float z, float n, float f) {
    return (n * f) / (f - z * (f - n));
}

float hash(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453123);
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

    // Profilo di spessore dell'anello: massimo al centro (v=0.5), nullo ai bordi
    float widthProfile = sin(clamp(uv.y, 0.0, 1.0) * 3.14159265);
    widthProfile = pow(widthProfile, 0.85);

    // Striature di plasma relativistico ad altissima velocita lungo la circonferenza
    float flowTime = u_time * 16.0;
    float streak1 = sin(uv.x * 24.0 * 3.14159265 + flowTime);
    float streak2 = cos(uv.x * 48.0 * 3.14159265 - flowTime * 1.5);
    float streak3 = noise(vec2(uv.x * 32.0 + flowTime * 0.8, uv.y * 8.0));

    float plasma = (streak1 * 0.45 + streak2 * 0.35 + streak3 * 0.20 + 1.0) * 0.5;

    // Taglio netto cel-shading per dentellature anime
    float intensity = widthProfile * (0.65 + plasma * 0.45);
    if (intensity < 0.22) {
        discard;
    }

    // Palette cromatica ad alto contrasto (Hakai / Cosmic Plasma)
    vec3 deepPurple = u_color.rgb * vec3(0.50, 0.08, 0.75);
    vec3 brightNeon = u_color.rgb;
    vec3 hotWhite   = vec3(1.0, 0.95, 1.0);

    vec3 color;
    if (intensity > 0.78) {
        color = hotWhite;
    } else if (intensity > 0.45) {
        color = brightNeon;
    } else {
        color = deepPurple;
    }

    float alpha = clamp((intensity - 0.22) / 0.78 * u_color.a, 0.0, 1.0);

    // Soft Depth Fading: elimina intersezioni dure con blocchi
    if (u_fadeDistance > 0.001 && u_screenResolution.x > 0.0) {
        vec2 screenUV = gl_FragCoord.xy / u_screenResolution;
        float rawSceneZ = texture2D(u_depthTexture, screenUV).r;
        float sceneZ = linearizeDepth(rawSceneZ, u_near, u_far);
        float fragZ  = linearizeDepth(gl_FragCoord.z, u_near, u_far);

        float deltaZ = sceneZ - fragZ;
        float depthFade = clamp(deltaZ / u_fadeDistance, 0.0, 1.0);
        depthFade = smoothstep(0.0, 1.0, depthFade);
        alpha *= depthFade;
    }

    if (alpha < 0.015) {
        discard;
    }

    gl_FragColor = vec4(color, alpha);
}
