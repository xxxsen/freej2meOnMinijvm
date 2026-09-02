/*
 * Reconstructed source for the fork's bundled FreeJ2ME MascotCapsule implementation.
 * Browser backend integration changes are maintained by j2me-web contributors.
 */
package com.mascotcapsule.micro3d.v3.base;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

public class BufferUtils {
    private BufferUtils() {
    }

    public static IntBuffer createIntBuffer(int capacity) {
        return BufferUtils.createByteBuffer(capacity * 4).asIntBuffer();
    }

    public static FloatBuffer createFloatBuffer(int capacity) {
        return BufferUtils.createByteBuffer(capacity * 4).asFloatBuffer();
    }

    public static ByteBuffer createByteBuffer(int capacity) {
        return ByteBuffer.allocateDirect(capacity).order(ByteOrder.nativeOrder());
    }

    public static ShortBuffer createShortBuffer(int capacity) {
        return BufferUtils.createByteBuffer(capacity * 2).asShortBuffer();
    }
}



