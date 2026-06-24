#version 150

uniform sampler2D DiffuseSampler;
uniform vec2 ScreenSize;
uniform vec2 BlurDirection;
uniform float BlurRadius;

in vec2 texCoord;

out vec4 fragColor;

void main() {
    vec2 texel = BlurDirection / max(ScreenSize, vec2(1.0));
    float scale = max(BlurRadius, 1.0);

    vec4 color = 0.2270270 * texture(DiffuseSampler, texCoord);
    color += 0.1945946 * texture(DiffuseSampler, texCoord + texel * 1.3846154 * scale);
    color += 0.1945946 * texture(DiffuseSampler, texCoord - texel * 1.3846154 * scale);
    color += 0.1216216 * texture(DiffuseSampler, texCoord + texel * 3.2307692 * scale);
    color += 0.1216216 * texture(DiffuseSampler, texCoord - texel * 3.2307692 * scale);
    color += 0.0540540 * texture(DiffuseSampler, texCoord + texel * 5.0769230 * scale);
    color += 0.0540540 * texture(DiffuseSampler, texCoord - texel * 5.0769230 * scale);
    color += 0.0162162 * texture(DiffuseSampler, texCoord + texel * 6.9230769 * scale);
    color += 0.0162162 * texture(DiffuseSampler, texCoord - texel * 6.9230769 * scale);

    fragColor = color;
}
