#version 300 es

layout (location=0) in vec3 position;
layout (location=1) in vec2 texCoord;

out vec2 outTexCoord;

//uniform mat4 worldMatrix;
uniform mat4 projectionMatrix;
uniform mat4 objectPos;

void main()
{
    //gl_Position = projectionMatrix * worldMatrix * vec4(position, 1.0);
    gl_Position = projectionMatrix * objectPos * vec4(position, 1.0);
    outTexCoord = texCoord;
}