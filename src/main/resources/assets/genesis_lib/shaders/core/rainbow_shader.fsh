#version 150

uniform vec4 ColorModulator;
uniform float iTime;
uniform vec2 iResolution;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    // 使用原始texCoord
    vec2 uv = texCoord;

    // 彩虹颜色分段 - 改为基于y坐标（纵向/宽度方向）
    float segment = uv.y * 7.0; // 改为uv.y
    int colorIndex = int(floor(segment));
    float posInSegment = fract(segment);

    // 彩虹颜色数组
    vec3 rainbowColors[7];
    rainbowColors[0] = vec3(1.0, 0.0, 0.0);    // 红
    rainbowColors[1] = vec3(1.0, 0.5, 0.0);   // 橙
    rainbowColors[2] = vec3(1.0, 1.0, 0.0);   // 黄
    rainbowColors[3] = vec3(0.0, 1.0, 0.0);   // 绿
    rainbowColors[4] = vec3(0.0, 0.0, 1.0);   // 蓝
    rainbowColors[5] = vec3(0.29, 0.0, 0.51); // 靛
    rainbowColors[6] = vec3(0.5, 0.0, 1.0);   // 紫

    // 动态颜色（随时间变化）
    vec3 col = rainbowColors[int(mod(colorIndex + int(iTime * 2.0), 7))];

    // 基础透明度
    float alpha = 1.0;

    // 1. 拖尾尾部逐渐透明（基于UV的x坐标）
    float tailFade = 1.0 - uv.x; // x从0到1，尾部透明度从1到0
    alpha *= tailFade;

    // 2. 宽度方向边缘虚化（基于UV的y坐标）
    float edgeFactor = uv.y;
    if (edgeFactor < 0.2) {
        // 底部边缘虚化
        alpha *= edgeFactor / 0.2;
    } else if (edgeFactor > 0.8) {
        // 顶部边缘虚化
        alpha *= (1.0 - edgeFactor) / 0.2;
    }

    // 3. 增加平滑过渡
    alpha = smoothstep(0.0, 1.0, alpha); // 平滑过渡

    fragColor = vec4(col, alpha) * ColorModulator;
}