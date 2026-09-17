#version 120

varying vec2 v_texCoord;
varying vec3 v_normal;
varying vec3 v_viewDir;

uniform sampler2D u_depthTexture;
uniform vec2 u_screenResolution;
uniform float u_near;
uniform float u_far;
uniform float u_fadeDistance;

uniform float u_time;
uniform float u_progress;
uniform vec4 u_color;

// Griglia e distanza esagonale perfetta a nido d'ape senza cicli for (O(1))
vec4 hexGrid(vec2 uv) {
    vec2 s = vec2(1.0, 1.7320508);
    vec4 hC = floor(vec4(uv, uv - vec2(0.5, 1.0)) / s.xyxy) + 0.5;
    vec4 h = vec4(uv - hC.xy * s, uv - (hC.zw + 0.5) * s);
    return dot(h.xy, h.xy) < dot(h.zw, h.zw) ? vec4(h.xy, hC.xy) : vec4(h.zw, hC.zw + 9.73);
}

float hexEdgeDistance(vec2 p) {
    p = abs(p);
    float d = dot(p, normalize(vec2(1.0, 1.7320508)));
    return max(d, p.x);
}

float linearizeDepth(float z, float n, float f) {
    return (n * f) / (f - z * (f - n));
}

void main() {
    vec3 normal = normalize(v_normal);
    vec3 viewDir = normalize(v_viewDir);

    float ndv = clamp(abs(dot(normal, viewDir)), 0.0, 1.0);
    // Bordo Fresnel nitido (bordo della cupola intensamente luminescente)
    float rim = pow(1.0 - ndv, 2.5);

    // Tassellatura esagonale nitida a nido d'ape anime
    vec2 uv = v_texCoord * vec2(24.0, 16.0);
    vec4 h = hexGrid(uv);
    float edgeDist = hexEdgeDistance(h.xy);

    // Bordo della cella esagonale affilato
    float cellWire = smoothstep(0.40, 0.48, edgeDist);

    // Onda di scansione che pulsa verticalmente lungo la cupola
    float scanline = sin(v_texCoord.y * 25.0 - u_time * 6.0) * 0.5 + 0.5;

    // Trasparenza modulata: visibile dall'interno/centro, opaco sui bordi
    vec3 coreColor = u_color.rgb * 0.7;
    vec3 edgeColor = u_color.rgb * vec3(1.3, 1.2, 1.5);
    vec3 wireColor = vec3(1.0, 1.0, 1.0);

    vec3 finalColor = mix(coreColor, edgeColor, rim);
    finalColor = mix(finalColor, wireColor, cellWire * 0.85 + scanline * 0.25 * rim);

    float alpha = clamp((rim * 0.85 + cellWire * 0.45 + scanline * 0.15) * u_color.a, 0.0, 1.0);

    // Soft Depth Fading: la barriera sfuma nel terreno senza spigoli duri
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

    gl_FragColor = vec4(finalColor, alpha);
}
