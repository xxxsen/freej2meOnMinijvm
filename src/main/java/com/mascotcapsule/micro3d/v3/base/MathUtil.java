/*
 * Reconstructed source for the fork's bundled FreeJ2ME MascotCapsule implementation.
 * Browser backend integration changes are maintained by j2me-web contributors.
 */
package com.mascotcapsule.micro3d.v3.base;

public class MathUtil {
    public static final float TO_FLOAT = 2.4414062E-4f;
    static final float TO_RADIANS = 0.0015339808f;
    static final float[] IDENTITY_AFFINE = new float[]{1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f};

    public static int uSqrt(int p) {
        double a;
        if (p == 0) {
            return 0;
        }
        if (p < 0) {
            if (p > -196606) {
                return 65535;
            }
            a = (long)p & 0xFFFFFFFFL;
        } else {
            a = p;
        }
        return (int)Math.round(Math.sqrt(a));
    }

    public static int iSin(int p) {
        double radian = (double)p * Math.PI / 2048.0;
        return (int)Math.round(Math.sin(radian) * 4096.0);
    }

    public static int iCos(int p) {
        return MathUtil.iSin(p + 1024);
    }

    public static int iSqrt(int x) {
        if (x < 0) {
            throw new IllegalArgumentException("Negative arg=" + x);
        }
        if (x == 0) {
            return 0;
        }
        return (int)Math.round(Math.sqrt(x));
    }

    public static int iAtan2(int y, int x) {
        return (int)Math.round(Math.atan2((double)y / 4096.0, (double)x / 4096.0) * 4096.0);
    }

    static void multiplyMM(float[] m, float[] pm, float[] mvm) {
        float l00 = pm[0];
        float l01 = pm[4];
        float l02 = pm[8];
        float l03 = pm[12];
        float l10 = pm[1];
        float l11 = pm[5];
        float l12 = pm[9];
        float l13 = pm[13];
        float l20 = pm[2];
        float l21 = pm[6];
        float l22 = pm[10];
        float l23 = pm[14];
        float l30 = pm[3];
        float l31 = pm[7];
        float l32 = pm[11];
        float l33 = pm[15];
        float r00 = mvm[0];
        float r01 = mvm[3];
        float r02 = mvm[6];
        float r03 = mvm[9];
        float r10 = mvm[1];
        float r11 = mvm[4];
        float r12 = mvm[7];
        float r13 = mvm[10];
        float r20 = mvm[2];
        float r21 = mvm[5];
        float r22 = mvm[8];
        float r23 = mvm[11];
        m[0] = l00 * r00 + l01 * r10 + l02 * r20;
        m[1] = l10 * r00 + l11 * r10 + l12 * r20;
        m[2] = l20 * r00 + l21 * r10 + l22 * r20;
        m[3] = l30 * r00 + l31 * r10 + l32 * r20;
        m[4] = l00 * r01 + l01 * r11 + l02 * r21;
        m[5] = l10 * r01 + l11 * r11 + l12 * r21;
        m[6] = l20 * r01 + l21 * r11 + l22 * r21;
        m[7] = l30 * r01 + l31 * r11 + l32 * r21;
        m[8] = l00 * r02 + l01 * r12 + l02 * r22;
        m[9] = l10 * r02 + l11 * r12 + l12 * r22;
        m[10] = l20 * r02 + l21 * r12 + l22 * r22;
        m[11] = l30 * r02 + l31 * r12 + l32 * r22;
        m[12] = l00 * r03 + l01 * r13 + l02 * r23 + l03;
        m[13] = l10 * r03 + l11 * r13 + l12 * r23 + l13;
        m[14] = l20 * r03 + l21 * r13 + l22 * r23 + l23;
        m[15] = l30 * r03 + l31 * r13 + l32 * r23 + l33;
    }

    public static void rotateM12(float[] m, int ax, int ay, int az, float angle) {
        float rm22;
        float rm12;
        float rm02;
        float rm21;
        float rm11;
        float rm01;
        float rm20;
        float rm10;
        float rm00;
        float s = (float)Math.sin(angle *= 0.0015339808f);
        float c = (float)Math.cos(angle);
        if (4096 == ax && 0 == ay && 0 == az) {
            rm00 = 1.0f;
            rm10 = 0.0f;
            rm20 = 0.0f;
            rm01 = 0.0f;
            rm11 = c;
            rm21 = s;
            rm02 = 0.0f;
            rm12 = -s;
            rm22 = c;
        } else if (0 == ax && 4096 == ay && 0 == az) {
            rm00 = c;
            rm10 = 0.0f;
            rm20 = -s;
            rm01 = 0.0f;
            rm11 = 1.0f;
            rm21 = 0.0f;
            rm02 = s;
            rm12 = 0.0f;
            rm22 = c;
        } else if (0 == ax && 0 == ay && 4096 == az) {
            rm00 = c;
            rm10 = s;
            rm20 = 0.0f;
            rm01 = -s;
            rm11 = c;
            rm21 = 0.0f;
            rm02 = 0.0f;
            rm12 = 0.0f;
            rm22 = 1.0f;
        } else {
            float rLen = 1.0f / MathUtil.vectorLength(ax, ay, az);
            float x = (float)ax * rLen;
            float y = (float)ay * rLen;
            float z = (float)az * rLen;
            float nc = 1.0f - c;
            float xs = x * s;
            float ys = y * s;
            float zs = z * s;
            float xync = x * y * nc;
            float zxnc = z * x * nc;
            float yznc = y * z * nc;
            rm00 = x * x * nc + c;
            rm01 = xync - zs;
            rm02 = zxnc + ys;
            rm10 = xync + zs;
            rm11 = y * y * nc + c;
            rm12 = yznc - xs;
            rm20 = zxnc - ys;
            rm21 = yznc + xs;
            rm22 = z * z * nc + c;
        }
        float r00 = m[0];
        float r01 = m[3];
        float r02 = m[6];
        float r03 = m[9];
        float r10 = m[1];
        float r11 = m[4];
        float r12 = m[7];
        float r13 = m[10];
        float r20 = m[2];
        float r21 = m[5];
        float r22 = m[8];
        float r23 = m[11];
        m[0] = rm00 * r00 + rm01 * r10 + rm02 * r20;
        m[1] = rm10 * r00 + rm11 * r10 + rm12 * r20;
        m[2] = rm20 * r00 + rm21 * r10 + rm22 * r20;
        m[3] = rm00 * r01 + rm01 * r11 + rm02 * r21;
        m[4] = rm10 * r01 + rm11 * r11 + rm12 * r21;
        m[5] = rm20 * r01 + rm21 * r11 + rm22 * r21;
        m[6] = rm00 * r02 + rm01 * r12 + rm02 * r22;
        m[7] = rm10 * r02 + rm11 * r12 + rm12 * r22;
        m[8] = rm20 * r02 + rm21 * r12 + rm22 * r22;
        m[9] = rm00 * r03 + rm01 * r13 + rm02 * r23;
        m[10] = rm10 * r03 + rm11 * r13 + rm12 * r23;
        m[11] = rm20 * r03 + rm21 * r13 + rm22 * r23;
    }

    public static float vectorLength(float x, float y, float z) {
        return (float)Math.sqrt(x * x + y * y + z * z);
    }

    public static int clamp(int v, int min, int max) {
        return v < min ? min : (v > max ? max : v);
    }
}



