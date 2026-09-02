/*
 * Reconstructed source for the fork's bundled FreeJ2ME MascotCapsule implementation.
 * Browser backend integration changes are maintained by j2me-web contributors.
 */
package com.mascotcapsule.micro3d.v3.base;

import com.mascotcapsule.micro3d.v3.base.Loader;
import com.mascotcapsule.micro3d.v3.base.Resources;
import com.mascotcapsule.micro3d.v3.base.TextureData;
import java.io.IOException;

public final class TextureImpl {
    public final TextureData image;
    private final boolean isMutable;

    public TextureImpl() {
        this.image = new TextureData(256, 256);
        this.isMutable = true;
    }

    public TextureImpl(byte[] b) {
        if (b == null) {
            throw new NullPointerException();
        }
        try {
            this.image = Loader.loadBmpData(b, 0, b.length);
        }
        catch (IOException e) {
            System.err.println("Error loading data");
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        this.isMutable = false;
    }

    public TextureImpl(byte[] b, int offset, int length) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        }
        if (offset < 0 || offset + length > b.length) {
            throw new ArrayIndexOutOfBoundsException();
        }
        try {
            this.image = Loader.loadBmpData(b, offset, length);
        }
        catch (Exception e) {
            System.err.println("Error loading data");
            e.printStackTrace();
            throw e;
        }
        this.isMutable = false;
    }

    public TextureImpl(String name) throws IOException {
        if (name == null) {
            throw new NullPointerException();
        }
        byte[] b = Resources.getBytes(name);
        if (b == null) {
            throw new IOException();
        }
        try {
            this.image = Loader.loadBmpData(b, 0, b.length);
        }
        catch (IOException e) {
            System.err.println("Error loading data from [" + name + "]");
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        this.isMutable = false;
    }

    public void dispose() {
    }

    public boolean isMutable() {
        return this.isMutable;
    }

    public int getWidth() {
        return this.image.width;
    }

    public int getHeight() {
        return this.image.height;
    }
}



