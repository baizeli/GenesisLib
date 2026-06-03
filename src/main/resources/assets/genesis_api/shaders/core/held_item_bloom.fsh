#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D NearBlurSampler;
uniform sampler2D FarBlurSampler;
uniform vec4 OutlineColor;
uniform vec4 SecondaryColor;
uniform float AlphaThreshold;
uniform float Opacity;
uniform float GlowStrength;
uniform float BloomStrength;
uniform float BloomRadius;
uniform float ColorMode;
uniform float Time;

in vec2 texCoord;
out vec4 fragColor;

vec3 hsv2rgb(vec3 c) {
    vec3 rgb = clamp(abs(mod(c.x * 6.0 + vec3(0.0, 4.0, 2.0), 6.0) - 3.0) - 1.0, 0.0, 1.0);
    return c.z * mix(vec3(1.0), rgb, c.y);
}

void main() {
    float maskSample = texture(DiffuseSampler, texCoord).a;

    // 终极杀手锏：武器本体处，绝对禁止画泛光！
    if (maskSample > AlphaThreshold) {
        discard;
    }

    float nearBlur = texture(NearBlurSampler, texCoord).a;
    float farBlur = texture(FarBlurSampler, texCoord).a;

    float nearHalo = clamp(nearBlur, 0.0, 1.0);
    float farHalo = clamp(farBlur, 0.0, 1.0);

    float mergedHalo = max(farHalo, nearHalo * 0.92);
    float radiusFactor = clamp(BloomRadius / 2.0, 0.35, 6.0);
    float spread = smoothstep(0.004, 0.14 + radiusFactor * 0.018, mergedHalo);
    float edgeProximity = clamp(nearHalo / max(farHalo, 0.0001), 0.0, 1.0);
    float edgeWeight = 0.30 + 0.70 * pow(edgeProximity, 0.85);

    float glowBoost = 0.55 + GlowStrength * 0.85;
    float intensity = spread * edgeWeight * glowBoost * BloomStrength;
    float finalAlpha = clamp(intensity * Opacity, 0.0, 1.0);

    if (finalAlpha < 0.01) {
        discard;
    }

    vec4 color = OutlineColor;
    if (ColorMode == 1.0) {
        float scroll = fract(texCoord.x * 2.0 + texCoord.y * 2.0 + Time * 2.0);
        color = mix(OutlineColor, SecondaryColor, scroll);
    } else if (ColorMode == 2.0) {
        float hue = fract(texCoord.x * 0.5 + texCoord.y * 0.5 + Time);
        color = vec4(hsv2rgb(vec3(hue, 1.0, 1.0)), 1.0);
    }

    fragColor = vec4(color.rgb, finalAlpha);
}