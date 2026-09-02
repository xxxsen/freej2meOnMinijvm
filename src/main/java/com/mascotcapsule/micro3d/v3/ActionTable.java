/*
 * Reconstructed source for the fork's bundled FreeJ2ME MascotCapsule implementation.
 * Browser backend integration changes are maintained by j2me-web contributors.
 */
package com.mascotcapsule.micro3d.v3;

import com.mascotcapsule.micro3d.v3.base.ActTableImpl;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ActionTable {
    final ActTableImpl impl;

    public ActionTable(byte[] b) {
        this.impl = new ActTableImpl(b);
    }

    public ActionTable(String name) throws IOException {
        this.impl = new ActTableImpl(name);
    }

    public ActionTable(InputStream input) throws IOException {
        this(readAll(input));
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

    @Deprecated
    public final int getNumAction() {
        return this.impl.getNumActions();
    }

    public final int getNumActions() {
        return this.impl.getNumActions();
    }

    @Deprecated
    public final int getNumFrame(int idx) {
        return this.impl.getNumFrames(idx);
    }

    public final int getNumFrames(int idx) {
        return this.impl.getNumFrames(idx);
    }

    public int getMaxFrame(int idx) {
        return getNumFrames(idx);
    }
}


