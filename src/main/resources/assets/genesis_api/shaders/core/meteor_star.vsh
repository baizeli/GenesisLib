#version 150

in vec3 Position;
in vec4 Color;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform float Time;

out vec4 vertexColor;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    float shimmer = 0.88 + 0.12 * sin(Time * 45.0 + Position.x * 23.0 + Position.y * 17.0 + Position.z * 31.0);
    vertexColor = vec4(Color.rgb * shimmer, Color.a);
}
