#version 150

#define M_PI 3.1415926535897932384626433832795

#moj_import <fog.glsl>

const int cosmiccount = 10;
const int cosmicoutof = 101;
const float lightmix = 0.2f;

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

uniform float time;

uniform float yaw;
uniform float pitch;
uniform float externalScale;

uniform float opacity;

uniform mat2 cosmicuvs[cosmiccount];

in float vertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;
in vec4 normal;
in vec3 fPos;

out vec4 fragColor;

mat4 rotationMatrix(vec3 axis, float angle)
{
    axis = normalize(axis);
    float s = sin(angle);
    float c = cos(angle);
    float oc = 1.0 - c;

    return mat4(oc * axis.x * axis.x + c,           oc * axis.x * axis.y - axis.z * s,  oc * axis.z * axis.x + axis.y * s,  0.0,
    oc * axis.x * axis.y + axis.z * s,  oc * axis.y * axis.y + c,           oc * axis.y * axis.z - axis.x * s,  0.0,
    oc * axis.z * axis.x - axis.y * s,  oc * axis.y * axis.z + axis.x * s,  oc * axis.z * axis.z + c,           0.0,
    0.0,                                0.0,                                0.0,                                1.0);
}

vec3 getNebulaColor(float seed, float time) {
    float colorPhase = mod(seed + time * 0.0005, 1.0);
    
    vec3 color1 = vec3(0.80, 0.20, 0.90);
    vec3 color2 = vec3(0.10, 0.60, 1.00);
    vec3 color3 = vec3(1.00, 0.40, 0.10);
    
    if (colorPhase < 0.33) {
        return mix(color1, color2, colorPhase / 0.33);
    } else if (colorPhase < 0.66) {
        return mix(color2, color3, (colorPhase - 0.33) / 0.33);
    } else {
        return mix(color3, color1, (colorPhase - 0.66) / 0.34);
    }
}

void main (void)
{
    vec4 mask = texture(Sampler0, texCoord0.xy);

    float oneOverExternalScale = 1.0/externalScale;

    int uvtiles = 16;

    float colorTime = time * 0.0008;
    vec4 col = vec4(
    0.15 + sin(colorTime) * 0.1,
    0.07 + sin(colorTime + M_PI * 0.33) * 0.08,
    0.16 + sin(colorTime + M_PI * 0.66) * 0.12,
    1.0
    );

    float pulse = mod(time, 400) / 400.0;

    col.r += sin(pulse * M_PI * 2.0) * 0.25 * 0.60 + 0.35 * 0.60;
    col.g += sin(pulse * M_PI * 1.5 + M_PI * 0.3) * 0.15 * 0.30 + 0.20 * 0.30;
    col.b += cos(pulse * M_PI * 2.2 + M_PI * 0.7) * 0.30 * 0.80 + 0.45 * 0.80;

    float depth = length(fPos) / 10.0;

    vec3 deepSpace = getNebulaColor(123.456, time) * 0.3;
    vec3 nearSpace = getNebulaColor(789.012, time) * 0.8;
    col.rgb = mix(deepSpace, nearSpace, clamp(1.0 - depth * 0.4, 0.0, 1.0));

    float halo = sin(pulse * M_PI * 3.0) * 0.2 + 0.8;
    vec3 colorfulHalo = vec3(0.90, 0.70, 1.00) * halo * 0.4;
    col.rgb += colorfulHalo;

    float stripePattern = sin(fPos.x * 0.1 + time * 0.003) * sin(fPos.y * 0.15 + time * 0.002);
    vec3 stripeColor = vec3(0.50, 0.80, 0.60) * 0.1 * (stripePattern + 1.0) * 0.5;
    col.rgb += stripeColor;

    vec4 dir = normalize(vec4(-fPos, 0));

    float sb = sin(pitch);
    float cb = cos(pitch);
    dir = normalize(vec4(dir.x, dir.y * cb - dir.z * sb, dir.y * sb + dir.z * cb, 0));

    float sa = sin(-yaw);
    float ca = cos(-yaw);
    dir = normalize(vec4(dir.z * sa + dir.x * ca, dir.y, dir.z * ca - dir.x * sa, 0));

    vec4 ray;

    for (int i = 0; i < 16; i++) {
        int mult = 16 - i;

        int j = i + 7;
        float rand1 = (j * j * 4321 + j * 8) * 2.0F;
        int k = j + 1;
        float rand2 = (k * k * k * 239 + k * 37) * 3.6F;
        float rand3 = rand1 * 347.4 + rand2 * 63.4;

        vec3 axis = normalize(vec3(sin(rand1), sin(rand2), cos(rand3)));

        ray = dir * rotationMatrix(axis, mod(rand3, 2 * M_PI));

        float rawu = 0.5 + (atan(ray.z, ray.x) / (2 * M_PI));
        float rawv = 0.5 + (asin(ray.y) / M_PI);

        float scale = mult * 0.5 + 2.75;
        float u = rawu * scale * externalScale;
        float v = (rawv + time * 0.0002 * oneOverExternalScale) * scale * 0.6 * externalScale;

        vec2 tex = vec2(u, v);

        int tu = int(mod(floor(u * uvtiles), uvtiles));
        int tv = int(mod(floor(v * uvtiles), uvtiles));

        int position = ((171 * tu) + (489 * tv) + (303 * (i + 31)) + 17209) ^ 10;
        int symbol = int(mod(position, cosmicoutof));
        int rotation = int(mod(pow(tu, float(tv)) + tu + 3 + tv * i, 8));
        bool flip = false;
        if (rotation >= 4) {
            rotation -= 4;
            flip = true;
        }

        if (symbol >= 0 && symbol < cosmiccount) {

            vec2 cosmictex = vec2(1.0, 1.0);
            vec4 tcol = vec4(1.0, 0.0, 0.0, 1.0);

            float ru = clamp(mod(u, 1.0) * uvtiles - tu, 0.0, 1.0);
            float rv = clamp(mod(v, 1.0) * uvtiles - tv, 0.0, 1.0);

            if (flip) {
                ru = 1.0 - ru;
            }

            float oru = ru;
            float orv = rv;

            if (rotation == 1) {
                oru = 1.0 - rv;
                orv = ru;
            } else if (rotation == 2) {
                oru = 1.0 - ru;
                orv = 1.0 - rv;
            } else if (rotation == 3) {
                oru = rv;
                orv = 1.0 - ru;
            }

            float umin = cosmicuvs[symbol][0][0];
            float umax = cosmicuvs[symbol][1][0];
            float vmin = cosmicuvs[symbol][0][1];
            float vmax = cosmicuvs[symbol][1][1];

            cosmictex.x = umin * (1.0 - oru) + umax * oru;
            cosmictex.y = vmin * (1.0 - orv) + vmax * orv;

            tcol = texture(Sampler0, cosmictex);

            float a = tcol.r * (0.5 + (1.0 / mult) * 1.0) * (1.0 - smoothstep(0.15, 0.48, abs(rawv - 0.5)));

            float starType = mod(rand1 * rand2, 100.0);
            vec3 starColor;

            if (starType < 14.3) {
                starColor = vec3(0.40, 0.70, 1.00);
            } else if (starType < 28.6) {
                starColor = vec3(1.00, 0.30, 0.10);
            } else if (starType < 42.9) {
                starColor = vec3(1.00, 0.60, 0.20);
            } else if (starType < 57.2) {
                starColor = vec3(1.00, 1.00, 0.40);
            } else if (starType < 71.5) {
                starColor = vec3(0.20, 1.00, 0.30);
            } else if (starType < 85.8) {
                starColor = vec3(0.80, 0.20, 1.00);
            } else {
                starColor = vec3(1.00, 0.40, 0.80);
            }

            starColor *= vec3(
                1.0 + mod(rand1, 20.0) / 500.0,
                1.0 + mod(rand2, 20.0) / 500.0,
                1.0 + mod(rand3, 20.0) / 500.0
            );

            float twinkle = sin(time * 0.006 + rand1 * 0.1) * sin(time * 0.009 + rand2 * 0.15) * 0.4 + 0.6;
            starColor *= twinkle;

            float distanceFade = 1.0 - float(i) / 20.0;

            float depthGlow = 1.0 + sin(depth * M_PI + time * 0.003) * 0.3;
            vec3 glowColor = vec3(0.90, 0.70, 1.00) * 0.2;
            starColor = starColor * distanceFade * depthGlow + glowColor;

            vec3 distanceGlow = vec3(0.90, 0.70, 1.00) * (1.0 - distanceFade) * 0.3;
            starColor += distanceGlow;

            col = col + vec4(starColor, 1.0) * a;
        }
    }

    float lightPhase = time * 0.0003;
    vec3 lightTint = vec3(0.9, 0.9, 0.9) * 0.1 + vec3(0.9, 0.9, 0.9);
    vec3 shade = vertexColor.rgb * (lightmix) + lightTint * (1.0 - lightmix);
    col.rgb *= shade;

    col.a *= mask.r * opacity;

    float finalTint = time * 0.002;
    col.rgb *= vec3(
    1.0 + sin(finalTint) * 0.05,
    1.0 + sin(finalTint + M_PI * 0.33) * 0.05,
    1.0 + sin(finalTint + M_PI * 0.66) * 0.05
    );

    col = clamp(col, 0.0, 1.0);

    fragColor = linear_fog(col * ColorModulator, vertexDistance, FogStart, FogEnd, FogColor);
}