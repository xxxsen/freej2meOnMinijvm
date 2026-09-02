/*
 * Reconstructed source for the fork's bundled FreeJ2ME MascotCapsule implementation.
 * Browser backend integration changes are maintained by j2me-web contributors.
 */
package com.mascotcapsule.micro3d.v3.base;

import com.mascotcapsule.micro3d.v3.base.FrameState;

public interface Micro3dBackend {
    public void bind(Object var1, boolean var2);

    public void flushFrame(FrameState var1);

    public void flushItems(FrameState var1);

    public void release(Object var1);

    public boolean isAvailable();

    public int getTargetWidth();

    public int getTargetHeight();
}



