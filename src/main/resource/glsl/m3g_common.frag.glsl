#version 330
#ifdef GL_ES
precision mediump float;
#endif

in vec4 vColor;
in vec2 vTexCoord0;
in vec2 vTexCoord1;
in float vFogDistance;

uniform int uUseTexture0;
uniform int uUseTexture1;
uniform int uTextureMode0;
uniform int uTextureMode1;
uniform sampler2D uTexture0;
uniform sampler2D uTexture1;
uniform vec4 uTextureBlendColor0;
uniform vec4 uTextureBlendColor1;
uniform float uAlphaThreshold;
uniform int uFogMode;
uniform vec4 uFogColor;
uniform float uFogNear;
uniform float uFogFar;
uniform float uFogDensity;

out vec4 fragColor;

vec4 applyTexture(vec4 baseColor, vec4 sampleColor, int textureMode, vec4 blendColor) {
    if (textureMode == 0) {
        return sampleColor;
    }
    if (textureMode == 2) {
        return min(baseColor + sampleColor, vec4(1.0));
    }
    if (textureMode == 3) {
        return vec4(mix(baseColor.rgb, blendColor.rgb, sampleColor.rgb), baseColor.a * sampleColor.a);
    }
    if (textureMode == 4) {
        return vec4(mix(baseColor.rgb, sampleColor.rgb, sampleColor.a), baseColor.a);
    }
    return baseColor * sampleColor;
}

vec4 sampleM3GTexture(sampler2D tex, vec2 uv) {
    return texture(tex, uv);
}

float computeFogFactor(float distanceValue) {
    if (uFogMode == 81) {
        if (distanceValue <= 0.0) {
            return 0.0;
        }
        if (uFogFar <= uFogNear) {
            return distanceValue >= uFogFar ? 1.0 : 0.0;
        }
        return clamp((distanceValue - uFogNear) / (uFogFar - uFogNear), 0.0, 1.0);
    }
    if (uFogMode == 80) {
        if (distanceValue <= 0.0) {
            return 0.0;
        }
        return clamp(1.0 - exp(-max(0.0, uFogDensity) * distanceValue), 0.0, 1.0);
    }
    return 0.0;
}

void main() {
    vec4 color = vColor;
    if (uUseTexture0 != 0) {
        color = applyTexture(color, sampleM3GTexture(uTexture0, vTexCoord0), uTextureMode0, uTextureBlendColor0);
    }
    if (uUseTexture1 != 0) {
        color = applyTexture(color, sampleM3GTexture(uTexture1, vTexCoord1), uTextureMode1, uTextureBlendColor1);
    }
    if (color.a < uAlphaThreshold) {
        discard;
    }
    float fogFactor = computeFogFactor(vFogDistance);
    if (fogFactor > 0.0) {
        color.rgb = mix(color.rgb, uFogColor.rgb, fogFactor);
    }
    fragColor = color;
}
