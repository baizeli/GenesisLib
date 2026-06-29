#version 150

in vec3 Position;
in vec4 Color;
in vec2 UV0;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out vec4 vertexColor;
out vec2 texCoord;
out vec2 screenCoord;
out vec3 viewPos;

void main() {
    vec4 view = ModelViewMat * vec4(Position, 1.0);
    vec4 clip = ProjMat * view;
    gl_Position = clip;
    vertexColor = Color;
    texCoord = UV0;
    screenCoord = clip.xy / clip.w * 0.5 + 0.5;
    viewPos = view.xyz;
}
