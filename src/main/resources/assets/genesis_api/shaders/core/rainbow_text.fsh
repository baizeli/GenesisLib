#version 150 core

in float v_Phase;          // 由顶点着色器传过来的相位（就是 uv.x）
out vec4 fragColor;

uniform sampler1D u_Rainbow;

void main() {
    fragColor = texture(u_Rainbow, v_Phase);
}