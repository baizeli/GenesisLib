#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D DepthSampler;
uniform vec2 ScreenSize;
uniform vec4 OutlineColor;
uniform vec4 SecondaryColor;
uniform float OutlineWidth;
uniform float Softness;
uniform float AlphaThreshold;
uniform float Opacity;
uniform float DepthWeight;
uniform float GlowStrength;
uniform float ColorMode;
uniform float Time;

in vec2 texCoord;
out vec4 fragColor;

const int OUTLINE_SAMPLE_COUNT = 16;
const vec2 OUTLINE_DIRECTIONS[OUTLINE_SAMPLE_COUNT] = vec2[](
vec2(1.0, 0.0), vec2(0.9238795, 0.3826834), vec2(0.7071068, 0.7071068), vec2(0.3826834, 0.9238795),
vec2(0.0, 1.0), vec2(-0.3826834, 0.9238795), vec2(-0.7071068, 0.7071068), vec2(-0.9238795, 0.3826834),
vec2(-1.0, 0.0), vec2(-0.9238795, -0.3826834), vec2(-0.7071068, -0.7071068), vec2(-0.3826834, -0.9238795),
vec2(0.0, -1.0), vec2(0.3826834, -0.9238795), vec2(0.7071068, -0.7071068), vec2(0.9238795, -0.3826834)
);

float depthAt(vec2 uv) {
    return texture(DepthSampler, uv).r;
}

float maskAlpha(vec2 uv) {
    return texture(DiffuseSampler, uv).a;
}

float maskWeight(float alpha) {
    return smoothstep(max(0.0, AlphaThreshold - 0.05), AlphaThreshold + 0.05, alpha);
}

vec3 hsv2rgb(vec3 c) {
    vec3 rgb = clamp(abs(mod(c.x * 6.0 + vec3(0.0, 4.0, 2.0), 6.0) - 3.0) - 1.0, 0.0, 1.0);
    return c.z * mix(vec3(1.0), rgb, c.y);
}

void main() {
    float centerAlpha = maskAlpha(texCoord);

    // 终极杀手锏：如果当前像素属于武器本体，直接丢弃，绝不覆盖一丝一毫！
    if (centerAlpha > AlphaThreshold) {
        discard;
    }

    float outerCore = 0.0;
    float outerFeather = 0.0;
    float bridgeCore = 0.0;
    float coreSum = 0.0;
    float featherSum = 0.0;
    float nearestDepth = depthAt(texCoord);

    vec2 texelSize = 1.0 / max(ScreenSize, vec2(1.0));
    float coreRadius = max(0.25, OutlineWidth);
    float featherRadius = max(coreRadius + 0.25, OutlineWidth * (1.0 + Softness * 0.45));
    float bridgeRadius = max(0.25, coreRadius * 0.5);

    for(int i = 0; i < OUTLINE_SAMPLE_COUNT; i++) {
        vec2 direction = OUTLINE_DIRECTIONS[i] * texelSize;
        vec2 coreUv = texCoord + direction * coreRadius;
        vec2 featherUv = texCoord + direction * featherRadius;
        vec2 bridgeUv = texCoord + direction * bridgeRadius;

        float coreSample = maskWeight(maskAlpha(coreUv));
        float featherSample = maskWeight(maskAlpha(featherUv));
        float bridgeSample = maskWeight(maskAlpha(bridgeUv));

        outerCore = max(outerCore, coreSample);
        outerFeather = max(outerFeather, featherSample);
        bridgeCore = max(bridgeCore, bridgeSample);
        coreSum += coreSample;
        featherSum += featherSample;
        nearestDepth = min(nearestDepth, min(depthAt(coreUv), depthAt(bridgeUv)));
    }

    float coreAverage = coreSum / float(OUTLINE_SAMPLE_COUNT);
    float featherAverage = featherSum / float(OUTLINE_SAMPLE_COUNT);
    float cornerFill = max(outerCore, mix(bridgeCore, coreAverage, 0.35));

    float softnessNorm = clamp((Softness - 0.5) / 1.5, 0.0, 1.0);
    float shell = clamp(max(cornerFill, max(outerFeather * 0.92, featherAverage * 0.70)), 0.0, 1.0);
    shell = pow(shell, mix(1.35, 0.72, softnessNorm));

    float depthContrast = clamp((depthAt(texCoord) - nearestDepth) * (1.2 + DepthWeight * 5.0), 0.0, 1.0);
    float depthBoost = mix(1.0, 1.0 + depthContrast * 1.8, clamp(DepthWeight / 4.0, 0.0, 1.0));
    shell *= depthBoost;

    float glow = shell * shell * (0.6 + GlowStrength * 0.8);
    float finalAlpha = clamp(shell * Opacity, 0.0, 1.0);

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

    fragColor = vec4(color.rgb * (1.0 + glow), finalAlpha);
}
