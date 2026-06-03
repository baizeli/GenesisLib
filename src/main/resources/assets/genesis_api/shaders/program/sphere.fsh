#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D DepthSampler;
uniform vec2 InSize;
uniform vec2 OutSize;
uniform float Time;
uniform vec3 CameraPos;
uniform mat4 ProjMat;
uniform mat4 IViewRotMat; // 观察空间 -> 世界空间 旋转矩阵

in vec2 texCoord;
out vec4 fragColor;

mat2 rot(float a) {
    float s = sin(a), c = cos(a);
    return mat2(c, -s, s, c);
}

float getLinearDepth(vec2 uv) {
    float depth = texture(DepthSampler, uv).r;
    float z = depth * 2.0 - 1.0;
    return (2.0 * ProjMat[3][2]) / (ProjMat[2][2] + z);
}

float sdSphere(vec3 p, float r) {
    return length(p) - r;
}

void main() {
    float dummy = (InSize.x + InSize.y) * 0.000001;

    // 1. 视口归一化
    vec2 uv = (texCoord - 0.5);
    uv.x *= OutSize.x / OutSize.y;

    // 2. 深度 (先不管)
    float sceneDepth = getLinearDepth(texCoord);



    // 3. 构建射线 (World Space)

    // 射线起点 (ro) 直接就是世界坐标下的相机位置
    vec3 ro = CameraPos;

    // 射线方向 (rd)
    // 先计算屏幕空间的初始方向 (朝向 Z 负方向)
    vec3 rdView = normalize(vec3(uv, -1.2)); // -1.2 控制 FOV
    // 然后用旋转矩阵将其转到世界空间
    vec3 rd = mat3(IViewRotMat) * rdView;

    // 4. 球体位置 (World Space)
    vec3 sphereCenter = vec3(0.0, 0.0, 0.0);



    vec4 backCol = texture(DiffuseSampler, texCoord);
    vec3 col = backCol.rgb;

    // 5. 光线步进 (World Space)
    float t = 0.0;
    bool hit = false;
    float maxDist = 200.0;

    for(int i = 0; i < 80; i++) {
        vec3 p = ro + rd * t; // p 直接就是世界坐标

        // 计算相对于球心的坐标用于 SDF 和纹理
        vec3 p_rel = p - sphereCenter;

        // 旋转动画
        p_rel.xz *= rot(Time * 0.5);
        p_rel.yz *= rot(Time * 0.3);

        float d = sdSphere(p_rel, 1.5);

        // 简易深度遮挡：
        // sceneDepth 是 View Space 的 Z 值（负数表示前方）
        // 而现在的 t 是 World Space 的距离。
        // 在不穿墙的情况下，通常 t < abs(sceneDepth)
        // 但为了简单，先不加深度判断，确保位置对了再说
        // if (t > -sceneDepth) break;

        if(d < 0.001) {
            hit = true;
            break;
        }

        t += d;
        if(t > maxDist) break;
    }

    // 6. 渲染
    if(hit) {
        vec3 p = ro + rd * t;
        vec3 p_rel = p - sphereCenter;
        p_rel.xz *= rot(Time * 0.5);
        p_rel.yz *= rot(Time * 0.3);

        float pattern = sin(p_rel.x * 10.0) * sin(p_rel.y * 10.0) * sin(p_rel.z * 10.0);
        pattern = smoothstep(-0.2, 0.2, pattern);

        vec3 baseCol = 0.5 + 0.5 * cos(Time + p_rel.xyx + vec3(0, 2, 4));
        vec3 sphereCol = mix(baseCol * 0.2, baseCol, pattern);

        // 法线光照 (使用旋转后的 rd 会更自然)
        vec3 n = normalize(p - sphereCenter);
        // 光源方向也定义在世界空间
        vec3 lightDir = normalize(vec3(0.5, 1.0, -0.5));
        float diff = max(dot(n, lightDir), 0.0);
        sphereCol *= (diff + 0.3);

        col = sphereCol;
    }

    fragColor = vec4(col + dummy, 1.0);
}