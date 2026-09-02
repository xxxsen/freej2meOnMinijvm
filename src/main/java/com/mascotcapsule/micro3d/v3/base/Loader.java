/*
 * Reconstructed source for the fork's bundled FreeJ2ME MascotCapsule implementation.
 * Browser backend integration changes are maintained by j2me-web contributors.
 */
package com.mascotcapsule.micro3d.v3.base;

import com.mascotcapsule.micro3d.v3.base.Action;
import com.mascotcapsule.micro3d.v3.base.BufferUtils;
import com.mascotcapsule.micro3d.v3.base.MathUtil;
import com.mascotcapsule.micro3d.v3.base.Model;
import com.mascotcapsule.micro3d.v3.base.SparseIntArray;
import com.mascotcapsule.micro3d.v3.base.TextureData;
import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

class Loader {
    private static final int BMP_FILE_HEADER_SIZE = 14;
    private static final int BMP_VERSION_3 = 40;
    private static final int BMP_VERSION_CORE = 12;
    private static final int[] POOL_NORMALS = new int[]{0, 0, 4096, 0, 0, -4096, 0, 0};
    private static final int[] SIZES = new int[]{8, 10, 13, 16};
    private final byte[] data;
    private final int length;
    private int pos;
    private int cached;
    private int cache;

    private Loader(byte[] data, int offset, int length) {
        this.data = data;
        this.length = length;
        this.pos = offset;
    }

    static Model loadMbacData(byte[] data, int offset, int length) throws IOException {
        int numColors;
        int numPatterns;
        int numPolyC4;
        int numPolyC3;
        int numTextures;
        int boneFormat;
        int polygonFormat;
        int normalFormat;
        int vertexFormat;
        Loader loader = new Loader(data, offset, length);
        if (loader.readUByte() != 77 || loader.readUByte() != 66) {
            throw new RuntimeException("Not a MBAC file");
        }
        int version = loader.readUByte();
        if (loader.readUByte() != 0 || version < 2 || version > 5) {
            throw new RuntimeException("Unsupported MBAC version: " + version);
        }
        if (version > 3) {
            vertexFormat = loader.readUByte();
            normalFormat = loader.readUByte();
            polygonFormat = loader.readUByte();
            boneFormat = loader.readUByte();
        } else {
            vertexFormat = 1;
            normalFormat = 0;
            polygonFormat = 1;
            boneFormat = 1;
        }
        if (boneFormat != 1) {
            throw new RuntimeException("Unexpected bone format: " + boneFormat);
        }
        int numVertices = loader.readUShort();
        int numPolyT3 = loader.readUShort();
        int numPolyT4 = loader.readUShort();
        int numBones = loader.readUShort();
        if (polygonFormat < 3) {
            numTextures = 1;
            numPolyC3 = 0;
            numPolyC4 = 0;
            numPatterns = 1;
            numColors = 0;
        } else {
            numPolyC3 = loader.readUShort();
            numPolyC4 = loader.readUShort();
            numTextures = loader.readUShort();
            numPatterns = loader.readUShort();
            numColors = loader.readUShort();
        }
        if (numVertices > 21845 || numTextures > 16 || numPatterns > 33 || numColors > 256) {
            throw new RuntimeException(String.format("MBAC format error:\nnumVertices=%d numTextures=%d numPatterns=%d numColors=%d\n", numVertices, numTextures, numPatterns, numColors));
        }
        Model model = new Model(numVertices, numBones, numPatterns, numTextures, numPolyT3, numPolyT4, numPolyC3, numPolyC4);
        int[][][] patterns = new int[numPatterns][numTextures + 1][2];
        if (version == 5) {
            for (int i = 0; i < numPatterns; ++i) {
                int[][] pattern = patterns[i];
                pattern[0][0] = loader.readUShort();
                pattern[0][1] = loader.readUShort();
                for (int j = 1; j <= numTextures; ++j) {
                    pattern[j][0] = loader.readUShort();
                    pattern[j][1] = loader.readUShort();
                }
            }
        } else {
            patterns[0] = new int[][]{{numPolyC3, numPolyC4}, {numPolyT3, numPolyT4}};
        }
        if (vertexFormat == 1) {
            loader.readVerticesV1(model.originalVertices);
        } else if (vertexFormat == 2) {
            loader.readVerticesV2(model.originalVertices);
        } else {
            throw new RuntimeException("Unexpected vertexFormat: " + vertexFormat);
        }
        loader.clearCache();
        model.originalVertices.rewind();
        if (normalFormat != 0) {
            FloatBuffer normals = BufferUtils.createFloatBuffer(numVertices * 3);
            if (normalFormat == 1) {
                try {
                    loader.readNormalsV1(normals);
                }
                catch (IOException e) {
                    e.printStackTrace();
                    throw new RuntimeException("Normals loading error", e);
                }
            } else if (normalFormat == 2) {
                try {
                    loader.readNormalsV2(normals);
                }
                catch (IOException e) {
                    e.printStackTrace();
                    throw new RuntimeException("Normals loading error", e);
                }
            } else {
                throw new RuntimeException("Unsupported normalFormat: " + normalFormat);
            }
            normals.rewind();
            model.originalNormals = normals;
            int len = numVertices * 3 + 3;
            model.normals = BufferUtils.createFloatBuffer(len);
            model.normals.put(--len, 1.0f);
        }
        loader.clearCache();
        if (model.hasPolyC) {
            loader.readPolyC(model, numVertices, numColors, numPolyC3);
        }
        if (model.hasPolyT) {
            switch (polygonFormat) {
                case 1: {
                    loader.readPolyV1(model, numVertices, numPolyT3);
                    break;
                }
                case 2: {
                    loader.readPolyV2(model, numVertices, numPolyT3);
                    break;
                }
                case 3: {
                    loader.readPolyV3(model, numVertices, numPolyT3);
                    break;
                }
                default: {
                    throw new RuntimeException("Unexpected polygonFormat: " + polygonFormat);
                }
            }
        }
        loader.clearCache();
        int c3 = 0;
        int c4 = numPolyC3;
        int t3 = 0;
        int t4 = numPolyT3;
        Model.Polygon[] polygonsC = model.polygonsC;
        Model.Polygon[] polygonsT = model.polygonsT;
        for (int i = 0; i < numPatterns; ++i) {
            int j;
            int[][] pattern = patterns[i];
            int[] polygons = pattern[0];
            int cnt = polygons[0];
            int p = i == 0 ? 0 : 1 << i;
            for (j = 0; j < cnt; ++j) {
                polygonsC[c3++].pattern = p;
            }
            cnt = polygons[1];
            for (j = 0; j < cnt; ++j) {
                polygonsC[c4++].pattern = p;
            }
            for (j = 0; j < numTextures; ++j) {
                Model.Polygon polygon;
                int k;
                polygons = pattern[j + 1];
                cnt = polygons[0];
                for (k = 0; k < cnt; ++k) {
                    polygon = polygonsT[t3++];
                    polygon.pattern = p;
                    polygon.face = j;
                }
                cnt = polygons[1];
                for (k = 0; k < cnt; ++k) {
                    polygon = polygonsT[t4++];
                    polygon.pattern = p;
                    polygon.face = j;
                }
            }
        }
        int count = loader.readBones(numBones, model);
        if (count != numVertices) {
            throw new RuntimeException("Bones vertices = " + count + ", but all vertices = " + numVertices);
        }
        int available = loader.available();
        if (version >= 4) {
            available -= 20;
        }
        if (available > 0) {
            System.out.println("Uninterpreted bytes in MBAC (" + available + ", v=" + version);
        }
        return model;
    }

    static Action[] loadMtraData(byte[] data, int offset, int length) throws IOException {
        Loader loader = new Loader(data, offset, length);
        if (loader.readUByte() != 77 || loader.readUByte() != 84) {
            throw new RuntimeException("Not a MTRA file");
        }
        int version = loader.readUByte();
        if (loader.readUByte() != 0 || version < 2 || version > 5) {
            throw new RuntimeException("Unsupported version: " + version);
        }
        int numActions = loader.readUShort();
        int numBones = loader.readUShort();
        Action[] actions = new Action[numActions];
        int[] transTypeCounts = new int[8];
        for (int i = 0; i < 8; ++i) {
            transTypeCounts[i] = loader.readUShort();
        }
        if (transTypeCounts[7] != 0) {
            System.out.println("ActTableData: transTypeCounts[7] = " + transTypeCounts[7]);
        }
        int dataSize = loader.readInt();
        for (int action = 0; action < numActions; ++action) {
            SparseIntArray sparseIntArray;
            Action act;
            int keyframes = loader.readUShort();
            actions[action] = act = new Action(keyframes, numBones);
            for (int bone = 0; bone < numBones; ++bone) {
                act.boneActions[bone] = loader.readBoneAction(act, bone * 12);
            }
            if (version < 5) continue;
            int count = loader.readUShort();
            actions[action].dynamic = sparseIntArray = new SparseIntArray(count);
            for (int j = 0; j < count; ++j) {
                int frame = loader.readUShort();
                int pattern = loader.readInt();
                sparseIntArray.put(frame, pattern);
            }
        }
        int available = loader.available();
        if (version >= 4) {
            available -= 20;
        }
        if (available > 0) {
            System.out.println("ActTableData: uninterpreted bytes in MTRA");
        }
        return actions;
    }

    static TextureData loadBmpData(byte[] data, int offset, int length) throws IOException {
        int stride;
        boolean reversed;
        int numColors;
        int height;
        int width;
        Loader loader = new Loader(data, offset, length);
        if (loader.readUByte() != 66 || loader.readUByte() != 77) {
            throw new RuntimeException("Not a BMP!");
        }
        loader.skip(8);
        int rasterOffset = loader.readInt();
        int dibHeaderSize = loader.readInt();
        if (dibHeaderSize == 12) {
            width = loader.readUShort();
            height = loader.readUShort();
            loader.skip(2);
            int bpp = loader.readUShort();
            if (bpp != 8) {
                throw new RuntimeException("Unsupported BMP format: bpp = " + bpp);
            }
            numColors = 256;
            reversed = true;
        } else if (dibHeaderSize == 40) {
            width = loader.readInt();
            int h = loader.readInt();
            if (h < 0) {
                height = -h;
                reversed = false;
            } else {
                height = h;
                reversed = true;
            }
            loader.skip(2);
            int bpp = loader.readUShort();
            if (bpp != 8) {
                throw new RuntimeException("Unsupported BMP format: bpp = " + bpp);
            }
            int compression = loader.readInt();
            if (compression != 0) {
                throw new RuntimeException("Unsupported BMP format: compression = " + compression);
            }
            loader.skip(12);
            numColors = loader.readInt();
            if (numColors == 0) {
                numColors = 256;
            }
            loader.skip(4);
        } else {
            throw new RuntimeException("Unsupported BMP version = " + dibHeaderSize);
        }
        int paletteOffset = 14 + dibHeaderSize;
        if (rasterOffset < paletteOffset + numColors * 4) {
            rasterOffset = paletteOffset + numColors * 4;
        }
        TextureData textureData = new TextureData(width, height);
        ByteBuffer raster = textureData.getRaster();
        int remainder = width % 4;
        int n = stride = remainder == 0 ? width : width + 4 - remainder;
        if (reversed) {
            for (int i = height - 1; i >= 0; --i) {
                int j = rasterOffset + i * stride;
                int s = j + width;
                for (; j < s; ++j) {
                    byte idx = data[j];
                    int p = (idx & 0xFF) * 4 + paletteOffset;
                    byte b = data[p++];
                    byte g = data[p++];
                    byte r = data[p];
                    raster.put(r).put(g).put(b).put((byte)(idx == 0 ? 0 : 255));
                }
            }
        } else {
            for (int i = 0; i < height; ++i) {
                int j = rasterOffset + i * stride;
                int s = j + width;
                for (; j < s; ++j) {
                    byte idx = data[j];
                    int p = (idx & 0xFF) * 4 + paletteOffset;
                    byte b = data[p++];
                    byte g = data[p++];
                    byte r = data[p];
                    raster.put(r).put(g).put(b).put((byte)(idx == 0 ? 0 : 255));
                }
            }
        }
        return textureData;
    }

    private void readVerticesV1(FloatBuffer vertices) throws IOException {
        while (vertices.hasRemaining()) {
            vertices.put(this.readShort());
        }
    }

    private void readVerticesV2(FloatBuffer vertices) throws IOException {
        while (vertices.hasRemaining()) {
            int chunk = this.readUBits(8);
            int type = chunk >> 6;
            int size = SIZES[type];
            int count = (chunk & 0x3F) + 1;
            if (count > vertices.remaining()) {
                throw new IOException("Vertex data largest numVertices param");
            }
            for (int i = 0; i < count; ++i) {
                vertices.put(this.readBits(size));
                vertices.put(this.readBits(size));
                vertices.put(this.readBits(size));
            }
        }
    }

    private void readNormalsV1(FloatBuffer normals) throws IOException {
        while (normals.hasRemaining()) {
            normals.put(this.readShort());
        }
    }

    private void readNormalsV2(FloatBuffer normals) throws IOException {
        int len = normals.capacity() / 3;
        for (int i = 0; i < len; ++i) {
            int y;
            int z;
            int x = this.readUBits(7);
            if (x == 64) {
                int type = this.readUBits(3);
                if (type > 5) {
                    throw new RuntimeException("Normal read error");
                }
                z = POOL_NORMALS[type++];
                y = POOL_NORMALS[type++];
                x = POOL_NORMALS[type];
            } else {
                x = x << 25 >> 19;
                y = this.readUBits(7) << 25 >> 19;
                int sign = this.readUBits(1);
                int dq = 0x1000000 - x * x - y * y;
                int n = z = dq > 0 ? (int)Math.round(Math.sqrt(dq)) : 0;
                if (sign == 1) {
                    z = -z;
                }
            }
            normals.put(x);
            normals.put(y);
            normals.put(z);
        }
    }

    private void readPolyC(Model model, int numVertex, int numColor, int numTriangles) throws IOException {
        int c;
        int b;
        int a;
        int material;
        int i;
        int materialBits = this.readUByte();
        int vertexIndexBits = this.readUByte();
        int colorBits = this.readUByte();
        int colorIdBits = this.readUByte();
        int unknownByte = this.readUByte();
        if (unknownByte != 0) {
            System.out.println("PolyC unknownByte = " + unknownByte);
        }
        byte[] colors = new byte[numColor * 3];
        for (int i2 = 0; i2 < colors.length; ++i2) {
            colors[i2] = (byte)this.readUBits(colorBits);
        }
        Model.Polygon[] polygonsC = model.polygonsC;
        for (i = 0; i < numTriangles; ++i) {
            Model.Polygon polygon;
            material = this.readUBits(materialBits) << 1;
            if ((material & 0xFC09) != 0) {
                throw new RuntimeException("Unexpected material: " + material);
            }
            a = this.readUBits(vertexIndexBits);
            b = this.readUBits(vertexIndexBits);
            c = this.readUBits(vertexIndexBits);
            if (a >= numVertex || b >= numVertex || c >= numVertex) {
                throw new IOException("Format error: indices greatest or equal num vertices");
            }
            int colorId = this.readUBits(colorIdBits) * 3;
            byte R = colors[colorId++];
            byte G = colors[colorId++];
            byte B = colors[colorId];
            byte light = (byte)((material & 0x20) >> 5);
            byte specular = (byte)((material & 0x40) >> 6);
            byte[] materialData = new byte[]{R, G, B, light, specular, R, G, B, light, specular, R, G, B, light, specular};
            polygonsC[i] = polygon = new Model.Polygon(material, materialData, a, b, c);
        }
        for (i = numTriangles; i < polygonsC.length; ++i) {
            Model.Polygon polygon;
            material = this.readUBits(materialBits) << 1;
            if ((material & 0xFC09) != 0) {
                throw new RuntimeException("Unexpected material: " + material);
            }
            a = this.readUBits(vertexIndexBits);
            b = this.readUBits(vertexIndexBits);
            c = this.readUBits(vertexIndexBits);
            int d = this.readUBits(vertexIndexBits);
            if (a >= numVertex || b >= numVertex || c >= numVertex || d >= numVertex) {
                throw new IOException("Format error: indices greatest or equal num vertices");
            }
            int colorId = this.readUBits(colorIdBits) * 3;
            byte R = colors[colorId++];
            byte G = colors[colorId++];
            byte B = colors[colorId];
            byte light = (byte)((material & 0x20) >> 5);
            byte specular = (byte)((material & 0x40) >> 6);
            byte[] materialData = new byte[]{R, G, B, light, specular, R, G, B, light, specular, R, G, B, light, specular, R, G, B, light, specular, R, G, B, light, specular, R, G, B, light, specular};
            polygonsC[i] = polygon = new Model.Polygon(material, materialData, a, b, c, c, b, d);
        }
    }

    private void readPolyV1(Model model, int numVertices, int numPolyT3) throws IOException {
        int i;
        Model.Polygon[] polygons = model.polygonsT;
        for (i = 0; i < numPolyT3; ++i) {
            int material = this.readUShort();
            if ((material & 0xFFF9) != 0) {
                throw new IOException("Unexpected material: " + material);
            }
            int a = this.readUShort();
            int b = this.readUShort();
            int c = this.readUShort();
            if (a >= numVertices || b >= numVertices || c >= numVertices) {
                throw new IOException("Format error: indices greatest or equal num vertices");
            }
            int mat = (material & 4) << 2 | (material & 2) >> 1;
            byte transparent = (byte)(mat & 1);
            byte[] texCoords = new byte[]{this.readByte(), this.readByte(), 1, 0, transparent, this.readByte(), this.readByte(), 1, 0, transparent, this.readByte(), this.readByte(), 1, 0, transparent};
            polygons[i] = new Model.Polygon(mat, texCoords, a, b, c);
        }
        int len = polygons.length;
        for (i = numPolyT3; i < len; ++i) {
            int material = this.readUShort();
            if ((material & 0xFFF8) != 0 || (material & 1) == 0) {
                throw new IOException("Unexpected material: " + material);
            }
            int a = this.readUShort();
            int b = this.readUShort();
            int c = this.readUShort();
            int d = this.readUShort();
            if (a >= numVertices || b >= numVertices || c >= numVertices || d >= numVertices) {
                throw new IOException("Format error: indices greatest or equal num vertices");
            }
            byte uA = this.readByte();
            byte vA = this.readByte();
            byte uB = this.readByte();
            byte vB = this.readByte();
            byte uC = this.readByte();
            byte vC = this.readByte();
            byte uD = this.readByte();
            byte vD = this.readByte();
            int mat = (material & 4) << 2 | (material & 2) >> 1;
            byte transparent = (byte)(mat & 1);
            byte[] texCoords = new byte[]{uA, vA, 1, 0, transparent, uB, vB, 1, 0, transparent, uC, vC, 1, 0, transparent, uC, vC, 1, 0, transparent, uB, vB, 1, 0, transparent, uD, vD, 1, 0, transparent};
            polygons[i] = new Model.Polygon(mat, texCoords, a, b, c, c, b, d);
        }
    }

    private void readPolyV2(Model model, int numVertex, int numTriangles) throws IOException {
        int i;
        int matBitSize = this.readUByte();
        int vertexIdxSize = this.readUByte();
        Model.Polygon[] polygons = model.polygonsT;
        for (i = 0; i < numTriangles; ++i) {
            int material = this.readUBits(matBitSize);
            if ((material & 0xFF88) != 0) {
                throw new IOException("Unexpected material: " + material);
            }
            int a = this.readUBits(vertexIdxSize);
            int b = this.readUBits(vertexIdxSize);
            int c = this.readUBits(vertexIdxSize);
            if (a >= numVertex || b >= numVertex || c >= numVertex) {
                throw new IOException("Format error: indices greatest or equal num vertices");
            }
            byte transparent = (byte)(material & 1);
            byte light = (byte)((material & 0x20) >> 5);
            byte specular = (byte)((material & 0x40) >> 6);
            byte[] texCoords = new byte[]{(byte)this.readUBits(7), (byte)this.readUBits(7), light, specular, transparent, (byte)this.readUBits(7), (byte)this.readUBits(7), light, specular, transparent, (byte)this.readUBits(7), (byte)this.readUBits(7), light, specular, transparent};
            polygons[i] = new Model.Polygon(material, texCoords, a, b, c);
        }
        int len = polygons.length;
        for (i = numTriangles; i < len; ++i) {
            int material = this.readUBits(matBitSize);
            if ((material & 0xFF88) != 0) {
                throw new RuntimeException("Unexpected material: " + material);
            }
            int a = this.readUBits(vertexIdxSize);
            int b = this.readUBits(vertexIdxSize);
            int c = this.readUBits(vertexIdxSize);
            int d = this.readUBits(vertexIdxSize);
            if (a >= numVertex || b >= numVertex || c >= numVertex || d >= numVertex) {
                throw new RuntimeException("Format error: indices greatest or equal num vertices");
            }
            byte uA = (byte)this.readUBits(7);
            byte vA = (byte)this.readUBits(7);
            byte uB = (byte)this.readUBits(7);
            byte vB = (byte)this.readUBits(7);
            byte uC = (byte)this.readUBits(7);
            byte vC = (byte)this.readUBits(7);
            byte uD = (byte)this.readUBits(7);
            byte vD = (byte)this.readUBits(7);
            byte transparent = (byte)(material & 1);
            byte light = (byte)((material & 0x20) >> 5);
            byte specular = (byte)((material & 0x40) >> 6);
            byte[] texCoords = new byte[]{uA, vA, light, specular, transparent, uB, vB, light, specular, transparent, uC, vC, light, specular, transparent, uC, vC, light, specular, transparent, uB, vB, light, specular, transparent, uD, vD, light, specular, transparent};
            polygons[i] = new Model.Polygon(material, texCoords, a, b, c, c, b, d);
        }
    }

    private void readPolyV3(Model model, int numVertex, int numTriangles) throws IOException {
        int i;
        int materialBits = this.readUBits(8);
        int vertexIndexBits = this.readUBits(8);
        int uvBits = this.readUBits(8);
        int unknownByte = this.readUBits(8);
        if (unknownByte != 0) {
            System.out.println("PolyT v3: unknownByte = " + unknownByte);
        }
        Model.Polygon[] polygons = model.polygonsT;
        for (i = 0; i < numTriangles; ++i) {
            int material = this.readUBits(materialBits);
            if ((material & 0xFC08) != 0) {
                throw new IOException("Unexpected material: " + material);
            }
            int a = this.readUBits(vertexIndexBits);
            int b = this.readUBits(vertexIndexBits);
            int c = this.readUBits(vertexIndexBits);
            if (a >= numVertex || b >= numVertex || c >= numVertex) {
                throw new IOException("Format error: indices greatest or equal num vertices");
            }
            byte transparent = (byte)(material & 1);
            byte light = (byte)((material & 0x20) >> 5);
            byte specular = (byte)((material & 0x40) >> 6);
            byte[] texCoords = new byte[]{(byte)this.readUBits(uvBits), (byte)this.readUBits(uvBits), light, specular, transparent, (byte)this.readUBits(uvBits), (byte)this.readUBits(uvBits), light, specular, transparent, (byte)this.readUBits(uvBits), (byte)this.readUBits(uvBits), light, specular, transparent};
            polygons[i] = new Model.Polygon(material, texCoords, a, b, c);
        }
        int len = polygons.length;
        for (i = numTriangles; i < len; ++i) {
            int material = this.readUBits(materialBits);
            if ((material & 0xFC08) != 0) {
                throw new IOException("Unexpected material: " + material);
            }
            int a = this.readUBits(vertexIndexBits);
            int b = this.readUBits(vertexIndexBits);
            int c = this.readUBits(vertexIndexBits);
            int d = this.readUBits(vertexIndexBits);
            if (a >= numVertex || b >= numVertex || c >= numVertex || d >= numVertex) {
                throw new IOException("Format error: indices greatest or equal num vertices");
            }
            byte uA = (byte)this.readUBits(uvBits);
            byte vA = (byte)this.readUBits(uvBits);
            byte uB = (byte)this.readUBits(uvBits);
            byte vB = (byte)this.readUBits(uvBits);
            byte uC = (byte)this.readUBits(uvBits);
            byte vC = (byte)this.readUBits(uvBits);
            byte uD = (byte)this.readUBits(uvBits);
            byte vD = (byte)this.readUBits(uvBits);
            byte transparent = (byte)(material & 1);
            byte light = (byte)((material & 0x20) >> 5);
            byte specular = (byte)((material & 0x40) >> 6);
            byte[] texCoords = new byte[]{uA, vA, light, specular, transparent, uB, vB, light, specular, transparent, uC, vC, light, specular, transparent, uC, vC, light, specular, transparent, uB, vB, light, specular, transparent, uD, vD, light, specular, transparent};
            polygons[i] = new Model.Polygon(material, texCoords, a, b, c, c, b, d);
        }
    }

    private int readBones(int numBones, Model model) throws IOException {
        ByteBuffer bones = model.bones;
        int boneVertexSum = 0;
        for (int i = 0; i < numBones; ++i) {
            int boneVertices = this.readUShort();
            short parent = this.readShort();
            if (parent < -1) {
                throw new RuntimeException("Format error (negative parent). Please report this bug");
            }
            bones.putInt(boneVertices);
            bones.putInt(parent);
            bones.putFloat((float)this.readShort() * 2.4414062E-4f);
            bones.putFloat((float)this.readShort() * 2.4414062E-4f);
            bones.putFloat((float)this.readShort() * 2.4414062E-4f);
            bones.putFloat(this.readShort());
            bones.putFloat((float)this.readShort() * 2.4414062E-4f);
            bones.putFloat((float)this.readShort() * 2.4414062E-4f);
            bones.putFloat((float)this.readShort() * 2.4414062E-4f);
            bones.putFloat(this.readShort());
            bones.putFloat((float)this.readShort() * 2.4414062E-4f);
            bones.putFloat((float)this.readShort() * 2.4414062E-4f);
            bones.putFloat((float)this.readShort() * 2.4414062E-4f);
            bones.putFloat(this.readShort());
            boneVertexSum += boneVertices;
        }
        bones.rewind();
        return boneVertexSum;
    }

    private Action.Bone readBoneAction(Action act, int mtxOffset) throws IOException {
        int type = this.readUByte();
        Action.Bone boneAction = new Action.Bone(type, mtxOffset, act.matrices);
        switch (type) {
            case 0: {
                float[] m = act.matrices;
                m[mtxOffset] = (float)this.readShort() * 2.4414062E-4f;
                m[mtxOffset + 1] = (float)this.readShort() * 2.4414062E-4f;
                m[mtxOffset + 2] = (float)this.readShort() * 2.4414062E-4f;
                m[mtxOffset + 3] = this.readShort();
                m[mtxOffset + 4] = (float)this.readShort() * 2.4414062E-4f;
                m[mtxOffset + 5] = (float)this.readShort() * 2.4414062E-4f;
                m[mtxOffset + 6] = (float)this.readShort() * 2.4414062E-4f;
                m[mtxOffset + 7] = this.readShort();
                m[mtxOffset + 8] = (float)this.readShort() * 2.4414062E-4f;
                m[mtxOffset + 9] = (float)this.readShort() * 2.4414062E-4f;
                m[mtxOffset + 10] = (float)this.readShort() * 2.4414062E-4f;
                m[mtxOffset + 11] = this.readShort();
                break;
            }
            case 1: {
                System.arraycopy(MathUtil.IDENTITY_AFFINE, 0, act.matrices, mtxOffset, 12);
                break;
            }
            case 2: {
                int count = this.readUShort();
                Action.Animation translate = new Action.Animation(count);
                for (int j = 0; j < count; ++j) {
                    int kf = this.readUShort();
                    short x = this.readShort();
                    short y = this.readShort();
                    short z = this.readShort();
                    translate.set(j, kf, x, y, z);
                }
                boneAction.translate = translate;
                count = this.readUShort();
                Action.Animation scale = new Action.Animation(count);
                for (int j = 0; j < count; ++j) {
                    int kf = this.readUShort();
                    float x = (float)this.readShort() * 2.4414062E-4f;
                    float y = (float)this.readShort() * 2.4414062E-4f;
                    float z = (float)this.readShort() * 2.4414062E-4f;
                    scale.set(j, kf, x, y, z);
                }
                boneAction.scale = scale;
                count = this.readUShort();
                Action.Animation rotate = new Action.Animation(count);
                for (int j = 0; j < count; ++j) {
                    int kf = this.readUShort();
                    float x = this.readShort();
                    float y = this.readShort();
                    float z = this.readShort();
                    rotate.set(j, kf, x, y, z);
                }
                boneAction.rotate = rotate;
                count = this.readUShort();
                Action.RollAnim roll = new Action.RollAnim(count);
                for (int j = 0; j < count; ++j) {
                    int kf = this.readUShort();
                    float r = (float)this.readShort() * 0.0015339808f;
                    roll.set(j, kf, r);
                }
                boneAction.roll = roll;
                break;
            }
            case 3: {
                Action.Animation translate = new Action.Animation(1);
                short transX = this.readShort();
                short transY = this.readShort();
                short transZ = this.readShort();
                translate.set(0, 0, transX, transY, transZ);
                boneAction.translate = translate;
                int count = this.readUShort();
                Action.Animation rotate = new Action.Animation(count);
                for (int j = 0; j < count; ++j) {
                    int kf = this.readUShort();
                    float x = this.readShort();
                    float y = this.readShort();
                    float z = this.readShort();
                    rotate.set(j, kf, x, y, z);
                }
                boneAction.rotate = rotate;
                float r = (float)this.readShort() * 0.0015339808f;
                Action.RollAnim roll = new Action.RollAnim(1);
                roll.set(0, 0, r);
                boneAction.roll = roll;
                break;
            }
            case 4: {
                int count = this.readUShort();
                Action.Animation rotate = new Action.Animation(count);
                for (int j = 0; j < count; ++j) {
                    int kf = this.readUShort();
                    float x = this.readShort();
                    float y = this.readShort();
                    float z = this.readShort();
                    rotate.set(j, kf, x, y, z);
                }
                boneAction.rotate = rotate;
                count = this.readUShort();
                Action.RollAnim roll = new Action.RollAnim(count);
                for (int j = 0; j < count; ++j) {
                    int kf = this.readUShort();
                    float r = (float)this.readShort() * 0.0015339808f;
                    roll.set(j, kf, r);
                }
                boneAction.roll = roll;
                break;
            }
            case 5: {
                int count = this.readUShort();
                Action.Animation rotate = new Action.Animation(count);
                for (int j = 0; j < count; ++j) {
                    int kf = this.readUShort();
                    float x = this.readShort();
                    float y = this.readShort();
                    float z = this.readShort();
                    rotate.set(j, kf, x, y, z);
                }
                boneAction.rotate = rotate;
                break;
            }
            case 6: {
                int count = this.readUShort();
                Action.Animation translate = new Action.Animation(count);
                for (int j = 0; j < count; ++j) {
                    int kf = this.readUShort();
                    short x = this.readShort();
                    short y = this.readShort();
                    short z = this.readShort();
                    translate.set(j, kf, x, y, z);
                }
                boneAction.translate = translate;
                count = this.readUShort();
                Action.Animation rotate = new Action.Animation(count);
                for (int j = 0; j < count; ++j) {
                    int kf = this.readUShort();
                    float x = this.readShort();
                    float y = this.readShort();
                    float z = this.readShort();
                    rotate.set(j, kf, x, y, z);
                }
                boneAction.rotate = rotate;
                count = this.readUShort();
                Action.RollAnim roll = new Action.RollAnim(count);
                for (int j = 0; j < count; ++j) {
                    int kf = this.readUShort();
                    float r = (float)this.readShort() * 0.0015339808f;
                    roll.set(j, kf, r);
                }
                boneAction.roll = roll;
                break;
            }
            default: {
                throw new RuntimeException("Animation type " + type + " is not supported");
            }
        }
        return boneAction;
    }

    private byte readByte() throws IOException {
        if (this.pos >= this.length) {
            throw new EOFException();
        }
        return this.data[this.pos++];
    }

    private int readUByte() throws IOException {
        if (this.pos >= this.length) {
            throw new EOFException();
        }
        return this.data[this.pos++] & 0xFF;
    }

    private short readShort() throws IOException {
        if (this.pos + 2 > this.length) {
            throw new EOFException();
        }
        return (short)(this.data[this.pos++] & 0xFF | this.data[this.pos++] << 8);
    }

    private int readUShort() throws IOException {
        if (this.pos + 2 > this.length) {
            throw new EOFException();
        }
        return this.data[this.pos++] & 0xFF | (this.data[this.pos++] & 0xFF) << 8;
    }

    private int readInt() throws IOException {
        if (this.pos + 4 > this.length) {
            throw new EOFException();
        }
        return this.data[this.pos++] & 0xFF | (this.data[this.pos++] & 0xFF) << 8 | (this.data[this.pos++] & 0xFF) << 16 | this.data[this.pos++] << 24;
    }

    private int available() {
        return this.length - this.pos;
    }

    private int readUBits(int size) throws IOException {
        if (size > 25) {
            System.out.println("readUBits(size=" + size + ')');
            throw new IllegalArgumentException("Invalid bit size=" + size);
        }
        while (size > this.cached) {
            this.cache |= this.readUByte() << this.cached;
            this.cached += 8;
        }
        int mask = ~(-1 << size);
        int result = this.cache & mask;
        this.cached -= size;
        this.cache >>>= size;
        return result;
    }

    private int readBits(int size) throws IOException {
        int lzb = 32 - size;
        return this.readUBits(size) << lzb >> lzb;
    }

    private void clearCache() {
        this.cache = 0;
        this.cached = 0;
    }

    private void skip(int n) throws EOFException {
        if (this.pos + n > this.length) {
            throw new EOFException();
        }
        this.pos += n;
    }
}


