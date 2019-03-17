uniform mat4 u_projViewTrans;
uniform mat4 u_worldTrans;
uniform float u_time;
uniform vec3 u_dir;

attribute vec3 a_position;
attribute float a_speed;
attribute float a_start;
attribute float a_type;

varying float v_type;

void main()	{
    v_type = a_type;

    vec3 position = a_position + u_dir * a_speed * (u_time - a_start);

    gl_Position = u_projViewTrans * u_worldTrans * vec4(position, 1.0);
    gl_PointSize = 10. + a_position.z * 50.;
}