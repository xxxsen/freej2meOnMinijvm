#version 330
#ifdef GL_ES
precision mediump float;
#endif

layout(location = 0) in vec3 aPosition;
layout(location = 1) in vec4 aColor;
layout(location = 2) in vec2 aTexCoord0;
layout(location = 3) in vec2 aTexCoord1;

uniform mat4 uMvp;
uniform mat4 uModelView;
uniform float uDepthRangeScale;
uniform float uDepthRangeBias;

out vec4 vColor;
out vec2 vTexCoord0;
out vec2 vTexCoord1;
out float vFogDistance;

void main() {
    vec4 eyePosition = uModelView * vec4(aPosition, 1.0);
    gl_Position = uMvp * vec4(aPosition, 1.0);
    gl_Position.z = gl_Position.z * uDepthRangeScale + gl_Position.w * uDepthRangeBias;
    vColor = aColor;
    vTexCoord0 = aTexCoord0;
    vTexCoord1 = aTexCoord1;
    vFogDistance = length(eyePosition.xyz);
}
