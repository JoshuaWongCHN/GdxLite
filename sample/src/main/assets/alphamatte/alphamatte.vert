#version 300 es

in vec3 a_position;
in vec2 a_texCoord0;

out vec2 v_texCoord0;

void main()	{
    v_texCoord0 = vec2(a_texCoord0.x, 1.-a_texCoord0.y);

    gl_Position = vec4(a_position, 1.0);
}