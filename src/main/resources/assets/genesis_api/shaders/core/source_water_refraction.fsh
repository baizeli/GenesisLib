#version 150

#moj_import <fog.glsl>

uniform sampler2D Sampler0;
uniform sampler2D SceneSampler;

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;
uniform vec2 ScreenSize;
uniform float Time;
uniform vec4 TintColor;
uniform float Alpha;
uniform float RefractionStrength;
uniform float EdgeWobbleStrength;
uniform float WaveStrength;
uniform float WaveScale;
uniform float FlowSpeed;
uniform float DistanceFadeStart;
uniform float DistanceFadeEnd;
uniform float EdgeFadeStrength;
uniform float Quality;

in float vertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;
in vec2 screenCoord;
in vec3 viewPos;
in vec3 waterPos;
in vec3 surfaceNormal;

out vec4 fragColor;

float wave(vec2 p, float t) {
    float a = sin(p.x * 1.73 + p.y * 0.81 + t * 1.13);
    float b = sin(p.x * -0.62 + p.y * 1.41 + t * 0.87);
    float c = sin((p.x + p.y) * 0.74 + t * 1.71);
    return (a + b * 0.62 + c * 0.38) / 2.0;
}

vec2 rippleField(vec3 pos, vec3 normal, float t) {
    vec2 horizontal = pos.xz * WaveScale;
    vec2 verticalX = pos.xy * WaveScale;
    vec2 verticalZ = pos.zy * WaveScale;
    float sideX = abs(normal.x);
    float sideZ = abs(normal.z);
    float top = abs(normal.y);
    vec2 p = horizontal * top + verticalX * sideX + verticalZ * sideZ;

    float w1 = wave(p, t);
    float w2 = wave(p * 2.17 + vec2(11.7, -3.4), t * 1.37);
    float wx = wave(p + vec2(0.37, 0.0), t) - wave(p - vec2(0.37, 0.0), t);
    float wy = wave(p + vec2(0.0, 0.37), t) - wave(p - vec2(0.0, 0.37), t);

    vec2 r = normalize(vec2(wx, wy) + vec2(w1, w2) * 0.25);
    if (length(r) < 0.001) {
        r = vec2(0.0);
    }
    return r;
}

float distanceFade(float d) {
    float range = max(1.0, DistanceFadeEnd - DistanceFadeStart);
    return 1.0 - clamp((d - DistanceFadeStart) / range, 0.0, 1.0);
}

void main() {
    vec4 baseTex = texture(Sampler0, texCoord0);
    vec4 base = baseTex * vertexColor * ColorModulator;
    if (base.a <= 0.003) {
        discard;
    }

    float t = Time * FlowSpeed;
    float dist = length(viewPos);
    float fade = distanceFade(dist);
    vec3 n = normalize(surfaceNormal);
    vec3 viewDir = normalize(-viewPos);
    float grazing = pow(1.0 - clamp(abs(dot(n, viewDir)), 0.0, 1.0), 1.7);

    vec2 ripple = rippleField(waterPos, n, t) * WaveStrength * fade;
    float edgeBoost = 1.0 + grazing * EdgeFadeStrength;
    vec2 wobble = ripple * edgeBoost;

    vec3 clearTint = TintColor.rgb * base.rgb;
    float configuredAlpha = clamp(Alpha * base.a, 0.05, 0.95);
    float waterAlpha = clamp(configuredAlpha + grazing * EdgeWobbleStrength * 2.0, 0.05, 0.95);

    vec3 color;
    if (Quality >= 3.0 && RefractionStrength > 0.0) {
        vec2 refractUv = clamp(screenCoord + wobble * RefractionStrength, vec2(0.001), vec2(0.999));
        vec3 refracted = texture(SceneSampler, refractUv).rgb;
        color = mix(refracted, clearTint, waterAlpha * 0.58);
    } else {
        float flow = wave(waterPos.xz * WaveScale + vec2(t * 0.11, -t * 0.07), t);
        float shimmer = 0.88 + flow * 0.045 * WaveStrength * fade;
        color = clearTint * shimmer;
    }

    color = min(color, vec3(0.92));
    vec4 outColor = vec4(color, waterAlpha);
    fragColor = linear_fog(outColor, vertexDistance, FogStart, FogEnd, FogColor);
}
