/*
 * Reconstructed source for the fork's bundled FreeJ2ME MascotCapsule implementation.
 * Browser backend integration changes are maintained by j2me-web contributors.
 */
package com.mascotcapsule.micro3d.v3.base;

import java.util.Arrays;

public class SparseIntArray
implements Cloneable {
    private int[] mKeys;
    private int[] mValues;
    private int mSize;

    public SparseIntArray() {
        this(0);
    }

    public SparseIntArray(int initialCapacity) {
        if (initialCapacity == 0) {
            this.mKeys = new int[0];
            this.mValues = new int[0];
        } else {
            this.mKeys = new int[initialCapacity];
            this.mValues = new int[this.mKeys.length];
        }
        this.mSize = 0;
    }

    public SparseIntArray clone() {
        SparseIntArray clone = null;
        try {
            clone = (SparseIntArray)super.clone();
            clone.mKeys = (int[])this.mKeys.clone();
            clone.mValues = (int[])this.mValues.clone();
        }
        catch (CloneNotSupportedException cloneNotSupportedException) {
            // empty catch block
        }
        return clone;
    }

    public int get(int key) {
        return this.get(key, 0);
    }

    public int get(int key, int valueIfKeyNotFound) {
        int i = SparseIntArray.search(this.mKeys, this.mSize, key);
        if (i < 0) {
            return valueIfKeyNotFound;
        }
        return this.mValues[i];
    }

    public void delete(int key) {
        int i = SparseIntArray.search(this.mKeys, this.mSize, key);
        if (i >= 0) {
            this.removeAt(i);
        }
    }

    public void removeAt(int index) {
        System.arraycopy(this.mKeys, index + 1, this.mKeys, index, this.mSize - (index + 1));
        System.arraycopy(this.mValues, index + 1, this.mValues, index, this.mSize - (index + 1));
        --this.mSize;
    }

    public void put(int key, int value) {
        int i = SparseIntArray.search(this.mKeys, this.mSize, key);
        if (i >= 0) {
            this.mValues[i] = value;
        } else {
            this.mKeys = SparseIntArray.insert(this.mKeys, this.mSize, i ^= 0xFFFFFFFF, key);
            this.mValues = SparseIntArray.insert(this.mValues, this.mSize, i, value);
            ++this.mSize;
        }
    }

    public int size() {
        return this.mSize;
    }

    public int keyAt(int index) {
        if (index >= this.mSize) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
        return this.mKeys[index];
    }

    public int valueAt(int index) {
        if (index >= this.mSize) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
        return this.mValues[index];
    }

    public void setValueAt(int index, int value) {
        if (index >= this.mSize) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
        this.mValues[index] = value;
    }

    public int indexOfKey(int key) {
        return SparseIntArray.search(this.mKeys, this.mSize, key);
    }

    public int indexOfValue(int value) {
        for (int i = 0; i < this.mSize; ++i) {
            if (this.mValues[i] != value) continue;
            return i;
        }
        return -1;
    }

    public void clear() {
        this.mSize = 0;
    }

    public void append(int key, int value) {
        if (this.mSize != 0 && key <= this.mKeys[this.mSize - 1]) {
            this.put(key, value);
            return;
        }
        this.mKeys = SparseIntArray.append(this.mKeys, this.mSize, key);
        this.mValues = SparseIntArray.append(this.mValues, this.mSize, value);
        ++this.mSize;
    }

    public int[] copyKeys() {
        if (this.size() == 0) {
            return null;
        }
        return Arrays.copyOf(this.mKeys, this.size());
    }

    public String toString() {
        if (this.size() <= 0) {
            return "{}";
        }
        StringBuilder buffer = new StringBuilder(this.mSize * 28);
        buffer.append('{');
        for (int i = 0; i < this.mSize; ++i) {
            if (i > 0) {
                buffer.append(", ");
            }
            int key = this.keyAt(i);
            buffer.append(key);
            buffer.append('=');
            int value = this.valueAt(i);
            buffer.append(value);
        }
        buffer.append('}');
        return buffer.toString();
    }

    private static int search(int[] mKeys, int mSize, int key) {
        for (int i = 0; i < mSize && i < mKeys.length; ++i) {
            if (mKeys[i] != key) continue;
            return i;
        }
        return -1;
    }

    private static int[] append(int[] mKeys, int mSize, int key) {
        if (mKeys.length <= mSize) {
            int[] nArray = mKeys;
            mKeys = new int[mSize + 16];
            System.arraycopy(nArray, 0, mKeys, 0, mSize);
        }
        mKeys[mSize] = key;
        return mKeys;
    }

    private static int[] insert(int[] mKeys, int mSize, int i, int key) {
        int size;
        if (mKeys.length <= mSize) {
            int[] nArray = mKeys;
            mKeys = new int[mSize + 16];
            System.arraycopy(nArray, 0, mKeys, 0, mSize);
        }
        if ((size = mSize - i) > 0) {
            System.arraycopy(mKeys, i, mKeys, i + 1, size);
        }
        mKeys[i] = key;
        return mKeys;
    }
}



