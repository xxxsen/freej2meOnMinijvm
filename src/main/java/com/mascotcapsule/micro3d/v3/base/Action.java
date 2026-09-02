/*
 * Reconstructed source for the fork's bundled FreeJ2ME MascotCapsule implementation.
 * Browser backend integration changes are maintained by j2me-web contributors.
 */
package com.mascotcapsule.micro3d.v3.base;

import com.mascotcapsule.micro3d.v3.base.SparseIntArray;

public class Action {
    final int keyframes;
    final Bone[] boneActions;
    final float[] matrices;
    SparseIntArray dynamic;

    Action(int keyframes, int numBones) {
        this.keyframes = keyframes;
        this.boneActions = new Bone[numBones];
        this.matrices = new float[numBones * 12];
    }

    static final class RollAnim {
        private final int[] keys;
        final float[] values;

        RollAnim(int count) {
            this.keys = new int[count];
            this.values = new float[count];
        }

        void set(int idx, int kf, float v) {
            this.keys[idx] = kf;
            this.values[idx] = v;
        }

        float get(float kgf) {
            int max = this.keys.length - 1;
            if (kgf >= (float)this.keys[max]) {
                return this.values[max];
            }
            for (int i = max - 1; i >= 0; --i) {
                int key = this.keys[i];
                if ((float)key > kgf) continue;
                float value = this.values[i];
                if ((float)key == kgf) {
                    return value;
                }
                int nextKey = this.keys[i + 1];
                float nextValue = this.values[i + 1];
                return value + (nextValue - value) / (float)(nextKey - key) * (kgf - (float)key);
            }
            return 0.0f;
        }
    }

    static final class Animation {
        private final int[] keys;
        final float[][] values;

        Animation(int count) {
            this.keys = new int[count];
            this.values = new float[count][3];
        }

        void set(int idx, int kf, float x, float y, float z) {
            this.keys[idx] = kf;
            this.values[idx][0] = x;
            this.values[idx][1] = y;
            this.values[idx][2] = z;
        }

        void get(float kgf, float[] arr) {
            int max = this.keys.length - 1;
            if (kgf >= (float)this.keys[max]) {
                float[] value = this.values[max];
                arr[0] = value[0];
                arr[1] = value[1];
                arr[2] = value[2];
                return;
            }
            for (int i = max - 1; i >= 0; --i) {
                int prevKey = this.keys[i];
                if ((float)prevKey > kgf) continue;
                float[] prevVal = this.values[i];
                float x = prevVal[0];
                float y = prevVal[1];
                float z = prevVal[2];
                if ((float)prevKey == kgf) {
                    arr[0] = x;
                    arr[1] = y;
                    arr[2] = z;
                    return;
                }
                int nextKey = this.keys[i + 1];
                float[] nextValue = this.values[i + 1];
                float delta = (kgf - (float)prevKey) / (float)(nextKey - prevKey);
                arr[0] = x + (nextValue[0] - x) * delta;
                arr[1] = y + (nextValue[1] - y) * delta;
                arr[2] = z + (nextValue[2] - z) * delta;
                return;
            }
        }
    }

    static final class Bone {
        private final int type;
        private final int mtxOffset;
        private final float[] matrix;
        RollAnim roll;
        Animation rotate;
        Animation scale;
        Animation translate;
        private int frame = -1;

        Bone(int type, int mtxOffset, float[] matrix) {
            this.type = type;
            this.mtxOffset = mtxOffset;
            this.matrix = matrix;
        }

        void setFrame(int frame) {
            if (this.frame == frame) {
                return;
            }
            this.frame = frame;
            float kgf = (float)frame / 65536.0f;
            switch (this.type) {
                case 2: {
                    float[] arr = new float[3];
                    this.translate.get(kgf, arr);
                    this.matrix[this.mtxOffset + 3] = arr[0];
                    this.matrix[this.mtxOffset + 7] = arr[1];
                    this.matrix[this.mtxOffset + 11] = arr[2];
                    this.rotate.get(kgf, arr);
                    this.rotate(arr[0], arr[1], arr[2]);
                    float r = this.roll.get(kgf);
                    this.roll(r);
                    this.scale.get(kgf, arr);
                    float x = arr[0];
                    float y = arr[1];
                    float z = arr[2];
                    int n = this.mtxOffset;
                    this.matrix[n] = this.matrix[n] * x;
                    int n2 = this.mtxOffset + 1;
                    this.matrix[n2] = this.matrix[n2] * y;
                    int n3 = this.mtxOffset + 2;
                    this.matrix[n3] = this.matrix[n3] * z;
                    int n4 = this.mtxOffset + 4;
                    this.matrix[n4] = this.matrix[n4] * x;
                    int n5 = this.mtxOffset + 5;
                    this.matrix[n5] = this.matrix[n5] * y;
                    int n6 = this.mtxOffset + 6;
                    this.matrix[n6] = this.matrix[n6] * z;
                    int n7 = this.mtxOffset + 8;
                    this.matrix[n7] = this.matrix[n7] * x;
                    int n8 = this.mtxOffset + 9;
                    this.matrix[n8] = this.matrix[n8] * y;
                    int n9 = this.mtxOffset + 10;
                    this.matrix[n9] = this.matrix[n9] * z;
                    break;
                }
                case 3: {
                    float[] arr = (float[])this.translate.values[0].clone();
                    this.matrix[this.mtxOffset + 3] = arr[0];
                    this.matrix[this.mtxOffset + 7] = arr[1];
                    this.matrix[this.mtxOffset + 11] = arr[2];
                    this.rotate.get(kgf, arr);
                    this.rotate(arr[0], arr[1], arr[2]);
                    float r = this.roll.values[0];
                    this.roll(r);
                    break;
                }
                case 4: {
                    float[] arr = new float[3];
                    this.rotate.get(kgf, arr);
                    this.rotate(arr[0], arr[1], arr[2]);
                    float r = this.roll.get(kgf);
                    this.roll(r);
                    break;
                }
                case 5: {
                    float[] arr = new float[3];
                    this.rotate.get(kgf, arr);
                    this.rotate(arr[0], arr[1], arr[2]);
                    break;
                }
                case 6: {
                    float[] arr = new float[3];
                    this.translate.get(kgf, arr);
                    this.matrix[this.mtxOffset + 3] = arr[0];
                    this.matrix[this.mtxOffset + 7] = arr[1];
                    this.matrix[this.mtxOffset + 11] = arr[2];
                    this.rotate.get(kgf, arr);
                    this.rotate(arr[0], arr[1], arr[2]);
                    float r = this.roll.get(kgf);
                    this.roll(r);
                    break;
                }
            }
        }

        private void rotate(float x, float y, float z) {
            float rld = 1.0f / (float)Math.sqrt(x * x + y * y + z * z);
            z *= rld;
            float xx = (x *= rld) * x;
            float yy = (y *= rld) * y;
            if (xx > 0.0f || yy > 0.0f) {
                float a = (1.0f - z) / (yy + xx);
                float b = a * -(x * y);
                this.matrix[this.mtxOffset] = z + yy * a;
                this.matrix[this.mtxOffset + 1] = b;
                this.matrix[this.mtxOffset + 2] = x;
                this.matrix[this.mtxOffset + 4] = b;
                this.matrix[this.mtxOffset + 5] = z + xx * a;
                this.matrix[this.mtxOffset + 6] = y;
                this.matrix[this.mtxOffset + 8] = -x;
                this.matrix[this.mtxOffset + 9] = -y;
            } else {
                this.matrix[this.mtxOffset] = 1.0f;
                this.matrix[this.mtxOffset + 1] = 0.0f;
                this.matrix[this.mtxOffset + 2] = 0.0f;
                this.matrix[this.mtxOffset + 4] = 0.0f;
                this.matrix[this.mtxOffset + 5] = z;
                this.matrix[this.mtxOffset + 6] = 0.0f;
                this.matrix[this.mtxOffset + 8] = 0.0f;
                this.matrix[this.mtxOffset + 9] = 0.0f;
            }
            this.matrix[this.mtxOffset + 10] = z;
        }

        private void roll(float angle) {
            float s = (float)Math.sin(angle);
            float c = (float)Math.cos(angle);
            float m00 = this.matrix[this.mtxOffset];
            float m01 = this.matrix[this.mtxOffset + 1];
            float m10 = this.matrix[this.mtxOffset + 4];
            float m11 = this.matrix[this.mtxOffset + 5];
            float m20 = this.matrix[this.mtxOffset + 8];
            float m21 = this.matrix[this.mtxOffset + 9];
            this.matrix[this.mtxOffset] = m00 * c + m01 * s;
            this.matrix[this.mtxOffset + 1] = m01 * c - m00 * s;
            this.matrix[this.mtxOffset + 4] = m10 * c + m11 * s;
            this.matrix[this.mtxOffset + 5] = m11 * c - m10 * s;
            this.matrix[this.mtxOffset + 8] = m20 * c + m21 * s;
            this.matrix[this.mtxOffset + 9] = m21 * c - m20 * s;
        }
    }
}



