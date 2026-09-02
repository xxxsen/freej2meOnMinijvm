/*
 * Reconstructed source for the fork's bundled FreeJ2ME MascotCapsule implementation.
 * Browser backend integration changes are maintained by j2me-web contributors.
 */
package com.mascotcapsule.micro3d.v3;

import com.mascotcapsule.micro3d.v3.Vector3D;
import com.mascotcapsule.micro3d.v3.base.MathUtil;

public class AffineTrans {
    public int m00;
    public int m01;
    public int m02;
    public int m03;
    public int m10;
    public int m11;
    public int m12;
    public int m13;
    public int m20;
    public int m21;
    public int m22;
    public int m23;

    public AffineTrans() {
    }

    public AffineTrans(AffineTrans a) {
        this.set(a);
    }

    public AffineTrans(int[] a) {
        this.set(a);
    }

    public AffineTrans(int[][] a) {
        this.set(a);
    }

    public AffineTrans(int[] a, int offset) {
        this.set(a, offset);
    }

    public AffineTrans(int m00, int m01, int m02, int m03, int m10, int m11, int m12, int m13, int m20, int m21, int m22, int m23) {
        this.m00 = m00;
        this.m01 = m01;
        this.m02 = m02;
        this.m03 = m03;
        this.m10 = m10;
        this.m11 = m11;
        this.m12 = m12;
        this.m13 = m13;
        this.m20 = m20;
        this.m21 = m21;
        this.m22 = m22;
        this.m23 = m23;
    }

    public final void get(int[] a) {
        this.get(a, 0);
    }

    public final void get(int[] a, int offset) {
        if (a == null) {
            throw new NullPointerException();
        }
        if (offset < 0 || a.length - offset < 12) {
            throw new IllegalArgumentException();
        }
        a[offset++] = this.m00;
        a[offset++] = this.m01;
        a[offset++] = this.m02;
        a[offset++] = this.m03;
        a[offset++] = this.m10;
        a[offset++] = this.m11;
        a[offset++] = this.m12;
        a[offset++] = this.m13;
        a[offset++] = this.m20;
        a[offset++] = this.m21;
        a[offset++] = this.m22;
        a[offset] = this.m23;
    }

    public final void lookAt(Vector3D pos, Vector3D look, Vector3D up) {
        if (pos == null || look == null || up == null) {
            throw new NullPointerException();
        }
        int mpx = -pos.x;
        int mpy = -pos.y;
        int mpz = -pos.z;
        Vector3D tmp = Vector3D.outerProduct(look, up);
        tmp.unit();
        this.m00 = tmp.x;
        this.m01 = tmp.y;
        this.m02 = tmp.z;
        this.m03 = mpx * tmp.x + mpy * tmp.y + mpz * tmp.z + 2048 >> 12;
        tmp = Vector3D.outerProduct(look, tmp);
        tmp.unit();
        this.m10 = tmp.x;
        this.m11 = tmp.y;
        this.m12 = tmp.z;
        this.m13 = mpx * tmp.x + mpy * tmp.y + mpz * tmp.z + 2048 >> 12;
        tmp.set(look);
        tmp.unit();
        this.m20 = tmp.x;
        this.m21 = tmp.y;
        this.m22 = tmp.z;
        this.m23 = mpx * tmp.x + mpy * tmp.y + mpz * tmp.z + 2048 >> 12;
    }

    public final void mul(AffineTrans a) {
        if (a == null) {
            throw new NullPointerException();
        }
        this.mulA2(this, a);
    }

    public final void mul(AffineTrans a1, AffineTrans a2) {
        if (a1 == null || a2 == null) {
            throw new NullPointerException();
        }
        this.mulA2(a1, a2);
    }

    @Deprecated
    public final void multiply(AffineTrans a) {
        if (a == null) {
            throw new NullPointerException();
        }
        this.mulA2(this, a);
    }

    @Deprecated
    public final void multiply(AffineTrans a1, AffineTrans a2) {
        if (a1 == null || a2 == null) {
            throw new NullPointerException();
        }
        this.mulA2(a1, a2);
    }

    @Deprecated
    public final void rotationV(Vector3D v, int r) {
        this.setRotation(v, r);
    }

    public final void rotationX(int r) {
        int cos = MathUtil.iCos(r);
        int sin = MathUtil.iSin(r);
        this.m00 = 4096;
        this.m01 = 0;
        this.m02 = 0;
        this.m10 = 0;
        this.m11 = cos;
        this.m12 = -sin;
        this.m20 = 0;
        this.m21 = sin;
        this.m22 = cos;
    }

    public final void rotationY(int r) {
        int cos = MathUtil.iCos(r);
        int sin = MathUtil.iSin(r);
        this.m00 = cos;
        this.m01 = 0;
        this.m02 = sin;
        this.m10 = 0;
        this.m11 = 4096;
        this.m12 = 0;
        this.m20 = -sin;
        this.m21 = 0;
        this.m22 = cos;
    }

    public final void rotationZ(int r) {
        int cos = MathUtil.iCos(r);
        int sin = MathUtil.iSin(r);
        this.m00 = cos;
        this.m01 = -sin;
        this.m02 = 0;
        this.m10 = sin;
        this.m11 = cos;
        this.m12 = 0;
        this.m20 = 0;
        this.m21 = 0;
        this.m22 = 4096;
    }

    public final void set(AffineTrans a) {
        if (a == null) {
            throw new NullPointerException();
        }
        this.m00 = a.m00;
        this.m01 = a.m01;
        this.m02 = a.m02;
        this.m03 = a.m03;
        this.m10 = a.m10;
        this.m11 = a.m11;
        this.m12 = a.m12;
        this.m13 = a.m13;
        this.m20 = a.m20;
        this.m21 = a.m21;
        this.m22 = a.m22;
        this.m23 = a.m23;
    }

    public final void set(int[] a) {
        this.set(a, 0);
    }

    public final void set(int[][] a) {
        if (a == null) {
            throw new NullPointerException();
        }
        if (a.length < 3) {
            throw new IllegalArgumentException();
        }
        if (a[0].length < 4 || a[1].length < 4 || a[2].length < 4) {
            throw new IllegalArgumentException();
        }
        this.m00 = a[0][0];
        this.m01 = a[0][1];
        this.m02 = a[0][2];
        this.m03 = a[0][3];
        this.m10 = a[1][0];
        this.m11 = a[1][1];
        this.m12 = a[1][2];
        this.m13 = a[1][3];
        this.m20 = a[2][0];
        this.m21 = a[2][1];
        this.m22 = a[2][2];
        this.m23 = a[2][3];
    }

    public final void set(int[] a, int offset) {
        if (a == null) {
            throw new NullPointerException();
        }
        if (offset < 0 || a.length - offset < 12) {
            throw new IllegalArgumentException();
        }
        this.m00 = a[offset++];
        this.m01 = a[offset++];
        this.m02 = a[offset++];
        this.m03 = a[offset++];
        this.m10 = a[offset++];
        this.m11 = a[offset++];
        this.m12 = a[offset++];
        this.m13 = a[offset++];
        this.m20 = a[offset++];
        this.m21 = a[offset++];
        this.m22 = a[offset++];
        this.m23 = a[offset];
    }

    public final void set(int m00, int m01, int m02, int m03, int m10, int m11, int m12, int m13, int m20, int m21, int m22, int m23) {
        this.m00 = m00;
        this.m01 = m01;
        this.m02 = m02;
        this.m03 = m03;
        this.m10 = m10;
        this.m11 = m11;
        this.m12 = m12;
        this.m13 = m13;
        this.m20 = m20;
        this.m21 = m21;
        this.m22 = m22;
        this.m23 = m23;
    }

    public final void setIdentity() {
        this.m00 = 4096;
        this.m01 = 0;
        this.m02 = 0;
        this.m03 = 0;
        this.m10 = 0;
        this.m11 = 4096;
        this.m12 = 0;
        this.m13 = 0;
        this.m20 = 0;
        this.m21 = 0;
        this.m22 = 4096;
        this.m23 = 0;
    }

    public final void setRotation(Vector3D v, int r) {
        if (v == null) {
            throw new NullPointerException();
        }
        int x = v.x;
        int y = v.y;
        int z = v.z;
        int cos = MathUtil.iCos(r);
        int sin = MathUtil.iSin(r);
        int xs = x * sin + 2048 >> 12;
        int ys = y * sin + 2048 >> 12;
        int zs = z * sin + 2048 >> 12;
        int nc = 4096 - cos;
        int xync = (x * y + 2048 >> 12) * nc + 2048 >> 12;
        int yznc = (y * z + 2048 >> 12) * nc + 2048 >> 12;
        int zxnc = (x * z + 2048 >> 12) * nc + 2048 >> 12;
        this.m00 = cos + ((x * x + 2048 >> 12) * nc + 2048 >> 12);
        this.m01 = xync - zs;
        this.m02 = zxnc + ys;
        this.m10 = zs + xync;
        this.m11 = cos + ((y * y + 2048 >> 12) * nc + 2048 >> 12);
        this.m20 = zxnc - ys;
        this.m12 = yznc - xs;
        this.m21 = xs + yznc;
        this.m22 = cos + ((z * z + 2048 >> 12) * nc + 2048 >> 12);
    }

    @Deprecated
    public final void setRotationX(int r) {
        this.rotationX(r);
    }

    @Deprecated
    public final void setRotationY(int r) {
        this.rotationY(r);
    }

    @Deprecated
    public final void setRotationZ(int r) {
        this.rotationZ(r);
    }

    @Deprecated
    public final void setViewTrans(Vector3D pos, Vector3D look, Vector3D up) {
        this.lookAt(pos, look, up);
    }

    public final Vector3D transform(Vector3D v) {
        if (v == null) {
            throw new NullPointerException();
        }
        int x = (v.x * this.m00 + v.y * this.m01 + v.z * this.m02 + 2048 >> 12) + this.m03;
        int y = (v.x * this.m10 + v.y * this.m11 + v.z * this.m12 + 2048 >> 12) + this.m13;
        int z = (v.x * this.m20 + v.y * this.m21 + v.z * this.m22 + 2048 >> 12) + this.m23;
        return new Vector3D(x, y, z);
    }

    @Deprecated
    public final Vector3D transPoint(Vector3D v) {
        return this.transform(v);
    }

    private void mulA2(AffineTrans a1, AffineTrans a2) {
        int l00 = a1.m00;
        int l01 = a1.m01;
        int l02 = a1.m02;
        int l10 = a1.m10;
        int l11 = a1.m11;
        int l12 = a1.m12;
        int l20 = a1.m20;
        int l21 = a1.m21;
        int l22 = a1.m22;
        int r00 = a2.m00;
        int r01 = a2.m01;
        int r02 = a2.m02;
        int r03 = a2.m03;
        int r10 = a2.m10;
        int r11 = a2.m11;
        int r12 = a2.m12;
        int r13 = a2.m13;
        int r20 = a2.m20;
        int r21 = a2.m21;
        int r22 = a2.m22;
        int r23 = a2.m23;
        this.m00 = l00 * r00 + l01 * r10 + l02 * r20 + 2048 >> 12;
        this.m01 = l00 * r01 + l01 * r11 + l02 * r21 + 2048 >> 12;
        this.m02 = l00 * r02 + l01 * r12 + l02 * r22 + 2048 >> 12;
        this.m03 = (l00 * r03 + l01 * r13 + l02 * r23 + 2048 >> 12) + a1.m03;
        this.m10 = l10 * r00 + l11 * r10 + l12 * r20 + 2048 >> 12;
        this.m11 = l10 * r01 + l11 * r11 + l12 * r21 + 2048 >> 12;
        this.m12 = l10 * r02 + l11 * r12 + l12 * r22 + 2048 >> 12;
        this.m13 = (l10 * r03 + l11 * r13 + l12 * r23 + 2048 >> 12) + a1.m13;
        this.m20 = l20 * r00 + l21 * r10 + l22 * r20 + 2048 >> 12;
        this.m21 = l20 * r01 + l21 * r11 + l22 * r21 + 2048 >> 12;
        this.m22 = l20 * r02 + l21 * r12 + l22 * r22 + 2048 >> 12;
        this.m23 = (l20 * r03 + l21 * r13 + l22 * r23 + 2048 >> 12) + a1.m23;
    }
}



