#version 300 es

precision mediump float;
in  vec2 outTexCoord;
out vec4 fragColor;

uniform sampler2D texture_sampler;

void main()
{
    fragColor = texture(texture_sampler, outTexCoord);
    //fragColor = vec4(1.0f, 1.0f, 1.0f, 1.0f);
}