#version 150

in vec4 vertexColor;

out vec4 fragColor;

void main() {
    vec3 glow = vertexColor.rgb * (1.18 + vertexColor.a * 0.75);
    fragColor = vec4(glow, vertexColor.a);
}
