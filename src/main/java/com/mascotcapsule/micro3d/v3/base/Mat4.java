/*
 * Reconstructed source for the fork's bundled FreeJ2ME MascotCapsule implementation.
 * Browser backend integration changes are maintained by j2me-web contributors.
 */
package com.mascotcapsule.micro3d.v3.base;

public final class Mat4 {
    private Mat4() {
    }

    public static void multiply(float[] dst, float[] a, float[] b) {
        float a00 = a[0];
        float a01 = a[4];
        float a02 = a[8];
        float a03 = a[12];
        float a10 = a[1];
        float a11 = a[5];
        float a12 = a[9];
        float a13 = a[13];
        float a20 = a[2];
        float a21 = a[6];
        float a22 = a[10];
        float a23 = a[14];
        float a30 = a[3];
        float a31 = a[7];
        float a32 = a[11];
        float a33 = a[15];
        float b0 = b[0];
        float b1 = b[1];
        float b2 = b[2];
        float b3 = b[3];
        dst[0] = a00 * b0 + a01 * b1 + a02 * b2 + a03 * b3;
        dst[1] = a10 * b0 + a11 * b1 + a12 * b2 + a13 * b3;
        dst[2] = a20 * b0 + a21 * b1 + a22 * b2 + a23 * b3;
        dst[3] = a30 * b0 + a31 * b1 + a32 * b2 + a33 * b3;
        b0 = b[4];
        b1 = b[5];
        b2 = b[6];
        b3 = b[7];
        dst[4] = a00 * b0 + a01 * b1 + a02 * b2 + a03 * b3;
        dst[5] = a10 * b0 + a11 * b1 + a12 * b2 + a13 * b3;
        dst[6] = a20 * b0 + a21 * b1 + a22 * b2 + a23 * b3;
        dst[7] = a30 * b0 + a31 * b1 + a32 * b2 + a33 * b3;
        b0 = b[8];
        b1 = b[9];
        b2 = b[10];
        b3 = b[11];
        dst[8] = a00 * b0 + a01 * b1 + a02 * b2 + a03 * b3;
        dst[9] = a10 * b0 + a11 * b1 + a12 * b2 + a13 * b3;
        dst[10] = a20 * b0 + a21 * b1 + a22 * b2 + a23 * b3;
        dst[11] = a30 * b0 + a31 * b1 + a32 * b2 + a33 * b3;
        b0 = b[12];
        b1 = b[13];
        b2 = b[14];
        b3 = b[15];
        dst[12] = a00 * b0 + a01 * b1 + a02 * b2 + a03 * b3;
        dst[13] = a10 * b0 + a11 * b1 + a12 * b2 + a13 * b3;
        dst[14] = a20 * b0 + a21 * b1 + a22 * b2 + a23 * b3;
        dst[15] = a30 * b0 + a31 * b1 + a32 * b2 + a33 * b3;
    }

    public static void transformPoint(float[] m, float x, float y, float z, float[] out) {
        out[0] = x * m[0] + y * m[4] + z * m[8] + m[12];
        out[1] = x * m[1] + y * m[5] + z * m[9] + m[13];
        out[2] = x * m[2] + y * m[6] + z * m[10] + m[14];
        out[3] = x * m[3] + y * m[7] + z * m[11] + m[15];
    }

    public static void transformDirection(float[] m, float x, float y, float z, float[] out) {
        out[0] = x * m[0] + y * m[4] + z * m[8];
        out[1] = x * m[1] + y * m[5] + z * m[9];
        out[2] = x * m[2] + y * m[6] + z * m[10];
    }
}



