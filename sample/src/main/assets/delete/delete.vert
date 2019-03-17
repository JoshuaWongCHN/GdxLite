attribute vec3 a_position;
attribute vec2 a_texCoord0;

attribute vec2 a_velocity;
attribute vec2 a_acceleration;
attribute float a_start;

uniform float u_time;
uniform mat4 u_projViewTrans;
uniform mat4 u_worldTrans;

varying vec2 v_texCoord0;

void main()	{
    v_texCoord0 = vec2(a_texCoord0.x, 1.-a_texCoord0.y);

    float t = max(u_time - a_start, 0.0);
    vec2 position = a_position.xy + a_velocity * t + 0.5 * a_acceleration * t * t;
    gl_Position = u_projViewTrans * u_worldTrans * vec4(position.xy, 0.0, 1.0);
}