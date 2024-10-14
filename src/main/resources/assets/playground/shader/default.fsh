#version 330 core

//in vec2 pass_TextureUV;

in vec3 pass_color;
in float pass_time;

out vec4 fragColor;

//uniform sampler2D tex;

void main() {
//    texture(tex, pass_TextureUV)
//    if (col.a == 0.0) {
//        discard;
//    }
    fragColor = vec4(1.0, 0.0, 1.0, 1.0);
}

