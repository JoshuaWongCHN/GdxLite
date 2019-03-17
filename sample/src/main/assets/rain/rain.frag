precision mediump float;

uniform sampler2D u_tex0;

varying float v_type;

void main()	{
    vec4 color;

    if(v_type >= 0.) {
        color = texture2D(u_tex0, gl_PointCoord);
    } else {
        color = vec4(0., 0., 0., 1.);
    }

    gl_FragColor = color;
}