#version 150

in vec4 vColor;
in vec2 vUV;

uniform vec4 slashColor;
uniform vec4 slashCoreColor;

out vec4 fragColor;

void main() {
    vec2 pos = vUV * 2.0 - 1.0;

    float lengthMask = 1.0 - pow(abs(pos.x), 2.5);
    if (lengthMask <= 0.01) discard;

    float cleaveProgress = vColor.r;
    bool isLeftToRight = vColor.g > 0.5;

    float currentX = pos.x * 0.5 + 0.5;
    if (isLeftToRight) {
        currentX = 1.0 - currentX;
    }

    float wipeMask = smoothstep(cleaveProgress, cleaveProgress - 0.3, currentX);
    float finalMask = lengthMask * wipeMask;
    if (finalMask <= 0.01) discard;

    float core = smoothstep(0.15, 0.0, abs(pos.y));
    float glow = smoothstep(0.8, 0.0, abs(pos.y));

    vec3 coreColor = slashCoreColor.rgb;
    vec3 glowColor = slashColor.rgb;
    vec3 finalColor = (glowColor * glow * 1.5) + (coreColor * core * 2.0);

    float bladeFront = smoothstep(cleaveProgress - 0.1, cleaveProgress, currentX);
    finalColor += mix(coreColor, glowColor, 0.25) * bladeFront * 1.5;

    fragColor = vec4(finalColor, finalMask * vColor.a);
}
