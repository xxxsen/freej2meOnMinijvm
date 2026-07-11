#version 330
#ifdef GL_ES
precision mediump float;
#endif

// micro3d (MascotCapsule v3) GL backend vertex shader.
//
// Vertex layout (location-based, interleaved VBO uploaded per draw bucket):
//   loc 0: vec3 aPosition   (model space, already skinned)
//   loc 1: vec3 aNormal     (model space, already skinned; (0,0,0) if none)
//   loc 2: vec4 aColor      (per-vertex rgba; white if unused)
//   loc 3: vec2 aTexCoord   (texel units, 0..texSize-1)
//   loc 4: vec4 aFlags      (x=lightFlag, y=specularFlag, z=transparentFlag, w=clipW-or-0)
//
// Matrices:
//   uMvp        : column-major mat4 = proj * view   (view is the packed 3x4 AffineTrans)
//   uNormalMat  : mat3 = upper-left 3x3 of the view matrix (raw rotation, row-major
//                 source packed column-major here; transforms model normals to view space)

layout(location = 0) in vec3 aPosition;
layout(location = 1) in vec3 aNormal;
layout(location = 2) in vec4 aColor;
layout(location = 3) in vec2 aTexCoord;
layout(location = 4) in vec4 aFlags;

uniform mat4 uMvp;
uniform mat3 uNormalMat;

out vec2 vTexCoord;       // texel units
out vec3 vViewNormal;     // view-space normal (for lighting + sphere map)
out vec4 vColor;
flat out int vLightFlag;
flat out int vSpecularFlag;
flat out int vTransparentFlag;

void main() {
    if (abs(aFlags.w) > 1.0e-6) {
        gl_Position = vec4(aPosition, aFlags.w);
    } else {
        gl_Position = uMvp * vec4(aPosition, 1.0);
    }
    vViewNormal = uNormalMat * aNormal;
    vColor = aColor;
    vTexCoord = aTexCoord;
    vLightFlag = aFlags.x > 0.5 ? 1 : 0;
    vSpecularFlag = aFlags.y > 0.5 ? 1 : 0;
    vTransparentFlag = aFlags.z > 0.5 ? 1 : 0;
}
