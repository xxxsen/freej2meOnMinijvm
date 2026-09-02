/*
 * Reconstructed source for the fork's bundled FreeJ2ME MascotCapsule implementation.
 * Browser backend integration changes are maintained by j2me-web contributors.
 */
package com.mascotcapsule.micro3d.v3;

import com.mascotcapsule.micro3d.v3.Effect3D;
import com.mascotcapsule.micro3d.v3.Figure;
import com.mascotcapsule.micro3d.v3.FigureLayout;
import com.mascotcapsule.micro3d.v3.RenderProxy;
import com.mascotcapsule.micro3d.v3.Texture;
import com.mascotcapsule.micro3d.v3.base.Engine;
import com.mascotcapsule.micro3d.v3.base.Micro3dBackend;
import com.mascotcapsule.micro3d.v3.base.SoftwareMicro3dBackend;
import javax.microedition.lcdui.Graphics;
import org.recompile.mobile.PlatformGraphics;

public class Graphics3D {
    public static final int COMMAND_AFFINE_INDEX = -2030043136;
    public static final int COMMAND_AMBIENT_LIGHT = -1610612736;
    public static final int COMMAND_ATTRIBUTE = -2097152000;
    public static final int COMMAND_CENTER = -2063597568;
    public static final int COMMAND_CLIP = -2080374784;
    public static final int COMMAND_DIRECTION_LIGHT = -1593835520;
    public static final int COMMAND_END = Integer.MIN_VALUE;
    public static final int COMMAND_FLUSH = -2113929216;
    public static final int COMMAND_LIST_VERSION_1_0 = -33554431;
    public static final int COMMAND_NOP = -2130706432;
    public static final int COMMAND_PARALLEL_SCALE = -1879048192;
    public static final int COMMAND_PARALLEL_SIZE = -1862270976;
    public static final int COMMAND_PERSPECTIVE_FOV = -1845493760;
    public static final int COMMAND_PERSPECTIVE_WH = -1828716544;
    public static final int COMMAND_TEXTURE_INDEX = -2046820352;
    public static final int COMMAND_THRESHOLD = -1358954496;
    public static final int ENV_ATTR_LIGHTING = 1;
    public static final int ENV_ATTR_SPHERE_MAP = 2;
    public static final int ENV_ATTR_TOON_SHADING = 4;
    public static final int ENV_ATTR_SEMI_TRANSPARENT = 8;
    public static final int PATTR_BLEND_ADD = 64;
    public static final int PATTR_BLEND_HALF = 32;
    public static final int PATTR_BLEND_NORMAL = 0;
    public static final int PATTR_BLEND_SUB = 96;
    public static final int PATTR_COLORKEY = 16;
    public static final int PATTR_LIGHTING = 1;
    public static final int PATTR_SPHERE_MAP = 2;
    public static final int PDATA_COLOR_NONE = 0;
    public static final int PDATA_COLOR_PER_COMMAND = 1024;
    public static final int PDATA_COLOR_PER_FACE = 2048;
    public static final int PDATA_NORMAL_NONE = 0;
    public static final int PDATA_NORMAL_PER_FACE = 512;
    public static final int PDATA_NORMAL_PER_VERTEX = 768;
    public static final int PDATA_POINT_SPRITE_PARAMS_PER_CMD = 4096;
    public static final int PDATA_POINT_SPRITE_PARAMS_PER_FACE = 8192;
    public static final int PDATA_POINT_SPRITE_PARAMS_PER_VERTEX = 12288;
    public static final int PDATA_TEXURE_COORD = 12288;
    public static final int PDATA_TEXURE_COORD_NONE = 0;
    public static final int POINT_SPRITE_LOCAL_SIZE = 0;
    public static final int POINT_SPRITE_NO_PERS = 2;
    public static final int POINT_SPRITE_PERSPECTIVE = 0;
    public static final int POINT_SPRITE_PIXEL_SIZE = 1;
    public static final int PRIMITVE_LINES = 0x2000000;
    public static final int PRIMITVE_POINTS = 0x1000000;
    public static final int PRIMITVE_POINT_SPRITES = 0x5000000;
    public static final int PRIMITVE_QUADS = 0x4000000;
    public static final int PRIMITVE_TRIANGLES = 0x3000000;
    private static final String MINIJVM_BACKEND_FACTORY = "com.mascotcapsule.micro3d.v3.MiniJvmMicro3dFactory";
    private static final String BACKEND_FACTORY_PROPERTY = "freej2me.micro3d.backend.factory";
    private Graphics graphics;
    private final Engine engine;
    private final Micro3dBackend backend = Graphics3D.createBackend();

    public Graphics3D() {
        this.engine = new Engine(this.backend);
    }

    private static Micro3dBackend createBackend() {
        try {
            String factoryName = System.getProperty(BACKEND_FACTORY_PROPERTY);
            BackendFactory factory = createBackendFactory(factoryName);
            Micro3dBackend gl = factory.create();
            if (gl != null && gl.isAvailable()) {
                return gl;
            }
        }
        catch (Throwable throwable) {
            System.out.println("[J2ME_3D_V1] api=MASCOT backend=SOFTWARE event=fallback items=0 reason="
                    + throwable.getClass().getName());
        }
        return new SoftwareMicro3dBackend();
    }

    static BackendFactory createBackendFactory(String factoryName) throws Exception {
        if (factoryName == null || factoryName.length() == 0 || MINIJVM_BACKEND_FACTORY.equals(factoryName)) {
            // Keep the browser backend independent of the MIDlet class loader. Some
            // games install their own loader before first touching Graphics3D, which
            // makes reflective lookup of a platform class fail even though it is in
            // the adapter JAR.
            return new MiniJvmMicro3dFactory();
        } else {
            Class<?> factoryClass = Class.forName(factoryName);
            return (BackendFactory)factoryClass.newInstance();
        }
    }

    public final synchronized void bind(Graphics graphics) {
        this.bind(graphics, true);
    }

    public final synchronized void bind(PlatformGraphics graphics) {
        this.bind((Graphics)graphics, true);
    }

    public final synchronized void bind(Graphics graphics, boolean doClip) {
        if (graphics == null) {
            throw new NullPointerException("Argument 'Graphics' is NULL");
        }
        if (this.graphics != null) {
            throw new IllegalStateException("Target already bound");
        }
        this.graphics = graphics;
        this.backend.bind(graphics, doClip);
    }

    public final void dispose() {
    }

    public final void drawCommandList(Texture[] textures, int x, int y, FigureLayout layout, Effect3D effect, int[] commandList) {
        this.checkTargetIsValid();
        if (layout == null || effect == null || commandList == null) {
            throw new NullPointerException();
        }
        RenderProxy.getViewTrans(layout.affine, this.engine.getViewMatrix(), 0);
        RenderProxy.setTextureArray(this.engine, textures);
        RenderProxy.setAffineArray(this.engine, layout.affineArray);
        this.engine.setCenter(layout.centerX + x, layout.centerY + y);
        this.engine.resetEnvironmentSize();
        RenderProxy.setProjection(this.engine, layout);
        RenderProxy.setEffects(this.engine, effect);
        this.engine.drawCommandList(commandList);
    }

    public final void drawCommandList(Texture texture, int x, int y, FigureLayout layout, Effect3D effect, int[] commandList) {
        this.checkTargetIsValid();
        if (layout == null || effect == null || commandList == null) {
            throw new NullPointerException();
        }
        RenderProxy.getViewTrans(layout.affine, this.engine.getViewMatrix(), 0);
        RenderProxy.setAffineArray(this.engine, layout.affineArray);
        this.engine.setCenter(layout.centerX + x, layout.centerY + y);
        this.engine.resetEnvironmentSize();
        RenderProxy.setProjection(this.engine, layout);
        RenderProxy.setEffects(this.engine, effect);
        if (texture != null) {
            this.engine.setTexture(texture.impl);
        }
        this.engine.drawCommandList(commandList);
    }

    public final void drawFigure(Figure figure, int x, int y, FigureLayout layout, Effect3D effect) {
        this.checkTargetIsValid();
        if (figure == null || layout == null || effect == null) {
            throw new NullPointerException();
        }
        RenderProxy.getViewTrans(layout.affine, this.engine.getViewMatrix(), 0);
        this.engine.setCenter(layout.centerX + x, layout.centerY + y);
        this.engine.resetEnvironmentSize();
        RenderProxy.setProjection(this.engine, layout);
        RenderProxy.setEffects(this.engine, effect);
        Texture texture = figure.getTexture();
        if (texture != null) {
            this.engine.setTexture(texture.impl);
        }
        this.engine.postFigure(figure.impl);
        this.engine.flushFrame();
        this.engine.resetQueue();
    }

    public final void flush() {
        this.checkTargetIsValid();
        this.engine.flushItems();
        this.engine.resetQueue();
    }

    public final synchronized void release(Graphics graphics) {
        if (graphics == null) {
            throw new NullPointerException("Argument 'Graphics' is NULL");
        }
        if (graphics != this.graphics) {
            if (this.backend != null) {
                this.engine.resetQueue();
            }
            this.graphics = null;
            return;
        }
        this.engine.flushItems();
        this.engine.resetQueue();
        this.backend.release(graphics);
        this.graphics = null;
    }

    public final synchronized void release() {
        if (this.graphics == null) throw new NullPointerException();
        release(this.graphics);
    }

    public final void renderFigure(Figure figure, int x, int y, FigureLayout layout, Effect3D effect) {
        this.checkTargetIsValid();
        if (figure == null || layout == null || effect == null) {
            throw new NullPointerException();
        }
        RenderProxy.getViewTrans(layout.affine, this.engine.getViewMatrix(), 0);
        RenderProxy.setTextureArray(this.engine, figure.textures);
        this.engine.setCenter(layout.centerX + x, layout.centerY + y);
        this.engine.resetEnvironmentSize();
        RenderProxy.setProjection(this.engine, layout);
        RenderProxy.setEffects(this.engine, effect);
        this.engine.postFigure(figure.impl);
    }

    public final void renderPrimitives(Texture texture, int x, int y, FigureLayout layout, Effect3D effect, int command, int numPrimitives, int[] vertexCoords, int[] normals, int[] textureCoords, int[] colors) {
        this.checkTargetIsValid();
        if (layout == null || effect == null || vertexCoords == null || normals == null || textureCoords == null || colors == null) {
            throw new NullPointerException();
        }
        if (command < 0 || numPrimitives <= 0 || numPrimitives >= 256) {
            throw new IllegalArgumentException();
        }
        RenderProxy.getViewTrans(layout.affine, this.engine.getViewMatrix(), 0);
        this.engine.setCenter(layout.centerX + x, layout.centerY + y);
        this.engine.resetEnvironmentSize();
        RenderProxy.setProjection(this.engine, layout);
        RenderProxy.setEffects(this.engine, effect);
        if (texture != null) {
            this.engine.setTexture(texture.impl);
        }
        this.engine.postPrimitives(command | numPrimitives << 16, vertexCoords, 0, normals, 0, textureCoords, 0, colors, 0);
    }

    private void checkTargetIsValid() throws IllegalStateException {
        if (this.graphics == null) {
            throw new IllegalStateException("No target is bound");
        }
    }

    public static interface BackendFactory {
        public Micro3dBackend create();
    }
}
