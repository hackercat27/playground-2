#version 330 core

in vec3 position;
//in vec2 textureUV;

//out vec2 pass_TextureUV;
in vec3 color;
in float time;

out vec3 pass_color;
out float pass_time;

uniform mat4 transform;

void main() {
//    pass_TextureUV = textureUV;

    pass_color = vec3(position.x, position.y, 1.0);

    gl_Position = transform * vec4(position.x, position.y, position.z, 1.0);
}