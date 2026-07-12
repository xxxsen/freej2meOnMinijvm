#version 330
#ifdef GL_ES
precision mediump float;
#endif

// micro3d (MascotCapsule v3) GL backend fragment shader.
// Mirrors the reference SoftwareMicro3dBackend/Micro3dRasterizer shading math.

in vec2 vTexCoord;       // texel units (0..texSize-1)
in vec3 vViewNormal;     // view-space normal
in vec4 vColor;
flat in int vLightFlag;
flat in int vSpecularFlag;
flat in int vTransparentFlag;

uniform int  uHasTexture;       // 0/1
uniform int  uEnableLighting;   // 0/1  (global env lighting on)
uniform int  uToon;             // 0/1
uniform int  uUseTextureAlpha;  // 0/1  (point-sprite path keeps texel alpha)
uniform vec2 uTexSize;          // texture width/height in texels (for uv normalization)
uniform sampler2D uTexture;

uniform int  uHasSphere;        // 0/1
uniform sampler2D uSphere;

// Light (single ambient + directional). Intensities & direction are pre-converted
// to float by the CPU (divide fixed-point by 4096); direction is NOT normalized here yet.
uniform float uAmbIntensity;    // ambIntensity / 4096
uniform float uDirIntensity;    // dirIntensity / 4096
uniform vec3  uLightDir;        // direction vector (raw, will normalize)
uniform int   uToonThreshold;   // 0..255
uniform int   uToonHigh;        // 0..255
uniform int   uToonLow;         // 0..255

// blend mode: 0=NORMAL, 2=HALF, 4=ADD, 6=SUB. Blending is handled by GL state.
uniform int uBlendMode;

out vec4 fragColor;

void main() {
    vec4 baseColor = vColor;

    // texture sampling + color-key
    if (uHasTexture != 0) {
        vec2 uv = vTexCoord / uTexSize;
        vec4 texel = texture(uTexture, uv);
        // color-key discard (transparent flag => alpha < 128 is transparent)
        if (vTransparentFlag != 0 && texel.a < 0.5) {
            discard;
        }
        if (uUseTextureAlpha != 0) {
            baseColor = vec4(baseColor.rgb * texel.rgb, baseColor.a * texel.a);
        } else {
            // modulate, alpha forced opaque (reference tex.fsh semantics)
            baseColor = vec4(baseColor.rgb * texel.rgb, 1.0);
        }
    }

    vec3 finalRgb = baseColor.rgb;

    // lighting (per-fragment, like the reference rasterizer)
    if (uEnableLighting != 0 && vLightFlag != 0) {
        vec3 n = vViewNormal;
        float len = length(n);
        if (len > 1.0e-6) {
            n /= len;
            vec3 l = uLightDir;
            float dlen = length(l);
            if (dlen <= 1.0e-6) {
                l = vec3(0.0, 0.0, 1.0);
            } else {
                l /= dlen;
            }
            float lambert = dot(n, l);
            if (lambert < 0.0) lambert = 0.0;

            float amb = uAmbIntensity;
            float dir = uDirIntensity * lambert;
            if (dir > 4.0) dir = 4.0;
            float light = amb + dir;
            if (light < 0.0) light = 0.0;
            if (light > 1.0) light = 1.0;

            if (uToon != 0) {
                light = (light * 255.0 < float(uToonThreshold))
                        ? float(uToonLow) / 255.0
                        : float(uToonHigh) / 255.0;
            }
            finalRgb = baseColor.rgb * light;
        }
    }

    // sphere / specular additive map (only when lighting enabled)
    if (uHasSphere != 0 && uEnableLighting != 0 && vSpecularFlag != 0) {
        vec3 n = vViewNormal;
        float len = length(n);
        if (len > 1.0e-6) {
            n /= len;
        }
        float su = n.x / 128.0 + 32.0;
        float sv = n.y / 128.0 + 32.0;
        // CLAMP_TO_EDGE equivalent; texelFetch needs integer tex coords, emulate with clamp
        ivec2 sphereSize = textureSize(uSphere, 0);
        int tx = int(clamp(su, 0.0, float(sphereSize.x - 1)));
        int ty = int(clamp(sv, 0.0, float(sphereSize.y - 1)));
        vec3 sphereRgb = texelFetch(uSphere, ivec2(tx, ty), 0).rgb;
        finalRgb += sphereRgb;
    }

    finalRgb = clamp(finalRgb, 0.0, 1.0);

    fragColor = vec4(finalRgb, uUseTextureAlpha != 0 ? baseColor.a : 1.0);
}
