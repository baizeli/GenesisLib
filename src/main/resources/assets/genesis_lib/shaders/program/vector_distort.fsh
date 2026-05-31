#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D VectorSampler;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec4 vectorData = texture(VectorSampler, texCoord);


    vec2 offset = vec2(0.0);
    float strength = 0.0;

    if (vectorData.a > 0.01) {
        offset = (vectorData.rg - 0.5) * 2.0;
        strength = 0.05;
    }

    vec2 distortedUV = texCoord + (offset * strength);
    vec4 sceneColor = texture(DiffuseSampler, distortedUV);

    // 刀气染色
//    sceneColor.rgb += vec3(0.5, 0.2, 0.8) * vectorData.a * 0.2;
    // 刀气染色(基于强度)
//     vec3 tintColor = vec3(0.5, 0.2, 0.8);
//     sceneColor.rgb = mix(sceneColor.rgb, tintColor, strength * 0.2);
    fragColor = vec4(sceneColor.rgb, 1.0);
//    fragColor = texture(VectorSampler, texCoord);
}