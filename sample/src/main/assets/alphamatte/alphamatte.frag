#version 300 es
precision mediump float;

uniform sampler2D u_tex0;
uniform sampler2D u_tex1;
uniform float u_time;

in vec2 v_texCoord0;

out vec4 fragColor;

void main()	{
    vec4 colorText = texture(u_tex0, v_texCoord0);
    vec4 colorMask = texture(u_tex1, v_texCoord0 + vec2(-u_time/20.0, 0.0));
    gl_FragColor = vec4(mix(vec3(0.0), colorText.rgb, colorMask.r), 1.0);
}