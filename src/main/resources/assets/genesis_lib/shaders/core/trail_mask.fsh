#version 150

uniform sampler2D Sampler0;

in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

void main() {
    vec4 mask = texture(Sampler0, texCoord0);
    float maskValue = max(mask.r, max(mask.g, mask.b)) * mask.a;
    float alpha = smoothstep(0.02, 0.28, maskValue) * vertexColor.a;

    if (alpha <= 0.003) {
        discard;
    }

    vec3 glow = vertexColor.rgb * (0.85 + maskValue * 0.45);
    fragColor = vec4(glow, alpha);
}
