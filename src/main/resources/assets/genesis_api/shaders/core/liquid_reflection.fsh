#version 150

uniform sampler2D DiffuseSampler;
uniform vec2 ScreenSize;
uniform float Time;
uniform float ReflectionStrength;
uniform float FlowStrength;
uniform float FlowSpeed;
uniform vec4 TintColor;

in vec4 vertexColor;
in vec2 texCoord;
in vec2 screenCoord;
in vec3 viewPos;

out vec4 fragColor;

float hash(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453);
}

float noise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    vec2 u = f * f * (3.0 - 2.0 * f);
    return mix(mix(hash(i), hash(i + vec2(1.0, 0.0)), u.x),
               mix(hash(i + vec2(0.0, 1.0)), hash(i + vec2(1.0, 1.0)), u.x), u.y);
}

float fbm(vec2 p) {
    float v = 0.0;
    float a = 0.5;
    for (int i = 0; i < 4; i++) {
        v += noise(p) * a;
        p = p * 2.03 + vec2(17.7, 9.2);
        a *= 0.5;
    }
    return v;
}

void main() {
    vec2 flow = texCoord * vec2(10.0, 18.0);
    float t = Time * FlowSpeed;
    float waveA = fbm(flow + vec2(t * 0.85, -t * 0.42));
    float waveB = fbm(flow.yx * 1.35 + vec2(-t * 0.28, t * 0.72));
    vec2 ripple = vec2(waveA - 0.5, waveB - 0.5);

    float distanceFade = clamp(1.0 - length(viewPos) / 56.0, 0.0, 1.0);
    float grazing = pow(clamp(1.0 - abs(normalize(viewPos).y), 0.0, 1.0), 1.6);
    float flowMask = smoothstep(0.16, 0.94, waveA * 0.7 + waveB * 0.3);
    float aspect = ScreenSize.x / max(ScreenSize.y, 1.0);

    vec2 reflectUv = vec2(screenCoord.x, 1.0 - screenCoord.y);
    reflectUv += ripple * (0.018 + 0.018 * grazing) * FlowStrength;
    reflectUv.x = screenCoord.x + (reflectUv.x - screenCoord.x) / max(aspect, 0.25);
    reflectUv += vec2(sin((texCoord.y + t) * 22.0), cos((texCoord.x - t) * 18.0)) * 0.0025 * FlowStrength;
    reflectUv = clamp(reflectUv, vec2(0.001), vec2(0.999));

    vec3 reflected = texture(DiffuseSampler, reflectUv).rgb;
    vec3 refracted = texture(DiffuseSampler, clamp(screenCoord + ripple * 0.010 * FlowStrength, vec2(0.001), vec2(0.999))).rgb;
    vec3 tint = TintColor.rgb * vertexColor.rgb;

    vec3 color = mix(refracted * tint, reflected, ReflectionStrength * (0.38 + grazing * 0.62));
    color += tint * flowMask * 0.16;
    color += vec3(0.75, 0.95, 1.0) * pow(flowMask, 5.0) * 0.18;

    float alpha = TintColor.a * vertexColor.a;
    alpha *= mix(0.38, 0.9, grazing) * distanceFade;
    alpha += flowMask * 0.08;
    alpha = clamp(alpha, 0.0, 0.78);

    fragColor = vec4(color, alpha);
}
