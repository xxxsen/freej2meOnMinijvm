/*
 * Reconstructed source for the fork's bundled FreeJ2ME MascotCapsule implementation.
 * Browser backend integration changes are maintained by j2me-web contributors.
 */
package com.mascotcapsule.micro3d.v3.base;

import com.mascotcapsule.micro3d.v3.base.Action;
import com.mascotcapsule.micro3d.v3.base.Loader;
import com.mascotcapsule.micro3d.v3.base.Resources;
import com.mascotcapsule.micro3d.v3.base.SparseIntArray;
import java.io.IOException;

public class ActTableImpl {
    Action[] actions;

    public ActTableImpl(byte[] b) {
        if (b == null) {
            throw new NullPointerException();
        }
        try {
            this.actions = Loader.loadMtraData(b, 0, b.length);
        }
        catch (IOException e) {
            System.err.println("Error loading data");
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public ActTableImpl(byte[] b, int offset, int length) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        }
        if (offset < 0 || offset + length > b.length) {
            throw new ArrayIndexOutOfBoundsException();
        }
        try {
            this.actions = Loader.loadMtraData(b, offset, length);
        }
        catch (Exception e) {
            System.err.println("Error loading data");
            e.printStackTrace();
            throw e;
        }
    }

    public ActTableImpl(String name) throws IOException {
        if (name == null) {
            throw new NullPointerException();
        }
        byte[] bytes = Resources.getBytes(name);
        if (bytes == null) {
            throw new IOException();
        }
        try {
            this.actions = Loader.loadMtraData(bytes, 0, bytes.length);
        }
        catch (IOException e) {
            System.err.println("Error loading data from [" + name + "]");
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public final void dispose() {
        this.actions = null;
    }

    public final int getNumActions() {
        this.checkDisposed();
        return this.actions.length;
    }

    public final int getNumFrames(int idx) {
        this.checkDisposed();
        if (idx < 0 || idx >= this.actions.length) {
            throw new IllegalArgumentException();
        }
        return this.actions[idx].keyframes << 16;
    }

    private void checkDisposed() {
        if (this.actions == null) {
            throw new IllegalStateException("ActionTable disposed!");
        }
    }

    public int getPattern(int action, int frame, int defValue) {
        Action act = this.actions[action];
        SparseIntArray dynamic = act.dynamic;
        if (dynamic != null) {
            int iFrame = frame < 0 ? 0 : frame >> 16;
            for (int i = dynamic.size() - 1; i >= 0; --i) {
                if (dynamic.keyAt(i) > iFrame) continue;
                return dynamic.valueAt(i);
            }
        }
        return defValue;
    }
}



