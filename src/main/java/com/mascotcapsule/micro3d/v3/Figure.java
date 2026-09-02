/*
 * Reconstructed source for the fork's bundled FreeJ2ME MascotCapsule implementation.
 * Browser backend integration changes are maintained by j2me-web contributors.
 */
package com.mascotcapsule.micro3d.v3;

import com.mascotcapsule.micro3d.v3.ActionTable;
import com.mascotcapsule.micro3d.v3.Texture;
import com.mascotcapsule.micro3d.v3.base.FigureImpl;
import java.io.IOException;

public class Figure {
    Texture[] textures;
    int textureIndex = -1;
    final FigureImpl impl;

    public Figure(byte[] b) {
        this.impl = new FigureImpl(b);
    }

    public Figure(String name) throws IOException {
        this.impl = new FigureImpl(name);
    }

    public final void dispose() {
        this.impl.dispose();
    }

    public final int getNumPatterns() {
        return this.impl.getNumPatterns();
    }

    public final int getNumPattern() {
        return getNumPatterns();
    }

    public final int getNumTextures() {
        if (this.textures == null) {
            return 0;
        }
        return this.textures.length;
    }

    public final Texture getTexture() {
        if (this.textureIndex < 0) {
            return null;
        }
        return this.textures[this.textureIndex];
    }

    public final void selectTexture(int idx) {
        if (idx < 0 || idx >= this.getNumTextures()) {
            throw new IllegalArgumentException();
        }
        this.textureIndex = idx;
    }

    public final void setPattern(int idx) {
        this.impl.setPattern(idx);
    }

    public final void setPosture(ActionTable actionTable, int action, int frame) {
        if (actionTable == null) {
            throw new NullPointerException();
        }
        this.impl.setPosture(actionTable.impl, action, frame);
    }

    public final void setTexture(Texture tex) {
        if (tex == null) {
            throw new NullPointerException();
        }
        if (!tex.isForModel) {
            throw new IllegalArgumentException();
        }
        this.textures = new Texture[]{tex};
        this.textureIndex = 0;
    }

    public final void setTexture(Texture[] t) {
        if (t == null) {
            throw new NullPointerException();
        }
        if (t.length == 0) {
            throw new IllegalArgumentException();
        }
        for (Texture texture : t) {
            if (texture == null) {
                throw new NullPointerException();
            }
            if (texture.isForModel) continue;
            throw new IllegalArgumentException();
        }
        this.textures = t;
        this.textureIndex = -1;
    }
}


