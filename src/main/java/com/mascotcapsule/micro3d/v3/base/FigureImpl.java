/*
 * Reconstructed source for the fork's bundled FreeJ2ME MascotCapsule implementation.
 * Browser backend integration changes are maintained by j2me-web contributors.
 */
package com.mascotcapsule.micro3d.v3.base;

import com.mascotcapsule.micro3d.v3.base.ActTableImpl;
import com.mascotcapsule.micro3d.v3.base.Action;
import com.mascotcapsule.micro3d.v3.base.BufferUtils;
import com.mascotcapsule.micro3d.v3.base.Loader;
import com.mascotcapsule.micro3d.v3.base.Model;
import com.mascotcapsule.micro3d.v3.base.Resources;
import com.mascotcapsule.micro3d.v3.base.SparseIntArray;
import com.mascotcapsule.micro3d.v3.base.Utils;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Arrays;

public class FigureImpl {
    Model model;
    private int pattern;

    public FigureImpl(byte[] b) {
        if (b == null) {
            throw new NullPointerException();
        }
        try {
            this.init(b, 0, b.length);
        }
        catch (Exception e) {
            System.err.println("Error loading data");
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public FigureImpl(byte[] b, int offset, int length) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        }
        if (offset < 0 || offset + length > b.length) {
            throw new ArrayIndexOutOfBoundsException();
        }
        try {
            this.init(b, offset, length);
        }
        catch (Exception e) {
            System.err.println("Error loading data");
            e.printStackTrace();
            throw e;
        }
    }

    public FigureImpl(String name) throws IOException {
        if (name == null) {
            throw new NullPointerException();
        }
        byte[] bytes = Resources.getBytes(name);
        if (bytes == null) {
            throw new IOException("Error reading resource: " + name);
        }
        try {
            this.init(bytes, 0, bytes.length);
        }
        catch (Exception e) {
            System.err.println("Error loading data from [" + name + "]");
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private synchronized void init(byte[] bytes, int offset, int length) throws IOException {
        this.model = Loader.loadMbacData(bytes, offset, length);
        Utils.transform(this.model.originalVertices, this.model.vertices, this.model.originalNormals, this.model.normals, this.model.bones, null);
        this.sortPolygons();
        this.fillTexCoordBuffer();
    }

    private void sortPolygons() {
        Model.Polygon[] polygonsT = this.model.polygonsT;
        Arrays.sort(polygonsT, (a, b) -> {
            if (a.blendMode != b.blendMode) {
                return a.blendMode - b.blendMode;
            }
            if (a.face != b.face) {
                return a.face - b.face;
            }
            return a.doubleFace - b.doubleFace;
        });
        int[][][] subMeshesLengthsT = this.model.subMeshesLengthsT;
        int[] indexArray = this.model.indices;
        int pos = 0;
        for (Model.Polygon p : polygonsT) {
            int[] indices = p.indices;
            int length = indices.length;
            int[] nArray = subMeshesLengthsT[p.blendMode >> 1][p.face];
            int n = p.doubleFace;
            nArray[n] = nArray[n] + length;
            System.arraycopy(indices, 0, indexArray, pos, length);
            pos += length;
        }
        Model.Polygon[] polygonsC = this.model.polygonsC;
        Arrays.sort(polygonsC, (a, b) -> {
            if (a.blendMode != b.blendMode) {
                return a.blendMode - b.blendMode;
            }
            return a.doubleFace - b.doubleFace;
        });
        int[][] subMeshesLengthsC = this.model.subMeshesLengthsC;
        for (Model.Polygon p : polygonsC) {
            int[] indices = p.indices;
            int length = indices.length;
            int[] nArray = subMeshesLengthsC[p.blendMode >> 1];
            int n = p.doubleFace;
            nArray[n] = nArray[n] + length;
            System.arraycopy(indices, 0, indexArray, pos, length);
            pos += length;
        }
    }

    private void fillTexCoordBuffer() {
        ByteBuffer buffer = this.model.texCoordArray;
        buffer.rewind();
        for (Model.Polygon poly : this.model.polygonsT) {
            buffer.put(poly.texCoords);
            poly.texCoords = null;
        }
        for (Model.Polygon poly : this.model.polygonsC) {
            buffer.put(poly.texCoords);
            poly.texCoords = null;
        }
        buffer.rewind();
    }

    public final void dispose() {
        this.model = null;
    }

    public final synchronized void setPosture(ActTableImpl actTable, int action, int frame) {
        if (action < 0 || action >= actTable.getNumActions()) {
            throw new IllegalArgumentException();
        }
        Action act = actTable.actions[action];
        SparseIntArray dynamic = act.dynamic;
        if (dynamic != null) {
            int iFrame = frame < 0 ? 0 : frame >> 16;
            for (int i = 0; i <= dynamic.size(); ++i) {
                if (dynamic.keyAt(i) > iFrame) continue;
                this.pattern = dynamic.valueAt(i);
                this.applyPattern();
                break;
            }
        }
        this.applyBoneAction(act, frame < 0 ? 0 : frame);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void applyBoneAction(Action act, int frame) {
        Action.Bone[] actionBones = act.boneActions;
        if (actionBones.length == 0) {
            return;
        }
        float[] fArray = act.matrices;
        synchronized (act.matrices) {
            for (Action.Bone actionBone : actionBones) {
                actionBone.setFrame(frame);
            }
            Utils.transform(this.model.originalVertices, this.model.vertices, this.model.originalNormals, this.model.normals, this.model.bones, act.matrices);
            // ** MonitorExit[var4_4] (shouldn't be in output)
            return;
        }
    }

    private void applyPattern() {
        int length;
        int i;
        int pp;
        int[] indices;
        int[] indexArray = this.model.indices;
        int pos = 0;
        int invalid = this.model.vertices.capacity() / 3 - 1;
        for (Model.Polygon p : this.model.polygonsT) {
            indices = p.indices;
            length = indices.length;
            pp = p.pattern;
            if ((pp & this.pattern) == pp) {
                for (i = 0; i < length; ++i) {
                    indexArray[pos++] = indices[i];
                }
                continue;
            }
            for (length = indices.length; length > 0; --length) {
                indexArray[pos++] = invalid;
            }
        }
        for (Model.Polygon p : this.model.polygonsC) {
            indices = p.indices;
            length = indices.length;
            pp = p.pattern;
            if ((pp & this.pattern) == pp) {
                for (i = 0; i < length; ++i) {
                    indexArray[pos++] = indices[i];
                }
                continue;
            }
            for (length = indices.length; length > 0; --length) {
                indexArray[pos++] = invalid;
            }
        }
    }

    public final int getNumPatterns() {
        return this.model.numPatterns;
    }

    public final synchronized void setPattern(int idx) {
        this.pattern = idx;
        this.applyPattern();
    }

    synchronized void prepareBuffers() {
        if (this.model.vertexArray == null) {
            this.model.vertexArray = BufferUtils.createFloatBuffer(this.model.vertexArrayCapacity);
        }
        Utils.fillBuffer(this.model.vertexArray, this.model.vertices, this.model.indices);
        if (this.model.originalNormals == null) {
            return;
        }
        if (this.model.normalsArray == null) {
            this.model.normalsArray = BufferUtils.createFloatBuffer(this.model.vertexArrayCapacity);
        }
        Utils.fillBuffer(this.model.normalsArray, this.model.normals, this.model.indices);
    }

    public synchronized void setPosture(ActTableImpl actTable, int action, int frame, int pattern) {
        if (action < 0 || action >= actTable.getNumActions()) {
            throw new IllegalArgumentException();
        }
        this.pattern = pattern;
        this.applyPattern();
        this.applyBoneAction(actTable.actions[action], frame < 0 ? 0 : frame);
    }

    synchronized void fillBuffers(FloatBuffer vertices, FloatBuffer normals) {
        Utils.fillBuffer(vertices, this.model.vertices, this.model.indices);
        if (normals != null) {
            Utils.fillBuffer(normals, this.model.normals, this.model.indices);
        }
    }
}


