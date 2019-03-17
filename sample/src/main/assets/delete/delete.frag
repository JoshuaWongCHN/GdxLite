precision highp float;

uniform sampler2D u_tex0;

varying vec2 v_texCoord0;

void main()	{
    gl_FragColor = vec4(texture2D(u_tex0, v_texCoord0));
}