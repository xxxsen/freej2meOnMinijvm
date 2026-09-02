/*
 * Reconstructed source for the fork's bundled FreeJ2ME MascotCapsule implementation.
 * Browser backend integration changes are maintained by j2me-web contributors.
 */
package com.mascotcapsule.micro3d.v3.base;

import com.mascotcapsule.micro3d.v3.base.BufferUtils;
import java.nio.ByteBuffer;

public class TextureData {
    private final ByteBuffer raster;
    final int width;
    final int height;

    TextureData(int width, int height) {
        this.raster = BufferUtils.createByteBuffer(width * height * 4);
        this.width = width;
        this.height = height;
    }

    public ByteBuffer getRaster() {
        this.raster.rewind();
        return this.raster;
    }
}



