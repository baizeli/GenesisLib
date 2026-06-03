#version 150

uniform sampler2D Sample0;
uniform vec4 ColorModulator;
uniform float iTime;
uniform float intensity;
uniform float waveSpeed;
uniform float waveFrequency;

in vec2 fragUV0;
out vec4 fragColor;

float random(vec2 st) {
    return fract(sin(dot(st.xy, vec2(12.9898, 78.233))) * 43758.5453123);
}

float noise(vec2 st) {
    vec2 i = floor(st);
    vec2 f = fract(st);

    float a = random(i);
    float b = random(i + vec2(1.0, 0.0));
    float c = random(i + vec2(0.0, 1.0));
    float d = random(i + vec2(1.0, 1.0));

    vec2 u = f * f * (3.0 - 2.0 * f);
    return mix(a, b, u.x) + (c - a) * u.y * (1.0 - u.x) + (d - b) * u.x * u.y;
}

void main() {
    // 使用屏幕UV坐标
    vec2 uv = fragUV0;

    // 应用热浪扭曲效果
    float distortion = sin(uv.y * waveFrequency + iTime * waveSpeed) * intensity;
    distortion += sin(uv.y * waveFrequency * 1.5 + iTime * waveSpeed * 1.3) * intensity * 0.5;

    vec2 noiseUV = uv * vec2(3.0, 6.0) + iTime * 0.5;
    distortion += noise(noiseUV) * intensity * 0.8;

    // 应用扭曲到UV坐标
    vec2 distortedUV = uv;
    distortedUV.x += distortion;

    // 从屏幕纹理采样
    vec4 color = texture(Sample0, distortedUV);

    // 应用颜色调制（可选）
    fragColor = color * ColorModulator;
}