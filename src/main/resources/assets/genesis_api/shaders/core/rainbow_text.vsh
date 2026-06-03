#version 150 core

layout (location = 0) in vec3 Position;
layout (location = 1) in vec4 Color;
layout (location = 2) in vec2 UV;
layout (location = 3) in uint LightMap;

uniform mat4 ModelViewMatrix;
uniform mat4 ProjMatrix;

out float v_Phase;

void main() {
    v_Phase = UV.x;                      // 把 u 坐标直接当相位
    gl_Position = ProjMatrix * ModelViewMatrix * vec4(Position, 1.0);
}