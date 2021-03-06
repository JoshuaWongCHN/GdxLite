attribute vec3 a_position;
attribute vec2 a_texCoord0;

varying vec2 v_texCoord0;

void main()	{
    v_texCoord0 = vec2(a_texCoord0.x, 1.0 - a_texCoord0.y);
    gl_Position = vec4(a_position, 1.0);
}