#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D DistortSampler;

uniform vec2 InSize;
uniform vec2 OutSize;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

void main() {
    vec2 texel = 1.0 / InSize;

    float mask = texture(DistortSampler, texCoord).r;

    // ===== 螺旋扭曲 =====

    // 以屏幕中心为螺旋中心（稳定、好调）
    vec2 center = vec2(0.5, 0.5);

    vec2 toCenter = texCoord - center;
    float dist = length(toCenter);

    // 扭曲强度（mask 决定是否生效）
    float strength = mask * 5.0;

    // 距离衰减（越远越弱）
    float falloff = smoothstep(0.5, 0.0, dist);

    // 最终旋转角度
    float angle = strength * falloff;

    float s = sin(angle);
    float c = cos(angle);

    mat2 rot = mat2(
    c, -s,
    s,  c
    );

    vec2 spiralUV = center + rot * toCenter;

    vec4 baseColor = texture(DiffuseSampler, spiralUV);

    // ===== 边缘检测（mask 边框）=====
    float mL = texture(DistortSampler, texCoord + vec2(-texel.x, 0)).r;
    // 采样右边相邻像素的mask值
    float mR = texture(DistortSampler, texCoord + vec2( texel.x, 0)).r;
    // 采样上方相邻像素的mask值
    float mU = texture(DistortSampler, texCoord + vec2(0,  texel.y)).r;
    // 采样下方相邻像素的mask值
    float mD = texture(DistortSampler, texCoord + vec2(0, -texel.y)).r;

    // 计算当前像素与四个相邻像素的mask值差异之和，得到边缘强度
    float edge =
    abs(mask - mL)      // 左侧差异
    + abs(mask - mR)    // 右侧差异
    + abs(mask - mU)    // 上方差异
    + abs(mask - mD);   // 下方差异

    // 使用step函数创建边界效果：
    // step(0.2, edge) - 如果边缘强度大于等于0.2则返回1，否则返回0
    // step(0.01, mask) - 如果mask值大于等于0.01则返回1，否则返回0
    // 两个step结果相乘，确保只有在mask存在且边缘明显的位置才显示边框
    float border = step(0.2, edge) * step(0.01, mask);

    // 定义边框颜色 (粉色/洋红色)
    vec3 borderColor = vec3(1.0, 0.3, 0.8);

    // 根据border值混合基础颜色和边框颜色：
    // 如果border为1，则使用边框颜色；如果border为0，则使用原始颜色

    // vec3 finalColor = mix(baseColor.rgb, borderColor, border); // 注释掉边框渲染，不显示边框效果
    vec3 finalColor = baseColor.rgb; // 直接使用基础颜色，不添加边框



    // 设置最终输出颜色，alpha值固定为1.0
    fragColor = vec4(finalColor, 1.0);




}
