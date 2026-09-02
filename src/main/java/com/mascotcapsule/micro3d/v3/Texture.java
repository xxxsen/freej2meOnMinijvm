/*
 * Reconstructed source for the fork's bundled FreeJ2ME MascotCapsule implementation.
 * Browser backend integration changes are maintained by j2me-web contributors.
 */
package com.mascotcapsule.micro3d.v3;

import com.mascotcapsule.micro3d.v3.base.TextureImpl;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Texture {
    public final TextureImpl impl;
    final boolean isForModel;
    protected Effect3D shading;

    public Texture() {
        this.isForModel = true;
        this.impl = new TextureImpl();
    }

    public Texture(byte[] b, boolean isForModel) {
        this.isForModel = isForModel;
        this.impl = new TextureImpl(b);
    }

    public Texture(String name, boolean isForModel) throws IOException {
        this.isForModel = isForModel;
        this.impl = new TextureImpl(name);
    }

    public Texture(InputStream input, boolean isForModel) throws IOException {
        this(readAll(input), isForModel);
    }

    private static byte[] readAll(InputStream input) throws IOException {
        if (input == null) throw new NullPointerException();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int count;
        while ((count = input.read(buffer)) >= 0) {
            if (count > 0) output.write(buffer, 0, count);
        }
        return output.toByteArray();
    }

    public final void dispose() {
        this.impl.dispose();
    }

    public void setNormalShader() {
        shading = new Effect3D();
    }

    public void setToonShader(int threshold, int high, int low) {
        if (shading == null) shading = new Effect3D();
        shading.setShadingType(Effect3D.TOON_SHADING);
        shading.setToonParams(threshold, high, low);
    }

    public final Effect3D getShading() {
        return shading;
    }
}


